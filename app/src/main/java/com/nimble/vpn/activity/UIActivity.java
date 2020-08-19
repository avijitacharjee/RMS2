package com.nimble.vpn.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.anchorfree.partner.api.response.RemainingTraffic;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.nimble.vpn.BuildConfig;
import com.nimble.vpn.InAppPurchase.IabBroadcastReceiver;
import com.nimble.vpn.InAppPurchase.IabHelper;
import com.nimble.vpn.InAppPurchase.IabResult;
import com.nimble.vpn.InAppPurchase.Inventory;
import com.nimble.vpn.InAppPurchase.Purchase;
import com.nimble.vpn.Preference;
import com.nimble.vpn.R;
import com.nimble.vpn.utils.AdMod;
import com.nimble.vpn.utils.AdModFacebook;
import com.nimble.vpn.utils.Converter;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nimble.vpn.utils.Constant.INAPPKEY;
import static com.nimble.vpn.utils.Constant.INAPPSKUUNIT;
import static com.nimble.vpn.utils.Constant.MONTHLYKEY;
import static com.nimble.vpn.utils.Constant.PRIMIUM_STATE;
import static com.nimble.vpn.utils.Constant.PURCHASETIME;
import static com.nimble.vpn.utils.Constant.SIXMONTHKEY;
import static com.nimble.vpn.utils.Constant.YEARKEY;

public abstract class UIActivity extends AppCompatActivity implements View.OnClickListener, IabBroadcastReceiver.IabBroadcastListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    public String SKU_DELAROY_MONTHLY;
    public String SKU_DELAROY_SIXMONTH;
    public String SKU_DELAROY_YEARLY;
    public String base64EncodedPublicKey;
    @BindView(R.id.server_ip)
    TextView server_ip;
    @BindView(R.id.img_connect)
    ImageView img_connect;
    @BindView(R.id.connect_btn)
    TextView connectBtnTextView;
    @BindView(R.id.connection_state)
    TextView connectionStateTextView;
    @BindView(R.id.connection_progress)
    ProgressBar connectionProgressBar;
    @BindView(R.id.traffic_limit)
    TextView trafficLimitTextView;
    @BindView(R.id.optimal_server_btn)
    LinearLayout currentServerBtn;
    @BindView(R.id.selected_server)
    TextView selectedServerTextView;
    @BindView(R.id.country_flag)
    ImageView country_flag;
    @BindView(R.id.txt_download)
    TextView txt_download;
    @BindView(R.id.txt_upload)
    TextView txt_upload;
    @BindView(R.id.lin_spped)
    LinearLayout lin_spped;
    @BindView(R.id.lay_pro)
    LinearLayout lay_pro;
    Toolbar toolbar;
    androidx.appcompat.app.ActionBarDrawerToggle mDrawerToggle;
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    Preference preference;
    boolean mSubscribedToDelaroy = false;
    String mDelaroySku = "";
    boolean mAutoRenewEnabled = false;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                unlockdata();
                return;
            }

            // First find out which subscription is auto renewing
            Purchase delaroyMonthly = inventory.getPurchase(SKU_DELAROY_MONTHLY);
            Purchase delaroySixMonth = inventory.getPurchase(SKU_DELAROY_SIXMONTH);
            Purchase delaroyYearly = inventory.getPurchase(SKU_DELAROY_YEARLY);
            if (delaroyMonthly != null && delaroyMonthly.isAutoRenewing()) {
                mDelaroySku = SKU_DELAROY_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (delaroySixMonth != null && delaroySixMonth.isAutoRenewing()) {
                mDelaroySku = SKU_DELAROY_SIXMONTH;
                mAutoRenewEnabled = true;
            } else if (delaroyYearly != null && delaroyYearly.isAutoRenewing()) {
                mDelaroySku = SKU_DELAROY_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mDelaroySku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToDelaroy = (delaroyMonthly != null && verifyDeveloperPayload(delaroyMonthly))
                    || (delaroySixMonth != null && verifyDeveloperPayload(delaroySixMonth))
                    || (delaroyYearly != null && verifyDeveloperPayload(delaroyYearly));

            if (mDelaroySku != "") {
                preference.setStringpreference(INAPPSKUUNIT, mDelaroySku);
                preference.setLongpreference(PURCHASETIME, inventory.getPurchase(mDelaroySku).getPurchaseTime());
            }
            unlockdata();
        }
    };
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerList;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    /**
     * Update UI and check Remaining Traffic on every 10 seconds
     */
    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            checkRemainingTraffic();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };

    protected abstract void isLoggedIn(Callback<Boolean> callback);

    protected abstract void loginToVpn();

    protected abstract void isConnected(Callback<Boolean> callback);

    protected abstract void connectToVpn();

    protected abstract void disconnectFromVnp();

    protected abstract void chooseServer();

    protected abstract void getCurrentServer(Callback<String> callback);

    protected abstract void checkRemainingTraffic();

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    private void unlockdata() {
        if (mSubscribedToDelaroy) {
            unlock();
        } else {
            preference.setBooleanpreference(PRIMIUM_STATE, false);
        }
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_ID));
        LoadInterstitialAd();
        LoadBannerAd();
    }

    public void unlock() {
        preference.setBooleanpreference(PRIMIUM_STATE, true);
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mHelper == null) {
            return;
        }
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loginToVpn();

        preference = new Preference(this);
        SKU_DELAROY_MONTHLY = preference.getStringpreference(MONTHLYKEY, SKU_DELAROY_MONTHLY);
        SKU_DELAROY_SIXMONTH = preference.getStringpreference(SIXMONTHKEY, SKU_DELAROY_SIXMONTH);
        SKU_DELAROY_YEARLY = preference.getStringpreference(YEARKEY, SKU_DELAROY_YEARLY);
        base64EncodedPublicKey = preference.getStringpreference(INAPPKEY, base64EncodedPublicKey);

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(UIActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, get an inventory of stuff.
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mDrawerList = findViewById(R.id.left_drawer);
        setupToolbar();
        setupDrawerToggle();

        LinearLayout info = findViewById(R.id.personal_info);
        LinearLayout password = findViewById(R.id.change_password);
        LinearLayout rate_us = findViewById(R.id.rate_us);
        LinearLayout privacy_policy = findViewById(R.id.privacy_policy);
        LinearLayout terms = findViewById(R.id.terms_to_use);
        LinearLayout share_applink = findViewById(R.id.share_app_link);
        LinearLayout more_apps = findViewById(R.id.more_app);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnClickListener(this);
        info.setOnClickListener(this);
        rate_us.setOnClickListener(this);
        password.setOnClickListener(this);
        privacy_policy.setOnClickListener(this);
        terms.setOnClickListener(this);
        share_applink.setOnClickListener(this);
        more_apps.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.personal_info:
                Toast.makeText(this, "Personal information", Toast.LENGTH_SHORT).show();
                break;
            case R.id.change_password:
                Toast.makeText(this, "change password", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rate_us:
                Rate_App();
                break;
            case R.id.privacy_policy:
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
                break;
            case R.id.terms_to_use:
                startActivity(new Intent(this, TermsToUseActivity.class));
                break;
            case R.id.share_app_link:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "Download Free" + getResources().getString(R.string.app_name) + " App\n" + BuildConfig.APP_LINK);
                startActivity(Intent.createChooser(i, "Share Via"));
                break;
            case R.id.more_app:
                Uri uri = Uri.parse("market://search?q=pub:" + "Oneioo+Business+Solution+Pvt.+Ltd.");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/search?q=pub:" + "Oneioo+Business+Solution+Pvt.+Ltd.")));
                }
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void Rate_App() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getApplication().getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplication().getPackageName())));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle() {
        mDrawerToggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startUIUpdateTask();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUIUpdateTask();
    }

    @OnClick(R.id.img_connect)
    public void onConnectBtnClick(View v) {
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    disconnectFromVnp();
                } else {
                    connectToVpn();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
            }
        });
    }

    @OnClick(R.id.optimal_server_btn)
    public void onServerChooserClick(View v) {
        chooseServer();
    }

    @OnClick(R.id.lay_pro)
    public void openPro(View v) {
        Intent intent = new Intent(UIActivity.this, PremiumActivity.class);
        startActivity(intent);
    }

    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }

    /**
     * Update UI according to VPN state
     */
    protected void updateUI() {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {
                switch (vpnState) {
                    case IDLE: {
                        connectBtnTextView.setEnabled(true);
                        connectBtnTextView.setText(R.string.connect);
                        connectionStateTextView.setText(R.string.disconnected);
                        server_ip.setText(R.string.default_server_ip_text);
                        img_connect.setImageDrawable(getResources().getDrawable(R.drawable.off_btn));
                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                        selectedServerTextView.setText(R.string.select_country);
                        lin_spped.setVisibility(View.GONE);
                        if(preference.isBooleenPreference(PRIMIUM_STATE)){
                            lay_pro.setVisibility(View.GONE);
                        }
                        else {
                            lay_pro.setVisibility(View.VISIBLE);
                        }
                        hideConnectProgress();
                        break;
                    }
                    case CONNECTED: {
                        connectBtnTextView.setEnabled(true);
                        img_connect.setImageDrawable(getResources().getDrawable(R.drawable.on_btn));
                        connectBtnTextView.setText(R.string.disconnect);
                        connectionStateTextView.setText(R.string.connected);
                        lin_spped.setVisibility(View.VISIBLE);
                        lay_pro.setVisibility(View.GONE);
                        hideConnectProgress();
                        break;
                    }
                    case CONNECTING_VPN:
                    case CONNECTING_CREDENTIALS:
                    case CONNECTING_PERMISSIONS: {
                        connectBtnTextView.setText(R.string.connecting);
                        connectionStateTextView.setText(R.string.connecting);
                        connectBtnTextView.setEnabled(false);
                        lin_spped.setVisibility(View.GONE);
                        if(preference.isBooleenPreference(PRIMIUM_STATE)){
                            lay_pro.setVisibility(View.GONE);
                        }
                        else {
                            lay_pro.setVisibility(View.VISIBLE);
                        }
                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                        selectedServerTextView.setText(R.string.select_country);
                        showConnectProgress();
                        break;
                    }
                    case PAUSED: {
                        connectBtnTextView.setEnabled(false);
                        connectBtnTextView.setText(R.string.paused);
                        connectionStateTextView.setText(R.string.paused);
                        lin_spped.setVisibility(View.GONE);
                        if(preference.isBooleenPreference(PRIMIUM_STATE)){
                            lay_pro.setVisibility(View.GONE);
                        }
                        else {
                            lay_pro.setVisibility(View.VISIBLE);
                        }
                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                        selectedServerTextView.setText(R.string.select_country);
                        break;
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                selectedServerTextView.setText(R.string.select_country);
            }
        });
        getCurrentServer(new Callback<String>() {
            @Override
            public void success(@NonNull final String currentServer) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                        selectedServerTextView.setText(R.string.select_country);
                        if (!currentServer.equals("")) {
                            Locale locale = new Locale("", currentServer);
                            Resources resources = getResources();
                            String sb = "drawable/" + currentServer.toLowerCase();
                            country_flag.setImageResource(resources.getIdentifier(sb, null, getPackageName()));
                            selectedServerTextView.setText(locale.getDisplayCountry());
                        } else {
                            country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                            selectedServerTextView.setText(R.string.select_country);
                        }
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {
                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.flag_default));
                selectedServerTextView.setText(R.string.select_country);
            }
        });
    }

    /**
     * Update Traffic Status
     *
     * @param outBytes Upload bytes
     * @param inBytes  Download bytes
     */
    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Converter.humanReadableByteCountOld(outBytes, false);
        String inString = Converter.humanReadableByteCountOld(inBytes, false);

        txt_download.setText(inString);
        txt_upload.setText(outString);
    }

    /**
     * Update Remaining Traffic
     *
     * @param remainingTrafficResponse Response of traffic
     */
    protected void updateRemainingTraffic(RemainingTraffic remainingTrafficResponse) {
        if (remainingTrafficResponse.isUnlimited()) {
            trafficLimitTextView.setText(R.string.unlimited_available);
        } else {
            String trafficUsed = Converter.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "Mb";
            String trafficLimit = Converter.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "Mb";
            trafficLimitTextView.setText(getResources().getString(R.string.traffic_limit, trafficUsed, trafficLimit));
        }
    }

    /**
     * Show IP Address of Connected Server
     *
     * @param ipaddress IP Address
     */
    protected void ShowIPaddera(String ipaddress) {
        server_ip.setText(ipaddress);
    }


    protected void showConnectProgress() {
        connectionProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideConnectProgress() {
        connectionProgressBar.setVisibility(View.GONE);
    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void LoadInterstitialAd() {
        if (BuildConfig.GOOGlE_AD) {
            AdMod.buildAdFullScreen(getApplicationContext(), new AdMod.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFaildToLoad(int i) {
                }
            });
        } else if (BuildConfig.FACEBOOK_AD) {
            AdModFacebook.buildAdFullScreen(getApplicationContext(), new AdModFacebook.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFaildToLoad(AdError adError) {
                }

                @Override
                public void onInterstitialDismissed() {
                }

                @Override
                public void onInterstitialDisplayed() {
                }

                @Override
                public void onLoggingImpression() {
                }
            });
        }
    }

    public void LoadBannerAd() {
        RelativeLayout adContainer = findViewById(R.id.adView);
        if (BuildConfig.GOOGlE_AD) {
            AdMod.buildAdBanner(getApplicationContext(), adContainer, 0, new AdMod.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFaildToLoad(int i) {
                }
            });
        } else if (BuildConfig.FACEBOOK_AD) {
            AdModFacebook.buildAdBanner(this, adContainer, 0, new AdModFacebook.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFaildToLoad(AdError adError) {
                }

                @Override
                public void onInterstitialDismissed() {
                }

                @Override
                public void onInterstitialDisplayed() {
                }

                @Override
                public void onLoggingImpression() {
                }
            });
        }
    }
}
