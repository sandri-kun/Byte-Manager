package org.bytedata.manager.utils;

import android.content.pm.PackageInfo;

import org.bytedata.manager.ApplicationLoader;
import org.bytedata.manager.tgnet.TLRPC;

import java.io.File;

public class SharedConfig {
    private static final String TAG = "SharedConfig: ";
    private static final Object sync = new Object();
    private static final Object localIdSync = new Object();
    public static boolean noStatusBar = false;
    public static String storageCacheDir;
    public static TLRPC.TL_help_appUpdate pendingAppUpdate;
    public static int pendingAppUpdateBuildVersion;
    public static long lastUpdateCheckTime;
    private static boolean configLoaded;

    public static void checkLogsToDelete() {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        int lastLogsCheckTime = UserConfig.getInstance().getSharedPreferences().getInt("lastLogsCheckTime", 0);
        int time = (int) (System.currentTimeMillis() / 1000);
        if (Math.abs(time - lastLogsCheckTime) < 60 * 60) { // 1 jam
            FileLog.d(TAG + "Check logs disabled");
            return;
        }
        lastLogsCheckTime = time;
        int finalLastLogsCheckTime = lastLogsCheckTime;
        Utilities.cacheClearQueue.postRunnable(() -> {
            long startTime = System.currentTimeMillis();
            try {
                File dir = AndroidUtilities.getLogsDir();
                if (dir == null) {
                    return;
                }
                FileLog.cleanupLogs();
            } catch (Throwable e) {
                FileLog.e(e);
            }
            UserConfig.getInstance().getSharedPreferences().edit().putInt("lastLogsCheckTime", finalLastLogsCheckTime).apply();
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d(TAG + "Check logs task end time " + (System.currentTimeMillis() - startTime));
            }
        });
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded || ApplicationLoader.applicationContext == null) {
            }
        }
    }

    public static void saveConfig() {
        synchronized (sync) {
            try {

            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static boolean setNewAppVersionAvailable(TLRPC.TL_help_appUpdate update) {
        String updateVersionString = null;
        int versionCode = 0;
        try {
            PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
            updateVersionString = packageInfo.versionName;
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (versionCode == 0) {
            versionCode = BuildVars.BUILD_VERSION;
        }
        if (updateVersionString == null) {
            updateVersionString = BuildVars.BUILD_VERSION_STRING;
        }
        if (update.version == null || updateVersionString.compareTo(update.version) >= 0) {
            return false;
        }
        pendingAppUpdate = update;
        pendingAppUpdateBuildVersion = versionCode;
        saveConfig();
        return true;
    }

    //by me
    public static boolean setCanNotSkipAppUpdate(TLRPC.TL_help_appUpdate update) {
        String updateVersionString = null;
        int versionCode = 0;
        try {
            PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
            updateVersionString = packageInfo.versionName;
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (versionCode == 0) {
            versionCode = BuildVars.BUILD_VERSION;
        }
        if (updateVersionString == null) {
            updateVersionString = BuildVars.BUILD_VERSION_STRING;
        }
        return update.version != null && updateVersionString.compareTo(update.versionCanSkip) < 0;
    }

    //end
}

