package ru.wqkcpf.moderationhelper.chat;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class ChatNicknameParser {
    private static final Pattern COLOR_CODES = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final Pattern MINECRAFT_NICK = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private static final Set<String> RANKS = Set.of(
            "HT5", "LT5", "HT4", "LT4", "HT3", "LT3", "HT2", "LT2", "HT1", "LT1",
            "RHT3", "RLT3", "RHT2", "RLT2", "RHT1", "RLT1",
            "XHT5", "XLT5", "XHT4", "XLT4", "XHT3", "XLT3", "XHT2", "XLT2", "XHT1", "XLT1",
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    );

    private static final Set<String> SERVER_MARKERS = Set.of(
            "anarchy-alpha", "anarchy-beta", "anarchy-gamma", "anarchy-new", "duels"
    );

    private static final String[] SCREENSHOT_EXCLUDE_WORDS = {
            "Tick Speed", "Reach", "Fighting suspiciously", "Block Interaction"
    };

    private ChatNicknameParser() {}

    public static Optional<String> findNickname(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) {
            return Optional.empty();
        }

        String line = stripMinecraftColors(rawLine);
        String[] rawTokens = line.split("\\s+");

        for (int i = 0; i < rawTokens.length; i++) {
            String token = cleanupToken(rawTokens[i]);
            if (token.isBlank()) continue;

            String upper = token.toUpperCase(Locale.ROOT);
            String lower = token.toLowerCase(Locale.ROOT);

            if (RANKS.contains(upper)) {
                continue;
            }

            if (SERVER_MARKERS.contains(lower)) {
                Optional<String> next = findNextNickname(rawTokens, i + 1);
                if (next.isPresent()) return next;
                continue;
            }

            if (isNickname(token)) {
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }

    public static boolean shouldSkipScreenshot(String rawLine) {
        if (rawLine == null) return false;
        String lower = rawLine.toLowerCase(Locale.ROOT);
        for (String word : SCREENSHOT_EXCLUDE_WORDS) {
            if (lower.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String stripMinecraftColors(String text) {
        return COLOR_CODES.matcher(text == null ? "" : text).replaceAll("");
    }

    public static boolean isNickname(String token) {
        return MINECRAFT_NICK.matcher(token).matches();
    }

    private static Optional<String> findNextNickname(String[] tokens, int from) {
        for (int j = from; j < tokens.length; j++) {
            String candidate = cleanupToken(tokens[j]);
            if (candidate.isBlank()) continue;
            String upper = candidate.toUpperCase(Locale.ROOT);
            if (RANKS.contains(upper)) continue;
            if (isNickname(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    /** Removes brackets, arrows, colons and other garbage around the nickname, but keeps latin letters, digits and underscore. */
    private static String cleanupToken(String token) {
        if (token == null) return "";
        String stripped = stripMinecraftColors(token).trim();
        stripped = stripped.replaceAll("^[\\[\\]{}()<>:;|/\\\\\\-—–➡→»«'\"`~,.!?]+", "");
        stripped = stripped.replaceAll("[\\[\\]{}()<>:;|/\\\\\\-—–➡→»«'\"`~,.!?]+$", "");
        return stripped;
    }
}
