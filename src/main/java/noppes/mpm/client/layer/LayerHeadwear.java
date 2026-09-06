/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.client.layer.LayerInterface;
import noppes.mpm.client.layer.LayerPreRender;
import noppes.mpm.client.model.ModelHeadwear;
import noppes.mpm.shared.client.model.Model2DRenderer;

public class LayerHeadwear
extends LayerInterface
implements LayerPreRender {
    private final ModelHeadwear headwear = new ModelHeadwear();

    public LayerHeadwear(PlayerRenderer render) {
        super(render);
    }

    @Override
    public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        if (MorePlayerModels.HeadWearType != 1 || !this.base.head.visible || this.headwear == null) {
            return;
        }
        if (this.player.hurtTime > 0 || this.player.deathTime > 0) {
            // empty if block
        }
        this.base.head.translateAndRotate(mStack);
        ResourceLocation texture = SkinUtil.getTexture(this.player);
        ResourceLocation previousTexture = Model2DRenderer.textureOverride;
        try {
            Model2DRenderer.textureOverride = texture;
            VertexConsumer ivertex = typeBuffer.getBuffer(RenderType.entityTranslucent(texture));
            this.headwear.render(mStack, ivertex, lightmapUV, OverlayTexture.NO_OVERLAY);
        } finally {
            Model2DRenderer.textureOverride = previousTexture;
        }
    }

    @Override
    public void rotate(float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
    }

    @Override
    public void preRender(AbstractClientPlayer player) {
        boolean bl = this.base.hat.visible = this.base.head.visible && MorePlayerModels.HeadWearType != 1;
        if (!this.base.hat.visible) {
            this.headwear.config = null;
        }
    }
}
