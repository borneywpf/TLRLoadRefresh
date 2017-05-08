package com.think.uiloader;

import android.app.Application;

import com.think.uiloader.ui.di.components.ApplicationComponent;
import com.think.uiloader.ui.di.components.DaggerApplicationComponent;
import com.think.uiloader.ui.di.modules.ApplicationModule;

/**
 * Created by borney on 5/8/17.
 */
public class App extends Application {
    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
