package com.qx.wanke.a1clickbluetooth_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DeviceActivity extends AppCompatActivity {

    public static final int SHOOT=1;
    public static final int SELECT=2;
    public static final int PHOTO_REQUEST_CUT=3;
    private ImageView icon;
    private Uri imageUri;
    private byte[] img;
//    这里用byte[]，不要用Byte[]，否则下面的img=baos.toByteArray();会报类型不匹配
    private CheckBox chk_a2dp;
    private CheckBox chk_headset;
    private String TAG="anil1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail2);
        Intent intent=getIntent();
        final int position=intent.getIntExtra("position",0);
//        为什么getIntExtra一定要有第二个参数做默认值？
        final int dbId=intent.getIntExtra("dbId",0);

        Button shoot=(Button)findViewById(R.id.btn_shoot);
        Button select=(Button)findViewById(R.id.btn_select);
        Button origin=(Button)findViewById(R.id.btn_origin);
        Button confirm=(Button)findViewById(R.id.btn_ok);
        Button originName=(Button)findViewById(R.id.btn_origin_name);
        final CheckBox chk_a2dp=(CheckBox)findViewById(R.id.chk_a2dp);
        final CheckBox chk_headset=(CheckBox)findViewById(R.id.chk_headset);

//        final TextView dev_name=(TextView)findViewById(R.id.dev_name);
        final Devices devices= DataSupport.find(Devices.class,dbId);
        String dbName=devices.getLabel();
//        dev_name.setText(dbName);
        final TextView newDevName=(TextView)findViewById(R.id.new_dev_name);
        newDevName.setTextSize(10);
        newDevName.setHint(dbName+"(可点击修改)");

        icon=(ImageView)findViewById(R.id.device_icon);
        icon.setImageBitmap(BitmapFactory.decodeByteArray(devices.getDev_img(),0,devices.getDev_img().length));

        if(devices.getA2dp()!=null){
            chk_a2dp.setChecked(true);
        }else{
            chk_a2dp.setChecked(false);
        }
        if(devices.getHeadset()!=null){
            chk_headset.setChecked(true);
        }else{
            chk_headset.setChecked(false);
        }

        shoot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //创建File对象，用于存储拍照后的图片
                File outputImage=new File(getExternalCacheDir(), "output_img.jpg");
                File tempImage= new File(getExternalCacheDir(),"temp_img.img");
                try{
                    if(outputImage.exists()){outputImage.delete();}
                    outputImage.createNewFile();
                    if(tempImage.exists()){tempImage.delete();}
                    tempImage.createNewFile();
                }catch (IOException e){e.printStackTrace();}
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(DeviceActivity.this, "com.qx.wanke.a1clickbluetooth_1.fileprovider", outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机
//                Log.d("anil", "onClick: 启动相机");
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,SHOOT);
            }
        });

        select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(DeviceActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Devices updateDevice=new Devices();
                if (!TextUtils.isEmpty(newDevName.getText())){
                    updateDevice.setLabel(String.valueOf(newDevName.getText()));
//                    为什么不能直接用newDevName.getText()?这个不已经是String了么？还要再String一下？
                }
//                updateDevice.update(dbId);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap=((BitmapDrawable)icon.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                img=baos.toByteArray();
                updateDevice.setDev_img(img);

                if(chk_a2dp.isChecked()){
                    updateDevice.setA2dp("音频");
                }else{
//                    updateDevice.setA2dp(null);  运行发现去掉勾选的，没更新数据库，再进页面又被勾选了，原来是设为默认值，要用setToDefault
                    updateDevice.setToDefault("a2dp");
                }
                if(chk_headset.isChecked()){
                    updateDevice.setHeadset("|电话");
                }else{
                    updateDevice.setToDefault("headset");
//                    updateDevice.setHeadset(null);
                }

                updateDevice.update(dbId);

//                Intent intent=new Intent(DeviceActivity.this,MainActivity.class);
//                startActivity(intent);
//                用Intent再回头去开启MainActivity是可以的，但正常写法应该是用finish()结束自己，自然跳回MainActivity。如果Main不是singleTask，还有个
//                用处是，返回后可以自动刷新页面，更新了device的新数据显示出来
                Intent intent=new Intent();
//                intent.putExtra("position",String.valueOf(position));
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        originName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDevName.setText(devices.getSys_label());
            }
        });

        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icon.setImageResource(R.drawable.lyej_80);
            }
        });
    }

    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,SELECT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this, "用户拒绝了读写sd卡的权限，因此无法打开相册。", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("anil", "onActivityResult: all");
        switch (requestCode) {
            case SHOOT:
//                Log.d("anil", "onActivityResult: shoot");
                if (resultCode == RESULT_OK) {
//                    Log.d(TAG, "onActivityResult: 照片拍完，即将进入crop");
//                    用这句log发现拍照正常，那应该是crop里面出错了。是imageUri的问题吗？
//                    crop(imageUri);
//                    总是发现不了小米升级8.2后一进入crop就闪退的原因，干脆尝试拍照后不crop，直接显示到icon里，发现可能是因为拍照尺寸太大，
//                    有一次回Main界面正常显示了，有3次都是点确定后，卡死，重启app进入Main后，只剩两个蓝牙设备，拍照的是第3个，从3到后面都
//                    不显示设备了，重新打开蓝牙，进入app，显示所有的设备了，刚拍的照片也不存在了。

//                    暂时先用下面这段用着，再想android7 小米8.2的crop问题
//                    从Android 2.2开始系统新增了一个缩略图ThumbnailUtils类，位于framework包下的android.media.ThumbnailUtils位置，可以帮助
//                      我们从mediaprovider中获取系统中的视频或图片文件的缩略图，该类提供了三种静态方法可以直接调用获取。
//                    1、extractThumbnail (source, width, height)：
/**
 * 创建一个指定大小的缩略图
 * @param source 源文件(Bitmap类型)
 * @param width  压缩成的宽度
 * @param height 压缩成的高度
 */
//                    ThumbnailUtils.extractThumbnail(source, width, height);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap bitmap2=ThumbnailUtils.extractThumbnail(bitmap, 160, 160);
                        this.icon.setImageBitmap(bitmap2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case SELECT:
                if (resultCode == RESULT_OK) {
//                    判断手机系统版本
                    if (Build.VERSION.SDK_INT >= 19) {
//                        4.4及以上系统用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
//                        4.4以下系统用这个方法
                        handleImageBeforeKitKat(data);
                    }
                    crop(imageUri);
                }
                break;

            case PHOTO_REQUEST_CUT:
                if(data!=null){
                    Bitmap bitmap=data.getParcelableExtra("data");
                    this.icon.setImageBitmap(bitmap);
//                    为什么要加this?
                }

            default:
                break;
        }
    }

    @TargetApi(19)
//    android4.4开始，选取相册中的图片不再返回图片真实的Uri,而是一个封装过的Uri，需要解析。
//    如果Uri是document类型，则取出document id进行处理，如果不是，则普通处理。
//    如果Uri的authority是media格式的话，document id还需要再次解析，通过字符串分割的方式取出后半部分才能得到真正的数字id，取出的id
//    用于构建新的Uri和条件语句。（然后把这些值作为参数传入到getImagePath()方法中，可获取图片的真实路径。）
    private void handleImageOnKitKat(Intent data){
        imageUri=data.getData();
        if(DocumentsContract.isDocumentUri(this,imageUri)){
            //如果是document类型的Uri，通过document id处理
            String docId=DocumentsContract.getDocumentId(imageUri);
            if("com.android.providers.media.documents".equals(imageUri.getAuthority())){
                String id=docId.split(":")[1];//解析出数字格式的id
                String selection=MediaStore.Images.Media._ID+"="+id;
            }else if("com.android.prviders.downloads.documents".equals(imageUri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
            }else if("content".equalsIgnoreCase(imageUri.getScheme())){
            }else if("file".equalsIgnoreCase(imageUri.getScheme())){
            }
        }
    }

    private void handleImageBeforeKitKat(Intent data){
        imageUri=data.getData();
    }

    private void crop(Uri uri){
//        裁剪图片意图
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
//        裁剪框比例1:1
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
//        裁剪后输出的图片尺寸
        intent.putExtra("outputX",160);
        intent.putExtra("outputY",160);

        intent.putExtra("outputFormat","JPEG"); //图片格式
        intent.putExtra("noFaceDetection",true); //取消人脸识别
        intent.putExtra("return-data",true);
//        这里如果改成false，会导致不论拍照、还是相册选取，都是直接退回调用页面，不进入crop阶段。但拍照确定后不闪退了。
        Log.d(TAG, "crop: "+String.valueOf(uri));
        startActivityForResult(intent,PHOTO_REQUEST_CUT);
    }

}
