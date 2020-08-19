package com.nimble.vpn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nimble.vpn.BuildConfig;
import com.nimble.vpn.Preference;
import com.nimble.vpn.dialog.EatFoodyTextView;
import com.nimble.vpn.utils.NetworkState;
import com.nimble.vpn.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.nimble.vpn.utils.Constant.*;

public class SplashActivity extends AppCompatActivity {

    Preference preference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        EatFoodyTextView version = findViewById(R.id.Version);
        version.setText(getString(R.string.version) + packageInfo.versionName);

        preference=new Preference(SplashActivity.this);
        preference.setStringpreference(INAPPKEY, BuildConfig.IN_APPKEY);
        preference.setStringpreference(MONTHLYKEY, BuildConfig.MONTHLY);
        preference.setStringpreference(SIXMONTHKEY,BuildConfig.SIX_MONTH);
        preference.setStringpreference(YEARKEY, BuildConfig.YEARLY);
        if (NetworkState.isOnline(this)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            }, 3000);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.network_error))
                    .setMessage(getString(R.string.network_error_message))
                    .setNegativeButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    onBackPressed();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
