package com.example.wuxiang.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private ToggleButton t;
    private Button b;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView lv;
    private List<HashMap<String,String>> list;
    private MyBaseAdapter baseAdapter;
    private BluetoothDevice devices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = new ArrayList<HashMap<String,String>>();
        init();
        setListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(myReceiver, filter);
       /* filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);*/
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myReceiver, filter);
    }
    private void init(){
        //Android 6.0之后有些权限需要在代码中实现，在清单文件中声明有时候不起作用
        onRequestPermissionsResult(100, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION},null);
        t = (ToggleButton)this.findViewById(R.id.toggleButton);
        b = (Button)this.findViewById(R.id.search);
        lv = (ListView)this.findViewById(R.id.lv_bt);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        t.setChecked(mBluetoothAdapter.isEnabled());
        baseAdapter = new MyBaseAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    if(devices.size()>0){
                        for(Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext();){
                            BluetoothDevice device = (BluetoothDevice)it.next();
                            HashMap<String,String> map = new HashMap<String, String>();
                            map.put("name",device.getName());
                            map.put("address",device.getAddress());
                            list.add(map);
                            lv.setAdapter(baseAdapter);
                        }
                    }else{
                       Toast.makeText(MainActivity.this,"还没有已配对的远程蓝牙设备！",Toast.LENGTH_SHORT).show();
                    }
    }
    private void setListener(){
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t.isChecked()){
                    mBluetoothAdapter.enable();
                }else{
                    Toast.makeText(MainActivity.this,"关闭蓝牙",Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.disable();
                    list.removeAll(list);
                    baseAdapter.notifyDataSetChanged();
                    lv.setAdapter(baseAdapter);
                }
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressBarIndeterminateVisibility(true);
                setTitle("正在扫描....");
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    }
                    // 开始搜索蓝牙设备,搜索到的蓝牙设备通过广播返回
                    mBluetoothAdapter.startDiscovery();
            }
        });
    }
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    setProgressBarIndeterminateVisibility(false);
                    setTitle("搜索蓝牙设备");
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                    switch (state){
                        case BluetoothAdapter.STATE_ON:
                            System.out.println("STATE_ON");
                            t.setChecked(true);
                        case BluetoothAdapter.STATE_OFF:
                            System.out.println("STATE_OFF");
                            if(!mBluetoothAdapter.isEnabled()){
                                t.setChecked(false);
                                list.removeAll(list);
                                baseAdapter.notifyDataSetChanged();
                                lv.setAdapter(baseAdapter);
                            }
                            //t.setChecked(false);

                            break;
                        default:
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    System.out.println("ACTION_FOUND");
                    HashMap<String,String> map = new HashMap<String, String>();
                    devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (devices.getBondState() != BluetoothDevice.BOND_BONDED) {
                        map.put("name",devices.getName());
                        map.put("address",devices.getAddress());
                        list.add(map);
                        System.out.println(devices.getName());
                    }
                    lv.setAdapter(baseAdapter);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }
    class MyBaseAdapter extends BaseAdapter{
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.bt_layout,null);
                holder = new ViewHolder();
                holder.tv = (TextView)convertView.findViewById(R.id.textView);
                holder.tv_ = (TextView)convertView.findViewById(R.id.textView_2);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.tv.setText(list.get(position).get("name")+"");
            holder.tv_.setText(list.get(position).get("address")+"");
            return convertView;
        }
    }
    public class ViewHolder{
        TextView tv;
        TextView tv_;
    }
    class BtAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    };
}
