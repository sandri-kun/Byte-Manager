package org.bytedata.manager.ui.actionbar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.bytedata.manager.utils.FileLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedViewModel extends ViewModel {
    private static final String TAG = "SharedViewModel: ";
    public static int totalEvents = 1;
    public static final int POST_NOTIFICATION = totalEvents++;
    public static final int CONFIG_LOADED = totalEvents++;
    public static final int FIREBASE_ANALYTICS = totalEvents++;
    // Menggunakan ConcurrentHashMap untuk menyimpan LiveData dengan Bundle
    private final Map<Integer, MutableLiveData<Bundle>> liveDataMap = new ConcurrentHashMap<>();

    // Set LiveData tanpa argumen, menggunakan Bundle kosong
    public void setLiveData(int id) {
        try {
            liveDataMap.computeIfAbsent(id, k -> new MutableLiveData<>()).setValue(new Bundle());
        } catch (Exception e) {
            FileLog.e(TAG + e.getMessage());
        }
    }

    // Set LiveData dengan argumen berupa Bundle
    public void setLiveData(int id, @NonNull Bundle bundle) {
        try {
            liveDataMap.computeIfAbsent(id, k -> new MutableLiveData<>()).setValue(bundle);
        } catch (Exception e) {
            FileLog.e(TAG + e.getMessage());
        }
    }

    // Mendapatkan LiveData berdasarkan id
    @NonNull
    public LiveData<Bundle> getLiveData(int id) {
        return liveDataMap.computeIfAbsent(id, k -> new MutableLiveData<>());
    }
}
