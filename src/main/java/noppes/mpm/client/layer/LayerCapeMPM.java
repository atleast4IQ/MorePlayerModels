/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.CapeLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.ModelPartConfig;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.constants.EnumParts;

public class LayerCapeMPM
extends CapeLayer {
    private PlayerRenderer render;

    public LayerCapeMPM(PlayerRenderer render) {
        super((RenderLayerParent)render);
        this.render = render;
    }

    public void render(PoseStack mStack, MultiBufferSource renderTypeBuffer, int lightmapUV, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        ModelData data = ModelData.get((Player)player);
        ModelPartConfig config = data.getPartConfig(EnumParts.BODY);
        mStack.pushPose();
        try {
            if (player.isCrouching() && data.moveAnimation != EnumAnimation.CRAWL) {
                mStack.translate(0.0, 0.0, (double)(-2.0f + config.scaleZ) * 0.0625);
            }
            mStack.translate((double)config.transX, (double)config.transY, (double)config.transZ + (double)(-1.0f + config.scaleZ) * 0.0625);
            mStack.scale(config.scaleX, config.scaleY, 1.0f);
            if (data.moveAnimation == EnumAnimation.CRAWL) {
                int rotation = 78;
                if (player.isCrouching()) {
                    mStack.mulPose(Axis.XP.rotationDegrees(-25.0f));
                }
            }
            if (player.hurtTime > 0 || player.deathTime > 0) {
                // empty if block
            }
            super.render(noppes.mpm.client.RenderStateScope.copyPose(mStack), renderTypeBuffer, lightmapUV, player, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
        } finally {
            mStack.popPose();
        }
    }
}

