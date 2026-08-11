/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.gui.GuiCreationEntities;
import noppes.mpm.client.gui.GuiCreationExtra;
import noppes.mpm.client.gui.GuiCreationScale;
import noppes.mpm.client.gui.select.GuiTextureSelection;
import noppes.mpm.client.gui.util.GuiNPCInterface;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.GuiNpcLabel;
import noppes.mpm.client.gui.util.GuiNpcSlider;
import noppes.mpm.client.gui.util.ISliderListener;
import noppes.mpm.client.gui.util.ISubGuiListener;
import noppes.mpm.util.MPMEntityUtil;

public abstract class GuiCreationScreenInterface
extends GuiNPCInterface
implements ISubGuiListener,
ISliderListener {
    public static String Message = "";
    public LivingEntity entity;
    public int active = 0;
    private Player player;
    public int xOffset = 0;
    public ModelData playerdata;
    public static GuiCreationScreenInterface Gui = new GuiCreationEntities();
    private static float rotation = 0.5f;

    public GuiCreationScreenInterface() {
        this.playerdata = ModelData.get((Player)Minecraft.getInstance().player);
        this.xSize = 400;
        this.ySize = 240;
        this.xOffset = 140;
        this.player = Minecraft.getInstance().player;
        this.closeOnEsc = true;
    }

    @Override
    public void init() {
        super.init();
        this.entity = this.playerdata.getEntity((Player)this.minecraft.player);
        this.addButton(new GuiNpcButton(this, 1, this.guiLeft + 62, this.guiTop, 60, 20, "gui.entity"){

            @Override
            public void onClick(double x, double y) {
                GuiCreationScreenInterface.this.openGui(new GuiCreationEntities());
            }
        });
        if (this.entity != null) {
            GuiCreationExtra gui = new GuiCreationExtra();
            gui.playerdata = this.playerdata;
            if (!gui.getData(this.entity).isEmpty()) {
                this.addButton(new GuiNpcButton(this, 2, this.guiLeft, this.guiTop + 23, 60, 20, "gui.extra"){

                    @Override
                    public void onClick(double x, double y) {
                        GuiCreationScreenInterface.this.openGui(new GuiCreationExtra());
                    }
                });
            } else if (this.active == 2) {
                this.openGui(new GuiCreationEntities());
                return;
            }
        }
        if (this.entity == null) {
            this.addButton(new GuiNpcButton(this, 3, this.guiLeft + 62, this.guiTop + 23, 60, 20, "gui.scale"){

                @Override
                public void onClick(double x, double y) {
                    GuiCreationScreenInterface.this.openGui(new GuiCreationScale());
                }
            });
        }
        this.getButton((int)this.active).active = false;
        this.addButton(new GuiNpcButton(this, 66, this.guiLeft + this.xSize - 20, this.guiTop, 20, 20, "X"){

            @Override
            public void onClick(double x, double y) {
                GuiCreationScreenInterface.this.close();
            }
        });
        this.addLabel(new GuiNpcLabel(0, Message, this.guiLeft + 120, this.guiTop + this.ySize - 10, 0xFF0000));
        this.getLabel(0).center(this.xSize - 120);
        this.addSlider(new GuiNpcSlider(this, 500, this.guiLeft + this.xOffset + 142, this.guiTop + 210, 120, 20, rotation));
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float f) {
        super.render(graphics, x, y, f);
        LivingEntity entity = this.entity = this.playerdata.getEntity((Player)this.minecraft.player);
        if (entity == null) {
            entity = this.player;
        } else {
            // This is intentionally the original MPM creation-screen route:
            // entity selection affects the player in-game, but the editor
            // preview remains the player model so its armor layer copies the
            // player pose. Rendering a selected mob here mixes its armor
            // model with the player texture and causes the visible clipping.
            MPMEntityUtil.copy((LivingEntity)this.minecraft.player, (LivingEntity)this.player);
        }
        if (!(this.getSubGui() instanceof GuiTextureSelection)) {
            this.drawEntity(graphics, (LivingEntity)this.player, this.xOffset + 200, 200, 2.0f, (int)(-rotation * 360.0f) + 180, this.guiLeft, this.guiTop);
        }
    }

    @Override
    public void save() {
    }

    @Override
    public boolean drawSubGuiBackground() {
        return true;
    }

    public void openGui(GuiNPCInterface gui) {
        this.parent.setSubGui(gui);
        if (gui instanceof GuiCreationScreenInterface) {
            Gui = (GuiCreationScreenInterface)gui;
        }
    }

    @Override
    public void subGuiClosed(GuiNPCInterface subgui) {
        this.init();
    }

    @Override
    public void mouseDragged(GuiNpcSlider slider) {
        if (slider.id == 500) {
            rotation = slider.sliderValue;
            slider.setString("" + (int)(rotation * 360.0f));
        }
    }
}
