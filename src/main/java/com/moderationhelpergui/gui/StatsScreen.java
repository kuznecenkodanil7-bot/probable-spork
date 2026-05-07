package com.moderationhelpergui.gui;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.rules.PunishmentType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class StatsScreen extends BaseModerationScreen {
    public StatsScreen() {
        super(Text.literal("Moderation Helper GUI — статистика"));
    }

    @Override
    protected void init() {
        int x = panelX() + 60;
        int w = Math.min(360, panelW() - 120);
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> client.setScreen(null))
                .dimensions(x, height - 58, w, 24).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackgroundPanel(context);
        drawStatsPanel(context);
        drawRecentPlayers(context, mouseX, mouseY);

        int x = panelX();
        int y = panelY();
        context.drawCenteredTextWithShadow(textRenderer, "Панель мода", x + panelW() / 2, y + 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "H открывает только эту панель — без скрина и без поиска ника", x + panelW() / 2, y + 40, 0x7DD3FC);

        int sx = x + 70;
        int sy = y + 86;
        context.drawTextWithShadow(textRenderer, "Сессия", sx, sy, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "warn:  " + ModerationHelperClient.STATS.get(PunishmentType.WARN), sx, sy + 24, 0xE5E7EB);
        context.drawTextWithShadow(textRenderer, "mute:  " + ModerationHelperClient.STATS.get(PunishmentType.MUTE), sx, sy + 44, 0xE5E7EB);
        context.drawTextWithShadow(textRenderer, "ban:   " + ModerationHelperClient.STATS.get(PunishmentType.BAN), sx, sy + 64, 0xE5E7EB);
        context.drawTextWithShadow(textRenderer, "ipban: " + ModerationHelperClient.STATS.get(PunishmentType.IPBAN), sx, sy + 84, 0xE5E7EB);
        context.drawTextWithShadow(textRenderer, "Клик по нику слева копирует ник и открывает меню наказаний.", sx, sy + 124, 0xA7F3D0);

        super.render(context, mouseX, mouseY, delta);
    }
}
