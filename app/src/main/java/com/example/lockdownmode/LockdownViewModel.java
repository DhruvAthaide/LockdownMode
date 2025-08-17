package com.example.lockdownmode;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LockdownViewModel extends ViewModel {

    private LockdownRepository repo;
    private MutableLiveData<Boolean> isLockdownActive = new MutableLiveData<>(false);

    public LockdownViewModel(LockdownRepository repo) {
        this.repo = repo;
        this.isLockdownActive.setValue(repo.isLockdownActive());
    }

    public LiveData<Boolean> getIsLockdownActive() {
        return isLockdownActive;
    }

    public void activateLockdown(Context context) {
        repo.activateLockdown(context);
        isLockdownActive.setValue(true);
    }
    public void exitLockdown(Context context, String pin) {
        repo.exitLockdown(context, pin);
        isLockdownActive.setValue(false);
    }
}