package com.moderationhelpergui.gui;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.rules.PunishmentRule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class DurationScreen extends BaseModerationScreen {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)(permanent|perm|forever|навсегда|[1-9][0-9]*[dh])");

    private final String nick;
    private final PunishmentRule rule;
    private final Path tempScreenshot;
    private TextFieldWidget durationField;

    public DurationScreen(String nick, PunishmentRule rule, Path tempScreenshot) {
        super(Text.literal("Выбор времени"));
        this.nick = nick;
        this.rule = rule;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int x = panelX() + 60;
        int y = panelY() + 92;
        int w = Math.min(360, panelW() - 120);

        durationField = new TextFieldWidget(textRenderer, x, y, w, 24, Text.literal("Время"));
        durationField.setMaxLength(32);
        durationField.setText(rule.defaultDuration() == null ? "" : rule.defaultDuration());
        addDrawableChild(durationField);
        setInitialFocus(durationField);

        int sx = x;
        int sy = y + 34;
        int buttonW = 82;
        int index = 0;
        for (String suggestion : rule.suggestedDurations()) {
            int bx = sx + (index % 4) * (buttonW + 8);
            int by = sy + (index / 4) * 25;
            addDrawableChild(ButtonWidget.builder(Text.literal(suggestion), button -> durationField.setText(suggestion))
                    .dimensions(bx, by, buttonW, 20).build());
            index++;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Дальше"), button -> continueToReason())
                .dimensions(x, height - 84, w, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button ->
                client.setScreen(new ReasonScreen(nick, rule.type(), tempScreenshot))
        ).dimensions(x, height - 54, w, 24).build());
    }

    private void continueToReason() {
        String duration = normalize(durationField.getText());
        if (!isValid(duration)) {
            ModerationHelperClient.notifyClient("Время должно быть формата 7d, 12h или permanent.");
            return;
        }
        client.setScreen(new ReasonInputScreen(nick, rule, duration, tempScreenshot));
    }

    private boolean isValid(String duration) {
        return duration != null && DURATION_PATTERN.matcher(duration).matches();
    }

    private String normalize(String value) {
        String text = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (text.equals("perm") || text.equals("forever") || text.equals("навсегда")) return "permanent";
        return text;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackgroundPanel(context);
        drawStatsPanel(context);
        drawRecentPlayers(context, mouseX, mouseY);

        int x = panelX();
        int y = panelY();
        context.drawCenteredTextWithShadow(textRenderer, "Выбор времени", x + panelW() / 2, y + 14, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Игрок: " + nick + " | Причина: " + rule.id(), x + panelW() / 2, y + 38, 0x7DD3FC);
        context.drawTextWithShadow(textRenderer, "Формат: 7d — дни, 12h — часы, permanent — навсегда", x + 60, y + 68, 0xE5E7EB);

        super.render(context, mouseX, mouseY, delta);
    }
}
