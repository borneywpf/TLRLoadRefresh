package com.think.uiloader.data.cache.internal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by borney on 3/7/17.
 */

public class BitmapByteMapper implements ByteMapper<Bitmap> {

    BitmapByteMapper() {
        
    }

    @Override
    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    @Override
    public Bitmap getObject(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
