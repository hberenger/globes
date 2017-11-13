package com.bureau.nocomment.globes.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.bureau.nocomment.globes.model.ModelRepository;
import com.bureau.nocomment.globes.service.KioskService;

public class Globes extends Application {
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        ModelRepository.getInstance().loadItemLibrary(mAppContext);
        ModelRepository.getInstance().loadProjectPictograms(this);

        startService(new Intent(this, KioskService.class)); // start KioskService
    }

    public static synchronized Context getAppContext() {
        return mAppContext;
    }
}
