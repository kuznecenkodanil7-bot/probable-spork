package com.moderationhelpergui.gui;

import com.moderationhelpergui.rules.PunishmentRule;
import com.moderationhelpergui.rules.PunishmentRules;
import com.moderationhelpergui.rules.PunishmentType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;

public final class ReasonScreen extends BaseModerationScreen {
    private final String nick;
    private final PunishmentType type;
    private final Path tempScreenshot;

    public ReasonScreen(String nick, PunishmentType type, Path tempScreenshot) {
        super(Text.literal("Выбор причины"));
        this.nick = nick;
        this.type = type;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        List<PunishmentRule> rules = PunishmentRules.rulesFor(type);
        int x = panelX() + 18;
        int y = panelY() + 66;
        int colW = (panelW() - 52) / 2;
        int h = 22;
        int gap = 25;

        for (int i = 0; i < rules.size(); i++) {
            PunishmentRule rule = rules.get(i);
            int col = i % 2;
            int row = i / 2;
            int bx = x + col * (colW + 16);
            int by = y + row * gap;

            addDrawableChild(ButtonWidget.builder(Text.literal(cut(rule.displayName(), 34)), button ->
                    client.setScreen(new DurationScreen(nick, rule, tempScreenshot))
            ).dimensions(bx, by, colW, h).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button ->
                client.setScreen(new PunishmentScreen(nick, tempScreenshot))
        ).dimensions(panelX() + 18, height - 52, 120, 24).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackgroundPanel(context);
        drawStatsPanel(context);
        drawRecentPlayers(context, mouseX, mouseY);

        int x = panelX();
        int y = panelY();
        context.drawCenteredTextWithShadow(textRenderer, "Выбор причины: " + type.commandName.toUpperCase(), x + panelW() / 2, y + 14, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Игрок: " + nick, x + panelW() / 2, y + 38, 0x7DD3FC);

        super.render(context, mouseX, mouseY, delta);
    }
}
