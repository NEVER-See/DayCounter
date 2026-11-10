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

- `build/<game>/libs/daycounter-1.3.0+<game>.jar` — installable client/server mod.
- `build/<game>/libs/daycounter-1.3.0+<game>-sources.jar` — source archive; not for installation.
- `build/<game>/reports/tests/test/index.html` — test report.

For example, the command above produces `build/1.21.1/libs/daycounter-1.3.0+1.21.1.jar`.

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

The matrix helper accepts the same `-JavaHome`, `-CacheRoot`, and `-Offline` options. It writes `dist/<game>/daycounter-1.3.0+<game>.jar` and `dist/<game>/SHA256SUMS.txt` for each completed target. The archive for a previous mod version is `dist/<game>/archive/<modversion>/`, with copies of all 16 targets’ 1.2.0 JARs and checksums already stored under `archive/1.2.0/`. The 1.3.0 release is listed in [release files](compatibility.md#release-files).

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

The HUD uses Minecraft's font and sounds, elapsed time for animation, and cached layout calculations. The new-day icon uses 20 prepared frames with alternating 130 ms and 120 ms delays. The client plays them once, then holds the last frame through the fade. The persistent display uses the static PNG. Dedicated servers skip these graphics resources. It adds no timer threads, hardware polling, or external service requests; synchronization uses the game connection. Performance has not been measured, so no particular FPS impact is claimed.

## Validation

Local validation for **1.3.0** on **2026-11-10**, after the mod-list icon and settings-header corrections. The pre-release corrections kept the 1.3.0 version number.

| Check | Result |
| --- | --- |
| Gradle builds | **16/16 passed**. |
| JUnit tests | **1,175 executions**, with 71–77 per target and zero failures, errors, or skipped tests. |
| Mod-list icon | The new `verifyModIcon` task rejected the previous 304×312 image and accepted the padded 312×312 PNG. It runs before resource processing and as part of `check`. |
| Fabric Knot CLIENT probes | **2/2 passed** on 1.21.1 and 26.3 with Loader 0.20.1, selected latest API, and Mod Menu. |
| Release JAR audits | **16/16 passed**, including metadata, Java class versions, the square icon, and prepared frames. JARs are 68,369–71,363 bytes. |
| Archive comparison | For all 16 JARs, only the icon and the two settings UI classes differ from the preceding local 1.3.0 build. |

The preceding local 1.3.0 build passed **32 SERVER and 32 CLIENT probes**, covering minimum Loader + minimum API and Loader 0.20.1 + selected latest API for all targets. Those results apply to that archived build. The full probe matrix was not repeated after this correction; the current client checks cover the oldest and newest targets. Server code and all other JAR entries are byte-identical to the preceding build. Runtime probes use **Java 21 for 1.21.x** and **Java 25 for 26.x**. SHA-256 checks match the current audited JARs to the local files in `dist/` and the aggregate ZIP.

Each probe initializes production **Fabric Knot in SERVER or CLIENT mode with the actual release JAR**, then explicitly invokes mod entry points. The earlier server probes checked hooks, saved-data persistence/reload, and legacy migration for 26.x. Client probes check initialization, the 150% default, both translations, and the Mod Menu settings-screen factory where Mod Menu is installed. Mod Menu can require newer dependencies than DayCounter.

The tests cover day transitions, saved-data serialization and migration, payload handling, configuration, Apply/Cancel behavior, notification timing, icon frame selection and restart, default and saved scale, duplicate updates, and layout boundaries. Run `build` for each changed target before distributing it; reports are at `build/<game>/reports/tests/test/index.html`.

The maintainer reported a manual in-game check on **Minecraft 26.3** on **2026-11-10**. That report does not establish manual coverage of the other Minecraft targets or every gameplay scenario.

**These automated checks did not invoke `Minecraft.main`, run the game loop, open a world or window, render the HUD, exercise input, play sounds, or measure FPS.** Full gameplay validation remains outstanding. It should cover fresh/upgraded worlds, dawn, sleep, time commands, reconnecting, world save/restart, multiplayer, Apply/Cancel/Reset, both mod languages, sound playback, and small windows. Input-method positioning in scaled numeric fields remains a known limitation.

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

Результат — `build/<версия игры>/libs/daycounter-1.3.0+<версия игры>.jar`; рядом создаётся архив `-sources.jar`. Для установки нужен обычный JAR. Отчёт тестов: `build/<версия игры>/reports/tests/test/index.html`.

Вместо `build` доступны `test`, `runClient` и `runServer` с тем же `-PminecraftVersion`. Тестовые экземпляры создаются в `run/<версия игры>/client` и `run/<версия игры>/server`. Сервер требует принятия Minecraft EULA. Для экрана настроек добавьте совместимый Mod Menu в `run/<версия игры>/client/mods`.

### PowerShell и подготовка релиза

```powershell
.\build.ps1 -MinecraftVersion '1.21.1'
.\build-matrix.ps1
.\build-matrix.ps1 -MinecraftVersions '1.21.1','26.3'
```

Первая команда собирает одну версию, вторая — все 16, третья — выбранные версии. `build.ps1` принимает `-Tasks`, например `@('test')`. Общие параметры: `-JavaHome` для JDK 25, `-CacheRoot` для кэша и временных данных вместо `.cache`, `-Offline` для работы с уже загруженными зависимостями.

`build-matrix.ps1` сохраняет каждый готовый мод в `dist/<версия игры>/daycounter-1.3.0+<версия игры>.jar` и создаёт рядом `SHA256SUMS.txt`. Архив предыдущей версии мода хранится в `dist/<версия игры>/archive/<версия мода>/`. Копии JAR и контрольных сумм 1.2.0 для всех 16 версий игры уже сохранены в `archive/1.2.0/`. Релиз 1.3.0 описан в разделе [файлов релиза](compatibility.md#русский).

### Зависимости и устройство

[Таблица совместимости](compatibility.md#русский) формируется по [gradle/targets.json](../gradle/targets.json), включая минимальные версии Loader и API. Метаданные JAR требуют `fabricloader >= loaderMin` и `fabric-api >= apiFirst` для выбранной игры. Общие версии сборки: Fabric Loader 0.20.1, Loom 1.19.3, Gradle 9.8.2 и JUnit 5.14.1. Mod Menu необязателен и выбирается отдельно для каждой версии игры. Для проверки другой совместимой Fabric API добавьте, например, `-PfabricApiVersion=0.102.0+1.21.1` к сборке Minecraft 1.21.1.

Общие исходники ориентированы на **26.3**. `gradle/server-compat.gradle`, `gradle/client-compat.gradle` и `src/compat/` адаптируют различия API во время сборки. Результаты в `build/<версия игры>/generated/sources/` создаются заново; изменяйте основной код или правила адаптации. Для 1.21.x выполняется remapping по официальным Mojang mappings; для 26.x используется сборка без remapping. Выбор версии не добавляет переключений во время работы игры.

Общая логика находится в `src/main/java/dev/daycounter/`, клиентская — в `src/client/java/dev/daycounter/`, ресурсы — в `src/main/resources/`, тесты — в `src/test/java/`. Один JAR содержит обе части для конкретной версии Minecraft; выделенный сервер не загружает клиентскую отрисовку.

Счётчик хранится в мире и синхронизируется при входе и наступлении нового дня. Естественный ход времени и сон обрабатываются отдельно от команд. Подробности хранения и переноса — в [руководстве по настройке](configuration.md#русский).

Анимация иконки нового дня использует 20 подготовленных кадров длительностью 130 и 120 мс по очереди. Клиент проигрывает их один раз и удерживает последний кадр до исчезновения уведомления. В постоянном режиме отображается неподвижная PNG-иконка. Выделенный сервер не загружает эти графические ресурсы.

Мод использует игровые шрифты, звуки и подключение; дополнительных потоков таймера, опроса оборудования и внешних запросов нет.

### Проверки и ограничения

Локальные проверки **1.3.0** от **2026-11-10**, после исправления иконки списка модов и заголовка настроек. Исправления до публикации вошли в тот же номер версии 1.3.0.

| Проверка | Результат |
| --- | --- |
| Сборки Gradle | **16 из 16 прошли**. |
| JUnit | **1175 запусков**, по 71–77 на версию, без сбоев, ошибок и пропусков. |
| Иконка списка модов | Задача `verifyModIcon` отклонила прежнюю иконку 304×312 и приняла PNG 312×312 с прозрачными боковыми полями. Она выполняется перед обработкой ресурсов и в составе `check`. |
| Fabric Knot CLIENT | **2 из 2 прошли** для 1.21.1 и 26.3 с Loader 0.20.1, последней выбранной API и Mod Menu. |
| Готовые JAR | **16 из 16 прошли** проверку метаданных, версий Java-классов, квадратной иконки и подготовленных кадров. Размер JAR — 68 369–71 363 байт. |
| Сравнение с архивом | В каждом из 16 JAR изменились только иконка и два класса интерфейса настроек. |

Предыдущий локальный вариант 1.3.0 прошёл **32 серверные и 32 клиентские пробы** для минимальных и последних выбранных пар Loader/API всех целей. Эти результаты относятся к архивному варианту. После текущей правки полная матрица проб не повторялась: свежая клиентская проверка охватила самую старую и новую цели. Серверный код и остальные записи JAR побайтово совпадают с предыдущей сборкой. Пробы выполняются на **Java 21 для 1.21.x** и **Java 25 для 26.x**. Контрольные суммы SHA-256 подтвердили соответствие текущих проверенных JAR файлам в `dist/` и общем ZIP.

Пробы инициализируют **Fabric Knot в режиме SERVER или CLIENT с настоящим релизным JAR**, затем явно вызывают точки входа мода. Предыдущие серверные пробы проверяли hooks, сохранение и повторную загрузку данных, а для 26.x — перенос старого счётчика. Клиентские пробы проверяют инициализацию, масштаб 150% по умолчанию, оба перевода и фабрику экрана настроек при установленном Mod Menu. Mod Menu может требовать более новые зависимости.

Тесты охватывают счётчик, сохранение и миграцию, сеть, настройки, применение и отмену, время уведомления, выбор и перезапуск кадров анимации, масштаб по умолчанию и сохранённый масштаб, а также расположение. Перед распространением изменённого JAR выполните `build` для соответствующих версий; отчёт находится в `build/<версия игры>/reports/tests/test/index.html`.

Разработчик сообщил о ручной проверке в игре на **Minecraft 26.3** от **2026-11-10**. Это не подтверждает ручную проверку остальных версий Minecraft или каждого игрового сценария.

**Эти автоматические проверки не вызывали `Minecraft.main`, не запускали игровой цикл, мир или окно, не проверяли отрисовку HUD, ввод, воспроизведение звука и FPS.** Полноценная игровая проверка ещё предстоит: новые и перенесённые миры, рассвет, сон, команды времени, переподключение, сохранение и перезапуск мира, сеть, применение/отмена/сброс настроек, языки, звуки и небольшие окна. Известно ограничение расположения IME в масштабированных числовых полях.
