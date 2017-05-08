package com.think.uiloader.domain;

import com.think.uiloader.data.datasource.ImageStore;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.data.executor.PostExecutionThread;
import com.think.uiloader.data.executor.ThreadExecutor;

import javax.inject.Inject;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by borney on 5/8/17.
 */
public class ImageCase extends Case {
    private ImageStore mImageStore;

    @Inject
    protected ImageCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, ImageStore store) {
        super(threadExecutor, postExecutionThread);
        mImageStore = store;
    }

    public void images(int fromIndex, int returnNum, DisposableObserver<ImageEntity> observer) {
        execute(mImageStore.images(fromIndex, returnNum), observer);
    }
}
