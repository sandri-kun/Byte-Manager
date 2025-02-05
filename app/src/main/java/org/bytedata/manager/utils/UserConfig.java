package org.bytedata.manager.utils;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.bytedata.manager.ApplicationLoader;

public class UserConfig extends BaseController {
    public final static int MAX_ACCOUNT_DEFAULT_COUNT = 3;
    public final static int MAX_ACCOUNT_COUNT = 4;
    public static int selectedAccount;
    private static volatile UserConfig Instance = null;
    public SharedPreferences sharedPreferences;

    public UserConfig(int num) {
        super(num);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.applicationContext);
    }

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

    public static int getAccount() {
        return selectedAccount;
    }

    public static void setAccount(int num) {
        selectedAccount = num;
    }

    public static int getMaxAccountCount() {
        return MAX_ACCOUNT_COUNT;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public boolean isClientActivated() {
        return false;
    }
}

