package org.bytedata.manager.utils;

import org.bytedata.manager.ApplicationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MessagesStorage extends BaseController {

    private static final MessagesStorage[] Instance = new MessagesStorage[UserConfig.MAX_ACCOUNT_COUNT];
    private static final Object[] lockObjects = new Object[UserConfig.MAX_ACCOUNT_COUNT];

    static {
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            lockObjects[i] = new Object();
        }
    }

    private final DispatchQueue storageQueue;
    private int mainUnreadCount;
    private File cacheFile;
    private File walCacheFile;
    private File shmCacheFile;

    public MessagesStorage(int instance) {
        super(instance);
        storageQueue = new DispatchQueue("storageQueue_" + instance);
        storageQueue.postRunnable(() -> openDatabase(1));
    }

    public static MessagesStorage getInstance(int num) {
        MessagesStorage localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (lockObjects[num]) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new MessagesStorage(num);
                }
            }
        }
        return localInstance;
    }

    public DispatchQueue getStorageQueue() {
        return storageQueue;
    }

    public int getMainUnreadCount() {
        return mainUnreadCount;
    }

    public ArrayList<File> getDatabaseFiles() {
        ArrayList<File> files = new ArrayList<>();
        files.add(cacheFile);
        files.add(walCacheFile);
        files.add(shmCacheFile);
        return files;
    }

    public void openDatabase(int openTries) {
        File filesDir = ApplicationLoader.getFilesDirFixed();
        if (currentAccount != 0) {
            filesDir = new File(filesDir, "account" + currentAccount + "/");
            filesDir.mkdirs();
        }
        cacheFile = new File(filesDir, "cache4.db");
        walCacheFile = new File(filesDir, "cache4.db-wal");
        shmCacheFile = new File(filesDir, "cache4.db-shm");
    }

    public interface IntCallback {
        void run(int param);
    }

    public interface LongCallback {
        void run(long param);
    }

    public interface StringCallback {
        void run(String param);
    }

    public interface BooleanCallback {
        void run(boolean param);
    }

    public static class TopicKey {
        public long dialogId;
        public int topicId;

        public static TopicKey of(long dialogId, int topicId) {
            TopicKey topicKey = new TopicKey();
            topicKey.dialogId = dialogId;
            topicKey.topicId = topicId;
            return topicKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TopicKey topicKey = (TopicKey) o;
            return dialogId == topicKey.dialogId && topicId == topicKey.topicId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dialogId, topicId);
        }
    }
}
