package org.bytedata.manager.tgnet;

import android.content.pm.PackageInfo;

import com.google.gson.annotations.SerializedName;

import org.bytedata.manager.ApplicationLoader;
import org.bytedata.manager.utils.BuildVars;
import org.bytedata.manager.utils.FileLog;

import java.util.List;

@SuppressWarnings("unchecked")
public class TLRPC {

    public static class AppAds {
        @SerializedName("ads_version")
        public String adsVersion;
        @SerializedName("app_id")
        public String appId;
        @SerializedName("banner_id")
        public String bannerId;
        @SerializedName("interstitial_id")
        public String interstitialId;
        @SerializedName("native_id")
        public String nativeId;
        @SerializedName("rewarded_id")
        public String rewardedId;
        @SerializedName("app_open_id")
        public String appOpenId;
    }

    public class Document {
        @SerializedName("file_name")
        public String fileName;
        @SerializedName("size")
        public String size;
        @SerializedName("date")
        public long date;
    }

    public class TextListItem {
        @SerializedName("title")
        public String title;
        @SerializedName("subtitle")
        public String subtitle;
    }

    public class TL_help_appUpdate {
        @SerializedName("server_status")
        public boolean serverStatus;
        @SerializedName("title")
        public String title;
        @SerializedName("version")
        public String version;
        @SerializedName("version_can_skip")
        public String versionCanSkip;
        @SerializedName("can_not_skip")
        public boolean canNotSkip;
        @SerializedName("enable_ads")
        public boolean enableAds;
        @SerializedName("app_ads_config")
        public TLRPC.AppAds appAdsConfig;
        @SerializedName("max_app_open_ads_count")
        public int maxAppOpenAdsCount;
        @SerializedName("enable_log")
        public boolean enableLog;
        @SerializedName("url")
        public String url;
        @SerializedName("text")
        public String text;
        @SerializedName("side_note_text")
        public String sideNoteText;
        @SerializedName("document")
        public TLRPC.Document document;
        @SerializedName("textList")
        public List<TLRPC.TextListItem> textList;

        public boolean setNewAppVersionAvailable() {
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
            return version != null && updateVersionString.compareTo(version) < 0;
        }

        public boolean setCanNotSkipAppUpdate() {
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
            return version != null && updateVersionString.compareTo(versionCanSkip) < 0;
        }
    }
}