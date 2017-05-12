package com.think.uiloader.ui.di.components;


import com.think.uiloader.ui.LListViewActivity;
import com.think.uiloader.ui.RAutoRefreshActivity;
import com.think.uiloader.ui.RCannotMoveHeadByTLRActivity;
import com.think.uiloader.ui.RGridViewActivity;
import com.think.uiloader.ui.RKeepContentActivity;
import com.think.uiloader.ui.RLListViewActivity;
import com.think.uiloader.ui.RListViewActivity;
import com.think.uiloader.ui.RMaterialHeadActivity;
import com.think.uiloader.ui.RMaterialHeadKeepContentActivity;
import com.think.uiloader.ui.RNotKeepHeadActivity;
import com.think.uiloader.ui.ROtherLibraryActivity;
import com.think.uiloader.ui.RRecyclerViewActivity;
import com.think.uiloader.ui.RRefreshMaxMoveDistanceActivity;
import com.think.uiloader.ui.TLRMultiContentActivity;
import com.think.uiloader.ui.TLRSunofOtherActivity;
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
    void inject(RNotKeepHeadActivity activity);
    void inject(RCannotMoveHeadByTLRActivity activity);
    void inject(RAutoRefreshActivity activity);
    void inject(RRefreshMaxMoveDistanceActivity activity);
    void inject(RGridViewActivity activity);
    void inject(ROtherLibraryActivity activity);
    void inject(TLRMultiContentActivity activity);
    void inject(TLRSunofOtherActivity activity);
    void inject(LListViewActivity activity);
    void inject(RLListViewActivity activity);
    void inject(RMaterialHeadActivity activity);
    void inject(RMaterialHeadKeepContentActivity activity);
    void inject(RKeepContentActivity activity);
}
