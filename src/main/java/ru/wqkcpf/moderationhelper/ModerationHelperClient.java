package ru.wqkcpf.moderationhelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wqkcpf.moderationhelper.chat.ChatLineTracker;
import ru.wqkcpf.moderationhelper.chat.ChatNicknameParser;
import ru.wqkcpf.moderationhelper.config.ModConfig;
import ru.wqkcpf.moderationhelper.gui.PunishmentScreen;
import ru.wqkcpf.moderationhelper.gui.StatsScreen;
import ru.wqkcpf.moderationhelper.keybind.KeybindManager;
import ru.wqkcpf.moderationhelper.obs.ObsController;
import ru.wqkcpf.moderationhelper.recent.RecentPlayersManager;
import ru.wqkcpf.moderationhelper.screenshot.ScreenshotManager;
import ru.wqkcpf.moderationhelper.stats.SessionStats;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class ModerationHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "moderation_helper_gui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;
    public static SessionStats STATS;
    public static RecentPlayersManager RECENT_PLAYERS;
    public static ScreenshotManager SCREENSHOTS;
    public static ObsController OBS;
    public static KeybindManager KEYBINDS;

    private static boolean checkRecordingOverlayVisible = false;
    private static Instant checkRecordingStartedAt;
    private static String checkedNick;

    @Override
    public void onInitializeClient() {
        CONFIG = ModConfig.load();
        STATS = new SessionStats();
        RECENT_PLAYERS = new RecentPlayersManager(CONFIG.recentPlayersLimit);
        SCREENSHOTS = new ScreenshotManager(CONFIG);
        OBS = new ObsController(CONFIG);
        KEYBINDS = new KeybindManager();

        SCREENSHOTS.ensureDirectories();
        SCREENSHOTS.cleanupOldScreenshots();
        KEYBINDS.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KEYBINDS.tick(client);
            updateRecordingOverlay(client);
        });

        LOGGER.info("Moderation Helper GUI initialized");
    }

    /**
     * Called from ChatScreenMixin after middle-click in opened chat.
     * The line is taken from our recent-chat cache before the custom menu is displayed.
     */
    public static boolean handleChatMiddleClick(String chatLine) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        Optional<String> nickOptional = ChatNicknameParser.findNickname(chatLine);
        if (nickOptional.isEmpty()) {
            message("§cНик не найден в строке чата.");
            return false;
        }

        String nick = nickOptional.get();
        RECENT_PLAYERS.add(nick);

        Path tempScreenshot = null;
        if (!ChatNicknameParser.shouldSkipScreenshot(chatLine)) {
            tempScreenshot = SCREENSHOTS.makeTempScreenshot(nick).orElse(null);
        }

        Path finalTempScreenshot = tempScreenshot;
        client.execute(() -> client.setScreen(new PunishmentScreen(nick, finalTempScreenshot)));
        return true;
    }

    public static void openPunishmentMenuNoScreenshot(String nick) {
        MinecraftClient client = MinecraftClient.getInstance();
        RECENT_PLAYERS.add(nick);
        client.setScreen(new PunishmentScreen(nick, null));
    }

    public static void openStatsScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new StatsScreen());
    }

    public static void startCheck(String nick) {
        sendCommand("tpp " + nick);
        sendCommand("tp " + nick);
        sendCommand(CONFIG.checkCommandTemplate.replace("{nick}", nick).replaceFirst("^/", ""));

        String tellText = CONFIG.checkTellTemplate.replace("{nick}", nick);
        sendCommand("tell " + nick + " " + tellText);

        checkedNick = nick;
        checkRecordingStartedAt = Instant.now();
        checkRecordingOverlayVisible = true;
        OBS.startRecording();
        message("§aИгрок вызван на проверку: §f" + nick);
    }

    public static void stopCheckRecording(String reason) {
        checkRecordingOverlayVisible = false;
        checkRecordingStartedAt = null;
        checkedNick = null;
        OBS.stopRecording();
        message("§eЗапись OBS остановлена" + (reason == null || reason.isBlank() ? "." : ": " + reason));
    }

    public static boolean isCheckRecordingActive() {
        return checkRecordingOverlayVisible;
    }

    private static void updateRecordingOverlay(MinecraftClient client) {
        if (!checkRecordingOverlayVisible || checkRecordingStartedAt == null || client.inGameHud == null) {
            return;
        }

        long seconds = Duration.between(checkRecordingStartedAt, Instant.now()).toSeconds();
        long min = seconds / 60;
        long sec = seconds % 60;
        String nickPart = checkedNick == null ? "" : " | " + checkedNick;
        client.inGameHud.setOverlayMessage(Text.literal(String.format("Идёт запись: %02d:%02d%s", min, sec, nickPart)), false);
    }

    public static void sendCommand(String commandWithoutSlash) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            message("§cНельзя отправить команду: игрок не подключён к серверу.");
            return;
        }
        String command = commandWithoutSlash.startsWith("/") ? commandWithoutSlash.substring(1) : commandWithoutSlash;
        client.player.networkHandler.sendChatCommand(command);
    }

    public static void message(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(text), false);
        } else {
            LOGGER.info(text.replace('§', '&'));
        }
    }

    public static String latestTrackedLineAt(double x, double y) {
        return ChatLineTracker.findLineAt(x, y).orElse("");
    }

    public static boolean shouldIgnoreStopKeyBecauseChatOpen(MinecraftClient client) {
        return client.currentScreen instanceof ChatScreen;
    }
}
