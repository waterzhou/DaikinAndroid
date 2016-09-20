package org.achartengine.chartdemo.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

/**
 * Created by water.zhou on 1/8/2015.
 */
public class PlugActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "Plug";
    private String dataSource;
    private Handler mHandler;
    private udpbroadcast udpBroadcast = null;
    private static AppPreferences mAppPreferences;
    private String SERVER_IP = null;
    private Socket socket;
    private Integer SERVERPORT = 8899;
    private SimpleDateFormat mDateFormat;
    private Button mSyncTimeButton;
    private Button mAdduserButton;
    //private Button mLockButton;
    private Switch mSwitch;
    private EditText mUserInputField;
    private EditText mPasswordInputField;
    private ImageView mLockStatus;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    // while this is true, the server will continue running
    private boolean mRun = false;
    private String mServerMessage;
    private TCPClient mTcpClient;
    private static final int START_GETPLUGSTATUS = 0x2000;
    private static final int DELAY = 2000;
    private boolean mPlugStatus = false;
    private String mClockname;
    private String mClockPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.plugactivity);

        Bundle bundle = this.getIntent().getExtras();
        dataSource = bundle.getString("dataSource");
        final TextView IpText = (TextView) findViewById(R.id.TextView01);
        mAppPreferences = new AppPreferences(getApplicationContext());
        SERVER_IP = mAppPreferences.getParameter(dataSource);
        IpText.setText(SERVER_IP);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        // mSyncTimeButton = (Button) findViewById(R.id.config_sync_time);

        mLockStatus= (ImageView) findViewById(R.id.imageView);
        mLockStatus.setOnClickListener(this);

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case START_GETPLUGSTATUS: {
                        if (mTcpClient != null) {
                            mTcpClient.sendMessage("AT+QUERY");
                        }
                        startGetPlugStatus();
                        break;
                    }
                }
            }
        };
        new connectTask().execute("");
        startGetPlugStatus();
    }
    private void startGetPlugStatus() {
        mHandler.sendEmptyMessageDelayed(START_GETPLUGSTATUS,DELAY);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView: {
                if (mTcpClient != null) {
                    if (mPlugStatus)
                        mTcpClient.sendMessage("AT+CLOSE");
                    else
                        mTcpClient.sendMessage("AT+OPEN");
                }
                break;
            }
        }
    }



    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            },SERVER_IP,SERVERPORT);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0].equalsIgnoreCase("AT+QUERY=OPEN")) {
                mPlugStatus = true;
                mLockStatus.setImageResource(R.drawable.ic_lock_open_black_48dp);
                PlugActivity.this.findViewById(R.id.plug).setBackgroundResource(R.drawable.plugroom1);
                //mSwitch.setChecked(true);
                //mLockButton.setText("Lock");
            }
            if (values[0].equalsIgnoreCase("AT+QUERY=CLOSE")) {
                mPlugStatus = false;
                mLockStatus.setImageResource(R.drawable.ic_https_black_48dp);
                PlugActivity.this.findViewById(R.id.plug).setBackgroundResource(R.drawable.plugroom2);
                //mSwitch.setChecked(false);
                //mLockButton.setText("Unlock");
            }

            //in the arrayList we add the messaged received from server
           // Toast.makeText(PlugActivity.this, values[0], Toast.LENGTH_SHORT).show();
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mTcpClient != null) {
            mTcpClient.out.close();
            try {
                mTcpClient.in.close();
            } catch (Exception e) {
                Log.e(TAG, "cancel error");
            }
        }
//        System.exit(RESULT_OK);
        mTcpClient.stopClient();
    }
}
