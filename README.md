# DayCounter

A compact new-day notification for **Minecraft 26.3 / Fabric** with a persistent world day counter.

[Download](https://github.com/NEVER-See/DayCounter/releases) · [Configuration](docs/configuration.md) · [Development](docs/development.md) · [Changelog](CHANGELOG.md) · [Русский](#русский)

## Features

- A small day notification with a Minecraft-style font and a new-day sound.
- Animated notifications lasting 5–50 seconds, or a persistent display.
- Adjustable size and position, including manual coordinates.
- 18 built-in Minecraft sounds with previews in settings.
- English by default; a small **EN/RU** button in the top-right corner of settings changes the mod's language independently of Minecraft.
- A world-owned count that starts at **Day 1** when the mod begins tracking that world. Natural dawns and successful sleep advance it. Time commands themselves do not; the next natural dawn still counts. Earlier world history is not reconstructed.

## Install

Use **Java 25**, Minecraft **26.3**, Fabric Loader **0.20.1 or newer**, and Fabric API **0.165.0+26.3**. **Mod Menu 21.0.0** is optional and opens the settings screen.

1. Download `daycounter-1.1.0+26.3.jar` from [Releases](https://github.com/NEVER-See/DayCounter/releases).
2. Put it and Fabric API in your Minecraft instance's `mods` folder.
3. For a remote server, install the **same DayCounter JAR** and Fabric API on the server as well. The singleplayer installation already includes the integrated server side.

The day display requires the server's counter. On a remote server without DayCounter, no number is shown. Players without the client mod can still connect to a server that has it, but will not see the notification.

With Mod Menu, open **Mods → DayCounter → Configure**. **Apply** saves changes; **Cancel** discards unsaved changes. Without Mod Menu, close the game and edit `config/daycounter.json`, created on the first client launch. See the [configuration guide](docs/configuration.md).

The full source and tests are available in this repository under the [MIT License](LICENSE). Build instructions and the scope of validation are in the [development guide](docs/development.md).

## Русский

DayCounter — компактное уведомление нового дня для **Minecraft 26.3 / Fabric**. Счётчик хранится в мире и начинается с **дня 1** после начала отслеживания. Естественный рассвет и успешный сон увеличивают число; команды времени сами по себе — нет. Следующий естественный рассвет учитывается. История мира до установки не восстанавливается.

Можно выбрать анимацию на 5–50 секунд или постоянное отображение, размер, положение и один из 18 звуков Minecraft. По умолчанию используется английский язык. Квадратная кнопка **EN/RU** в правом верхнем углу настроек переключает язык мода независимо от языка игры. **Применить** сохраняет изменения; **Отмена** отменяет несохранённые.

Для установки нужны **Java 25**, **Minecraft 26.3**, **Fabric Loader ≥ 0.20.1** и **Fabric API 0.165.0+26.3**. Скачайте `daycounter-1.1.0+26.3.jar` из [Releases](https://github.com/NEVER-See/DayCounter/releases) и поместите его вместе с Fabric API в папку `mods`. Для удалённого сервера тот же JAR нужен и на клиенте, и на сервере. Для одиночной игры достаточно установки в клиент. Без серверной части число не отображается.

**Mod Menu 21.0.0** необязателен: он открывает экран настроек. Без него закройте игру и измените `config/daycounter.json`, который создаётся при первом запуске клиента. Подробнее: [настройка](docs/configuration.md#русский), [сборка и проверки](docs/development.md#русский), [изменения](CHANGELOG.md). Исходники и тесты опубликованы под [лицензией MIT](LICENSE).
