package com.example.lockdownmode.di;

import android.content.Context;

import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.LockdownRepository;
import com.example.lockdownmode.LockdownViewModel;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

public class Injector {

    private static SecurePrefs prefs;

    public static void init(Context ctx) {
        if (prefs == null) prefs = new SecurePrefs(ctx);
    }

    public static SecurePrefs providePrefs(Context ctx) {
        if (prefs == null) init(ctx);
        return prefs;
    }

    public static LockdownRepository provideRepo(Context ctx) {
        return new LockdownRepository(providePrefs(ctx));
    }

    public static ViewModelProvider.Factory provideVMMFactory(Context ctx) {
        return new ViewModelProvider.NewInstanceFactory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new LockdownViewModel(provideRepo(ctx));
            }
        };
    }
}