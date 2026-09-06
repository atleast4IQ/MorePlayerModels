/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.ConfirmLinkScreen
 *  net.minecraft.client.gui.screens.ConfirmScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforgespi.language.IModInfo
 */
package noppes.mpm.client.gui;

import java.util.ArrayList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import noppes.mpm.ModelData;
import noppes.mpm.client.Preset;
import noppes.mpm.client.PresetController;
import noppes.mpm.client.RenderEvent;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.client.gui.GuiConfig;
import noppes.mpm.client.gui.GuiCreationLoad;
import noppes.mpm.client.gui.GuiCreationNewParts;
import noppes.mpm.client.gui.GuiCreationOptions;
import noppes.mpm.client.gui.GuiCreationScreenInterface;
import noppes.mpm.client.gui.GuiEditEmotes;
import noppes.mpm.client.gui.util.GuiCustomScroll;
import noppes.mpm.client.gui.util.GuiLinkButton;
import noppes.mpm.client.gui.util.GuiNPCInterface;
import noppes.mpm.client.gui.util.GuiNpcButton;
import noppes.mpm.client.gui.util.ICustomScrollListener;
import noppes.mpm.client.gui.util.ISubGuiListener;
import noppes.mpm.packets.Packets;
import noppes.mpm.packets.server.PacketPlayerDataUpdate;

public class GuiMPM
extends GuiNPCInterface
implements ICustomScrollListener,
ISubGuiListener {
    public static final ResourceLocation resource = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/smallbg.png");
    public ModelData playerdata;
    protected CompoundTag original = new CompoundTag();
    private GuiCustomScroll scroll = null;
    private static boolean showMenu = false;

    public GuiMPM() {
        this.playerdata = ModelData.get((Player)Minecraft.getInstance().player);
        this.original = this.playerdata.writeToNBT();
        this.xSize = 182;
        this.ySize = 185;
        this.drawDefaultBackground = false;
        this.closeOnEsc = true;
        if (PresetController.instance.presets.isEmpty()) {
            PresetController.instance.load();
        }
    }

    @Override
    public void init() {
        this.xSize = showMenu ? 240 : 182;
        super.init();
        if (this.scroll == null) {
            this.scroll = new GuiCustomScroll(this, 0);
            this.scroll.setSize(90, 184);
        }
        ArrayList<String> list = new ArrayList<String>();
        for (Preset preset : PresetController.instance.presets.values()) {
            if (!preset.menu) continue;
            list.add(preset.name);
        }
        this.scroll.setList(list);
        this.scroll.setSelected(this.playerdata.presetName);
        if (!this.scroll.hasSelected()) {
            this.scroll.setSelectedIndex(0);
        }
        this.scroll.guiLeft = this.guiLeft + 4;
        this.scroll.guiTop = this.guiTop + 12;
        this.addScroll(this.scroll);
        this.addButton(new GuiNpcButton(0, this.guiLeft + 98, this.guiTop + 176, 78, 20, "gui.menu", button -> {
            showMenu = !showMenu;
            this.init();
        }));
        if (showMenu) {
            this.addButton(new GuiNpcButton(1, this.guiLeft - 91, this.guiTop + 10, 90, 20, "gui.add", button -> this.setSubGui(new GuiCreationLoad())));
            this.addButton(new GuiNpcButton(2, this.guiLeft - 91, this.guiTop + 31, 90, 20, "gui.remove", button -> {
                ConfirmScreen gui = new ConfirmScreen(result -> {
                    if (result) {
                        PresetController.instance.removePreset(this.scroll.getSelected());
                        this.scroll.getList().remove(this.scroll.getSelected());
                        Preset preset = PresetController.instance.getPreset(this.scroll.getList().get(0));
                        this.playerdata.readFromNBT(preset.data.writeToNBT());
                        this.playerdata.presetName = preset.name;
                    }
                    Minecraft.getInstance().setScreen((Screen)this);
                }, (Component)Component.empty(), (Component)Component.translatable((String)"message.delete"));
                this.minecraft.setScreen((Screen)gui);
            }));
            this.addButton(new GuiNpcButton(3, this.guiLeft - 91, this.guiTop + 52, 90, 20, "gui.editmodel", button -> {
                try {
                    this.setSubGui((GuiNPCInterface)((Object)((Object)GuiCreationScreenInterface.Gui.getClass().newInstance())));
                }
                catch (InstantiationException instantiationException) {
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }));
            this.addButton(new GuiNpcButton(8, this.guiLeft - 91, this.guiTop + 73, 90, 20, "gui.parts", button -> {
                ModelData data = ModelData.get((Player)this.player);
                this.setSubGui(new GuiCreationNewParts(data));
            }));
            this.addButton(new GuiNpcButton(4, this.guiLeft - 91, this.guiTop + 94, 90, 20, "gui.options", button -> {
                ModelData data = ModelData.get((Player)this.player);
                this.setSubGui(new GuiCreationOptions(data));
            }));
            this.getButton((int)4).active = this.scroll.getList().size() > 1;
            this.getButton((int)3).active = this.getButton((int)4).active;
            this.getButton((int)2).active = this.getButton((int)4).active;
            this.addButton(new GuiNpcButton(5, this.guiLeft - 91, this.guiTop + 138, 90, 20, "gui.config", button -> this.setSubGui(new GuiConfig())));
            this.addButton(new GuiNpcButton(6, this.guiLeft - 91, this.guiTop + 159, 90, 20, "gui.emotes", button -> this.setSubGui(new GuiEditEmotes())));
            this.addButton(new GuiNpcButton(7, this.guiLeft - 91, this.guiTop + 180, 90, 20, "config.reloadskins", button -> SkinUtil.reloadSkins()));
            this.addRenderableWidget(new GuiLinkButton(this.guiLeft + 184, this.guiTop + 40, (Component)Component.literal((String)"- Website"), button -> this.setScreen((Screen)new ConfirmLinkScreen(bo -> {
                if (bo) {
                    Util.getPlatform().openUri("http://www.kodevelopment.nl/minecraft/moreplayermodels/");
                }
                this.setScreen(this);
            }, "http://www.kodevelopment.nl/minecraft/moreplayermodels/", true))));
            this.addRenderableWidget(new GuiLinkButton(this.guiLeft + 184, this.guiTop + 52, 7506394, (Component)Component.literal((String)"- Discord"), button -> this.setScreen((Screen)new ConfirmLinkScreen(bo -> {
                if (bo) {
                    Util.getPlatform().openUri("http://www.kodevelopment.nl/discord");
                }
                this.setScreen(this);
            }, "http://www.kodevelopment.nl/discord", true))));
            this.addRenderableWidget(new GuiLinkButton(this.guiLeft + 184, this.guiTop + 64, 16345172, (Component)Component.literal((String)"- Patreon"), button -> this.setScreen((Screen)new ConfirmLinkScreen(bo -> {
                if (bo) {
                    Util.getPlatform().openUri("https://www.patreon.com/Noppes");
                }
                this.setScreen(this);
            }, "https://www.patreon.com/Noppes", true))));
            this.addRenderableWidget(new GuiLinkButton(this.guiLeft + 184, this.guiTop + 76, 16766720, (Component)Component.literal((String)"- Part packs"), button -> this.setScreen((Screen)new ConfirmLinkScreen(bo -> {
                if (bo) {
                    Util.getPlatform().openUri("http://www.kodevelopment.nl/minecraft/moreplayermodels/packs/");
                }
                this.setScreen(this);
            }, "http://www.kodevelopment.nl/minecraft/moreplayermodels/packs/", true))));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float f) {
        this.renderBackground(graphics, 0, 0, 0);
        graphics.blit(resource, this.guiLeft, this.guiTop + 8, 0, 0, this.xSize, 192);
        super.render(graphics, x, y, f);
        LivingEntity entity = this.playerdata.getEntity((Player)this.minecraft.player);
        if (entity == null) {
            entity = this.player;
        }
        if (!this.hasSubGui()) {
            this.renderEntityPreview(graphics, entity, this.guiLeft + 140, this.guiTop + 140, 56, x, y, this.playerdata.resourceLocation);
            if (showMenu) {
                IModInfo info = ModList.get().getMods().stream().filter(t -> t.getModId().equals("moreplayermodels")).findFirst().get();
                graphics.drawString(this.font, (Component)Component.literal((String)("More Player Models " + info.getVersion().toString())), this.guiLeft + 184, this.guiTop + 8, 0xFFFFFF);
                graphics.drawString(this.font, (Component)Component.literal((String)"by Noppes"), this.guiLeft + 184, this.guiTop + 18, 0xFFFFFF);
            }
        }
    }

    @Override
    public void scrollClicked(double i, double j, int button, GuiCustomScroll scroll) {
        Preset preset = PresetController.instance.getPreset(scroll.getSelected());
        if (preset != null) {
            this.playerdata.readFromNBT(preset.data.writeToNBT());
            this.playerdata.presetName = preset.name;
        }
    }

    @Override
    public void save() {
        CompoundTag newCompound = this.playerdata.writeToNBT();
        if (!this.original.equals((Object)newCompound)) {
            this.playerdata.lastEdited = System.currentTimeMillis();
            newCompound = this.playerdata.writeToNBT();
            this.playerdata.save();
            Packets.sendServer(new PacketPlayerDataUpdate(newCompound));
            this.original = newCompound;
        }
    }

    @Override
    public void subGuiClosed(GuiNPCInterface subgui) {
        Preset p;
        if ((subgui instanceof GuiCreationScreenInterface || subgui instanceof GuiCreationOptions || subgui instanceof GuiCreationNewParts) && (p = PresetController.instance.getPreset(this.getScroll(0).getSelected())) != null) {
            p.data = this.playerdata.copy();
            PresetController.instance.save();
        }
    }

    @Override
    public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }
}
