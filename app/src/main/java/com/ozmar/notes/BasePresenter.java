package com.ozmar.notes;

import android.support.annotation.NonNull;


public interface BasePresenter<V> {

    void attachView(@NonNull V view);

    void detachView();

    boolean isViewAttached();

}
