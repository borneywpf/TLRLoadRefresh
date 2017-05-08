package com.think.uiloader.data.net;

import com.think.uiloader.data.entity.ImageEntity;

import javax.inject.Inject;

import io.reactivex.Observable;
import retrofit2.Retrofit;

/**
 * Created by borney on 2/15/17.
 */

public class ApiConnection implements ApiClient, Api {
    private ApiClient apiClient;

    @Inject
    ApiConnection(Retrofit retrofit) {
        apiClient = retrofit.create(ApiClient.class);
    }

    @Override
    public Observable<ImageEntity> images(int fromIndex, int returnNum) {
        return apiClient.images(fromIndex, returnNum);
    }
}
