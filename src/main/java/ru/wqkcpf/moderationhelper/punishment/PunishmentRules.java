package ru.wqkcpf.moderationhelper.punishment;

import java.util.List;

public final class PunishmentRules {
    private PunishmentRules() {}

    public static final PunishmentRule WARN_2_1 = new PunishmentRule("2.1", "предупреждение", "", false);

    public static List<PunishmentRule> muteRules() {
        return List.of(
                new PunishmentRule("2.2", "Оскорбления кого-либо/чего-либо", "2d", false),
                new PunishmentRule("2.3", "Оскорбление родных", "5d", false),
                new PunishmentRule("2.4", "Сообщения сексуального характера", "2h", false),
                new PunishmentRule("2.5", "Неадекватное поведение", "2h", false),
                new PunishmentRule("2.6", "Реклама серверов и сторонних ресурсов", "1d", false),
                new PunishmentRule("2.7", "Пропаганда/агитация ненависти и вражды", "9h", false),
                new PunishmentRule("2.8", "Сторонние ссылки/реклама стримов и видео", "1d", false),
                new PunishmentRule("2.9", "Выдача себя за администратора", "12h", false),
                new PunishmentRule("2.10", "Угрозы вне игрового процесса", "12h", false),
                new PunishmentRule("2.11", "Угроза наказанием без причины", "6h", false),
                new PunishmentRule("2.12", "Обсуждение политики", "12h", false),
                new PunishmentRule("2.13", "Введение игроков в заблуждение", "2h", false),
                new PunishmentRule("2.14", "Попрошайничество у администрации", "6h", false),
                new PunishmentRule("2.15", "Помехи в голосовом чате", "4h", false)
        );
    }

    public static List<PunishmentRule> banRules() {
        return List.of(
                new PunishmentRule("2.2", "Оскорбления кого-либо/чего-либо", "2d", false),
                new PunishmentRule("2.3", "Оскорбление родных", "7d", false),
                new PunishmentRule("2.6", "Реклама серверов и сторонних ресурсов", "7d", false),
                new PunishmentRule("2.7", "Пропаганда/агитация ненависти и вражды", "3d", false),
                new PunishmentRule("3.1", "Никнейм с нарушением правил/названием читов", "permanent", true),
                new PunishmentRule("4.1", "Донатер без доказательств наказания", "20d", false)
        );
    }

    public static List<PunishmentRule> ipBanRules() {
        return List.of(
                new PunishmentRule("бот", "Бот", "permanent", true),
                new PunishmentRule("уход от проверки", "Уход от проверки", "30d", false),
                new PunishmentRule("время вышло", "Время проверки вышло", "30d", false),
                new PunishmentRule("неадекватное поведение во время проверки", "Неадекватное поведение во время проверки", "30d", false),
                new PunishmentRule("признание", "Признание на проверке", "20d", false),
                new PunishmentRule("3.3", "Торговля за реальные деньги", "30d", false),
                new PunishmentRule("3.6", "Использование багов/лаг-машины", "15d", false),
                new PunishmentRule("3.7", "Использование/хранение стороннего ПО", "30d", false),
                new PunishmentRule("3.8", "Обход бана за читы", "30d", false),
                new PunishmentRule("3.9", "Подстрекательство к нарушениям", "3d", false),
                new PunishmentRule("3.10", "Тим с игроком со сторонним ПО", "15d", false)
        );
    }

    public static List<PunishmentRule> rulesFor(PunishmentType type) {
        return switch (type) {
            case WARN -> List.of(WARN_2_1);
            case MUTE -> muteRules();
            case BAN -> banRules();
            case IPBAN -> ipBanRules();
        };
    }
}
