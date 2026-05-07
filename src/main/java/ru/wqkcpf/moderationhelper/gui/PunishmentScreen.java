package ru.wqkcpf.moderationhelper.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;
import ru.wqkcpf.moderationhelper.punishment.PunishmentExecutor;
import ru.wqkcpf.moderationhelper.punishment.PunishmentRules;
import ru.wqkcpf.moderationhelper.punishment.PunishmentType;

import java.nio.file.Path;

public class PunishmentScreen extends Screen {
    private final String nick;
    private final Path tempScreenshot;

    public PunishmentScreen(String nick, Path tempScreenshot) {
        super(Text.literal("Moderation Helper GUI"));
        this.nick = nick;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int panelW = Math.min(760, width - 32);
        int panelH = Math.min(440, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        int leftX = x + 22;
        int buttonW = 210;
        int buttonH = 25;
        int startY = y + 78;
        int gap = 30;

        addDrawableChild(GuiUtil.button(leftX, startY, buttonW, buttonH, "Warn — сразу 2.1", b ->
                PunishmentExecutor.execute(nick, PunishmentType.WARN, "", PunishmentRules.WARN_2_1.reason(), tempScreenshot)));

        addDrawableChild(GuiUtil.button(leftX, startY + gap, buttonW, buttonH, "Mute", b ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.MUTE, tempScreenshot))));

        addDrawableChild(GuiUtil.button(leftX, startY + gap * 2, buttonW, buttonH, "Ban", b ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.BAN, tempScreenshot))));

        addDrawableChild(GuiUtil.button(leftX, startY + gap * 3, buttonW, buttonH, "IPBan", b ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.IPBAN, tempScreenshot))));

        addDrawableChild(GuiUtil.button(leftX, startY + gap * 4 + 8, buttonW, buttonH, "Вызвать на проверку", b ->
                ModerationHelperClient.startCheck(nick)));

        addDrawableChild(GuiUtil.button(leftX, startY + gap * 5 + 8, buttonW, buttonH, "Снять с проверки", b ->
                ModerationHelperClient.stopCheckRecording("кнопка меню")));

        addRecentButtons(x + 22, y + panelH - 128, 190, 20);
    }

    private void addRecentButtons(int x, int y, int w, int h) {
        int i = 0;
        for (String recentNick : ModerationHelperClient.RECENT_PLAYERS.list()) {
            if (i >= 5) break;
            int buttonY = y + 18 + i * 22;
            addDrawableChild(GuiUtil.button(x, buttonY, w, h, recentNick, b -> {
                MinecraftClient.getInstance().keyboard.setClipboard(recentNick);
                ModerationHelperClient.openPunishmentMenuNoScreenshot(recentNick);
            }));
            i++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        int panelW = Math.min(760, width - 32);
        int panelH = Math.min(440, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        GuiUtil.drawPanel(context, x, y, panelW, panelH);
        context.drawTextWithShadow(textRenderer, Text.literal("Moderation Helper GUI"), x + 22, y + 20, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Ник: " + nick), x + 22, y + 42, GuiUtil.ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("Выбери действие для игрока"), x + 22, y + 58, GuiUtil.GRAY);

        int rightX = x + panelW - 230;
        context.fill(rightX - 10, y + 54, x + panelW - 22, y + 178, GuiUtil.PANEL_LIGHT);
        context.drawTextWithShadow(textRenderer, Text.literal("Статистика сессии"), rightX, y + 66, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("warn: " + ModerationHelperClient.STATS.warn()), rightX, y + 88, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("mute: " + ModerationHelperClient.STATS.mute()), rightX, y + 106, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("ban: " + ModerationHelperClient.STATS.ban()), rightX, y + 124, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("ipban: " + ModerationHelperClient.STATS.ipban()), rightX, y + 142, GuiUtil.GRAY);

        context.fill(rightX - 10, y + 196, x + panelW - 22, y + 324, GuiUtil.PANEL_LIGHT);
        context.drawTextWithShadow(textRenderer, Text.literal("Подсказки"), rightX, y + 208, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("H — статистика и недавние"), rightX, y + 230, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("G — остановить OBS"), rightX, y + 248, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("СКМ по чату — открыть меню"), rightX, y + 266, GuiUtil.GRAY);

        context.drawTextWithShadow(textRenderer, Text.literal("Недавние игроки"), x + 22, y + panelH - 130, GuiUtil.WHITE);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
