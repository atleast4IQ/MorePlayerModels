/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package noppes.mpm.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import noppes.mpm.ModelPartConfig;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.ClientProxy;
import noppes.mpm.constants.EnumParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ModelPart.class})
public class ModelRendererMixin {
    private ModelPartConfig mpmconfig;

    @Inject(at={@At(value="HEAD")}, method={"translateAndRotate"})
    private void translateAndRotatePre(PoseStack mStack, CallbackInfo callbackInfo) {
        if (MorePlayerModels.Compatibility) {
            return;
        }
        this.mpmconfig = this.getMpmconfig();
        if (this.mpmconfig != null) {
            mStack.translate(this.mpmconfig.transX, this.mpmconfig.transY, this.mpmconfig.transZ);
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"translateAndRotate"})
    private void translateAndRotatePost(PoseStack mStack, CallbackInfo callbackInfo) {
        if (MorePlayerModels.Compatibility) {
            return;
        }
        this.mpmconfig = this.getMpmconfig();
        if (this.mpmconfig != null) {
            mStack.scale(this.mpmconfig.scaleX, this.mpmconfig.scaleY, this.mpmconfig.scaleZ);
        }
    }

    private ModelPartConfig getMpmconfig() {
        // Armor layers are discovered lazily.  The base player model must not
        // depend on both variants being present: integrations can already use
        // MPM's transforms for their worn models while this early return leaves
        // the actual player at vanilla size.
        if (ClientProxy.data == null || ClientProxy.playerModel == null) {
            return null;
        }
        ModelPart model = (ModelPart)(Object)this;
        HumanoidModel armorOuter = ClientProxy.armorLayer == null ? null : (HumanoidModel)ClientProxy.armorLayer.getOuter();
        HumanoidModel armorInner = ClientProxy.armorLayer == null ? null : (HumanoidModel)ClientProxy.armorLayer.getInner();
        HumanoidModel slimArmorOuter = ClientProxy.armorLayerSlim == null ? null : (HumanoidModel)ClientProxy.armorLayerSlim.getOuter();
        HumanoidModel slimArmorInner = ClientProxy.armorLayerSlim == null ? null : (HumanoidModel)ClientProxy.armorLayerSlim.getInner();
        if (model == ClientProxy.playerModel.body || model == ClientProxy.playerModel.jacket || armorOuter != null && model == armorOuter.body || slimArmorOuter != null && model == slimArmorOuter.body || armorInner != null && model == armorInner.body || slimArmorInner != null && model == slimArmorInner.body) {
            return ClientProxy.data.getPartConfig(EnumParts.BODY);
        }
        if (model == ClientProxy.playerModel.head || model == ClientProxy.playerModel.hat || armorOuter != null && model == armorOuter.head || slimArmorOuter != null && model == slimArmorOuter.head) {
            return ClientProxy.data.getPartConfig(EnumParts.HEAD);
        }
        if (model == ClientProxy.playerModel.leftLeg || model == ClientProxy.playerModel.leftPants || armorOuter != null && model == armorOuter.leftLeg || armorInner != null && model == armorInner.leftLeg || slimArmorOuter != null && model == slimArmorOuter.leftLeg || slimArmorInner != null && model == slimArmorInner.leftLeg) {
            return ClientProxy.data.getPartConfig(EnumParts.LEG_LEFT);
        }
        if (model == ClientProxy.playerModel.rightLeg || model == ClientProxy.playerModel.rightPants || armorOuter != null && model == armorOuter.rightLeg || armorInner != null && model == armorInner.rightLeg || slimArmorOuter != null && model == slimArmorOuter.rightLeg || slimArmorInner != null && model == slimArmorInner.rightLeg) {
            return ClientProxy.data.getPartConfig(EnumParts.LEG_RIGHT);
        }
        if (model == ClientProxy.playerModel.leftArm || model == ClientProxy.playerModel.leftSleeve || armorOuter != null && model == armorOuter.leftArm || slimArmorOuter != null && model == slimArmorOuter.leftArm) {
            return ClientProxy.data.getPartConfig(EnumParts.ARM_LEFT);
        }
        if (model == ClientProxy.playerModel.rightArm || model == ClientProxy.playerModel.rightSleeve || armorOuter != null && model == armorOuter.rightArm || slimArmorOuter != null && model == slimArmorOuter.rightArm) {
            return ClientProxy.data.getPartConfig(EnumParts.ARM_RIGHT);
        }
        return null;
    }
}
