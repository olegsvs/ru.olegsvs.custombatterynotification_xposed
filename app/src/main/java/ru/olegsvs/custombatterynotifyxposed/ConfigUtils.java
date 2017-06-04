package ru.olegsvs.custombatterynotifyxposed;

import de.robv.android.xposed.XSharedPreferences;
import ru.olegsvs.custombatterynotifyxposed.xposed.CustomBatteryIconXposed;

public class ConfigUtils {

    private static ConfigUtils mInstance;

    private XSharedPreferences mPrefs;
    public IconsConfig icons;

    private ConfigUtils() {
        mInstance = this;
        mPrefs = new XSharedPreferences(CustomBatteryIconXposed.PACKAGE_OWN);
        loadConfig();
    }

    private void loadConfig() {
        icons = new IconsConfig(mPrefs);
    }

    public static ConfigUtils getInstance() {
        if (mInstance == null)
            mInstance = new ConfigUtils();
        return mInstance;
    }

    public static IconsConfig icons() {
        return getInstance().icons;
    }

    public class IconsConfig {
        public int battery;

        public IconsConfig(XSharedPreferences prefs) {
            battery = MainActivity.ICONS[prefs.getInt("batteryicon", 0)];
        }
    }

}
