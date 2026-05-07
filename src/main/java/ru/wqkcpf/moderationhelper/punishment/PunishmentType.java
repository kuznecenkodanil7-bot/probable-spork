package ru.wqkcpf.moderationhelper.punishment;

public enum PunishmentType {
    WARN("warn", "Warn", "warn"),
    MUTE("mute", "Mute", "mute"),
    BAN("ban", "Ban", "ban"),
    IPBAN("ipban", "IPBan", "ipban");

    public final String command;
    public final String displayName;
    public final String folder;

    PunishmentType(String command, String displayName, String folder) {
        this.command = command;
        this.displayName = displayName;
        this.folder = folder;
    }
}
