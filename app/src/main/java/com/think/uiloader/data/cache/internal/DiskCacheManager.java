package com.think.uiloader.data.cache.internal;

import android.annotation.SuppressLint;

import java.io.File;
import java.util.List;

/**
 * Created by borney on 3/1/17.
 */
@SuppressLint("NewApi")
class DiskCacheManager implements Cache {
    private FileManager fileManager;
    private String cacheDir;
    private int maxCount;
    private int maxSpace;
    private long age;

    DiskCacheManager(FileManager fileManager, String cacheDir, int maxCount, int maxSpace,
            long age) {
        this.fileManager = fileManager;
        this.cacheDir = cacheDir;
        this.maxCount = maxCount;
        this.maxSpace = maxSpace;
        this.age = age;
    }

    @Override
    public <T> void putByteMapper(String key, T obj, ByteMapper<T> mapper) {
        //ensure total file count and space
        byte[] bytes = mapper.getBytes(obj);
        ensureTotalCount();
        ensureTotalSpace(bytes);

        //create cache file
        File file = buildFile(key);
        createNotExistsParent(file);

        //write data to cache file
        fileManager.writeBytes(file, bytes);

        //update file and it's parent list modify time
        updateLastModified(file, System.currentTimeMillis());
    }

    @Override
    public <T> T getByteMapper(String key, ByteMapper<T> mapper) {
        File file = buildFile(key);
        if (file.exists()) {
            byte[] bytes = fileManager.readBytes(file);
            return mapper.getObject(bytes);
        } else {
            return null;
        }
    }

    @Override
    public boolean isExpired(String key) {
        return isExpired(key, age);
    }

    @Override
    public boolean isExpired(String key, long age) {
        File file = buildFile(key);
        return System.currentTimeMillis() - file.lastModified() > age;
    }

    @Override
    public void evict(String key) {
        File file = buildFile(key);
        fileManager.deleFile(file);
    }

    @Override
    public void evictAll() {
        fileManager.deleFile(new File(cacheDir));
    }

    @Override
    public boolean isCached(String key) {
        File file = buildFile(key);
        return file.exists() && fileManager.calFileSize(file) != 0;
    }

    private void ensureTotalSpace(byte[] bytes) {
        int objSize = bytes.length;
        File file = new File(cacheDir);
        long cacheSize = fileManager.calFileSize(file);
        while (cacheSize + objSize > maxSpace) {
            long removeSize = removeLastModifiedFile(file);
            cacheSize -= removeSize;
        }
    }

    private void ensureTotalCount() {
        File file = new File(cacheDir);
        int size = fileManager.calFileCount(file);
        while (size > maxCount) {
            removeLastModifiedFile(file);
            size--;
        }
    }

    private File buildFile(String key) {
        return new File(key);
    }

    private void createNotExistsParent(File file) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    private void updateLastModified(File file, long time) {
        if (file.getPath().equals(cacheDir)) {
            return;
        }
        file.setLastModified(time);
        updateLastModified(file.getParentFile(), time);
    }

    private long removeLastModifiedFile(File parent) {
        List<File> files = fileManager.allFiles(parent);
        if (!files.isEmpty()) {
            File lastModifyFile = files.get(0);
            long removedSize = lastModifyFile.length();
            for (File f : files) {
                if (f.lastModified() < lastModifyFile.lastModified()) {
                    lastModifyFile = f;
                    removedSize = lastModifyFile.length();
                }
            }
            lastModifyFile.delete();
            return removedSize;
        }
        return 0;
    }
}
