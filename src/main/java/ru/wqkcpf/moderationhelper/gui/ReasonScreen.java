package ru.wqkcpf.moderationhelper.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;
import ru.wqkcpf.moderationhelper.punishment.PunishmentRule;
import ru.wqkcpf.moderationhelper.punishment.PunishmentRules;
import ru.wqkcpf.moderationhelper.punishment.PunishmentType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReasonScreen extends Screen {
    private final String nick;
    private final PunishmentType type;
    private final Path tempScreenshot;
    private TextFieldWidget manualReasonField;

    public ReasonScreen(String nick, PunishmentType type, Path tempScreenshot) {
        super(Text.literal("Выбор причины"));
        this.nick = nick;
        this.type = type;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int panelW = Math.min(820, width - 24);
        int panelH = Math.min(520, height - 24);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        manualReasonField = new TextFieldWidget(textRenderer, x + 24, y + panelH - 70, 300, 22, Text.literal("Причина"));
        manualReasonField.setMaxLength(80);
        manualReasonField.setText("");
        addDrawableChild(manualReasonField);

        addDrawableChild(GuiUtil.button(x + 334, y + panelH - 70, 160, 22, "Своя причина", b -> {
            String reason = manualReasonField.getText().trim();
            if (reason.isBlank()) {
                ModerationHelperClient.message("§cВведите причину.");
                return;
            }
            client.setScreen(new DurationScreen(nick, type, reason, "", false, tempScreenshot));
        }));

        addDrawableChild(GuiUtil.button(x + panelW - 126, y + panelH - 38, 100, 22, "Назад", b ->
                client.setScreen(new PunishmentScreen(nick, tempScreenshot))));

        List<PunishmentRule> rules = getRules();
        int columns = type == PunishmentType.IPBAN ? 2 : 2;
        int buttonW = (panelW - 64) / columns;
        int buttonH = 24;
        int startY = y + 76;
        int startX = x + 24;
        for (int i = 0; i < rules.size(); i++) {
            PunishmentRule rule = rules.get(i);
            int col = i % columns;
            int row = i / columns;
            int bx = startX + col * (buttonW + 12);
            int by = startY + row * 30;
            addDrawableChild(GuiUtil.button(bx, by, buttonW, buttonH, shortLabel(rule), b -> {
                String defaultDuration = rule.permanent() ? "permanent" : rule.defaultDuration();
                client.setScreen(new DurationScreen(nick, type, rule.reason(), defaultDuration, rule.permanent(), tempScreenshot));
            }));
        }
    }

    private List<PunishmentRule> getRules() {
        List<PunishmentRule> rules = new ArrayList<>(PunishmentRules.rulesFor(type));
        switch (type) {
            case MUTE -> ModerationHelperClient.CONFIG.extraQuickMuteReasons.forEach(r -> rules.add(new PunishmentRule(r, "из конфига", "", false)));
            case BAN -> ModerationHelperClient.CONFIG.extraQuickBanReasons.forEach(r -> rules.add(new PunishmentRule(r, "из конфига", "", false)));
            case IPBAN -> ModerationHelperClient.CONFIG.extraQuickIpBanReasons.forEach(r -> rules.add(new PunishmentRule(r, "из конфига", "", false)));
            default -> {}
        }
        return rules;
    }

    private String shortLabel(PunishmentRule rule) {
        String label = rule.label();
        return label.length() > 64 ? label.substring(0, 61) + "..." : label;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        int panelW = Math.min(820, width - 24);
        int panelH = Math.min(520, height - 24);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        GuiUtil.drawPanel(context, x, y, panelW, panelH);
        context.drawTextWithShadow(textRenderer, Text.literal(type.displayName + " — выбор причины"), x + 24, y + 20, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Ник: " + nick), x + 24, y + 42, GuiUtil.ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("После причины откроется выбор времени. Время можно исправить вручную."), x + 24, y + 58, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("Своя причина:"), x + 24, y + panelH - 84, GuiUtil.GRAY);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
