package com.think.uiloader.data.store.internal;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by borney on 3/21/17.
 */

@SuppressLint("NewApi")
class ConfigImpl implements Config {
    private static final String APP_CONFIG = "app_config";
    private Context mContext;
    private File mConfigFile;

    ConfigImpl(Context context, String configFileName) {
        mContext = context;
        mConfigFile = new File(mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE), configFileName);
    }

    @Override
    public void setProperty(String key, String value) {
        Properties props = getProps();
        props.setProperty(key, value);
        setProps(props);
    }

    @Override
    public String getProperty(String key) {
        Properties props = getProps();
        return props.getProperty(key);
    }

    @Override
    public void setProps(Properties properties) {

        try (FileOutputStream fos = new FileOutputStream(mConfigFile)) {
            properties.store(fos, null);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Properties getProps() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(mConfigFile)) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
