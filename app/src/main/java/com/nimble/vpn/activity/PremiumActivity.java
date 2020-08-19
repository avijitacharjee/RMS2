package com.nimble.vpn.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.nimble.vpn.InAppPurchase.IabBroadcastReceiver;
import com.nimble.vpn.InAppPurchase.IabHelper;
import com.nimble.vpn.InAppPurchase.IabResult;
import com.nimble.vpn.InAppPurchase.Inventory;
import com.nimble.vpn.InAppPurchase.Purchase;
import com.nimble.vpn.Preference;
import com.nimble.vpn.R;

import java.util.ArrayList;
import java.util.List;

import static com.nimble.vpn.utils.Constant.INAPPKEY;
import static com.nimble.vpn.utils.Constant.INAPPSKUUNIT;
import static com.nimble.vpn.utils.Constant.MONTHLYKEY;
import static com.nimble.vpn.utils.Constant.PRIMIUM_STATE;
import static com.nimble.vpn.utils.Constant.PURCHASETIME;
import static com.nimble.vpn.utils.Constant.SIXMONTHKEY;
import static com.nimble.vpn.utils.Constant.YEARKEY;

public class PremiumActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {

    static final int RC_REQUEST = 10001;
    private static final String TAG = "PremiumActivity";
    public String SKU_DELAROY_MONTHLY;
    public String SKU_DELAROY_SIXMONTH;
    public String SKU_DELAROY_YEARLY;
    public String base64EncodedPublicKey;
    CardView one_month, six_months, twelve_months;
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    boolean mSubscribedToDelaroy = false;
    String mDelaroySku = "";
    boolean mAutoRenewEnabled = false;
    String mSelectedSubscriptionPeriod = "";
    private Preference preference;

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
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");

                return;
            }


            if (purchase.getSku().equals(SKU_DELAROY_MONTHLY)
                    || purchase.getSku().equals(SKU_DELAROY_SIXMONTH)
                    || purchase.getSku().equals(SKU_DELAROY_YEARLY)) {
                preference.setStringpreference(INAPPSKUUNIT, purchase.getSku());
                preference.setLongpreference(PURCHASETIME, purchase.getPurchaseTime());
                unlock();
                alert("Thank you for subscribing to Delaroy!");
                mSubscribedToDelaroy = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mDelaroySku = purchase.getSku();

            }
        }

    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_premium);

        preference = new Preference(PremiumActivity.this);
        one_month = findViewById(R.id.one_month_layout);
        six_months = findViewById(R.id.six_months_layout);
        twelve_months = findViewById(R.id.twelve_months_layout);

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

                mBroadcastReceiver = new IabBroadcastReceiver(PremiumActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        one_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    complain("Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                mSelectedSubscriptionPeriod = SKU_DELAROY_MONTHLY;
                PurchaseFlow();
            }
        });
        six_months.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    complain("Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                mSelectedSubscriptionPeriod = SKU_DELAROY_SIXMONTH;
                PurchaseFlow();
            }
        });
        twelve_months.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    complain("Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                mSelectedSubscriptionPeriod = SKU_DELAROY_YEARLY;
                PurchaseFlow();
            }
        });
    }

    private void PurchaseFlow() {
        String payload = "";
        List<String> oldSkus = null;
        if (!TextUtils.isEmpty(mDelaroySku)
                && !mDelaroySku.equals(mSelectedSubscriptionPeriod)) {
            // The user currently has a valid subscription, any purchase action is going to
            // replace that subscription
            oldSkus = new ArrayList<String>();
            oldSkus.add(mDelaroySku);
        }
        try {
            mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
                    oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        }
    }

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
    }

    public void unlock() {
        preference.setBooleanpreference(PRIMIUM_STATE, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
}
