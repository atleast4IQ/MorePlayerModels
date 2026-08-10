/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package noppes.mpm.client.model.animation;

import java.util.HashMap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import noppes.mpm.ModelData;
import noppes.mpm.client.model.animation.AniBlank;
import noppes.mpm.client.model.animation.AniBow;
import noppes.mpm.client.model.animation.AniCrawling;
import noppes.mpm.client.model.animation.AniDancing;
import noppes.mpm.client.model.animation.AniHug;
import noppes.mpm.client.model.animation.AniNo;
import noppes.mpm.client.model.animation.AniPoint;
import noppes.mpm.client.model.animation.AniWaving;
import noppes.mpm.client.model.animation.AniYes;
import noppes.mpm.client.model.animation.AnimationBase;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.constants.EnumParts;

public class AnimationHandler {
    private static final HashMap<EnumAnimation, AnimationBase> ANIMATIONS = new HashMap();

    public static void animateBipedPre(ModelData data, HumanoidModel bipedModel, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        bipedModel.body.z = 0.0f;
        bipedModel.body.y = 0.0f;
        bipedModel.body.x = 0.0f;
        bipedModel.body.zRot = 0.0f;
        bipedModel.body.yRot = 0.0f;
        bipedModel.body.xRot = 0.0f;
        bipedModel.head.xRot = 0.0f;
        bipedModel.hat.xRot = 0.0f;
        bipedModel.head.zRot = 0.0f;
        bipedModel.hat.zRot = 0.0f;
        bipedModel.head.x = 0.0f;
        bipedModel.hat.x = 0.0f;
        bipedModel.head.y = 0.0f;
        bipedModel.hat.y = 0.0f;
        bipedModel.head.z = 0.0f;
        bipedModel.hat.z = 0.0f;
        bipedModel.leftLeg.xRot = 0.0f;
        bipedModel.leftLeg.yRot = 0.0f;
        bipedModel.leftLeg.zRot = 0.0f;
        bipedModel.rightLeg.xRot = 0.0f;
        bipedModel.rightLeg.yRot = 0.0f;
        bipedModel.rightLeg.zRot = 0.0f;
        bipedModel.leftArm.x = 0.0f;
        bipedModel.leftArm.y = 2.0f;
        bipedModel.leftArm.z = 0.0f;
        bipedModel.rightArm.x = 0.0f;
        bipedModel.rightArm.y = 2.0f;
        bipedModel.rightArm.z = 0.0f;
        AnimationBase animation = AnimationHandler.getAnimationFor(data.moveAnimation);
        if (animation != null) {
            animation.animatePre(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, (Entity)livingEntity, bipedModel, data.animationStart);
        }
        if ((animation = AnimationHandler.getAnimationFor(data.animation)) != null) {
            animation.animatePre(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, (Entity)livingEntity, bipedModel, data.animationStart);
        }
        if (bipedModel.crouching && data.moveAnimation == EnumAnimation.CRAWL) {
            bipedModel.crouching = false;
        }
    }

    public static void animateBipedPost(ModelData data, HumanoidModel bipedModel, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        AnimationBase animation = AnimationHandler.getAnimationFor(data.moveAnimation);
        if (animation != null) {
            animation.animatePost(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, (Entity)livingEntity, bipedModel, data.animationStart);
        }
        if ((animation = AnimationHandler.getAnimationFor(data.animation)) != null) {
            animation.animatePost(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, (Entity)livingEntity, bipedModel, data.animationStart);
        }
        if (bipedModel.crouching && data.moveAnimation != EnumAnimation.CRAWL) {
            bipedModel.body.xRot = 0.5f / data.getPartConfig((EnumParts)EnumParts.BODY).scaleY;
        }
        if (bipedModel instanceof PlayerModel) {
            AnimationHandler.syncPlayerSkinLayers((PlayerModel)bipedModel);
        } else {
            bipedModel.hat.copyFrom(bipedModel.head);
        }
    }

    /**
     * Keep the skin overlay parts on the same final pose as their base parts.
     *
     * The vanilla player model performs this copy during setupAnim, but MPM
     * animations run around that method and can change the base pose afterward.
     * This helper is also called immediately before rendering as a final guard.
     */
    public static void syncPlayerSkinLayers(PlayerModel playerModel) {
        playerModel.hat.copyFrom(playerModel.head);
        playerModel.leftPants.copyFrom(playerModel.leftLeg);
        playerModel.rightPants.copyFrom(playerModel.rightLeg);
        playerModel.leftSleeve.copyFrom(playerModel.leftArm);
        playerModel.rightSleeve.copyFrom(playerModel.rightArm);
        playerModel.jacket.copyFrom(playerModel.body);
    }

    public static void addAnimation(EnumAnimation enumAnimation, AnimationBase animationBase) {
        ANIMATIONS.put(enumAnimation, animationBase);
    }

    public static HashMap<EnumAnimation, AnimationBase> getAllAnimations() {
        return ANIMATIONS;
    }

    public static AnimationBase getAnimationFor(EnumAnimation animation) {
        try {
            if (!ANIMATIONS.containsKey((Object)animation)) {
                throw new IllegalAccessException("Animation " + animation.name() + " is not registered, maybe you forgot?");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ANIMATIONS.get((Object)animation);
    }

    public static void initAnimations() {
        AnimationHandler.addAnimation(EnumAnimation.NONE, new AniBlank());
        AnimationHandler.addAnimation(EnumAnimation.SLEEP, new AniBlank());
        AnimationHandler.addAnimation(EnumAnimation.CRAWL, new AniCrawling());
        AnimationHandler.addAnimation(EnumAnimation.HUG, new AniHug());
        AnimationHandler.addAnimation(EnumAnimation.DANCE, new AniDancing());
        AnimationHandler.addAnimation(EnumAnimation.WAVE, new AniWaving());
        AnimationHandler.addAnimation(EnumAnimation.WAG, new AniBlank());
        AnimationHandler.addAnimation(EnumAnimation.BOW, new AniBow());
        AnimationHandler.addAnimation(EnumAnimation.YES, new AniYes());
        AnimationHandler.addAnimation(EnumAnimation.NO, new AniNo());
        AnimationHandler.addAnimation(EnumAnimation.POINT, new AniPoint());
        AnimationHandler.addAnimation(EnumAnimation.DEATH, new AniBlank());
        AnimationHandler.addAnimation(EnumAnimation.CRY, new AnimationBase(){

            @Override
            public void animatePre(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {
            }

            @Override
            public void animatePost(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {
                model.head.xRot = 0.7f;
                model.hat.xRot = 0.7f;
            }
        });
        AnimationHandler.addAnimation(EnumAnimation.SIT, new AnimationBase(){

            @Override
            public void animatePre(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {
                model.riding = true;
            }

            @Override
            public void animatePost(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, Entity entity, HumanoidModel model, int animationStart) {
            }
        });
    }
}
