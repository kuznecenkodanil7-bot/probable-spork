package com.moderationhelpergui;

import com.moderationhelpergui.config.ModConfig;
import com.moderationhelpergui.gui.PunishmentScreen;
import com.moderationhelpergui.gui.StatsScreen;
import com.moderationhelpergui.keybind.KeybindManager;
import com.moderationhelpergui.obs.ObsController;
import com.moderationhelpergui.recent.RecentPlayersManager;
import com.moderationhelpergui.rules.PunishmentType;
import com.moderationhelpergui.chat.ChatNicknameParser;
import com.moderationhelpergui.screenshot.ScreenshotManager;
import com.moderationhelpergui.stats.SessionStats;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public final class ModerationHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "moderation_helper_gui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;
    public static SessionStats STATS;
    public static RecentPlayersManager RECENT_PLAYERS;
    public static ScreenshotManager SCREENSHOTS;
    public static ObsController OBS;

    private static boolean checkRecordingTimerActive = false;
    private static long checkRecordingStartedAtMs = 0L;
    private static long lastTimerSecond = -1L;

    @Override
    public void onInitializeClient() {
        CONFIG = ModConfig.load();
        STATS = new SessionStats();
        RECENT_PLAYERS = new RecentPlayersManager(CONFIG.recentPlayersLimit);
        SCREENSHOTS = new ScreenshotManager(CONFIG);
        OBS = new ObsController(CONFIG);

        KeybindManager.register();
        SCREENSHOTS.cleanupOldScreenshots();

        ClientTickEvents.END_CLIENT_TICK.register(client -> tickRecordingOverlay(client));
        LOGGER.info("Moderation Helper GUI loaded");
    }

    /**
     * Called by the chat-screen mixin when middle mouse button is clicked on a visible chat line.
     * Screenshot is created here before the punishment screen is opened.
     */
    public static void openPunishmentFromChatLine(String chatLine) {
        MinecraftClient client = MinecraftClient.getInstance();
        Optional<String> nickname = ChatNicknameParser.findNickname(chatLine);

        if (nickname.isEmpty()) {
            notifyClient("Ник не найден в сообщении чата.");
            return;
        }

        String nick = nickname.get();
        boolean noScreenshot = ChatNicknameParser.containsNoScreenshotKeyword(chatLine);
        Path tempScreenshot = null;

        if (!noScreenshot) {
            tempScreenshot = SCREENSHOTS.captureTemp(nick);
        }

        RECENT_PLAYERS.add(nick);
        Path finalTempScreenshot = tempScreenshot;
        client.execute(() -> client.setScreen(new PunishmentScreen(nick, finalTempScreenshot)));
    }

    public static void openPunishmentWithoutScreenshot(String nick) {
        if (nick == null || nick.isBlank()) {
            notifyClient("Ник не указан.");
            return;
        }
        RECENT_PLAYERS.add(nick);
        MinecraftClient.getInstance().setScreen(new PunishmentScreen(nick, null));
    }

    public static void openStatsScreen() {
        MinecraftClient.getInstance().setScreen(new StatsScreen());
    }

    public static void issuePunishment(String nick, PunishmentType type, String duration, String reason, Path tempScreenshot) {
        if (MinecraftClient.getInstance().player == null) return;

        String cleanReason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
        String cleanDuration = duration == null || duration.isBlank() ? "none" : duration.trim();

        switch (type) {
            case WARN -> sendCommand("warn " + nick + " " + cleanReason);
            case MUTE -> sendCommand("mute " + nick + " " + cleanDuration + " " + cleanReason);
            case BAN -> sendCommand("ban " + nick + " " + cleanDuration + " " + cleanReason);
            case IPBAN -> sendCommand("ipban " + nick + " " + cleanDuration + " " + cleanReason);
        }

        STATS.increment(type);
        RECENT_PLAYERS.add(nick);
        SCREENSHOTS.finalizeScreenshot(tempScreenshot, nick, type, cleanDuration, cleanReason);

        if (type == PunishmentType.IPBAN && shouldStopObsAfterIpBan(cleanReason)) {
            stopObsRecording("IPBan выдан: запись OBS остановлена автоматически.");
        }

        notifyClient("Наказание отправлено: " + type.commandName + " → " + nick);
        MinecraftClient.getInstance().setScreen(null);
    }

    private static boolean shouldStopObsAfterIpBan(String reason) {
        String value = reason.trim().toLowerCase();
        return !(value.equals("бот") || value.startsWith("3.8"));
    }

    public static void warnImmediately(String nick, Path tempScreenshot) {
        issuePunishment(nick, PunishmentType.WARN, "none", "2.1", tempScreenshot);
    }

    public static void startCheck(String nick) {
        ModConfig config = CONFIG;
        sendCommand("tpp " + nick);
        sendCommand("tp " + nick);
        sendCommand(formatCommand(config.checkCommandTemplate, nick));

        String tellMessage = config.checkTellMessage == null ? "" : config.checkTellMessage.trim();
        if (!tellMessage.isEmpty()) {
            sendCommand("tell " + nick + " " + tellMessage);
        }

        OBS.startRecording();
        startRecordingTimer();
        notifyClient("Проверка начата для " + nick);
    }

    public static void stopCheck(String nick) {
        String command = formatCommand(CONFIG.removeCheckCommandTemplate, nick);
        if (!command.isBlank()) {
            sendCommand(command);
        }
        stopObsRecording("Проверка снята: запись OBS остановлена.");
    }

    public static void stopObsRecording(String message) {
        OBS.stopRecording();
        stopRecordingTimer();
        if (message != null && !message.isBlank()) {
            notifyClient(message);
        }
    }

    public static String formatCommand(String template, String nick) {
        if (template == null || template.isBlank()) return "";
        String command = template.replace("{nick}", nick).trim();
        return command.startsWith("/") ? command.substring(1) : command;
    }

    public static void sendCommand(String commandWithoutOrWithSlash) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) return;
        String command = commandWithoutOrWithSlash.startsWith("/")
                ? commandWithoutOrWithSlash.substring(1)
                : commandWithoutOrWithSlash;
        client.player.networkHandler.sendChatCommand(command);
    }

    public static void notifyClient(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        LOGGER.info(message);
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[MHG] " + message), false);
        }
    }

    public static void startRecordingTimer() {
        checkRecordingTimerActive = true;
        checkRecordingStartedAtMs = System.currentTimeMillis();
        lastTimerSecond = -1L;
    }

    public static void stopRecordingTimer() {
        checkRecordingTimerActive = false;
        lastTimerSecond = -1L;
    }

    private static void tickRecordingOverlay(MinecraftClient client) {
        if (!checkRecordingTimerActive || client.player == null) return;

        long seconds = Duration.ofMillis(System.currentTimeMillis() - checkRecordingStartedAtMs).toSeconds();
        if (seconds == lastTimerSecond) return;
        lastTimerSecond = seconds;

        long minutes = seconds / 60;
        long sec = seconds % 60;
        client.player.sendMessage(Text.literal(String.format("Идёт запись: %02d:%02d", minutes, sec)), true);
    }
}
