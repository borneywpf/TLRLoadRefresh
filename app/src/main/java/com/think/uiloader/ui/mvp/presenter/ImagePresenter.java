package com.think.uiloader.ui.mvp.presenter;

import com.google.common.base.Optional;
import com.think.tlr.TLRLog;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.domain.DefaultObserver;
import com.think.uiloader.domain.ImageCase;
import com.think.uiloader.ui.mvp.contract.ImageContract;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

/**
 * Created by borney on 5/8/17.
 */
public class ImagePresenter implements ImageContract.Presenter {
    private ImageCase mImageCase;
    private WeakReference<ImageContract.View> mView;

    @Inject
    ImagePresenter(ImageCase imageCase) {
        mImageCase = imageCase;
    }


    @Override
    public void images(int startIndex, int returnNum) {
        if (getView().isPresent()) {
            getView().get().startImages();
        }
        mImageCase.images(startIndex, returnNum, new DefaultObserver<ImageEntity>() {
            @Override
            public void onNext(ImageEntity entity) {
                TLRLog.v("-----------------------get images success!!!-------------------");
                if (getView().isPresent()) {
                    getView().get().imagesSuccess(entity.getImgs());
                }
            }

            @Override
            public void onComplete() {
                if (getView().isPresent()) {
                    getView().get().endImages();
                }
            }

            @Override
            public void onError(Throwable exception) {
                if (getView().isPresent()) {
                    getView().get().error(0);
                }
            }
        });
    }

    @Override
    public void setView(ImageContract.View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    private Optional<ImageContract.View> getView() {
        return Optional.fromNullable(mView.get());
    }
}
