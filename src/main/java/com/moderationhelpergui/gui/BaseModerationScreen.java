package com.moderationhelpergui.gui;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.rules.PunishmentType;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseModerationScreen extends Screen {
    private final List<RecentArea> recentAreas = new ArrayList<>();

    protected BaseModerationScreen(Text title) {
        super(title);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    protected void drawBackgroundPanel(DrawContext context) {
        context.fill(0, 0, width, height, 0xAA05070B);
        int panelX = Math.max(12, width / 2 - 250);
        int panelY = 24;
        int panelW = Math.min(500, width - 170);
        int panelH = height - 48;
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC111827);
        context.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFF7DD3FC);
    }

    protected void drawStatsPanel(DrawContext context) {
        int x = width - 145;
        int y = 24;
        int w = 130;
        int h = 116;
        context.fill(x, y, x + w, y + h, 0xCC0B1220);
        context.fill(x, y, x + w, y + 1, 0xFF22C55E);
        context.drawTextWithShadow(textRenderer, "Статистика", x + 10, y + 10, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "warn:  " + ModerationHelperClient.STATS.get(PunishmentType.WARN), x + 10, y + 30, 0xFFE7E7);
        context.drawTextWithShadow(textRenderer, "mute:  " + ModerationHelperClient.STATS.get(PunishmentType.MUTE), x + 10, y + 48, 0xFFE7E7);
        context.drawTextWithShadow(textRenderer, "ban:   " + ModerationHelperClient.STATS.get(PunishmentType.BAN), x + 10, y + 66, 0xFFE7E7);
        context.drawTextWithShadow(textRenderer, "ipban: " + ModerationHelperClient.STATS.get(PunishmentType.IPBAN), x + 10, y + 84, 0xFFE7E7);
    }

    protected void drawRecentPlayers(DrawContext context, int mouseX, int mouseY) {
        recentAreas.clear();
        int x = 16;
        int y = height - 178;
        int w = 170;
        int h = 155;
        context.fill(x, y, x + w, y + h, 0xCC0B1220);
        context.fill(x, y, x + w, y + 1, 0xFFF59E0B);
        context.drawTextWithShadow(textRenderer, "Недавние игроки", x + 10, y + 10, 0xFFFFFF);

        int rowY = y + 30;
        for (String nick : ModerationHelperClient.RECENT_PLAYERS.list()) {
            if (rowY + 14 > y + h - 6) break;
            boolean hover = mouseX >= x + 8 && mouseX <= x + w - 8 && mouseY >= rowY - 2 && mouseY <= rowY + 12;
            if (hover) {
                context.fill(x + 7, rowY - 3, x + w - 7, rowY + 13, 0x5538BDF8);
            }
            context.drawTextWithShadow(textRenderer, nick, x + 12, rowY, 0xDDFEFF);
            recentAreas.add(new RecentArea(nick, x + 8, rowY - 2, x + w - 8, rowY + 12));
            rowY += 16;
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        for (RecentArea area : recentAreas) {
            if (area.contains(mouseX, mouseY)) {
                if (client != null) {
                    client.keyboard.setClipboard(area.nick);
                    ModerationHelperClient.openPunishmentWithoutScreenshot(area.nick);
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    protected int panelX() {
        return Math.max(12, width / 2 - 250);
    }

    protected int panelY() {
        return 24;
    }

    protected int panelW() {
        return Math.min(500, width - 170);
    }

    protected String cut(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private record RecentArea(String nick, int x1, int y1, int x2, int y2) {
        boolean contains(double x, double y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }
    }
}
