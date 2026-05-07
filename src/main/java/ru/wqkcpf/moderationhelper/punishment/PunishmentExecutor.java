package ru.wqkcpf.moderationhelper.punishment;

import ru.wqkcpf.moderationhelper.ModerationHelperClient;

import java.nio.file.Path;
import java.util.Locale;

public final class PunishmentExecutor {
    private PunishmentExecutor() {}

    public static void execute(String nick, PunishmentType type, String duration, String reason, Path tempScreenshot) {
        String safeReason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
        String safeDuration = duration == null ? "" : duration.trim();

        String command = switch (type) {
            case WARN -> "warn " + nick + " " + safeReason;
            case MUTE -> "mute " + nick + " " + safeDuration + " " + safeReason;
            case BAN -> "ban " + nick + " " + safeDuration + " " + safeReason;
            case IPBAN -> "ipban " + nick + " " + safeDuration + " " + safeReason;
        };

        ModerationHelperClient.sendCommand(command);
        ModerationHelperClient.STATS.increment(type);
        ModerationHelperClient.RECENT_PLAYERS.add(nick);
        ModerationHelperClient.SCREENSHOTS.finalizeScreenshot(tempScreenshot, nick, type, safeDuration, safeReason);

        if (type == PunishmentType.IPBAN && shouldAutoStopObsOnIpBan(safeReason)) {
            ModerationHelperClient.stopCheckRecording("IPBan: " + safeReason);
        }
    }

    private static boolean shouldAutoStopObsOnIpBan(String reason) {
        String r = reason.toLowerCase(Locale.ROOT).trim();
        return !r.equals("3.8") && !r.equals("бот");
    }
}
