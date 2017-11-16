package com.bureau.nocomment.globes.application;

import android.app.Application;
import android.content.Context;

import com.bureau.nocomment.globes.model.ModelRepository;

public class Globes extends Application {

    public static final long kRESET_DELAY = 15*60*1000; // reset app if not used during 15 minutes

    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        ModelRepository.getInstance().loadItemLibrary(mAppContext);
        ModelRepository.getInstance().loadProjectPictograms(this);
    }

    public static synchronized Context getAppContext() {
        return mAppContext;
    }
}
