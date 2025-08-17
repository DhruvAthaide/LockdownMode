package com.example.lockdownmode;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.di.Injector;
import java.util.List;

public class LockdownAccessibilityService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case "CLEAR_RECENTS":
                    clearRecents();
                    break;
                case "CLOSE_BG_APPS":
                    killBackgroundProcesses();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void clearRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                List<AccessibilityNodeInfo> clearButtons = root.findAccessibilityNodeInfosByText("Clear all");
                if (!clearButtons.isEmpty()) {
                    clearButtons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                root.recycle();
            }
        }, 500); // Delay to allow recents screen to load
    }

    private void killBackgroundProcesses() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    am.killBackgroundProcesses(process.processName);
                }
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SecurePrefs prefs = Injector.providePrefs(this);
        if (prefs.getMinimalLauncher() && prefs.getLockdownActive()) {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Intent intent = new Intent(this, LockdownLauncherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onInterrupt() { }
}