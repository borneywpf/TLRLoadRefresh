package com.think.uiloader.ui.di.components;


import com.think.uiloader.ui.AutoRefreshActivity;
import com.think.uiloader.ui.CannotMoveHeadByTLRActivity;
import com.think.uiloader.ui.ListViewActivity;
import com.think.uiloader.ui.KeepHeadActivity;
import com.think.uiloader.ui.RecyclerViewActivity;
import com.think.uiloader.ui.RefreshMaxMoveDistanceActivity;
import com.think.uiloader.ui.di.AScope;

import dagger.Component;

/**
 * Created by borney on 3/13/17.
 */

@AScope
@Component(dependencies = {ApplicationComponent.class})
public interface ActivityComponent {
    void inject(ListViewActivity activity);
    void inject(RecyclerViewActivity activity);
    void inject(KeepHeadActivity activity);
    void inject(CannotMoveHeadByTLRActivity activity);
    void inject(AutoRefreshActivity activity);
    void inject(RefreshMaxMoveDistanceActivity activity);
}
