package com.moderationhelpergui.stats;

import com.moderationhelpergui.rules.PunishmentType;

import java.util.EnumMap;
import java.util.Map;

public final class SessionStats {
    private final Map<PunishmentType, Integer> values = new EnumMap<>(PunishmentType.class);

    public SessionStats() {
        for (PunishmentType type : PunishmentType.values()) {
            values.put(type, 0);
        }
    }

    public void increment(PunishmentType type) {
        values.put(type, get(type) + 1);
    }

    public int get(PunishmentType type) {
        return values.getOrDefault(type, 0);
    }
}
