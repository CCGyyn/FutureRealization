package com.ccg.futurerealization.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/** Test-APK-only lifecycle control for its own fake mail activity. */
public class FakeEmailControlReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, FakeEmailActivity.class),
                intent.getBooleanExtra("enabled", false) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        setResultCode(Activity.RESULT_OK);
    }
}
