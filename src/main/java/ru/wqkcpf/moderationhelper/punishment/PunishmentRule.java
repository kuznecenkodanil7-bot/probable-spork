package ru.wqkcpf.moderationhelper.punishment;

public record PunishmentRule(
        String reason,
        String description,
        String defaultDuration,
        boolean permanent
) {
    public String label() {
        String time = permanent ? "навсегда" : defaultDuration;
        return reason + " — " + description + (time == null || time.isBlank() ? "" : " [" + time + "]");
    }
}
