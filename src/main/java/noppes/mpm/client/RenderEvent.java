/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.client.event.ClientChatReceivedEvent
 *  net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent$Chat
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 *  net.neoforged.neoforge.client.event.RenderHighlightEvent
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Pre
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 */
package noppes.mpm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import noppes.mpm.LogWriter;
import noppes.mpm.ModelData;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.ChatMessages;
import noppes.mpm.client.ClientEventHandler;
import noppes.mpm.client.layer.LayerPreRender;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.mixin.EntityMixin;
import noppes.mpm.mixin.LivingRenderer2Mixin;
import noppes.mpm.shared.client.model.util.BatchRenderer;
import noppes.mpm.util.PixelmonHelper;

public class RenderEvent {
    public static RenderEvent Instance;
    private static ResourceLocation entityResource;
    private static LivingEntity textureEntity;
    private static boolean renderingGuiPreview;

    public static ResourceLocation textureFor(LivingEntity entity) {
        return entity == textureEntity ? entityResource : null;
    }

    public static void withTexture(LivingEntity entity, ResourceLocation texture, Runnable renderer) {
        ResourceLocation previousTexture = entityResource;
        LivingEntity previousEntity = textureEntity;
        entityResource = texture;
        textureEntity = entity;
        try {
            renderer.run();
        } finally {
            entityResource = previousTexture;
            textureEntity = previousEntity;
        }
    }

    public static void renderGuiPreview(Runnable renderer) {
        boolean previous = renderingGuiPreview;
        renderingGuiPreview = true;
        try {
            renderer.run();
        } finally {
            renderingGuiPreview = previous;
        }
    }

    public RenderEvent() {
        Instance = this;
        Minecraft mc = Minecraft.getInstance();
    }

    /** Called after the complete Pre dispatch, inside LivingRendererMixin's owned render scope. */
    public static void prepare(RenderLivingEvent.Pre<?, ?> event) {
        if (renderingGuiPreview) {
            return;
        }
        float offset;
        if (!(event.getEntity() instanceof AbstractClientPlayer) || event.isCanceled()) {
            return;
        }
        AbstractClientPlayer player = (AbstractClientPlayer)event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        PoseStack mStack = event.getPoseStack();
        if (ClientEventHandler.camera.enabled && player == mc.player) {
            player.yHeadRot = player.yBodyRot;
            player.yHeadRotO = player.yBodyRotO;
            player.xRotO = ClientEventHandler.camera.playerPitch;
            player.setXRot(player.xRotO);
            mc.gameRenderer.pick(ClientEventHandler.partialTick);
        }
        ModelData data = ModelData.get((Player)player);
        if (data.moveAnimation == EnumAnimation.SLEEP) {
            player.yBodyRot = player.yBodyRotO = data.sleepRotation;
            player.yHeadRot = player.yHeadRotO = Math.min(Math.max(player.yHeadRot, data.sleepRotation - 60.0f), data.sleepRotation + 60.0f);
            player.xRotO = Math.min(Math.max(player.getXRot(), 0.0f), 60.0f);
            player.setXRot(player.xRotO);
        }
        if (((EntityMixin)player).getDimensions().height() - (offset = data.getOffsetCamera((Player)player)) < 0.0f) {
            offset = 0.0f;
        }
        ((EntityMixin)player).setEyeHeight(player.getEyeHeight(player.getPose()) - offset);
        Entity customEntity = data.getEntity((Player)player);
        if (customEntity != null) {
            float previousYaw = customEntity.getYRot();
            float previousOldYaw = customEntity.yRotO;
            boolean previousShift = customEntity.isShiftKeyDown();
            try {
                if (ClientEventHandler.camera.enabled && player == mc.player) {
                    customEntity.setYRot(player.getYRot());
                    customEntity.yRotO = player.yRotO;
                }
                event.setCanceled(true);
                if (PixelmonHelper.isPixelmon(customEntity)) {
                    customEntity.setShiftKeyDown(true);
                }
                // Custom entity models (for example a wolf) own their renderer and
                // texture.  The old player-skin override turns their geometry into
                // a scrambled player-skin atlas on 1.21.1.
                mc.getEntityRenderDispatcher().render(customEntity, 0.0, 0.0, 0.0, 0.0f, event.getPartialTick(), RenderStateScope.copyPose(mStack), event.getMultiBufferSource(), event.getPackedLight());
            } finally {
                customEntity.setYRot(previousYaw);
                customEntity.yRotO = previousOldYaw;
                customEntity.setShiftKeyDown(previousShift);
            }
            return;
        }
        offset = 0.0f;
        if (!MorePlayerModels.DisableFlyingAnimation && player.getAbilities().flying && player.level().isEmptyBlock(player.blockPosition())) {
            offset = Mth.cos((float)((float)player.tickCount * 0.1f)) * -0.06f;
        }
        if (data.moveAnimation == EnumAnimation.SIT) {
            offset = (float)((double)offset + (0.5 - (double)data.getLegsY() * 0.8));
        }
        mStack.translate(0.0f, -offset, 0.0f);
        List layers = ((LivingRenderer2Mixin)event.getRenderer()).getLayers();
        for (Object object : layers) {
            RenderLayer layer = (RenderLayer)object;
            if (!(layer instanceof LayerPreRender)) continue;
            ((LayerPreRender)layer).preRender(player);
        }
    }

    @SubscribeEvent
    public void hand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ModelData data = ModelData.get((Player)mc.player);
        LivingEntity entity = data.getEntity((Player)mc.player);
        if (entity != null || data.moveAnimation == EnumAnimation.SLEEP || data.moveAnimation == EnumAnimation.CRAWL || data.animation == EnumAnimation.BOW && mc.player.getMainHandItem().isEmpty()) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent event) {
        if (MorePlayerModels.HasServerSide) {
            return;
        }
        try {
            ChatMessages.parseMessage(event.getMessage().getString());
        }
        catch (Exception ex) {
            LogWriter.warn("Cant handle chatmessage: " + event.getMessage() + ":" + ex.getMessage());
        }
    }

    @SubscribeEvent
    public void selectionBox(RenderHighlightEvent.Block event) {
        if (MorePlayerModels.HideSelectionBox) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void nameTag(RenderNameTagEvent event) {
        if (MorePlayerModels.HidePlayerNames && event.getEntity() instanceof Player) {
            event.setCanRender(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public void overlay(CustomizeGuiOverlayEvent.Chat event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || MorePlayerModels.Tooltips == 0) {
            return;
        }
        ItemStack item = mc.player.getMainHandItem();
        if (item.isEmpty()) {
            return;
        }
        String name = item.getDisplayName().getString();
        int x = mc.getWindow().getGuiScaledWidth() - mc.font.width(name);
        int posX = 4;
        int posY = 4;
        if (MorePlayerModels.Tooltips % 2 == 0) {
            posX = x - 4;
        }
        if (MorePlayerModels.Tooltips > 2) {
            posY = mc.getWindow().getGuiScaledHeight() - 24;
        }
        event.getGuiGraphics().drawString(mc.font, name, posX, posY, 0xFFFFFF);
        if (item.isDamageableItem()) {
            int max = item.getMaxDamage();
            String dam = max - item.getDamageValue() + "/" + max;
            x = mc.getWindow().getGuiScaledWidth() - mc.font.width(dam);
            if (MorePlayerModels.Tooltips == 2 || MorePlayerModels.Tooltips == 4) {
                posX = x - 4;
            }
            event.getGuiGraphics().drawString(mc.font, dam, posX, posY + 12, 0xFFFFFF);
        }
    }

    static {
        entityResource = null;
    }
}
