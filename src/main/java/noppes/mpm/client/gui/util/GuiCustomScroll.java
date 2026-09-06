/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package noppes.mpm.client.gui.util;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.mpm.client.gui.util.GuiNpcTextField;
import noppes.mpm.client.gui.util.ICustomScrollListener;
import noppes.mpm.mixin.MouseHelperMixin;
import noppes.mpm.util.NaturalOrderComparator;

public class GuiCustomScroll
extends Screen {
    public static final ResourceLocation resource = ResourceLocation.fromNamespaceAndPath("moreplayermodels", "textures/gui/misc.png");
    protected List<String> list;
    private List<String> filteredList = new ArrayList<String>();
    public int id;
    public int guiLeft = 0;
    public int guiTop = 0;
    public String selected = "";
    private HashSet<String> selectedList;
    private int hover;
    private int listHeight;
    private int scrollY;
    private int maxScrollY;
    private int scrollHeight;
    private boolean isScrolling;
    public boolean multipleSelection = false;
    private ICustomScrollListener listener;
    private boolean isSorted = true;
    public boolean visible = true;
    private boolean selectable = true;
    private boolean mouseInList = false;
    private String lastClickedItem;
    private long lastClickedTime = 0L;
    private GuiNpcTextField textField;
    private boolean hasSearch = true;
    private String search = "";

    public GuiCustomScroll(Screen parent, int id) {
        super((Component)Component.empty());
        this.width = 176;
        this.height = 159;
        this.hover = -1;
        this.selectedList = new HashSet();
        this.listHeight = 0;
        this.scrollY = 0;
        this.scrollHeight = 0;
        this.isScrolling = false;
        if (parent instanceof ICustomScrollListener) {
            this.listener = (ICustomScrollListener)parent;
        }
        this.list = new ArrayList<String>();
        this.id = id;
        this.textField = new GuiNpcTextField(0, null, 0, 0, 176, 20, "");
    }

    public GuiCustomScroll(Screen parent, int id, boolean multipleSelection) {
        this(parent, id);
        this.multipleSelection = multipleSelection;
    }

    public void setSize(int x, int y) {
        this.textField.setWidth(x);
        this.height = y - this.textFieldHeight();
        this.width = x;
        this.listHeight = 14 * this.getActiveList().size();
        this.scrollHeight = this.listHeight > 0 ? (int)((double)(this.height - 8) / (double)this.listHeight * (double)(this.height - 8)) : Integer.MAX_VALUE;
        this.maxScrollY = this.listHeight - (this.height - 8) - 1;
        if (this.maxScrollY > 0 && this.scrollY > this.maxScrollY || this.maxScrollY <= 0 && this.scrollY > this.scrollHeight) {
            this.scrollY = 0;
        }
    }

    public void disabledSearch() {
        this.hasSearch = false;
    }

    private int textFieldHeight() {
        return this.hasSearch ? 22 : 0;
    }

    private void reset() {
        if (this.search.isEmpty()) {
            this.filteredList.clear();
        } else {
            String[] keys = this.search.toLowerCase().split(" ");
            this.filteredList = this.list.stream().filter(f -> {
                for (String k : keys) {
                    if (f.toLowerCase().contains(k)) continue;
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
        }
        this.setSize(this.width, this.height + this.textFieldHeight());
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height + this.textFieldHeight();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        if (this.hasSearch) {
            this.textField.setX(this.guiLeft);
            this.textField.setY(this.guiTop);
            this.textField.render(graphics, mouseX, mouseY, partialTicks);
        }
        this.guiTop += this.textFieldHeight();
        this.mouseInList = this.isMouseOver(mouseX, mouseY);
        graphics.fillGradient(this.guiLeft, this.guiTop, this.width + this.guiLeft, this.height + this.guiTop, -1072689136, -804253680);
        if (this.scrollHeight < this.height - 8) {
            this.drawScrollBar(graphics);
        }
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        try {
            poseStack.translate((float)this.guiLeft, (float)this.guiTop, 0.0f);
            if (this.selectable) {
                this.hover = this.getMouseOver(mouseX, mouseY);
            }
            this.drawItems(graphics);
        } finally {
            poseStack.popPose();
        }
        if (this.scrollHeight < this.height - 8) {
            mouseX -= this.guiLeft;
            mouseY -= this.guiTop;
            if (((MouseHelperMixin)this.minecraft.mouseHandler).getActiveButton() == 0) {
                if (mouseX >= this.width - 11 && mouseX < this.width - 6 && mouseY >= 4 && mouseY < this.height) {
                    this.isScrolling = true;
                }
            } else {
                this.isScrolling = false;
            }
            if (this.isScrolling) {
                this.scrollY = (mouseY - 8) * this.listHeight / (this.height - 8) - this.scrollHeight;
                if (this.scrollY < 0) {
                    this.scrollY = 0;
                }
                if (this.scrollY > this.maxScrollY) {
                    this.scrollY = this.maxScrollY;
                }
            }
        }
        this.guiTop -= this.textFieldHeight();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double mouseScrolledY) {
        if (mouseScrolledY != 0.0 && this.mouseInList) {
            this.scrollY += mouseScrolledY > 0.0 ? -14 : 14;
            if (this.scrollY > this.maxScrollY) {
                this.scrollY = this.maxScrollY;
            }
            if (this.scrollY < 0) {
                this.scrollY = 0;
            }
            return true;
        }
        return false;
    }

    public boolean mouseInOption(int i, int j, int k) {
        int l = 4;
        int i1 = 14 * k + 4 - this.scrollY;
        return i >= l - 1 && i < l + this.width - 11 && j >= i1 - 1 && j < i1 + 8;
    }

    protected void drawItems(GuiGraphics graphics) {
        List<String> l = this.getActiveList();
        for (int i = 0; i < l.size(); ++i) {
            int j = 4;
            int k = 14 * i + 4 - this.scrollY;
            if (k < 4 || k + 12 >= this.height) continue;
            int xOffset = this.scrollHeight < this.height - 8 ? 0 : 10;
            String displayString = I18n.get((String)l.get(i), (Object[])new Object[0]);
            Object text = "";
            float maxWidth = (float)(this.width + xOffset - 8) * 0.8f;
            if ((float)this.font.width(displayString) > maxWidth) {
                char c;
                for (int h = 0; h < displayString.length() && !((float)this.font.width((String)(text = (String)text + (c = displayString.charAt(h)))) > maxWidth); ++h) {
                }
                if (displayString.length() > ((String)text).length()) {
                    text = (String)text + "...";
                }
            } else {
                text = displayString;
            }
            if (this.multipleSelection && this.selectedList.contains(text) || !this.multipleSelection && this.selected.equals(l.get(i))) {
                graphics.vLine(j - 2, k - 4, k + 10, -1);
                graphics.vLine(j + this.width - 18 + xOffset, k - 4, k + 10, -1);
                graphics.hLine(j - 2, j + this.width - 18 + xOffset, k - 3, -1);
                graphics.hLine(j - 2, j + this.width - 18 + xOffset, k + 10, -1);
                graphics.drawString(this.font, (String)text, j, k, 0xFFFFFF);
                continue;
            }
            if (i == this.hover) {
                graphics.drawString(this.font, (String)text, j, k, 65280);
                continue;
            }
            graphics.drawString(this.font, (String)text, j, k, 0xFFFFFF);
        }
    }

    public String getSelected() {
        if (this.selected.isEmpty()) {
            return null;
        }
        return this.selected;
    }

    private List<String> getActiveList() {
        if (!this.search.isEmpty()) {
            return this.filteredList;
        }
        return this.list;
    }

    private int getMouseOver(int i, int j) {
        if ((i -= this.guiLeft) >= 4 && i < this.width - 4 && (j -= this.guiTop) >= 4 && j < this.height) {
            for (int j1 = 0; j1 < this.getActiveList().size(); ++j1) {
                if (!this.mouseInOption(i, j, j1)) continue;
                return j1;
            }
        }
        return -1;
    }

    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        if (this.hasSearch) {
            boolean bo = this.textField.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
            if (!this.search.equals(this.textField.getValue())) {
                this.search = this.textField.getValue().trim();
                this.reset();
            }
            return bo;
        }
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        if (this.hasSearch) {
            boolean bo = this.textField.charTyped(p_231042_1_, p_231042_2_);
            if (!this.search.equals(this.textField.getValue())) {
                this.search = this.textField.getValue().trim();
                this.reset();
            }
            return bo;
        }
        return super.charTyped(p_231042_1_, p_231042_2_);
    }

    public boolean mouseClicked(double i, double j, int k) {
        if (this.hasSearch) {
            this.textField.setFocused(this.textField.mouseClicked(i, j, k));
        }
        if (k != 0 || this.hover < 0) {
            return false;
        }
        List<String> list = this.getActiveList();
        if (this.multipleSelection) {
            if (this.selectedList.contains(list.get(this.hover))) {
                this.selectedList.remove(list.get(this.hover));
            } else {
                this.selectedList.add(list.get(this.hover));
            }
        } else {
            if (this.hover >= 0) {
                this.selected = list.get(this.hover);
            }
            this.hover = -1;
        }
        if (this.listener != null) {
            long time = System.currentTimeMillis();
            this.listener.scrollClicked(i, j, k, this);
            if (!this.selected.isEmpty() && this.selected == this.lastClickedItem && time - this.lastClickedTime < 500L) {
                this.listener.scrollDoubleClicked(this.selected, this);
            }
            this.lastClickedTime = time;
            this.lastClickedItem = this.selected;
        }
        return true;
    }

    private void drawScrollBar(GuiGraphics graphics) {
        int j;
        int i = this.guiLeft + this.width - 9;
        int k = j = this.guiTop + (int)((double)this.scrollY / (double)this.listHeight * (double)(this.height - 8)) + 4;
        graphics.blit(resource, i, k, this.width, 9, 5, 1);
        ++k;
        while (k < j + this.scrollHeight - 1) {
            graphics.blit(resource, i, k, this.width, 10, 5, 1);
            ++k;
        }
        graphics.blit(resource, i, k, this.width, 11, 5, 1);
    }

    public boolean hasSelected() {
        return !this.selected.isEmpty();
    }

    public void setList(List<String> list) {
        if (this.isSameList(list)) {
            return;
        }
        this.isSorted = true;
        this.scrollY = 0;
        Collections.sort(list, new NaturalOrderComparator());
        this.list = list;
        this.reset();
    }

    public void setUnsortedList(List<String> list) {
        if (this.isSameList(list)) {
            return;
        }
        this.isSorted = false;
        this.scrollY = 0;
        this.list = list;
        this.reset();
    }

    private boolean isSameList(List<String> list) {
        if (this.list.size() != list.size()) {
            return false;
        }
        for (String s : this.list) {
            if (list.contains(s)) continue;
            return false;
        }
        return true;
    }

    public void replace(String old, String name) {
        String select = this.getSelected();
        this.list.remove(old);
        this.list.add(name);
        if (this.isSorted) {
            Collections.sort(this.list, new NaturalOrderComparator());
        }
        if (old.equals(select)) {
            select = name;
        }
        this.selected = select;
        this.reset();
    }

    public void setSelected(String name) {
        this.selected = name;
    }

    public void clear() {
        this.list = new ArrayList<String>();
        this.selected = "";
        this.scrollY = 0;
        this.search = "";
        this.textField.setValue("");
        this.reset();
    }

    public void clearSelection() {
        this.list = new ArrayList<String>();
        this.selected = "";
    }

    public List<String> getList() {
        return this.list;
    }

    public HashSet<String> getSelectedList() {
        return this.selectedList;
    }

    public void setSelectedList(HashSet<String> selectedList) {
        this.selectedList = selectedList;
    }

    public GuiCustomScroll setUnselectable() {
        this.selectable = false;
        return this;
    }

    public void scrollTo(String name) {
        int i = this.list.indexOf(name);
        if (i < 0 || this.scrollHeight >= this.height - 8) {
            return;
        }
        int pos = (int)(1.0f * (float)i / (float)this.list.size() * (float)this.listHeight);
        if (pos > this.maxScrollY) {
            pos = this.maxScrollY;
        }
        this.scrollY = pos;
    }

    public boolean isMouseOver(int x, int y) {
        return x >= this.guiLeft && x <= this.guiLeft + this.width && y >= this.guiTop && y <= this.guiTop + this.height;
    }

    public int getSelectedIndex() {
        return this.list.indexOf(this.selected);
    }

    public void setSelectedIndex(int i) {
        this.selected = i < 0 ? "" : this.list.get(i);
    }
}

