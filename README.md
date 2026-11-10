# DayCounter

A compact new-day notification for **Minecraft / Fabric**, with a persistent world day counter. **DayCounter 1.3.0** supports 16 stable Minecraft releases: **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2, and 26.3**.

[Download](https://github.com/NEVER-See/DayCounter/releases) · [Compatibility](docs/compatibility.md) · [Configuration](docs/configuration.md) · [Development](docs/development.md) · [Changelog](CHANGELOG.md) · [Русский](#русский)

## Features

- A small day notification with the new mod icon, a Minecraft-style font, and a new-day sound.
- A new-day icon animation that plays once for 2.5 seconds. The last frame stays visible until the notification fades. Set the total notification duration to 5–50 seconds or use a persistent display with a static icon.
- Adjustable size and position, including manual coordinates. New and reset settings use 150% scale.
- 18 built-in Minecraft sounds with previews in settings.
- English by default; the square **EN/RU** button in settings changes the mod's language independently of Minecraft.
- A world-owned count starting at **Day 1** when tracking begins. Natural dawns and successful sleep advance it. Time commands themselves do not; the next natural dawn still counts. Earlier world history is not reconstructed.

## Install

Use **Java 21 for Minecraft 1.21.x** or **Java 25 for Minecraft 26.x**, a compatible stable **Fabric Loader** (the build uses **0.20.1**), and a Fabric API release compatible with your exact Minecraft version. The [compatibility table](docs/compatibility.md) lists the selected dependencies and validation status. See the [validation status](docs/compatibility.md#validation-status) for the 1.3.0 builds.

1. Choose the JAR for your **exact Minecraft version** from [Releases](https://github.com/NEVER-See/DayCounter/releases): `daycounter-1.3.0+<game>.jar`. For example, Minecraft 1.21.1 uses `daycounter-1.3.0+1.21.1.jar`.
2. Put that JAR and the matching Fabric API in your instance's `mods` folder. Install only one DayCounter JAR.
3. For a remote server, install the **same target-specific DayCounter JAR** and a compatible Fabric API on the server as well. Singleplayer already includes the integrated server side.

Each JAR supports its named Minecraft version and works on both the client and server. Release `v1.3.0` contains 18 assets: 16 separate JARs, `SHA256SUMS.txt`, and `daycounter-1.3.0-all-versions.zip`; see [release files](docs/compatibility.md#release-files).

The display requires the server's counter. On a remote server without DayCounter, no number is shown. Players without the client mod can still connect to a server that has it, but will not see the notification.

**Mod Menu is optional**; choose its version from the compatibility table and open **Mods → DayCounter → Configure**. **Apply** saves changes; **Cancel** discards unsaved changes. Without Mod Menu, close the game and edit `config/daycounter.json`, created on the first client launch. See the [configuration guide](docs/configuration.md), including world storage and migration.

Source and tests are available under the [MIT License](LICENSE). Instructions for building a chosen Minecraft target are in the [development guide](docs/development.md).

## Русский

DayCounter — компактное уведомление нового дня для **Minecraft / Fabric**. **Версия 1.3.0** поддерживает 16 стабильных выпусков **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 и 26.3**. Зависимости указаны в [таблице совместимости](docs/compatibility.md#русский), статус проверок 1.3.0 — в [разделе проверок](docs/compatibility.md#статус-проверок).

Счётчик хранится в мире и начинается с **дня 1** после начала отслеживания. Естественный рассвет и успешный сон увеличивают число; команды времени сами по себе — нет. Следующий естественный рассвет учитывается. История мира до установки не восстанавливается.

Иконка нового дня проигрывает анимацию один раз за 2,5 секунды и остаётся на последнем кадре до исчезновения уведомления. Можно настроить время показа от 5 до 50 секунд или включить постоянный счётчик с неподвижной иконкой. Доступны настройка размера и положения и выбор одного из 18 звуков Minecraft. В новых настройках и после сброса масштаб составляет 150%. По умолчанию используется английский язык. Квадратная кнопка **EN/RU** в настройках переключает язык мода независимо от языка игры. **Применить** сохраняет изменения; **Отмена** отменяет несохранённые.

Для установки нужны **Java 21 для Minecraft 1.21.x** или **Java 25 для Minecraft 26.x**, совместимый стабильный **Fabric Loader** (сборка использует **0.20.1**) и Fabric API, совместимый с точной версией игры. Скачайте из [Releases](https://github.com/NEVER-See/DayCounter/releases) соответствующий файл `daycounter-1.3.0+<версия игры>.jar` и поместите его вместе с Fabric API в `mods`. Например, для Minecraft 1.21.1 нужен `daycounter-1.3.0+1.21.1.jar`. Устанавливайте только один JAR DayCounter.

Для каждой версии Minecraft выпускается отдельный JAR, общий для клиента и сервера. На удалённом сервере используйте тот же JAR, что и на клиенте; для одиночной игры достаточно установки в клиент. Без серверной части число не отображается. Набор `v1.3.0` содержит 18 файлов: 16 JAR, `SHA256SUMS.txt` и `daycounter-1.3.0-all-versions.zip`.

**Mod Menu необязателен**: совместимая версия открывает экран настроек. Без него закройте игру и измените `config/daycounter.json`, который создаётся при первом запуске клиента. Подробнее: [настройка и данные мира](docs/configuration.md#русский), [сборка и проверки](docs/development.md#русский), [изменения](CHANGELOG.md). Исходники и тесты опубликованы под [лицензией MIT](LICENSE).
