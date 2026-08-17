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

@Mixin(ModelPart.class)
public class ModelRendererMixin {
    @Inject(at = @At("TAIL"), method = "translateAndRotate")
    private void scalePart(PoseStack poseStack, CallbackInfo callbackInfo) {
        if (MorePlayerModels.Compatibility) {
            return;
        }
        ModelPartConfig config = getMpmConfig();
        if (config != null) {
            poseStack.scale(config.scaleX, config.scaleY, config.scaleZ);
        }
    }

    private ModelPartConfig getMpmConfig() {
        if (ClientProxy.data == null || ClientProxy.playerModel == null) {
            return null;
        }
        ModelPart model = (ModelPart)(Object)this;
        HumanoidModel<?> armorOuter = ClientProxy.armorLayer == null ? null : ClientProxy.armorLayer.getOuter();
        HumanoidModel<?> armorInner = ClientProxy.armorLayer == null ? null : ClientProxy.armorLayer.getInner();
        HumanoidModel<?> slimArmorOuter = ClientProxy.armorLayerSlim == null ? null : ClientProxy.armorLayerSlim.getOuter();
        HumanoidModel<?> slimArmorInner = ClientProxy.armorLayerSlim == null ? null : ClientProxy.armorLayerSlim.getInner();
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
