package com.example.transfer;

import android.app.Application;
import android.content.Context;

/**
 * Created on 2020/3/13
 *
 * @author ZhangMingli
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
