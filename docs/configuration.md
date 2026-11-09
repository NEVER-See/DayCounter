# Configuration

[README](../README.md) · [Compatibility](compatibility.md) · [Русский](#русский)

## Settings screen

Install the optional **Mod Menu** version listed for your Minecraft version in the [compatibility table](compatibility.md), then open **Mods → DayCounter → Configure**. Appearance and sound settings are local to each client; they do not alter the world's counter.

- **Apply** saves valid changes and makes them active. It is available when settings have changed.
- **Cancel**, including closing the screen, discards changes made since the last Apply.
- **Reset** restores defaults in the editor. Use Apply to save them or Cancel to discard them.
- The square **EN/RU** button in the top-right corner switches the settings language. English is the default. Apply also changes the day notification's language; Minecraft's own language is unaffected.
- Selecting a sound previews it immediately. Saving the selection still requires Apply.

With animation enabled, the notification fades in and out during the configured duration. Disabling animation makes the current day stay visible during gameplay; the duration field is then unused. The display is hidden while a screen is open or the HUD is hidden. Joining a world synchronizes the count silently: animated mode waits for the next new day, while persistent mode shows the received day.

## Configuration file

The client creates `config/daycounter.json` inside its Minecraft instance on first launch. Without Mod Menu, close the game, edit this file, and relaunch. Settings are not reloaded from disk while the game is running.

Default contents:

```json
{
  "durationSeconds": 8,
  "animationEnabled": true,
  "scale": 1,
  "position": "TOP",
  "xPercent": 50,
  "yPercent": 24,
  "soundId": "minecraft:block.note_block.chime",
  "language": "EN"
}
```

| Field | Default | Accepted values and meaning |
| --- | --- | --- |
| `durationSeconds` | `8` | Number from `5` to `50`; used in animated mode. |
| `animationEnabled` | `true` | `true` for a timed notification; `false` for persistent display. |
| `scale` | `1` | Number from `0.5` to `2` (50–200%). The display also fits itself to the available window. |
| `position` | `"TOP"` | `"TOP"`: upper part of the screen; `"HUD"`: above the hotbar area; `"MANUAL"`: use center coordinates below. |
| `xPercent` | `50` | Horizontal center from `0` (left) to `100` (right), used in manual mode. |
| `yPercent` | `24` | Vertical center from `0` (top) to `100` (bottom), used in manual mode. |
| `soundId` | `"minecraft:block.note_block.chime"` | One of the sound identifiers listed below. |
| `language` | `"EN"` | `"EN"` or `"RU"`; independent of Minecraft's selected language. |

Manual positioning keeps the whole notification within the screen, so a requested center at an edge is moved inward. Numeric values outside the supported ranges are clamped when the file is loaded. Missing or invalid values fall back to defaults. Unsupported sound identifiers use the default chime. Use JSON numbers with a decimal point; the settings screen also accepts a decimal comma.

If the file cannot be parsed, defaults are used and a warning is written to the game log. The original file is retained until settings are explicitly saved with Apply. Files larger than 64 KiB are rejected. If saving fails, the previously applied settings remain active.

## Sounds

All audio is supplied by Minecraft. Only these identifiers are supported:

| Sound / Звук | `soundId` |
| --- | --- |
| Chime / Перезвон | `minecraft:block.note_block.chime` |
| Pling note / Нота «Плинг» | `minecraft:block.note_block.pling` |
| Experience orb / Сфера опыта | `minecraft:entity.experience_orb.pickup` |
| Amethyst / Аметист | `minecraft:block.amethyst_block.chime` |
| Bell / Колокол | `minecraft:block.bell.use` |
| Level up / Новый уровень | `minecraft:entity.player.levelup` |
| Note bell / Колокольчик | `minecraft:block.note_block.bell` |
| Harp / Арфа | `minecraft:block.note_block.harp` |
| Flute / Флейта | `minecraft:block.note_block.flute` |
| Xylophone / Ксилофон | `minecraft:block.note_block.xylophone` |
| 8-bit note / 8-битная нота | `minecraft:block.note_block.bit` |
| Guitar / Гитара | `minecraft:block.note_block.guitar` |
| Banjo / Банджо | `minecraft:block.note_block.banjo` |
| Cowbell / Коровий колокольчик | `minecraft:block.note_block.cow_bell` |
| Iron xylophone / Металлофон | `minecraft:block.note_block.iron_xylophone` |
| Enchanting / Зачарование | `minecraft:block.enchantment_table.use` |
| Button click / Щелчок кнопки | `minecraft:ui.button.click` |
| Notification / Уведомление | `minecraft:ui.toast.in` |

## World data and multiplayer

The counter belongs to the world, not to a player or client settings file. In Minecraft **1.21.x**, its path relative to the world folder is `data/daycounter_days.dat`. In **26.x**, DayCounter uses Overworld saved data with identifier `daycounter:days`; for all supported **26.x** versions, its path relative to the world folder is:

```text
dimensions/minecraft/overworld/data/daycounter/days.dat
```

When opening a 1.21.x world in 26.x, DayCounter imports the legacy file once if no modern counter exists. It preserves the original file and writes the imported counter through normal world saves. Existing modern data takes precedence. An unreadable or invalid legacy counter is logged and starts at day 1; the original is retained. Back up the whole world before upgrading, and include its counter files when moving it. The stored number starts at 1 and advances at natural dawns and successful sleep transitions. It does not infer days that passed before installation or while the mod was absent. Changing client settings or resetting them does not reset the world count.

Singleplayer uses the server component included in the same JAR. For remote play, install the same DayCounter JAR for your exact Minecraft version on the server and on each client that should display the day. Without the server component, the client hides the count instead of estimating it. Clients without DayCounter can connect, but receive no notification.

## Русский

### Экран настроек

Установите необязательный **Mod Menu** для своей версии Minecraft по [таблице совместимости](compatibility.md#русский) и откройте **Моды → DayCounter → Настройки**. Оформление и звук настраиваются отдельно на каждом клиенте и не меняют счётчик мира.

- **Применить** сохраняет допустимые изменения и включает их.
- **Отмена** и закрытие экрана отменяют изменения после последнего применения.
- **Сбросить** возвращает значения по умолчанию в редакторе; для сохранения нажмите «Применить».
- Квадратная кнопка **EN/RU** в правом верхнем углу меняет язык настроек. После применения этот язык используется и в уведомлении. По умолчанию выбран английский; язык Minecraft не меняется.
- Выбор звука сразу воспроизводит образец. Для сохранения выбора нажмите «Применить».

С анимацией уведомление показывается заданное время с плавным появлением и исчезновением. Без анимации текущий день виден постоянно во время игры, а время показа не используется. При открытом экране или скрытом HUD блок не показывается. При входе число синхронизируется без звука: в постоянном режиме оно видно сразу, в анимированном уведомление ждёт следующего нового дня.

### Файл и значения

`config/daycounter.json` создаётся в папке игрового экземпляра при первом запуске клиента. Без Mod Menu закройте игру, отредактируйте файл и запустите её снова. Пример JSON и все 18 идентификаторов звуков приведены выше.

| Поле | По умолчанию | Значение |
| --- | --- | --- |
| `durationSeconds` | `8` | Время показа: 5–50 секунд. |
| `animationEnabled` | `true` | `true` — анимация; `false` — постоянный режим. |
| `scale` | `1` | Масштаб 0,5–2; блок дополнительно вписывается в окно. |
| `position` | `"TOP"` | `"TOP"` — сверху; `"HUD"` — над панелью быстрого доступа; `"MANUAL"` — вручную. |
| `xPercent`, `yPercent` | `50`, `24` | Координаты центра от 0 до 100% для ручного режима. X растёт вправо, Y — вниз. |
| `soundId` | `"minecraft:block.note_block.chime"` | Идентификатор из таблицы звуков выше. |
| `language` | `"EN"` | `"EN"` или `"RU"`, независимо от языка игры. |

Блок остаётся внутри экрана даже при координатах у края. Числа вне диапазона ограничиваются его границами; отсутствующие или неверные значения заменяются стандартными. В JSON дробные числа записываются через точку; в полях экрана настроек можно использовать и запятую.

При повреждённом JSON применяются стандартные настройки, а в журнал игры записывается предупреждение. Исходный файл сохраняется до явного нажатия «Применить». Файлы размером более 64 КиБ не загружаются. Если сохранение не удалось, прежние применённые настройки продолжают действовать.

### Счётчик мира

Число хранится отдельно от клиентских настроек. В **Minecraft 1.21.x** это `data/daycounter_days.dat` относительно папки мира. В **26.x** используется запись основного мира с идентификатором `daycounter:days`; во всех поддерживаемых **26.x** её путь относительно папки мира — `dimensions/minecraft/overworld/data/daycounter/days.dat`.

При открытии мира 1.21.x в 26.x старый счётчик импортируется один раз, если современных данных ещё нет. Исходный файл остаётся нетронутым; новый счётчик записывается при обычном сохранении мира. Существующие современные данные имеют приоритет. При ошибке чтения или недопустимом значении в журнал записывается сообщение, отсчёт начинается с дня 1, а исходник сохраняется. Перед обновлением сделайте резервную копию всего мира; при переносе включайте файлы счётчика. Сброс оформления не сбрасывает число дней.

Отсчёт начинается с дня 1. Естественные рассветы и успешный сон увеличивают число, команды времени сами по себе — нет. Дни до установки и время работы мира без мода не восстанавливаются.

Для одиночной игры достаточно одного JAR в клиенте. На удалённом сервере один и тот же JAR для точной версии Minecraft нужен на сервере и на клиентах, которым требуется уведомление. Без серверной части число скрыто; игроки без клиентского мода могут подключаться, но уведомления не видят.
