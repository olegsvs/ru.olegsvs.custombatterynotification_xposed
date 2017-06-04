package ru.olegsvs.custombatterynotifyxposed;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ru.olegsvs.custombatterynotifyxposed.xposed.CustomBatteryIconXposed;

import static ru.olegsvs.custombatterynotifyxposed.xposed.CustomBatteryIconXposed.PACKAGE_OWN;

public class MainActivity extends AppCompatActivity implements IconSelectionView.IconSelectionListener, View.OnClickListener {

    public static final String ICON_CHANGED = "ru.olegsvs.custombatterynotifyxposed.action.ICON_CHANGED";
    public static final String EXTRA_ICON_TYPE = "extra.ICON_TYPE";
    public static final String EXTRA_ICON_VALUE = "extra.ICON_VALUE";
    private static final String HIDE_APP_ICON = "hide_app_icon";
    private SharedPreferences mPreferences;
    private static final String TAG = "MainActivity";
    public static int[] ICONS;

    static {
        ICONS = new int[] {
                R.drawable.gn_stat_sys_battery_0,
                R.drawable.gn_stat_sys_battery_20,
                R.drawable.gn_stat_sys_battery_30,
                R.drawable.gn_stat_sys_battery_50,
                R.drawable.gn_stat_sys_battery_60,
                R.drawable.gn_stat_sys_battery_80,
                R.drawable.gn_stat_sys_battery_90,
                R.drawable.gn_stat_sys_battery_100
        };
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getPreferences();

        IconSelectionView batteryChooseView = (IconSelectionView) findViewById(R.id.battery_choose);
        batteryChooseView.setTitle("Battery icon");
        batteryChooseView.setStrings(getResources().getStringArray(R.array.icons_name));
        batteryChooseView.setIcons(ICONS);
        batteryChooseView.setListener(this);
        batteryChooseView.setSharedPreferences(preferences);
        batteryChooseView.setKey("batteryicon");

        if (!isActivated()) {
            batteryChooseView.setVisibility(View.GONE);

            View warning = findViewById(R.id.warning);
            warning.setVisibility(View.VISIBLE);

            TextView warningText = (TextView) findViewById(R.id.warningText);

            if (hasXposedInstaller()) {
                warningText.setText(R.string.module_not_activated);
                warning.setClickable(true);
                warning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(getXposedInstallerLaunchIntent());
                    }
                });
            } else {
                warningText.setText(R.string.xposed_required);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("WorldReadableFiles")
    public SharedPreferences getPreferences() {
        if (mPreferences != null)
            return mPreferences;
        mPreferences = getSharedPreferences("ru.olegsvs.custombatterynotifyxposed.shared_preferences", MODE_WORLD_READABLE);
        return mPreferences;
    }

    @Override
    public void onSelect(IconSelectionView view, int position) {
//        Intent intent = new Intent(ICON_CHANGED);
//        intent.putExtra(EXTRA_ICON_TYPE, view.getId() == R.id.battery_choose ? 0 : 1);
//        intent.putExtra(EXTRA_ICON_VALUE, position);
//        intent.setPackage(CustomBatteryIconXposed.PACKAGE_SYSTEMUI);
//        sendBroadcast(intent);
        Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
        startActivity(intent);
    }

    public boolean isActivated() {
        return false;
    }

    public boolean hasXposedInstaller() {
        return isActivated() || getXposedInstallerLaunchIntent() != null;
    }

    public Intent getXposedInstallerLaunchIntent() {
        return getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.hide_app_icon).setChecked(getPreferences().getBoolean(HIDE_APP_ICON, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.hide_app_icon) {
            boolean checked = !item.isChecked();
            item.setChecked(checked);
            getPreferences().edit().putBoolean(HIDE_APP_ICON, checked).apply();
            int mode = checked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            getPackageManager().setComponentEnabledSetting(new ComponentName(this,PACKAGE_OWN + ".MainShortcut" ), mode, PackageManager.DONT_KILL_APP);
        }
        return super.onOptionsItemSelected(item);
    }
}
