package com.think.uiloader.data.store.internal;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by borney on 3/21/17.
 */

@SuppressLint("NewApi")
class ObjImpl<T extends Serializable> implements Obj<T> {
    private static final String APP_OBJECT = "app_obj";
    private Context mContext;
    private File mObjFile;

    ObjImpl(Context context, String objFileName) {
        mContext = context;
        mObjFile = new File(mContext.getDir(APP_OBJECT, Context.MODE_PRIVATE), objFileName);
    }

    @Override
    public void set(T obj) {
        try (FileOutputStream fos = new FileOutputStream(mObjFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(obj);
            oos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T get() {
        try (FileInputStream fis = new FileInputStream(mObjFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (T) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            if (e instanceof InvalidClassException) {
                mObjFile.delete();
            }
        }
        return null;
    }
}
