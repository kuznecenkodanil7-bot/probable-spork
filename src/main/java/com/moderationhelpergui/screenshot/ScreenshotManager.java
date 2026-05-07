package com.moderationhelpergui.screenshot;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.config.ModConfig;
import com.moderationhelpergui.rules.PunishmentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public final class ScreenshotManager {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final ModConfig config;
    private final Path root;

    public ScreenshotManager(ModConfig config) {
        this.config = config;
        this.root = MinecraftClient.getInstance().runDirectory.toPath().resolve(config.screenshotsRoot);
        ensureFolders();
    }

    public Path captureTemp(String nick) {
        try {
            ensureFolders();
            String safeNick = sanitize(nick);
            String datetime = LocalDateTime.now().format(FILE_TIME);
            String filename = safeNick + "_" + datetime + ".png";
            Path relative = Path.of("temp").resolve(filename);
            Path absolute = root.resolve(relative);

            Files.createDirectories(absolute.getParent());
            ScreenshotRecorder.saveScreenshot(
                    root.toFile(),
                    relative.toString().replace('\\', '/'),
                    MinecraftClient.getInstance().getFramebuffer(),
                    1,
                    text -> ModerationHelperClient.LOGGER.info("Screenshot: {}", text.getString())
            );
            return absolute;
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to make temp screenshot", e);
            ModerationHelperClient.notifyClient("Не удалось сделать скриншот, меню всё равно открыто.");
            return null;
        }
    }

    public void finalizeScreenshot(Path tempScreenshot, String nick, PunishmentType type, String duration, String reason) {
        if (tempScreenshot == null) return;

        try {
            ensureFolders();
            if (!Files.exists(tempScreenshot)) {
                ModerationHelperClient.LOGGER.warn("Temp screenshot does not exist yet: {}", tempScreenshot);
                return;
            }

            String datetime = LocalDateTime.now().format(FILE_TIME);
            String filename = sanitize(nick) + "_"
                    + sanitize(type.commandName) + "_"
                    + sanitize(duration) + "_"
                    + sanitize(reason) + "_"
                    + datetime + ".png";

            Path targetDir = root.resolve(type.folderName);
            Files.createDirectories(targetDir);
            Path target = unique(targetDir.resolve(filename));
            Files.move(tempScreenshot, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to sort screenshot", e);
            ModerationHelperClient.notifyClient("Не удалось перенести скриншот в папку наказания.");
        }
    }

    public void cleanupOldScreenshots() {
        if (!config.cleanupScreenshotsEnabled) return;
        ensureFolders();

        Instant border = Instant.now().minusSeconds((long) config.screenshotRetentionDays * 24L * 60L * 60L);
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".png"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant().isBefore(border);
                        } catch (IOException ignored) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            ModerationHelperClient.LOGGER.warn("Failed to delete old screenshot: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.warn("Screenshot cleanup failed", e);
        }
    }

    private void ensureFolders() {
        try {
            Files.createDirectories(root.resolve("temp"));
            Files.createDirectories(root.resolve("warn"));
            Files.createDirectories(root.resolve("mute"));
            Files.createDirectories(root.resolve("ban"));
            Files.createDirectories(root.resolve("ipban"));
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.error("Failed to create screenshot folders", e);
        }
    }

    private Path unique(Path base) {
        if (!Files.exists(base)) return base;
        String name = base.getFileName().toString();
        String stem = name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
        Path dir = base.getParent();
        for (int i = 2; i < 999; i++) {
            Path candidate = dir.resolve(stem + "_" + i + ".png");
            if (!Files.exists(candidate)) return candidate;
        }
        return base;
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) return "none";
        String result = value.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-zА-Яа-я0-9._-]", "_");
        return result.length() > 80 ? result.substring(0, 80) : result;
    }
}
