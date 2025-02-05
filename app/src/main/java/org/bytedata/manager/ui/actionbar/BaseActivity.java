package org.bytedata.manager.ui.actionbar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.bytedata.manager.R;
import org.bytedata.manager.utils.LocaleController;

public abstract class BaseActivity<B extends ViewBinding> extends AppCompatActivity {

    // More
    public static final int REQUEST_CODE_LEGACY_STORAGE = 1;
    public static final int REQUEST_CODE_MEDIA_ACCESS = 2;
    private B binding;

    /**
     * Metode untuk mendapatkan instance binding yang aktif.
     */
    protected B getBinding() {
        return binding;
    }

    /**
     * Metode untuk inisialisasi binding. Harus di-override jika menggunakan ViewBinding.
     */
    protected B initializeBinding(@NonNull LayoutInflater inflater) {
        return null;
    }

    /**
     * Metode untuk mendapatkan layout ID. Override jika menggunakan layout ID.
     */
    @LayoutRes
    protected int layoutId() {
        return 0; // Default, jika subclass tidak mendefinisikan layout ID
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        int layoutId = layoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
        } else {
            binding = initializeBinding(inflater);
            if (binding != null) {
                setContentView(binding.getRoot());
            } else {
                throw new IllegalStateException("Either layoutId() or initializeBinding() must be overridden");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Hindari memory leak
    }

    public void requestPermissionMedia() {
        Activity activity = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API level 33+)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO
                        },
                        REQUEST_CODE_MEDIA_ACCESS);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10–12 (API level 29–31)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_LEGACY_STORAGE);
            }
        } else {
            // Android 8.0–9.0 (API level 23–28)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE_LEGACY_STORAGE);
            }
        }
    }

    public boolean hasMediaPermission() {
        Activity activity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API level 33+): Periksa izin granular untuk media
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10–12 (API level 29–31): Periksa izin READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 9 dan sebelumnya (API level 28 ke bawah): Periksa izin READ_EXTERNAL_STORAGE dan WRITE_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void showSettingsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(LocaleController.getString("PermissionDialogTitle", R.string.PermissionDialogTitle))
                .setMessage(LocaleController.getString("PermissionDialogMessage", R.string.PermissionDialogMessage))
                .setPositiveButton(LocaleController.getString("OpenSettings", R.string.OpenSettings), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }
}
