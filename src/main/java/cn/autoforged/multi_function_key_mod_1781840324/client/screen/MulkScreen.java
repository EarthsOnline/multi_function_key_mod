package cn.autoforged.multi_function_key_mod_1781840324.client.screen;

import cn.autoforged.multi_function_key_mod_1781840324.client.ModClientEvents;
import cn.autoforged.multi_function_key_mod_1781840324.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MulkScreen extends Screen {
    private static final int CARD_HEIGHT = 36;
    private static final int CARD_GAP = 4;
    private static final int SIDEBAR_WIDTH = 6;
    private static final int CONTENT_LEFT = 16;
    private static final int CONTENT_RIGHT = 16;
    private static final int BOTTOM_BAR_HEIGHT = 50;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private final List<BoundActionEntry> entries = new ArrayList<>();
    private boolean isWaitingForKey = false;
    private boolean showAddList = false;
    private int addScrollOffset = 0;

    private int listLeft;
    private int listRight;
    private int listTop;
    private int listBottom;
    private int listWidth;
    private int scrollBarTop;
    private int scrollBarHeight;
    private int scrollBarX;
    private boolean draggingScrollBar = false;

    public MulkScreen() {
        super(Component.translatable("mulk.screen.title"));
    }

    @Override
    protected void init() {
        super.init();
        entries.clear();
        loadBoundActions();

        listLeft = CONTENT_LEFT;
        listRight = width - CONTENT_RIGHT - SIDEBAR_WIDTH - 8;
        listWidth = listRight - listLeft;
        listTop = 40;
        listBottom = height - BOTTOM_BAR_HEIGHT - 10;
        scrollBarX = width - CONTENT_RIGHT - SIDEBAR_WIDTH;
        scrollBarTop = listTop;

        recalcScroll();

        addRenderableWidget(Button.builder(
                Component.translatable("mulk.button.cancel"),
                btn -> onClose()
        ).bounds(width / 2 - 150, height - BOTTOM_BAR_HEIGHT + 5, 90, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("mulk.button.add_action"),
                btn -> showAddList = !showAddList
        ).bounds(width / 2 - 50, height - BOTTOM_BAR_HEIGHT + 5, 100, 20).build());

        addRenderableWidget(Button.builder(
                isWaitingForKey ? Component.translatable("mulk.button.waiting") : Component.translatable("mulk.button.change_key"),
                btn -> startKeyCapture()
        ).bounds(width / 2 + 60, height - BOTTOM_BAR_HEIGHT + 5, 90, 20).build());
    }

    private void loadBoundActions() {
        List<? extends String> names = ModConfig.CLIENT.boundActions.get();
        Minecraft mc = Minecraft.getInstance();
        for (String name : names) {
            KeyMapping mapping = findKeyMapping(mc, name);
            if (mapping != null) {
                entries.add(new BoundActionEntry(mapping));
            }
        }
    }

    private void recalcScroll() {
        int totalContentHeight = entries.size() * (CARD_HEIGHT + CARD_GAP) - CARD_GAP;
        int viewportHeight = listBottom - listTop;
        maxScroll = Math.max(0, totalContentHeight - viewportHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        float visibleRatio = (float) viewportHeight / Math.max(totalContentHeight, viewportHeight);
        scrollBarHeight = Math.max(20, (int) (visibleRatio * viewportHeight));
    }

    private KeyMapping findKeyMapping(Minecraft mc, String name) {
        for (KeyMapping km : mc.options.keyMappings) {
            if (km.getName().equals(name)) {
                return km;
            }
        }
        return null;
    }

    private void startKeyCapture() {
        isWaitingForKey = true;
        showAddList = false;
        rebuildWidgets();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isWaitingForKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isWaitingForKey = false;
                rebuildWidgets();
                return true;
            }
            InputConstants.Key newKey = InputConstants.getKey(keyCode, scanCode);
            ModClientEvents.MULK_KEY.setKey(newKey);
            isWaitingForKey = false;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showAddList) {
            return handleAddListClick(mouseX, mouseY, button);
        }

        if (button == 0) {
            if (isScrollBarHit(mouseX, mouseY)) {
                draggingScrollBar = true;
                return true;
            }

            int index = hitTestEntry(mouseX, mouseY);
            if (index >= 0) {
                BoundActionEntry entry = entries.get(index);
                double cardLocalY = mouseY - listTop + scrollOffset - index * (CARD_HEIGHT + CARD_GAP);
                double buttonX = mouseX - listLeft;
                double executeX = listWidth - 120;
                double unbindX = listWidth - 58;

                if (cardLocalY >= 4 && cardLocalY <= CARD_HEIGHT - 4) {
                    if (buttonX >= executeX + 4 && buttonX <= executeX + 54) {
                        executeAction(entry);
                        return true;
                    }
                    if (buttonX >= unbindX && buttonX <= unbindX + 50) {
                        unbindAction(entry);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleAddListClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Minecraft mc = Minecraft.getInstance();
            int y = 80;
            int idx = 0;
            int skipped = (int) (addScrollOffset / 14);
            for (KeyMapping km : mc.options.keyMappings) {
                if (idx < skipped) { idx++; continue; }
                int ey = y + idx * 14 - addScrollOffset;
                if (ey > listBottom) break;
                if (ey >= 80 && mouseY >= ey && mouseY <= ey + 14 && mouseX >= listLeft && mouseX <= listRight) {
                    if (!isAlreadyBound(km)) {
                        addBoundAction(km.getName());
                    }
                    showAddList = false;
                    rebuildWidgets();
                    return true;
                }
                idx++;
            }
        }
        return false;
    }

    private boolean isScrollBarHit(double mouseX, double mouseY) {
        int totalContent = entries.size() * (CARD_HEIGHT + CARD_GAP) - CARD_GAP;
        if (totalContent <= listBottom - listTop) return false;
        int trackHeight = listBottom - listTop - scrollBarHeight;
        float ratio = trackHeight > 0 ? (float) scrollOffset / Math.max(maxScroll, 1) : 0;
        int barY = scrollBarTop + (int) (ratio * trackHeight);
        return mouseX >= scrollBarX && mouseX <= scrollBarX + SIDEBAR_WIDTH
                && mouseY >= barY && mouseY <= barY + scrollBarHeight;
    }

    private int hitTestEntry(double mouseX, double mouseY) {
        if (mouseX < listLeft || mouseX > listRight) return -1;
        double listMouseY = mouseY - listTop + scrollOffset;
        int index = (int) (listMouseY / (CARD_HEIGHT + CARD_GAP));
        if (index < 0 || index >= entries.size()) return -1;
        double cardY = listTop + index * (CARD_HEIGHT + CARD_GAP) - scrollOffset;
        if (mouseY < cardY || mouseY > cardY + CARD_HEIGHT) return -1;
        return index;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollBar && button == 0) {
            float scrollAreaHeight = listBottom - listTop - scrollBarHeight;
            float ratio = (float) ((mouseY - scrollBarTop) / scrollAreaHeight);
            scrollOffset = Math.max(0, Math.min(maxScroll, (int) (ratio * maxScroll)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingScrollBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showAddList) {
            Minecraft mc = Minecraft.getInstance();
            int viewHeight = listBottom - 80;
            int totalHeight = mc.options.keyMappings.length * 14;
            int maxAdd = Math.max(0, totalHeight - viewHeight);
            addScrollOffset = Math.max(0, Math.min(maxAdd, addScrollOffset - (int) (scrollY * 14)));
        } else {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * 20)));
        }
        return true;
    }

    private void executeAction(BoundActionEntry entry) {
        KeyMapping.click(entry.mapping.getKey());
        onClose();
    }

    private void unbindAction(BoundActionEntry entry) {
        List<String> current = new ArrayList<>(ModConfig.CLIENT.boundActions.get());
        current.remove(entry.mapping.getName());
        setBoundActions(current);
        entries.remove(entry);
        recalcScroll();
        rebuildWidgets();
    }

    private void addBoundAction(String name) {
        List<String> current = new ArrayList<>(ModConfig.CLIENT.boundActions.get());
        if (!current.contains(name)) {
            current.add(name);
            setBoundActions(current);
        }
        loadBoundActions();
        recalcScroll();
    }

    private boolean isAlreadyBound(KeyMapping km) {
        for (BoundActionEntry e : entries) {
            if (e.mapping.getName().equals(km.getName())) return true;
        }
        return false;
    }

    private void setBoundActions(List<String> list) {
        ModConfigSpec.ConfigValue<List<? extends String>> cv = ModConfig.CLIENT.boundActions;
        cv.set(list);
        cv.save();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, width, height, 0xC0101010);

        Component title = Component.translatable("mulk.screen.title");
        gui.drawString(font, title, (width - font.width(title)) / 2, 12, 0xFFFFFF, true);

        if (isWaitingForKey) {
            Component msg = Component.translatable("mulk.prompt.press_key");
            gui.drawString(font, msg, (width - font.width(msg)) / 2, height / 2 - 20, 0xFFFFAA, true);
            super.render(gui, mouseX, mouseY, partialTick);
            return;
        }

        if (showAddList) {
            renderAddActionList(gui, mouseX, mouseY);
            super.render(gui, mouseX, mouseY, partialTick);
            return;
        }

        gui.fill(listLeft - 4, listTop - 4, scrollBarX + SIDEBAR_WIDTH + 4, listBottom + 4, 0x60000000);
        gui.fill(listLeft, listTop, listRight, listBottom, 0x80000000);

        renderList(gui, mouseX, mouseY);
        renderScrollBar(gui);

        super.render(gui, mouseX, mouseY, partialTick);
    }

    private void renderAddActionList(GuiGraphics gui, int mouseX, int mouseY) {
        gui.fill(listLeft - 4, 56, scrollBarX + SIDEBAR_WIDTH + 4, listBottom + 4, 0x60000000);
        gui.fill(listLeft, 60, listRight, listBottom, 0x90000000);

        Component header = Component.translatable("mulk.add_action.title");
        gui.drawString(font, header, (width - font.width(header)) / 2, 64, 0xFFFFAA, true);

        Minecraft mc = Minecraft.getInstance();
        int y = 80;
        int idx = 0;
        int skipped = (int) (addScrollOffset / 14);
        for (KeyMapping km : mc.options.keyMappings) {
            if (idx < skipped) { idx++; continue; }
            int ey = y + idx * 14 - addScrollOffset;
            if (ey > listBottom) break;
            if (ey < 60) { idx++; continue; }

            boolean hover = mouseX >= listLeft && mouseX <= listRight && mouseY >= ey && mouseY <= ey + 14;
            boolean bound = isAlreadyBound(km);
            int bgColor = bound ? 0x402A2A2A : (hover ? 0x603A3A5A : 0x00000000);
            if (bgColor != 0) gui.fill(listLeft, ey, listRight, ey + 14, bgColor);

            Component displayName = Component.translatable(km.getName());
            int color = bound ? 0x666666 : (hover ? 0xFFFFFF : 0xCCCCCC);
            gui.drawString(font, displayName, listLeft + 4, ey + 2, color, !bound);

            if (bound) {
                gui.drawString(font, Component.translatable("mulk.add_action.bound"), listRight - 40, ey + 2, 0x888888, false);
            }

            idx++;
        }
    }

    private void renderList(GuiGraphics gui, int mouseX, int mouseY) {
        int viewStart = listTop;
        int viewEnd = listBottom;

        for (int i = 0; i < entries.size(); i++) {
            int y = listTop + i * (CARD_HEIGHT + CARD_GAP) - scrollOffset;
            if (y + CARD_HEIGHT < viewStart || y > viewEnd) continue;

            boolean hover = mouseX >= listLeft && mouseX <= listRight && mouseY >= y && mouseY <= y + CARD_HEIGHT;
            int bgColor = hover ? 0xFF2A2A3A : 0xFF1A1A2A;
            gui.fill(listLeft, y, listRight, y + CARD_HEIGHT, bgColor);
            gui.renderOutline(listLeft, y, listWidth, CARD_HEIGHT, 0xFF444466);

            BoundActionEntry entry = entries.get(i);
            Component name = Component.translatable(entry.mapping.getName());
            gui.drawString(font, name, listLeft + 6, y + 8, 0xCCCCFF, true);

            Component keyComponent = entry.mapping.getKey().getDisplayName();
            gui.drawString(font, keyComponent, listLeft + 6, y + 20, 0x888888, false);

            gui.fill(listLeft + listWidth - 120, y + 5, listLeft + listWidth - 66, y + CARD_HEIGHT - 5, 0xFF2A5A2A);
            gui.drawString(font, Component.translatable("mulk.action.execute"), listLeft + listWidth - 114, y + 11, 0x88FF88, true);

            gui.fill(listLeft + listWidth - 58, y + 5, listLeft + listWidth - 8, y + CARD_HEIGHT - 5, 0xFF5A2A2A);
            gui.drawString(font, Component.translatable("mulk.action.unbind"), listLeft + listWidth - 52, y + 11, 0xFF8888, true);
        }
    }

    private void renderScrollBar(GuiGraphics gui) {
        int totalContentHeight = entries.size() * (CARD_HEIGHT + CARD_GAP) - CARD_GAP;
        int viewportHeight = listBottom - listTop;
        if (totalContentHeight <= viewportHeight) return;

        float ratio = (float) scrollOffset / Math.max(maxScroll, 1);
        int trackHeight = viewportHeight - scrollBarHeight;
        int barY = scrollBarTop + (int) (ratio * trackHeight);

        gui.fill(scrollBarX, scrollBarTop, scrollBarX + SIDEBAR_WIDTH, listBottom, 0x40000000);
        gui.fill(scrollBarX, barY, scrollBarX + SIDEBAR_WIDTH, barY + scrollBarHeight, 0xCCAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class BoundActionEntry {
        final KeyMapping mapping;

        BoundActionEntry(KeyMapping mapping) {
            this.mapping = mapping;
        }
    }
}
