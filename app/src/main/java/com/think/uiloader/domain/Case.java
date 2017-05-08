package com.think.uiloader.domain;


import com.think.uiloader.data.executor.PostExecutionThread;
import com.think.uiloader.data.executor.ThreadExecutor;

import dagger.internal.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by borney on 2/15/17.
 */

public class Case {

    private final ThreadExecutor threadExecutor;
    private final PostExecutionThread postExecutionThread;
    private final CompositeDisposable disposables;

    protected Case(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
        this.disposables = new CompositeDisposable();
    }

    /**
     * Executes the current use case.
     */
    <T> void execute(Observable<T> observable, DisposableObserver<T> observer) {
        Preconditions.checkNotNull(observer);
        Preconditions.checkNotNull(observable);
        Scheduler postExecutionThreadScheduler = postExecutionThread.getScheduler();
        Scheduler scheduler = Schedulers.from(threadExecutor);
        DisposableObserver<T> disposableObserver = observable
                .subscribeOn(scheduler)
                .observeOn(postExecutionThreadScheduler)
                .subscribeWith(observer);
        addDisposable(disposableObserver);
    }

    /**
     * Dispose from current {@link CompositeDisposable}.
     */
    public void dispose() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    /**
     * Dispose from current {@link CompositeDisposable}.
     */
    private void addDisposable(Disposable disposable) {
        Preconditions.checkNotNull(disposable);
        Preconditions.checkNotNull(disposables);
        disposables.add(disposable);
    }
}
