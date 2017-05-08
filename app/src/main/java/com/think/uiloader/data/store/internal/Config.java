package com.think.uiloader.data.store.internal;

import java.util.Properties;

/**
 * Created by borney on 3/21/17.
 */

public interface Config {
    void setProperty(String key, String value);

    String getProperty(String key);

    void setProps(Properties properties);

    Properties getProps();
}
