package org.bytedata.manager.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.bytedata.manager.ApplicationLoader;
import org.bytedata.manager.R;
import org.bytedata.manager.ui.editor.components.CubicBezierInterpolator;
import org.bytedata.manager.ui.editor.components.TypefaceSpan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AndroidUtilities {
    public final static int LIGHT_STATUS_BAR_OVERLAY = 0x0f000000, DARK_STATUS_BAR_OVERLAY = 0x33000000;

    public final static int REPLACING_TAG_TYPE_LINK = 0;
    public final static int REPLACING_TAG_TYPE_BOLD = 1;

    public final static String TYPEFACE_ROBOTO_MEDIUM = "fonts/rmedium.ttf";
    public final static String TYPEFACE_ROBOTO_MEDIUM_ITALIC = "fonts/rmediumitalic.ttf";
    public final static String TYPEFACE_ROBOTO_MONO = "fonts/rmono.ttf";
    public final static String TYPEFACE_MERRIWEATHER_BOLD = "fonts/mw_bold.ttf";
    public final static String TYPEFACE_COURIER_NEW_BOLD = "fonts/courier_new_bold.ttf";
    public static final RectF rectTmp = new RectF();
    public static final Rect rectTmp2 = new Rect();
    public static final int FLAG_TAG_BR = 1;
    public static final int FLAG_TAG_BOLD = 2;
    public static final int FLAG_TAG_COLOR = 4;
    public static final int FLAG_TAG_URL = 8;
    public static final int FLAG_TAG_ALL = FLAG_TAG_BR | FLAG_TAG_BOLD | FLAG_TAG_URL;
    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();
    public static DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    public static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    public static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    public static float density = 1;
    public static Point displaySize = new Point();
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static boolean usingHardwareInput;
    public static int statusBarHeight = 0;
    public static int navigationBarHeight = 0;
    private static AccessibilityManager accessibilityManager;
    private static Vibrator vibrator;
    //More
    private static android.widget.Toast toast;
    private static HashMap<Window, ValueAnimator> navigationBarColorAnimators;
    private static Boolean isTablet = null, wasTablet = null, isSmallScreen = null;

    public static Vibrator getVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
        return vibrator;
    }

    public static void vibrateCursor(View view) {
        try {
            if (view == null || view.getContext() == null) return;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
            if (!((Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE)).hasAmplitudeControl())
                return;
            view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        } catch (Exception ignore) {
        }
    }

    public static void vibrate(View view) {
        try {
            if (view == null || view.getContext() == null) return;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
            if (!((Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE)).hasAmplitudeControl())
                return;
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        } catch (Exception ignore) {
        }
    }

    public static boolean isAccessibilityTouchExplorationEnabled() {
        if (accessibilityManager == null) {
            accessibilityManager = (AccessibilityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        }
        return accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled();
    }

    public static void makeAccessibilityAnnouncement(CharSequence what) {
        AccessibilityManager am = (AccessibilityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am.isEnabled()) {
            AccessibilityEvent ev = AccessibilityEvent.obtain();
            ev.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            ev.getText().add(what);
            am.sendAccessibilityEvent(ev);
        }
    }

    public static void fillStatusBarHeight(Context context) {
        if (context == null || AndroidUtilities.statusBarHeight > 0) {
            return;
        }
        AndroidUtilities.statusBarHeight = getStatusBarHeight(context);
        AndroidUtilities.navigationBarHeight = getNavigationBarHeight(context);
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private static int getNavigationBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static void checkDisplaySize(Context context, android.content.res.Configuration newConfiguration) {
        try {
            density = context.getResources().getDisplayMetrics().density;
            android.content.res.Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }

            usingHardwareInput = configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == android.content.res.Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
            if (configuration.screenWidthDp != android.content.res.Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != android.content.res.Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
            FileLog.e("tmessages " + "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
        } catch (Exception e) {
            FileLog.e("tmessages " + e);

        }
    }

    public static boolean isRTL(CharSequence text) {
        if (text == null || text.length() <= 0) {
            return false;
        }
        char c;
        for (int i = 0; i < text.length(); ++i) {
            c = text.charAt(i);
            if (c >= 0x590 && c <= 0x6ff) {
                return true;
            }
        }
        return false;
    }

    public static int lerp(int a, int b, float f) {
        return (int) (a + f * (b - a));
    }

    public static float lerpAngle(float a, float b, float f) {
        float delta = ((b - a + 360 + 180) % 360) - 180;
        return (a + delta * f + 360) % 360;
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double lerp(double a, double b, float f) {
        return a + f * (b - a);
    }

    public static float lerp(float[] ab, float f) {
        return lerp(ab[0], ab[1], f);
    }

    public static void lerp(RectF a, RectF b, float f, RectF to) {
        if (to != null) {
            to.set(
                    AndroidUtilities.lerp(a.left, b.left, f),
                    AndroidUtilities.lerp(a.top, b.top, f),
                    AndroidUtilities.lerp(a.right, b.right, f),
                    AndroidUtilities.lerp(a.bottom, b.bottom, f)
            );
        }
    }

    public static void lerp(Rect a, Rect b, float f, Rect to) {
        if (to != null) {
            to.set(
                    AndroidUtilities.lerp(a.left, b.left, f),
                    AndroidUtilities.lerp(a.top, b.top, f),
                    AndroidUtilities.lerp(a.right, b.right, f),
                    AndroidUtilities.lerp(a.bottom, b.bottom, f)
            );
        }
    }

    public static float getScreenHeightInDp() {
        return (float) displayMetrics.heightPixels;
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static int dpr(float value) {
        if (value == 0) {
            return 0;
        }
        return Math.round(density * value);
    }

    public static int dp2(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.floor(density * value);
    }

    public static int compare(int lhs, int rhs) {
        if (lhs == rhs) {
            return 0;
        } else if (lhs > rhs) {
            return 1;
        }
        return -1;
    }

    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }

    public static float getScreenWidth() {
        return android.content.res.Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static int getMyLayerVersion(int layer) {
        return layer & 0xffff;
    }

    public static int getPeerLayerVersion(int layer) {
        return (layer >> 16) & 0xffff;
    }

    public static int setMyLayerVersion(int layer, int version) {
        return layer & 0xffff0000 | version;
    }

    public static int setPeerLayerVersion(int layer, int version) {
        return layer & 0x0000ffff | (version << 16);
    }

    public static String formatFileSize(long size) {
        return formatFileSize(size, false);
    }

    public static String formatFileSize(long size, boolean removeZero) {
        if (size == 0) {
            return String.format("%d KB", 0);
        } else if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            float value = size / 1024.0f;
            if (removeZero && (value - (int) value) * 10 == 0) {
                return String.format("%d KB", (int) value);
            } else {
                return String.format("%.1f KB", value);
            }
        } else if (size < 1000 * 1024 * 1024) {
            float value = size / 1024.0f / 1024.0f;
            if (removeZero && (value - (int) value) * 10 == 0) {
                return String.format("%d MB", (int) value);
            } else {
                return String.format("%.1f MB", value);
            }
        } else {
            float value = (int) (size / 1024L / 1024L) / 1000.0f;
            if (removeZero && (value - (int) value) * 10 == 0) {
                return String.format("%d GB", (int) value);
            } else {
                return String.format("%.2f GB", value);
            }
        }
    }

    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t;
                    if (Build.VERSION.SDK_INT >= 26) {
                        Typeface.Builder builder = new Typeface.Builder(ApplicationLoader.applicationContext.getAssets(), assetPath);
                        if (assetPath.contains("medium")) {
                            builder.setWeight(700);
                        }
                        if (assetPath.contains("italic")) {
                            builder.setItalic(true);
                        }
                        t = builder.build();
                    } else {
                        t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath);
                    }
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.e("Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    }
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

    public static void showToast(String text) {
        if (toast == null) {
            toast = android.widget.Toast.makeText(ApplicationLoader.applicationContext, text, android.widget.Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }

    public static int calculateCollumns(int i) {
        return (int) (((double) (displaySize.x / i)) + 0.5d);
    }

    public static int calculateCollumns(int i, int i2) {
        return (int) (((double) (i2 / i)) + 0.5d);
    }

    //EncryptStringToFile
    public static String decodeBase64(String string) {
        return new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8);
    }

    public static String encodeBase64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptStringFromStorage(String storage) {
        return decryptStringFromStorage(storage, "megatoonsecures");
    }

    public static String decryptStringFromStorage(String storage, String key) {
        try {
            javax.crypto.Cipher instance = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = key.getBytes();
            instance.init(2, new javax.crypto.spec.SecretKeySpec(bytes, "AES"), new javax.crypto.spec.IvParameterSpec(bytes));
            java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(storage, "r");
            byte[] bArr = new byte[((int) randomAccessFile.length())];
            randomAccessFile.readFully(bArr);
            return new String(instance.doFinal(bArr));
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    public static void encryptStringToStorage(String string, String storage) {
        encryptStringToStorage(string, "megatoonsecures", storage);
    }

    public static void encryptStringToStorage(String string, String key, String storage) {
        try {
            javax.crypto.Cipher instance = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            instance.init(1, new javax.crypto.spec.SecretKeySpec(key.getBytes(), "AES"), new javax.crypto.spec.IvParameterSpec(key.getBytes()));
            byte[] doFinal = instance.doFinal(string.trim().getBytes());
            java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(storage, "rw");
            randomAccessFile.setLength(0);
            randomAccessFile.write(doFinal);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static String encryptString(String data) throws Exception {
        return encryptString(data, BuildVars.PUBLIC_KEY);
    }

    public static String encryptString(String data, String key) throws Exception {
        // Mengubah key string menjadi SecretKey menggunakan AES
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");

        // Membuat cipher AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Enkripsi data
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Encode hasil enkripsi ke Base64 agar mudah disimpan atau dikirim
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decryptString(String encryptedData) throws Exception {
        return decryptString(encryptedData, BuildVars.PUBLIC_KEY);
    }

    public static String decryptString(String encryptedData, String key) throws Exception {
        // Mengubah key string menjadi SecretKey menggunakan AES
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");

        // Membuat cipher AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        // Decode data terenkripsi dari Base64
        byte[] decodedEncryptedData = Base64.getDecoder().decode(encryptedData);

        // Dekripsi data
        byte[] decryptedData = cipher.doFinal(decodedEncryptedData);

        // Mengembalikan data yang didekripsi sebagai string
        return new String(decryptedData);
    }

    //File app
    public static String readFileAssets(String inFile) {
        String tContents = "";
        try {
            InputStream stream = ApplicationLoader.applicationContext.getAssets().open(inFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            FileLog.e(e);
        }
        return tContents;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            android.os.StatFs stat = new android.os.StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatFileSize(availableBlocks * blockSize);
        } else {
            return "ERROR";
        }
    }

    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            android.os.StatFs stat = new android.os.StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatFileSize(totalBlocks * blockSize);
        } else {
            return "Error";
        }
    }

    public static ArrayList<File> getRootDirs() {
        ArrayList<File> result = null;
        if (Build.VERSION.SDK_INT >= 19) {
            File[] dirs = ApplicationLoader.applicationContext.getExternalFilesDirs(null);
            if (dirs != null) {
                for (int a = 0; a < dirs.length; a++) {
                    if (dirs[a] == null) {
                        continue;
                    }
                    String path = dirs[a].getAbsolutePath();
                    int idx = path.indexOf("/Android");
                    if (idx >= 0) {
                        if (result == null) {
                            result = new ArrayList<>();
                        }
                        File file = new File(path.substring(0, idx));
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i).getPath().equals(file.getPath())) {
                                continue;
                            }
                        }
                        result.add(file);
                    }
                }
            }
        }
        if (result == null) {
            result = new ArrayList<>();
        }
        if (result.isEmpty()) {
            result.add(Environment.getExternalStorageDirectory());
        }
        return result;
    }

    public static File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file;
                if (Build.VERSION.SDK_INT >= 19) {
                    File[] dirs = ApplicationLoader.applicationContext.getExternalCacheDirs();
                    file = dirs[0];
                    if (!TextUtils.isEmpty(SharedConfig.storageCacheDir)) {
                        for (int a = 0; a < dirs.length; a++) {
                            if (dirs[a] != null && dirs[a].getAbsolutePath().startsWith(SharedConfig.storageCacheDir)) {
                                file = dirs[a];
                                break;
                            }
                        }
                    }
                } else {
                    file = ApplicationLoader.applicationContext.getExternalCacheDir();
                }
                if (file != null && (file.exists() || file.mkdirs()) && file.canWrite()) {
                    return file;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        try {
            File file = ApplicationLoader.applicationContext.getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            File file = ApplicationLoader.applicationContext.getFilesDir();
            if (file != null) {
                File cacheFile = new File(file, "cache/");
                cacheFile.mkdirs();
                if ((file.exists() || file.mkdirs()) && file.canWrite()) {
                    return cacheFile;
                }
            }
        } catch (Exception e) {

        }
        return new File("");
    }

    public static String initializeCacheString() {
        return formatFileSize(initializeCache());
    }

    public static long initializeCache() {
        if (ApplicationLoader.applicationContext == null) {
            return 0;
        }
        long size = 0;
        size += getDirSize(ApplicationLoader.applicationContext.getCacheDir());
        size += getDirSize(ApplicationLoader.applicationContext.getExternalCacheDir());
        return size;
    }

    public static long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static Bitmap getBitmapFromAsset(String path) {
        InputStream stream = null;
        try {
            stream = ApplicationLoader.applicationContext.getAssets().open(path);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getDrawableFromAsset(String path) {
        InputStream stream = null;
        try {
            stream = ApplicationLoader.applicationContext.getAssets().open(path);
            return Drawable.createFromStream(stream, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getColorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        int rMean = (r1 + r2) / 2;
        int r = r1 - r2;
        int g = g1 - g2;
        int b = b1 - b2;
        return (((512 + rMean) * r * r) >> 8) + (4 * g * g) + (((767 - rMean) * b * b) >> 8);
    }

    public static int getAverageColor(int color1, int color2) {
        int r1 = Color.red(color1);
        int r2 = Color.red(color2);
        int g1 = Color.green(color1);
        int g2 = Color.green(color2);
        int b1 = Color.blue(color1);
        int b2 = Color.blue(color2);
        return Color.argb(255, (r1 / 2 + r2 / 2), (g1 / 2 + g2 / 2), (b1 / 2 + b2 / 2));
    }

    public static void setLightStatusBar(Window window, boolean enable) {
        setLightStatusBar(window, enable, false);
    }

    public static void setLightStatusBar(Window window, boolean enable, boolean forceTransparentStatusbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (enable) {
                if ((flags & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) == 0) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(flags);
                }
                int statusBarColor;
                if (!SharedConfig.noStatusBar && !forceTransparentStatusbar) {
                    statusBarColor = LIGHT_STATUS_BAR_OVERLAY;
                } else {
                    statusBarColor = Color.TRANSPARENT;
                }
                if (window.getStatusBarColor() != statusBarColor) {
                    window.setStatusBarColor(statusBarColor);
                }
            } else {
                if ((flags & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(flags);
                }
                int statusBarColor;
                if (!SharedConfig.noStatusBar && !forceTransparentStatusbar) {
                    statusBarColor = DARK_STATUS_BAR_OVERLAY;
                } else {
                    statusBarColor = Color.TRANSPARENT;
                }
                if (window.getStatusBarColor() != statusBarColor) {
                    window.setStatusBarColor(statusBarColor);
                }
            }
        }
    }

    public static boolean getLightNavigationBar(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            return (flags & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) > 0;
        }
        return false;
    }

    public static void setLightNavigationBar(View view, boolean enable) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = view.getSystemUiVisibility();
            if (enable) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            view.setSystemUiVisibility(flags);
        }
    }

    public static void setLightNavigationBar(Window window, boolean enable) {
        if (window != null) {
            setLightNavigationBar(window.getDecorView(), enable);
        }
    }

    public static void setNavigationBarColor(Window window, int color) {
        setNavigationBarColor(window, color, true);
    }

    public static void setNavigationBarColor(Window window, int color, boolean animated) {
        setNavigationBarColor(window, color, animated, null);
    }

    public static void setNavigationBarColor(Window window, int color, boolean animated, IntColorCallback onUpdate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (navigationBarColorAnimators != null) {
                ValueAnimator animator = navigationBarColorAnimators.get(window);
                if (animator != null) {
                    animator.cancel();
                    navigationBarColorAnimators.remove(window);
                }
            }

            if (!animated) {
                if (onUpdate != null) {
                    onUpdate.run(color);
                }
                try {
                    window.setNavigationBarColor(color);
                } catch (Exception ignore) {
                }
            } else {
                ValueAnimator animator = ValueAnimator.ofArgb(window.getNavigationBarColor(), color);
                animator.addUpdateListener(a -> {
                    int tcolor = (int) a.getAnimatedValue();
                    if (onUpdate != null) {
                        onUpdate.run(tcolor);
                    }
                    try {
                        window.setNavigationBarColor(tcolor);
                    } catch (Exception ignore) {
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (navigationBarColorAnimators != null) {
                            navigationBarColorAnimators.remove(window);
                        }
                    }
                });
                animator.setDuration(200);
                animator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                animator.start();
                if (navigationBarColorAnimators == null) {
                    navigationBarColorAnimators = new HashMap<>();
                }
                navigationBarColorAnimators.put(window, animator);
            }
        }
    }

    public static float computePerceivedBrightness(int color) {
        return (Color.red(color) * 0.2126f + Color.green(color) * 0.7152f + Color.blue(color) * 0.0722f) / 255f;
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (ApplicationLoader.applicationHandler == null) {
            return;
        }
        if (delay == 0) {
            ApplicationLoader.applicationHandler.post(runnable);
        } else {
            ApplicationLoader.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        if (ApplicationLoader.applicationHandler == null) {
            return;
        }
        ApplicationLoader.applicationHandler.removeCallbacks(runnable);
    }

    public static boolean isTabletForce() {
        return ApplicationLoader.applicationContext != null && ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isTablet);
    }

    public static boolean isTabletInternal() {
        if (isTablet == null) {
            isTablet = isTabletForce();
        }
        return isTablet;
    }

    public static void resetTabletFlag() {
        if (wasTablet == null) {
            wasTablet = isTabletInternal();
        }
        isTablet = null;
        //SharedConfig.updateTabletConfig();
    }

    public static void resetWasTabletFlag() {
        wasTablet = null;
    }

    public static Boolean getWasTablet() {
        return wasTablet;
    }

    public static boolean isTablet() {
        return isTabletInternal()/* && !SharedConfig.forceDisableTabletMode*/;
    }

    public static boolean isSmallScreen() {
        if (isSmallScreen == null) {
            isSmallScreen = (Math.max(displaySize.x, displaySize.y) - statusBarHeight - navigationBarHeight) / density <= 650;
        }
        return isSmallScreen;
    }

    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 690;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    public static void setScrollViewEdgeEffectColor(HorizontalScrollView scrollView, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scrollView.setEdgeEffectColor(color);
        } else if (Build.VERSION.SDK_INT >= 21) {
            try {
                Field field = HorizontalScrollView.class.getDeclaredField("mEdgeGlowLeft");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(scrollView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }

                field = HorizontalScrollView.class.getDeclaredField("mEdgeGlowRight");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(scrollView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static void setScrollViewEdgeEffectColor(ScrollView scrollView, int color) {
        if (Build.VERSION.SDK_INT >= 29) {
            scrollView.setTopEdgeEffectColor(color);
            scrollView.setBottomEdgeEffectColor(color);
        } else if (Build.VERSION.SDK_INT >= 21) {
            try {
                Field field = ScrollView.class.getDeclaredField("mEdgeGlowTop");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(scrollView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }

                field = ScrollView.class.getDeclaredField("mEdgeGlowBottom");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(scrollView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Exception ignore) {

            }
        }
    }

    public static void setTouchAnimForView(final View view, final long animDuration) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ObjectAnimator scaleX = new ObjectAnimator();
                        scaleX.setTarget(view);
                        scaleX.setPropertyName("scaleX");
                        scaleX.setFloatValues(0.9f);
                        scaleX.setDuration((int) animDuration);
                        scaleX.start();

                        ObjectAnimator scaleY = new ObjectAnimator();
                        scaleY.setTarget(view);
                        scaleY.setPropertyName("scaleY");
                        scaleY.setFloatValues(0.9f);
                        scaleY.setDuration((int) animDuration);
                        scaleY.start();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {

                        ObjectAnimator scaleX = new ObjectAnimator();
                        scaleX.setTarget(view);
                        scaleX.setPropertyName("scaleX");
                        scaleX.setFloatValues((float) 1);
                        scaleX.setDuration((int) animDuration);
                        scaleX.start();

                        ObjectAnimator scaleY = new ObjectAnimator();
                        scaleY.setTarget(view);
                        scaleY.setPropertyName("scaleY");
                        scaleY.setFloatValues((float) 1);
                        scaleY.setDuration((int) animDuration);
                        scaleY.start();

                        break;
                    }
                }
                return false;
            }
        });
        view.setClipToOutline(true);
    }

    public static SpannableStringBuilder replaceTags(String str) {
        return replaceTags(str, FLAG_TAG_ALL);
    }

    public static SpannableStringBuilder replaceTags(String str, int flag, Object... args) {
        try {
            int start;
            int end;
            StringBuilder stringBuilder = new StringBuilder(str);
            if ((flag & FLAG_TAG_BR) != 0) {
                while ((start = stringBuilder.indexOf("<br>")) != -1) {
                    stringBuilder.replace(start, start + 4, "\n");
                }
                while ((start = stringBuilder.indexOf("<br/>")) != -1) {
                    stringBuilder.replace(start, start + 5, "\n");
                }
            }
            ArrayList<Integer> bolds = new ArrayList<>();
            if ((flag & FLAG_TAG_BOLD) != 0) {
                while ((start = stringBuilder.indexOf("<b>")) != -1) {
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    if (end == -1) {
                        end = stringBuilder.indexOf("<b>");
                    }
                    stringBuilder.replace(end, end + 4, "");
                    bolds.add(start);
                    bolds.add(end);
                }
                while ((start = stringBuilder.indexOf("**")) != -1) {
                    stringBuilder.replace(start, start + 2, "");
                    end = stringBuilder.indexOf("**");
                    if (end >= 0) {
                        stringBuilder.replace(end, end + 2, "");
                        bolds.add(start);
                        bolds.add(end);
                    }
                }
            }
            if ((flag & FLAG_TAG_URL) != 0) {
                while ((start = stringBuilder.indexOf("**")) != -1) {
                    stringBuilder.replace(start, start + 2, "");
                    end = stringBuilder.indexOf("**");
                    if (end >= 0) {
                        stringBuilder.replace(end, end + 2, "");
                        bolds.add(start);
                        bolds.add(end);
                    }
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            for (int a = 0; a < bolds.size() / 2; a++) {
                spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableStringBuilder;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new SpannableStringBuilder(str);
    }

    public static String formatShortDuration(int duration) {
        return formatDuration(duration, false);
    }

    public static String formatLongDuration(int duration) {
        return formatDuration(duration, true);
    }

    public static String formatDuration(int duration, boolean isLong) {
        int h = duration / 3600;
        int m = duration / 60 % 60;
        int s = duration % 60;
        if (h == 0) {
            if (isLong) {
                return String.format(Locale.US, "%02d:%02d", m, s);
            } else {
                return String.format(Locale.US, "%d:%02d", m, s);
            }
        } else {
            return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
        }
    }

    public static String formatFullDuration(int duration) {
        int h = duration / 3600;
        int m = duration / 60 % 60;
        int s = duration % 60;
        if (duration < 0) {
            return String.format(Locale.US, "-%02d:%02d:%02d", Math.abs(h), Math.abs(m), Math.abs(s));
        } else {
            return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
        }
    }

    public static String formatDurationNoHours(int duration, boolean isLong) {
        int m = duration / 60;
        int s = duration % 60;
        if (isLong) {
            return String.format(Locale.US, "%02d:%02d", m, s);
        } else {
            return String.format(Locale.US, "%d:%02d", m, s);
        }
    }

    public static String formatShortDuration(int progress, int duration) {
        return formatDuration(progress, duration, false);
    }

    public static String formatLongDuration(int progress, int duration) {
        return formatDuration(progress, duration, true);
    }

    public static String formatDuration(int progress, int duration, boolean isLong) {
        int h = duration / 3600;
        int m = duration / 60 % 60;
        int s = duration % 60;

        int ph = progress / 3600;
        int pm = progress / 60 % 60;
        int ps = progress % 60;

        if (duration == 0) {
            if (ph == 0) {
                if (isLong) {
                    return String.format(Locale.US, "%02d:%02d / -:--", pm, ps);
                } else {
                    return String.format(Locale.US, "%d:%02d / -:--", pm, ps);
                }
            } else {
                return String.format(Locale.US, "%d:%02d:%02d / -:--", ph, pm, ps);
            }
        } else {
            if (ph == 0 && h == 0) {
                if (isLong) {
                    return String.format(Locale.US, "%02d:%02d / %02d:%02d", pm, ps, m, s);
                } else {
                    return String.format(Locale.US, "%d:%02d / %d:%02d", pm, ps, m, s);
                }
            } else {
                return String.format(Locale.US, "%d:%02d:%02d / %d:%02d:%02d", ph, pm, ps, h, m, s);
            }
        }
    }

    public static String formatVideoDuration(int progress, int duration) {
        int h = duration / 3600;
        int m = duration / 60 % 60;
        int s = duration % 60;

        int ph = progress / 3600;
        int pm = progress / 60 % 60;
        int ps = progress % 60;

        if (ph == 0 && h == 0) {
            return String.format(Locale.US, "%02d:%02d / %02d:%02d", pm, ps, m, s);
        } else {
            if (h == 0) {
                return String.format(Locale.US, "%d:%02d:%02d / %02d:%02d", ph, pm, ps, m, s);
            } else if (ph == 0) {
                return String.format(Locale.US, "%02d:%02d / %d:%02d:%02d", pm, ps, h, m, s);
            } else {
                return String.format(Locale.US, "%d:%02d:%02d / %d:%02d:%02d", ph, pm, ps, h, m, s);
            }
        }
    }

    public static String formatCount(int count) {
        if (count < 1000) return Integer.toString(count);

        ArrayList<String> strings = new ArrayList<>();
        while (count != 0) {
            int mod = count % 1000;
            count /= 1000;
            if (count > 0) {
                strings.add(String.format(Locale.ENGLISH, "%03d", mod));
            } else {
                strings.add(Integer.toString(mod));
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = strings.size() - 1; i >= 0; i--) {
            stringBuilder.append(strings.get(i));
            if (i != 0) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }

    public static File getLogsDir() {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File path = ApplicationLoader.applicationContext.getExternalFilesDir(null);
                File dir = new File(path.getAbsolutePath() + "/logs");
                dir.mkdirs();
                return dir;
            }
        } catch (Exception e) {

        }
        try {
            File dir = new File(ApplicationLoader.applicationContext.getCacheDir() + "/logs");
            dir.mkdirs();
            return dir;
        } catch (Exception e) {

        }
        try {
            File dir = new File(ApplicationLoader.applicationContext.getFilesDir() + "/logs");
            dir.mkdirs();
            return dir;
        } catch (Exception e) {

        }
        ApplicationLoader.appCenterLog(new RuntimeException("can't create logs directory"));
        return null;
    }

    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        return copyFile(sourceFile, new FileOutputStream(destFile));
    }

    public static boolean copyFile(InputStream sourceFile, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile.equals(destFile)) {
            return true;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        try (FileInputStream source = new FileInputStream(sourceFile); FileOutputStream destination = new FileOutputStream(destFile)) {
            destination.getChannel().transferFrom(source.getChannel(), 0, source.getChannel().size());
        } catch (Exception e) {
            FileLog.e(e);
            return false;
        }
        return true;
    }

    public static void appCenterLog(Throwable e) {
        ApplicationLoader.appCenterLog(e);
    }

    // detect Error NO SPaCe left on device :(
    public static boolean isENOSPC(Exception e) {
        return (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        e instanceof IOException &&
                        (e.getCause() instanceof ErrnoException &&
                                ((ErrnoException) e.getCause()).errno == OsConstants.ENOSPC) ||
                        (e.getMessage() != null && e.getMessage().equalsIgnoreCase("no space left on device"))
        );
    }

    @Keep
    public static void allowPopupIcon(androidx.appcompat.widget.PopupMenu popupMenu) {
        try {
            Field mPopup = PopupMenu.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            Object menuHelper = mPopup.get(popupMenu);
            if (menuHelper != null) {
                Method setForceIcons = menuHelper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuHelper, true);
            }
        } catch (Exception e) {
            FileLog.e(e.getMessage());
        }
    }

    public static int getOffsetColor(int color1, int color2, float offset, float alpha) {
        int rF = Color.red(color2);
        int gF = Color.green(color2);
        int bF = Color.blue(color2);
        int aF = Color.alpha(color2);
        int rS = Color.red(color1);
        int gS = Color.green(color1);
        int bS = Color.blue(color1);
        int aS = Color.alpha(color1);
        return Color.argb((int) ((aS + (aF - aS) * offset) * alpha), (int) (rS + (rF - rS) * offset), (int) (gS + (gF - gS) * offset), (int) (bS + (bF - bS) * offset));
    }

    public static Point getRealScreenSize() {
        Point size = new Point();
        try {
            WindowManager windowManager = (WindowManager) ApplicationLoader.applicationContext
                    .getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.getDefaultDisplay().getRealSize(size);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    size.set((Integer) mGetRawW.invoke(windowManager.getDefaultDisplay()),
                            (Integer) mGetRawH.invoke(windowManager.getDefaultDisplay()));
                } catch (Exception e) {
                    size.set(windowManager.getDefaultDisplay().getWidth(),
                            windowManager.getDefaultDisplay().getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static float cascade(float fullAnimationT, float position, float count, float waveLength) {
        final float waveDuration = 1f / count * waveLength;
        final float waveOffset = position / count * (1f - waveDuration);
        return MathUtils.clamp((fullAnimationT - waveOffset) / waveDuration, 0, 1);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        try {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                return;
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static ArrayList<File> getDataDirs() {
        ArrayList<File> result = null;
        if (Build.VERSION.SDK_INT >= 19) {
            File[] dirs = ApplicationLoader.applicationContext.getExternalFilesDirs(null);
            if (dirs != null) {
                for (int a = 0; a < dirs.length; a++) {
                    if (dirs[a] == null) {
                        continue;
                    }
                    String path = dirs[a].getAbsolutePath();

                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(dirs[a]);
                }
            }
        }
        if (result == null) {
            result = new ArrayList<>();
        }
        if (result.isEmpty()) {
            result.add(Environment.getExternalStorageDirectory());
        }
        return result;
    }

    public static View findChildViewUnder(ViewGroup parent, float x, float y) {
        if (parent == null) return null;
        if (parent.getVisibility() != View.VISIBLE) return null;
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View child = parent.getChildAt(i);
            if (child == null) continue;
            if (child.getVisibility() != View.VISIBLE) continue;
            if (child instanceof ViewGroup) {
                View foundChild = findChildViewUnder((ViewGroup) child, x - child.getLeft(), y - child.getTop());
                if (foundChild != null) {
                    return foundChild;
                }
            } else if (
                    x >= child.getX() && x <= child.getX() + child.getWidth() &&
                            y >= child.getY() && x <= child.getY() + child.getHeight()
            ) {
                return child;
            }
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void notifyDataSetChanged(RecyclerView listView) {
        if (listView == null) return;
        if (listView.getAdapter() == null) return;
        if (listView.isComputingLayout()) {
            listView.post(() -> {
                if (listView.getAdapter() != null) {
                    listView.getAdapter().notifyDataSetChanged();
                }
            });
        } else {
            listView.getAdapter().notifyDataSetChanged();
        }
    }

    public static int getDefault2SpanCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;

        if (screenWidthDp <= 360) {
            return 2; // Small screens (ponsel kecil)
        } else if (screenWidthDp <= 700) {
            return 3; // Medium screens (ponsel besar atau tablet kecil)
        } else if (screenWidthDp <= 1024) {
            return 4; // Large screens (tablet besar atau Chromebook kecil)
        } else {
            return 5; // Extra large screens (monitor besar atau layar ultra-wide)
        }
    }

    public static int getDefault1SpanCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;

        if (screenWidthDp <= 580) {
            return 1; // Small screens (ponsel kecil)
        } else if (screenWidthDp <= 600) {
            return 2; // Medium screens (ponsel besar atau tablet kecil)
        } else if (screenWidthDp <= 1024) {
            return 3; // Large screens (tablet besar atau Chromebook kecil)
        } else {
            return 4; // Extra large screens (monitor besar atau layar ultra-wide)
        }
    }

    public interface IntColorCallback {
        void run(int color);
    }

    public static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                boolean result = super.onTouchEvent(widget, buffer, event);
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Selection.removeSelection(buffer);
                }
                return result;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return false;
        }
    }
}

