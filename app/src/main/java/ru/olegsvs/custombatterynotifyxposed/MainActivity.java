package ru.olegsvs.custombatterynotifyxposed;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static ru.olegsvs.custombatterynotifyxposed.xposed.CustomBatteryIconXposed.PACKAGE_OWN;

public class MainActivity extends AppCompatActivity implements IconSelectionView.IconSelectionListener, View.OnClickListener {

    public static final String ICON_CHANGED = "ru.olegsvs.custombatterynotifyxposed.action.ICON_CHANGED";
    public static final String EXTRA_ICON_TYPE = "extra.ICON_TYPE";
    public static final String EXTRA_ICON_VALUE = "extra.ICON_VALUE";
    private BatteryManager mBatteryManager = null;
    private SharedPreferences mPreferences;
    private static final String TAG = "MainActivity";
    public static int[] ICONS;
    SharedPreferences sharedPref;
    static {
        ICONS = new int[] {
                R.drawable.gn_stat_sys_battery_30,
                R.drawable.stat_sys_battery_37
        };
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getPreferences();
        sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);

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
        } else {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("iconValue" , position);
        editor.apply();
        editor.commit();
        showNotifyClick2(null);
    }


    public void showNotifyClick2(View v){
        try {
            Log.i("TGM", "showNotifyClick2: clicked");
            mBatteryManager = null;
            Intent intent = new Intent(getApplicationContext(), BatteryManagerService.class);
            stopService(intent);
            if(BatteryManagerService.isMyServiceRunning()) {
                stopService(intent);
                startService(intent);
            } else startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
