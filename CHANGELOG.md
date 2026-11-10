# Changelog

## 1.3.0 — 2026-11-10

- Fixed Mod Menu icon loading with a square PNG; enlarged the settings header icon and centered the EN/RU button label.
- Updated the mod icon. The new-day icon plays once over 2.5 seconds and holds the last frame until the notification fades.
- The persistent counter uses the static icon when animation is off.
- New and reset settings use 150% scale. Saved valid scales are kept. The supported range is 50–200%.
- Targets the same 16 Minecraft versions as 1.2.0. Previous JARs and checksums are stored in `dist/<game>/archive/<modversion>/`.

### Русский

- Исправлена загрузка иконки Mod Menu: используется квадратная PNG. Увеличен значок в заголовке настроек и отцентрирована надпись EN/RU.
- Обновлена иконка мода. Иконка нового дня проигрывает анимацию один раз за 2,5 секунды и остаётся на последнем кадре до исчезновения уведомления.
- При выключенной анимации постоянный счётчик показывает неподвижную иконку.
- В новых настройках и после сброса масштаб составляет 150%. Ранее сохранённые допустимые значения сохраняются. Диапазон составляет 50–200%.
- Поддерживаются те же 16 версий Minecraft, что и в 1.2.0. Предыдущие JAR и контрольные суммы хранятся в `dist/<версия игры>/archive/<версия мода>/`.

## 1.2.0 — 2026-11-10

- Added target-specific builds for 16 stable Minecraft versions: 1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2, and 26.3. Each `1.2.0+<game>` JAR works on both the client and server for its named version.
- Added a dependency matrix, per-target build output, and a PowerShell helper that prepares `dist/<game>/` with the mod JAR and SHA-256 checksum.
- Adapted server clocks, saved data, networking, rendering, and settings APIs during the build, while keeping shared source code and the existing day-counting behavior.
- Added one-time import of the 1.21.x world counter when opening an upgraded world in 26.x. Existing modern data takes precedence; the original legacy file is preserved.
- Kept independent EN/RU settings, optional Mod Menu integration, and JSON configuration without Mod Menu.
- Prepared 16 installable JARs, a SHA-256 manifest, and `daycounter-1.2.0-all-versions.zip` with per-game folders.
- Passed all 16 builds, 1,063 JUnit test executions, 32 server probes, 32 client probes, and all 16 JAR content/metadata audits. Probes initialize the release JAR through Fabric Knot and invoke mod entry points; they do not run the game loop. Full gameplay and FPS remain untested. See [compatibility](docs/compatibility.md) and [validation scope](docs/development.md#validation).

### Русский

- Добавлены отдельные сборки для 16 стабильных версий Minecraft: 1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 и 26.3. Каждый JAR `1.2.0+<версия игры>` подходит клиенту и серверу указанной версии.
- Добавлены таблица зависимостей, отдельные результаты сборки и PowerShell-скрипт подготовки `dist/<версия игры>/` с JAR и контрольной суммой SHA-256.
- Серверные часы, сохранение, сеть, отрисовка и настройки адаптируются при сборке. Общий исходный код и поведение счётчика сохранены.
- При открытии обновлённого мира в 26.x однократно импортируется счётчик 1.21.x. Уже существующие современные данные имеют приоритет; старый файл сохраняется.
- Сохранены независимый выбор EN/RU, необязательный Mod Menu и настройка через JSON без него.
- Подготовлены 16 установочных JAR, файл контрольных сумм SHA-256 и `daycounter-1.2.0-all-versions.zip` с папками версий.
- Прошли все 16 сборок, 1063 запуска JUnit-тестов, 32 серверные и 32 клиентские проверки, а также проверка содержимого и метаданных всех 16 JAR. Пробы инициализируют готовый JAR через Fabric Knot и вызывают точки входа мода без игрового цикла. Полноценная игра и FPS ещё не проверены. См. [совместимость](docs/compatibility.md#русский) и [границы проверок](docs/development.md#русский).

## 1.1.0+26.3 — 2026-11-04

- Added a compact **EN/RU** button in the top-right corner of settings.
- English is now the default language. The mod's language is saved separately from Minecraft's language and applies to settings and day notifications.
- Language changes follow the same **Apply / Cancel** behavior as other settings.
- The client configuration file is created on first launch, so settings can also be edited without Mod Menu.

### Русский

- В правом верхнем углу настроек добавлена компактная кнопка **EN/RU**.
- Английский язык установлен по умолчанию. Язык мода сохраняется независимо от языка Minecraft и используется в настройках и уведомлении дня.
- Для смены языка действуют кнопки **Применить / Отмена**, как и для остальных настроек.
- Файл клиентских настроек создаётся при первом запуске; его можно редактировать без Mod Menu.

## 1.0.0+26.3 — 2026-11-04

- Initial version for Minecraft 26.3 / Fabric, with one JAR for clients and servers.
- Added a persistent world counter starting at Day 1, advanced by natural dawns and successful sleep. Time commands do not directly add days.
- Added client synchronization, a compact notification, animated and persistent modes, configurable duration, scale and position, and 18 Minecraft sounds.
- Added Mod Menu settings, English and Russian translations, and configuration validation.

### Русский

- Первая версия для Minecraft 26.3 / Fabric с единым JAR для клиента и сервера.
- Добавлен сохраняемый счётчик мира с дня 1. Естественный рассвет и успешный сон увеличивают число; команды времени напрямую дни не добавляют.
- Добавлены синхронизация с клиентом, компактное уведомление, анимация и постоянный режим, настройка времени, размера и положения, а также 18 звуков Minecraft.
- Добавлены настройки через Mod Menu, английский и русский переводы и проверка конфигурации.
