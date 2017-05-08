package com.think.uiloader.data.cache.internal;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 2/27/17.
 */
@SuppressLint("NewApi")
@SuppressWarnings("unused")
class FileManager {

    FileManager() {

    }

    /**
     * write byte array to file
     */
    public void writeBytes(File file, byte[] content) {
        checkNotNull(file);
        checkNotNull(content);
        Log.d("TCache", "content:" + content.length);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ByteBuffer buffer = ByteBuffer.allocate(content.length);
            FileChannel channel = fos.getChannel();
            buffer.put(content);
            buffer.flip();
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * return byte array from file
     *
     * @param file which provide content
     * @return byte array of file content
     */
    public byte[] readBytes(File file) {
        checkNotNull(file);
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            FileChannel fisChannel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int j = 0;
            byte[] result = new byte[fis.available()];
            while (fisChannel.read(buffer) > 0) {
                buffer.flip();
                for (int i = 0; i < buffer.limit(); i++) {
                    result[j++] = buffer.get();
                }
                buffer.clear();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleFile(File file) {
        checkNotNull(file);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleFile(f);
            }

        }
        file.delete();
    }

    public long calFileSize(File file) {
        if (file.isDirectory()) {
            long length = 0;
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    length += f.length();
                } else {
                    length += calFileSize(f);
                }
            }
            return length;
        } else {
            return file.length();
        }

    }

    public int calFileCount(File file) {
        return allFiles(file).size();
    }

    public List<File> allFiles(File file) {
        ArrayList<File> files = new ArrayList<>();
        allFiles(file, files);
        return files;
    }

    private void allFiles(File file, List<File> files) {
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File f : listFiles) {
                allFiles(f, files);
            }
        } else {
            files.add(file);
        }
    }

    private Object checkNotNull(Object object) {
        if (object == null) {
            throw new NullPointerException("error:object is null");
        }
        return object;
    }
}
