package ru.wqkcpf.moderationhelper.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class GuiUtil {
    private GuiUtil() {}

    public static final int PANEL = 0xCC101218;
    public static final int PANEL_LIGHT = 0xCC1A1D26;
    public static final int ACCENT = 0xFF6FA8FF;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFFB8C0CC;

    public static ButtonWidget button(int x, int y, int w, int h, String label, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(Text.literal(label), action).dimensions(x, y, w, h).build();
    }

    public static void drawPanel(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, PANEL);
        context.fill(x, y, x + w, y + 1, 0x55FFFFFF);
        context.fill(x, y + h - 1, x + w, y + h, 0x55000000);
    }

    public static void drawTitle(Screen screen, DrawContext context, String title, int x, int y) {
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(title), x, y, WHITE);
    }
}
