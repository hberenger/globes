package com.bureau.nocomment.globes.application;

import android.app.Application;
import android.content.Context;

import com.bureau.nocomment.globes.model.ModelRepository;

public class Globes extends Application {
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        ModelRepository.getInstance().loadItemLibrary(mAppContext);
    }

    public static synchronized Context getAppContext() {
        return mAppContext;
    }
}
