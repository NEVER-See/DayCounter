package dev.daycounter.config;

import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigStore {
    private static final System.Logger LOGGER = System.getLogger("DayCounter");
    private static volatile DayCounterConfig current = new DayCounterConfig();

    private ConfigStore() { }
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("daycounter.json"); }
    public static DayCounterConfig load() { current = ConfigFile.read(path()); return current; }

    /** Read-only current value. Editors must use copy() or SettingsDraft. */
    public static DayCounterConfig get() { return current; }

    /** A failed save leaves the currently applied settings unchanged. */
    public static boolean save(DayCounterConfig config) {
        DayCounterConfig safe = config.normalized();
        try {
            ConfigFile.write(path(), safe);
            current = safe;
            return true;
        } catch (IOException | RuntimeException error) {
            LOGGER.log(System.Logger.Level.ERROR, "Could not save DayCounter settings", error);
            return false;
        }
    }
}
