# Development

[README](../README.md) · [Configuration](configuration.md) · [Русский](#русский)

## Build

Use a **Java 25 JDK**. The repository includes the Gradle wrapper; a separate Gradle installation is unnecessary. Set `JAVA_HOME` to the JDK if your default Java version differs.

From the repository root, run:

```sh
./gradlew build
```

On Windows PowerShell:

```powershell
.\gradlew.bat build
```

The first build downloads Gradle and the declared dependencies. The resulting universal mod JAR is `build/libs/daycounter-1.1.0+26.3.jar`; `build/libs/daycounter-1.1.0+26.3-sources.jar` contains the source. Install the regular JAR in Minecraft, not the sources JAR.

Useful tasks:

| Task | Purpose |
| --- | --- |
| `./gradlew test` | Run the JUnit tests. |
| `./gradlew build` | Compile, test, and package the mod and source archive. |
| `./gradlew runClient` | Start a development client in `run/client`. |
| `./gradlew runServer` | Start a development server in `run/server`. |

Use `gradlew.bat` instead of `./gradlew` on Windows. A development server requires the usual Minecraft EULA acceptance. Use separate test worlds for gameplay validation.

Build versions are declared in `build.gradle` and `gradle/wrapper/gradle-wrapper.properties`:

| Component | Version |
| --- | --- |
| Minecraft | `26.3` |
| Java | `25` |
| Fabric Loader | `0.20.1` |
| Fabric API | `0.165.0+26.3` |
| Fabric Loom | `1.19.3` |
| Gradle wrapper | `9.8.2` |
| Mod Menu, optional | `21.0.0` |
| JUnit | `5.14.1` |

Mod Menu is a compile-only integration. Add its compatible JAR to the development client's `run/client/mods` folder to open settings there.

## Source layout

| Path | Responsibility |
| --- | --- |
| `src/main/java/dev/daycounter/DayCounter.java` | Common entry point and event registration. |
| `src/main/java/dev/daycounter/server/` | World day model, saved data, and server announcements. |
| `src/main/java/dev/daycounter/mixin/` | Hooks for natural clock progression and successful sleep. |
| `src/main/java/dev/daycounter/network/` | Day synchronization payload. |
| `src/main/java/dev/daycounter/config/` | Defaults, validation, file persistence, editable settings, and sound catalog. |
| `src/main/java/dev/daycounter/hud/` | Notification timing and layout calculations without rendering. |
| `src/client/java/dev/daycounter/` | Client initialization, HUD rendering, and settings UI. |
| `src/main/resources/` | Mod metadata, mixin declarations, icons, and language resources. |
| `src/test/java/` | Tests for the day model, persistence, synchronization, settings, timing, and layout. |

The source and tests are public so the behavior can be inspected and reproduced. The common and client source sets are packaged into one JAR. Dedicated servers load the common entry point without loading client rendering classes.

## Counting and runtime behavior

The world counter begins at 1. The natural-clock hook compares the Overworld clock immediately before and after its normal tick, counting crossed dawn boundaries. The sleep hook handles successful transitions to the wake-up marker separately. Commands that change time occur outside these hooks and do not directly advance the counter. The next natural dawn still counts.

The server stores the count in Overworld saved data and sends it to compatible clients on joining and on a new day. Joining is a silent synchronization. The client clears its notification state when connecting or disconnecting, so a previous world's number is not carried across sessions.

The HUD draws a small panel using Minecraft's font and sound resources. It uses elapsed time for animation and caches layout calculations. There are no additional timer threads, hardware polling, or external service requests. Multiplayer synchronization uses the existing game connection. This describes the implementation, not a guarantee of a particular FPS impact.

## Validation

The test suite covers day-count transitions, saved-data serialization, payload handling, configuration parsing and validation, editor Apply/Cancel behavior, notification timing, duplicate updates, and layout boundaries. Run `build` against the current checkout before distributing a modified JAR. Test reports are generated at `build/reports/tests/test/index.html`.

Validation of version **1.1.0+26.3** completed a Gradle build and **64 tests** with no failures. Fabric loader probes loaded the common entry point on the server without client classes, loaded client entry points and UI classes on the client, and checked both server mixin transformations. These checks do not establish end-to-end gameplay behavior.

Real-world multiplayer, sleep, world save/restart, full in-game UI interaction, and performance measurements have not yet been completed. In particular, no FPS claim is made. Input-method positioning in scaled numeric fields remains a known limitation.

For gameplay validation, check a fresh world and an existing world, natural dawn, sleep, forward and backward time commands, reconnecting, saving and restarting, a server without the mod, and clients with and without the mod. Exercise Apply/Cancel/Reset, both mod languages while Minecraft uses a different language, sound selection, and small windows. Record the exact version and environment with the results.

## Русский

### Сборка

Нужен **JDK 25**. Gradle устанавливать отдельно не требуется: используйте включённый wrapper. При необходимости укажите JDK через `JAVA_HOME`.

Из корня репозитория выполните `./gradlew build`, а в Windows PowerShell — `.\gradlew.bat build`. Первая сборка загрузит Gradle и зависимости. Результат: `build/libs/daycounter-1.1.0+26.3.jar`; исходники: `build/libs/daycounter-1.1.0+26.3-sources.jar`. В игру устанавливается обычный JAR.

Задача `test` запускает тесты; `runClient` и `runServer` открывают тестовый клиент и сервер в `run/client` и `run/server`. Сервер требует стандартного принятия Minecraft EULA. Для экрана настроек в тестовом клиенте добавьте Mod Menu 21.0.0 в `run/client/mods`.

Версии зависимостей перечислены выше и закреплены в `build.gradle`; версия Gradle — в `gradle/wrapper/gradle-wrapper.properties`.

### Устройство

Общая часть в `src/main/java/dev/daycounter/` содержит счётчик, сохранение, сетевой пакет, настройки и расчёты уведомления. Клиентская часть в `src/client/java/dev/daycounter/` отвечает за отрисовку и экран настроек. Ресурсы находятся в `src/main/resources/`, тесты — в `src/test/java/`. Обе части упакованы в один JAR; выделенный сервер не загружает клиентскую отрисовку.

Счётчик хранится в мире и синхронизируется при входе и наступлении нового дня. Естественный ход времени и сон обрабатываются отдельно от команд времени. При входе синхронизация бесшумная; при смене подключения клиент сбрасывает прежнее уведомление.

Мод использует шрифт и звуки Minecraft, небольшую панель и кэш расчётов расположения. Дополнительных потоков таймера, опроса оборудования и запросов к внешним сервисам нет. Обмен между клиентом и сервером идёт через игровое подключение. Конкретное влияние на FPS не заявляется.

### Проверки и ограничения

Тесты проверяют счётчик, сериализацию, сетевой пакет, конфигурацию, применение и отмену настроек, время уведомления и границы расположения. Перед распространением изменённого JAR запускайте `build`; отчёт появится в `build/reports/tests/test/index.html`.

Для **1.1.0+26.3** прошли сборка и **64 теста** без ошибок. Проверены общий entrypoint на сервере без клиентских классов, клиентский entrypoint и классы интерфейса, преобразование обоих серверных mixin. Эти проверки не заменяют полный игровой тест. Полные проверки сети, сна, сохранения и перезапуска мира, работы всех элементов интерфейса и производительности ещё не завершены. Есть известное ограничение расположения IME в масштабированных числовых полях.

Для игровой проверки используйте отдельные миры и сервер. Проверьте рассвет, сон, команды времени, переподключение, сохранение и перезапуск, отсутствие мода на сервере или клиенте, применение/отмену/сброс настроек, оба языка, звуки и небольшие окна. Сохраняйте результаты вместе с точной версией и параметрами среды.
