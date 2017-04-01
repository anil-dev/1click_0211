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
//    private int connected=-1;
//    connected表示当前连接的设备在列表中的序号（如果连接后拖动更改了排序呢？也需要在拖动的方法里同时更改connected值）。
//    需要先赋初值-1，表示没有设备被连接。另外，还需要在打开蓝牙的时候，自动连接的设备监测方法里，修改connected值。


    private IntentFilter intentFilter;
    private BtStateReceiver btStateReceiver;
//    2017.3.18，《第二行代码》P171，广播接收器。继承BroadcastReceiver，监听intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//    不再使用while (mBluetoothAdapaer.getState()!=BluetoothAdapter.STATE_ON){}，1、使app启动白屏时间缩短0.5s 2、在app里需要随时监听蓝牙变化，以
//    改变按钮颜色、（是否 蓝牙关闭时，点击设备，要通过这个来打开蓝牙后连接设备？）

    private IntentFilter a2dpIntentFilter;
    private A2dpReceiver a2dpReceiver;

    private IntentFilter headsetIntentFilter;
    private HeadsetReceiver headsetReceiver;

//    SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //防止要输入app名称时，软键盘挡住EditText

/*      郭霖p529说这段代码能让背景图和系统状态栏融合（他是frameLayout嵌入一个imageview填满屏幕的），我的Linear嵌套第一个Linear，不能完全融合，
            上方有白条
       if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/

        setContentView(R.layout.activity_main);

//        下面这两句是郭霖p111,隐藏标题栏的办法。也可以用p409.设定res/values/styles.xml里的AppTheme主题的方式，去掉所有页面的标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){actionBar.hide();}

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

/*      下面这段用来测量cardview的大小，以供制作图片时参考：为什么宽是1208？不是满屏吗？小米5 主屏尺寸 5.15英寸 主屏分辨率 1920x1080
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        CardView mCardView = (CardView) findViewById(R.id.card_view);
        mCardView.measure(w, h);
        int height = mCardView.getMeasuredHeight();
        int width = mCardView.getMeasuredWidth();
        Log.d(TAG, "onCreate: "+String.valueOf(width)+"*"+String.valueOf(height));
//      结果：  03-30 13:31:15.728 32379-32379/? D/anil: onCreate: 1208*680*/

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btStateReceiver=new BtStateReceiver();
        registerReceiver(btStateReceiver, intentFilter);

//        2017.3.22 22：58，下面注册broadcast the change in connection state of the A2DP profile，试验一下，感觉不错，连接、断开设备，打开蓝牙
//        自动连接设备，走远断开设备、走近音箱重新连接设备，都会弹出Toast告知A2DP的连接发生了变化。只有关闭蓝牙时没有提示，没有关系，关闭蓝牙时
//        我们监听STATE_OFF，直接把所有设备的颜色改成初始色。
        a2dpIntentFilter = new IntentFilter();
        a2dpIntentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        a2dpReceiver=new A2dpReceiver();
        registerReceiver(a2dpReceiver, a2dpIntentFilter);

        headsetIntentFilter = new IntentFilter();
        headsetIntentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        headsetReceiver=new HeadsetReceiver();
        registerReceiver(headsetReceiver, headsetIntentFilter);

        initBtDevices();
//        adapter.notifyDataSetChanged(); 这里应该不对，才把数据准备好，还没有显示recycler,怎么能notify呢？

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
//            Toast.makeText(MainActivity.this,"You clicked the position "+String.valueOf(position),Toast.LENGTH_SHORT).show();
//              这里用this，报错。parent.getContext()等的区别？
//                郭霖P131提示，是否可以用v.getContext()? 有空试试。

            if(!mBluetoothAdapaer.isEnabled()) {
                mBluetoothAdapaer.enable();
//                while (mBluetoothAdapaer.getState() != BluetoothAdapter.STATE_ON) {}
                Toast.makeText(MainActivity.this,"正在为你打开蓝牙，请稍候1-2秒…",Toast.LENGTH_SHORT).show();
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
//                Log.d("anil", "onItemClick: "+String.valueOf(mBluetoothA2dp==null));
//                进入app后隔几秒后，不论开关蓝牙，再点击设备，都能正常获得A2dp。但只要是关闭蓝牙时进入app，再立刻点击设备，就无法获得A2dp，导致
//                下面一句switch（)闪退。为什么？
//                见下面+100多行处，判断蓝牙是否打开的地方有5条解释。主因是：进入app后，立刻点击设备，会闪退。
//                  分析执行流程，应该是点击设备时，程序打开蓝牙，while判断蓝牙已开，就往下执行用A2dp拿设备
//                  的连接状态，而此时，蓝牙打开的广播监听到了，但未执行getA2dp
//                while(mBluetoothA2dp==null){};

                if(mBluetoothA2dp.getConnectionState(mBluetoothDevice)== STATE_CONNECTED||
                        mBluetoothHeadset.getConnectionState(mBluetoothDevice)== STATE_CONNECTED){
                    Toast.makeText(MainActivity.this,"断开"+deviceList.get(position).getBtName(),Toast.LENGTH_SHORT).show();
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
//        Log.d(TAG, "onCreate: initAppList()执行。");

        recyclerView2 = (RecyclerView) findViewById(R.id.recycler_view2);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView2.setLayoutManager(layoutManager2);
        adapter2 = new AppInfoAdapter(appInfoList);

        adapter2.setOnItemClickListener(new AppInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//            Toast.makeText(MainActivity.this,"you click the mAppList "+String.valueOf(position),Toast.LENGTH_SHORT).show();
            Intent intent=appInfoList.get(position).getIntent();
//            下面这2种方法，是用intent来查找该app是否存在本机里
            List<ResolveInfo> list =  MainActivity.this.getPackageManager().queryIntentActivities(intent, 0);
            if(list.size()>0) {
//            下面这个if里的判断：通过Intent的resolveActivity方法，并想该方法传入包管理器可以对包管理器进行查询以确定是否有Activity能够启动该Intent
//                结果试了一下不行，把今日头条极速版删了，再直接进一键蓝牙点启动app列表里的今日头条极速版，闪退了。为什么？
//            if(intent.resolveActivity(getPackageManager()) != null){
                Toast.makeText(MainActivity.this,"为你启动："+appInfoList.get(position).getAppLable(),Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }else{
                Toast.makeText(MainActivity.this,"你是不是把"+appInfoList.get(position).getAppLable()+"给咔嚓了呀？如果是的话，" +
                        "就先别点它了。你下次启动我的时候我会帮你把它从列表里删掉的:-)",Toast.LENGTH_SHORT).show();
            }
            }
        });

        ItemTouchHelper.Callback callback2=new MyItemTouchHelperCallback(adapter2);
//        这里的adapter和ITHAdapter各自是什么情况？
        ItemTouchHelper touchHelper2=new ItemTouchHelper(callback2);
        touchHelper2.attachToRecyclerView(recyclerView2);

        recyclerView2.setAdapter(adapter2);

//        new updateDevListTask().execute();

//      这里有发现一点：onResume里，GetAppsTask和UpdateDevListTask()都要执行，是不是在onCreate()里就不用啦？否则一启动就要搞2遍
//        无语，这里还搞出来一个幺蛾子，原来继承异步类的时候，类名写成updateDevListTask，准备首字母改成大写Update……，改好编译，居然报错：
//        Error:Error converting bytecode to dex: Cause: java.lang.RuntimeException: Exception parsing classes
//        想不起来刚才写了哪些东西，很紧张，一大堆东西又要恢复到开始状态重写重试？网上搜错误信息，有的说jar包重复啥的，也没用。还是用git
//        （git实在是太好了），看看刚才都写了些什么，发现前面写的大部分都运行通过了，最后改的就是类名，一试果然是不能改。
//        为什么？  另外：教训：写一点、就试试是否正常，该在git里保存的、提交的，就提交和push
//        new GetAppsTask().execute();

//        if(DataSupport.count(Apps.class)==0) {
//            getApps();
//        }

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

//        new updateDevListTask().execute();

        button_send = (Button)findViewById(R.id.button_send);
//        button_send.setEnabled(false); onCreate()里貌似不用设置，反正下面还要进入onResume()，设置颜色和enabled
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
                    edittext.setText(null);
                    appList = DataSupport
//                            .select("label","order")
//                        .where("label=?", appName)
//                        .where("exist=? and label like ?","1","%"+appName+"%")
                            .where("label like ?", "%" + appName + "%")
//                        改上一句的精确查询为现在的模糊查询
//                        .limit(1)
                            .find(Apps.class);
//                    Log.d(TAG, "onClick:模糊查找app ");

                    if (appList.size() == 0) {
                        InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                        Toast.makeText(MainActivity.this, "没找到含有“"+appName+"”的app，请重新输入", Toast.LENGTH_SHORT).show();
                    } else {
                        InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//                        int maxApp = DataSupport.where("order>?", "0").count(Apps.class);
                        int maxApp = DataSupport.max(Apps.class, "order1", int.class);
                        //order列求max总是闪退，改为exist列，不闪退。why？想了很久，发现order是litepal的保留字，不能做列名
//                        Log.d(TAG, "onClick: " + String.valueOf(maxApp));
                        for (Apps app : appList) {
                            maxApp++;

                            Apps updateApp=new Apps();
                            updateApp.setOrder1(maxApp);
                            updateApp.updateAll("label=?",app.getLabel());

//                            app.setOrder(maxApp);
//                            app.save();

//                            Log.d(TAG, "app name is " + app.getLabel()+" order1 is "+String.valueOf(app.getOrder1())+
//                                    " exist is "+String.valueOf(app.getExist()));
                        }
                        initAppList();
//                        没有下面这句notify的时候，app也能加进列表，但如果是列表已有的app(如喜马拉雅），就会在列表里原位置一个、末尾新加一个，
//                        退出app重进才会正常，加了notify，一切清爽了。——2017.3.23.17:37
                        adapter2.notifyDataSetChanged();
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

        button_switch=(Button)findViewById(R.id.button_switch);
        button_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapaer.isEnabled()){
//                    Toast.makeText(MainActivity.this,"关闭蓝牙",Toast.LENGTH_SHORT).show();
                    mBluetoothAdapaer.disable();
//                    button_switch.setBackgroundColor(Color.parseColor("#f6aa3e"));
                    Devices devices=new Devices();
                    devices.setToDefault("a2dp_conn");
                    devices.setToDefault("headset_conn");
                    devices.updateAll();
                    initBtDevices();
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(MainActivity.this,"尝试打开蓝牙",Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(MainActivity.this, LessonActivity.class);
                startActivity(intent);
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

        button_send.setTextColor(Color.BLACK);
        button_send.setEnabled(false);
        edittext.setHint("正在为你索引手机里所有app，请稍后提交…");
        new GetAppsTask().execute();
//        这段如果不开异步线程，app启动和back后重新进入的速度进很慢，一直等到下面这段刷新recycler2的指令执行完才显示界面，只好重新用异步执行
//        刷新，速度又变快了。为什么？思考：这个异步会不会一直执行到onResume()里刷新recycler蓝牙的运行过程里？两者是否会冲突？如果不会，为什么
//        前面在onResume()里加上这两个异步线程的时候，会时常闪退报错？说下标越界什么的。
//        getApps();
//        initAppList();
//        adapter2.notifyDataSetChanged();
//        edittext=(EditText)findViewById(R.id.input);
//        edittext.setHint("输入app名称，点提交可加入上方启动app列表…");
//        button_send.setTextColor(Color.WHITE);
//        button_send.setEnabled(true);
    }

/*  折腾一大圈，发现不能加onStop()，否则虽然home键出app关闭，再打开是可以实现在程序不运行期间被删除的app不显示在列表上，但进入Device页面后，点确定
，app立刻就闪退了，屏幕无任何错误提示，看as的log里是关于a2dp泄露的错误，头嗡一下，不知何从下手。
其实是因为进Device页面，已经触发了主页面的onStop()，主页面已经finish()，Device页面确定后finish()，程序就结束了。可以考虑在resume页面写关于删除app
recyclerview2重新显示的逻辑。
    @Override
    protected void onStop() {
//    增加onStop()，让app只要进入后台（用手机的back键或home键），就结束。再次进入相当于再次启动app，走onCreate()流程，这样可以重新显示
//    app的recycler，防止用户按home键出来，同时再删除在app列表里的应用，这时再进app，应用还在列表上，点击会闪退。
//    但还有一个隐患：onCreate()里，为了尽早显示界面，initApps()先执行 从数据库读给mAppList，然后就用recyclerview2显示出来，再用异步启动GetApps()，
//    更新Apps的数据库内容，这样如果在程序不运行期间(但在后台，重进时不走onCreate())，有启动列表里的app被删除了，本软件resume时并不知道。于是继续改：
//    在GetApps异步任务里class GetAppsTask extends AsyncTask<Void,Void,Void>{的        protected void onPostExecute(Void aVoid) {，异步任务执行完
//    后，1修改editText里面的内容，提示可以输入app名称，2.增加一个initApps和adapter2.notify的命令，重新获取app列表和通知adapter列表变化了，这样，
//    在本程序停止期间，如果有app列表里的app被删除了，刚进入程序时，先显示该位置是空白（为什么没有图标了？不是在数据库里吗？因为——
//      GetApp里只是拿包名写入数据库，在initApps里才pm.getApplicationInfo(app.getPackage_name(), 0);然后用info拿图标和启动intent），
//      然后当异步任务处理完时，recycler2就能正常显示了。现在只剩一个问题：app打开，界面到底什么时候显示？能否先显示界面，再等recyclerview数据准备好
//    后，再显示recycler？还是要等到recycler全部搞好，才能完整显示app界面？
        super.onStop();
        finish();
    }
*/

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


//        2017.3.26 仔细思考，下面这段异步获取当前本地apps，更新recycler2放在onResume()里面还是不很合适，每次Main回到前台，都要执行一遍，频率太高，
//        并且是异步执行，生命周期不好控制，如果快速按back键回桌面，再快速进入，重复几次，会导致recycler2显示不全，还是放在onCreate()里面，每次开启
//        app的时候运行一次就好，运行中，启动列表里的软件有增删，下次进入的时候会反应出来。同时可以在启动列表里的app的时候，加try……catch来捕获错误，
//        另外，不需要异步，应该也不影响启动速度吧？试一试。
//        同时，把蓝牙和各设备音频、电话状态的更新放在onResume()里面。也不用异步执行了。
//        button_send.setBackgroundColor(Color.parseColor("#f6aa3e"));
//        button_send.setTextColor(Color.BLACK);
//        button_send.setEnabled(false);
//        edittext.setHint("正在为你索引手机里所有app，请稍后提交…");
//        new GetAppsTask().execute();
        /*    为什么加onResume()这段，每次打开app，就不能显示recycler2了？注释掉getApp也不行。注释掉initAppList()就可以正常显示了，但
        希望不显示的本app后台期间被删除的启动app列表里的app还在里面，点击会闪退。
        为什么initAppList()会让列表不显示？又试了一下在不去掉initAppList，在initAppList里面打log，结果跑了一下app，发现用home、用back
        暂时退出app，下方的recycler有时候显示、有时候不显示、有时候显示的不全。晕……看来不是initAppList的问题，还是和onResume的机制有
        关系？奇怪的现象。留下来，以后解决。现在暂时去掉onResume()。或者暂时这样，或者加上BACK键退出app的逻辑
        有一点要注意的是：home退出后，再进入，edittext里面的文字不变，是直接恢复了退出前的现场，而back退出后，在进入，edittext里面是"正在
        索引……"，可以起到一部分找到新加app的作用

        晚上到家一看代码，找到了问题，GetAppsTask()是异步启动的啊，当然会和下面的InitAppList()同时运行，导致每次获取出来的数据不一样。
        其实下面的init和notify在GetAppsTask里都有了，只要这一句就OK了，改！
        2017.3.24 23:19 修改成功。本来是back键回来后，可以很快显示app列表里的"今日头条极速版"在后台期间，被删掉了。现在即使用home将一键蓝牙
        扔到后台，删除今日快报，再点开一键蓝牙，也能显示1s的今日头条，然后很快消失。
@Override
    protected void onResume() {
        super.onResume();
//        new GetAppsTask().execute();
        initAppList();
        adapter2.notifyDataSetChanged();
    }*/
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
                            button_switch.setTextColor(Color.BLUE);
                            Toast.makeText(context,"蓝牙已打开",Toast.LENGTH_SHORT).show();

                            getBluetoothA2dp();
                            getBluetoothHeadset();
                            getBtDevices();

//                            增加下面这两句，是否能第一次进入app后，只要打开蓝牙，就能立即显示设备列表？（2017.3.22小方手机安装后，没有立即显示
//                            device的recyclerview，第二次进入app后才显示
                            setColor();
                            initBtDevices();
                            adapter.notifyDataSetChanged();

//                            下面这句是否移动到initBtDevices()里面更好？每次init后直接通知recyclerview更新？不行，报空指针错。为什么？
//                        java.lang.RuntimeException: Unable to start activity ComponentInfo{com.qx.wanke.a1clickbluetooth_1/com.qx.wanke.
//                          a1clickbluetooth_1.MainActivity}: java.lang.NullPointerException: Attempt to invoke virtual method
//                          'void com.qx.wanke.a1clickbluetooth_1.BtDeviceAdapter.notifyDataSetChanged()' on a null object reference

//                            adapter.notifyDataSetChanged();

                            break;
//                        case BluetoothAdapter.STATE_DISCONNECTING:
//                            Toast.makeText(context,"BluetoothAdapter.STATE_DISCONNECTED",Toast.LENGTH_SHORT).show();
//                            break;
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
//            Toast.makeText(context, "a2dp is changed.", Toast.LENGTH_SHORT).show();
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
//            edittext=(EditText)findViewById(R.id.input);
            edittext.setHint("输入app名称，点提交可加入上方启动app列表…");
            button_send.setTextColor(Color.WHITE);
            button_send.setEnabled(true);
//            加上下面2句，可以使用back进入后台后，有app列表里的程序被卸载，一键蓝牙再启动时，该位置先为空，等索引完后，列表显示就正常了。
//            用home键去后台没有这个效果。被删除的app还在列表里显示，如果点击它会导致闪退。为什么？
            adapter2.notifyDataSetChanged();
        }
    }

    class updateDevListTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
//            这段原来改在onCreate()最前面，功能实现倒是没问题，进app就判断，然后根据蓝牙开关情况和各设备接通情况，
//              getBtDevices写给数据库，再init写给deviceList，然后recycler显示（不用nitify，因为在recyclerq前面），但导致了app的启动时间大大延长到2s左右
//            git果然是神器，对比了3.23 0:46分的0.995版，启动速度很快的，发现差别是，快版是一句init，就直接recycler显示。仔细想了一遍，还是先显示一下
//            recycler，等app界面都显示了，再在后面用AsyncTask异步获取设备当前状态，再更新界面。启动速度又恢复了。爽！这个git太好了，否则头脑写晕了，
//            几乎想不起来写了什么导致启动速度变慢的。

            //        把蓝牙是否打开的判断放到前面，如果打开，就拿a2dp headset，然后拿设备写入数据库，setColor()，
//         再initBtDevice()准备好子项列表，然后进入recyclerview的流程。如果没打开，就数据库的蓝牙连接全部设0，再initBtDevice()
//        解决了两个问题：1、第一次安装后，在已开蓝牙的情况下，进app，能立即显示设备
//        2、在蓝牙已连接音箱的情况下，进app，能立即显示音频连接图标的蓝色
            if (mBluetoothAdapaer.isEnabled()) {
                getBluetoothA2dp();
                getBluetoothHeadset();
//            增加这段，是发现两种极端情况，1第一次安装app，蓝牙已打开后再进入app，不显示设备。如果是进app再打开蓝牙，会在STATE_ON里显示设备。
//            2、如果用系统蓝牙连接了设备，此时才进入app，app不知道设备连接的情况，连接的音箱下面的颜色还是初始色，不变蓝。
                getBtDevices();
                setColor();
//            Log.d(TAG, "onCreate: setColor()执行");
                initBtDevices();
//            adapter.notifyDataSetChanged();

//            增加下面这段else，是为了防止比较极端的情况，即蓝牙连接时（此时连接状态是蓝色，数据库a2dp_conn或headset_conn是1），app被关闭（
//            进后台，或者进程被杀死），然后又手动在系统设置里关闭了蓝牙，这时数据库得不到更新（因为没有走app的监听蓝牙关闭的流程），所以再次
//            进入app后，蓝牙没开，但连接状态有蓝色）
            } else {
                Devices devices = new Devices();
                devices.setToDefault("a2dp_conn");
                devices.setToDefault("headset_conn");
                devices.updateAll();
                initBtDevices();
            }
//        基本解决获取不到A2dp，导致在点击设备时闪退的情况（
//          1、在没开蓝牙，进入app后，立刻点击设备，会闪退。分析执行流程，应该是点击设备时，程序打开蓝牙，while判断蓝牙已开，就往下执行用A2dp拿设备
//        的连接状态，而此时，蓝牙打开的广播监听到了，但未执行getA2dp
//          2、如果在onCreate()里，直接打开蓝牙，并等蓝牙打开后，执行getA2dp，会白屏很久
//          3、去掉上面这段，在recyclerview判断点击事件里，如果蓝牙没开，就打开，并提示等1s再点击，然后return掉这次点击，这时，广播监听应该能拿到A2dp了，
//        这两个流程不是异步吧？什么样的执行顺序？
//          4、但去掉上面这段后，如果开蓝牙时，进入app，就会没有广播，拿不到A2dp，点击设备后闪退。所以增加一个判断，如果进app时，蓝牙开，则直接拿A2dp
//          5、getA2dp()为什么不能在recycler的点击事件里调用？调用无效，拿不到A2dp。而在广播监听里，MainActivity里调用都正常。为什么？
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            initBtDevices();不是更新UI的函数，只是从数据库里读给deviceList，下面这句notify是更新UI的，必须写在onPostExecute()里，否则闪退
            adapter.notifyDataSetChanged();
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
//        下面这句是有时候要更换蓝牙设备的标志，需要全部删掉，让app重新写进数据库而用
//        DataSupport.deleteAll(Devices.class);
        Devices updateDevices=new Devices();
        updateDevices.setToDefault("exist");
//        updateDevices.setToDefault("a2dp");
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
                    devices.getA2dp(),devices.getA2dp_conn(),devices.getHeadset(),devices.getHeadset_conn(),devices.getId());
            deviceList.add(btDevice);
//            adapter.notifyDataSetChanged();

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

//        Method m = mBluetoothHeadset.getClass().getDeclaredMethod("disconnect",BluetoothDevice.class);
//        m.setAccessible(true);
//        m.invoke(mBluetoothHeadset, device);


    private void getApps(){

//        2017.3.24 23:36 发现一个小问题。考虑了几个极端的情况，一键蓝牙在后台时（home或back），有软件增删，于是在onResume里加入两个异步刷新
//        设备和app列表的功能，这样，一键蓝牙回到前台时，立刻能感知设备和app的变化，如果有增加，可以立即输入app名加入列表，如果有删除，在getApps
//        执行完后，会刷新列表，app消失。但如果又去后台，我又把这个app重装上，再回一键蓝牙，发现这个app又重新出现在原来的位置，而我并没有主动
//        去增加，思考这个流程，getApps是拿到本地所有app的名单，然后和数据库比对，有就把exist设为1，没有就把包名和label写入数据库，exist设为1。
//        而不会去管app列表里的会不会有被物理删除的，如果是我主动在列表里下滑掉的，会在AppInfoAdapter里的onDismiss()里updateApp.setToDefault("order1");
//        把order1顺序写成0，这样再进入列表就不会保留原顺序，而物理删除的没有这个过程，所以order1没有变化，一旦被删exist变0，再装上，它立刻就既有exist
//        值，又有order1值，直接就排进了app列表。打标记“为什么”，想想怎么解决，以后再处理。
//        还是解决掉吧，省的删掉的app，order1值都不变化，以后再装上，再加入列表，导致order1值的混乱。
//        加一句：DataSupport.deleteAll(Apps.class,"exist = ?","0"); 等本地app全部写入数据库，exist赋值1后，把所有exist=0的全删掉，数据库与本地app保持
//        一致。

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
//                加下面这句是防止有些app包名没变，但app显示名label改变了，如果不修改label，用户会搜索不到这个app（我的一键蓝牙，原来名字
//                1-Click Bluetooth……，改过后，导致一直没法用一键蓝牙四个字找到。其实这样看来，就不用判断了，直接把检索出来的包名、label和
//                exist再写入数据库即可？用郭霖的saveifnotexist？
                updateApp.setLabel(appInfo.loadLabel(pm).toString());
                updateApp.updateAll("package_name=?",appInfo.activityInfo.packageName);
            }
        }
        DataSupport.deleteAll(Apps.class,"exist = ?","0");

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
//        Log.d(TAG, "initAppList: ");
        for (Apps app : apps) {
            PackageManager pm = getPackageManager();
            AppInfo appInfo=new AppInfo();
//            Log.d(TAG, "initAppList: "+app.getPackage_name());
            try {
                ApplicationInfo info = pm.getApplicationInfo(app.getPackage_name(), 0);
                appInfo.setAppIcon(info.loadIcon(pm));
                appInfo.setAppLable(info.loadLabel(pm).toString()); //为什么要toString? loadLabel不是字符串？
//                Log.d(TAG, "initAppList: "+ info.loadLabel(pm).toString()+"加入列表");

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
