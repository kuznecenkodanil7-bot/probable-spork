package ru.wqkcpf.moderationhelper.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;

public class StatsScreen extends Screen {
    public StatsScreen() {
        super(Text.literal("Moderation Helper Stats"));
    }

    @Override
    protected void init() {
        int panelW = Math.min(620, width - 32);
        int panelH = Math.min(420, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        int rx = x + 28;
        int ry = y + 154;
        int i = 0;
        for (String nick : ModerationHelperClient.RECENT_PLAYERS.list()) {
            if (i >= 12) break;
            int bx = rx + (i % 2) * 190;
            int by = ry + (i / 2) * 24;
            addDrawableChild(GuiUtil.button(bx, by, 174, 20, nick, b -> {
                MinecraftClient.getInstance().keyboard.setClipboard(nick);
                ModerationHelperClient.openPunishmentMenuNoScreenshot(nick);
            }));
            i++;
        }

        addDrawableChild(GuiUtil.button(x + panelW - 128, y + panelH - 38, 100, 22, "Закрыть", b -> close()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        int panelW = Math.min(620, width - 32);
        int panelH = Math.min(420, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        GuiUtil.drawPanel(context, x, y, panelW, panelH);
        context.drawTextWithShadow(textRenderer, Text.literal("Moderation Helper — панель"), x + 28, y + 22, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("H открывает только эту панель: без ника, без скриншота, без поиска последнего сообщения."), x + 28, y + 44, GuiUtil.GRAY);

        context.fill(x + 28, y + 74, x + panelW - 28, y + 132, GuiUtil.PANEL_LIGHT);
        context.drawTextWithShadow(textRenderer, Text.literal("warn: " + ModerationHelperClient.STATS.warn()), x + 44, y + 88, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("mute: " + ModerationHelperClient.STATS.mute()), x + 164, y + 88, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("ban: " + ModerationHelperClient.STATS.ban()), x + 284, y + 88, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("ipban: " + ModerationHelperClient.STATS.ipban()), x + 404, y + 88, GuiUtil.GRAY);

        context.drawTextWithShadow(textRenderer, Text.literal("Недавние игроки"), x + 28, y + 138, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Клик по нику копирует его и открывает меню наказаний."), x + 28, y + panelH - 60, GuiUtil.GRAY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
