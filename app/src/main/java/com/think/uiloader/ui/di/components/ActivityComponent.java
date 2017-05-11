package com.think.uiloader.ui.di.components;


import com.think.uiloader.ui.RAutoRefreshActivity;
import com.think.uiloader.ui.RCannotMoveHeadByTLRActivity;
import com.think.uiloader.ui.RListViewActivity;
import com.think.uiloader.ui.RKeepHeadActivity;
import com.think.uiloader.ui.RRecyclerViewActivity;
import com.think.uiloader.ui.RRefreshMaxMoveDistanceActivity;
import com.think.uiloader.ui.di.AScope;

import dagger.Component;

/**
 * Created by borney on 3/13/17.
 */

@AScope
@Component(dependencies = {ApplicationComponent.class})
public interface ActivityComponent {
    void inject(RListViewActivity activity);
    void inject(RRecyclerViewActivity activity);
    void inject(RKeepHeadActivity activity);
    void inject(RCannotMoveHeadByTLRActivity activity);
    void inject(RAutoRefreshActivity activity);
    void inject(RRefreshMaxMoveDistanceActivity activity);
}
