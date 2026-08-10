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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.RenderEvent;
import noppes.mpm.client.model.animation.AnimationHandler;
import noppes.mpm.constants.BodyPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntityRenderer.class})
public class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    private boolean leftLegVisible;
    private boolean rightLegVisible;
    private boolean leftArmVisible;
    private boolean rightArmVisible;
    private boolean leftPantsVisible;
    private boolean rightPantsVisible;
    private boolean leftSleeveVisible;
    private boolean rightSleeveVisible;
    private boolean bodyVisible;
    private boolean jacketVisible;
    private boolean headVisible;
    private boolean hatVisible;

    @Inject(at={@At(value="HEAD")}, method={"getRenderType"}, cancellable=true)
    private void getModelName(T livingEntity, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_, CallbackInfoReturnable<RenderType> cir) {
        if (RenderEvent.entityResource != null) {
            if (p_230496_3_) {
                cir.setReturnValue(RenderType.itemEntityTranslucentCull((ResourceLocation)RenderEvent.entityResource));
            } else if (p_230496_2_) {
                LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
                cir.setReturnValue(r.getModel().renderType(RenderEvent.entityResource));
            } else {
                cir.setReturnValue((p_230496_4_ ? RenderType.outline((ResourceLocation)RenderEvent.entityResource) : null));
            }
            RenderEvent.entityResource = null;
            cir.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"render"}, cancellable=false)
    private void renderPre(T entity, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo cb) {
        LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
        if (entity instanceof AbstractClientPlayer && r.getModel() instanceof PlayerModel) {
            ModelData data = ModelData.get((Player)entity);
            PlayerModel model = (PlayerModel)r.getModel();
            this.leftLegVisible = model.leftLeg.visible;
            this.rightLegVisible = model.rightLeg.visible;
            this.leftArmVisible = model.leftArm.visible;
            this.rightArmVisible = model.rightArm.visible;
            this.leftPantsVisible = model.leftPants.visible;
            this.rightPantsVisible = model.rightPants.visible;
            this.leftSleeveVisible = model.leftSleeve.visible;
            this.rightSleeveVisible = model.rightSleeve.visible;
            this.bodyVisible = model.body.visible;
            this.jacketVisible = model.jacket.visible;
            this.headVisible = model.head.visible;
            this.hatVisible = model.hat.visible;
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

    @Inject(at={@At(value="TAIL")}, method={"render"}, cancellable=false)
    private void renderPost(T entity, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_, CallbackInfo cb) {
        LivingEntityRenderer r = (LivingEntityRenderer)(Object)this;
        if (entity instanceof AbstractClientPlayer && r.getModel() instanceof PlayerModel) {
            PlayerModel model = (PlayerModel)r.getModel();
            model.leftLeg.visible = this.leftLegVisible;
            model.rightLeg.visible = this.rightLegVisible;
            model.leftArm.visible = this.leftArmVisible;
            model.rightArm.visible = this.rightArmVisible;
            model.leftPants.visible = this.leftPantsVisible;
            model.rightPants.visible = this.rightPantsVisible;
            model.leftSleeve.visible = this.leftSleeveVisible;
            model.rightSleeve.visible = this.rightSleeveVisible;
            model.body.visible = this.bodyVisible;
            model.jacket.visible = this.jacketVisible;
            model.head.visible = this.headVisible;
            model.hat.visible = this.hatVisible;
        }
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
