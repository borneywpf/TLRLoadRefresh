package com.think.uiloader.ui.mvp.contract;

import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.ui.mvp.BasePresenter;
import com.think.uiloader.ui.mvp.BaseView;

import java.util.List;

/**
 * Created by borney on 5/8/17.
 */
public interface ImageContract {
    interface View extends BaseView {
        void startImages();

        void imagesSuccess(List<ImageEntity.Image> images);

        void endImages();
    }

    interface Presenter extends BasePresenter<View> {
        void images(int startIndex, int returnNum);
    }
}
