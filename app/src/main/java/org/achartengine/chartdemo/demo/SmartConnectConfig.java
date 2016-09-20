package org.achartengine.chartdemo.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.achartengine.chartdemo.demo.utils.SmartConnectDialogManager;
import org.achartengine.chartdemo.demo.utils.SmartConnectUtils;
import org.achartengine.chartdemo.demo.utils.SmartConnectWifiManager;


/**
 * Created by water.zhou on 10/23/2014.
 */
public class SmartConnectConfig extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    private static final String TAG = "SmartConnect:";

    /**
     * Wifi Manager instance which gives the network related information like
     * Wifi ,SSID etc.
     */
    private SmartConnectWifiManager mSmartWifiManager = null;

    /**
     * Sending a request to server is done onClick of this button,it interacts
     * with the smartConfig.jar library
     */
    private Button mStartConfig = null;
    private Button mChangeButton = null;

    private EditText mSSIDInputField = null;

    /**
     * The Password input field details are entered by user also called Key
     * field
     */
    private EditText mPasswordInputField = null;


    /**
     * The Encryption key field input field is entered by user
     */
    private EditText mKeyInputField = null;

    /**
     * Boolean to check if network is enabled or not
     */
    public boolean isNetworkConnecting = false;
    /**
     * A Dialog instance which is responsible to generate all dialogs in the app
     */
    private SmartConnectDialogManager mDialogManager = null;
    private ProgressDialog pd_config_refresh;
    private static AppPreferences mAppPreferences;

    private boolean eye = false;
    private ImageView ivEye;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartConnectUtils.setProtraitOrientationEnabled(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.configuration);
        mAppPreferences = new AppPreferences(getApplicationContext());

        initViews();
        setViewClickListeners();
        initData();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            mAppPreferences.setBridgeWiFiSSID(arg0.getItemAtPosition(arg2).toString());
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (broadcastReceiver != null) {
            // Unregister receiver.
            unregisterReceiver(broadcastReceiver);
            // The important bit here is to set the receiver
            // to null once it has been unregistered.
            broadcastReceiver = null;
        }
    }

    /**
     * Initialise all view components from xml
     */
    private void initViews() {
        mStartConfig = (Button) findViewById(R.id.config_start_button);
        mChangeButton = (Button) findViewById(R.id.config_change_button);
        mSSIDInputField = (EditText) findViewById(R.id.config_ssid_input);
        mPasswordInputField = (EditText) findViewById(R.id.config_passwd_input);
        ivEye = (ImageView) findViewById(R.id.eye);

        ivEye.setImageResource(R.drawable.w22);
        mPasswordInputField.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //mconfigProgress = (ProgressBar) findViewById(R.id.config_progress);
    }

    /**
     * returns the Wifi Manager instance which gives the network related
     * information like Wifi ,SSID etc.
     *
     * @return Wifi Manager instance
     */
    public SmartConnectWifiManager getWiFiManagerInstance() {
        if (mSmartWifiManager == null) {
            mSmartWifiManager = new SmartConnectWifiManager(SmartConnectConfig.this);
        }
        return mSmartWifiManager;
    }

    /**
     * Initialize all view componets in screen with input data
     */
    private void initData() {

        if (getWiFiManagerInstance().getCurrentSSID() != null
                && getWiFiManagerInstance().getCurrentSSID().length() > 0) {
            Log.d(TAG, "init data " + getWiFiManagerInstance().getCurrentSSID());
            mSSIDInputField.setText(getWiFiManagerInstance().getCurrentSSID());
            /**
             * removing the foucs of ssid when field is already configured from
             * Network
             */
            mSSIDInputField.setEnabled(false);
            mSSIDInputField.setFocusable(false);
            mSSIDInputField.setFocusableInTouchMode(false);
        }
    }

    /**
     * Init the click listeners of all required views
     */
    private void setViewClickListeners() {
        mStartConfig.setOnClickListener(this);
        mChangeButton.setOnClickListener(this);
        ivEye.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.config_start_button: {
                mAppPreferences.setRouterWiFiSSID(mSSIDInputField.getText().toString());
                mAppPreferences.setRouterWiFiKey(mPasswordInputField.getText().toString());
                Log.d(TAG, "ssid=" + mSSIDInputField.getText().toString() + "password=" + mPasswordInputField.getText().toString());
                /*new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
                        isSsidHiddenStr, taskResultCountStr);*/
                break;
            }
            case R.id.config_change_button: {
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
            case R.id.eye: {
                if (eye) {// 密码可见-->不可见
                    eye = false;
                    ivEye.setImageResource(R.drawable.w22);
                    mPasswordInputField.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {// 密码不可见--->可见
                    eye = true;
                    ivEye.setImageResource(R.drawable.w21);
                    mPasswordInputField.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                mPasswordInputField.setSelection(mPasswordInputField.getText().length());
                break;
            }
        }
    }

    private void dismissProcessDialog() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (pd_config_refresh != null) {
                    pd_config_refresh.dismiss();
                }
                pd_config_refresh = null;
            }
        });
    }

    /*Show process dialog*/
    private void showProcessDialog(final int progress) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (pd_config_refresh == null) {
                    pd_config_refresh = new ProgressDialog(SmartConnectConfig.this);
                }
                pd_config_refresh.setMessage("Smart Configuring...");
                pd_config_refresh.setCancelable(false);
                pd_config_refresh.setProgress(progress);
                pd_config_refresh.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd_config_refresh.setMax(100);
                pd_config_refresh.show();
            }
        });
    }




   /* private void prompt_confirm_dialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(text);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {
                Intent Search_Activity = new Intent(SmartConnectConfig.this, ChartDemo.class);
                Search_Activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(Search_Activity);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {

                System.exit(0);
            }
        })
                .create()
                .show();
    }
*/

    /**
     * Show timeout alert
     */
    private void showConnectionTimedOut(int dialogType) {
        if (mDialogManager == null) {
            mDialogManager = new SmartConnectDialogManager(SmartConnectConfig.this);
        }
        mDialogManager.showCustomAlertDialog(dialogType);
    }

    /**
     * Show Failure alert
     */
    private void showFailureAlert(int dialogType) {
        if (mDialogManager == null) {
            mDialogManager = new SmartConnectDialogManager(SmartConnectConfig.this);
        }
        mDialogManager.showCustomAlertDialog(dialogType);
    }

    /**
     * Throws an alert to user stating the success message recieved after
     * configuration
     */
    private void showConnectionSuccess(int dialogType) {
        if (mDialogManager == null) {
            mDialogManager = new SmartConnectDialogManager(SmartConnectConfig.this);
        }
        mDialogManager.showCustomAlertDialog(dialogType);
    }

    /**
     * Default listener for checkbox the encrypted key is enabled or disabled
     * based on check
     * <p/>
     * if it is checked we need to ensure the length of key is exactly 16 else
     * start is disabled.
     * <p/>
     * The start button is made semi transperent and click is disabled if above
     * case fails
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        /**
         * The start button is made semi transperent and click is disabled if
         * length is not 16chars case fails
         */
        if (isChecked) {
            if (mKeyInputField.length() != 16) {
                mStartConfig.setEnabled(false);
                mStartConfig.getBackground().setAlpha(150);

            } else {
                mStartConfig.setEnabled(false);
                mStartConfig.getBackground().setAlpha(255);
            }
            mKeyInputField.setEnabled(true);
            mKeyInputField.setFocusable(true);
            mKeyInputField.setFocusableInTouchMode(true);
            mKeyInputField.setTextColor(getResources().getColor(R.color.black));
        } else {
            mKeyInputField.setEnabled(false);
            mKeyInputField.setFocusable(false);
            mKeyInputField.setFocusableInTouchMode(false);
            mKeyInputField.setTextColor(getResources().getColor(
                    R.color.disabled_text_color));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // overriden method for text changed listener

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // overriden method for text changed listener
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        /**
         * if is checked and length matches 16 the start is enabled else
         * disabled
         */

    }

    /**
     * A broadcast reciever which is registered to notify the app about the
     * changes in network or Access point is switched by the Device Wifimanager
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {

                } else {
                    mSSIDInputField.setText("");
                }
            }
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getType() == ConnectivityManager.TYPE_WIFI && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    isNetworkConnecting = true;
                    mSSIDInputField.setText(mSmartWifiManager.getCurrentSSID());
                    mSSIDInputField.setEnabled(false);
                    mSSIDInputField.setFocusable(false);
                    mSSIDInputField.setFocusableInTouchMode(false);
                }
            }
        }
    };
}
