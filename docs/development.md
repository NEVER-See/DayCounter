# Development

[README](../README.md) · [Compatibility](compatibility.md) · [Configuration](configuration.md) · [Русский](#русский)

## Build one Minecraft target

Use **JDK 25 to build every target**. The wrapper supplies Gradle; a separate Gradle installation is unnecessary. Set `JAVA_HOME` to the JDK if necessary. Runtime Java is separate: the 1.21.x artifacts target Java 21, while the 26.x artifacts target Java 25.

From the repository root:

```sh
./gradlew build -PminecraftVersion=1.21.1
```

On Windows PowerShell:

```powershell
.\gradlew.bat build -PminecraftVersion=1.21.1
```

Replace `1.21.1` with an exact version from [gradle/targets.json](../gradle/targets.json). Omitting `-PminecraftVersion` selects **26.3**. Unsupported targets fail early. The first build downloads Gradle and dependencies.

For a chosen `<game>`, output is:

- `build/<game>/libs/daycounter-1.4.0+<game>.jar` — installable client/server mod.
- `build/<game>/libs/daycounter-1.4.0+<game>-sources.jar` — source archive; not for installation.
- `build/<game>/reports/tests/test/index.html` — test report.

For example, the command above produces `build/1.21.1/libs/daycounter-1.4.0+1.21.1.jar`.

| Task | Purpose |
| --- | --- |
| `./gradlew test -PminecraftVersion=1.21.1` | Run the target's JUnit tests. |
| `./gradlew build -PminecraftVersion=1.21.1` | Compile, test, and package its mod and source archive. |
| `./gradlew runClient -PminecraftVersion=1.21.1` | Start a client in `run/1.21.1/client`. |
| `./gradlew runServer -PminecraftVersion=1.21.1` | Start a server in `run/1.21.1/server`. |

Use `gradlew.bat` on Windows. A development server requires the usual Minecraft EULA acceptance. Use separate test worlds. Mod Menu is compile-only: add the target's compatible Mod Menu JAR to `run/<game>/client/mods` to open settings in the development client.

## PowerShell helpers and release files

The optional helper runs the same wrapper with project-local caches by default:

```powershell
.\build.ps1 -MinecraftVersion '1.21.1'
.\build.ps1 -MinecraftVersion '26.3' -Tasks @('test')
```

`-JavaHome` selects a JDK 25 installation; `-CacheRoot` selects the Gradle cache and temporary-data directory instead of `.cache`. `-Offline` reuses already downloaded dependencies.

To build all 16 targets and prepare their installable JARs and SHA-256 checksums:

```powershell
.\build-matrix.ps1
```

To prepare a subset:

```powershell
.\build-matrix.ps1 -MinecraftVersions '1.21.1','26.3'
```

The matrix helper accepts the same `-JavaHome`, `-CacheRoot`, and `-Offline` options. It writes `dist/<game>/daycounter-1.4.0+<game>.jar` and `dist/<game>/SHA256SUMS.txt` for each completed target. The archive for a previous mod version is `dist/<game>/archive/<modversion>/`, with 1.2.0 and 1.3.0 JARs and checksums retained for all 16 targets. Local release files are listed in [release files](compatibility.md#release-files).

## Dependencies and compatibility code

[gradle/targets.json](../gradle/targets.json) defines each Minecraft version's runtime Java, minimum Loader, selected Fabric API, lower Fabric API validation endpoint, and optional Mod Menu version. Generated mod metadata requires `fabricloader >= loaderMin` and `fabric-api >= apiFirst` for the chosen target. [build.gradle](../build.gradle) pins the shared build dependencies:

| Component | Version |
| --- | --- |
| Build JDK | `25` |
| Fabric Loader used by the build | `0.20.1` |
| Fabric Loom | `1.19.3` |
| Gradle wrapper | `9.8.2` |
| JUnit | `5.14.1` |

The [compatibility table](compatibility.md) contains the per-target dependencies. For an explicit Fabric API validation build, select a compatible version from that table:

```sh
./gradlew build -PminecraftVersion=1.21.1 -PfabricApiVersion=0.102.0+1.21.1
```

The canonical Java sources use **Minecraft 26.3** APIs. During `generateCompatibility`, [gradle/server-compat.gradle](../gradle/server-compat.gradle), [gradle/client-compat.gradle](../gradle/client-compat.gradle), and `src/compat/` adapt verified API differences for the selected target. Generated sources are written to `build/<game>/generated/sources/`; edit the canonical source or compatibility definitions, not generated files.

Minecraft 1.21.x uses remapping with official Mojang mappings; 26.x uses the non-remapping build. Target selection happens at build time, so the shipped JAR has no runtime Minecraft-version dispatch overhead. Each JAR contains both common and client code for one exact Minecraft release. Dedicated servers load the common entry point without loading client rendering classes.

## Source layout

| Path | Responsibility |
| --- | --- |
| `src/main/java/dev/daycounter/DayCounter.java` | Common entry point and event registration. |
| `src/main/java/dev/daycounter/server/` | World day model, saved data, legacy migration, and announcements. |
| `src/main/java/dev/daycounter/mixin/` | Natural clock progression and successful sleep hooks. |
| `src/main/java/dev/daycounter/network/` | Day synchronization payload. |
| `src/main/java/dev/daycounter/config/` | Defaults, validation, file persistence, settings editor, and sounds. |
| `src/main/java/dev/daycounter/hud/` | Notification timing and layout without rendering. |
| `src/client/java/dev/daycounter/` | Client initialization, HUD rendering, and settings UI. |
| `src/main/resources/` | Metadata, mixin declarations, icons, and language resources. |
| `src/test/java/` | Shared behavior tests and migration tests. |
| `src/compat/` | Build-time adaptations and target-specific test fragments. |
| `gradle/` | Target matrix, compatibility transforms, and wrapper files. |

## Counting and runtime behavior

The world count begins at 1. Natural-clock hooks compare the Overworld clock before and after its normal tick; successful sleep transitions are handled separately. Time commands do not directly advance the counter. The next natural dawn still counts.

The server stores and synchronizes the count when a player joins and when a new day starts. Joining is silent. The client clears its notification state on connection changes. See [world data and migration](configuration.md#world-data-and-multiplayer) for storage and upgrades from 1.21.x.

The HUD uses Minecraft's font and sounds, elapsed time for animation, and cached labels/layout calculations. The four styles share one renderer with static previews in settings. Notification timing is captured when an event arrives; changing settings cannot revive an expired event. A newer day replaces an event that is still waiting. The client tick consumes the pending sound once when its delay ends. The new-day icon uses 20 prepared frames with alternating 130 ms and 120 ms delays. The client plays them once, then holds the last frame through the fade. The persistent display uses the static PNG while animating new day numbers. Backgrounds use small sets of pixel rectangles. Plaque engraving follows a cached pixel path through the sun, then the digits; completed rows use cropped texture draws with a short hot trail. Three small mask textures are reused and rebuilt only when their label, size, or resources change. Magic uses clipped native glyph/texture passes. The scroll colour sequence is selected once per event, never per frame. Dedicated servers skip these graphics resources. It adds no timer threads, hardware polling, or external service requests; synchronization uses the game connection. The 1.21.1 HUD batches its small shapes; later versions already buffer these operations. Icon textures load at client startup. Text metrics refresh on resource reload or font-option changes rather than every frame. CPU rendering measurements are described below; no particular in-world FPS gain is claimed.

The plaque digit mask reads Minecraft’s ASCII font atlas, including texture replacements from resource packs. TTF and Unihex replacements do not alter this mask. The game font itself is not bundled with the mod.

## Validation

Version **1.4.0** passed all **16 builds** and **1,463 JUnit test executions** across the target matrix. All 16 release JARs passed metadata, dependency, bytecode, resource and checksum checks. The final release JARs passed **32 client loader probes**: the selected and minimum Loader/API pairs for every target.

The common/server classes passed **32 server loader probes** before the final rendering correction. A byte-for-byte comparison confirmed those classes are unchanged in all 16 final JARs; these are retained server results, not 32 additional runs. The client probes were repeated for the final rendering code.

Separate native rendering checks covered **1.21.1 and 26.3**. The 26.3 run exercised all four styles, number transitions, long counts, resource reloads, font options and the EN/RU settings screen. The 1.21.1 run exercised seven rendering scenarios, including plaque transitions. Controlled 1.21.1 measurements confirmed reduced CPU drawing overhead from batching. These isolated checks did not measure world gameplay FPS or GPU frame time and do not replace full multiplayer gameplay testing.

The automated loader probes initialize production Fabric Knot with the built JAR and invoke entry points. They check client/server class separation, default configuration, translations and the optional Mod Menu factory. They do not start a world or establish visual or FPS correctness. A separate isolated UI run is required for rendering and input checks. Tests cover counting, persistence, migration, configuration, notification timing, delayed sound and layout boundaries.

## Русский

### Сборка одной версии

Для сборки **всех вариантов нужен JDK 25**. Для запуска готового мода используется **Java 21 в Minecraft 1.21.x** или **Java 25 в 26.x**. Gradle устанавливать отдельно не нужно; при необходимости укажите JDK в `JAVA_HOME`.

Из корня репозитория выполните:

```sh
./gradlew build -PminecraftVersion=1.21.1
```

В Windows PowerShell:

```powershell
.\gradlew.bat build -PminecraftVersion=1.21.1
```

Выберите точную версию из [gradle/targets.json](../gradle/targets.json). По умолчанию собирается **26.3**; неподдерживаемая версия отклоняется. Первая сборка загружает зависимости.

Результат — `build/<версия игры>/libs/daycounter-1.4.0+<версия игры>.jar`; рядом создаётся архив `-sources.jar`. Для установки нужен обычный JAR. Отчёт тестов: `build/<версия игры>/reports/tests/test/index.html`.

Вместо `build` доступны `test`, `runClient` и `runServer` с тем же `-PminecraftVersion`. Тестовые экземпляры создаются в `run/<версия игры>/client` и `run/<версия игры>/server`. Сервер требует принятия Minecraft EULA. Для экрана настроек добавьте совместимый Mod Menu в `run/<версия игры>/client/mods`.

### PowerShell и подготовка релиза

```powershell
.\build.ps1 -MinecraftVersion '1.21.1'
.\build-matrix.ps1
.\build-matrix.ps1 -MinecraftVersions '1.21.1','26.3'
```

Первая команда собирает одну версию, вторая — все 16, третья — выбранные версии. `build.ps1` принимает `-Tasks`, например `@('test')`. Общие параметры: `-JavaHome` для JDK 25, `-CacheRoot` для кэша и временных данных вместо `.cache`, `-Offline` для работы с уже загруженными зависимостями.

`build-matrix.ps1` сохраняет каждый готовый мод в `dist/<версия игры>/daycounter-1.4.0+<версия игры>.jar` и создаёт рядом `SHA256SUMS.txt`. Архив предыдущей версии мода хранится в `dist/<версия игры>/archive/<версия мода>/`. Копии JAR и контрольных сумм 1.2.0 и 1.3.0 сохранены для всех 16 версий игры. Локальные файлы описаны в разделе [файлов релиза](compatibility.md#русский).

### Зависимости и устройство

[Таблица совместимости](compatibility.md#русский) формируется по [gradle/targets.json](../gradle/targets.json), включая минимальные версии Loader и API. Метаданные JAR требуют `fabricloader >= loaderMin` и `fabric-api >= apiFirst` для выбранной игры. Общие версии сборки: Fabric Loader 0.20.1, Loom 1.19.3, Gradle 9.8.2 и JUnit 5.14.1. Mod Menu необязателен и выбирается отдельно для каждой версии игры. Для проверки другой совместимой Fabric API добавьте, например, `-PfabricApiVersion=0.102.0+1.21.1` к сборке Minecraft 1.21.1.

Общие исходники ориентированы на **26.3**. `gradle/server-compat.gradle`, `gradle/client-compat.gradle` и `src/compat/` адаптируют различия API во время сборки. Результаты в `build/<версия игры>/generated/sources/` создаются заново; изменяйте основной код или правила адаптации. Для 1.21.x выполняется remapping по официальным Mojang mappings; для 26.x используется сборка без remapping. Выбор версии не добавляет переключений во время работы игры.

Общая логика находится в `src/main/java/dev/daycounter/`, клиентская — в `src/client/java/dev/daycounter/`, ресурсы — в `src/main/resources/`, тесты — в `src/test/java/`. Один JAR содержит обе части для конкретной версии Minecraft; выделенный сервер не загружает клиентскую отрисовку.

Счётчик хранится в мире и синхронизируется при входе и наступлении нового дня. Естественный ход времени и сон обрабатываются отдельно от команд. Подробности хранения и переноса — в [руководстве по настройке](configuration.md#русский).

Анимация иконки нового дня использует 20 подготовленных кадров длительностью 130 и 120 мс по очереди. Клиент проигрывает их один раз и удерживает последний кадр до исчезновения уведомления. В постоянном режиме отображается неподвижная PNG-иконка, а смена числа анимируется. Четыре вида используют общий рендерер и его статичные образцы в настройках. Фоны рисуются пиксельными прямоугольниками. Выжигание проходит по сохранённому пути пикселей солнца и цифр: готовые строки выводятся частями текстуры, а короткий раскалённый след — отдельно. Три небольшие маски переиспользуются и обновляются только при изменении числа, размера или ресурсов. Магия использует ограниченные проходы игровых символов и текстур. Цветовая последовательность свитка выбирается один раз на событие. Время события фиксируется при получении; изменение настроек не возвращает завершённый показ. Новый день заменяет ожидающее уведомление, звук выдаётся один раз после задержки. Выделенный сервер не загружает эти графические ресурсы.

В 1.21.1 мелкие фигуры HUD группируются; более поздние версии уже буферизуют такие операции. Текстуры иконки загружаются при старте клиента. Размеры текста пересчитываются после смены ресурсов или параметров шрифта, а не каждый кадр. Мод использует игровые шрифты, звуки и подключение; дополнительных потоков таймера, опроса оборудования и внешних запросов нет.

Маска цифр таблички берётся из ASCII-атласа Minecraft и учитывает его замену ресурс-паком. TTF и Unihex не меняют эту маску. Сам шрифт игры в мод не включён.

### Проверки и ограничения

Версия **1.4.0** прошла **16 сборок** и **1463 выполнения JUnit-тестов** по всей матрице. Для всех 16 JAR проверены метаданные, зависимости, байткод, ресурсы и контрольные суммы. Финальные JAR прошли **32 клиентские проверки загрузки**: выбранные и минимальные пары Loader/API для каждой цели.

Общие/серверные классы прошли **32 серверные проверки загрузки** до последней правки отрисовки. Побайтовое сравнение подтвердило, что эти классы не изменились во всех 16 финальных JAR. Это сохранённые серверные результаты, а не 32 дополнительных запуска. Клиентские проверки повторены для финального кода отрисовки.

Отдельные нативные проверки графики выполнены на **1.21.1 и 26.3**. На 26.3 проверены четыре вида, смена числа, длинные значения, перезагрузка ресурсов, параметры шрифта и настройки EN/RU. На 1.21.1 проверены семь сценариев отрисовки, включая переходы таблички. Контролируемые измерения 1.21.1 подтвердили уменьшение затрат CPU благодаря группировке отрисовки. Эти изолированные проверки не измеряют FPS в мире или время GPU и не заменяют полноценную сетевую игру.

Автоматические пробы запускают production Fabric Knot с собранным JAR и вызывают точки входа. Они проверяют разделение клиентской/серверной части, стандартные настройки, переводы и необязательную фабрику Mod Menu. Мир не запускается, отрисовка и FPS такими пробами не проверяются. Для графики и ввода нужен отдельный запуск интерфейса. Тесты проверяют счётчик, сохранение, миграцию, настройки, задержки, звук и расположение.
