package com.qx.wanke.a1clickbluetooth_1;

import android.app.Application;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private List<Apps> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //防止要输入app名称时，软键盘挡住EditText
        setContentView(R.layout.activity_main);

        if(DataSupport.count(Apps.class)==0) {
            getApps();
        }

//        Apps updateApps=new Apps();
//        updateApps.setToDefault("order1");
//        updateApps.updateAll();

//        for(int id=1;id<DataSupport.count(Apps.class);id++){
//            Apps apps=DataSupport.find(Apps.class,id);
//            Log.d(TAG, apps.getLabel()+String.valueOf(apps.getExist()) +String.valueOf(apps.getOrder()));
//        }

        if(!mBluetoothAdapaer.isEnabled()){
          mBluetoothAdapaer.enable();
        }
        while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}

        getBluetoothA2dp();
//      这句getBlueToothA2dp()写在setOnItemClickListener里面，运行到“getBluetoothA2dp()开始执行……” 就程序闪退
//        无法Log到“onServiceConnected:”。移到这里就正常。连接小米蓝牙音箱成功。why？

        initBtDevices();
        final RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        BtDeviceAdapter adapter=new BtDeviceAdapter(deviceList);

        adapter.setOnItemClickListener(new BtDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,"You clicked the position "+String.valueOf(position),Toast.LENGTH_SHORT).show();
//              这里用this，报错。parent.getContext()等的区别？
                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
//                BtDevices btDevice = btList.get(position);
                connect();
            }
        });
        adapter.setmOnItemLongClickListener(new BtDeviceAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "You long clicked the position " + String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        initAppList();
        Log.d(TAG, "onCreate: initAppList()执行。");

        final RecyclerView recyclerView2 = (RecyclerView) findViewById(R.id.recycler_view2);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView2.setLayoutManager(layoutManager2);
        final AppInfoAdapter adapter2 = new AppInfoAdapter(appInfoList);

        adapter2.setOnItemClickListener(new AppInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            Toast.makeText(MainActivity.this,"you click the mAppList "+String.valueOf(position),Toast.LENGTH_SHORT).show();
            Intent intent=appInfoList.get(position).getIntent();
            startActivity(intent);
            }
        });

        ItemTouchHelper.Callback callback=new MyItemTouchHelperCallback(adapter2);
//        这里的adapter和ITHAdapter各自是什么情况？
        ItemTouchHelper touchHelper=new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView2);

        recyclerView2.setAdapter(adapter2);

        button_send = (Button)findViewById(R.id.button_send);
        edittext = (EditText) findViewById(R.id.input);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                String appName=edittext.getText().toString();
//                如果在-5行处写Edittext edittext，就说是内部类，要求把edittext声明成final才行，改成
//                在Activity的开头写private Edittext edittext;就不提示，why？
//                List<Apps> app_list = DataSupport.select("id","package_name","label", "intent", "order")
//                        .where("label=?", appName)
//                        .limit(1)
//                        .find(Apps.class);

//                List<Apps> applist=DataSupport.findAll(Apps.class);
                if (!TextUtils.isEmpty(appName)) {
                    appList = DataSupport
//                            .select("label","order")
//                        .where("label=?", appName)
//                        .where("exist=? and label like ?","1","%"+appName+"%")
                            .where("label like ?", "%" + appName + "%")
//                        改上一句的精确查询为现在的模糊查询
//                        .limit(1)
                            .find(Apps.class);
                    Log.d(TAG, "onClick:模糊查找app ");

                    if (appList.size() == 0) {
                        Log.d(TAG, "app is not exist.");
                    } else {
//                        int maxApp = DataSupport.where("order>?", "0").count(Apps.class);
                        int maxApp = DataSupport.max(Apps.class, "order1", int.class);
                        //order列求max总是闪退，改为exist列，不闪退。why？想了很久，发现order是litepal的保留字，不能做列名
                        Log.d(TAG, "onClick: " + String.valueOf(maxApp));
                        for (Apps app : appList) {
                            maxApp++;

                            Apps updateApp=new Apps();
                            updateApp.setOrder1(maxApp);
                            updateApp.updateAll("label=?",app.getLabel());

//                            app.setOrder(maxApp);
//                            app.save();

                            Log.d(TAG, "app name is " + app.getLabel()+" order1 is "+String.valueOf(app.getOrder1())+
                                    " exist is "+String.valueOf(app.getExist()));
                        }
                        initAppList();
                        recyclerView2.scrollToPosition(appInfoList.size()-1);
                    }
                }
                //错误之处: applist写apps，导致变量引用不清。packagename写成packageName，导致运行到此处崩溃
//                if (applist.size()==0){
//                    Log.d(TAG, "onClick: 没找到");
//                }else{
//                    for(Apps app:applist){
//                         Log.d(TAG, "app name is "+app.getLabel());}
//                }
            }
        });

        Button button_sys=(Button)findViewById(R.id.button_sys);
        button_sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });

        Button button_switch=(Button)findViewById(R.id.button_switch);
        button_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapaer.isEnabled()){
                    mBluetoothAdapaer.disable();
                }else mBluetoothAdapaer.enable();
            }
        });
    }

    private void initBtDevices(){
        Set<BluetoothDevice> pairedDevices=mBluetoothAdapaer.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                BtDevice btDevice=new BtDevice(device.getName(),device.getAddress(),R.drawable.lyej_80);
                deviceList.add(btDevice);
            }
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

    private void connect(){
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
    }

    private void getApps(){
        Connector.getDatabase();
//        DataSupport.deleteAll(Apps.class);

        Apps updateApps=new Apps();
        updateApps.setToDefault("exist");
        updateApps.updateAll();

        PackageManager pm=getPackageManager();
        List<PackageInfo> packageInfos=pm.getInstalledPackages(0);
        for(int i=0;i<packageInfos.size();i++){
            PackageInfo packageInfo = packageInfos.get(i);

            List<Apps> catchAppList = DataSupport.where("package_name = ?", packageInfo.packageName).find(Apps.class);
            if(catchAppList.size()==0) {
                Apps apps = new Apps();
                apps.setPackage_name(packageInfo.packageName);
                apps.setExist(1);
                apps.setLabel(packageInfo.applicationInfo.loadLabel(pm).toString());
//                Intent launchIntent = new Intent();
//              launchIntent.setComponent(new ComponentName(packageInfo.packageName, packageInfo.applicationInfo.loadLabel(pm).toString()));
//               参考wiz中 辅助功能 里的，packagemanager里《android获取应用程序包一》
//              apps.setIntent(launchIntent.toString());
                apps.save();
                Log.d(TAG, "getApps: "+String.valueOf(i)+"-"+apps.getPackage_name()+"-"+apps.getLabel()+"-"+apps.getExist());
            }else{
                Apps updateApp=new Apps();
                updateApp.setExist(1);
                updateApp.updateAll("package_name = ?", packageInfo.packageName);
                Log.d(TAG, "getApps: update "+String.valueOf(i)+"-"+packageInfo.packageName+"-"+updateApp.getLabel()+"-"+" exist="+String.valueOf(updateApp.getExist()));
            }
        }
    }

    private void initAppList(){
        appInfoList.clear();
//        加这句解决每次添加app时，当前的app会存在2遍的问题；用appInfoList=null;报错，闪退
//        Caused by: java.lang.NullPointerException: Attempt to invoke interface method 'boolean java.util.List.add(java.lang.Object)' on a null object reference
        List<Apps> apps=DataSupport.select("package_name")
                .where("exist=? and order1>?","1","0")
                .order("order1")
                .find(Apps.class);
        Log.d(TAG, "initAppList: ");
        for (Apps app : apps) {
            PackageManager pm = getPackageManager();
            AppInfo appInfo=new AppInfo();
            Log.d(TAG, "initAppList: "+app.getPackage_name());
            try {
                ApplicationInfo info = pm.getApplicationInfo(app.getPackage_name(), 0);
                appInfo.setAppIcon(info.loadIcon(pm));
                appInfo.setAppLable(info.loadLabel(pm).toString()); //为什么要toString? loadLabel不是字符串？
                appInfo.setIntent(pm.getLaunchIntentForPackage(app.getPackage_name()));
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//            Application application=pm.getPackageInfo(app.getPackage_name(),0).applicationInfo;
//            AppInfo appInfo=new AppInfo();
//            appInfo.setAppIcon(application.);
            appInfoList.add(appInfo);
        }
    }
}
