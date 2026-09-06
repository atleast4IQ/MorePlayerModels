/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.LivingEntity
 *  org.joml.Matrix4f
 */
package noppes.mpm.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import noppes.mpm.ModelData;
import noppes.mpm.ModelEyeData;
import noppes.mpm.client.gui.GuiModelColor;
import noppes.mpm.client.gui.select.GuiTextureSelection;
import noppes.mpm.client.gui.util.GuiButtonBiDirectional;
import noppes.mpm.client.gui.util.GuiColorButton;
import noppes.mpm.client.gui.util.GuiCustomScroll;
import noppes.mpm.client.gui.util.GuiNPCInterface;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.GuiNpcButtonYesNo;
import noppes.mpm.client.gui.util.GuiNpcLabel;
import noppes.mpm.client.gui.util.GuiNpcSlider;
import noppes.mpm.client.gui.util.GuiNpcTextField;
import noppes.mpm.client.gui.util.ICustomScrollListener;
import noppes.mpm.client.gui.util.ITextfieldListener;
import noppes.mpm.client.layer.LayerParts;
import noppes.mpm.client.parts.ModelPartWrapper;
import noppes.mpm.client.parts.MpmPart;
import noppes.mpm.client.parts.MpmPartAbstractClient;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.client.parts.MpmPartDataClient;
import noppes.mpm.client.parts.MpmPartEyes;
import noppes.mpm.client.parts.MpmPartReader;
import noppes.mpm.constants.BodyPart;
import noppes.mpm.constants.PartBehaviorType;
import noppes.mpm.constants.PartRenderType;
import noppes.mpm.shared.util.ColorUtil;
import noppes.mpm.shared.util.NopVector2i;
import noppes.mpm.shared.util.NopVector3f;
import org.joml.Matrix4f;

public class GuiCreationNewParts
extends GuiNPCInterface
implements ITextfieldListener,
ICustomScrollListener {
    private GuiCustomScroll scroll;
    private ModelData data;
    private ModelData renderData = new ModelData();
    private List<MpmPart> list = new ArrayList<MpmPart>();
    private List<String> menus = new ArrayList<String>();
    private static String active = "";
    private static PlayerModel biped;
    private List<GuiMpmPart> guiParts = new ArrayList<GuiMpmPart>();
    private static final ResourceLocation blankSkin;
    private static final ResourceLocation colorWheel;
    private static float rotation;

    public GuiCreationNewParts(ModelData data) {
        this.data = data;
        this.menus = MpmPartReader.PARTS.values().stream().map(p -> p.menu).distinct().sorted().collect(Collectors.toList());
        if (!this.menus.isEmpty() && (active.isEmpty() || !this.menus.contains(active))) {
            active = this.menus.get(0);
        }
        this.closeOnEsc = true;
        this.xSize = 420;
        this.ySize = 240;
    }

    @Override
    public void init() {
        super.init();
        biped = new PlayerModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), true);
        this.list = new ArrayList<MpmPart>(MpmPartReader.PARTS.values().stream().sorted(Comparator.comparing(t -> t.id)).filter(t -> t.menu.equals(active) && t.parentId == null).collect(Collectors.toList()));
        if (this.scroll == null) {
            this.scroll = new GuiCustomScroll(this, 0);
            this.scroll.setList(this.menus);
            this.scroll.disabledSearch();
        }
        this.scroll.guiLeft = this.guiLeft + 4;
        this.scroll.guiTop = this.guiTop + 4;
        this.scroll.setSize(100, 110);
        this.scroll.setSelected(active);
        this.addScroll(this.scroll);
        GuiNpcSlider slider = new GuiNpcSlider(this, 500, this.guiLeft + 4, this.guiTop + 216, 100, 20, rotation);
        slider.setListener(t -> {
            rotation = t.sliderValue;
            t.setString("" + (int)(rotation * 360.0f));
        });
        this.addSlider(slider);
        if (this.guiParts.isEmpty()) {
            for (int i = 0; i < this.list.size(); ++i) {
                int column = i % 4;
                MpmPart part = this.list.get(i);
                GuiMpmPart gui = new GuiMpmPart(this.guiLeft + column * 70 + 110 + column, this.guiTop + i / 4 * 70 + 4, part);
                this.addRenderableWidget(gui);
                this.guiParts.add(gui);
            }
        } else {
            for (int i = 0; i < this.guiParts.size(); ++i) {
                int column = i % 4;
                GuiMpmPart gui = this.guiParts.get(i);
                this.addRenderableWidget(gui);
                gui.setX(this.guiLeft + column * 70 + 110 + column);
                gui.setY(this.guiTop + i / 4 * 70 + 4);
            }
        }
        this.addButton(new GuiNpcButton(66, this.guiLeft + 396, this.guiTop + 2, 20, 20, "X", b -> this.close()));
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float f) {
        super.render(graphics, i, j, f);
        this.drawEntity(graphics, (LivingEntity)this.player, 50, 200, 1.4f, (int)(-rotation * 360.0f) + 180, this.guiLeft, this.guiTop);
        if (!this.hasSubGui()) {
            for (GuiMpmPart gui : this.guiParts) {
                gui.renderModel(graphics, i, j, f);
            }
            for (GuiMpmPart gui : this.guiParts) {
                gui.renderIcons(graphics, i, j, f);
            }
            for (GuiMpmPart gui : this.guiParts) {
                if (!gui.infoHovered) continue;
                List<FormattedCharSequence> text = Arrays.asList(Component.translatable((String)gui.part.name).getVisualOrderText(), Component.translatable((String)"message.madeby", (Object[])new Object[]{gui.part.author}).getVisualOrderText());
                if (!gui.part.isEnabled) {
                    text = Arrays.asList(Component.translatable((String)"gui.disabled", (Object[])new Object[]{gui.part.author}).getVisualOrderText());
                }
                graphics.renderTooltip(this.font, text, i, j);
            }
        }
    }

    @Override
    public void buttonEvent(GuiNpcButton btn) {
    }

    @Override
    public void save() {
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 23) {
            // empty if block
        }
    }

    @Override
    public void scrollClicked(double i, double j, int k, GuiCustomScroll scroll) {
        if (scroll.getSelectedIndex() >= 0) {
            active = scroll.getSelected();
            this.guiParts.clear();
            this.init();
        }
    }

    @Override
    public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }

    static {
        blankSkin = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/entity/gray.png");
        colorWheel = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/colorwheel.png");
        rotation = 0.5f;
    }

    class GuiMpmPart
    extends AbstractWidget {
        public static final int SIZE = 70;
        public boolean basic;
        private List<MpmPart> all;
        private MpmPart part;
        private MpmPartData data;
        private boolean selected;
        boolean colorPickerHovered;
        boolean infoHovered;
        boolean settingsHovered;
        boolean hoverL;
        boolean hoverR;
        int zPos;

        public GuiMpmPart(int x, int y, MpmPart part) {
            super(x, y, 70, 70, (Component)Component.empty());
            this.basic = false;
            this.all = new ArrayList<MpmPart>();
            this.selected = true;
            this.colorPickerHovered = false;
            this.infoHovered = false;
            this.settingsHovered = false;
            this.hoverL = false;
            this.hoverR = false;
            this.zPos = 0;
            this.part = part;
            this.all.add(part);
            for (Map.Entry<ResourceLocation, MpmPart> entry : MpmPartReader.PARTS.entrySet()) {
                if (entry.getValue().parentId == null || !entry.getValue().parentId.equals((Object)part.id)) continue;
                this.all.add(entry.getValue());
            }
            for (MpmPart p : this.all) {
                this.data = GuiCreationNewParts.this.data.mpmParts.stream().filter(t -> t != null && p.id.equals(t.partId)).findFirst().orElse(null);
                if (this.data == null) continue;
                this.part = p;
                break;
            }
            this.all = this.all.stream().sorted(Comparator.comparing(t -> t.id)).collect(Collectors.toList());
            if (this.data == null) {
                this.data = part.id.equals((Object)ModelEyeData.RESOURCE) || part.id.equals((Object)ModelEyeData.RESOURCE_RIGHT) || part.id.equals((Object)ModelEyeData.RESOURCE_LEFT) ? new ModelEyeData() : new MpmPartData();
                this.data.clientData = new MpmPartDataClient();
                this.data.partId = part.id;
                this.data.usePlayerSkin = part.defaultUsePlayerSkins;
                this.selected = false;
            }
        }

        protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
        }

        public void renderModel(GuiGraphics graphics, int xMouse, int yMouse, float tick) {
            int x1 = this.getX();
            int x2 = this.getX() + 70;
            int y1 = this.getY();
            int y2 = this.getY() + 70 - 1;
            graphics.fill(x1, y1, x2, y2, -3750202);
            Minecraft minecraft = Minecraft.getInstance();
            GuiCreationNewParts.this.renderData.mpmParts = GuiCreationNewParts.this.data.mpmParts;
            PoseStack matrixstack = graphics.pose();
            matrixstack.pushPose();
            try {
                matrixstack.translate((double)(this.getX() + 10), (double)(this.getY() + 10), 150.0);
                matrixstack.mulPose(new Matrix4f().scaling(1.0f, 1.0f, -1.0f));
                EntityRenderDispatcher entityrenderermanager = minecraft.getEntityRenderDispatcher();
                MultiBufferSource.BufferSource irendertypebuffer$impl = graphics.bufferSource();
                VertexConsumer ivertex = irendertypebuffer$impl.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)GuiCreationNewParts.this.player.getSkin().texture()));
                Lighting.setupForEntityInInventory();
                noppes.mpm.client.RenderStateScope.runAsFancy(() -> {
                    var previousParts = noppes.mpm.client.RenderStateScope.snapshot(biped);
                    try (var partScope = this.part instanceof MpmPartAbstractClient clientPart ? clientPart.saveRenderState() : null) {
                        ModelPartWrapper modelPart;
                        GuiCreationNewParts.biped.leftLeg.visible = !this.part.hiddenParts.contains((Object)BodyPart.LEFT_LEG) && !this.part.hiddenParts.contains((Object)BodyPart.LEGS);
                        GuiCreationNewParts.biped.leftPants.visible = GuiCreationNewParts.biped.leftPants.visible && GuiCreationNewParts.biped.leftLeg.visible;
                        GuiCreationNewParts.biped.rightLeg.visible = !this.part.hiddenParts.contains((Object)BodyPart.RIGHT_LEG) && !this.part.hiddenParts.contains((Object)BodyPart.LEGS);
                        GuiCreationNewParts.biped.rightPants.visible = GuiCreationNewParts.biped.rightPants.visible && GuiCreationNewParts.biped.rightLeg.visible;
                        GuiCreationNewParts.biped.leftArm.visible = !this.part.hiddenParts.contains((Object)BodyPart.LEFT_ARM) && !this.part.hiddenParts.contains((Object)BodyPart.ARMS);
                        GuiCreationNewParts.biped.leftSleeve.visible = GuiCreationNewParts.biped.leftSleeve.visible && GuiCreationNewParts.biped.leftArm.visible;
                        GuiCreationNewParts.biped.rightArm.visible = !this.part.hiddenParts.contains((Object)BodyPart.RIGHT_ARM) && !this.part.hiddenParts.contains((Object)BodyPart.ARMS);
                        GuiCreationNewParts.biped.rightSleeve.visible = GuiCreationNewParts.biped.rightSleeve.visible && GuiCreationNewParts.biped.rightArm.visible;
                        GuiCreationNewParts.biped.body.visible = !this.part.hiddenParts.contains((Object)BodyPart.BODY);
                        GuiCreationNewParts.biped.jacket.visible = GuiCreationNewParts.biped.jacket.visible && GuiCreationNewParts.biped.body.visible;
                        GuiCreationNewParts.biped.head.visible = !this.part.hiddenParts.contains((Object)BodyPart.HEAD);
                        boolean bl = GuiCreationNewParts.biped.hat.visible = GuiCreationNewParts.biped.hat.visible && GuiCreationNewParts.biped.head.visible;
                        if (this.part.bodyPart == BodyPart.HEAD) {
                            matrixstack.translate(24.0f, 34.0f, 25.0f);
                            matrixstack.scale(36.0f, 36.0f, 36.0f);
                            matrixstack.mulPose(Axis.XP.rotation(0.3926991f));
                            matrixstack.mulPose(Axis.YP.rotation((float)this.part.previewRotation * ((float)Math.PI / 180)));
                            GuiCreationNewParts.biped.head.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        }
                        if (this.part.bodyPart == BodyPart.LEGS) {
                            matrixstack.translate(18.0f, 4.0f, 25.0f);
                            matrixstack.scale(36.0f, 36.0f, 36.0f);
                            matrixstack.mulPose(Axis.XP.rotation(0.3926991f));
                            matrixstack.mulPose(Axis.YP.rotation((float)this.part.previewRotation * ((float)Math.PI / 180)));
                            GuiCreationNewParts.biped.body.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                            if (this.part.animationType == PartBehaviorType.LEGS) {
                                modelPart = this.part.getPart("right_leg");
                                if (modelPart != null) {
                                    modelPart.setRot(new NopVector3f(GuiCreationNewParts.biped.rightLeg.xRot, GuiCreationNewParts.biped.rightLeg.yRot, GuiCreationNewParts.biped.rightLeg.zRot));
                                    modelPart.setPos(new NopVector3f(GuiCreationNewParts.biped.rightLeg.x, GuiCreationNewParts.biped.rightLeg.y, GuiCreationNewParts.biped.rightLeg.z));
                                }
                                if ((modelPart = this.part.getPart("left_leg")) != null) {
                                    modelPart.setRot(new NopVector3f(GuiCreationNewParts.biped.leftLeg.xRot, GuiCreationNewParts.biped.leftLeg.yRot, GuiCreationNewParts.biped.leftLeg.zRot));
                                    modelPart.setPos(new NopVector3f(GuiCreationNewParts.biped.leftLeg.x, GuiCreationNewParts.biped.leftLeg.y, GuiCreationNewParts.biped.leftLeg.z));
                                }
                            }
                            GuiCreationNewParts.biped.rightLeg.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                            GuiCreationNewParts.biped.leftLeg.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        }
                        if (this.part.bodyPart == BodyPart.ARMS) {
                            matrixstack.translate(18.0f, 12.0f, 25.0f);
                            matrixstack.scale(36.0f, 36.0f, 36.0f);
                            matrixstack.mulPose(Axis.XP.rotation(0.3926991f));
                            matrixstack.mulPose(Axis.YP.rotation((float)this.part.previewRotation * ((float)Math.PI / 180)));
                            GuiCreationNewParts.biped.body.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                            if (this.part.animationType == PartBehaviorType.ARMS) {
                                modelPart = this.part.getPart("right_arm");
                                if (modelPart != null) {
                                    modelPart.setRot(new NopVector3f(GuiCreationNewParts.biped.rightArm.xRot, GuiCreationNewParts.biped.rightArm.yRot, GuiCreationNewParts.biped.rightArm.zRot));
                                    modelPart.setPos(new NopVector3f(GuiCreationNewParts.biped.rightArm.x, GuiCreationNewParts.biped.rightArm.y, GuiCreationNewParts.biped.rightArm.z));
                                }
                                if ((modelPart = this.part.getPart("left_arm")) != null) {
                                    modelPart.setRot(new NopVector3f(GuiCreationNewParts.biped.leftArm.xRot, GuiCreationNewParts.biped.leftArm.yRot, GuiCreationNewParts.biped.leftArm.zRot));
                                    modelPart.setPos(new NopVector3f(GuiCreationNewParts.biped.leftArm.x, GuiCreationNewParts.biped.leftArm.y, GuiCreationNewParts.biped.leftArm.z));
                                }
                            }
                            GuiCreationNewParts.biped.leftArm.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                            GuiCreationNewParts.biped.rightArm.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        }
                        if (this.part.bodyPart == BodyPart.BODY) {
                            matrixstack.translate(18.0f, 18.0f, 25.0f);
                            matrixstack.scale(36.0f, 36.0f, 36.0f);
                            matrixstack.mulPose(Axis.XP.rotation(0.3926991f));
                            matrixstack.mulPose(Axis.YP.rotation((float)this.part.previewRotation * ((float)Math.PI / 180)));
                            GuiCreationNewParts.biped.body.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        }
                        if (this.part.renderType != PartRenderType.NONE) {
                            MpmPartAbstractClient partc = (MpmPartAbstractClient)this.part;
                            partc.pos = NopVector3f.ZERO;
                            partc.rot = NopVector3f.ZERO;
                            LayerParts.renderPart(this.data, partc, matrixstack, (MultiBufferSource)irendertypebuffer$impl, 0xF000F0, (AbstractClientPlayer)minecraft.player, biped, GuiCreationNewParts.this.renderData);
                        }
                    } finally {
                        previousParts.forEach(noppes.mpm.client.RenderStateScope.PartState::restore);
                    }
                });
                graphics.flush();
            } finally {
                matrixstack.popPose();
                Lighting.setupFor3DItems();
            }
        }

        public void renderWidget(GuiGraphics graphics, int xMouse, int yMouse, float tick) {
            if (GuiCreationNewParts.this.hasSubGui()) {
                this.renderModel(graphics, xMouse, yMouse, tick);
                this.renderIcons(graphics, xMouse, yMouse, tick);
            }
        }

        public void renderIcons(GuiGraphics graphics, int xMouse, int yMouse, float tick) {
            int color = -1;
            if (!this.basic) {
                if (this.isHovered) {
                    color = -65536;
                }
                int x1 = this.getX();
                int x2 = this.getX() + 70;
                int y1 = this.getY();
                int y2 = this.getY() + 70 - 1;
                graphics.hLine(x1, x2, y1, color);
                graphics.hLine(x1, x2, y2, color);
                graphics.vLine(x1, y1, y2, color);
                graphics.vLine(x2, y1, y2, color);
                x1 = this.getX() + 70 - 16;
                x2 = this.getX() + 70;
                y1 = this.getY() + 1;
                y2 = this.getY() + 70 - 1;
                graphics.fill(x1, y1, x2, y2, -3750202);
                color = -1;
                x1 = this.getX() + 70 - 14;
                x2 = this.getX() + 70 - 2;
                y1 = this.getY() + 2;
                y2 = this.getY() + 14;
                graphics.fill(x1, y1, x2, y2, -16777216);
                graphics.hLine(x1, x2, y1, color);
                graphics.hLine(x1, x2, y2, color);
                graphics.vLine(x1, y1, y2, color);
                graphics.vLine(x2, y1, y2, color);
                if (!this.part.isEnabled) {
                    graphics.drawString(GuiCreationNewParts.this.font, (Component)Component.literal((String)"X").withStyle(ChatFormatting.BOLD), x1 + 4, y1 + 3, 0xFF0000);
                } else if (this.selected) {
                    char c = (char)Integer.parseInt("2713", 16);
                    graphics.drawString(GuiCreationNewParts.this.font, (Component)Component.literal((String)("" + c)).withStyle(ChatFormatting.BOLD), x1 + 3, y1 + 2, 65280);
                }
            }
            int guiY = this.getY() + 16;
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            int size = 14;
            int x1 = this.getX() + 70 - 15;
            int x2 = x1 + size;
            int y1 = guiY;
            int y2 = y1 + size;
            boolean bl = this.colorPickerHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
            if (this.colorPickerHovered) {
                --x1;
                --y1;
                size = 16;
            }
            graphics.blit(colorWheel, x1, y1, 0, 0.0f, 0.0f, size, size, size, size);
            guiY += 15;
            if (this.all.size() > 1) {
                x1 = this.getX() + 70 - 17;
                x2 = x1 + 6;
                y1 = guiY;
                y2 = y1 + 8;
                this.hoverL = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
                graphics.blit(GuiButtonBiDirectional.resource, x1, y1, 0, this.hoverL ? 76 : 60, 6, 8);
                String s = "" + this.all.indexOf(this.part);
                graphics.drawString(GuiCreationNewParts.this.font, s, (float)x1 + 9.5f - (float)GuiCreationNewParts.this.font.width(s) / 2.0f, (float)y1 + 0.5f, 0, false);
                x1 = this.getX() + 70 - 5;
                x2 = x1 + 6;
                y1 = guiY;
                y2 = y1 + 8;
                this.hoverR = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
                graphics.blit(GuiButtonBiDirectional.resource, x1, y1, 6, this.hoverR ? 76 : 60, 6, 8);
                guiY += 11;
            }
            if (!this.basic) {
                x1 = this.getX() + 70 - 15;
                x2 = x1 + 14;
                y1 = guiY;
                y2 = y1 + 14;
                this.settingsHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
                graphics.blit(GuiButtonBiDirectional.resource, x1, y1, 0, this.settingsHovered ? 140 : 126, 14, 14);
                size = 8;
                x1 = this.getX() + 70 - 10;
                x2 = x1 + size;
                y1 = this.getY() + 70 - 12;
                y2 = y1 + size;
                this.infoHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
                MutableComponent text = Component.literal((String)"i").withStyle(ChatFormatting.BOLD);
                if (this.infoHovered) {
                    text = text.withStyle(ChatFormatting.UNDERLINE);
                }
                graphics.drawString(GuiCreationNewParts.this.font, (Component)text, x1 + 3, y1 + 2, 0, false);
            }
        }

        public void onClick(double xMouse, double yMouse) {
            if (this.colorPickerHovered) {
                if (GuiCreationNewParts.this.hasSubGui()) {
                    GuiCreationNewParts.this.getSubGui().setSubGui(new GuiModelColor(GuiCreationNewParts.this, this.data.getColor(), color -> this.data.setColor(color)));
                } else {
                    GuiCreationNewParts.this.setSubGui(new GuiModelColor(GuiCreationNewParts.this, this.data.getColor(), color -> this.data.setColor(color)));
                }
            } else if (this.hoverL) {
                int index = (this.all.indexOf(this.part) + this.all.size() - 1) % this.all.size();
                this.part = this.all.get(index);
                this.data.partId = this.part.id;
            } else if (this.hoverR) {
                int index = (this.all.indexOf(this.part) + 1) % this.all.size();
                this.part = this.all.get(index);
                this.data.partId = this.part.id;
            } else if (this.settingsHovered) {
                if (this.data instanceof ModelEyeData) {
                    GuiCreationNewParts.this.setSubGui(new EyesPart((ModelEyeData)this.data, (MpmPartEyes)this.part));
                } else {
                    GuiCreationNewParts.this.setSubGui(new TexturePart(this.data, this.part));
                }
            } else if (this.part.isEnabled && !this.basic) {
                boolean bl = this.selected = !this.selected;
                if (this.selected) {
                    GuiCreationNewParts.this.data.mpmParts.add(this.data);
                } else {
                    GuiCreationNewParts.this.data.mpmParts.removeIf(t -> t.partId.equals((Object)this.data.partId));
                }
            }
            GuiCreationNewParts.this.data.refreshParts();
        }
    }

    class EyesPart
    extends GuiNPCInterface {
        private MpmPartEyes part;
        private ModelEyeData data;

        public EyesPart(ModelEyeData data, MpmPartEyes part) {
            this.data = data;
            this.part = part;
            this.xSize = 310;
            this.ySize = 200;
            this.closeOnEsc = true;
        }

        @Override
        public void init() {
            super.init();
            this.guiLeft += 55;
            int y = this.guiTop + 8;
            this.addButton(new GuiButtonBiDirectional(21, this.guiLeft + 110, y, 110, 20, new String[]{"gui.playerskin", "gui.normal", "gui.texture"}, this.data.skinType, b -> {
                this.data.skinType = ((GuiButtonBiDirectional)b).getValue();
                this.init();
            }));
            this.addLabel(new GuiNpcLabel(21, "part.eyes", this.guiLeft + 56, y + 5));
            if (this.data.skinType == 1) {
                this.addButton(new GuiColorButton(3, this.guiLeft + 230, y, ColorUtil.rgbToColor(this.data.color), b -> this.setSubGui(new GuiModelColor(GuiCreationNewParts.this, ColorUtil.rgbToColor(this.data.color), color -> {
                    this.data.color = ColorUtil.colorToRgb(color);
                }))));
            }
            y += 25;
            if (this.data.skinType == 2) {
                this.addTextField(new GuiNpcTextField(this, 2, this.guiLeft + 110, y, 195, 20, this.data.url, tf -> this.data.setUrl(tf.getValue())));
                this.addLabel(new GuiNpcLabel(2, "config.skinurl", this.guiLeft + 56, y + 5));
            }
            this.addButton(new GuiButtonBiDirectional(22, this.guiLeft + 54, y += 25, 100, 20, new String[]{"gui.normal", "gui.big"}, this.data.eyeSize, b -> {
                this.data.eyeSize = ((GuiButtonBiDirectional)b).getValue();
                this.init();
            }));
            if (this.data.glint || this.data.skinType == 1 || this.data.skinType == 2) {
                this.addButton(new GuiButtonBiDirectional(23, this.guiLeft + 156, y, 100, 20, new String[]{"gui.normal", "gui.mirror"}, this.data.mirror ? 1 : 0, b -> {
                    this.data.mirror = ((GuiButtonBiDirectional)b).getValue() == 1;
                    this.init();
                }));
            }
            this.addLabel(new GuiNpcLabel(3, "eye.pupil", this.guiLeft + 4, y + 5));
            this.addButton(new GuiButtonBiDirectional(51, this.guiLeft + 54, y += 25, 100, 20, new String[]{I18n.get((String)"gui.down", (Object[])new Object[0]) + "x2", "gui.down", "gui.normal", "gui.up", I18n.get((String)"gui.up", (Object[])new Object[0]) + "x2"}, this.data.eyePos.y + 2, b -> {
                this.data.eyePos = new NopVector2i(this.data.eyePos.x, ((GuiButtonBiDirectional)b).getValue() - 2);
            }));
            this.addButton(new GuiButtonBiDirectional(50, this.guiLeft + 156, y, 100, 20, new String[]{"gui.inward", "gui.normal", "gui.outward"}, this.data.eyePos.x + 1, b -> {
                this.data.eyePos = new NopVector2i(((GuiButtonBiDirectional)b).getValue() - 1, this.data.eyePos.y);
            }));
            this.addLabel(new GuiNpcLabel(50, "gui.position", this.guiLeft + 4, y + 5));
            this.addButton(new GuiNpcButtonYesNo(34, this.guiLeft + 54, y += 25, this.data.glint, b -> {
                this.data.glint = ((GuiNpcButtonYesNo)b).getBoolean();
                this.init();
            }));
            this.addLabel(new GuiNpcLabel(34, "eye.glint", this.guiLeft + 4, y + 5));
            this.addButton(new GuiColorButton(35, this.guiLeft + 162, y, ColorUtil.rgbToColor(this.data.browColor), b -> this.setSubGui(new GuiModelColor(GuiCreationNewParts.this, ColorUtil.rgbToColor(this.data.browColor), color -> {
                this.data.browColor = ColorUtil.colorToRgb(color);
            }))));
            this.addButton(new GuiButtonBiDirectional(36, this.guiLeft + 214, y, 70, 20, new String[]{"gui.disabled", "1", "2", "3", "4", "5", "6", "7", "8"}, (int)(this.data.browThickness.y * 10.0f), b -> {
                this.data.browThickness = new NopVector3f(1.0f, (float)((GuiButtonBiDirectional)b).getValue() / 10.0f, 1.0f);
            }));
            this.addLabel(new GuiNpcLabel(35, "eye.lash", this.guiLeft + 112, y + 5));
            this.addButton(new GuiNpcButtonYesNo(40, this.guiLeft + 54, y += 25, !this.data.disableBlink, b -> {
                this.data.disableBlink = !((GuiNpcButtonYesNo)b).getBoolean();
                this.init();
            }));
            this.addLabel(new GuiNpcLabel(40, "eye.blink", this.guiLeft + 4, y + 5));
            if (!this.data.disableBlink) {
                this.addButton(new GuiColorButton(41, this.guiLeft + 162, y, ColorUtil.rgbToColor(this.data.lidColor), b -> this.setSubGui(new GuiModelColor(GuiCreationNewParts.this, ColorUtil.rgbToColor(this.data.lidColor), color -> {
                    this.data.lidColor = ColorUtil.colorToRgb(color);
                }))));
                this.addLabel(new GuiNpcLabel(41, "eye.lid", this.guiLeft + 112, y + 5));
            }
            this.addButton(new GuiNpcButton(66, this.guiLeft + 288, this.guiTop + 4, 20, 20, "X", b -> this.close()));
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.fill(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, -3750202);
            graphics.hLine(this.guiLeft, this.guiLeft + this.xSize, this.guiTop, -1);
            graphics.hLine(this.guiLeft, this.guiLeft + this.xSize, this.guiTop + this.ySize, -1);
            graphics.vLine(this.guiLeft, this.guiTop, this.guiTop + this.ySize, -1);
            graphics.vLine(this.guiLeft + this.xSize, this.guiTop, this.guiTop + this.ySize, -1);
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            try {
                posestack.translate((double)(this.guiLeft + 10), (double)(this.guiTop + 10), 150.0);
                posestack.scale(1.0f, 1.0f, -1.0f);
                RenderSystem.applyModelViewMatrix();
                PoseStack matrixstack = new PoseStack();
                matrixstack.pushPose();
                try {
                    EntityRenderDispatcher entityrenderermanager = this.minecraft.getEntityRenderDispatcher();
                    MultiBufferSource.BufferSource irendertypebuffer$impl = this.minecraft.renderBuffers().bufferSource();
                    VertexConsumer ivertex = irendertypebuffer$impl.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)this.player.getSkin().texture()));
                    Lighting.setupForEntityInInventory();
                    noppes.mpm.client.RenderStateScope.runAsFancy(() -> {
                        var previousParts = noppes.mpm.client.RenderStateScope.snapshot(biped);
                        try (var partScope = this.part instanceof MpmPartAbstractClient clientPart ? clientPart.saveRenderState() : null) {
                            GuiCreationNewParts.biped.body.visible = !this.part.hiddenParts.contains((Object)BodyPart.BODY);
                            GuiCreationNewParts.biped.jacket.visible = GuiCreationNewParts.biped.jacket.visible && GuiCreationNewParts.biped.body.visible;
                            GuiCreationNewParts.biped.head.visible = !this.part.hiddenParts.contains((Object)BodyPart.HEAD);
                            GuiCreationNewParts.biped.hat.visible = GuiCreationNewParts.biped.hat.visible && GuiCreationNewParts.biped.head.visible;
                            matrixstack.translate(19.0f, 43.0f, 25.0f);
                            matrixstack.scale(100.0f, 100.0f, 100.0f);
                            GuiCreationNewParts.biped.head.render(noppes.mpm.client.RenderStateScope.copyPose(matrixstack), ivertex, 0xF000F0, OverlayTexture.NO_OVERLAY);
                            this.part.pos = NopVector3f.ZERO;
                            this.part.rot = NopVector3f.ZERO;
                            LayerParts.renderPart(this.data, this.part, matrixstack, (MultiBufferSource)irendertypebuffer$impl, 0xF000F0, (AbstractClientPlayer)this.minecraft.player, biped, GuiCreationNewParts.this.renderData);
                        } finally {
                            previousParts.forEach(noppes.mpm.client.RenderStateScope.PartState::restore);
                        }
                    });
                    irendertypebuffer$impl.endBatch();
                } finally {
                    matrixstack.popPose();
                }
            } finally {
                posestack.popPose();
                Lighting.setupFor3DItems();
                RenderSystem.applyModelViewMatrix();
            }
        }

        @Override
        public void render(GuiGraphics graphics, int i, int j, float f) {
            super.render(graphics, i, j, f);
        }

        @Override
        public void save() {
        }
    }

    class TexturePart
    extends GuiNPCInterface {
        private MpmPart part;
        private MpmPartData data;
        private GuiMpmPart partGui;

        public TexturePart(MpmPartData data, MpmPart part) {
            this.data = data;
            this.part = part;
            this.xSize = 310;
            this.ySize = 200;
            this.closeOnEsc = true;
        }

        @Override
        public void init() {
            super.init();
            this.guiLeft += 55;
            this.partGui = new GuiMpmPart(this.guiLeft + 2, this.guiTop + 2, this.part);
            this.partGui.zPos = 250;
            this.partGui.basic = true;
            this.addRenderableWidget(this.partGui);
            if (!this.part.disableCustomTextures) {
                this.addLabel(new GuiNpcLabel(21, "gui.playerskin", this.guiLeft + 4, this.guiTop + 110));
                this.addButton(new GuiNpcButtonYesNo(21, this.guiLeft + 76, this.guiTop + 105, this.data.usePlayerSkin, b -> {
                    this.data.usePlayerSkin = ((GuiNpcButtonYesNo)b).getBoolean();
                    this.init();
                }));
                if (!this.data.usePlayerSkin) {
                    this.addLabel(new GuiNpcLabel(1, "gui.texture", this.guiLeft + 4, this.guiTop + 130));
                    ResourceLocation loc = this.data.getDefaultTexture();
                    this.addTextField(new GuiNpcTextField(this, 1, this.guiLeft + 4, this.guiTop + 140, 220, 20, loc == null ? "" : loc.toString(), tf -> this.data.setTexture(tf.getValue())));
                    this.addButton(new GuiNpcButton(1, this.guiLeft + 226, this.guiTop + 140, 80, 20, "gui.select", b -> this.setSubGui(new GuiTextureSelection(GuiCreationNewParts.this.data, loc, r -> {
                        this.data.texture = r;
                    }))));
                    this.addLabel(new GuiNpcLabel(2, "config.skinurl", this.guiLeft + 4, this.guiTop + 168));
                    this.addTextField(new GuiNpcTextField(this, 2, this.guiLeft + 4, this.guiTop + 178, 220, 20, this.data.url, tf -> this.data.setUrl(tf.getValue())));
                }
            }
            this.addButton(new GuiNpcButton(66, this.guiLeft + 276, this.guiTop + 4, 20, 20, "X", b -> this.close()));
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.fill(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, -3750202);
            graphics.hLine(this.guiLeft, this.guiLeft + this.xSize, this.guiTop, -1);
            graphics.hLine(this.guiLeft, this.guiLeft + this.xSize, this.guiTop + this.ySize, -1);
            graphics.vLine(this.guiLeft, this.guiTop, this.guiTop + this.ySize, -1);
            graphics.vLine(this.guiLeft + this.xSize, this.guiTop, this.guiTop + this.ySize, -1);
        }

        @Override
        public void render(GuiGraphics graphics, int i, int j, float f) {
            super.render(graphics, i, j, f);
            if (!this.hasSubGui()) {
                this.partGui.renderModel(graphics, i, j, f);
            }
        }

        @Override
        public void save() {
        }
    }
}
