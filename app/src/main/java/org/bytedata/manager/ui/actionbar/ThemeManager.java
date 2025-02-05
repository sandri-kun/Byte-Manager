package org.bytedata.manager.ui.actionbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    private static final String THEME_PREF = "theme-data";
    private static final String THEME_KEY = "ide-theme";

    public static void applyTheme(Context context, int type) {
        saveTheme(context, type);

        switch (type) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static int getCurrentTheme(Context context) {
        return getPreferences(context).getInt(THEME_KEY, THEME_SYSTEM);
    }

    public static boolean isSystemTheme(Context context) {
        return getCurrentTheme(context) == THEME_SYSTEM;
    }

    public static int getSystemAppliedTheme(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_NO:
                return THEME_LIGHT;
            case Configuration.UI_MODE_NIGHT_YES:
                return THEME_DARK;
            default:
                return THEME_SYSTEM;
        }
    }

    public static void saveTheme(Context context, int theme) {
        getPreferences(context).edit().putInt(THEME_KEY, theme).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE);
    }
}