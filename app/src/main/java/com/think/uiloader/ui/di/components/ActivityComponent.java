package com.think.uiloader.ui.di.components;


import com.think.uiloader.ui.TLRListActivity;
import com.think.uiloader.ui.TLRKeepHeadActivity;
import com.think.uiloader.ui.TLRRecyclerActivity;
import com.think.uiloader.ui.di.AScope;

import dagger.Component;

/**
 * Created by borney on 3/13/17.
 */

@AScope
@Component(dependencies = {ApplicationComponent.class})
public interface ActivityComponent {
    void inject(TLRListActivity activity);
    void inject(TLRRecyclerActivity activity);
    void inject(TLRKeepHeadActivity activity);
}
