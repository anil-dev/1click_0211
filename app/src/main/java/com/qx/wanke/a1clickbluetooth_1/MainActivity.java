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
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import static android.bluetooth.BluetoothClass.Service.AUDIO;
import static android.bluetooth.BluetoothClass.Service.TELEPHONY;

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
    private BtDeviceAdapter adapter;
    private Context mcontext=this;
//    private int connected=-1;
//    connected表示当前连接的设备在列表中的序号（如果连接后拖动更改了排序呢？也需要在拖动的方法里同时更改connected值）。
//    需要先赋初值-1，表示没有设备被连接。另外，还需要在打开蓝牙的时候，自动连接的设备监测方法里，修改connected值。


    private IntentFilter intentFilter;
    private BtStateReceiver btStateReceiver;
//    2017.3.18，《第二行代码》P171，广播接收器。继承BroadcastReceiver，监听intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//    不再使用while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}，1、使app启动白屏时间缩短0.5s 2、在app里需要随时监听蓝牙变化，以
//    改变按钮颜色、（是否 蓝牙关闭时，点击设备，要通过这个来打开蓝牙后连接设备？）


//    SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //防止要输入app名称时，软键盘挡住EditText
        setContentView(R.layout.activity_main);

/*      使用recyclerview的ondismiss修改设备信息（还尝试了用按钮setOffLongItemClick切换长按修改和长按拖拽），不需要用preferences文件了。
        editor=getSharedPreferences("data",MODE_PRIVATE).edit();
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        Log.d(TAG, "onCreate: 准备赋值flag");
        editor.putInt("flag",1);
        editor.apply();
//        看书不仔细！没写这句apply(),导致flag一直没写进去，后面读到的永远是0.
//        这句写在onCreate()外层，是报错的，要写在这里。外层只能声明变量。
        Log.d(TAG, "onCreate: flag 赋值 "+pref.getInt("flag",0));*/

        Connector.getDatabase();
        //        DataSupport.deleteAll(Apps.class);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btStateReceiver=new BtStateReceiver();
        registerReceiver(btStateReceiver, intentFilter);

        initBtDevices();

        final RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new BtDeviceAdapter(deviceList,MainActivity.this);
//        这里传入MainActivity.this，为了能在adapter里启动DeviceActivity
//        BtDeviceAdapter adapter=new BtDeviceAdapter(deviceList);

        adapter.setmOnA2dpClickListener(new BtDeviceAdapter.OnA2dpClickListener() {
            @Override
            public void onA2dpClick(int position) {
                if(!mBluetoothAdapaer.isEnabled()) {
                    mBluetoothAdapaer.enable();
                    Toast.makeText(MainActivity.this,"正在打开蓝牙，请1秒后再点击"+deviceList.get(position).getBtName()+"的图标",Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
                if(deviceList.get(position).getA2dp()==null){return;}
                switch (mBluetoothA2dp.getConnectionState(mBluetoothDevice)){
                    case BluetoothProfile.STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的A2dp",Toast.LENGTH_SHORT).show();
                        disconnectA2dp();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接A2dp，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开A2dp，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的A2dp",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this,"正在打开蓝牙，请1秒后再点击"+deviceList.get(position).getBtName()+"的图标",Toast.LENGTH_SHORT).show();
                    return;
                }
                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
                if(deviceList.get(position).getHeadset()==null){return;}
                switch (mBluetoothHeadset.getConnectionState(mBluetoothDevice)){
                    case BluetoothProfile.STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的Headset",Toast.LENGTH_SHORT).show();
                        disconnectHeadset();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接Headset，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开Headset，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的Headset",Toast.LENGTH_SHORT).show();
                        connectHeadset();
                        break;
                }
            }
        });

        adapter.setmOnImageClickListener(new BtDeviceAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {
//            Toast.makeText(MainActivity.this,"You clicked the position "+String.valueOf(position),Toast.LENGTH_SHORT).show();
//              这里用this，报错。parent.getContext()等的区别？
//                郭霖P131提示，是否可以用v.getContext()? 有空试试。

            if(!mBluetoothAdapaer.isEnabled()) {
                mBluetoothAdapaer.enable();
//                while (mBluetoothAdapaer.getState() != BluetoothAdapter.STATE_ON) {}
                Toast.makeText(MainActivity.this,"正在打开蓝牙，请1秒后再点击"+deviceList.get(position).getBtName()+"的图标",Toast.LENGTH_SHORT).show();
                return;
            }
//                getBluetoothA2dp();
//                Log.d(TAG, "onItemClick: A2dp is gotten.");
//                Toast.makeText(MainActivity.this,"A2dp is gotten.",Toast.LENGTH_SHORT).show();
//                2017.3.19-调整了MainActivity结构（原来是直接开蓝牙，等获得BluetoothAdapter后获取A2dp，再全部显示，现在是直接显示界面，监听
//                BluetoothAdapter.ACTION_STATE_CHANGED，ON后用BtAdapter获得A2dp。以前的故障在这里又复现。在onItemClickListener里面不能getBluetoothA2dp,
//                只能在Main里调用getBluetoothA2dp获取，在这里获取，会获取不到，导致下面的connect中的getClass()闪退。为什么？
//                在Main外面的class BtStateReceiver extends BroadcastReceiver，调用getBluetoothA2dp，也能获取A2dp。为什么？

                mBluetoothDevice = mBluetoothAdapaer.getRemoteDevice(deviceList.get(position).getBtMacAdress());
//                BtDevices btDevice = btList.get(position);
//                connect();

//                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTED){
//                    disconnect();
//                }else{
//                    connect();
//                }
                Log.d("anil", "onItemClick: "+String.valueOf(mBluetoothA2dp==null));
//                进入app后隔几秒后，不论开关蓝牙，再点击设备，都能正常获得A2dp。但只要是关闭蓝牙时进入app，再立刻点击设备，就无法获得A2dp，导致
//                下面一句switch（)闪退。为什么？
//                见下面+100多行处，判断蓝牙是否打开的地方有5条解释。主因是：进入app后，立刻点击设备，会闪退。
//                  分析执行流程，应该是点击设备时，程序打开蓝牙，while判断蓝牙已开，就往下执行用A2dp拿设备
//                  的连接状态，而此时，蓝牙打开的广播监听到了，但未执行getA2dp
//                while(mBluetoothA2dp==null){};

                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTED||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTED){
                    disconnectA2dp();
                    disconnectHeadset();
//                    if(deviceList.get(position).getA2dp()!=null)。需不需要加判断是否勾选的单独的连接断开？还是不加吧，要不有些
//                    设备有A2dp而用户没有勾选，就会断不掉。
                    return;
//                    这句return;还必须有，没有的话，会关不掉音箱。正在唱歌的音箱，点击设备后，先关闭，再弹toast说正连接，稍慢点，再重新连接，
//                    等于把下面几个判断全部走了一圈。要思考一下为什么？执行完一条判断后，不是应该往下走，直接走出这个点击事件了吗？
                }
                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTED||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_DISCONNECTED){
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

//                    mBluetoothHeadset.getConnectionState(mBluetoothDevice)==BluetoothHeadset.STATE_AUDIO_CONNECTED，写上面这句自动弹出的
//                _AUDIO_CONNECTED是什么鬼？
/*              这一大段的整体思路是，用两个switch分别判断被点击的设备A2dp和headset是否开启，没开，则打开，开了，则关闭。
                但这样的逻辑不对，有可能导致一个设备只连接了a2dp，被点击后会断开a2dp，而同时再打开headset，重新用if判断写在上面。
               switch (mBluetoothA2dp.getConnectionState(mBluetoothDevice)){
                    case BluetoothProfile.STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的整体",Toast.LENGTH_SHORT).show();
//                        for(int i=1;i<50000;i++){}
                        disconnectA2dp();
//                        disconnectHeadset();
//                        while(mBluetoothA2dp.getConnectionState(mBluetoothDevice)!=BluetoothProfile.STATE_DISCONNECTED){}
//                        加了这句，仍有连续两次点击设备（打算连上就断），会显示“断开”，然后直接再次连接。为什么？
//                          仔细观察流程，disconnect()是执行了的，并且设备也断开了，只是在断开后，没有再走这个onClick片段，
//                          android系统自己主动再次连接了蓝牙，进入系统的蓝牙设置里，
//                          尝试这种连上就断的方式，发现系统蓝牙也是这个德性，连上后就断（点确定），也会断开后，重新连接。是因为底层判断短时间
//                          两次连接，不判断为断开，而自动重连吗？
//                        while(mBluetoothA2dp.getConnectionState(mBluetoothDevice)==BluetoothProfile.STATE_CONNECTING){}
                        break;

                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接整体，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开整体，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的整体",Toast.LENGTH_SHORT).show();
                        connectA2dp();
//                        connectHeadset();
                        break;
                }

                switch (mBluetoothHeadset.getConnectionState(mBluetoothDevice)){
                    case BluetoothProfile.STATE_CONNECTED:
                        Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName()+"的Headset",Toast.LENGTH_SHORT).show();
                        disconnectHeadset();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试连接Headset，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Toast.makeText(MainActivity.this,deviceList.get(position).getBtName()+"正在尝试断开headset，请稍慢点击。",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Toast.makeText(MainActivity.this,"尝试连接"+deviceList.get(position).getBtName()+"的Headset",Toast.LENGTH_SHORT).show();
                        connectHeadset();
                        break;
                }*/

//                if (connected!=position) {
//                    Boolean isConnected=connect();
//                    Toast.makeText(MainActivity.this,String.valueOf(isConnected),Toast.LENGTH_SHORT).show();
//                    if (isConnected) {
//                        connected = position;
//                    }
//                }else{
//                    disconnect();
//                    connected=-1;
//                }
//                用connected标志位的问题是：有些时候，打开app就会自动连接蓝牙设备，而并没有监听蓝牙的连接状态，所以导致该设备并没有写入connected位置
//                尝试用BluetoothA2dp的getConnectionState来写。
//                最好的实现是：进入app后，监听各设备的连接状态，数据库里增加connected栏，是连接的就写入1（A2DP）或2（HFP)，再在recycler里用蓝色反应
//                出来。


            }
        });
        adapter.setmOnItemLongClickListener(new BtDeviceAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "You long clicked the position " + String.valueOf(position), Toast.LENGTH_SHORT).show();
//                上面这句执行不到。是不是因为加了ItemTouchHelper，导致长按被它拦截了？想去ITH里面写Toast提示，结果context不知道用什么。
            }
        });

//        ItemTouchHelper.Callback callback=new MyItemTouchHelperCallback(adapter);
        ItemTouchHelper.Callback callback=new MyItemTouchHelperCallback(adapter);
//        写完这句，死活提示adpater错误，运行提示：Error:(119, 73) 错误: 不兼容的类型: BtDeviceAdapter无法转换为ItemTouchHelperAdapter
//        仔细想想，原来是BtDeviceAdapter声明时没有写implements 这个抽象类
        ItemTouchHelper touchHelper=new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

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
//            Toast.makeText(MainActivity.this,"you click the mAppList "+String.valueOf(position),Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this,"为你启动："+appInfoList.get(position).getAppLable(),Toast.LENGTH_SHORT).show();
            Intent intent=appInfoList.get(position).getIntent();
            startActivity(intent);
            }
        });

        ItemTouchHelper.Callback callback2=new MyItemTouchHelperCallback(adapter2);
//        这里的adapter和ITHAdapter各自是什么情况？
        ItemTouchHelper touchHelper2=new ItemTouchHelper(callback2);
        touchHelper2.attachToRecyclerView(recyclerView2);

        recyclerView2.setAdapter(adapter2);

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

/*        if(!mBluetoothAdapaer.isEnabled()){
            mBluetoothAdapaer.enable();
        }
//        else{
//            getBluetoothA2dp();
//        }

//        while(mBluetoothAdapaer==null){}  这句没这种写法，下面写法才是正确判断蓝牙是否已开
        while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}
        getBluetoothA2dp();
//        1、用else来打开A2dp不对，不论蓝牙何时开启，都应当获取A2dp。
//        2、为什么加上这段，打开app就要白屏很久？不是应该显示界面，然后蓝牙归蓝牙开启吗？（主因就是
//          while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}这句，不写这句，界面打开也很快）*/
        if(mBluetoothAdapaer.isEnabled()){
            getBluetoothA2dp();
            getBluetoothHeadset();
        }
//        基本解决获取不到A2dp，导致在点击设备时闪退的情况（
//          1、在没开蓝牙，进入app后，立刻点击设备，会闪退。分析执行流程，应该是点击设备时，程序打开蓝牙，while判断蓝牙已开，就往下执行用A2dp拿设备
//        的连接状态，而此时，蓝牙打开的广播监听到了，但未执行getA2dp
//          2、如果在onCreate()里，直接打开蓝牙，并等蓝牙打开后，执行getA2dp，会白屏很久
//          3、去掉上面这段，在recyclerview判断点击事件里，如果蓝牙没开，就打开，并提示等1s再点击，然后return掉这次点击，这时，广播监听应该能拿到A2dp了，
//        这两个流程不是异步吧？什么样的执行顺序？
//          4、但去掉上面这段后，如果开蓝牙时，进入app，就会没有广播，拿不到A2dp，点击设备后闪退。所以增加一个判断，如果进app时，蓝牙开，则直接拿A2dp
//          5、getA2dp()为什么不能在recycler的点击事件里调用？调用无效，拿不到A2dp。而在广播监听里，MainActivity里调用都正常。为什么？

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

        final Button button_switch=(Button)findViewById(R.id.button_switch);
        button_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapaer.isEnabled()){
                    mBluetoothAdapaer.disable();
//                    button_switch.setBackgroundColor(Color.parseColor("#f6aa3e"));
                }else{
                    mBluetoothAdapaer.enable();
//                    button_switch.setBackgroundColor(Color.BLUE);
//                    设置开关蓝牙，按钮变色的两个问题：1、圆角没有了 2、关闭蓝牙很快，图标消失，按钮变色，
//                      但打开蓝牙很慢，按钮变色后1s，系统栏的蓝牙图标才出现
                }

            }
        });

        Button btn_setting = (Button) findViewById(R.id.button_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*          设置按钮暂时不用了。思考是否改做切换 连接A2DP和HFP的开关
            SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
            int flag=pref.getInt("flag",0);
            Log.d(TAG, "onClick:1 "+String.valueOf(flag));
            editor.putInt("flag",1-flag);
            editor.apply();
            Log.d(TAG, "onClick:2 "+String.valueOf(pref.getInt("flag",0)));
            if(flag==0){
                adapter.setmOnItemLongClickListener(new BtDeviceAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "You long clicked the position " + String.valueOf(position), Toast.LENGTH_SHORT).show();
                    }
                });
             recyclerView.setAdapter(adapter);
            }else{
                adapter.setOffItemLongClickListener();
//                setOffItemClickListener()神来之笔，对应setOn，
//                怪异：setOff应该写LongClick的，但写Click就能实现，点“设置”后，切换长按的两种方式（拖动或进页面），改成LongClick反而不行
//        2017.3.12 18:47终于搞定长按的2种方式，setOffItemLongClick()精彩，但开始错写成setOffItemClick()，居然取消了单击，但可以切换长按
//        的两种方式，改写LongClick反而没反应，只能进一种拖动方式。再仔细检查，原来是BtDeviceAdapter里的flag捣乱，去掉flag条件后，搞定。
                recyclerView.setAdapter(adapter);
            }*/
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btStateReceiver);
    }

    class BtStateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context,"BtState is changed",Toast.LENGTH_SHORT).show();
            switch (intent.getAction()){
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
                    switch (blueState){

//                          while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}
//
//                         getBluetoothA2dp();
////                        这句getBlueToothA2dp()写在setOnItemClickListener里面，运行到“getBluetoothA2dp()开始执行……” 就程序闪退
////                        无法Log到“onServiceConnected:”。移到这里就正常。连接小米蓝牙音箱成功。why？
//
//                          getBtDevices();

//                        case BluetoothAdapter.STATE_CONNECTED:
//                            Toast.makeText(context,"BluetoothAdapter.STATE_CONNECTED",Toast.LENGTH_SHORT).show();
//                            break;
//                        测试了STATE_CONNECTED和STATE_ON，只弹出了STATA_ON，不知道CONNECTED什么用途？
                        case BluetoothAdapter.STATE_ON:
                            Toast.makeText(context,"BluetoothAdapter.STATE_ON",Toast.LENGTH_SHORT).show();
                            getBluetoothA2dp();
                            getBluetoothHeadset();
                            getBtDevices();

//                            增加下面这两句，是否能第一次进入app后，只要打开蓝牙，就能立即显示设备列表？（2017.3.22小方手机安装后，没有立即显示
//                            device的recyclerview，第二次进入app后才显示
                            initBtDevices();
                            adapter.notifyDataSetChanged();

                            break;
//                        case BluetoothAdapter.STATE_DISCONNECTING:
//                            Toast.makeText(context,"BluetoothAdapter.STATE_DISCONNECTED",Toast.LENGTH_SHORT).show();
//                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Toast.makeText(context,"BluetoothAdapter.STATE_OFF",Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                initBtDevices();
                adapter.notifyDataSetChanged();
//                写这段的逻辑：在BtDeviceAdapter里长按设备图标，会启动设备界面（在Main里调用BtDeviceAdapter时传了MainActivity，原来传的是
//                context，可以启动Activity，但不能startActivityForResult(),这个必须用Activity才能调。所以传入MainActivity mActivity更好。
//                调用方是BtDeviceAdapter,但要在MainActivity这里写接受返回的onActivityResult()，要写在onCreate()方法外，为此把原来在onCreate()里面
//                的adapter（设备recyclerview的适配器）放到类的最前面进行声明，否则这里在onCreate()外，没法用这个变量。
//                然后在这里，initBtDevice()，重新把数据库里的新数据，写给deviceList，发现界面上新输入的内容已经能体现，但deviceList重复了两次设备
//                列表，在initBtDevice()的开头，加上deviceList.clear()，使调用方法前，先清一遍列表，成功实现了修改完设备数据，返回就能再Main界面看见
//                最新数据了。
//                开始是因为没用singleTask,且Device界面不是finish()返回，而是用Intent再调Main界面，新开Main导致修改的数据能立刻体现在新Main界面里。如下：
//                如果launchMode不是singleTask，则每次进入DeviceActivity改完设备细节后，返回MainActivity，会自动改名，不需重启app，
//                但会开多次MainActivity。设置了singleTask，只开一次窗口，但改完返回后，不自动改刚修改的设备信息。
//                又发现：这里不用设置singleTask，只要长按设备列表打开设备修改页面后，用finish()结束，而不是用Intent再回开Main窗口，就不会新打开Main
        }
    }

    private void getBtDevices(){
//        DataSupport.deleteAll(Devices.class);
//        修改了蓝牙设备的图标获得方式，原来是直接取R.id的lyej.png的图标，现在改成从数据库里取。所以先删除掉Devices表里所有数据，重新取
//        改完所有数据库列，和activity里的代码，发现运行后没有蓝牙图标那行recyclerview了，想起来数据库版本要加1，加后，正常显示了
        Devices updateDevices=new Devices();
        updateDevices.setToDefault("exist");
//        updateDevices.setToDefault("a2dp");
        updateDevices.updateAll();

        Resources res=getResources();
        Bitmap bmp= BitmapFactory.decodeResource(res,R.drawable.lyej_80);
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

//                2017.3.22-去掉下面的hasService的判断，改用设备详细新表页面里，用户自己勾选是否有单独的a2dp媒体或headset电话。
//                下面这个hasService(AUDIO)能找到除了小米人体秤以外的设备，是正确的。但第一次找到后不显示，应该notify一下。
//                if(device.getBluetoothClass().hasService(AUDIO)){
//                    devices.setA2dp("mt");
//                    Log.d(TAG, "getBtDevices: audio");
//                }
//                这个只能找到JAC，找不到lg耳机等可以接电话的设备。
//                if(device.getBluetoothClass().hasService(TELEPHONY)) {
//                    devices.setHeadset("电话");
//                }

                if (catchDevice.size()==0){
                    devices.setExist(1);
                    devices.setLabel(device.getName());
                    devices.setSys_label(device.getName());
                    devices.setMac(device.getAddress());
                    devices.setOrder1(i);
                    devices.setDev_img(img);
                    devices.save();
                }else{
//                    devices.setA2dp("媒体");
//                    Devices updateDevice=new Devices();
                    devices.setExist(1);
//                    updateDevice.setOrder1(i);
//                    上面这句是因为在编写调试这段代码时，
//                    数据库里已经有了7个设备，每次用getBondedDevices()找出来的，都在数据库里，所以无法更新order1，都是i
//                    的初值1，为了先生成1-7的次序，加上这句。如果是新装app，则第一次执行时，就会得到1-7的顺序，只有在人为修改后，才会
//                    变更次序，并保留在数据库里。
                    devices.updateAll("mac=?",device.getAddress());
                }
                i=i+1;
//                这句居然都能出错，int i=0;赋值开始放在循环内，导致每次结尾+1，到循环开始时又变回1，as居然能发现这个问题，i=i+1的第一个i
//                是灰色的，提示这个i never used
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
                    devices.getA2dp(),devices.getHeadset(),devices.getId());
            deviceList.add(btDevice);

//            这里deviceList（recycler的数据表）和这个循环里用到的devicesList（从数据库里读出的设备表）应该怎么命名，
//            才能更清晰易写易读？
        }

/*      这段是原来初始化蓝牙设备recyclerview的语句，直接从getBondedDevices()里面读取，现在更改为先读到
        数据库里getBtDevices()，再从数据库里初始化initBtDevices()
        Set<BluetoothDevice> pairedDevices=mBluetoothAdapaer.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                BtDevice btDevice=new BtDevice(device.getName(),device.getAddress(),R.drawable.lyej_80);
                deviceList.add(btDevice);
            }
        }*/
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
//        Boolean isConneted=false;
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
//        Boolean isConneted=false;
//        if(mBluetoothDevice.getBluetoothClass().getMajorDeviceClass()==1024){
            try {
                connect_method = mBluetoothHeadset.getClass().getMethod("connect", BluetoothDevice.class);
                connect_method.setAccessible(true);
                connect_method.invoke(mBluetoothHeadset,mBluetoothDevice);
            }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
//        }
        return;
    }

    private void disconnectA2dp() {
        Method disconnect_method = null;
//        while(mBluetoothA2dp.getConnectionState(mBluetoothDevice)!=BluetoothProfile.STATE_DISCONNECTED) {
        try {
            disconnect_method = mBluetoothA2dp.getClass().getMethod("disconnect", BluetoothDevice.class);
            disconnect_method.setAccessible(true);
            disconnect_method.invoke(mBluetoothA2dp, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
//            }
        }
    }

    private void disconnectHeadset() {
        Method disconnect_method = null;
//        while(mBluetoothA2dp.getConnectionState(mBluetoothDevice)!=BluetoothProfile.STATE_DISCONNECTED) {
        try {
            disconnect_method = mBluetoothHeadset.getClass().getMethod("disconnect", BluetoothDevice.class);
            disconnect_method.setAccessible(true);
            disconnect_method.invoke(mBluetoothHeadset, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
//            }
        }
    }

//        Method m = mBluetoothHeadset.getClass().getDeclaredMethod("disconnect",BluetoothDevice.class);
//        m.setAccessible(true);
//        m.invoke(mBluetoothHeadset, device);


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
                updateApp.updateAll("package_name=?",appInfo.activityInfo.packageName);
            }
        }

        /*List<PackageInfo> packageInfos=pm.getInstalledPackages(0);
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
        }*/
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
                appInfo.setId(app.getId());
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
