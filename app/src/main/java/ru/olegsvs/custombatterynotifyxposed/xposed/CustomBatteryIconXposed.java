package ru.olegsvs.custombatterynotifyxposed.xposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ru.olegsvs.custombatterynotifyxposed.MainActivity;
import ru.olegsvs.custombatterynotifyxposed.R;

public class CustomBatteryIconXposed implements IXposedHookLoadPackage {
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    private static final String SLOT_BATTERY_CUSTOM = "battery_custom";
    private static final String PACKAGE_ANDROID = "android";
    public static final String PACKAGE_OWN = "ru.olegsvs.custombatterynotifyxposed";
    private static final String MAIN_ACTIVITY = PACKAGE_OWN + ".MainActivity";
    private static final String TAG = "CustomBatteryNotifyXposed";

    private static final String mainFilePath = "/sys/class/power_supply/battery/";
    private static final String capacityFilePath = "/sys/class/power_supply/battery/capacity-smb";
    private static final String statusFilePath = "/sys/class/power_supply/battery/status-smb";
    private int mState = 0;
    private int mMicrophone = 0;
    private Object mService;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                updateHeadset(intent);
            } else if (intent.getAction().equals(MainActivity.ICON_CHANGED)) {
                updateHeadsetIcon(intent);
            }
        }
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookSystemUI(lpparam);
        hookAndroid(lpparam);

        if (!lpparam.packageName.equals(PACKAGE_OWN)) return;
        XposedHelpers.findAndHookMethod(MAIN_ACTIVITY, lpparam.classLoader, "isActivated", XC_MethodReplacement.returnConstant(true));
    }

    private void hookAndroid(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE_ANDROID)) return;

        Class<?> classStatusBarManagerService = XposedHelpers.findClass("com.android.internal.statusbar.StatusBarIconList", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(classStatusBarManagerService, "defineSlots", String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String[] slots = (String[]) param.args[0];
                int N = slots.length;

                String[] newSlots = new String[N + 1];
                for (int i = 0, j = 0; i < N + 1; i++) {
                    newSlots[i] = slots[j];
                    if (Objects.equals(slots[j], "phone_signal")) {
                        Log.i("TGM", "beforeHookedMethod: " + slots[j].toString() + " " + j);
                        newSlots[++i] = "battery_custom";
                    }
                    j++;
                }
                param.args[0] = newSlots;
            }
        });
    }

    private void hookSystemUI(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE_SYSTEMUI)) return;

        Class<?> classPhoneStatusBarPolicy = XposedHelpers.findClass("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader);
        XposedBridge.hookAllConstructors(classPhoneStatusBarPolicy, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                mService = XposedHelpers.getObjectField(param.thisObject, "mService");
                setIcon(SLOT_BATTERY_CUSTOM, R.drawable.gn_stat_sys_battery_0, 0, null);
                setIconVisibility(SLOT_BATTERY_CUSTOM, false);
//                for (int i = 0; i < 9000000; i++) {
//
//                    setIcon(SLOT_HEADSET, R.drawable.gn_stat_sys_battery_100, 0, null);
//
//
//
//                }
//                setIcon(SLOT_HEADPHONE, ConfigUtils.icons().headphone, 0, null);

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_HEADSET_PLUG);
                filter.addAction(MainActivity.ICON_CHANGED);
                context.registerReceiver(mIntentReceiver, filter, null, (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler"));
            }
        });
    }

    public void setIcon(String slot, int iconId, int iconLevel, String contentDescription) {
        try {
            Object svc = XposedHelpers.callMethod(mService, "getService");
            if (svc != null) {
                XposedHelpers.callMethod(svc, "setIcon", slot, PACKAGE_OWN, iconId, iconLevel, contentDescription);
            }
        } catch (Throwable ex) {
            // system process is dead anyway.
            throw new RuntimeException(ex);
        }
    }

    public void setIconVisibility(String slot, boolean visible) {
        try {
            Object svc = XposedHelpers.callMethod(mService, "getService");
            if (svc != null) {
                XposedHelpers.callMethod(svc, "setIconVisibility", slot, true);
            }
        } catch (Throwable ex) {
            // system process is dead anyway.
            throw new RuntimeException(ex);
        }
    }

    private void updateHeadset(Intent intent) {
        mState = intent.getIntExtra("state", 0);
        mMicrophone = intent.getIntExtra("microphone", 0);

        updateIconVisibilities();
    }

    private void updateIconVisibilities() {
        setIconVisibility(SLOT_BATTERY_CUSTOM, mState != 0 && mMicrophone == 1);
    }

    private void updateHeadsetIcon(Intent intent) {
        int value = intent.getIntExtra(MainActivity.EXTRA_ICON_VALUE, 0);

        setIcon(SLOT_BATTERY_CUSTOM , MainActivity.ICONS[value], 0, null);

        updateIconVisibilities();
    }
}
