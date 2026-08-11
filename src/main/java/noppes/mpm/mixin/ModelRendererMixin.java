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
        // First-person arms can render before ClientProxy has discovered both
        // vanilla armor layers. Do not dereference those delayed renderers
        // during client startup.
        if (ClientProxy.data == null || ClientProxy.playerModel == null || ClientProxy.armorLayer == null || ClientProxy.armorLayerSlim == null) {
            return null;
        }
        ModelPart model = (ModelPart)(Object)this;
        if (model == ClientProxy.playerModel.body || model == ClientProxy.playerModel.jacket || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).body || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).body || model == ((HumanoidModel)ClientProxy.armorLayer.getInner()).body || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getInner()).body) {
            return ClientProxy.data.getPartConfig(EnumParts.BODY);
        }
        if (model == ClientProxy.playerModel.head || model == ClientProxy.playerModel.hat || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).head || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).head) {
            return ClientProxy.data.getPartConfig(EnumParts.HEAD);
        }
        if (model == ClientProxy.playerModel.leftLeg || model == ClientProxy.playerModel.leftPants || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).leftLeg || model == ((HumanoidModel)ClientProxy.armorLayer.getInner()).leftLeg || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).leftLeg || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getInner()).leftLeg) {
            return ClientProxy.data.getPartConfig(EnumParts.LEG_LEFT);
        }
        if (model == ClientProxy.playerModel.rightLeg || model == ClientProxy.playerModel.rightPants || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).rightLeg || model == ((HumanoidModel)ClientProxy.armorLayer.getInner()).rightLeg || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).rightLeg || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getInner()).rightLeg) {
            return ClientProxy.data.getPartConfig(EnumParts.LEG_RIGHT);
        }
        if (model == ClientProxy.playerModel.leftArm || model == ClientProxy.playerModel.leftSleeve || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).leftArm || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).leftArm) {
            return ClientProxy.data.getPartConfig(EnumParts.ARM_LEFT);
        }
        if (model == ClientProxy.playerModel.rightArm || model == ClientProxy.playerModel.rightSleeve || model == ((HumanoidModel)ClientProxy.armorLayer.getOuter()).rightArm || model == ((HumanoidModel)ClientProxy.armorLayerSlim.getOuter()).rightArm) {
            return ClientProxy.data.getPartConfig(EnumParts.ARM_RIGHT);
        }
        return null;
    }
}
