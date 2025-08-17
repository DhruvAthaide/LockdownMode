package com.example.lockdownmode;

import android.app.Application;
import com.example.lockdownmode.di.Injector;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Injector.init(this);
    }
}