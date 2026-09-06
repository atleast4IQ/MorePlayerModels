/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package noppes.mpm.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.SkinUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractClientPlayer.class})
public class AbstractClientPlayerEntityMixin {
    /**
     * In 1.21.1 the player skin is exposed as a {@link PlayerSkin} record.  A
     * renderer-specific texture override does not update this record, so mods
     * which correctly ask the player for its skin would continue to receive the
     * Mojang profile texture.  Preserve the profile's cape, elytra, model, and
     * security data while replacing only the texture after MPM has loaded one.
     */
    @Inject(at={@At(value="RETURN")}, method={"getSkin"}, cancellable=true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        Player player = (Player)(Object)this;
        ModelData data = ModelData.get(player);
        SkinUtil.load(data, player);
        if (!data.resourceLoaded || data.resourceLocation == null) {
            return;
        }

        PlayerSkin skin = cir.getReturnValue();
        if (data.resourceLocation.equals(skin.texture())) {
            return;
        }
        cir.setReturnValue(new PlayerSkin(data.resourceLocation, skin.textureUrl(), skin.capeTexture(), skin.elytraTexture(), skin.model(), skin.secure()));
    }
}
