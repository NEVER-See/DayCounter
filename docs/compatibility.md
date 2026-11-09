# Compatibility

[README](../README.md) · [Configuration](configuration.md) · [Development](development.md) · [Русский](#русский)

## Minecraft and dependencies

DayCounter **1.2.0** has a separate target for each of the 16 stable Minecraft versions below. Install only the JAR named for your exact version. The same JAR works on its client and server. Snapshots, pre-releases, release candidates, unlisted Minecraft versions, and future API releases are outside this matrix.

Use a compatible stable **Fabric Loader**, at least the minimum listed below; the selected latest Loader for this matrix and the build is **0.20.1**. Also meet the requirements of your chosen Fabric API and optional Mod Menu release. Install **Fabric API** on both the client and server. Choose an API release whose metadata supports your exact Minecraft version. Its version suffix need not be identical: for example, the selected `0.155.3+26.1.2` build is shared by the listed 26.1 targets.

The table reflects [gradle/targets.json](../gradle/targets.json). Each JAR declares the listed minimum Loader and Fabric API versions. Validation covered two pairs per Minecraft target: **minimum Loader + minimum API**, and **Loader 0.20.1 + selected latest API**. The latter is also the default build pair. These endpoints do not guarantee every intermediate or future release. **Mod Menu is optional** and is only needed to open the settings screen; JSON configuration remains available without it.

The listed Loader/API minimums apply to **DayCounter without Mod Menu**. Optional Mod Menu can require newer dependencies; check the metadata of its selected release. For Minecraft **26.2**, DayCounter with the minimum API requires **Loader 0.18.4**, while **Mod Menu 20.0.3 requires Loader 0.19.2 or newer**. For Minecraft **1.21.9 and 1.21.10**, DayCounter requires **Loader 0.17.0**, while **Mod Menu 16.0.1 requires Loader 0.17.2 or newer**. Installing Mod Menu does not change DayCounter's own minimum.

| Minecraft | Runtime Java | Minimum Fabric Loader | Minimum Fabric API | Selected latest Fabric API | Optional Mod Menu |
| --- | --- | --- | --- | --- | --- |
| 1.21.1 | 21 | `0.15.11` | `0.102.0+1.21.1` | `0.116.18+1.21.1` | `11.0.5` |
| 1.21.2 | 21 | `0.16.7` | `0.106.1+1.21.2` | `0.106.1+1.21.2` | `12.0.1` |
| 1.21.3 | 21 | `0.16.7` | `0.106.1+1.21.3` | `0.114.1+1.21.3` | `12.0.1` |
| 1.21.4 | 21 | `0.16.9` | `0.110.5+1.21.4` | `0.119.4+1.21.4` | `13.0.4` |
| 1.21.5 | 21 | `0.16.10` | `0.119.5+1.21.5` | `0.128.2+1.21.5` | `14.0.2` |
| 1.21.6 | 21 | `0.16.13` | `0.127.0+1.21.6` | `0.128.2+1.21.6` | `15.0.2` |
| 1.21.7 | 21 | `0.16.13` | `0.128.1+1.21.7` | `0.129.0+1.21.7` | `15.0.2` |
| 1.21.8 | 21 | `0.16.13` | `0.129.0+1.21.8` | `0.136.1+1.21.8` | `15.0.2` |
| 1.21.9 | 21 | `0.17.0` | `0.133.14+1.21.9` | `0.134.1+1.21.9` | `16.0.1` |
| 1.21.10 | 21 | `0.17.0` | `0.134.1+1.21.10` | `0.138.4+1.21.10` | `16.0.1` |
| 1.21.11 | 21 | `0.17.3` | `0.139.4+1.21.11` | `0.141.6+1.21.11` | `17.0.1` |
| 26.1 | 25 | `0.18.4` | `0.144.0+26.1` | `0.155.3+26.1.2` | `18.0.2` |
| 26.1.1 | 25 | `0.18.4` | `0.144.4+26.1` | `0.155.3+26.1.2` | `18.0.2` |
| 26.1.2 | 25 | `0.18.4` | `0.145.4+26.1.2` | `0.155.3+26.1.2` | `18.0.2` |
| 26.2 | 25 | `0.18.4` | `0.152.1+26.2` | `0.161.0+26.2` | `20.0.3` |
| 26.3 | 25 | `0.19.3` | `0.160.5+26.3` | `0.166.0+26.3` | `21.0.0` |

**Build all targets with JDK 25.** The runtime Java column applies to Minecraft and the distributed JAR, not the build tools.

## Validation status

**Build and loader validation for 1.2.0 passed on 2026-11-10:** all 16 builds; 1,063 JUnit test executions (64–70 per target, no failures, errors, or skipped tests); 32 server and 32 client probes across the minimum/latest pairs; and metadata, Java class-version, and content audits for all 16 JARs. Runtime probes used Java 21 for 1.21.x and Java 25 for 26.x. SHA-256 checks tied the results to the distributed files.

The probes initialized the actual release JAR through Fabric Knot in SERVER/CLIENT mode and then invoked mod entry points. All latest client probes included Mod Menu and its settings-screen factory; minimum client probes used configurations with or without Mod Menu. Server probes checked hooks, saved-data reload, and 26.x legacy migration. They did not start the game loop or open a world/window. Rendering, input, sounds, full gameplay, and FPS remain untested. See the detailed [validation scope](development.md#validation).

## Release files

For each `<game>`, the release-preparation helper creates:

```text
dist/<game>/
  daycounter-1.2.0+<game>.jar
  SHA256SUMS.txt
```

For example, Minecraft 1.21.1 uses `dist/1.21.1/daycounter-1.2.0+1.21.1.jar`. These are separate builds, not a single JAR spanning all 16 Minecraft versions.

GitHub release **`v1.2.0`** contains **18 assets**: 16 installable JARs, one aggregate `SHA256SUMS.txt`, and `daycounter-1.2.0-all-versions.zip`. The ZIP preserves the per-game folders; extract only the JAR for your Minecraft version into `mods`. Source archives are for developers and are not installed in the game.

To verify a downloaded JAR in PowerShell, compare this output with the checksum manifest:

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath '.\daycounter-1.2.0+1.21.1.jar'
```

## World upgrades

The client settings file and independent EN/RU language choice are shared across targets. Minecraft 1.21.x stores its counter in `data/daycounter_days.dat` relative to the world folder. The 26.x builds import that file once when no modern counter exists and leave the original unchanged. See [world data and migration](configuration.md#world-data-and-multiplayer) before upgrading a world.

## Русский

### Версии и зависимости

Для **DayCounter 1.2.0** предусмотрено 16 отдельных сборок: **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 и 26.3**. Выбирайте JAR для точной версии игры. Один и тот же файл подходит клиенту и серверу этой версии. Снапшоты, предварительные выпуски, release candidate, отсутствующие в таблице версии Minecraft и будущие версии API в матрицу не входят.

Для **1.21.x нужна Java 21**, для **26.x — Java 25**. Для сборки любого варианта нужен **JDK 25**. Используйте совместимый стабильный **Fabric Loader** не ниже указанного в таблице. Последний выбранный Loader для этой матрицы и сборки — **0.20.1**. Учитывайте также требования выбранных Fabric API и необязательного Mod Menu.

**Fabric API обязателен** на клиенте и сервере. Его метаданные должны разрешать вашу точную версию Minecraft. Суффикс может отличаться: например, выбранный `0.155.3+26.1.2` используется для трёх вариантов 26.1. Минимальные версии Loader, минимальные и последние выбранные версии API, а также версии необязательного **Mod Menu** перечислены в таблице выше и закреплены в [gradle/targets.json](../gradle/targets.json). Каждый JAR объявляет эти минимумы Loader и API. Для каждой версии Minecraft проверены две пары: **минимальный Loader + минимальная API** и **Loader 0.20.1 + последняя выбранная API**. Вторая пара используется при обычной сборке. Совместимость со всеми промежуточными и будущими выпусками не обещается. Без Mod Menu настройки доступны через JSON.

Минимальные версии Loader/API в таблице относятся к **DayCounter без Mod Menu**. Необязательный Mod Menu может требовать более новые зависимости; сверяйтесь с метаданными выбранного выпуска. Например, для **Minecraft 26.2** DayCounter с минимальной API требует **Loader 0.18.4**, а **Mod Menu 20.0.3 — Loader 0.19.2 или новее**. Для **Minecraft 1.21.9 и 1.21.10** DayCounter требует **Loader 0.17.0**, а **Mod Menu 16.0.1 — Loader 0.17.2 или новее**. Требование дополнительного мода не повышает собственный минимум DayCounter.

### Статус проверок

**Сборки и проверки загрузки 1.2.0 прошли 2026-11-10:** все 16 сборок; 1063 запуска JUnit-тестов (64–70 на версию, без ошибок и пропусков); 32 серверные и 32 клиентские пробы минимальных и последних выбранных пар; проверка метаданных, версии Java-классов и содержимого всех 16 JAR. Пробы выполнялись на Java 21 для 1.21.x и Java 25 для 26.x. Контрольные суммы SHA-256 связали результаты с распространяемыми файлами.

Пробы инициализировали готовый JAR через Fabric Knot в режимах SERVER/CLIENT и затем вызывали точки входа мода. Все клиентские пробы последних выбранных пар включали Mod Menu и фабрику настроек; минимальные пары проверялись с Mod Menu или без него. Серверные пробы проверяли hooks, сохранение и повторную загрузку данных, а для 26.x — перенос старого счётчика. Игровой цикл, мир и окно не запускались. Отрисовка, ввод, звук, полноценная игра и FPS ещё не проверены. Подробности — в [руководстве разработчика](development.md#русский).

### Файлы релиза

Для каждой версии скрипт подготовки создаёт `dist/<версия игры>/daycounter-1.2.0+<версия игры>.jar` и соседний `SHA256SUMS.txt`. Пример: `dist/1.21.1/daycounter-1.2.0+1.21.1.jar`.

GitHub-релиз **`v1.2.0`** содержит **18 файлов**: 16 отдельных JAR, общий `SHA256SUMS.txt` и `daycounter-1.2.0-all-versions.zip`. ZIP сохраняет отдельные папки версий. В `mods` помещается только JAR для вашей игры. Архивы исходников в игру не устанавливаются. Контрольную сумму можно проверить командой `Get-FileHash`, приведённой выше, и сравнить со списком.

### Перенос мира

Клиентский JSON и независимый выбор EN/RU одинаковы для всех сборок. Счётчик 1.21.x хранится в `data/daycounter_days.dat` относительно мира. В 26.x он импортируется один раз при отсутствии современных данных; исходный файл не изменяется. Подробности и путь новых данных — в [руководстве по настройке](configuration.md#русский).
