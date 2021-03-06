package ru.olegsvs.custombatterynotifyxposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by olegsvs on 11.05.2017.
 */

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
            if(sharedPref.getBoolean("serviceAutoStart", false)) {
                Intent myIntent = new Intent(context, BatteryManagerService.class);
                context.startService(myIntent);
                Log.w("TGM", "BatteryBroadcastReceiver: onReceive exec");
            } else {
                Log.w("TGM", "BatteryBroadcastReceiver: autostart service is disabled");
            }
        }
    }
}
