package org.bytedata.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.MaterialColors;
import com.hjq.language.MultiLanguages;

import com.androidnetworking.AndroidNetworking;
import org.bytedata.manager.databinding.ActivityMainBinding;
import org.bytedata.manager.databinding.DrawerMainBinding;
import org.bytedata.manager.ui.actionbar.BaseActivity;
import org.bytedata.manager.ui.actionbar.SharedViewModel;
import org.bytedata.manager.utils.AndroidUtilities;
import org.bytedata.manager.utils.BuildVars;
import org.bytedata.manager.utils.FileLog;
import org.bytedata.manager.utils.SharedConfig;

@SuppressLint("CustomSplashScreen")
public class LaunchActivity extends BaseActivity<ActivityMainBinding> {
    private final String TAG = "LaunchActivity: ";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }

    @Override
    protected ActivityMainBinding initializeBinding(@NonNull LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Shared View Model
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.getLiveData(SharedViewModel.CONFIG_LOADED).observe(this, bundle -> {
            FileLog.d(TAG + "Config loaded");
        });

        // windows
        if (BuildVars.DEBUG_VERSION) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                    .detectLeakedClosableObjects()
                    .build());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                ObjectAnimator animator = ObjectAnimator.ofFloat(splashScreenView, View.ALPHA, 1f, 0f);
                animator.setInterpolator(org.bytedata.manager.ui.editor.components.CubicBezierInterpolator.DEFAULT);
                animator.setDuration(0L);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashScreenView.remove();
                    }
                });
                animator.start();
            });
        }
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
        AndroidUtilities.fillStatusBarHeight(ApplicationLoader.applicationContext);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int dynamicColor = MaterialColors.getColor(this, R.attr.colorSurface, Color.BLACK);
        boolean isLight = (dynamicColor & 0xFFFFFF) > 0xFFFFFF / 2;
        getWindow().getDecorView().setSystemUiVisibility(isLight ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0);

        AndroidNetworking.initialize(getApplicationContext());

        // logic
        initializeLogic();
    }

    private void initializeLogic() {
        setSupportActionBar(getBinding().toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getBinding().toolbar.setNavigationOnClickListener(_v -> onBackPressed());

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(LaunchActivity.this, getBinding().drawerLayout, getBinding().toolbar, R.string.AppName, R.string.AppName);
            getBinding().drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    private static LaunchActivity staticInstanceForAlerts;
    private void checkFreeDiscSpace(final int force) {
        staticInstanceForAlerts = this;
        //AutoDeleteMediaTask.run();
        SharedConfig.checkLogsToDelete();
    }

    public static void checkFreeDiscSpaceStatic(final int force) {
        if (staticInstanceForAlerts != null) {
            staticInstanceForAlerts.checkFreeDiscSpace(force);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}