# Moderation Helper GUI

Клиентский Fabric-мод для Minecraft Java Edition 1.21.11. Мод помогает модератору быстро выдавать `warn`, `mute`, `ban`, `ipban`, делать скрин чата до открытия меню, хранить недавних игроков, вести статистику за сессию и управлять записью OBS через `obs-websocket`.

## Что реализовано

- СКМ по строке чата открывает меню наказаний по найденному нику.
- Скриншот делается до открытия GUI и сначала кладётся в `moderation_screenshots/temp/`.
- После наказания скрин переносится в `warn`, `mute`, `ban` или `ipban`.
- Скрин не делается, если в строке есть `Tick Speed`, `Reach`, `Fighting suspiciously`, `Block Interaction`.
- Игнорируются ранги `HT5/LT5/...`, римские цифры `I-X`, а также слова `anarchy-alpha`, `anarchy-beta`, `anarchy-gamma`, `anarchy-new`, `duels`.
- Ник фильтруется под Minecraft-формат: латиница, цифры, `_`, длина 3-16.
- `Warn` выдаётся сразу по причине `2.1`.
- `Mute`, `Ban`, `IPBan` разделены по категориям причин.
- H открывает только статистику и список недавних игроков: без скрина и без поиска ника в чате.
- G останавливает запись OBS, но если открыт чат, G игнорируется и запись не останавливается.
- Кнопка `Вызвать на проверку` отправляет `/tpp`, `/tp`, `/check`, `/tell`, запускает OBS и таймер `Идёт запись: 00:00` над хотбаром.
- Кнопка `Снять с проверки` вызывает команду из конфига и останавливает OBS.
- При `ipban` запись OBS останавливается автоматически, кроме причин `бот` и `3.8`.
- Старые скриншоты удаляются при старте клиента, по умолчанию старше 30 дней.

## Установка

1. Установи Fabric Loader для Minecraft `1.21.11`.
2. Поставь Fabric API под `1.21.11`.
3. Собери мод:

```bash
gradle build
```

4. Готовый jar будет в:

```text
build/libs/
```

5. Положи jar в папку:

```text
.minecraft/mods/
```

## Сборка через GitHub

Проект уже содержит workflow:

```text
.github/workflows/build.yml
```

Залей папку проекта в репозиторий GitHub. Во вкладке `Actions` запусти `Build Fabric Mod`. После сборки jar будет в артефактах workflow.

## Управление

Все клавиши настраиваются в Minecraft:

```text
Options -> Controls -> Key Binds -> Moderation Helper GUI
```

По умолчанию:

- `СКМ` по строке чата — открыть меню наказаний по нику.
- `H` — открыть статистику и недавних игроков.
- `G` — остановить OBS-запись, если чат не открыт.

## OBS WebSocket

В OBS 28+ `obs-websocket` обычно уже встроен.

Настройка:

1. Открой OBS.
2. Зайди в `Tools -> WebSocket Server Settings`.
3. Включи WebSocket server.
4. Проверь порт: обычно `4455`.
5. Укажи пароль, если он включён.
6. В конфиге мода пропиши тот же пароль.

Конфиг создаётся после первого запуска:

```text
.minecraft/config/moderation-helper-gui.json
```

Основные поля:

```json
{
  "obsEnabled": true,
  "obsHost": "localhost",
  "obsPort": 4455,
  "obsPassword": "",
  "recentPlayersLimit": 15,
  "cleanupScreenshotsEnabled": true,
  "screenshotRetentionDays": 30,
  "screenshotsRoot": "moderation_screenshots",
  "checkCommandTemplate": "/check {nick}",
  "removeCheckCommandTemplate": "/uncheck {nick}"
}
```

## Команды проверки

При нажатии `Вызвать на проверку` мод отправляет:

```text
/tpp {nick}
/tp {nick}
/check {nick}
/tell {nick} Здравствуйте, проверка на читы. В течении 5 минут жду ваш Anydesk (наилучший вариант, скачать можно в любом браузере)/Discord. Также сообщаю, что в случае признания на наличие чит-клиентов срок бана составит 20 дней, вместо 30.
```

Текст `/tell` можно изменить в конфиге через поле `checkTellMessage`.

## Скриншоты

Папка по умолчанию:

```text
.minecraft/moderation_screenshots/
```

Структура:

```text
moderation_screenshots/
  temp/
  warn/
  mute/
  ban/
  ipban/
```

Временный файл:

```text
temp/{nick}_{datetime}.png
```

Финальный файл:

```text
{nick}_{punishment}_{duration}_{reason}_{datetime}.png
```

Запрещённые символы в имени файла заменяются на `_`.

## Важное замечание

Для разных серверов команды могут отличаться. Если на твоём сервере `permanent` не принимается, замени в коде/конфиге на нужный формат, например `9999d` или `forever`.
