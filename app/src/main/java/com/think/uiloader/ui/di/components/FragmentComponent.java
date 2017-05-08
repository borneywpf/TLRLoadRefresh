package com.think.uiloader.ui.di.components;


import com.think.uiloader.ui.di.AScope;

import dagger.Component;

/**
 * Created by borney on 2/22/17.
 */
@AScope
@Component(dependencies = ApplicationComponent.class)
public interface FragmentComponent {
}
