package com.moderationhelpergui.recent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecentPlayersManager {
    private final int limit;
    private final List<String> players = new ArrayList<>();

    public RecentPlayersManager(int limit) {
        this.limit = Math.max(1, limit);
    }

    public void add(String nick) {
        if (nick == null || nick.isBlank()) return;
        players.removeIf(existing -> existing.equalsIgnoreCase(nick));
        players.add(0, nick);
        while (players.size() > limit) {
            players.remove(players.size() - 1);
        }
    }

    public List<String> list() {
        return Collections.unmodifiableList(players);
    }
}
