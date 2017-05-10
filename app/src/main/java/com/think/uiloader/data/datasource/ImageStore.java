package com.think.uiloader.data.datasource;

import android.content.Context;

import com.think.uiloader.data.cache.ImageCache;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.data.net.ApiConnection;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * Created by borney on 5/8/17.
 */
public class ImageStore extends AbstractStore {
    private ImageCache mCache;

    @Inject
    ImageStore(Context context, ApiConnection api, ImageCache cache) {
        super(context, api);
        mCache = cache;
    }

    public Observable<ImageEntity> images(int fromIndex, int returnNum) {
        final String key = absKey(fromIndex, returnNum);

        if (!isThereInternetConnection()) {
            return mCache.get(key);
        }

        return api.images(fromIndex, returnNum)
                .filter(new Predicate<ImageEntity>() {
                    @Override
                    public boolean test(ImageEntity entity) throws Exception {
                        return entity != null && entity.getImgs().size() > 0;
                    }
                })
                .doOnNext(new Consumer<ImageEntity>() {
                    @Override
                    public void accept(ImageEntity entity) throws Exception {
                        mCache.put(key, entity);
                    }
                })
                .onErrorResumeNext(this.<ImageEntity>errorResumeNext());
    }

    private String absKey(int index, int num) {
        StringBuilder sb = new StringBuilder();
        sb.append("images");
        sb.append(File.separator);
        sb.append(index);
        sb.append(File.separator);
        sb.append(num);
        return sb.toString();
    }
}
