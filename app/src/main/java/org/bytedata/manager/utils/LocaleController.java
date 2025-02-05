package org.bytedata.manager.utils;

import androidx.annotation.StringRes;

import org.bytedata.manager.ApplicationLoader;

import java.util.HashMap;
import java.util.Locale;

public class LocaleController {

    static final int QUANTITY_OTHER = 0x0000;
    static final int QUANTITY_ZERO = 0x0001;
    static final int QUANTITY_ONE = 0x0002;
    static final int QUANTITY_TWO = 0x0004;
    static final int QUANTITY_FEW = 0x0008;
    static final int QUANTITY_MANY = 0x0010;
    private static final HashMap<Integer, String> resourcesCacheMap = new HashMap<>();
    public static boolean isRTL = false;
    public static int nameDisplayOrder = 1;
    public static boolean is24HourFormat = false;
    private static volatile LocaleController Instance = null;
    private final Locale systemDefaultLocale;
    private final HashMap<String, String> localeValues = new HashMap<>();

    public LocaleController() {
        systemDefaultLocale = Locale.getDefault();
    }

    public static LocaleController getInstance() {
        LocaleController localInstance = Instance;
        if (localInstance == null) {
            synchronized (LocaleController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new LocaleController();
                }
            }
        }
        return localInstance;
    }

    public static String getString(@StringRes int res) {
        String key = resourcesCacheMap.get(res);
        if (key == null) {
            resourcesCacheMap.put(res, key = ApplicationLoader.applicationContext.getResources().getResourceEntryName(res));
        }
        return getString(key, res);
    }

    public static void clearResourceCacheMap() {
        resourcesCacheMap.clear();
    }

    public static String getString(String key, int res) {
        return getInstance().getStringInternal(key, res);
    }

    public Locale getSystemDefaultLocale() {
        return systemDefaultLocale;
    }

    private String getStringInternal(String key, int res) {
        return getStringInternal(key, null, res);
    }

    private String getStringInternal(String key, String fallback, int res) {
        String value = BuildVars.USE_CLOUD_STRINGS ? localeValues.get(key) : null;
        if (value == null) {
            if (BuildVars.USE_CLOUD_STRINGS && fallback != null) {
                value = localeValues.get(fallback);
            }
            if (value == null) {
                try {
                    value = ApplicationLoader.applicationContext.getString(res);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
        if (value == null) {
            value = "LOC_ERR:" + key;
        }
        return value;
    }

}
