package com.moderationhelpergui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moderationhelpergui.ModerationHelperClient;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean obsEnabled = true;
    public String obsHost = "localhost";
    public int obsPort = 4455;
    public String obsPassword = "";

    public int recentPlayersLimit = 15;

    public boolean cleanupScreenshotsEnabled = true;
    public int screenshotRetentionDays = 30;
    public String screenshotsRoot = "moderation_screenshots";

    public String checkCommandTemplate = "/check {nick}";
    public String removeCheckCommandTemplate = "/uncheck {nick}";
    public String checkTellMessage = "Здравствуйте, проверка на читы. В течении 5 минут жду ваш Anydesk (наилучший вариант, скачать можно в любом браузере)/Discord. Также сообщаю, что в случае признания на наличие чит-клиентов срок бана составит 20 дней, вместо 30.";

    /** Быстрые причины можно менять под свой сервер. */
    public Map<String, List<String>> quickReasons = new LinkedHashMap<>();

    public ModConfig() {
        quickReasons.put("mute", List.of("2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9", "2.10", "2.11", "2.12", "2.13", "2.14", "2.15"));
        quickReasons.put("ban", List.of("2.2", "2.3", "2.6", "2.7", "3.1", "4.1"));
        quickReasons.put("ipban", List.of("бот", "уход от проверки", "время вышло", "неадекватное поведение во время проверки", "признание", "3.3", "3.6", "3.7", "3.8", "3.9", "3.10"));
    }

    public static ModConfig load() {
        Path file = configPath();
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                ModConfig defaults = new ModConfig();
                defaults.save();
                return defaults;
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded == null) loaded = new ModConfig();
                loaded.fixNulls();
                loaded.save();
                return loaded;
            }
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to load config. Default config will be used.", e);
            return new ModConfig();
        }
    }

    public void save() {
        Path file = configPath();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.error("Failed to save config", e);
        }
    }

    private void fixNulls() {
        if (obsHost == null || obsHost.isBlank()) obsHost = "localhost";
        if (screenshotsRoot == null || screenshotsRoot.isBlank()) screenshotsRoot = "moderation_screenshots";
        if (checkCommandTemplate == null) checkCommandTemplate = "/check {nick}";
        if (removeCheckCommandTemplate == null) removeCheckCommandTemplate = "/uncheck {nick}";
        if (checkTellMessage == null) checkTellMessage = "";
        if (quickReasons == null || quickReasons.isEmpty()) quickReasons = new ModConfig().quickReasons;
        if (recentPlayersLimit < 1) recentPlayersLimit = 15;
        if (screenshotRetentionDays < 1) screenshotRetentionDays = 30;
    }

    public static Path configPath() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config")
                .resolve("moderation-helper-gui.json");
    }
}
