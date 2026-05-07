package ru.wqkcpf.moderationhelper.screenshot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;
import ru.wqkcpf.moderationhelper.config.ModConfig;
import ru.wqkcpf.moderationhelper.punishment.PunishmentType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenshotManager {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT)
            .withZone(ZoneId.systemDefault());

    private final ModConfig config;
    private final Path baseDir;

    public ScreenshotManager(ModConfig config) {
        this.config = config;
        this.baseDir = config.screenshotBasePath();
    }

    public void ensureDirectories() {
        try {
            Files.createDirectories(baseDir.resolve("temp"));
            Files.createDirectories(baseDir.resolve("warn"));
            Files.createDirectories(baseDir.resolve("mute"));
            Files.createDirectories(baseDir.resolve("ban"));
            Files.createDirectories(baseDir.resolve("ipban"));
            Files.createDirectories(baseDir.resolve("archive"));
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.error("Failed to create screenshot directories", e);
        }
    }

    public Optional<Path> makeTempScreenshot(String nick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getFramebuffer() == null) {
            return Optional.empty();
        }

        ensureDirectories();
        String now = FILE_TIME.format(Instant.now());
        Path path = baseDir.resolve("temp").resolve(sanitize(nick) + "_" + now + ".png");

        try {
            Files.createDirectories(path.getParent());
            ScreenshotRecorder.takeScreenshot(client.getFramebuffer(), nativeImage -> {
                try {
                    nativeImage.writeTo(path);
                    ModerationHelperClient.LOGGER.info("Saved temp moderation screenshot: {}", path);
                } catch (Exception e) {
                    ModerationHelperClient.LOGGER.error("Failed to write screenshot: " + path, e);
                    ModerationHelperClient.message("§cНе удалось сохранить скриншот: " + e.getMessage());
                } finally {
                    nativeImage.close();
                }
            });
            return Optional.of(path);
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to capture screenshot", e);
            ModerationHelperClient.message("§cНе удалось сделать скриншот: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void finalizeScreenshot(Path tempScreenshot, String nick, PunishmentType type, String duration, String reason) {
        if (tempScreenshot == null) return;

        try {
            ensureDirectories();
            if (Files.notExists(tempScreenshot)) {
                // ScreenshotRecorder writes asynchronously. Try once next tick-ish through a short helper thread.
                new Thread(() -> retryMove(tempScreenshot, nick, type, duration, reason), "ModerationScreenshotMoveRetry").start();
                return;
            }
            moveNow(tempScreenshot, nick, type, duration, reason);
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Failed to finalize screenshot", e);
            ModerationHelperClient.message("§cСкриншот не перенесён: " + e.getMessage());
        }
    }

    private void retryMove(Path tempScreenshot, String nick, PunishmentType type, String duration, String reason) {
        try {
            Thread.sleep(500L);
            if (Files.exists(tempScreenshot)) {
                moveNow(tempScreenshot, nick, type, duration, reason);
            }
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.error("Retry screenshot move failed", e);
        }
    }

    private void moveNow(Path tempScreenshot, String nick, PunishmentType type, String duration, String reason) throws IOException {
        String now = FILE_TIME.format(Instant.now());
        String fileName = sanitize(nick) + "_" + type.command + "_" + sanitize(duration == null || duration.isBlank() ? "none" : duration)
                + "_" + sanitize(reason == null || reason.isBlank() ? "unknown" : reason) + "_" + now + ".png";
        Path target = baseDir.resolve(type.folder).resolve(fileName);
        Files.createDirectories(target.getParent());
        Files.move(tempScreenshot, target, StandardCopyOption.REPLACE_EXISTING);
        ModerationHelperClient.LOGGER.info("Moved moderation screenshot to {}", target);
    }

    public void cleanupOldScreenshots() {
        String mode = config.screenshotCleanupMode == null ? "OFF" : config.screenshotCleanupMode.toUpperCase(Locale.ROOT);
        if (mode.equals("OFF")) return;

        ensureDirectories();
        Instant cutoff = Instant.now().minusSeconds(config.screenshotRetentionDays * 24L * 60L * 60L);
        try (Stream<Path> paths = Files.walk(baseDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                    .filter(path -> !path.startsWith(baseDir.resolve("archive")))
                    .forEach(path -> cleanupOne(path, cutoff, mode));
        } catch (IOException e) {
            ModerationHelperClient.LOGGER.error("Screenshot cleanup failed", e);
        }
    }

    private void cleanupOne(Path path, Instant cutoff, String mode) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            if (attrs.lastModifiedTime().toInstant().isAfter(cutoff)) return;

            if (mode.equals("DELETE")) {
                Files.deleteIfExists(path);
            } else if (mode.equals("ARCHIVE")) {
                Path archive = baseDir.resolve("archive").resolve(path.getFileName().toString());
                Files.createDirectories(archive.getParent());
                Files.move(path, archive, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.warn("Failed to cleanup screenshot {}", path, e);
        }
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) return "none";
        return value.replaceAll("[\\\\/:*?\"<>|\\s]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
