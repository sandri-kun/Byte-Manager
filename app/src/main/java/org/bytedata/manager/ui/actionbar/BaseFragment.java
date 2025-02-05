package org.bytedata.manager.ui.editor.actionbar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.bytedata.manager.R;
import org.bytedata.manager.utils.LocaleController;

public abstract class BaseFragment<B extends ViewBinding> extends Fragment {

    // More
    public static final int REQUEST_CODE_LEGACY_STORAGE = 1;
    public static final int REQUEST_CODE_MEDIA_ACCESS = 2;
    public boolean onSeachViewOpen = false;
    private B binding;

    /**
     * Override untuk Fragment berbasis View Binding.
     */
    protected B getBinding() {
        return binding;
    }

    /**
     * Override untuk Fragment berbasis View Binding.
     */
    @Nullable
    protected B initializeBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return null;
    }

    /**
     * Override untuk Fragment berbasis layout ID.
     */
    @LayoutRes
    protected int layoutId() {
        return 0; // Default, jika subclass tidak mendefinisikan layout ID
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return requireView().findViewById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = layoutId();
        if (layoutId != 0) {
            return inflater.inflate(layoutId, container, false);
        }

        binding = initializeBinding(inflater, container);
        if (binding != null) {
            return binding.getRoot();
        }

        throw new IllegalStateException("Either layoutId() or initializeBinding() must be overridden");
    }

    public void onBackPressed() {
        requireActivity().finish();
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
    }

    public void onActivityFragmentResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Hindari memory leak
    }

    public void requestPermissionMedia(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API level 33+)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO,
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

    public boolean hasMediaPermission(Activity activity) {
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
        if (getContext() == null) {
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(LocaleController.getString("PermissionDialogTitle", R.string.PermissionDialogTitle))
                .setMessage(LocaleController.getString("PermissionDialogMessage", R.string.PermissionDialogMessage))
                .setPositiveButton(LocaleController.getString("OpenSettings", R.string.OpenSettings), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }
}