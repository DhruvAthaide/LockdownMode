package com.example.lockdownmode;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.util.PermissionUtils;
import java.util.ArrayList;
import java.util.List;

public class LockdownRepository {

    private final SecurePrefs prefs;

    public LockdownRepository(SecurePrefs prefs) {
        this.prefs = prefs;
    }

    public void activateLockdown(Context ctx) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = LockdownDeviceAdminReceiver.getComponentName(ctx);

            if (dpm.isAdminActive(admin)) {
                if (prefs.getDisableCamera()) {
                    dpm.setCameraDisabled(admin, true);
                }

                if (prefs.getDisableMicrophone() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<String> appsWithMicPermission = getAppsWithRecordAudioPermission(ctx);
                    if (!appsWithMicPermission.isEmpty()) {
                        dpm.setPackagesSuspended(admin, appsWithMicPermission.toArray(new String[0]), true);
                    }
                }
            }

            if (prefs.getDisableGps()) {
                PermissionUtils.disableLocationServices(ctx);
            }

            if (prefs.getMinimalLauncher()) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(ctx, LockdownLauncherActivity.class);
                ctx.startActivity(intent);
            }

            if (prefs.getClearRecentApps() || prefs.getKillBackgroundProcesses()) {
                Intent accIntent = new Intent(ctx, LockdownAccessibilityService.class);
                if (prefs.getClearRecentApps()) {
                    accIntent.setAction("CLEAR_RECENTS");
                    ctx.startService(accIntent);
                }
                if (prefs.getKillBackgroundProcesses()) {
                    accIntent.setAction("CLOSE_BG_APPS");
                    ctx.startService(accIntent);
                }
            }

            // Send SMS to emergency contact if set
            String emergencyContact = prefs.getEmergencyContact();
            if (emergencyContact != null && !emergencyContact.isEmpty()) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(emergencyContact, null, "Emergency: Lockdown mode activated on device.", null, null);
            }

            prefs.setLockdownActive(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitLockdown(Context ctx, String enteredPin) {
        if (!prefs.isPinCorrect(enteredPin)) return;

        try {
            DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = LockdownDeviceAdminReceiver.getComponentName(ctx);

            if (dpm.isAdminActive(admin)) {
                if (prefs.getDisableCamera()) {
                    dpm.setCameraDisabled(admin, false);
                }

                if (prefs.getDisableMicrophone() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<String> appsWithMicPermission = getAppsWithRecordAudioPermission(ctx);
                    if (!appsWithMicPermission.isEmpty()) {
                        dpm.setPackagesSuspended(admin, appsWithMicPermission.toArray(new String[0]), false);
                    }
                }
            }

            if (prefs.getDisableGps()) {
                PermissionUtils.enableLocationServices(ctx);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(ctx, MainActivity.class);
            ctx.startActivity(intent);

            prefs.setLockdownActive(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLockdownActive() {
        return prefs.getLockdownActive();
    }

    private List<String> getAppsWithRecordAudioPermission(Context ctx) {
        List<String> appsWithPermission = new ArrayList<>();
        PackageManager pm = ctx.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkg : packages) {
            if (pkg.requestedPermissions != null) {
                for (String permission : pkg.requestedPermissions) {
                    if ("android.permission.RECORD_AUDIO".equals(permission)) {
                        appsWithPermission.add(pkg.packageName);
                        break;
                    }
                }
            }
        }
        return appsWithPermission;
    }
}