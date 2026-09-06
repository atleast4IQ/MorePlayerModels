/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.RenderEvent;
import noppes.mpm.client.gui.select.GuiTextureSelection;
import noppes.mpm.client.gui.util.GuiButtonBiDirectional;
import noppes.mpm.client.gui.util.GuiNPCInterface;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.GuiNpcButtonYesNo;
import noppes.mpm.client.gui.util.GuiNpcLabel;
import noppes.mpm.client.gui.util.GuiNpcTextField;
import noppes.mpm.client.gui.util.ITextfieldListener;

public class GuiCreationOptions
extends GuiNPCInterface
implements ITextfieldListener {
    private ModelData playerdata;

    public GuiCreationOptions(ModelData data) {
        this.xSize = 366;
        this.ySize = 226;
        this.closeOnEsc = true;
        this.setBackground("menubg.png");
        this.playerdata = data;
    }

    @Override
    public void init() {
        super.init();
        this.addButton(new GuiButtonBiDirectional(this, 9, this.guiLeft + 70, this.guiTop + 50, 100, 20, new String[]{"gui.default", "config.humanfemale", "config.humanmale", "config.goblinmale"}, this.playerdata.soundType));
        this.addLabel(new GuiNpcLabel(5, "config.sounds", this.guiLeft + 4, this.guiTop + 55));
        this.addButton(new GuiButtonBiDirectional(this, 11, this.guiLeft + 70, this.guiTop + 72, 100, 20, new String[]{"gui.default", "config.steve", "config.slim"}, this.playerdata.modelType));
        this.addLabel(new GuiNpcLabel(53, "config.modeltype", this.guiLeft + 4, this.guiTop + 77));
        this.addButton(new GuiNpcButtonYesNo(12, this.guiLeft + 70, this.guiTop + 94, 100, 20, this.playerdata.wingMode == 1, b -> {
            this.playerdata.wingMode = ((GuiNpcButton)b).getValue();
        }));
        this.addLabel(new GuiNpcLabel(54, "config.hideelytra", this.guiLeft + 4, this.guiTop + 99));
        this.addTextField(new GuiNpcTextField(52, this, this.guiLeft + 4, this.guiTop + 150, 266, 20, this.playerdata.url));
        this.addLabel(new GuiNpcLabel(52, "config.skinurl", this.guiLeft + 4, this.guiTop + 140));
        this.addButton(new GuiNpcButton(10, this.guiLeft + 272, this.guiTop + 150, 80, 20, "gui.select", button -> {
            ResourceLocation loc = null;
            if (this.playerdata.url != null && !this.playerdata.url.isEmpty() && !this.playerdata.url.startsWith("http")) {
                loc = ResourceLocation.tryParse(this.playerdata.url);
            }
            this.setSubGui(new GuiTextureSelection(this.playerdata, loc, resource -> {
                this.playerdata.url = resource == null ? "" : resource.toString();
                this.playerdata.resourceInit = false;
                this.playerdata.resourceLoaded = false;
                this.playerdata.resourceLocation = null;
            }));
        }));
        this.addButton(new GuiNpcButton(this, 66, this.guiLeft + this.xSize - 24, this.guiTop + 4, 20, 20, "X"));
    }

    @Override
    public void buttonEvent(GuiNpcButton button) {
        if (button.id == 9) {
            this.playerdata.soundType = (short)button.getValue();
        }
        if (button.id == 11) {
            this.playerdata.modelType = button.getValue();
        }
        if (button.id == 66) {
            this.close();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float f) {
        super.render(graphics, x, y, f);
        LivingEntity entity = this.playerdata.getEntity((Player)this.minecraft.player);
        if (entity == null) {
            entity = this.player;
        }
        if (!this.hasSubGui()) {
            this.renderEntityPreview(graphics, entity, this.guiLeft + 270, this.guiTop + 120, 56, x, y, this.playerdata.resourceLocation);
        }
    }

    @Override
    public void save() {
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        this.playerdata.url = guiNpcTextField.getValue();
        this.playerdata.resourceInit = false;
        this.playerdata.resourceLoaded = false;
        this.playerdata.resourceLocation = null;
    }
}
