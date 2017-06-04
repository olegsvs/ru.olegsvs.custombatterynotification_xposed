package ru.olegsvs.custombatterynotifyxposed;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import ru.olegsvs.custombatterynotifyxposed.xposed.CustomBatteryIconXposed;

/**
 * Created by olegsvs on 11.05.2017.
 */

public class BatteryManagerService extends Service{
    private int iconRes[] = {
            R.drawable.gn_stat_sys_battery_0,
            R.drawable.gn_stat_sys_battery_20,
            R.drawable.gn_stat_sys_battery_30,
            R.drawable.gn_stat_sys_battery_50,
            R.drawable.gn_stat_sys_battery_60,
            R.drawable.gn_stat_sys_battery_80,
            R.drawable.gn_stat_sys_battery_90,
            R.drawable.gn_stat_sys_battery_100

    };
    private final int NOTIFICATION_CUSTOM_BATTERY = 444;
    private int BAT_CAPACITY = 0;
    private int count = 0;
    private int c = 0;
    private static int interval = 2000;
    private final String PERCENT= "%";
    private final String UNRECOGNIZED_VALUES= "Unrecognized values";

    private static boolean IS_STARTED = false;

    Handler myHandler = null;
    BatteryManager mBatteryManager = null;
    Runnable runnable = null;

    public static boolean isMyServiceRunning() {
        Log.w("TGM", "check BatteryManagerService is running ? " + IS_STARTED);
        return IS_STARTED;
    }

    public static void setInterval(int interval) {
        BatteryManagerService.interval = interval * 1000;
        Log.w("TGM", "BatteryManagerService setInterval: " + BatteryManagerService.interval);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IS_STARTED = false;
        mBatteryManager = null;
        if(myHandler != null)
        myHandler.removeCallbacks(runnable);
        Log.w("TGM", "BatteryManagerService onDestroy: BatteryManagerService destroy!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Log.w("TGM", "onStartCommand: BatteryManagerService start");
        if(intent != null) mBatteryManager = intent.getParcelableExtra("BatteryManager");
            if(mBatteryManager == null) {
//                String lastTypeBattery = sharedPref.getString("lastTypeBattery", "null");
//                String lastStateBattery = sharedPref.getString("lastStateBattery", "null");
//                mBatteryManager = new BatteryManager(lastTypeBattery, lastStateBattery);
                mBatteryManager = new BatteryManager("/sys/class/power_supply/BAT0/present","/sys/class/power_supply/BAT0/status");

            }
            if(true /*mBatteryManager.isSupport*/) {
                Log.w("TGM", "BatteryManagerService onStartCommand: mBatteryManager.isSupport = " + mBatteryManager.isSupport);

                interval = 1000 * sharedPref.getInt("interval", 2);
                Log.w("TGM", "onStartCommand: load interval = " + interval);
                createNotify();
                Log.w("TGM", "onStartCommand: create and show notification");
                return super.onStartCommand(intent, flags, startId);
            }
//            } else stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private String getResults() {
        try{
        BAT_CAPACITY = Integer.parseInt(this.mBatteryManager.getCapacity());
            if(!(BAT_CAPACITY >= 0 && BAT_CAPACITY <= 100)){
                Log.e("TGM", "getResults: BAT_CAPACITY index error [0::100]! " + BAT_CAPACITY);
                Toast.makeText(getApplicationContext(),"BAT_CAPACITY index error [0::100] = " + BAT_CAPACITY,Toast.LENGTH_LONG).show();
                this.mBatteryManager = null;
                stopSelf();
                return UNRECOGNIZED_VALUES;

            }
        return BAT_CAPACITY + PERCENT + this.mBatteryManager.getState(); } catch (Exception e)  {
            Log.e("TGM", "getResults: Unrecognized values! " + e.toString());
            Toast.makeText(getApplicationContext(),"Unrecognized values! " + e.toString(),Toast.LENGTH_LONG).show();
            this.mBatteryManager = null;
            stopSelf();
            return UNRECOGNIZED_VALUES;
        }

    }

    private void createNotify() {
        try {
            int color = 0xff123456;

            myHandler = new Handler();
            Log.w("TGM", "handler started");
            Log.w("TGM", "state : " + getResults());
            runnable = new Runnable(){

                @Override
                public void run() {
                    if(IS_STARTED) {
                        if (c == 7) c = 0;
                        Intent intent = new Intent(MainActivity.ICON_CHANGED);
                        intent.putExtra(MainActivity.EXTRA_ICON_TYPE, 0);
                        intent.putExtra(MainActivity.EXTRA_ICON_VALUE, c);
                        c++;
                        intent.setPackage(CustomBatteryIconXposed.PACKAGE_SYSTEMUI);
                        sendBroadcast(intent);
                        Log.i("TGM", "run: getResults" + getResults() + "BAT " + BAT_CAPACITY);
                        if(++count%20 == 0) Runtime.getRuntime().gc();
                        myHandler.postDelayed(runnable, 1000);
                    }
                }

            };

            myHandler.post(runnable);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IS_STARTED = true;
        Log.w("TGM", "BatteryManagerService onCreate: creating BatteryManagerService");
    }
}
