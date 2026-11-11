# Compatibility

[README](../README.md) · [Configuration](configuration.md) · [Development](development.md) · [Русский](#русский)

## Minecraft and dependencies

DayCounter **1.4.0** provides a separate build for each of the 16 stable Minecraft versions below, starting with **1.21.1**. Install only the JAR named for your exact version. The same JAR works on its client and server. Snapshots, pre-releases, release candidates, unlisted Minecraft versions, and future API releases are outside this matrix.

Use a compatible stable **Fabric Loader**, at least the minimum listed below; the selected latest Loader for this matrix and the build is **0.20.1**. Also meet the requirements of your chosen Fabric API and optional Mod Menu release. Install **Fabric API** on both the client and server. Choose an API release whose metadata supports your exact Minecraft version. Its version suffix need not be identical: for example, the selected `0.155.3+26.1.2` build is shared by the listed 26.1 targets.

The table reflects [gradle/targets.json](../gradle/targets.json). Each JAR declares the listed minimum Loader and Fabric API versions. The validation matrix uses two pairs per Minecraft target: **minimum Loader + minimum API**, and **Loader 0.20.1 + selected latest API**. The latter is also the default build pair. These endpoints do not guarantee every intermediate or future release. **Mod Menu is optional** and is only needed to open the settings screen; JSON configuration remains available without it.

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

The **1.4.0** release passed all 16 builds, 1,463 JUnit test executions, 32 final client loader probes and all release JAR audits. Server loader checks cover byte-identical common/server classes. Native UI checks were performed on 1.21.1 and 26.3. See the [development guide](development.md#validation) for the exact scope and limitations, including server result reuse and the absence of full gameplay FPS testing.

## Release files

The release-preparation helper writes the **1.4.0** build and checksum for each `<game>`.

```text
dist/<game>/
  daycounter-1.4.0+<game>.jar
  SHA256SUMS.txt
```

For example, the 1.4.0 JAR for Minecraft 1.21.1 is `dist/1.21.1/daycounter-1.4.0+1.21.1.jar`. Previous JARs and checksums are stored in `dist/<game>/archive/<modversion>/`. The archive contains 1.2.0 and 1.3.0 for all 16 targets.

The local bundle `dist/daycounter-1.4.0-all-versions.zip` contains the 16 new JARs and their per-game checksum files.

Download **1.4.0** from [GitHub Releases](https://github.com/NEVER-See/DayCounter/releases/tag/v1.4.0). The multi-version release consists of 16 installable JARs, an aggregate `SHA256SUMS.txt`, and the combined ZIP. Extract only the JAR for your Minecraft version into `mods`. Source archives are for developers and are not installed in the game.

To verify a downloaded JAR in PowerShell, compare this output with the checksum manifest:

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath '.\daycounter-1.4.0+1.21.1.jar'
```

## Listing dependencies on download sites

Each release JAR already declares its exact Minecraft target, minimum Fabric Loader and Fabric API, Java requirement, and optional Mod Menu in `fabric.mod.json`. This metadata is generated separately for each target.

Modrinth also stores dependencies in each version listing. Set the exact Minecraft version and Fabric loader, then add **Fabric API as Required** and **Mod Menu as Optional**. If the suggested files target a different Minecraft version, select the matching releases from the table above. The **Selected latest Fabric API** and **Optional Mod Menu** columns identify the versions used together in the current build matrix; they are not one shared dependency pair for all Minecraft versions.

A Modrinth dependency can refer to a whole project or a specific version. Use a specific compatible version when you need an exact selection; project-level dependencies leave version selection to the site/client. The JAR metadata does not replace these listing fields. See the [Modrinth version API](https://docs.modrinth.com/api/operations/createversion/). Other download sites have their own dependency settings.

## World upgrades

The client settings file and independent EN/RU language choice are shared across targets. Minecraft 1.21.x stores its counter in `data/daycounter_days.dat` relative to the world folder. The 26.x builds import that file once when no modern counter exists and leave the original unchanged. See [world data and migration](configuration.md#world-data-and-multiplayer) before upgrading a world.

## Русский

### Версии и зависимости

**DayCounter 1.4.0** доступен в 16 отдельных сборках, начиная с **Minecraft 1.21.1**. Поддерживаемые версии — **1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 и 26.3**. Выбирайте JAR для точной версии игры. Один и тот же файл подходит клиенту и серверу этой версии. Снапшоты, предварительные выпуски, release candidate, отсутствующие в таблице версии Minecraft и будущие версии API в матрицу не входят.

Для **1.21.x нужна Java 21**, для **26.x — Java 25**. Для сборки любого варианта нужен **JDK 25**. Используйте совместимый стабильный **Fabric Loader** не ниже указанного в таблице. Последний выбранный Loader для этой матрицы и сборки — **0.20.1**. Учитывайте также требования выбранных Fabric API и необязательного Mod Menu.

**Fabric API обязателен** на клиенте и сервере. Его метаданные должны разрешать вашу точную версию Minecraft. Суффикс может отличаться: например, выбранный `0.155.3+26.1.2` используется для трёх вариантов 26.1. Минимальные версии Loader, минимальные и последние выбранные версии API, а также версии необязательного **Mod Menu** перечислены в таблице выше и закреплены в [gradle/targets.json](../gradle/targets.json). Каждый JAR объявляет эти минимумы Loader и API. Матрица проверок использует две пары для каждой версии Minecraft: **минимальный Loader + минимальная API** и **Loader 0.20.1 + последняя выбранная API**. Вторая пара используется при обычной сборке. Совместимость со всеми промежуточными и будущими выпусками не обещается. Без Mod Menu настройки доступны через JSON.

Минимальные версии Loader/API в таблице относятся к **DayCounter без Mod Menu**. Необязательный Mod Menu может требовать более новые зависимости; сверяйтесь с метаданными выбранного выпуска. Например, для **Minecraft 26.2** DayCounter с минимальной API требует **Loader 0.18.4**, а **Mod Menu 20.0.3 — Loader 0.19.2 или новее**. Для **Minecraft 1.21.9 и 1.21.10** DayCounter требует **Loader 0.17.0**, а **Mod Menu 16.0.1 — Loader 0.17.2 или новее**. Требование дополнительного мода не повышает собственный минимум DayCounter.

### Статус проверок

Выпуск **1.4.0** прошёл 16 сборок, 1463 выполнения JUnit-тестов, 32 финальные клиентские проверки загрузки и аудит всех JAR. Серверные проверки относятся к побайтово неизменным общим/серверным классам. Нативный интерфейс проверен на 1.21.1 и 26.3. Точный объём и ограничения, включая повторное использование серверных результатов и отсутствие проверки FPS в мире, описаны в [руководстве разработчика](development.md#проверки-и-ограничения).

### Файлы релиза

Для каждой версии игры скрипт подготовки 1.4.0 создаёт `dist/<версия игры>/daycounter-1.4.0+<версия игры>.jar` и соседний `SHA256SUMS.txt`. Для Minecraft 1.21.1 это `dist/1.21.1/daycounter-1.4.0+1.21.1.jar`. Предыдущие JAR и контрольные суммы хранятся в `dist/<версия игры>/archive/<версия мода>/`. Версии 1.2.0 и 1.3.0 сохранены для всех 16 вариантов.

Локальный архив `dist/daycounter-1.4.0-all-versions.zip` содержит 16 новых JAR и отдельный файл контрольных сумм для каждой версии игры.

Скачайте **1.4.0** в [GitHub Releases](https://github.com/NEVER-See/DayCounter/releases/tag/v1.4.0). Полный многоверсионный выпуск включает 16 отдельных JAR, общий `SHA256SUMS.txt` и ZIP. В `mods` помещается только JAR для вашей игры; архивы исходников не устанавливаются. Контрольную сумму можно сравнить со списком командой `Get-FileHash`, приведённой выше.

### Зависимости на сайтах загрузки

Каждый JAR уже содержит в `fabric.mod.json` точную версию Minecraft, минимальные Fabric Loader и Fabric API, требование Java и необязательный Mod Menu. Эти сведения формируются отдельно для каждой версии игры.

На Modrinth зависимости дополнительно указываются в карточке каждого выпуска. Выберите точную версию Minecraft и Fabric, затем добавьте **Fabric API — Required**, **Mod Menu — Optional**. Если подсказка предлагает файл для другой версии игры, выберите подходящий выпуск из таблицы выше. Столбцы **Selected latest Fabric API** и **Optional Mod Menu** показывают версии, использованные вместе в текущей матрице сборки; это не одна общая пара для всех Minecraft.

Зависимость Modrinth может ссылаться на проект целиком или конкретный выпуск. Для точного выбора укажите совместимый выпуск; зависимость от проекта оставляет выбор версии сайту/клиенту. Метаданные JAR не заменяют эти поля карточки. См. [API версий Modrinth](https://docs.modrinth.com/api/operations/createversion/). У других сайтов настройки зависимостей свои.

### Перенос мира

Клиентский JSON и независимый выбор EN/RU одинаковы для всех сборок. Счётчик 1.21.x хранится в `data/daycounter_days.dat` относительно мира. В 26.x он импортируется один раз при отсутствии современных данных; исходный файл не изменяется. Подробности и путь новых данных — в [руководстве по настройке](configuration.md#русский).
