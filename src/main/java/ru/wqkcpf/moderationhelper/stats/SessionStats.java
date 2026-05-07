package ru.wqkcpf.moderationhelper.stats;

import ru.wqkcpf.moderationhelper.punishment.PunishmentType;

public class SessionStats {
    private int warn;
    private int mute;
    private int ban;
    private int ipban;

    public void increment(PunishmentType type) {
        switch (type) {
            case WARN -> warn++;
            case MUTE -> mute++;
            case BAN -> ban++;
            case IPBAN -> ipban++;
        }
    }

    public int warn() { return warn; }
    public int mute() { return mute; }
    public int ban() { return ban; }
    public int ipban() { return ipban; }
}
