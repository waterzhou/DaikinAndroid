package org.achartengine.chartdemo.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.chartdemo.demo.utils.SmartConnectContants;

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
    private udpbroadcast udpBroadcast = null;
    private static AppPreferences mAppPreferences;
    private String SERVER_IP = null;
    private Socket socket;
    private Integer SERVERPORT = 8899;
    private SimpleDateFormat mDateFormat;
    private Button mButtonGetPicture;
    private Button mButtonGetTemperature;
    private Button mAdduserButton;

    private EditText mUserInputField;
    private EditText mPasswordInputField;
    private ImageView mLockStatus;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    private TCPClient mTcpClient;
    private static final int START_GETPLUGSTATUS = 0x2000;
    private static final int DELAY = 2000;
    private static ConnectionHandler mConnectionHandler;

    private HandlerThread mConnectionThread;
    private boolean mPlugStatus = false;
    private boolean mBoolGetpicture = false;
    private boolean mBoolGettemperature = false;

    class ConnectionHandler extends Handler {
        public ConnectionHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SmartConnectContants.MSG_GET_PICTURE:
                {


                    break;
                }
            }
        }
    }
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
        Log.i(TAG, "server ip is " + SERVER_IP);
        IpText.setText(SERVER_IP);
        mButtonGetPicture = (Button) findViewById(R.id.ButtonGetPicture);
        mButtonGetTemperature = (Button) findViewById(R.id.ButtonGetTemperature);
        mButtonGetPicture.setOnClickListener(this);
        mButtonGetTemperature.setOnClickListener(this);

        // Start another thread to solve different tasks
        mConnectionThread = new HandlerThread("ConnectionThread");
        mConnectionThread.start();
        mConnectionHandler = new ConnectionHandler(mConnectionThread.getLooper());
/*        mLockStatus= (ImageView) findViewById(R.id.imageView);
        mLockStatus.setOnClickListener(this);*/

        new connectTask().execute("");
        //startGetPlugStatus();
    }

    private Handler  mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SmartConnectContants.UPDATE_UI_BUTTON: {
                    if (mBoolGetpicture) {
                        mButtonGetPicture.setEnabled(false);
                        mButtonGetPicture.getBackground().setAlpha(150);
                    }
                    if (mBoolGettemperature) {
                        mButtonGetTemperature.setEnabled(false);
                        mButtonGetTemperature.getBackground().setAlpha(150);
                    }
                    mHandler.sendEmptyMessageDelayed(SmartConnectContants.RESTORE_UI_BUTTON, 3000);
                    break;
                }
                case SmartConnectContants.RESTORE_UI_BUTTON:{
                    if (mBoolGetpicture) {
                        mBoolGetpicture = false;
                        mButtonGetPicture.setEnabled(true);
                        mButtonGetPicture.getBackground().setAlpha(255);
                    }
                    if (mBoolGettemperature) {
                        mBoolGettemperature = false;
                        mButtonGetTemperature.setEnabled(true);
                        mButtonGetTemperature.getBackground().setAlpha(255);
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ButtonGetPicture: {
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("getpicture");
                }
                mBoolGetpicture = true;
                mHandler.sendEmptyMessage(SmartConnectContants.UPDATE_UI_BUTTON);
                break;
            }
            case R.id.ButtonGetTemperature:{
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("gettemperature");
                }
                mBoolGettemperature = true;
                mHandler.sendEmptyMessage(SmartConnectContants.UPDATE_UI_BUTTON);
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
            Log.d(TAG, "Tcp receive:" + values[0]);
           //Toast.makeText(PlugActivity.this, values[0], Toast.LENGTH_SHORT).show();
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
        }

        @Override
        protected void onPostExecute(TCPClient result){
            super.onPostExecute(result);
            Log.d(TAG, "In on post execute");
            if (result != null && result.isRunning()){
                result.stopClient();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mTcpClient != null) {
            mTcpClient.stopClient();
            mTcpClient.out.close();
            try {
                mTcpClient.in.close();
            } catch (Exception e) {
                Log.e(TAG, "cancel error");
            }
            mTcpClient.closeSocket();
        }
//        System.exit(RESULT_OK);
    }
}
