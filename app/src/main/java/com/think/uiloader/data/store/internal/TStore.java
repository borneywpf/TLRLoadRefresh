package com.think.uiloader.data.store.internal;

import android.content.Context;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by borney on 3/21/17.
 */

public class TStore implements Store {
    private static final String DEF_PREFERENCE_NAME = "def_preference";
    private static final String DEF_CONFIG_NAME = "def_config";
    private static final Map<String, Preference> PREFERENCE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Config> CONFIG_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Obj> OBJECT_MAP = new ConcurrentHashMap<>();
    private static Store STORE;

    private TStore(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Store get(Context context) {
        if (STORE == null) {
            synchronized (TStore.class) {
                if (STORE == null) {
                    STORE = new TStore(context);
                }
            }
        }
        return STORE;
    }

    private Context mContext;

    @Override
    public void set(String key, int value) {
        getPreference(DEF_PREFERENCE_NAME).set(key, value);
    }

    @Override
    public int get(String key, int def) {
        return getPreference(DEF_PREFERENCE_NAME).get(key, def);
    }

    @Override
    public void set(String key, float value) {
        getPreference(DEF_PREFERENCE_NAME).set(key, value);
    }

    @Override
    public float get(String key, float def) {
        return getPreference(DEF_PREFERENCE_NAME).get(key, def);
    }

    @Override
    public void set(String key, long value) {
        getPreference(DEF_PREFERENCE_NAME).set(key, value);
    }

    @Override
    public long get(String key, long def) {
        return getPreference(DEF_PREFERENCE_NAME).get(key, def);
    }

    @Override
    public void set(String key, boolean value) {
        getPreference(DEF_PREFERENCE_NAME).set(key, value);
    }

    @Override
    public boolean get(String key, boolean def) {
        return getPreference(DEF_PREFERENCE_NAME).get(key, def);
    }

    @Override
    public void set(String key, String value) {
        getPreference(DEF_PREFERENCE_NAME).set(key, value);
    }

    @Override
    public String get(String key, String def) {
        return getPreference(DEF_PREFERENCE_NAME).get(key, def);
    }

    @Override
    public void setProperty(String key, String value) {
        getConfig(DEF_CONFIG_NAME).setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return getConfig(DEF_CONFIG_NAME).getProperty(key);
    }

    @Override
    public void setProps(Properties properties) {
        getConfig(DEF_CONFIG_NAME).setProps(properties);
    }

    @Override
    public Properties getProps() {
        return getConfig(DEF_CONFIG_NAME).getProps();
    }

    @Override
    public Preference getPreference(String name) {
        Preference preference = PREFERENCE_MAP.get(name);
        if (preference == null) {
            preference = new PreferenceImpl(mContext, name);
            PREFERENCE_MAP.put(name, preference);
        }
        return preference;
    }

    @Override
    public Config getConfig(String name) {
        Config config = CONFIG_MAP.get(name);
        if (config == null) {
            config = new ConfigImpl(mContext, name);
            CONFIG_MAP.put(name, config);
        }
        return config;
    }

    @Override
    public <T extends Serializable> Obj<T> getObject(String name) {
        Obj<T> obj = OBJECT_MAP.get(name);
        if (obj == null) {
            obj = new ObjImpl(mContext, name);
            OBJECT_MAP.put(name, obj);
        }
        return obj;
    }
}
