/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package noppes.mpm.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.ModelData;
import noppes.mpm.client.Preset;
import noppes.mpm.client.PresetController;
import noppes.mpm.client.RenderEvent;
import noppes.mpm.client.gui.util.GuiCustomScroll;
import noppes.mpm.client.gui.util.GuiNPCInterface;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.GuiNpcTextField;
import noppes.mpm.client.gui.util.ICustomScrollListener;

public class GuiCreationLoad
extends GuiNPCInterface
implements ICustomScrollListener {
    private List<String> list = new ArrayList<String>();
    private GuiCustomScroll scroll;
    private static final ResourceLocation resource = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/smallbg.png");
    private ModelData playerdata;
    private CompoundTag original = new CompoundTag();
    private String selected = "Normal";
    private HashMap<String, Preset> presets = Preset.GetDefault();

    public GuiCreationLoad() {
        this.playerdata = ModelData.get((Player)Minecraft.getInstance().player);
        this.original = this.playerdata.writeToNBT();
        this.drawDefaultBackground = false;
        this.closeOnEsc = true;
        this.presets.putAll(PresetController.instance.presets);
    }

    @Override
    public void init() {
        super.init();
        if (this.scroll == null) {
            this.scroll = new GuiCustomScroll(this, 0);
            for (Preset preset : this.presets.values()) {
                this.list.add(preset.name);
            }
            this.scroll.setList(this.list);
            this.scroll.setSelected(this.selected);
            this.scroll.scrollTo(this.selected);
        }
        this.scroll.guiLeft = this.guiLeft + 4;
        this.scroll.guiTop = this.guiTop + 33;
        this.scroll.setSize(100, 144);
        this.addScroll(this.scroll);
        this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 4, this.guiTop + 12, 172, 20, I18n.get((String)"gui.new", (Object[])new Object[0])));
        this.addButton(new GuiNpcButton(this, 10, this.guiLeft + 4, this.guiTop + this.ySize - 46, 86, 20, "gui.done"));
        this.addButton(new GuiNpcButton(this, 11, this.guiLeft + 92, this.guiTop + this.ySize - 46, 86, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiNpcButton btn) {
        if (btn.id == 10) {
            this.original = this.playerdata.writeToNBT();
            Preset p = new Preset();
            p.menu = true;
            String name = this.getTextField(0).getValue();
            if (((String)name).trim().isEmpty()) {
                name = I18n.get("gui.new");
            }
            while (PresetController.instance.presets.containsKey(name.toLowerCase())) {
                name = name + "_";
            }
            p.name = name;
            p.data = this.playerdata.copy();
            p.data.presetName = name;
            PresetController.instance.addPreset(p);
            this.close();
        }
        if (btn.id == 11) {
            this.close();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float f) {
        this.renderBackground(graphics, 0, 0, 0);
        graphics.blit(resource, this.guiLeft, this.guiTop + 8, 0, 0, this.xSize, 192);
        super.render(graphics, x, y, f);
        this.renderEntityPreview(graphics, this.player, this.guiLeft + 144, this.guiTop + 140, 40, x, y, this.playerdata.resourceLocation);
    }

    @Override
    public void scrollClicked(double i, double j, int k, GuiCustomScroll scroll) {
        this.selected = scroll.getSelected();
        Preset preset = this.presets.get(this.selected.toLowerCase());
        if (preset != null) {
            this.playerdata.readFromNBT(preset.data.writeToNBT());
            this.init();
        }
    }

    @Override
    public void save() {
        this.playerdata.readFromNBT(this.original);
    }

    @Override
    public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }
}
