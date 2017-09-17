package com.bureau.nocomment.globes.application;

import android.app.Application;
import android.content.Context;

public class Globes extends Application {
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
    }

    public static synchronized Context getAppContext() {
        return mAppContext;
    }
}
