package com.think.uiloader.data.store.internal;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by borney on 3/21/17.
 */

class PreferenceImpl implements Preference {
    private SharedPreferences mPreferences;

    PreferenceImpl(Context context, String name) {
        mPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Override
    public void set(String key, int value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    @Override
    public int get(String key, int def) {
        return mPreferences.getInt(key, def);
    }

    @Override
    public void set(String key, float value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putFloat(key, value);
        edit.apply();
    }

    @Override
    public float get(String key, float def) {
        return mPreferences.getFloat(key, def);
    }

    @Override
    public void set(String key, long value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    @Override
    public long get(String key, long def) {
        return mPreferences.getLong(key, def);
    }

    @Override
    public void set(String key, boolean value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    @Override
    public boolean get(String key, boolean def) {
        return mPreferences.getBoolean(key, def);
    }

    @Override
    public void set(String key, String value) {
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(key, value);
        edit.apply();
    }

    @Override
    public String get(String key, String def) {
        return mPreferences.getString(key, def);
    }
}
