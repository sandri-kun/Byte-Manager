package org.bytedata.manager.utils;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.bytedata.manager.ApplicationLoader;

public class UserConfig extends BaseController {
    public static int selectedAccount;
    public final static int MAX_ACCOUNT_DEFAULT_COUNT = 3;
    public final static int MAX_ACCOUNT_COUNT = 4;

    public SharedPreferences sharedPreferences;

    private static volatile UserConfig Instance = null;
    public static UserConfig getInstance() {
        return getInstance(0);
    }
    public static UserConfig getInstance(int type) {
        UserConfig localInstance = Instance;
        if (localInstance == null) {
            synchronized (UserConfig.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UserConfig(type);
                }
            }
        }
        return localInstance;
    }

    public UserConfig(int num) {
        super(num);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static void setAccount(int num) {
        selectedAccount = num;
    }

    public static int getAccount() {
        return selectedAccount;
    }

    public static int getMaxAccountCount() {
        return MAX_ACCOUNT_COUNT;
    }

    public boolean isClientActivated() {
        return false;
    }
}

