package com.moderationhelpergui.rules;

import java.util.List;

public record PunishmentRule(
        PunishmentType type,
        String id,
        String title,
        String defaultDuration,
        List<String> suggestedDurations
) {
    public String displayName() {
        return id + " — " + title;
    }

    public boolean isPermanent() {
        return defaultDuration != null && defaultDuration.equalsIgnoreCase("permanent");
    }
}
