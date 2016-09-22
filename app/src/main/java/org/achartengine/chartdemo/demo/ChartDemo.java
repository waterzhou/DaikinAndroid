/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chartdemo.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.achartengine.chartdemo.demo.utils.SmartConnectUtils;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartDemo extends Activity {

    private static final String TAG = "Wifi";
    private static final int AddNode = 1001;
    private static final int START_SEARCH = 1003;
    private static final int DELAY = 1000;

    private ListView listView_Node;
    private Menu optionsMenu;

    private  ItemListBaseAdapter mAdapter;
    //private ArrayList<String> listNode = new ArrayList<String>();
    private List<Map<String, Object>> listNode = new ArrayList<Map<String, Object>>();
    private List<Map<String, String>> listProduct = new ArrayList<Map<String, String>>();
    ArrayList<ItemDetails> image_details = new ArrayList<ItemDetails>();


    private Handler mHandler;
    private String dataSource = null;
    private udpbroadcast udpBroadcast = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static AppPreferences mAppPreferences;

    private void startSearch() {
        mHandler.sendEmptyMessageDelayed(START_SEARCH, DELAY);
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        mAppPreferences = new AppPreferences(getApplicationContext());
        SmartConnectUtils.setProtraitOrientationEnabled(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                Log.d(TAG, "Refreshing Number");
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        image_details.clear();
                        listProduct.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                }, 2000);
            }
        });

        listView_Node = (ListView) findViewById(R.id.listView_Node);
         /*1. add listview adapter*/
        mAdapter = new ItemListBaseAdapter(this, image_details);
        listView_Node.setAdapter(mAdapter);
        listView_Node.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                String productType = null;
                Object o = listView_Node.getItemAtPosition(position);
                ItemDetails obj_itemDetails = (ItemDetails)o;
                udpBroadcast.close();
                dataSource = obj_itemDetails.getName();
                for (Map<String, String> tmp : listProduct) {
                    if (tmp.containsKey(dataSource))
                        productType = tmp.get(dataSource);
                }
                //If id1=2, it means elock; then id1=1, it means wps
              /*  if (productType.equals("2")) {
                    Intent intent = new Intent(ChartDemo.this, ElockActivity.class);
                    intent.putExtra("dataSource", dataSource);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (productType.equals("1")) {
                    Intent intent = new Intent(ChartDemo.this, NumChartActivity.class);
                    intent.putExtra("dataSource", dataSource);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (productType.equals("4")) {
                    Intent intent = new Intent(ChartDemo.this, ScaleActivity.class);
                    intent.putExtra("dataSource", dataSource);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else*/ if (productType.equals("2")) {
                    Intent intent = new Intent(ChartDemo.this, PlugActivity.class);
                    intent.putExtra("dataSource", dataSource);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        /*2. add a handler*/
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case AddNode: {
                        Map<String, String> listItem = new HashMap<String, String>();
                        listItem.put(msg.obj.toString(), String.valueOf(msg.arg1));
                        listProduct.add(listItem);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                    case START_SEARCH: {
                        if (udpBroadcast.isClosed())
                            udpBroadcast.open();
                        udpBroadcast.receive();
                        mHandler.sendEmptyMessageDelayed(START_SEARCH, DELAY);
                        break;
                    }
                }

            }
        };

        /*3. Create one udpbroadcast object*/
        udpBroadcast = new udpbroadcast() {

            private String getDataSource(DatagramPacket datagramPacket) {
                int i;
                String data = new String(datagramPacket.getData(), 2, 9);
             /*check the length of name*/
                for (i = 0; i < 9; i++) {
                    if (datagramPacket.getData()[2 + i] == 0)
                        break;
                }
                String ssid = new String(datagramPacket.getData(), 2, i);
                return ssid;
            }

            private boolean IsExsits(String SSID) {
                for (ItemDetails tmp : image_details) {
                    Log.d(TAG, "list node is " + SSID);
                    if (tmp.getName().equals(SSID))
                        return true;
                }
                return false;
            }

            /*
            *  typedef struct s_msg_temp_keepalive {
	        *    uint8_t id0;
	        *    uint8_t id1;
	        *    uint8_t name[9];
	        *    uint8_t type;
            *   } t_msg_temp_keepalive;
            *   For PWS: keepalive:id0=0, id1=1;
            *            data report: id0=0, id1=3;
            *   For E-lock: keepalive: id0=0, id1=2;
            *
            *   For E-scale: keepalive: id0=0, id1=4;
            *
            *
            *
            * */
            @Override
            public void onReceived(List<DatagramPacket> packets) {

                for (DatagramPacket packet : packets) {

                    String data = new String(packet.getData(), 0, packet.getLength());
                    /*Parse ssid*/
                    if (packet.getData()[0] == 0 &&
                            ( packet.getData()[1] == 1 ||
                             packet.getData()[1] == 2 ||
                             packet.getData()[1] == 4 ||
                             packet.getData()[1] == 5) ) {
                        String tmp_ssid = getDataSource(packet);

                        Log.d(TAG, "Get alive report: " + tmp_ssid);
                        if (IsExsits(tmp_ssid) == false) {
                            Log.d(TAG, "Not exist need add it");
                            //For JB version it has an extra quote. so need just ignore it.
                            mAppPreferences.setParameter(tmp_ssid, packet.getAddress().toString().substring(1));
                            ItemDetails item_details = new ItemDetails();
                            item_details.setName(tmp_ssid);
                            if (packet.getData()[1] == 1) {
                                item_details.setItemDescription("Personal weather station");
                                if (packet.getData()[11] == 2) {
                                    item_details.setPrice("Location: office");
                                } else if (packet.getData()[11] == 3){
                                    item_details.setPrice("Location: toilet");
                                }
                                item_details.setImageNumber(1);
                            }
                            if (packet.getData()[1] == 2) {
                                item_details.setItemDescription("air conditon");
                                if (packet.getData()[11] == 2) {
                                    item_details.setPrice("camera&temperature");
                                } else if (packet.getData()[11] == 3){
                                    item_details.setPrice("Location: toilet");
                                }
                                item_details.setImageNumber(2);
                            }
                            if (packet.getData()[1] == 4) {
                                item_details.setItemDescription("E-Scale");
                                if (packet.getData()[11] == 2) {
                                    item_details.setPrice("Location: office");
                                } else if (packet.getData()[11] == 3){
                                    item_details.setPrice("Location: toilet");
                                }
                                item_details.setImageNumber(3);
                            }
                            if (packet.getData()[1] == 5) {
                                item_details.setItemDescription("E-Plug");
                                if (packet.getData()[11] == 2) {
                                    item_details.setPrice("Location: office");
                                } else if (packet.getData()[11] == 3){
                                    item_details.setPrice("Location: toilet");
                                }
                                item_details.setImageNumber(4);
                            }
                            image_details.add(item_details);

                            Message msg = mHandler.obtainMessage(AddNode);
                            msg.obj = tmp_ssid;
                            msg.arg1 = packet.getData()[1];
                            mHandler.sendMessage(msg);
                        } else {

                        }
                    }
                }
            }
        };
        /*4. Start auto update search*/
        startSearch();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.optionsMenu = menu;
        getMenuInflater().inflate(R.menu.wifi_activity_action_bar, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_add_pws: {
                udpBroadcast.close();
                mHandler.removeMessages(START_SEARCH);
                Intent smartConfigActivity = new Intent(this, SmartConnectConfig.class);
                startActivity(smartConfigActivity);
                return true;
            }
            case R.id.menu_about:
                Toast.makeText(getApplicationContext(), "Version 1.0", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    protected void onStop() {
        mHandler.removeMessages(START_SEARCH);
        udpBroadcast.close();
        super.onStop();
    }

    @Override
    protected void onResume() {
        startSearch();
        super.onResume();
    }
}