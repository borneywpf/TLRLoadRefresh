package com.think.uiloader.data.net;

import com.think.uiloader.data.entity.ImageEntity;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by borney on 2/14/17.
 */

public interface ApiClient {
    @GET(Api.IMAGE_URL)
    Observable<ImageEntity> images(@Query("pn")int fromIndex, @Query("rn") int returnNum);
}
