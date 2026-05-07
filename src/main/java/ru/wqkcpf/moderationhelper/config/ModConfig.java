package ru.wqkcpf.moderationhelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean obsEnabled = true;
    public String obsHost = "localhost";
    public int obsPort = 4455;
    public String obsPassword = "";

    public int recentPlayersLimit = 15;

    /** DELETE / ARCHIVE / OFF */
    public String screenshotCleanupMode = "DELETE";
    public int screenshotRetentionDays = 30;
    public String screenshotDir = "moderation_screenshots";

    public String checkCommandTemplate = "/check {nick}";
    public String checkTellTemplate = "Здравствуйте, проверка на читы. В течении 5 минут жду ваш Anydesk (наилучший вариант, скачать можно в любом браузере)/Discord. Также сообщаю, что в случае признания на наличие чит-клиентов срок бана составит 20 дней, вместо 30.";

    /** Можно менять/добавлять быстрые причины, но базовые причины из PunishmentRules остаются в коде. */
    public List<String> extraQuickMuteReasons = new ArrayList<>();
    public List<String> extraQuickBanReasons = new ArrayList<>();
    public List<String> extraQuickIpBanReasons = new ArrayList<>();

    public static ModConfig load() {
        Path path = configPath();
        try {
            if (Files.notExists(path)) {
                ModConfig config = new ModConfig();
                config.save();
                return config;
            }
            try (Reader reader = Files.newBufferedReader(path)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config == null) {
                    config = new ModConfig();
                }
                config.normalize();
                config.save();
                return config;
            }
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to load config, using defaults", e);
            return new ModConfig();
        }
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.error("Failed to save config", e);
        }
    }

    public Path screenshotBasePath() {
        Path p = Path.of(screenshotDir);
        if (p.isAbsolute()) {
            return p;
        }
        return FabricLoader.getInstance().getGameDir().resolve(p).normalize();
    }

    private void normalize() {
        if (recentPlayersLimit < 1) recentPlayersLimit = 15;
        if (screenshotRetentionDays < 1) screenshotRetentionDays = 30;
        if (screenshotDir == null || screenshotDir.isBlank()) screenshotDir = "moderation_screenshots";
        if (obsHost == null || obsHost.isBlank()) obsHost = "localhost";
        if (obsPort <= 0) obsPort = 4455;
        if (checkCommandTemplate == null || checkCommandTemplate.isBlank()) checkCommandTemplate = "/check {nick}";
        if (checkTellTemplate == null || checkTellTemplate.isBlank()) {
            checkTellTemplate = "Здравствуйте, проверка на читы. В течении 5 минут жду ваш Anydesk (наилучший вариант, скачать можно в любом браузере)/Discord. Также сообщаю, что в случае признания на наличие чит-клиентов срок бана составит 20 дней, вместо 30.";
        }
        String mode = screenshotCleanupMode == null ? "DELETE" : screenshotCleanupMode.toUpperCase();
        if (!mode.equals("DELETE") && !mode.equals("ARCHIVE") && !mode.equals("OFF")) {
            mode = "DELETE";
        }
        screenshotCleanupMode = mode;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("moderation_helper_gui.json");
    }
}
