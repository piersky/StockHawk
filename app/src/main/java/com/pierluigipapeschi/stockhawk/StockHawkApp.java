package com.pierluigipapeschi.stockhawk;

import android.app.Application;

import timber.log.Timber;

public class StockHawkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree(){
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + "[" + element.getLineNumber() + "]";
                }
            });
        }
    }
}
