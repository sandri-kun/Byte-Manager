package org.bytedata.manager.utils;

/*
 * This is the source code of Telegram for Android v. 7.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

import android.os.Build;

import org.bytedata.manager.ApplicationLoader;

public class BuildVars {

    public static boolean SHOW_APP_OPEN_AD = true;
    public static String TERMS_OF_SERVICE_URL = "https://www.indoverseid.site/project/iptv/privacy-policy";
    public static String TERM_OF_CONDITION = "https://www.indoverseid.site/project/iptv/term-condition";

    public static boolean DEBUG_VERSION = false;
    public static boolean LOGS_ENABLED = false;
    public static boolean DEBUG_PRIVATE_VERSION = false;
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = true;
    public static boolean NO_SCOPED_STORAGE = Build.VERSION.SDK_INT <= 29;
    public static int BUILD_VERSION = 3252;
    public static String BUILD_VERSION_STRING = "1.1.9";
    public static int APP_ID = 4;
    public static String APP_HASH = "014b35b6184100b085b0d0572f9b5103";

    // SafetyNet key for Google Identity SDK, set it to empty to disable
    public static String SAFETYNET_KEY = "AIzaSyDqt8P-7F7CPCseMkOiVRgb1LY8RN1bvH8";
    public static String SMS_HASH = isStandaloneApp() ? "w0lkcmTZkKh" : (DEBUG_VERSION ? "O2P2z+/jBpJ" : "oLeq9AcOZkT");
    public static String PLAYSTORE_APP_URL = "https://play.google.com/store/apps/details?id=org.drm.player";
    public static String TELEGRAM_APP_URL = "https://t.me/drm_stream_player";
    public static String GOOGLE_AUTH_CLIENT_ID = "760348033671-81kmi3pi84p11ub8hp9a1funsv0rn2p9.apps.googleusercontent.com";

    public static String HUAWEI_APP_ID = "101184875";

    // You can use this flag to disable Google Play Billing (If you're making fork and want it to be in Google Play)
    public static boolean IS_BILLING_UNAVAILABLE = false;

    static {
        if (ApplicationLoader.applicationContext != null) {
            LOGS_ENABLED = DEBUG_VERSION || UserConfig.getInstance(0).getSharedPreferences().getBoolean("logsEnabled", false);
            DEBUG_VERSION = UserConfig.getInstance(0).getSharedPreferences().getBoolean("logsEnabled", false);
        }
    }

    public static String PUBLIC_KEY = AndroidUtilities.decodeBase64("LytpbmRvdmVyc2VAd2VjYQ=="); // "/+indoverse@weca"
    public static String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    public static class Api {
        public static String GET_IP = "http://ip-api.com/json"; //LoginActivity
        public static String BASE_API = "https://next-indo-verse-id.vercel.app"; //BaseApi
        public static String GET_UPDATES = "https://next-indo-verse-id.vercel.app/project/iptv/config.json";
        public static String GET_UPDATES2 = "https://raw.githubusercontent.com/sandri-kun/NextIndoVerseID/refs/heads/main/public/project/iptv/config.json"; //update
    }

    public static boolean useInvoiceBilling() {
        return DEBUG_VERSION || isStandaloneApp() || isBetaApp() || isHuaweiStoreApp() || hasDirectCurrency();
    }

    private static boolean hasDirectCurrency() {
        return false;
    }

    private static Boolean standaloneApp;
    public static boolean isStandaloneApp() {
        if (standaloneApp == null) {
            standaloneApp = ApplicationLoader.applicationContext != null && "org.anime.project.utils.web".equals(ApplicationLoader.applicationContext.getPackageName());
        }
        return standaloneApp;
    }

    private static Boolean betaApp;
    public static boolean isBetaApp() {
        if (betaApp == null) {
            betaApp = ApplicationLoader.applicationContext != null && "org.anime.project.utils.beta".equals(ApplicationLoader.applicationContext.getPackageName());
        }
        return betaApp;
    }


    public static boolean isHuaweiStoreApp() {
        return ApplicationLoader.isHuaweiStoreBuild();
    }
}
