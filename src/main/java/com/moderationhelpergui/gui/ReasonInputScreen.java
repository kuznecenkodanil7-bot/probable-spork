package com.moderationhelpergui.gui;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.rules.PunishmentRule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;

public final class ReasonInputScreen extends BaseModerationScreen {
    private final String nick;
    private final PunishmentRule rule;
    private final String duration;
    private final Path tempScreenshot;
    private TextFieldWidget reasonField;

    public ReasonInputScreen(String nick, PunishmentRule rule, String duration, Path tempScreenshot) {
        super(Text.literal("Подтверждение причины"));
        this.nick = nick;
        this.rule = rule;
        this.duration = duration;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int x = panelX() + 50;
        int y = panelY() + 110;
        int w = Math.min(380, panelW() - 100);

        reasonField = new TextFieldWidget(textRenderer, x, y, w, 24, Text.literal("Причина"));
        reasonField.setMaxLength(96);
        reasonField.setText(rule.id());
        addDrawableChild(reasonField);
        setInitialFocus(reasonField);

        addQuickReasonButtons(x, y + 34, w);

        addDrawableChild(ButtonWidget.builder(Text.literal("Выдать наказание"), button -> {
            String reason = reasonField.getText().trim();
            if (reason.isBlank()) {
                ModerationHelperClient.notifyClient("Причина не должна быть пустой.");
                return;
            }
            ModerationHelperClient.issuePunishment(nick, rule.type(), duration, reason, tempScreenshot);
        }).dimensions(x, height - 84, w, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button ->
                client.setScreen(new DurationScreen(nick, rule, tempScreenshot))
        ).dimensions(x, height - 54, w, 24).build());
    }

    private void addQuickReasonButtons(int x, int y, int width) {
        String key = rule.type().commandName;
        List<String> quick = ModerationHelperClient.CONFIG.quickReasons.getOrDefault(key, List.of());
        int buttonW = 86;
        int max = Math.min(quick.size(), 12);
        for (int i = 0; i < max; i++) {
            String reason = quick.get(i);
            int bx = x + (i % 4) * (buttonW + 8);
            int by = y + (i / 4) * 24;
            addDrawableChild(ButtonWidget.builder(Text.literal(cut(reason, 12)), button -> reasonField.setText(reason))
                    .dimensions(bx, by, buttonW, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackgroundPanel(context);
        drawStatsPanel(context);
        drawRecentPlayers(context, mouseX, mouseY);

        int x = panelX();
        int y = panelY();
        context.drawCenteredTextWithShadow(textRenderer, "Подтверждение причины", x + panelW() / 2, y + 14, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Игрок: " + nick + " | " + rule.type().commandName.toUpperCase() + " | " + duration, x + panelW() / 2, y + 38, 0x7DD3FC);
        context.drawTextWithShadow(textRenderer, cut(rule.displayName(), 72), x + 50, y + 74, 0xE5E7EB);
        context.drawTextWithShadow(textRenderer, "Можно оставить пункт правила или вручную изменить причину", x + 50, y + 92, 0xA7F3D0);

        super.render(context, mouseX, mouseY, delta);
    }
}
