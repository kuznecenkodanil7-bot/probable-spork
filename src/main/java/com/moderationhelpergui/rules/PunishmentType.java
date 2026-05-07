package com.moderationhelpergui.rules;

public enum PunishmentType {
    WARN("warn", "warn"),
    MUTE("mute", "mute"),
    BAN("ban", "ban"),
    IPBAN("ipban", "ipban");

    public final String commandName;
    public final String folderName;

    PunishmentType(String commandName, String folderName) {
        this.commandName = commandName;
        this.folderName = folderName;
    }
}
