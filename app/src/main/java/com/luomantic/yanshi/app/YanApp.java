package com.luomantic.yanshi.app;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

public class YanApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this);
    }

}
