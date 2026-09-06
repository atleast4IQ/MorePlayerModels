/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.ModelPartData;
import noppes.mpm.shared.client.model.NopModelPart;

public abstract class LayerInterface
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    protected AbstractClientPlayer player;
    protected ModelData playerdata;
    protected PlayerModel base;

    public LayerInterface(PlayerRenderer render) {
        super((RenderLayerParent)render);
        this.setBase((PlayerModel)this.getParentModel());
    }

    public void setBase(PlayerModel base) {
        this.base = base;
        this.createParts();
    }

    public void setColor(ModelPartData data, LivingEntity entity) {
    }

    protected void createParts() {
    }

    public ResourceLocation getResource(ModelPartData data) {
        if (data.playerTexture) {
            return SkinUtil.getTexture(this.player);
        }
        return data.getResource();
    }

    protected float red(ModelPartData data) {
        if (this.player.hurtTime > 0 || this.player.deathTime > 0) {
            return 1.0f;
        }
        return (float)(data.color >> 16 & 0xFF) / 255.0f;
    }

    protected float green(ModelPartData data) {
        if (this.player.hurtTime > 0 || this.player.deathTime > 0) {
            return 0.0f;
        }
        return (float)(data.color >> 8 & 0xFF) / 255.0f;
    }

    protected float blue(ModelPartData data) {
        if (this.player.hurtTime > 0 || this.player.deathTime > 0) {
            return 0.0f;
        }
        return (float)(data.color & 0xFF) / 255.0f;
    }

    protected float alpha() {
        if (this.player.hurtTime > 0 || this.player.deathTime > 0) {
            return 0.3f;
        }
        return 0.99f;
    }

    public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        if (this.base != this.getParentModel()) {
            this.setBase((PlayerModel)this.getParentModel());
        }
        AbstractClientPlayer previousPlayer = this.player;
        ModelData previousData = this.playerdata;
        try {
            this.player = player;
            this.playerdata = ModelData.get((Player)player);
            this.rotate(limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
            mStack.pushPose();
            try {
                if (player.isCrouching()) {
                    // empty if block
                }
                this.render(mStack, typeBuffer, lightmapUV, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
            } finally {
                mStack.popPose();
            }
        } finally {
            this.player = previousPlayer;
            this.playerdata = previousData;
        }
    }

    public void setRotation(NopModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    public abstract void render(PoseStack var1, MultiBufferSource var2, int var3, float var4, float var5, float var6, float var7, float var8, float var9);

    public abstract void rotate(float var1, float var2, float var3, float var4, float var5, float var6);
}
