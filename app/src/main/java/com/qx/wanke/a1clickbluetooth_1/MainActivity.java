package com.qx.wanke.a1clickbluetooth_1;

import android.app.Application;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.R.attr.id;
import static android.bluetooth.BluetoothClass.Service.AUDIO;
import static android.bluetooth.BluetoothClass.Service.TELEPHONY;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class MainActivity extends AppCompatActivity {

    public BluetoothAdapter mBluetoothAdapaer=BluetoothAdapter.getDefaultAdapter();
    public BluetoothA2dp mBluetoothA2dp;
    public BluetoothHeadset mBluetoothHeadset;
    public BluetoothDevice mBluetoothDevice;
    private List<BtDevice> deviceList= new ArrayList<>();
    private List<AppInfo> appInfoList = new ArrayList<>();
    private String TAG = "anil";
    private EditText edittext;
    private Button button_send;
    private Button button_switch;
    private List<Apps> appList;
    private BtDeviceAdapter adapter;
    private AppInfoAdapter adapter2;
    private RecyclerView recyclerView2;
    private Context mcontext=this;


    private IntentFilter intentFilter;
    private BtStateReceiver btStateReceiver;

    private IntentFilter a2dpIntentFilter;
    private A2dpReceiver a2dpReceiver;

    private IntentFilter headsetIntentFilter;
    private HeadsetReceiver headsetReceiver;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_main);

        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){actionBar.hide();}

        Connector.getDatabase();

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btStateReceiver=new BtStateReceiver();
        registerReceiver(btStateReceiver, intentFilter);

        a2dpIntentFilter = new IntentFilter();
        a2dpIntentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        a2dpReceiver=new A2dpReceiver();
        registerReceiver(a2dpReceiver, a2dpIntentFilter);

        headsetIntentFilter = new IntentFilter();
        headsetIntentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        headsetReceiver=new HeadsetReceiver();
        registerReceiver(headsetReceiver, headsetIntentFilter);

        initBtDevices();

        final RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new BtDeviceAdapter(deviceList,MainActivity.this);

        adapter.setmOnA2dpClickListener(new BtDeviceAdapter.OnA2dpClickListener() {
            @Override
            public void onA2dpClick(int position) {
                if(!mBluetoothAdapaer.isEnabled()) {
                    mBluetoothAdapaer.enable();
                    Toast.makeText(MainActivity.this,"正在为你打开蓝牙，请稍候1-2秒…",Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
                if(deviceList.get(position).getA2dp()==null){return;}
                switch (mBluetoothA2dp.getConnectionState(mBluetoothDevice)){
                    case STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的音频协议(A2DP)",Toast.LENGTH_SHORT).show();
                        disconnectA2dp();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接音频协议(A2DP)，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开音频协议(A2DP)，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的音频协议(A2DP)",Toast.LENGTH_SHORT).show();
                        connectA2dp();
                        break;
                }
            }
        });

        adapter.setmOnHeadsetClickListener(new BtDeviceAdapter.OnHeadsetClickListener() {
            @Override
            public void onHeadsetClick(int position) {
                if(!mBluetoothAdapaer.isEnabled()) {
                    mBluetoothAdapaer.enable();
                    Toast.makeText(MainActivity.this,"正在为你打开蓝牙，请稍候1-2秒…",Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
                if(deviceList.get(position).getHeadset()==null){return;}
                switch (mBluetoothHeadset.getConnectionState(mBluetoothDevice)){
                    case STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的电话协议(Headset)",Toast.LENGTH_SHORT).show();
                        disconnectHeadset();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接电话协议(Headset)，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开电话协议(Headset)，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的电话协议(Headset)",Toast.LENGTH_SHORT).show();
                        connectHeadset();
                        break;
                }
            }
        });

        adapter.setmOnImageClickListener(new BtDeviceAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

            if(!mBluetoothAdapaer.isEnabled()) {
                mBluetoothAdapaer.enable();
                Toast.makeText(MainActivity.this,"正在为你打开蓝牙，请稍候1-2秒…",Toast.LENGTH_SHORT).show();
                return;
            }

                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());

                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)== STATE_CONNECTED||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)== STATE_CONNECTED){
                    Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName(),Toast.LENGTH_SHORT).show();
                    disconnectA2dp();
                    disconnectHeadset();
                    return;
                }
                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTED||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTED){
                    Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName(),Toast.LENGTH_SHORT).show();
                    connectA2dp();
                    connectHeadset();
                    return;
                }
                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTING||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTING){
                    Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接，请稍慢点击。",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTING||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTING){
                    Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开，请稍慢点击。",Toast.LENGTH_SHORT).show();
                return;
                }



            }
        });
        adapter.setmOnItemLongClickListener(new BtDeviceAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "You long clicked the position " + String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });

        ItemTouchHelper.Callback callback=new MyItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper=new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(adapter);


        initAppList();
//        Log.d(TAG, "onCreate: initAppList()执行。");

        recyclerView2 = (RecyclerView) findViewById(R.id.recycler_view2);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView2.setLayoutManager(layoutManager2);
        adapter2 = new AppInfoAdapter(appInfoList);

        adapter2.setOnItemClickListener(new AppInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            Intent intent=appInfoList.get(position).getIntent();
            List<ResolveInfo> list =  MainActivity.this.getPackageManager().queryIntentActivities(intent, 0);
            if(list.size()>0) {
                Toast.makeText(MainActivity.this,"为你启动："+appInfoList.get(position).getAppLable(),Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }else{
                Toast.makeText(MainActivity.this,"你是不是把"+appInfoList.get(position).getAppLable()+"给咔嚓了呀？如果是的话，" +
                        "就先别点它了。你下次启动我的时候我会帮你把它从列表里删掉的:-)",Toast.LENGTH_SHORT).show();
            }
            }
        });

        ItemTouchHelper.Callback callback2=new MyItemTouchHelperCallback(adapter2);
        ItemTouchHelper touchHelper2=new ItemTouchHelper(callback2);
        touchHelper2.attachToRecyclerView(recyclerView2);

        recyclerView2.setAdapter(adapter2);


        button_send = (Button)findViewById(R.id.button_send);
        edittext = (EditText) findViewById(R.id.input);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                String appName=edittext.getText().toString();
                if (!TextUtils.isEmpty(appName)) {
                    edittext.setText(null);
                    appList = DataSupport
                            .where("label like ?", "%" + appName + "%")
                            .find(Apps.class);

                    if (appList.size() == 0) {
                        InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                        Toast.makeText(MainActivity.this, "没找到含有“"+appName+"”的app，请重新输入", Toast.LENGTH_SHORT).show();
                    } else {
                        InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                        int maxApp = DataSupport.max(Apps.class, "order1", int.class);
                        for (Apps app : appList) {
                            maxApp++;

                            Apps updateApp=new Apps();
                            updateApp.setOrder1(maxApp);
                            updateApp.updateAll("label=?",app.getLabel());

                        }
                        initAppList();
                        adapter2.notifyDataSetChanged();
                        recyclerView2.scrollToPosition(appInfoList.size()-1);
                    }
                }
            }
        });

        Button button_sys=(Button)findViewById(R.id.button_sys);
        button_sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });

        button_switch=(Button)findViewById(R.id.button_switch);
        button_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapaer.isEnabled()){
                    mBluetoothAdapaer.disable();
                    Devices devices=new Devices();
                    devices.setToDefault("a2dp_conn");
                    devices.setToDefault("headset_conn");
                    devices.updateAll();
                    initBtDevices();
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(MainActivity.this,"尝试打开蓝牙",Toast.LENGTH_SHORT).show();
                    mBluetoothAdapaer.enable();
                }

            }
        });

        Button btn_setting = (Button) findViewById(R.id.button_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LessonActivity.class);
                startActivity(intent);
            }
        });

        button_send.setTextColor(Color.BLACK);
        button_send.setEnabled(false);
        edittext.setHint("正在为你索引手机里所有app，请稍后提交…");
        new GetAppsTask().execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mBluetoothAdapaer.isEnabled()){
            button_switch.setTextColor(Color.BLUE);
            getBluetoothA2dp();
            getBluetoothHeadset();
            getBtDevices();
            setColor();
        }else{
            button_switch.setTextColor(Color.WHITE);
            Devices devices = new Devices();
            devices.setToDefault("a2dp_conn");
            devices.setToDefault("headset_conn");
            devices.updateAll();
        }
        initBtDevices();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btStateReceiver);
        unregisterReceiver(a2dpReceiver);
        unregisterReceiver(headsetReceiver);
    }

    class BtStateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
                    switch (blueState){

                        case BluetoothAdapter.STATE_ON:
                            button_switch.setTextColor(Color.BLUE);
                            Toast.makeText(context,"蓝牙已打开",Toast.LENGTH_SHORT).show();

                            getBluetoothA2dp();
                            getBluetoothHeadset();
                            getBtDevices();
                            setColor();
                            initBtDevices();
                            adapter.notifyDataSetChanged();

                            break;
                        case BluetoothAdapter.STATE_OFF:
                            button_switch.setTextColor(Color.WHITE);
                            Toast.makeText(context,"蓝牙已关闭",Toast.LENGTH_SHORT).show();
                            setColor();
                            initBtDevices();
                            adapter.notifyDataSetChanged();
                            break;
                    }
                    break;
            }
        }
    }

    class A2dpReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int a2dpState=intent.getIntExtra(BluetoothA2dp.EXTRA_STATE,0);
            if(a2dpState==STATE_CONNECTED||a2dpState==STATE_DISCONNECTED){
                setColor();
                initBtDevices();
                adapter.notifyDataSetChanged();
            }
        }
    }

    class HeadsetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int headsetState=intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,0);
            if(headsetState==STATE_CONNECTED||headsetState==STATE_DISCONNECTED){
                setColor();
                initBtDevices();
                adapter.notifyDataSetChanged();
            }
        }
    }

    class GetAppsTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            getApps();
            initAppList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            edittext.setHint("输入app名称，点提交可加入上方启动app列表…");
            button_send.setTextColor(Color.WHITE);
            button_send.setEnabled(true);
            adapter2.notifyDataSetChanged();
        }
    }

    class updateDevListTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mBluetoothAdapaer.isEnabled()) {
                getBluetoothA2dp();
                getBluetoothHeadset();
                getBtDevices();
                setColor();
                initBtDevices();

            } else {
                Devices devices = new Devices();
                devices.setToDefault("a2dp_conn");
                devices.setToDefault("headset_conn");
                devices.updateAll();
                initBtDevices();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                initBtDevices();
                adapter.notifyDataSetChanged();
        }
    }

    private void getBtDevices(){
        Devices updateDevices=new Devices();
        updateDevices.setToDefault("exist");
        updateDevices.updateAll();

        Resources res=getResources();
        Bitmap bmp= BitmapFactory.decodeResource(res,R.drawable.bluetooth);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] img=baos.toByteArray();

        Set<BluetoothDevice> pairedDevices=mBluetoothAdapaer.getBondedDevices();
        if(pairedDevices.size()>0){
            int i=1;
            for(BluetoothDevice device:pairedDevices){
                List<Devices> catchDevice = DataSupport.where("mac=?", device.getAddress()).find(Devices.class);

                Devices devices = new Devices();
                Log.d(TAG, "getBtDevices: "+device.getName()+"-"+String.valueOf(device.getBluetoothClass().getDeviceClass()));

                if (catchDevice.size()==0){
                    devices.setExist(1);
                    devices.setLabel(device.getName());
                    devices.setSys_label(device.getName());
                    devices.setMac(device.getAddress());
                    devices.setOrder1(i);
                    devices.setDev_img(img);
                    devices.save();
                }else{
                    devices.setExist(1);
                    devices.updateAll("mac=?",device.getAddress());
                }
                i=i+1;
            }
        }
    }

    private void initBtDevices(){
        deviceList.clear();

        List<Devices> devicesList=DataSupport.where("exist=? and order1>?","1","0")
                .order("order1")
                .find(Devices.class);
        for(Devices devices:devicesList){
            BtDevice btDevice=new BtDevice(devices.getLabel(),devices.getMac(),
                    BitmapFactory.decodeByteArray(devices.getDev_img(),0,devices.getDev_img().length),
                    devices.getA2dp(),devices.getA2dp_conn(),devices.getHeadset(),devices.getHeadset_conn(),devices.getId());
            deviceList.add(btDevice);
        }
    }

    public void getBluetoothA2dp(){
        Log.d(TAG, "getBluetoothA2dp()开始执行…… ");
        mBluetoothAdapaer.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                Log.d(TAG, "onServiceConnected: ");
                if(profile==BluetoothProfile.A2DP){
                    mBluetoothA2dp=(BluetoothA2dp) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
            }
        },BluetoothProfile.A2DP);
    }

    private void getBluetoothHeadset(){
        mBluetoothAdapaer.getProfileProxy(MainActivity.this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if(profile==BluetoothProfile.HEADSET){
                    mBluetoothHeadset=(BluetoothHeadset)proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
            }
        },BluetoothProfile.HEADSET);
    }

    private void connectA2dp(){
        Method connect_method=null;
        if(mBluetoothDevice.getBluetoothClass().getMajorDeviceClass()==1024){
            try {
                connect_method = mBluetoothA2dp.getClass().getMethod("connect", BluetoothDevice.class);
                connect_method.setAccessible(true);
                connect_method.invoke(mBluetoothA2dp,mBluetoothDevice);
            }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
        }
        return;
    }

    private void connectHeadset(){
        Method connect_method=null;
            try {
                connect_method = mBluetoothHeadset.getClass().getMethod("connect", BluetoothDevice.class);
                connect_method.setAccessible(true);
                connect_method.invoke(mBluetoothHeadset,mBluetoothDevice);
            }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
        return;
    }

    private void disconnectA2dp() {
        Method disconnect_method = null;
        try {
            disconnect_method = mBluetoothA2dp.getClass().getMethod("disconnect", BluetoothDevice.class);
            disconnect_method.setAccessible(true);
            disconnect_method.invoke(mBluetoothA2dp, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void disconnectHeadset() {
        Method disconnect_method = null;
        try {
            disconnect_method = mBluetoothHeadset.getClass().getMethod("disconnect", BluetoothDevice.class);
            disconnect_method.setAccessible(true);
            disconnect_method.invoke(mBluetoothHeadset, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setColor() {
        if (!mBluetoothAdapaer.isEnabled() || mBluetoothA2dp==null) {
            return;
        }
        List<BluetoothDevice> connectedA2dpDevices = mBluetoothA2dp.getConnectedDevices();
        if (connectedA2dpDevices.size() == 0) {
            Log.d(TAG, "setColor: 没找到已连接的a2dp设备");
            Devices device=new Devices();
            device.setToDefault("a2dp_conn");
            device.updateAll();
        }else{
            Log.d(TAG, "setColor: 找到了连接的a2dp设备");
            for(BluetoothDevice connectedA2dpDevice:connectedA2dpDevices){
                ContentValues values = new ContentValues();
                values.put("a2dp_conn", 1);
                DataSupport.updateAll(Devices.class,values,"mac=?",connectedA2dpDevice.getAddress());
            }
        }
        List<BluetoothDevice> connectedHeadsetDevices=mBluetoothHeadset.getConnectedDevices();
        if(connectedHeadsetDevices.size()==0){
            Devices device=new Devices();
            device.setToDefault("headset_conn");
            device.updateAll();
        }else{
            for(BluetoothDevice connectedHeadsetDevice:connectedHeadsetDevices){
                ContentValues values = new ContentValues();
                values.put("headset_conn",1);
                DataSupport.updateAll(Devices.class, values, "mac=?", connectedHeadsetDevice.getAddress());
            }
        }
    }

    private void getApps(){

        Apps updateApps=new Apps();
        updateApps.setToDefault("exist");
        updateApps.updateAll();

        PackageManager pm=getPackageManager();

        Intent mintent = new Intent(Intent.ACTION_MAIN, null);
        mintent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(mintent, 0);
        for(int i=0;i<allApps.size();i++){
            ResolveInfo appInfo = allApps.get(i);
            List<Apps> catchAppList = DataSupport.where("package_name = ?", appInfo.activityInfo.packageName).find(Apps.class);
            if(catchAppList.size()==0){
                Apps apps=new Apps();
                apps.setPackage_name(appInfo.activityInfo.packageName);
                apps.setExist(1);
                apps.setLabel(appInfo.loadLabel(pm).toString());
                apps.save();
            }else{
                Apps updateApp = new Apps();
                updateApp.setExist(1);
                updateApp.setLabel(appInfo.loadLabel(pm).toString());
                updateApp.updateAll("package_name=?",appInfo.activityInfo.packageName);
            }
        }
        DataSupport.deleteAll(Apps.class,"exist = ?","0");
    }

    private void initAppList(){
        appInfoList.clear();
        List<Apps> apps=DataSupport.select("package_name")
                .where("exist=? and order1>?","1","0")
                .order("order1")
                .find(Apps.class);
        for (Apps app : apps) {
            PackageManager pm = getPackageManager();
            AppInfo appInfo=new AppInfo();
            try {
                ApplicationInfo info = pm.getApplicationInfo(app.getPackage_name(), 0);
                appInfo.setAppIcon(info.loadIcon(pm));
                appInfo.setAppLable(info.loadLabel(pm).toString());

                appInfo.setIntent(pm.getLaunchIntentForPackage(app.getPackage_name()));
                appInfo.setId(app.getId());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appInfoList.add(appInfo);
        }
    }
}
