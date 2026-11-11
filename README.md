# DayCounter

A compact new-day notification for **Minecraft / Fabric**, with a persistent world day counter. DayCounter supports 16 stable Minecraft releases: **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2, and 26.3**.

[Download](https://github.com/NEVER-See/DayCounter/releases) · [Compatibility](docs/compatibility.md) · [Configuration](docs/configuration.md) · [Development](docs/development.md) · [Changelog](CHANGELOG.md) · [Русский](#русский)

## Features

- Four compact styles: **Standard**, **Cloud**, **Plaque**, and **Scroll**, using Minecraft's font and the mod's sun icon.
- A configurable **1–20 second delay** before showing a new day (default **1**); notification duration **5–50 seconds** (default **8**).
- A persistent display mode with an animated number change: rolling digits, fading, engraving, or a magical sweep.
- Independent gentle floating, three separately moving clouds, and a 20-colour magic effect for the scroll.
- Adjustable **50–400% scale** (default **150%**), preset or manual positioning, and automatic fitting for long counts and small windows.
- 18 built-in Minecraft sounds with previews in settings.
- English by default; the square **EN/RU** button in settings changes the mod's language independently of Minecraft.
- A world-owned count starting at **Day 1** when tracking begins. Natural dawns and successful sleep advance it. Time commands themselves do not; the next natural dawn still counts. Earlier world history is not reconstructed.

## Install

Use **Java 21 for Minecraft 1.21.x** or **Java 25 for Minecraft 26.x**, a compatible stable **Fabric Loader** (the build uses **0.20.1**), and a Fabric API release compatible with your exact Minecraft version. The [compatibility table](docs/compatibility.md) lists the selected dependencies and [validation status](docs/compatibility.md#validation-status).

1. Choose the JAR for your **exact Minecraft version** from [Releases](https://github.com/NEVER-See/DayCounter/releases): `daycounter-<mod version>+<game>.jar`. The part after `+` must match your Minecraft version.
2. Put that JAR and the matching Fabric API in your instance's `mods` folder. Install only one DayCounter JAR.
3. For a remote server, install the **same target-specific DayCounter JAR** and a compatible Fabric API on the server as well. Singleplayer already includes the integrated server side.

Each JAR supports its named Minecraft version and works on both the client and server. Multi-version bundles keep each game version in a separate folder; see [release files](docs/compatibility.md#release-files).

The display requires the server's counter. On a remote server without DayCounter, no number is shown. Players without the client mod can still connect to a server that has it, but will not see the notification.

**Mod Menu is optional**; choose its version from the compatibility table and open **Mods → DayCounter → Configure**. **Apply** saves changes; **Cancel** discards unsaved changes. Without Mod Menu, close the game and edit `config/daycounter.json`, created on the first client launch. See the [configuration guide](docs/configuration.md), including world storage and migration.

Source and tests are available under the [MIT License](LICENSE). Instructions for building a chosen Minecraft target are in the [development guide](docs/development.md).

## Русский

DayCounter — компактное уведомление нового дня для **Minecraft / Fabric**. Мод поддерживает 16 стабильных выпусков **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 и 26.3**. Зависимости указаны в [таблице совместимости](docs/compatibility.md#русский), статус проверок — в [разделе проверок](docs/compatibility.md#статус-проверок).

Счётчик хранится в мире и начинается с **дня 1** после начала отслеживания. Естественный рассвет и успешный сон увеличивают число; команды времени сами по себе — нет. Следующий естественный рассвет учитывается. История мира до установки не восстанавливается.

Доступны четыре вида: **Стандартный**, **Облачный**, **Табличка** и **Свиток**. Перед появлением нового дня можно задать задержку **1–20 секунд** (по умолчанию **1**), а время показа — **5–50 секунд** (по умолчанию **8**). В постоянном режиме анимируется только смена числа: прокрутка, прозрачность, выжигание или магическая линия. Подвижность включается отдельно; облака движутся независимо, а свиток использует палитру из 20 цветов.

Масштаб регулируется от **50 до 400%**, по умолчанию **150%**. Есть готовые положения и ручные координаты; длинные числа и весь блок автоматически вписываются в окно. Можно выбрать один из **18 звуков Minecraft**. Квадратная кнопка **EN/RU** переключает язык независимо от Minecraft; по умолчанию выбран английский. **Применить** сохраняет изменения, **Отмена** отменяет несохранённые.

Для установки нужны **Java 21 для Minecraft 1.21.x** или **Java 25 для Minecraft 26.x**, совместимый стабильный **Fabric Loader** (сборка использует **0.20.1**) и Fabric API, совместимый с точной версией игры. Скачайте из [Releases](https://github.com/NEVER-See/DayCounter/releases) соответствующий файл `daycounter-<версия мода>+<версия игры>.jar` и поместите его вместе с Fabric API в `mods`. Часть имени после `+` должна совпадать с версией Minecraft. Устанавливайте только один JAR DayCounter.

Для каждой версии Minecraft выпускается отдельный JAR, общий для клиента и сервера. На удалённом сервере используйте тот же JAR, что и на клиенте; для одиночной игры достаточно установки в клиент. Без серверной части число не отображается. В общем архиве разные версии игры разложены по отдельным папкам.

**Mod Menu необязателен**: совместимая версия открывает экран настроек. Без него закройте игру и измените `config/daycounter.json`, который создаётся при первом запуске клиента. Подробнее: [настройка и данные мира](docs/configuration.md#русский), [сборка и проверки](docs/development.md#русский), [изменения](CHANGELOG.md). Исходники и тесты опубликованы под [лицензией MIT](LICENSE).
