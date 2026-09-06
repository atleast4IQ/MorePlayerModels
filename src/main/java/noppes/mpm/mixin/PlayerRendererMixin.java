/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package noppes.mpm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.geom.ModelPart;
import noppes.mpm.client.RenderStateScope;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.ChatMessages;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.constants.EnumAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerRenderer.class})
public class PlayerRendererMixin {
    @WrapMethod(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void mpm$playerScope(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, Operation<Void> original) {
        PlayerRenderer renderer = (PlayerRenderer)(Object)this;
        try (var scope = new RenderStateScope(player, renderer.getModel())) {
            original.call(player, yaw, partialTick, poses, buffers, light);
        }
    }

    @WrapMethod(method = "renderHand")
    private void mpm$handScope(PoseStack poses, MultiBufferSource buffers, int light, AbstractClientPlayer player,
            ModelPart arm, ModelPart sleeve,
            Operation<Void> original) {
        PlayerRenderer renderer = (PlayerRenderer)(Object)this;
        try (var scope = new RenderStateScope(player, renderer.getModel())) {
            original.call(RenderStateScope.copyPose(poses), buffers, light, player, arm, sleeve);
        }
    }

    /**
     * PlayerRenderer now reads PlayerSkin.texture() directly.  The old
     * AbstractClientPlayer#getSkinTextureLocation hook is therefore not on
     * the render path in 1.21.1.
     */
    @Inject(at={@At(value="HEAD")}, method={"getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;"}, cancellable=true)
    private void getTextureLocation(AbstractClientPlayer player, CallbackInfoReturnable<ResourceLocation> callbackInfo) {
        ResourceLocation texture = SkinUtil.getTexture(player);
        if (!texture.equals(player.getSkin().texture())) {
            callbackInfo.setReturnValue(texture);
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"setupRotations"})
    private void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float bob, float rotation, float partialTick, float scale, CallbackInfo callbackInfo) {
        ModelData data = ModelData.get((Player)player);
        if (data.moveAnimation == EnumAnimation.SLEEP) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.translate(0.0f, -(1.8f + data.offsetY()) / 2.0f, 0.0f);
        } else if (data.moveAnimation == EnumAnimation.CRAWL) {
            if (player.isCrouching()) {
                poseStack.translate(0.0f, 0.075f, 0.0f);
            }
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0f));
            poseStack.translate(0.0f, -(1.8f + data.offsetY()) / 2.0f, 0.0f);
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"renderNameTag"})
    private void renderNameTag(AbstractClientPlayer player, Component p_117809_, PoseStack mStack, MultiBufferSource buffer, int lightmapUV, float partialTick, CallbackInfo callbackInfo) {
        Minecraft mc = Minecraft.getInstance();
        ChatMessages chat = ChatMessages.getChatMessages(player.getName().getString());
        if (!chat.hasMessage()) {
            return;
        }
        boolean inRange = player.distanceTo(mc.getCameraEntity()) <= 4.0f;
        mStack.pushPose();
        try {
            mStack.translate(0.0, 0.7 + (double)player.getBbHeight(), 0.0);
            chat.renderMessages(mStack, buffer, 1.0f, inRange, lightmapUV);
        } finally {
            mStack.popPose();
        }
    }
}
