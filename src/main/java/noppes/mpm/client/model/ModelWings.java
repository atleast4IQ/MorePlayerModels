/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 */
package noppes.mpm.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import noppes.mpm.shared.client.model.NopModelPart;

public class ModelWings
extends Model {
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // Wings are rendered through render(Entity, ...) by the layer implementation.
    }
    public NopModelPart body;
    public NopModelPart head;
    public NopModelPart left_wing_1;
    public NopModelPart right_wing_1;
    public NopModelPart left_wing_2;
    public NopModelPart left_wing_0;
    public NopModelPart left_wing_3;
    public NopModelPart left_wing_4;
    public NopModelPart right_wing_2 = new NopModelPart(64, 64, 42, 0);
    public NopModelPart right_wing_0;
    public NopModelPart right_wing_3;
    public NopModelPart right_wing_4;

    public ModelWings() {
        super(RenderType::entityCutoutNoCull);
        this.right_wing_2.setPos(0.0f, 4.0f, -1.0f);
        this.right_wing_2.addBox(-1.0f, 0.0f, 0.0f, 2.0f, 7.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.right_wing_2, 1.2292354f, 0.0f, 0.0f);
        this.left_wing_3 = new NopModelPart(64, 64, 26, 0);
        this.left_wing_3.setPos(0.0f, 7.0f, 2.0f);
        this.left_wing_3.addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.left_wing_3, -1.2292354f, 0.0f, 0.0f);
        this.right_wing_1 = new NopModelPart(64, 64, 8, 0);
        this.right_wing_1.setPos(-2.4f, 2.0f, 1.5f);
        this.right_wing_1.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.right_wing_1, 1.5358897f, -0.9424778f, 0.0f);
        this.left_wing_0 = new NopModelPart(64, 64, 6, 0);
        this.left_wing_0.setPos(2.4f, 2.0f, 1.5f);
        this.left_wing_0.addBox(-3.4f, -2.0f, -15.0f, 1.0f, 11.0f, 18.0f, 0.0f);
        this.right_wing_3 = new NopModelPart(64, 64, 50, 0);
        this.right_wing_3.setPos(0.0f, 7.0f, 2.0f);
        this.right_wing_3.addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.right_wing_3, -1.2292354f, 0.0f, 0.0f);
        this.left_wing_2 = new NopModelPart(64, 64, 16, 0);
        this.left_wing_2.setPos(0.0f, 4.0f, -1.0f);
        this.left_wing_2.addBox(-1.0f, 0.0f, 0.0f, 2.0f, 7.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.left_wing_2, 1.2292354f, 0.0f, 0.0f);
        this.body = new NopModelPart(64, 64, 0, 0);
        this.body.setPos(0.0f, 0.0f, 0.0f);
        this.body.addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.head = new NopModelPart(64, 64, 0, 34);
        this.head.setPos(0.0f, 0.0f, 0.0f);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.left_wing_1 = new NopModelPart(64, 64, 0, 0);
        this.left_wing_1.setPos(2.4f, 2.0f, 1.5f);
        this.left_wing_1.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.left_wing_1, 1.5358897f, 0.9424778f, 0.0f);
        this.right_wing_4 = new NopModelPart(64, 64, 64, 0);
        this.right_wing_4.setPos(0.0f, 5.0f, 0.0f);
        this.right_wing_4.addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.right_wing_4, -1.1383038f, 0.0f, 0.0f);
        this.left_wing_4 = new NopModelPart(64, 64, 34, 0);
        this.left_wing_4.setPos(0.0f, 5.0f, 0.0f);
        this.left_wing_4.addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, 0.0f);
        this.setRotateAngle(this.left_wing_4, -1.1383038f, 0.0f, 0.0f);
        this.right_wing_0 = new NopModelPart(64, 64, 44, 0);
        this.right_wing_0.setPos(-2.4f, 2.0f, 1.5f);
        this.right_wing_0.addBox(2.4f, -2.0f, -15.0f, 1.0f, 11.0f, 18.0f, 0.0f);
        this.right_wing_1.addChild(this.right_wing_2);
        this.left_wing_2.addChild(this.left_wing_3);
        this.body.addChild(this.right_wing_1);
        this.left_wing_1.addChild(this.left_wing_0);
        this.right_wing_2.addChild(this.right_wing_3);
        this.left_wing_1.addChild(this.left_wing_2);
        this.body.addChild(this.left_wing_1);
        this.right_wing_3.addChild(this.right_wing_4);
        this.left_wing_3.addChild(this.left_wing_4);
        this.right_wing_1.addChild(this.right_wing_0);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, PoseStack mStack, VertexConsumer ivertex, int lightmapUV) {
        mStack.pushPose();
        try {
            if (entityIn.isCrouching()) {
                mStack.translate(0.0f, 0.2f, 0.0f);
            }
            this.renderWings(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, mStack, ivertex, lightmapUV);
        } finally {
            mStack.popPose();
        }
    }

    public void renderWings(Entity player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, PoseStack mStack, VertexConsumer ivertex, int lightmapUV) {
        float motion = Math.abs(Mth.sin((float)(limbSwing * 0.033f + (float)Math.PI)) * 0.4f) * limbSwingAmount;
        boolean flapWings = player.level().isEmptyBlock(player.blockPosition().below());
        float speed = 0.55f + 0.5f * motion;
        float y = Mth.sin((float)(ageInTicks * 0.35f));
        float flap = y * 0.5f * speed;
        mStack.pushPose();
        try {
            if (flapWings) {
                Axis.YP.rotationDegrees(flap * 20.0f);
            }
            this.left_wing_1.render(mStack, ivertex, lightmapUV, OverlayTexture.NO_OVERLAY);
        } finally {
            mStack.popPose();
        }
        mStack.pushPose();
        try {
            if (flapWings) {
                Axis.YP.rotationDegrees(-flap * 20.0f);
            }
            this.right_wing_1.render(mStack, ivertex, lightmapUV, OverlayTexture.NO_OVERLAY);
        } finally {
            mStack.popPose();
        }
    }

    public void setRotateAngle(NopModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer iVertexBuilder, int i, int i1, float v, float v1, float v2, float v3) {
    }
}
