package ru.wqkcpf.moderationhelper.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;
import ru.wqkcpf.moderationhelper.punishment.PunishmentExecutor;
import ru.wqkcpf.moderationhelper.punishment.PunishmentType;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public class DurationScreen extends Screen {
    private static final Pattern DURATION = Pattern.compile("(?i)(\\d+[dh]|permanent|perm|forever|навсегда)");

    private final String nick;
    private final PunishmentType type;
    private final String reason;
    private final String defaultDuration;
    private final boolean permanent;
    private final Path tempScreenshot;
    private TextFieldWidget durationField;

    public DurationScreen(String nick, PunishmentType type, String reason, String defaultDuration, boolean permanent, Path tempScreenshot) {
        super(Text.literal("Выбор времени"));
        this.nick = nick;
        this.type = type;
        this.reason = reason;
        this.defaultDuration = defaultDuration == null ? "" : defaultDuration;
        this.permanent = permanent;
        this.tempScreenshot = tempScreenshot;
    }

    @Override
    protected void init() {
        int panelW = Math.min(520, width - 32);
        int panelH = 230;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        durationField = new TextFieldWidget(textRenderer, x + 28, y + 94, 180, 24, Text.literal("Время"));
        durationField.setMaxLength(24);
        durationField.setText(permanent ? "permanent" : defaultDuration);
        addDrawableChild(durationField);

        addDrawableChild(GuiUtil.button(x + 28, y + 138, 180, 24, "Выдать наказание", b -> submit()));
        addDrawableChild(GuiUtil.button(x + 218, y + 138, 120, 24, "Назад", b -> client.setScreen(new ReasonScreen(nick, type, tempScreenshot))));
    }

    private void submit() {
        String duration = durationField.getText().trim().toLowerCase(Locale.ROOT);
        if (type != PunishmentType.WARN && !DURATION.matcher(duration).matches()) {
            ModerationHelperClient.message("§cНеверное время. Пример: 7d, 12h, permanent.");
            return;
        }

        PunishmentExecutor.execute(nick, type, duration, reason, tempScreenshot);
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        int panelW = Math.min(520, width - 32);
        int panelH = 230;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        GuiUtil.drawPanel(context, x, y, panelW, panelH);
        context.drawTextWithShadow(textRenderer, Text.literal(type.displayName + " — время наказания"), x + 28, y + 24, GuiUtil.WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Ник: " + nick), x + 28, y + 46, GuiUtil.ACCENT);
        context.drawTextWithShadow(textRenderer, Text.literal("Причина: " + reason), x + 28, y + 64, GuiUtil.GRAY);
        context.drawTextWithShadow(textRenderer, Text.literal("Формат: 7d, 12h или permanent"), x + 28, y + 82, GuiUtil.GRAY);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
