/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicate
 *  com.google.common.base.Predicates
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.client.event.InputEvent$Key
 *  net.neoforged.neoforge.client.event.InputEvent$MouseScrollingEvent
 *  net.neoforged.neoforge.event.PlayLevelSoundEvent$AtEntity
 *  net.neoforged.neoforge.event.TickEvent$ClientTickEvent
 *  net.neoforged.neoforge.event.TickEvent$Phase
 *  net.neoforged.neoforge.event.TickEvent$PlayerTickEvent
 *  net.neoforged.neoforge.event.TickEvent$RenderTickEvent
 *  net.neoforged.neoforge.event.entity.living.LivingAttackEvent
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.LogicalSide
 */
package noppes.mpm.client;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import noppes.mpm.ModelData;
import noppes.mpm.ModelEyeData;
import noppes.mpm.ModelPartData;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.ServerTickHandler;
import noppes.mpm.client.ClientProxy;
import noppes.mpm.client.MpmCamera;
import noppes.mpm.client.MpmKeys;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.client.fx.EntityEnderFX;
import noppes.mpm.client.gui.GuiCreationScreenInterface;
import noppes.mpm.client.gui.GuiMPM;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.mixin.LivingEntityMixin;
import noppes.mpm.packets.Packets;
import noppes.mpm.packets.server.PacketAnimationUpdate;
import noppes.mpm.packets.server.PacketPing;
import noppes.mpm.sync.WebApi;
import noppes.mpm.util.MPMEntityUtil;

public class ClientEventHandler {
    public static float partialTick = 0.0f;
    private long lastAltClick = 0L;
    private boolean altIsPressed = false;
    private Level prevWorld;
    private static final Predicate<Player> playerSelector = Predicates.and((Predicate[])new Predicate[]{new Predicate<Player>(){
        final double range = 6400.0;

        public boolean apply(Player entity) {
            return entity != Minecraft.getInstance().player && entity.distanceToSqr((Entity)Minecraft.getInstance().player) <= 6400.0;
        }
    }});
    public static List<Player> playerList;
    private static final ResourceLocation female_death;
    private static final ResourceLocation female_hurt;
    private static final ResourceLocation female_attack;
    private static final ResourceLocation male_death;
    private static final ResourceLocation male_hurt;
    private static final ResourceLocation male_attack;
    private static final ResourceLocation goblin_death;
    private static final ResourceLocation goblin_hurt;
    private static final ResourceLocation goblin_attack;
    public static MpmCamera camera;

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
        LocalPlayer player;
        block18: {
            block17: {
                Entity entity = event.getEntity();
                if (!(entity instanceof LocalPlayer)) break block17;
                player = (LocalPlayer)entity;
                if (event.getSound() != null) break block18;
            }
            return;
        }
        ModelData data = ModelData.get((Player)player);
        if (data == null || data.soundType == 0) {
            return;
        }
        ResourceLocation sound = null;
        if (event.getSound().is(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.PLAYER_HURT)) && !player.isDeadOrDying() && !((LivingEntityMixin)player).isDead()) {
            if (data.soundType == 1) {
                sound = female_hurt;
            } else if (data.soundType == 2) {
                sound = male_hurt;
            } else if (data.soundType == 3) {
                sound = goblin_hurt;
            }
        }
        if (event.getSound().is(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.PLAYER_DEATH))) {
            if (data.soundType == 1) {
                sound = female_death;
            } else if (data.soundType == 2) {
                sound = male_death;
            } else if (data.soundType == 3) {
                sound = goblin_death;
            }
        }
        if (sound != null) {
            event.setSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvent.createVariableRangeEvent((ResourceLocation)sound)));
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        ModelData data = ModelData.get((Player)player);
        if (data == null || data.soundType == 0) {
            return;
        }
        ResourceLocation sound = null;
        if (data.soundType == 1) {
            sound = female_attack;
        } else if (data.soundType == 2) {
            sound = male_attack;
        } else if (data.soundType == 3) {
            sound = goblin_attack;
        }
        if (sound != null) {
            float pitch = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2f + 1.0f;
            player.playSound(SoundEvent.createVariableRangeEvent((ResourceLocation)sound), 0.9876543f, pitch);
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        if (MpmKeys.Screen.isDown()) {
            ModelData data = ModelData.get((Player)mc.player);
            data.setAnimation(EnumAnimation.NONE);
            if (mc.screen == null) {
                mc.setScreen((Screen)new GuiMPM());
            }
        }
        if (mc.screen == null) {
            // empty if block
        }
        if (!mc.isWindowActive()) {
            return;
        }
        if (MpmKeys.MPM1.isDown()) {
            ClientEventHandler.processAnimation(MorePlayerModels.button1);
        }
        if (MpmKeys.MPM2.isDown()) {
            ClientEventHandler.processAnimation(MorePlayerModels.button2);
        }
        if (MpmKeys.MPM3.isDown()) {
            ClientEventHandler.processAnimation(MorePlayerModels.button3);
        }
        if (MpmKeys.MPM4.isDown()) {
            ClientEventHandler.processAnimation(MorePlayerModels.button4);
        }
        if (MpmKeys.MPM5.isDown()) {
            ClientEventHandler.processAnimation(MorePlayerModels.button5);
        }
        InputConstants.Key i = InputConstants.Type.KEYSYM.getOrCreate(event.getKey());
        if (MpmKeys.Camera.isDown() && mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            long time = System.currentTimeMillis();
            if (!this.altIsPressed) {
                if (time - this.lastAltClick < 400L) {
                    camera.reset();
                } else {
                    camera.enable();
                    this.lastAltClick = time;
                }
            }
            this.altIsPressed = true;
        } else if (this.altIsPressed) {
            this.altIsPressed = false;
        }
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.isWindowActive() && mc.gameRenderer.getMainCamera().isDetached() && ClientEventHandler.camera.enabled && this.altIsPressed)) {
            return;
        }
        ClientEventHandler.camera.cameraDistance = (float)((double)ClientEventHandler.camera.cameraDistance - event.getScrollDeltaY());
        if (ClientEventHandler.camera.cameraDistance > 14.0f) {
            ClientEventHandler.camera.cameraDistance = 14.0f;
        } else if (ClientEventHandler.camera.cameraDistance < 1.0f) {
            ClientEventHandler.camera.cameraDistance = 1.0f;
        }
        event.setCanceled(true);
    }

    public static void processAnimation(int type) {
        if (type < 0 || type >= EnumAnimation.values().length || Minecraft.getInstance().player == null) {
            return;
        }
        PacketAnimationUpdate.setAnimation((Player)Minecraft.getInstance().player, EnumAnimation.values()[type]);
    }

    @SubscribeEvent
    public void onRenderTick(RenderFrameEvent.Pre event) {
        partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        camera.update(true);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            if (this.prevWorld != null) {
                this.prevWorld = null;
                SkinUtil.lastSkinTick = -20L;
            }
            MorePlayerModels.HasServerSide = false;
            return;
        }
        if (mc.player == null) {
            return;
        }
        if (this.prevWorld != mc.level) {
            GuiCreationScreenInterface.Message = "message.noserver";
            ModelData data = ModelData.get((Player)mc.player);
            MorePlayerModels.HasServerSide = true;
            Packets.sendServer(new PacketPing(MorePlayerModels.Version, data.writeToNBT()));
            MorePlayerModels.HasServerSide = false;
            this.prevWorld = mc.level;
        }
        // Camera height is persistent client state, independent of whether a hand/body is rendered.
        ModelData cameraData = ModelData.get(mc.player);
        ((noppes.mpm.mixin.EntityMixin)mc.player).setEyeHeight(
                mc.player.getEyeHeight(mc.player.getPose()) - cameraData.getOffsetCamera(mc.player));
        ++SkinUtil.lastSkinTick;
        if (mc.level.getLevelData().getGameTime() % 20L == 0L) {
            playerList = mc.level.players().stream().filter(playerSelector).collect(Collectors.toList());
            WebApi.instance.run();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide == false) {
            return;
        }
        ClientProxy.data = null;
        Player player = event.getEntity();
        ModelData data = ModelData.get(player);
        LivingEntity entity = data.getEntity(player);
        Minecraft mc = Minecraft.getInstance();
        if (entity != null) {
            MPMEntityUtil.copy((LivingEntity)player, entity);
        }
        if (!MorePlayerModels.HasServerSide && entity == null) {
            for (MpmPartData pd : data.mpmParts) {
                if (!(pd instanceof ModelEyeData)) continue;
                ((ModelEyeData)pd).update(player);
            }
        }
        if (data.inLove > 0) {
            --data.inLove;
            if (player.getRandom().nextBoolean()) {
                double d0 = player.getRandom().nextGaussian() * 0.02;
                double d1 = player.getRandom().nextGaussian() * 0.02;
                double d2 = player.getRandom().nextGaussian() * 0.02;
                player.level().addParticle((ParticleOptions)ParticleTypes.HEART, player.getX() + (double)(player.getRandom().nextFloat() * player.getBbWidth() * 2.0f) - (double)player.getBbWidth(), player.getY() + 0.5 + (double)(player.getRandom().nextFloat() * player.getBbHeight()), player.getZ() + (double)(player.getRandom().nextFloat() * player.getBbWidth() * 2.0f) - (double)player.getBbWidth(), d0, d1, d2);
            }
        }
        if (data.animation == EnumAnimation.CRY) {
            float f1 = player.getXRot() * (float)Math.PI / 180.0f;
            float dx = -Mth.sin((float)f1);
            float dz = Mth.cos((float)f1);
            float width = entity == null ? player.getBbWidth() : entity.getBbWidth();
            int i = 0;
            while ((float)i < 10.0f) {
                float f2 = (player.getRandom().nextFloat() - 0.5f) * width * 0.5f + dx * 0.15f;
                float f3 = (player.getRandom().nextFloat() - 0.5f) * width * 0.5f + dz * 0.15f;
                player.level().addParticle((ParticleOptions)ParticleTypes.SPLASH, player.getX() + (double)f2, player.getY() - (double)data.getBodyY() + (double)1.1f - 0.0, player.getZ() + (double)f3, (double)1.0E-25f, 0.0, (double)1.0E-25f);
                ++i;
            }
        }
        ServerTickHandler.checkMovementAnimation(player, data);
        if (data.animation != EnumAnimation.NONE) {
            ServerTickHandler.checkAnimation(player, data);
        }
        if (data.animation == EnumAnimation.DEATH) {
            if (player.deathTime == 0) {
                player.playSound(SoundEvents.GENERIC_HURT, 1.0f, 1.0f);
            }
            if (player.deathTime < 19) {
                ++player.deathTime;
            }
        }
        if (data.prevAnimation != data.animation && data.prevAnimation == EnumAnimation.DEATH && !player.isDeadOrDying()) {
            player.deathTime = 0;
        }
        data.prevMoveAnimation = data.moveAnimation;
        data.prevAnimation = data.animation;
        data.prevPosX = player.getX();
        data.prevPosY = player.getY();
        data.prevPosZ = player.getZ();
        ModelPartData particles = null;
        if (particles != null) {
            this.spawnParticles(player, data, particles);
        }
    }

    private void spawnParticles(Player player, ModelData data, ModelPartData particles) {
        if (!MorePlayerModels.EnableParticles) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        double height = 0.0 + (double)data.getBodyY();
        RandomSource rand = player.getRandom();
        for (int i = 0; i < 2; ++i) {
            EntityEnderFX fx = new EntityEnderFX((AbstractClientPlayer)player, player.getRandomX(0.5), player.getRandomY() - height - 0.25, player.getRandomZ(0.5), (rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0, particles);
            minecraft.particleEngine.add((Particle)fx);
        }
    }

    static {
        female_death = ResourceLocation.parse("moreplayermodels:human.female.death");
        female_hurt = ResourceLocation.parse("moreplayermodels:human.female.hurt");
        female_attack = ResourceLocation.parse("moreplayermodels:human.female.attack");
        male_death = ResourceLocation.parse("moreplayermodels:human.male.death");
        male_hurt = ResourceLocation.parse("moreplayermodels:human.male.hurt");
        male_attack = ResourceLocation.parse("moreplayermodels:human.male.attack");
        goblin_death = ResourceLocation.parse("moreplayermodels:goblin.male.death");
        goblin_hurt = ResourceLocation.parse("moreplayermodels:goblin.male.hurt");
        goblin_attack = ResourceLocation.parse("moreplayermodels:goblin.male.attack");
        camera = new MpmCamera();
    }
}
