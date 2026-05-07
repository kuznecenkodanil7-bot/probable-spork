package com.moderationhelpergui.chat;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatNicknameParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]{1,32}");
    private static final Pattern MINECRAFT_NICK_PATTERN = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private static final Set<String> IGNORED_PREFIXES = Set.of(
            "HT5", "LT5", "HT4", "LT4", "HT3", "LT3", "HT2", "LT2", "HT1", "LT1",
            "RHT3", "RLT3", "RHT2", "RLT2", "RHT1", "RLT1",
            "XHT5", "XLT5", "XHT4", "XLT4", "XHT3", "XLT3", "XHT2", "XLT2", "XHT1", "XLT1",
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    );

    private static final Set<String> SERVER_WORDS = Set.of(
            "anarchy-alpha", "anarchy-beta", "anarchy-gamma", "anarchy-new", "duels"
    );

    private static final Set<String> NO_SCREENSHOT_WORDS = Set.of(
            "tick speed", "reach", "fighting suspiciously", "block interaction"
    );

    private ChatNicknameParser() {}

    public static Optional<String> findNickname(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return Optional.empty();

        String message = stripMinecraftColors(rawMessage);
        String[] tokens = extractTokens(message);
        if (tokens.length == 0) return Optional.empty();

        for (int i = 0; i < tokens.length; i++) {
            String lower = tokens[i].toLowerCase(Locale.ROOT);
            String upper = tokens[i].toUpperCase(Locale.ROOT);

            if (SERVER_WORDS.contains(lower)) {
                Optional<String> nextNick = nextNickname(tokens, i + 1);
                if (nextNick.isPresent()) return nextNick;
            }

            if (i == 0 && IGNORED_PREFIXES.contains(upper)) {
                Optional<String> nextNick = nextNickname(tokens, i + 1);
                if (nextNick.isPresent()) return nextNick;
            }
        }

        for (String token : tokens) {
            String cleaned = cleanNicknameCandidate(token);
            if (isIgnoredToken(token)) continue;
            if (isMinecraftNickname(cleaned)) return Optional.of(cleaned);
        }

        return Optional.empty();
    }

    public static boolean containsNoScreenshotKeyword(String rawMessage) {
        if (rawMessage == null) return false;
        String lower = stripMinecraftColors(rawMessage).toLowerCase(Locale.ROOT);
        return NO_SCREENSHOT_WORDS.stream().anyMatch(lower::contains);
    }

    public static boolean isMinecraftNickname(String value) {
        return value != null && MINECRAFT_NICK_PATTERN.matcher(value).matches();
    }

    public static String stripMinecraftColors(String text) {
        return text
                .replaceAll("(?i)§[0-9A-FK-OR]", "")
                .replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    private static Optional<String> nextNickname(String[] tokens, int start) {
        for (int i = start; i < tokens.length; i++) {
            if (isIgnoredToken(tokens[i])) continue;
            String cleaned = cleanNicknameCandidate(tokens[i]);
            if (isMinecraftNickname(cleaned)) return Optional.of(cleaned);
        }
        return Optional.empty();
    }

    private static boolean isIgnoredToken(String token) {
        if (token == null) return true;
        return IGNORED_PREFIXES.contains(token.toUpperCase(Locale.ROOT))
                || SERVER_WORDS.contains(token.toLowerCase(Locale.ROOT));
    }

    private static String[] extractTokens(String message) {
        Matcher matcher = TOKEN_PATTERN.matcher(message);
        return matcher.results()
                .map(result -> result.group())
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private static String cleanNicknameCandidate(String token) {
        if (token == null) return "";
        return token.replaceAll("[^A-Za-z0-9_]", "");
    }

    public static String debugTokens(String rawMessage) {
        return Arrays.toString(extractTokens(stripMinecraftColors(rawMessage)));
    }
}
