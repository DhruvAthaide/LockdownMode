package com.example.lockdownmode;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;

public class LockdownDeviceAdminReceiver extends DeviceAdminReceiver {
    public static ComponentName getComponentName(Context ctx) {
        return new ComponentName(ctx, LockdownDeviceAdminReceiver.class);
    }
}