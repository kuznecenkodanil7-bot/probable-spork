package com.moderationhelpergui.rules;

import java.util.ArrayList;
import java.util.List;

public final class PunishmentRules {
    private PunishmentRules() {}

    public static List<PunishmentRule> rulesFor(PunishmentType type) {
        return switch (type) {
            case WARN -> warnRules();
            case MUTE -> muteRules();
            case BAN -> banRules();
            case IPBAN -> ipbanRules();
        };
    }

    public static List<PunishmentRule> warnRules() {
        return List.of(new PunishmentRule(PunishmentType.WARN, "2.1", "предупреждение", "none", List.of()));
    }

    public static List<PunishmentRule> muteRules() {
        List<PunishmentRule> rules = new ArrayList<>();
        rules.add(rule(PunishmentType.MUTE, "2.2", "Оскорбление кого-либо/чего-либо", "2h", "1h", "12h", "1d", "7d"));
        rules.add(rule(PunishmentType.MUTE, "2.3", "Оскорбление родных", "5d", "1d", "5d", "15d"));
        rules.add(rule(PunishmentType.MUTE, "2.4", "Сообщения сексуального характера", "2h", "2h"));
        rules.add(rule(PunishmentType.MUTE, "2.5", "Неадекватное поведение", "2h", "2h"));
        rules.add(rule(PunishmentType.MUTE, "2.6", "Реклама серверов и сторонних ресурсов", "1d", "1d"));
        rules.add(rule(PunishmentType.MUTE, "2.7", "Пропаганда/агитация ненависти и вражды", "9h", "9h"));
        rules.add(rule(PunishmentType.MUTE, "2.8", "Сторонние ссылки/реклама стримов и видео", "8h", "8h", "1d", "3d"));
        rules.add(rule(PunishmentType.MUTE, "2.9", "Выдача себя за администратора", "12h", "12h"));
        rules.add(rule(PunishmentType.MUTE, "2.10", "Угрозы вне игрового процесса", "12h", "12h"));
        rules.add(rule(PunishmentType.MUTE, "2.11", "Угроза наказанием без причины", "6h", "6h"));
        rules.add(rule(PunishmentType.MUTE, "2.12", "Обсуждение политики", "12h", "12h", "1d", "7d"));
        rules.add(rule(PunishmentType.MUTE, "2.13", "Введение игроков в заблуждение", "2h", "2h"));
        rules.add(rule(PunishmentType.MUTE, "2.14", "Попрошайничество у администрации", "6h", "6h"));
        rules.add(rule(PunishmentType.MUTE, "2.15", "Помехи в голосовом чате", "4h", "4h"));
        return rules;
    }

    public static List<PunishmentRule> banRules() {
        List<PunishmentRule> rules = new ArrayList<>();
        rules.add(rule(PunishmentType.BAN, "2.2", "Оскорбление кого-либо/чего-либо", "2d", "1d", "2d", "7d"));
        rules.add(rule(PunishmentType.BAN, "2.3", "Оскорбление родных", "7d", "3d", "7d"));
        rules.add(rule(PunishmentType.BAN, "2.6", "Реклама серверов и сторонних ресурсов", "14d", "1d", "7d", "14d"));
        rules.add(rule(PunishmentType.BAN, "2.7", "Пропаганда/агитация ненависти и вражды", "3d", "3d"));
        rules.add(rule(PunishmentType.BAN, "3.1", "Запрещённый никнейм", "permanent", "permanent"));
        rules.add(rule(PunishmentType.BAN, "4.1", "Донатер без доказательств", "20d", "5d", "15d", "20d", "30d"));
        return rules;
    }

    public static List<PunishmentRule> ipbanRules() {
        List<PunishmentRule> rules = new ArrayList<>();
        rules.add(rule(PunishmentType.IPBAN, "бот", "Бот", "permanent", "permanent"));
        rules.add(rule(PunishmentType.IPBAN, "уход от проверки", "Уход от проверки", "30d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "время вышло", "Время проверки вышло", "30d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "неадекватное поведение во время проверки", "Неадекватное поведение во время проверки", "30d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "признание", "Признание в читах", "20d", "20d"));
        rules.add(rule(PunishmentType.IPBAN, "3.3", "Торговля за реальные деньги", "30d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "3.6", "Баги сервера / лаг-машины", "15d", "1d", "7d", "15d"));
        rules.add(rule(PunishmentType.IPBAN, "3.7", "Использование/хранение стороннего ПО", "30d", "7d", "15d", "20d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "3.8", "Обход бана за читы другим аккаунтом", "30d", "30d"));
        rules.add(rule(PunishmentType.IPBAN, "3.9", "Подстрекательство к нарушению правил", "3d", "3d"));
        rules.add(rule(PunishmentType.IPBAN, "3.10", "Тим с игроком, использующим стороннее ПО", "15d", "15d"));
        return rules;
    }

    private static PunishmentRule rule(PunishmentType type, String id, String title, String defaultDuration, String... suggestions) {
        return new PunishmentRule(type, id, title, defaultDuration, List.of(suggestions));
    }
}
