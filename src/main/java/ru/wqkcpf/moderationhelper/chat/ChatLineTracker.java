package ru.wqkcpf.moderationhelper.chat;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class ChatLineTracker {
    private static final int MAX_LINES = 100;
    private static final LinkedList<String> LINES_NEWEST_FIRST = new LinkedList<>();

    private ChatLineTracker() {}

    public static synchronized void addLine(String text) {
        if (text == null || text.isBlank()) return;
        LINES_NEWEST_FIRST.addFirst(ChatNicknameParser.stripMinecraftColors(text));
        while (LINES_NEWEST_FIRST.size() > MAX_LINES) {
            LINES_NEWEST_FIRST.removeLast();
        }
    }

    public static synchronized Optional<String> latest() {
        return LINES_NEWEST_FIRST.isEmpty() ? Optional.empty() : Optional.of(LINES_NEWEST_FIRST.getFirst());
    }

    /**
     * Best-effort line hit detection for opened chat.
     * Minecraft 1.21.11 hides the original visible-message list behind private ChatHud internals, so this cache maps mouse Y to the recently received lines.
     */
    public static synchronized Optional<String> findLineAt(double mouseX, double mouseY) {
        if (LINES_NEWEST_FIRST.isEmpty()) {
            return Optional.empty();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();

        // Vanilla chat is rendered from bottom to top above the input field. This is intentionally conservative.
        int bottomY = screenHeight - 48;
        int lineHeight = 9;
        int row = (int) Math.floor((bottomY - mouseY) / lineHeight);

        List<String> copy = new ArrayList<>(LINES_NEWEST_FIRST);
        if (row >= 0 && row < copy.size()) {
            return Optional.of(copy.get(row));
        }

        // Fallback: if the click was inside the lower chat area, use the newest line instead of silently failing.
        if (mouseY >= screenHeight - 180 && mouseY <= screenHeight - 20) {
            return Optional.of(copy.getFirst());
        }
        return Optional.empty();
    }
}
