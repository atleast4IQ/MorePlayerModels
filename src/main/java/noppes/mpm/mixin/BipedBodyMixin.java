/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package noppes.mpm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.ClientProxy;
import noppes.mpm.client.model.animation.AnimationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidModel.class})
public class BipedBodyMixin<T extends LivingEntity> {
    @WrapMethod(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
    private void mpm$animationScope(T entity, float swing, float amount, float age, float yaw, float pitch,
            Operation<Void> original) {
        var previousData = ClientProxy.data;
        var previousModel = ClientProxy.playerModel;
        try {
            original.call(entity, swing, amount, age, yaw, pitch);
        } finally {
            ClientProxy.data = previousData;
            ClientProxy.playerModel = previousModel;
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"setupAnim"})
    private void setupAnimPre(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo) {
        HumanoidModel bipedModel = (HumanoidModel)(Object)this;
        if (livingEntity instanceof Player && bipedModel instanceof PlayerModel) {
            Player playerEntity = (Player)livingEntity;
            ClientProxy.playerModel = (PlayerModel)bipedModel;
            ClientProxy.data = ModelData.get(playerEntity);
            AnimationHandler.animateBipedPre(ClientProxy.data, bipedModel, livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"setupAnim"})
    private void setupAnimPost(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo) {
        HumanoidModel bipedModel = (HumanoidModel)(Object)this;
        if (livingEntity instanceof Player && bipedModel instanceof PlayerModel) {
            AnimationHandler.animateBipedPost(ClientProxy.data, bipedModel, livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}

