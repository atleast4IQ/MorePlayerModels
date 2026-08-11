/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.ModelPartConfig;
import noppes.mpm.client.parts.ModelPartWrapper;
import noppes.mpm.client.parts.MpmPart;
import noppes.mpm.client.parts.MpmPartAbstractClient;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.client.parts.MpmPartDataClient;
import noppes.mpm.constants.BodyPart;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.constants.EnumParts;
import noppes.mpm.constants.PartBehaviorType;
import noppes.mpm.constants.PartRenderType;
import noppes.mpm.shared.util.NopVector3f;

public class LayerParts
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public LayerParts(PlayerRenderer render) {
        super((RenderLayerParent)render);
    }

    public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        ModelData data = ModelData.get((Player)player);
        for (MpmPartData part : new ArrayList<>(data.mpmParts)) {
            if (part == null) {
                continue;
            }
            MpmPart mp = part.getPart();
            if (!(mp instanceof MpmPartAbstractClient partc) || mp.renderType == PartRenderType.NONE || !mp.isEnabled) continue;
            if (!(part.clientData instanceof MpmPartDataClient)) {
                part.clientData = new MpmPartDataClient<>();
            }
            this.rotate((MpmPartDataClient)part.clientData, data, partc, player, (PlayerModel)this.getParentModel(), limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch);
            LayerParts.renderPart(part, partc, mStack, typeBuffer, lightmapUV, player, (PlayerModel)this.getParentModel(), data);
        }
        data.startMoveAnimation = false;
        data.startAnimation = false;
    }

    public static void renderPart(MpmPartData data, MpmPartAbstractClient partc, PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, AbstractClientPlayer player, PlayerModel model, ModelData pdata) {
        ModelPartConfig config;
        ModelPartWrapper lmodelPart;
        ModelPartWrapper rmodelPart;
        mStack.pushPose();
        boolean shouldRender = true;
        if (partc.bodyPart == BodyPart.HEAD) {
            model.head.translateAndRotate(mStack);
        }
        if (partc.bodyPart == BodyPart.BODY) {
            model.body.translateAndRotate(mStack);
        }
        if (partc.bodyPart == BodyPart.LEGS) {
            rmodelPart = partc.getPart("right_leg");
            lmodelPart = partc.getPart("left_leg");
            if (rmodelPart != null) {
                shouldRender = false;
                mStack.pushPose();
                config = pdata.getPartConfig(EnumParts.LEG_RIGHT);
                mStack.translate(0.0f, config.transY * 2.0f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
                if (lmodelPart != null) {
                    lmodelPart.setVisible(false);
                }
                rmodelPart.setVisible(true);
                partc.render(data, mStack, typeBuffer, lightmapUV, player);
                mStack.popPose();
            }
            if (lmodelPart != null) {
                shouldRender = false;
                mStack.pushPose();
                config = pdata.getPartConfig(EnumParts.LEG_LEFT);
                mStack.translate(0.0f, config.transY * 2.0f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
                if (rmodelPart != null) {
                    rmodelPart.setVisible(false);
                }
                lmodelPart.setVisible(true);
                partc.render(data, mStack, typeBuffer, lightmapUV, player);
                mStack.popPose();
            }
            if (shouldRender) {
                config = pdata.getPartConfig(EnumParts.LEG_LEFT);
                mStack.translate(0.0f, config.transY * 2.0f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
            }
        }
        if (partc.bodyPart == BodyPart.ARMS) {
            rmodelPart = partc.getPart("right_arm");
            lmodelPart = partc.getPart("left_arm");
            if (rmodelPart != null) {
                shouldRender = false;
                mStack.pushPose();
                config = pdata.getPartConfig(EnumParts.ARM_RIGHT);
                mStack.translate(0.0f, config.transY + (1.0f - config.scaleY) * 0.125f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
                if (lmodelPart != null) {
                    lmodelPart.setVisible(false);
                }
                rmodelPart.setVisible(true);
                partc.render(data, mStack, typeBuffer, lightmapUV, player);
                mStack.popPose();
            }
            if (lmodelPart != null) {
                shouldRender = false;
                mStack.pushPose();
                config = pdata.getPartConfig(EnumParts.ARM_LEFT);
                mStack.translate(0.0f, config.transY + (1.0f - config.scaleY) * 0.125f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
                if (rmodelPart != null) {
                    rmodelPart.setVisible(false);
                }
                lmodelPart.setVisible(true);
                partc.render(data, mStack, typeBuffer, lightmapUV, player);
                mStack.popPose();
            }
            if (shouldRender) {
                config = pdata.getPartConfig(EnumParts.ARM_LEFT);
                mStack.translate(0.0f, config.transY + (1.0f - config.scaleY) * 0.125f, 0.0f);
                mStack.scale(config.scaleX, config.scaleY, config.scaleZ);
            }
        }
        if (shouldRender) {
            partc.render(data, mStack, typeBuffer, lightmapUV, player);
        }
        mStack.popPose();
    }

    private void rotate(MpmPartDataClient partData, ModelData playerdata, MpmPartAbstractClient part, AbstractClientPlayer player, PlayerModel base, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        ModelPartWrapper modelPartL;
        ModelPartWrapper modelPart;
        ModelPartWrapper modelPart2;
        PlayerModel model;
        partData.animation(part, EnumAnimation.STATIC, (int)age, partialTicks);
        EnumAnimation moveAnimation = playerdata.getMoveAnimtion(player);
        if (playerdata.startMoveAnimation) {
            partData.start(part);
        }
        boolean didAnimation = false;
        if (playerdata.animation != EnumAnimation.NONE) {
            if (playerdata.startAnimation) {
                partData.start(part);
            }
            didAnimation = partData.animation(part, playerdata.animation, (int)age, partialTicks);
        }
        if (!(didAnimation || moveAnimation != EnumAnimation.IDLE && moveAnimation != EnumAnimation.FLY_IDLE)) {
            partData.animation(part, moveAnimation, (int)age, partialTicks);
        } else {
            partData.animation(part, moveAnimation, Mth.cos((float)(limbSwing * 0.6662f)) * limbSwingAmount / 2.0f + 0.5f);
        }
        if (part.animationType == PartBehaviorType.LEGS) {
            model = (PlayerModel)this.getParentModel();
            modelPart2 = part.getPart("right_leg");
            if (modelPart2 != null) {
                modelPart2.setRot(new NopVector3f(model.rightLeg.xRot, model.rightLeg.yRot, model.rightLeg.zRot));
                modelPart2.setPos(new NopVector3f(model.rightLeg.x, model.rightLeg.y, model.rightLeg.z));
            }
            if ((modelPart2 = part.getPart("left_leg")) != null) {
                modelPart2.setRot(new NopVector3f(model.leftLeg.xRot, model.leftLeg.yRot, model.leftLeg.zRot));
                modelPart2.setPos(new NopVector3f(model.leftLeg.x, model.leftLeg.y, model.leftLeg.z));
            }
        }
        if (part.animationType == PartBehaviorType.ARMS) {
            model = (PlayerModel)this.getParentModel();
            modelPart2 = part.getPart("right_arm");
            if (modelPart2 != null) {
                modelPart2.setRot(new NopVector3f(model.rightArm.xRot, model.rightArm.yRot, model.rightArm.zRot));
                modelPart2.setPos(new NopVector3f(model.rightArm.x, model.rightArm.y, model.rightArm.z));
            }
            if ((modelPart2 = part.getPart("left_arm")) != null) {
                modelPart2.setRot(new NopVector3f(model.leftArm.xRot, model.leftArm.yRot, model.leftArm.zRot));
                modelPart2.setPos(new NopVector3f(model.leftArm.x, model.leftArm.y, model.leftArm.z));
            }
        }
        if (part.animationType == PartBehaviorType.BEARD) {
            part.rot = part.rot.set(base.head.xRot < 0.0f ? 0.0f : -base.head.xRot, part.rot.y, part.rot.z);
        }
        if (part.animationType == PartBehaviorType.HAIR) {
            ModelPart head = base.head;
            if (head.xRot < 0.0f) {
                part.rot = part.rot.set(-head.xRot * 1.2f, part.rot.y, part.rot.z);
                if (head.xRot > -1.0f) {
                    part.pos = part.pos.set(part.pos.x, -head.xRot * 1.5f, -head.xRot * 1.5f);
                }
            } else {
                part.pos = NopVector3f.ZERO;
            }
        }
        if (part.animationType == PartBehaviorType.WINGS) {
            float xRot;
            float zRot;
            modelPart = part.getPart("right_wing");
            modelPartL = part.getPart("left_wing");
            if (player.level().isEmptyBlock(player.blockPosition().below())) {
                float motion = Math.abs(Mth.sin((float)(limbSwing * 0.033f + (float)Math.PI)) * 0.4f) * limbSwingAmount;
                float speed = 0.55f + 0.5f * motion;
                float y = Mth.sin((float)(age * 0.35f));
                xRot = zRot = y * 0.5f * speed;
            } else {
                zRot = Mth.cos((float)(age * 0.09f)) * 0.05f + 0.05f;
                xRot = Mth.sin((float)(age * 0.067f)) * 0.05f;
            }
            if (modelPart != null && modelPartL != null) {
                modelPart.setRot(modelPart.oriRot.add(xRot, xRot, zRot));
                modelPartL.setRot(modelPartL.oriRot.add(xRot, -xRot, -zRot));
            }
        }
        if (part.animationType == PartBehaviorType.WINGS2) {
            float yRot;
            modelPart = part.getPart("right_wing");
            modelPartL = part.getPart("left_wing");
            if (player.level().isEmptyBlock(player.blockPosition().below())) {
                float motion = Math.abs(Mth.sin((float)(limbSwing * 0.033f + (float)Math.PI)) * 0.4f) * limbSwingAmount;
                float speed = 0.55f + 0.5f * motion;
                float y = Mth.sin((float)(age * 0.35f));
                yRot = y * 0.5f * speed;
            } else {
                yRot = Mth.sin((float)(age * 0.07f)) * 0.44f;
            }
            if (modelPart != null && modelPartL != null) {
                modelPart.setRot(modelPart.oriRot.add(0.0f, yRot, 0.0f));
                modelPartL.setRot(modelPartL.oriRot.add(0.0f, -yRot, 0.0f));
            }
        }
    }
}
