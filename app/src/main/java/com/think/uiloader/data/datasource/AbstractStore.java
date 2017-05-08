package com.think.uiloader.data.datasource;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.think.uiloader.data.net.ApiConnection;
import com.think.uiloader.data.store.internal.Store;
import com.think.uiloader.data.store.internal.TStore;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * Created by borney on 2/17/17.
 */

abstract class AbstractStore {
    private final String TAG = getClass().getSimpleName();
    Context context;
    ApiConnection api;
    Store store;

    AbstractStore(Context context, ApiConnection api) {
        this.context = context;
        this.api = api;
        this.store = TStore.get(context);
    }

    boolean isThereInternetConnection() {
        boolean isConnected;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());

        return isConnected;
    }

    <T> Function<Throwable, ObservableSource<T>> errorResumeNext () {
        return new Function<Throwable, ObservableSource<T>>() {
            @Override
            public ObservableSource<T> apply(
                    Throwable throwable) throws Exception {
                return (ObservableSource<T>) Observable.empty();
            }
        };
    }

    boolean isEmpty(String s) {
        return TextUtils.isEmpty(s);
    }
}
