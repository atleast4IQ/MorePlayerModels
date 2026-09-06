# 1.21.1 render lifecycle audit (2026-09-05)

## Confirmed crash path

Inspected the clean `MorePlayerModels` checkout, the locally resolved NeoForge 21.1.242
patched Minecraft source, and the installed Tethered Prism instance. The installed
Cataclysm 1.21.1-3.32 bytecode supplies the missing causal link:

1. `EntityRenderDispatcher.render` pushes its frame on the world's supplied stack.
2. `LivingEntityRenderer.render` dispatches `RenderLivingEvent.Pre` before vanilla's
   living-render push.
3. Cataclysm's normally registered `ClientEvent.onPreRenderEntity` checks
   `ClientProxy.blockedEntityRenders`. For a blocked entity that is not classified
   as its first-person player, it **synchronously dispatches Post inside Pre**, then
   cancels Pre and removes the UUID from that collection. Bytecode offsets 461–552;
   Post construction at 495–523, dispatch at 526, cancellation at 534.
4. The old MPM NORMAL Post subscriber sees an `AbstractClientPlayer` and pops, although
   its LOWEST Pre subscriber has not run yet. This removes the dispatcher's frame.
5. Canceled Pre prevents MPM's push and the normal living render. The dispatcher then
   performs its own pop, removing the root. The next entity's dispatcher push fails
   at `ArrayDeque.getLast`.

The instance's September 5 reports consistently show this last push failure, with
varying next entities (players, arrows, items, and Maledictus). In
`crash-2026-09-05_20.42.17-client.txt`, Maledictus has the local player as a passenger.
The reports alone do not capture the earlier unmatched pop; the installed bytecode
and event ordering establish it. No special-case check for Cataclysm or Sable is needed.

Ordinary NeoForge cancellation alone would NOT produce that unmatched pop: canceled
Living Pre returns without Living Post. Cancellation after the old MPM Pre instead
leaked its push. Exceptions also skipped Post. The old custom replacement path
canceled Pre, rendered recursively, and popped locally without finally.

## New lifetime and ownership

* MPM no longer subscribes to Living Pre or Post. A required WrapOperation at the
  actual Living Pre dispatch runs preparation only after all listeners finish and
  only if the final event is uncanceled. Synthetic Post events have no MPM cleanup.
* A composable WrapMethod surrounds the living render, including events, early
  returns and exceptions. It passes a private PoseStack initialized from the incoming
  pose/normal matrices. No MPM push or pop touches the caller's stack at this boundary.
  Recursive replacements also receive private stacks. This is scope isolation, not
  root replenishment or a depth-reset recovery mechanism; exceptions still propagate.
* Player render is separately wrapped before `setModelProperties`, so recursive
  calls using the same PlayerRenderer cannot overwrite the outer snapshot before it
  is captured. First-person `renderHand` has its own scope.
* Each call captures its own player rotations/eye height, model flags and complete
  player-part transforms/visibility, and previous ClientProxy model/data references.
  Finally restores them on every exit. No per-render snapshots live on renderer instances.
* All remaining MPM push/pop pairs are lexical try/finally scopes. Calls into
  recursive entity renderers and delegated item/cape/elytra rendering use private
  stacks so exceptional foreign scopes do not interfere with MPM's enclosing pose.
* GUI previews preserve dispatcher camera orientation and the actual previous shadow
  flag, restore entity rotations and lighting, and isolate their entity-render poses.
  The inventory preview uses vanilla's same matrix operations, explicitly scoped.

## Additional concrete fixes

* Removed shared `mpmPartStates` and twelve visibility snapshot fields whose HEAD/TAIL
  lifetime failed on canceled renders, exceptions, and renderer reuse.
* GUI texture overrides now have an entity target and lexical lifetime; they are no
  longer a global one-shot value consumed by whichever entity asks first.
* Restored previous Model2D texture overrides in simple addons/headwear, including
  nested renders and exceptions. Non-skin addons explicitly select no override.
* Scoped addon animation transforms and bilateral visibility, preview model flags,
  and shared eyelid/brow scales. Layer player/data references restore the outer call.
* Removed immediate shader/color writes from buffered chat rendering; its RenderTypes
  already own shader selection. Scoped the legacy immediate Model2D shader, texture,
  and texture-matrix changes.
* Replaced MPM uses of deprecated `RenderSystem.runAsFancy` with the same behavior
  protected by finally (vanilla's implementation leaves graphics mode changed on error).
* Camera eye height is updated as persistent client tick state rather than as a
  side effect of a potentially canceled first-person hand render. Temporary body-render
  adjustments are restored by the render scope.
* Removed obsolete `getModelName`/`getSkinTextureLocation` injections: those methods
  are absent in 1.21.1; the existing dispatcher/model selection and getSkin hooks replace them.
* Removed the empty `minecraft:` headwear texture placeholder that caused invalid-path
  errors on startup; its texture is supplied at render time. Closed Model2D image streams.
* Mixin compatibility level now matches the project's Java 21 bytecode.

## Animation resources

`AssetsFinder.find("animations", ".json")` enumerates every resource namespace.
The old loader passed every JSON object directly to the MPM enum-keyed parser, so
Bedrock's `format_version` became `EnumAnimation.FORMAT_VERSION`.

Discovery now recognizes MPM's direct animation keys and rejects the Bedrock/GeckoLib
`format_version`/`animations` envelope before invoking the animation parser. Legacy
MPM packs in other namespaces remain supported. Recognized malformed MPM documents
still reach strict validation and are logged with their resource location; unrelated
formats are not treated as malformed MPM. Readers are closed with try-with-resources;
resource failures catch Exception rather than swallowing VM Errors. Enum key conversion
uses Locale.ROOT.

## Focused inventory and limits

Reviewed all pose sites in RenderEvent, PlayerRendererMixin, ChatMessages, LayerParts,
LayerInterface, LayerCapeMPM, LayerElytraAlt, ModelWings, MpmPartSimple, MpmPartBedrock,
MpmPartEyes, NopModelPart, Model2DRenderer, GuiCreationNewParts, GuiNPCInterface and
GuiCustomScroll; all rendering mixins; ClientEventHandler render/tick hooks; addon
animation state and resource loading.

Intentional persistent state is not rolled back: attachment/configuration data,
per-player animation progress/start flags, texture caches, and camera controller state.
CameraMixin only computes/returns zoom; it has no temporary camera mutation to pair.
ModelRendererMixin applies transforms to its caller's pose; it does not own/pop that pose.
BipedBodyMixin still assigns the current model/data for model-part scaling, but the
surrounding player/living/hand scopes now restore these references for nested rendering.
Humanoid setupAnim also has a finally scope so reentrant setup calls restore the outer context.

Suspicious legacy paths left intact: BatchRenderer.draw only clears an unused queue
(no in-project add callers); the immediate Model2D cached drawing overload appears
unused; animation inheritance still uses legacy unqualified names; development startup
logs contain optional shader-uniform warnings. No demonstrated
connection to the pose crash justified a broader rewrite. This patch does not repair
arbitrary stack corruption introduced entirely by another mod outside MPM's scopes.

## Verification matrix

| Case | Evidence |
| --- | --- |
| Normal player / normal Pre + Post | Required call-site hook and wrapped method inspected; private-pose tests; client mixin bootstrap |
| Pre canceled before preparation | Preparation checks final cancellation; no MPM pose ownership through events |
| Cancellation by later listener | Preparation runs after the entire event dispatch; wrapped finally always executes |
| Synthetic Post during Pre | Installed Cataclysm bytecode traced; MPM Post subscriber removed entirely |
| Custom replacement / recursive entity render | Per-call local replacement and isolated recursive stack; exception propagation preserved |
| Nested players / shared renderer / multiple players | Player wrapper precedes setModelProperties; nested model snapshot tests |
| Passenger / local first-person body | No mount-specific branch in ownership; same player/living scopes |
| First-person hand | Separate wrapped hand call; persistent eye height does not depend on hand event |
| Exception in custom rendering | Private-stack callback exception test; finally restores temporary state |
| GUI and entity previews | Private entity stacks and scoped dispatcher, texture, graphics mode and rotations |
| Foreign animations on reload | Format tests plus strict parsing of all bundled animation libraries |

JVM tests validate isolated normal/canceled/nested/exceptional callbacks, scaled normals,
full nested ModelPart snapshots, foreign animation rejection, malformed MPM validation,
and bundled libraries. These are not an automated boss-fight or in-game visual test.
The actual modpack fight, mounted first-person views and GUI visuals still need an
in-game acceptance run with the rebuilt JAR. The Prism instance was inspected read-only.

## Inspected artifact fingerprints

* `moreplayermodels-1.0.5.jar`: SHA-256 `c0867f8dc88821c02dd0ecbcf6e7de02985aded8dde06c0763004050bb2f8968`
* `L_Ender's Cataclysm 1.21.1-3.32.jar`: SHA-256 `679c8687281cdac01e80de1672f27db1baf6ad5c68c74d752806b9d5ce246ac3`
* `sable-neoforge-1.21.1-2.0.3.jar.disabled`: SHA-256 `da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279`
* `sable-neoforge-1.21.1-2.0.3-entity-render-fix.jar`: SHA-256 `0d94d86b2e22a3835f261f5d11b8ffe4e156870d1da040bc117d489be507590b`

## Files changed

* `build.gradle`
* `docs/render-lifecycle-audit.md`
* `src/main/java/noppes/mpm/client/ChatMessages.java`
* `src/main/java/noppes/mpm/client/ClientEventHandler.java`
* `src/main/java/noppes/mpm/client/RenderEvent.java`
* `src/main/java/noppes/mpm/client/RenderStateScope.java`
* `src/main/java/noppes/mpm/client/gui/GuiCreationLoad.java`
* `src/main/java/noppes/mpm/client/gui/GuiCreationNewParts.java`
* `src/main/java/noppes/mpm/client/gui/GuiCreationOptions.java`
* `src/main/java/noppes/mpm/client/gui/GuiMPM.java`
* `src/main/java/noppes/mpm/client/gui/select/GuiTextureSelection.java`
* `src/main/java/noppes/mpm/client/gui/util/GuiCustomScroll.java`
* `src/main/java/noppes/mpm/client/gui/util/GuiNPCInterface.java`
* `src/main/java/noppes/mpm/client/layer/LayerBackItem.java`
* `src/main/java/noppes/mpm/client/layer/LayerCapeMPM.java`
* `src/main/java/noppes/mpm/client/layer/LayerElytraAlt.java`
* `src/main/java/noppes/mpm/client/layer/LayerHeadwear.java`
* `src/main/java/noppes/mpm/client/layer/LayerInterface.java`
* `src/main/java/noppes/mpm/client/layer/LayerParts.java`
* `src/main/java/noppes/mpm/client/model/ModelHeadwear.java`
* `src/main/java/noppes/mpm/client/model/ModelWings.java`
* `src/main/java/noppes/mpm/client/parts/ModelPartWrapper.java`
* `src/main/java/noppes/mpm/client/parts/MpmAnimationFormat.java`
* `src/main/java/noppes/mpm/client/parts/MpmPart.java`
* `src/main/java/noppes/mpm/client/parts/MpmPartAbstractClient.java`
* `src/main/java/noppes/mpm/client/parts/MpmPartBedrock.java`
* `src/main/java/noppes/mpm/client/parts/MpmPartEyes.java`
* `src/main/java/noppes/mpm/client/parts/MpmPartReader.java`
* `src/main/java/noppes/mpm/client/parts/MpmPartSimple.java`
* `src/main/java/noppes/mpm/mixin/AbstractClientPlayerEntityMixin.java`
* `src/main/java/noppes/mpm/mixin/BipedBodyMixin.java`
* `src/main/java/noppes/mpm/mixin/EntityRenderDispatcherAccess.java`
* `src/main/java/noppes/mpm/mixin/LivingRendererMixin.java`
* `src/main/java/noppes/mpm/mixin/PlayerRendererMixin.java`
* `src/main/java/noppes/mpm/shared/client/model/Model2DRenderer.java`
* `src/main/java/noppes/mpm/shared/client/model/NopModelPart.java`
* `src/main/resources/moreplayermodels.mixins.json`
* `src/test/java/noppes/mpm/client/RenderStateScopeTest.java`
* `src/test/java/noppes/mpm/client/parts/MpmAnimationFormatTest.java`

## Final validation

* `./gradlew build --console=plain`: successful; six JVM tests, zero failures/errors.
* `git diff --check`: clean. All 25 remaining push sites have local finally-protected pops.
* Development client bootstrap on NeoForge 21.1.242 loaded LivingRendererMixin,
  PlayerRendererMixin and BipedBodyMixin, completed resource reload, and reported no
  injection failures. The empty resource-path errors are gone. Optional shader-uniform
  warnings and the environment's missing narrator library remain unrelated limitations.
* Inspected `build/libs/moreplayermodels-1.0.5.jar`: includes the new render scope,
  animation discriminator and dispatcher accessor; excludes test classes.
* No Prism instance files were changed, and no boss-fight/visual acceptance is claimed.

## Command permission audit

`/mpm` changes targeted players' model data, including URLs, custom entities and NBT,
display names, copied models, animations, and scales. It was registered at permission
level 0, so every player could execute these administrative operations. The root now
requires vanilla command permission level 2. MPM's standalone emote and particle
commands deliberately remain level 0 because they operate only on their caller.
