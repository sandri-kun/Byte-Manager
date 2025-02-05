package org.bytedata.manager;

/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.color.DynamicColors;
import com.hjq.language.MultiLanguages;
import com.hjq.language.OnLanguageListener;

import org.bytedata.manager.ui.actionbar.ThemeManager;
import org.bytedata.manager.utils.FileLog;
import org.bytedata.manager.utils.UserConfig;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Locale;

public class ApplicationLoader extends Application {

    private static final int lastKnownNetworkType = -1;
    @SuppressLint("StaticFieldLeak")
    public static volatile Context applicationContext;
    public static volatile NetworkInfo currentNetworkInfo;
    public static volatile Handler applicationHandler;
    public static long startTime;
    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;
    public static volatile boolean mainInterfaceStopped = true;
    public static volatile boolean externalInterfacePaused = true;
    public static volatile boolean mainInterfacePausedStageQueue = true;
    public static boolean canDrawOverlays;
    public static volatile long mainInterfacePausedStageQueueTime;
    private static ApplicationLoader applicationLoaderInstance;
    private static ConnectivityManager connectivityManager;
    private static volatile boolean applicationInited = false;
    private static volatile ConnectivityManager.NetworkCallback networkCallback;
    private static long lastNetworkCheckTypeTime;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public ApplicationLoader() {
        super();
    }

    public static String getApplicationId() {
        return applicationLoaderInstance.onGetApplicationId();
    }

    public static boolean isHuaweiStoreBuild() {
        return applicationLoaderInstance.isHuaweiBuild();
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new File("/data/data/org.drm.player/files");
    }

    public static void postInitApplication() {
        if (applicationInited || applicationContext == null) {
            return;
        }
        applicationInited = true;
    }

    public static boolean isExpired() {
        // Gunakan tanggal default 1, 10, 2024
        return isExpired(13, 12, 2024);
    }

    public static boolean isExpired(int day, int month, int year) {
        // Validasi input
        if (!isValidDate(day, month, year)) {
            throw new IllegalArgumentException("Invalid date provided.");
        }

        // Dapatkan tanggal saat ini
        LocalDate currentDate = LocalDate.now();
        // Buat objek LocalDate untuk tanggal kedaluwarsa
        LocalDate expirationDate = LocalDate.of(year, month, day);

        // Bandingkan tanggal saat ini dengan tanggal kedaluwarsa
        return currentDate.isAfter(expirationDate);
    }

    // Validasi tanggal
    private static boolean isValidDate(int day, int month, int year) {
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void ensureCurrentNetworkGet(boolean force) {

    }

    public static boolean isRoaming() {
        try {
            ensureCurrentNetworkGet(false);
            return currentNetworkInfo != null && currentNetworkInfo.isRoaming();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static boolean isConnectedOrConnectingToWiFi() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo != null && (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || currentNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)) {
                NetworkInfo.State state = currentNetworkInfo.getState();
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING || state == NetworkInfo.State.SUSPENDED) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static boolean isConnectionSlow() {
        try {
            ensureCurrentNetworkGet(false);
            if (currentNetworkInfo != null && currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (currentNetworkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return true;
                }
            }
        } catch (Throwable ignore) {

        }
        return false;
    }

    public static void startAppCenter(Activity context) {
        applicationLoaderInstance.startAppCenterInternal(context);
    }

    public static void checkForUpdates() {
        applicationLoaderInstance.checkForUpdatesInternal();
    }

    public static void appCenterLog(Throwable e) {
        applicationLoaderInstance.appCenterLogInternal(e);
    }

    protected String onGetApplicationId() {
        return null;
    }

    protected boolean isHuaweiBuild() {
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }

    @Override
    public void onCreate() {
        applicationLoaderInstance = this;
        try {
            applicationContext = getApplicationContext();
        } catch (Throwable ignore) {

        }
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {
                    Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("error", Log.getStackTraceString(throwable));
                    startActivity(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(1);
                });
        super.onCreate();

        if (applicationContext == null) {
            applicationContext = getApplicationContext();
        }

        applicationHandler = new Handler(applicationContext.getMainLooper());

        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }

        if (!UserConfig.getInstance(0).getSharedPreferences().getBoolean("term-of-service", false)) {
            ThemeManager.saveTheme(applicationContext, ThemeManager.THEME_DARK);
        }

        ThemeManager.applyTheme(applicationContext, ThemeManager.getCurrentTheme(applicationContext));

        UserConfig.getInstance();

        MultiLanguages.init(this);
        MultiLanguages.setOnLanguageListener(new OnLanguageListener() {

            @Override
            public void onAppLocaleChange(Locale oldLocale, Locale newLocale) {
                FileLog.d("MultiLanguages" + oldLocale + "，New Locale：" + newLocale);
            }

            @Override
            public void onSystemLocaleChange(Locale oldLocale, Locale newLocale) {
                FileLog.d("MultiLanguages" + oldLocale + " New Locale：" + newLocale + "，System Language：" + MultiLanguages.isSystemLanguage(applicationContext));
            }
        });

        if (UserConfig.getInstance(0).getSharedPreferences().contains("language_code")) {
            String key = UserConfig.getInstance(0).getSharedPreferences().getString("language_code", "en");
            Locale selectedLocale = new Locale(key);
            MultiLanguages.setAppLanguage(ApplicationLoader.applicationContext, selectedLocale);
        } else {
            UserConfig.getInstance().getSharedPreferences().edit().putString("language_code", Locale.getDefault().getLanguage()).apply();
        }
    }

    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;

        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void appCenterLogInternal(Throwable e) {
    }

    protected void checkForUpdatesInternal() {
    }

    protected void startAppCenterInternal(Activity context) {
    }
}
