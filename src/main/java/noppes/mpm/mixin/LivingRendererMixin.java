/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package noppes.mpm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import noppes.mpm.client.RenderStateScope;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.RenderEvent;
import noppes.mpm.client.model.animation.AnimationHandler;
import noppes.mpm.constants.BodyPart;
import noppes.mpm.constants.EnumParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntityRenderer.class})
public class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @WrapMethod(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void mpm$renderScope(T entity, float yaw, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, Operation<Void> original) {
        LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>)(Object)this;
        try (RenderStateScope scope = new RenderStateScope(entity, renderer.getModel())) {
            RenderStateScope.renderIsolated(poses,
                    isolated -> original.call(entity, yaw, partialTick, isolated, buffers, light));
        }
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;", ordinal = 0),
            require = 1)
    private Event mpm$afterPre(IEventBus bus,
            Event event,
            Operation<Event> original) {
        Event result = original.call(bus, event);
        if (result instanceof RenderLivingEvent.Pre<?, ?> pre && !pre.isCanceled()) {
            RenderEvent.prepare(pre);
        }
        return result;
    }

    @Inject(at={@At(value="HEAD")}, method={"getRenderType"}, cancellable=true)
    private void getModelName(T livingEntity, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_, CallbackInfoReturnable<RenderType> cir) {
        ResourceLocation texture = RenderEvent.textureFor(livingEntity);
        if (texture != null) {
            if (p_230496_3_) {
                cir.setReturnValue(RenderType.itemEntityTranslucentCull((ResourceLocation)texture));
            } else if (p_230496_2_) {
                LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
                cir.setReturnValue(r.getModel().renderType(texture));
            } else {
                cir.setReturnValue((p_230496_4_ ? RenderType.outline((ResourceLocation)texture) : null));
            }
            cir.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"render"}, cancellable=false)
    private void renderPre(T entity, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo cb) {
        LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
        if (entity instanceof AbstractClientPlayer && r.getModel() instanceof PlayerModel) {
            ModelData data = ModelData.get((Player)entity);
            PlayerModel model = (PlayerModel)r.getModel();
            model.leftLeg.visible = model.leftLeg.visible && !data.hiddenParts.contains((Object)BodyPart.LEFT_LEG) && !data.hiddenParts.contains((Object)BodyPart.LEGS);
            model.leftPants.visible = model.leftPants.visible && model.leftLeg.visible;
            model.rightLeg.visible = model.rightLeg.visible && !data.hiddenParts.contains((Object)BodyPart.RIGHT_LEG) && !data.hiddenParts.contains((Object)BodyPart.LEGS);
            model.rightPants.visible = model.rightPants.visible && model.rightLeg.visible;
            model.leftArm.visible = model.leftArm.visible && !data.hiddenParts.contains((Object)BodyPart.LEFT_ARM) && !data.hiddenParts.contains((Object)BodyPart.ARMS);
            model.leftSleeve.visible = model.leftSleeve.visible && model.leftArm.visible;
            model.rightArm.visible = model.rightArm.visible && !data.hiddenParts.contains((Object)BodyPart.RIGHT_ARM) && !data.hiddenParts.contains((Object)BodyPart.ARMS);
            model.rightSleeve.visible = model.rightSleeve.visible && model.rightArm.visible;
            model.body.visible = model.body.visible && !data.hiddenParts.contains((Object)BodyPart.BODY);
            model.jacket.visible = model.jacket.visible && model.body.visible;
            model.head.visible = model.head.visible && !data.hiddenParts.contains((Object)BodyPart.HEAD);
            model.hat.visible = model.hat.visible && model.head.visible;
        }
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            shift = At.Shift.AFTER
        ),
        method = {"render"}
    )
    private void applyMpmTransforms(T entity, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo cb) {
        LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
        if (MorePlayerModels.Compatibility || !(entity instanceof AbstractClientPlayer) || !(r.getModel() instanceof PlayerModel)) {
            return;
        }

        PlayerModel model = (PlayerModel)r.getModel();
        ModelData data = ModelData.get((Player)entity);
        ModelPart[] parts = new ModelPart[]{
            model.head, model.body, model.leftArm, model.rightArm, model.leftLeg, model.rightLeg,
            model.hat, model.jacket, model.leftSleeve, model.rightSleeve, model.leftPants, model.rightPants
        };
        EnumParts[] types = new EnumParts[]{EnumParts.HEAD, EnumParts.BODY, EnumParts.ARM_LEFT, EnumParts.ARM_RIGHT, EnumParts.LEG_LEFT, EnumParts.LEG_RIGHT};
        for (int i = 0; i < parts.length; ++i) {
            ModelPart part = parts[i];
            if (i < types.length) {
                var config = data.getPartConfig(types[i]);
                part.x += config.transX * 16.0f;
                part.y += config.transY * 16.0f;
                part.z += config.transZ * 16.0f;
            }
        }
        AnimationHandler.syncPlayerSkinLayers(model);
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
        ),
        method = {"render"}
    )
    private void syncPlayerSkinLayers(T entity, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo cb) {
        LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
        if (entity instanceof AbstractClientPlayer && r.getModel() instanceof PlayerModel) {
            AnimationHandler.syncPlayerSkinLayers((PlayerModel)r.getModel());
        }
    }

}
