package org.bytedata.manager.utils;

/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

import android.util.Log;

import com.google.gson.Gson;

import org.bytedata.manager.ApplicationLoader;
import org.bytedata.manager.LaunchActivity;
import org.bytedata.manager.utils.time.FastDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class FileLog {
    private final static String tag = "log";
    private final static String mtproto_tag = "MTProto";
    public static boolean databaseIsMalformed = false;
    private static volatile FileLog Instance = null;
    private static Gson gson;
    private static HashSet<String> excludeRequests;
    private OutputStreamWriter streamWriter = null;
    private FastDateFormat dateFormat = null;
    private DispatchQueue logQueue = null;
    private File currentFile = null;
    private File networkFile = null;
    private File tonlibFile = null;
    private boolean initied;
    private OutputStreamWriter tlStreamWriter = null;
    private File tlRequestsFile = null;

    public FileLog() {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        init();
    }

    public static FileLog getInstance() {
        FileLog localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLog.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileLog();
                }
            }
        }
        return localInstance;
    }

    public static void ensureInitied() {
        getInstance().init();
    }

    public static String getNetworkLogPath() {
        if (!BuildVars.LOGS_ENABLED) {
            return "";
        }
        try {
            File dir = AndroidUtilities.getLogsDir();
            if (dir == null) {
                return "";
            }
            getInstance().networkFile = new File(dir, getInstance().dateFormat.format(System.currentTimeMillis()) + "_net.txt");
            return getInstance().networkFile.getAbsolutePath();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTonlibLogPath() {
        if (!BuildVars.LOGS_ENABLED) {
            return "";
        }
        try {
            File dir = AndroidUtilities.getLogsDir();
            if (dir == null) {
                return "";
            }
            getInstance().tonlibFile = new File(dir, getInstance().dateFormat.format(System.currentTimeMillis()) + "_tonlib.txt");
            return getInstance().tonlibFile.getAbsolutePath();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void e(final String message, final Throwable exception) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.e(tag, message, exception);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + message + "\n");
                    getInstance().streamWriter.write(exception.toString());
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.e(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final Throwable e) {
        e(e, true);
    }

    public static void e(final Throwable e, boolean logToAppCenter) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        if (BuildVars.DEBUG_VERSION && needSent(e) && logToAppCenter) {
            AndroidUtilities.appCenterLog(e);
        }
        if (BuildVars.DEBUG_VERSION && e.getMessage() != null && e.getMessage().contains("disk image is malformed") && !databaseIsMalformed) {
            FileLog.d("copy malformed files");
            databaseIsMalformed = true;
            File filesDir = ApplicationLoader.getFilesDirFixed();
            filesDir = new File(filesDir, "malformed_database/");
            filesDir.mkdirs();
            ArrayList<File> malformedFiles = MessagesStorage.getInstance(UserConfig.selectedAccount).getDatabaseFiles();
            for (int i = 0; i < malformedFiles.size(); i++) {
                try {
                    AndroidUtilities.copyFile(malformedFiles.get(i), new File(filesDir, malformedFiles.get(i).getName()));
                } catch (IOException ex) {
                    FileLog.e(ex);
                }
            }
        }
        ensureInitied();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {

                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + e + "\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for (int a = 0; a < stack.length; a++) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + stack[a] + "\n");
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            e.printStackTrace();
        }
    }

    public static void fatal(final Throwable e) {
        fatal(e, true);
    }

    public static void fatal(final Throwable e, boolean logToAppCenter) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        if (BuildVars.DEBUG_VERSION && needSent(e) && logToAppCenter) {
            AndroidUtilities.appCenterLog(e);
        }
        ensureInitied();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + e + "\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for (int a = 0; a < stack.length; a++) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/tmessages: " + stack[a] + "\n");
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    System.exit(2);
                }
            });
        } else {
            e.printStackTrace();
            if (BuildVars.DEBUG_PRIVATE_VERSION) {
                System.exit(2);
            }
        }
    }

    private static boolean needSent(Throwable e) {
        return !(e instanceof InterruptedException) /*|| e instanceof MediaCodecVideoConvertor.ConversionCanceledException*/ && !(e instanceof IgnoreSentException);
    }

    public static void d(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.d(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " D/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (AndroidUtilities.isENOSPC(e)) {
                        LaunchActivity.checkFreeDiscSpaceStatic(1);
                    }
                }
            });
        }
    }

    public static void w(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.w(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " W/tmessages: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void cleanupLogs() {
        ensureInitied();
        File dir = AndroidUtilities.getLogsDir();
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (int a = 0; a < files.length; a++) {
                File file = files[a];
                if (getInstance().currentFile != null && file.getAbsolutePath().equals(getInstance().currentFile.getAbsolutePath())) {
                    continue;
                }
                if (getInstance().networkFile != null && file.getAbsolutePath().equals(getInstance().networkFile.getAbsolutePath())) {
                    continue;
                }
                if (getInstance().tonlibFile != null && file.getAbsolutePath().equals(getInstance().tonlibFile.getAbsolutePath())) {
                    continue;
                }
                file.delete();
            }
        }
    }

    public void init() {
        if (initied) {
            return;
        }
        dateFormat = FastDateFormat.getInstance("dd_MM_yyyy_HH_mm_ss", Locale.US);
        String date = dateFormat.format(System.currentTimeMillis());
        try {
            File dir = AndroidUtilities.getLogsDir();
            if (dir == null) {
                return;
            }
            currentFile = new File(dir, date + ".txt");
            tlRequestsFile = new File(dir, date + "_mtproto.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logQueue = new DispatchQueue("logQueue");
            currentFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(currentFile);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("-----start log " + date + "-----\n");
            streamWriter.flush();

            FileOutputStream tlStream = new FileOutputStream(tlRequestsFile);
            tlStreamWriter = new OutputStreamWriter(tlStream);
            tlStreamWriter.write("-----start log " + date + "-----\n");
            tlStreamWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initied = true;
    }

    public static class IgnoreSentException extends Exception {

        public IgnoreSentException(String e) {
            super(e);
        }

    }
}
