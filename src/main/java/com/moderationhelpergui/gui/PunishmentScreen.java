package com.moderationhelpergui.gui;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.rules.PunishmentType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public final class PunishmentScreen extends BaseModerationScreen {
    private final String nick;
    private final Path tempScreenshot;

    public PunishmentScreen(String nick, Path tempScreenshot) {
        super(Text.literal("Moderation Helper GUI"));
        this.nick = nick;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int x = panelX() + 40;
        int y = panelY() + 66;
        int w = Math.min(360, panelW() - 80);
        int h = 24;
        int gap = 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Warn — сразу 2.1"), button ->
                ModerationHelperClient.warnImmediately(nick, tempScreenshot)
        ).dimensions(x, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Mute"), button ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.MUTE, tempScreenshot))
        ).dimensions(x, y + gap, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Ban"), button ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.BAN, tempScreenshot))
        ).dimensions(x, y + gap * 2, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("IPBan"), button ->
                client.setScreen(new ReasonScreen(nick, PunishmentType.IPBAN, tempScreenshot))
        ).dimensions(x, y + gap * 3, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Вызвать на проверку"), button ->
                ModerationHelperClient.startCheck(nick)
        ).dimensions(x, y + gap * 4 + 10, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Снять с проверки"), button ->
                ModerationHelperClient.stopCheck(nick)
        ).dimensions(x, y + gap * 5 + 10, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button ->
                client.setScreen(null)
        ).dimensions(x, y + gap * 6 + 24, w, h).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackgroundPanel(context);
        drawStatsPanel(context);
        drawRecentPlayers(context, mouseX, mouseY);

        int x = panelX();
        int y = panelY();
        context.drawCenteredTextWithShadow(textRenderer, "Moderation Helper GUI", x + panelW() / 2, y + 14, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Кого наказываем: " + nick, x + panelW() / 2, y + 38, 0x7DD3FC);
        context.drawTextWithShadow(textRenderer, "Скрин: " + (tempScreenshot == null ? "не создавался" : "temp сохранён"), x + 18, height - 46, 0xA7F3D0);

        super.render(context, mouseX, mouseY, delta);
    }
}
