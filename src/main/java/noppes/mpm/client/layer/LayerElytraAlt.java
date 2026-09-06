/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.ElytraLayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.ModelPartConfig;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.constants.EnumParts;

public class LayerElytraAlt<T extends LivingEntity, M extends EntityModel<T>>
extends ElytraLayer<T, M> {
    public LayerElytraAlt(RenderLayerParent<T, M> renderer, EntityModelSet set) {
        super(renderer, set);
    }

    public void render(PoseStack mStack, MultiBufferSource renderTypeBuffer, int lightmapUV, T entityLiving, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        if (!(entityLiving instanceof Player)) {
            super.render(noppes.mpm.client.RenderStateScope.copyPose(mStack), renderTypeBuffer, lightmapUV, entityLiving, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
            return;
        }
        Player player = (Player)entityLiving;
        ModelData data = ModelData.get(player);
        if (data.wingMode == 1) {
            return;
        }
        ModelPartConfig config = data.getPartConfig(EnumParts.BODY);
        mStack.pushPose();
        try {
            if (player.isCrouching() && data.moveAnimation != EnumAnimation.CRAWL) {
                mStack.translate(0.0f, 0.0f, (-2.0f + config.scaleZ) * 0.0625f);
            }
            mStack.translate(config.transX, config.transY, config.transZ + (-1.0f + config.scaleZ) * 0.0625f);
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
            super.render(noppes.mpm.client.RenderStateScope.copyPose(mStack), renderTypeBuffer, lightmapUV, entityLiving, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
        } finally {
            mStack.popPose();
        }
    }
}

