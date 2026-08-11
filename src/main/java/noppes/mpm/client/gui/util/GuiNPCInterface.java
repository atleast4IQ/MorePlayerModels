/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Renderable
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.joml.Matrix4f
 */
package noppes.mpm.client.gui.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import noppes.mpm.client.gui.util.GuiCustomScroll;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.GuiNpcLabel;
import noppes.mpm.client.gui.util.GuiNpcSlider;
import noppes.mpm.client.gui.util.GuiNpcTextField;
import noppes.mpm.client.gui.util.ISubGuiListener;
import noppes.mpm.client.RenderEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class GuiNPCInterface
extends Screen {
    public LocalPlayer player;
    public boolean drawDefaultBackground = true;
    private final HashMap<Integer, GuiNpcButton> npcbuttons = new HashMap();
    private final HashMap<Integer, GuiNpcTextField> textfields = new HashMap();
    private final HashMap<Integer, GuiNpcLabel> labels = new HashMap();
    private final HashMap<Integer, GuiCustomScroll> scrolls = new HashMap();
    private final HashMap<Integer, GuiNpcSlider> sliders = new HashMap();
    private final HashMap<Integer, Screen> extra = new HashMap();
    protected ResourceLocation background = null;
    public boolean closeOnEsc = false;
    public int guiLeft;
    public int guiTop;
    public int xSize;
    public int ySize;
    private GuiNPCInterface subgui;
    public GuiNPCInterface parent;
    public int mouseX;
    public int mouseY;

    public GuiNPCInterface() {
        super((Component)Component.empty());
        this.player = Minecraft.getInstance().player;
        this.xSize = 200;
        this.ySize = 222;
    }

    public void setBackground(String texture) {
        this.background = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/" + texture);
    }

    public void init() {
        super.init();
        GuiNpcTextField.unfocus();
        if (this.subgui != null) {
            this.subgui.init(this.minecraft, this.width, this.height);
            this.subgui.init();
        }
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.renderables.clear();
        this.labels.clear();
        this.children().clear();
        this.textfields.clear();
        this.npcbuttons.clear();
        this.scrolls.clear();
        this.sliders.clear();
    }

    public void tick() {
        if (this.subgui != null) {
            this.subgui.tick();
        } else {
            super.tick();
        }
    }

    public boolean mouseClicked(double i, double j, int k) {
        if (this.subgui != null) {
            return this.subgui.mouseClicked(i, j, k);
        }
        if (k == 0) {
            for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
                scroll.mouseClicked(i, j, k);
            }
        }
        this.mouseEvent(i, j, k);
        boolean bo = super.mouseClicked(i, j, k);
        GuiNpcTextField.handleFocus(this, bo);
        return bo;
    }

    public void mouseEvent(double x, double y, int k) {
    }

    public void buttonEvent(GuiNpcButton button) {
    }

    public boolean charTyped(char c, int i) {
        if (this.subgui != null) {
            return this.subgui.charTyped(c, i);
        }
        for (GuiCustomScroll s : this.scrolls.values()) {
            s.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (this.subgui != null) {
            return this.subgui.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
        if (this.closeOnEsc && (p_keyPressed_1_ == 256 || !GuiNpcTextField.hasActive() && this.isInventoryKey(p_keyPressed_1_))) {
            this.close();
            return true;
        }
        for (GuiCustomScroll s : this.scrolls.values()) {
            s.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    public void onClose() {
        super.onClose();
        GuiNpcTextField.unfocus();
    }

    public void close() {
        if (this.parent != null) {
            this.parent.closeSubGui(this);
        } else {
            this.setScreen(null);
        }
        this.save();
    }

    public void addButton(GuiNpcButton button) {
        this.npcbuttons.put(button.id, button);
        super.addRenderableWidget(button);
    }

    public GuiNpcButton getButton(int i) {
        return this.npcbuttons.get(i);
    }

    public void addTextField(GuiNpcTextField tf) {
        this.textfields.put(tf.id, tf);
        super.addRenderableWidget(tf);
    }

    public GuiNpcTextField getTextField(int i) {
        return this.textfields.get(i);
    }

    public void addLabel(GuiNpcLabel label) {
        this.labels.put(label.id, label);
    }

    public GuiNpcLabel getLabel(int i) {
        return this.labels.get(i);
    }

    public void addSlider(GuiNpcSlider slider) {
        this.sliders.put(slider.id, slider);
        this.addRenderableWidget(slider);
    }

    public GuiNpcSlider getSlider(int i) {
        return this.sliders.get(i);
    }

    public void addScroll(GuiCustomScroll scroll) {
        scroll.init(this.minecraft, scroll.width, scroll.height);
        this.scrolls.put(scroll.id, scroll);
    }

    public GuiCustomScroll getScroll(int id) {
        return this.scrolls.get(id);
    }

    public abstract void save();

    public void render(GuiGraphics graphics, int x, int y, float f) {
        this.mouseX = x;
        this.mouseY = y;
        if (this.subgui == null || this.subgui.drawSubGuiBackground()) {
            if (this.drawDefaultBackground) {
                this.renderBackground(graphics, 0, 0, 0);
            }
            if (this.background != null && this.minecraft.getTextureManager() != null) {
                if (this.xSize > 256) {
                    graphics.blit(this.background, this.guiLeft, this.guiTop, 0, 0, 250, this.ySize);
                    graphics.blit(this.background, this.guiLeft + 250, this.guiTop, 256 - (this.xSize - 250), 0, this.xSize - 250, this.ySize);
                } else {
                    graphics.blit(this.background, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
                }
            }
            for (GuiNpcLabel label : this.labels.values()) {
                label.drawLabel(graphics, this, this.font);
            }
            for (GuiCustomScroll scroll : this.scrolls.values()) {
                scroll.render(graphics, x, y, f);
            }
            for (Screen gui : this.extra.values()) {
                gui.render(graphics, x, y, f);
            }
            for (Renderable renderable : this.renderables) {
                renderable.render(graphics, x, y, f);
            }
        }
        if (this.subgui != null) {
            graphics.pose().translate(0.0f, 0.0f, 260.0f);
            this.subgui.render(graphics, x, y, f);
            graphics.pose().translate(0.0f, 0.0f, -260.0f);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrolledY) {
        if (this.subgui != null) {
            this.subgui.mouseScrolled(mouseX, mouseY, scrolledY);
        } else {
            for (GuiCustomScroll scroll : this.scrolls.values()) {
                scroll.mouseScrolled(mouseX, mouseY, scrolledY);
            }
        }
        return true;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (this.subgui != null) {
            return this.subgui.mouseDragged(x, y, button, dx, dy);
        }
        return super.mouseDragged(x, y, button, dx, dy);
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (this.subgui != null) {
            return this.subgui.mouseReleased(x, y, button);
        }
        return super.mouseReleased(x, y, button);
    }

    public boolean drawSubGuiBackground() {
        return true;
    }

    public Font getFont() {
        return this.font;
    }

    public void elementClicked() {
        if (this.subgui != null) {
            this.subgui.elementClicked();
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void doubleClicked() {
    }

    public boolean isInventoryKey(int i) {
        return this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey((int)i, (int)0));
    }

    public void setScreen(Screen gui) {
        this.minecraft.setScreen(gui);
    }

    public void setSubGui(GuiNPCInterface gui) {
        this.subgui = gui;
        this.subgui.parent = this;
        this.subgui.init(this.minecraft, this.width, this.height);
        this.init();
    }

    public void closeSubGui(GuiNPCInterface gui) {
        this.subgui = null;
        if (this instanceof ISubGuiListener) {
            ((ISubGuiListener)((Object)this)).subGuiClosed(gui);
        }
        this.init();
    }

    public boolean hasSubGui() {
        return this.subgui != null;
    }

    public GuiNPCInterface getSubGui() {
        if (this.hasSubGui() && this.subgui.hasSubGui()) {
            return this.subgui.getSubGui();
        }
        return this.subgui;
    }

    public void drawEntity(GuiGraphics graphics, LivingEntity entity, int x, int y, float zoomed, int rotation, int guiLeft, int guiTop) {
        // MPM's original preview path rotates the complete rendered entity on
        // one pose stack. This is essential for non-player avatars: rendering
        // them through the 1.21 inventory turntable leaves their armor layer
        // in a different rotation space from the avatar body.
        float bodyRot = entity.yBodyRot;
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;
        float scale = 1.0f;
        if ((double)entity.getBbHeight() > 2.4) {
            scale = 2.0f / entity.getBbHeight();
        }
        final float previewScale = scale * zoomed;
        float mouseOffsetX = (float)(guiLeft + x) - (float)this.mouseX;
        float mouseOffsetY = (float)(guiTop + y) - 50.0f * scale * zoomed - (float)this.mouseY;
        entity.yBodyRot = 0.0f;
        entity.setYRot((float)Math.atan(mouseOffsetX / 80.0f) * 40.0f + (float)rotation);
        entity.setXRot(-((float)Math.atan(mouseOffsetY / 40.0f)) * 20.0f);
        entity.yHeadRot = 0.0f;
        entity.yHeadRotO = 0.0f;
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        float renderScale = 30.0f * previewScale;
        // Vanilla 1.20 and 1.21 both scale all three model axes here. The
        // old port kept Z at -1 while scaling X/Y by the preview zoom, which
        // collapsed the depth gap between the player skin and armor and made
        // them occlude each other as the preview rotated.
        poseStack.translate(guiLeft + x, guiTop + y, 50.0f);
        poseStack.scale(renderScale, renderScale, -renderScale);
        // This is the original MPM root-stack orientation.  The former
        // port changed the two Y-axis rotations into X-axis rotations,
        // which flips the complete avatar upside down.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YN.rotationDegrees(rotation));
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(Axis.YN.rotationDegrees(180.0f));
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack, graphics.bufferSource(), 0xF000F0));
        graphics.flush();
        dispatcher.setRenderShadow(true);
        poseStack.popPose();
        Lighting.setupFor3DItems();
        entity.yBodyRot = bodyRot;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
    }

    /**
     * 1.20's preview API used the given coordinates as the legacy model
     * centre.  The 1.21 rectangle helper additionally translates by half an
     * entity height, which pushes every old MPM preview down into its buttons.
     * Recreate the old pose with the 1.21 renderer instead.
     */
    protected void renderEntityPreview(GuiGraphics graphics, LivingEntity entity, int centerX, int centerY, int scale, int mouseX, int mouseY) {
        float horizontal = (float)Math.atan(((float)centerX - (float)mouseX) / 40.0f);
        float vertical = (float)Math.atan(((float)centerY - (float)mouseY) / 40.0f);
        Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf cameraOrientation = Axis.XP.rotationDegrees(vertical * 20.0f);
        pose.mul(cameraOrientation);
        float bodyRot = entity.yBodyRot;
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;
        RenderEvent.renderGuiPreview(() -> {
            entity.yBodyRot = 180.0f + horizontal * 20.0f;
            entity.setYRot(180.0f + horizontal * 40.0f);
            entity.setXRot(-vertical * 20.0f);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            InventoryScreen.renderEntityInInventory(
                    graphics,
                    centerX,
                    centerY,
                    (float)scale / entity.getScale(),
                    new Vector3f(),
                    pose,
                    cameraOrientation,
                    entity
            );
        });
        entity.yBodyRot = bodyRot;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
    }

    public void openLink(String link) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}
