package com.qx.wanke.a1clickbluetooth_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class DeviceActivity extends AppCompatActivity {

    public static final int SHOOT=1;
    public static final int SELECT=2;
    public static final int PHOTO_REQUEST_CUT=3;
    private ImageView icon;
    private Uri imageUri;
    private byte[] img;
//    这里用byte[]，不要用Byte[]，否则下面的img=baos.toByteArray();会报类型不匹配

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail2);
        Intent intent=getIntent();
        int position=intent.getIntExtra("position",0);
//        为什么getIntExtra一定要有第二个参数做默认值？
        final int dbId=intent.getIntExtra("dbId",0);

        Button shoot=(Button)findViewById(R.id.btn_shoot);
        Button select=(Button)findViewById(R.id.btn_select);
        Button origin=(Button)findViewById(R.id.btn_origin);
        Button confirm=(Button)findViewById(R.id.btn_ok);
        Button originName=(Button)findViewById(R.id.btn_origin_name);

        final TextView dev_name=(TextView)findViewById(R.id.dev_name);
        final Devices devices= DataSupport.find(Devices.class,dbId);
        String dbName=devices.getLabel();
        dev_name.setText(dbName);
        final TextView newDevName=(TextView)findViewById(R.id.new_dev_name);

        icon=(ImageView)findViewById(R.id.device_icon);
        icon.setImageBitmap(BitmapFactory.decodeByteArray(devices.getDev_img(),0,devices.getDev_img().length));


        shoot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //创建File对象，用于存储拍照后的图片
                File outputImage=new File(getExternalCacheDir(), "output_img.jpg");
                try{
                    if(outputImage.exists()){outputImage.delete();}
                    outputImage.createNewFile();
                }catch (IOException e){e.printStackTrace();}
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(DeviceActivity.this, "com.qx.wanke.a1clickbluetooth_1.fileprovider", outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机
                Log.d("anil", "onClick: 启动相机");
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
                if (!TextUtils.isEmpty(newDevName.getText()) && newDevName.getText()!=dev_name){
                    updateDevice.setLabel(String.valueOf(newDevName.getText()));
//                    为什么不能直接用newDevName.getText()?这个不已经是String了么？还要再String一下？
                }else{
                    updateDevice.setLabel(String.valueOf(dev_name.getText()));
                }
//                updateDevice.update(dbId);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap=((BitmapDrawable)icon.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                img=baos.toByteArray();
                updateDevice.setDev_img(img);
                updateDevice.update(dbId);

                Intent intent=new Intent(DeviceActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        originName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dev_name.setText(devices.getSys_label());
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
                Log.d("anil", "onActivityResult: shoot");
                if (resultCode == RESULT_OK) {
                    crop(imageUri);
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
        startActivityForResult(intent,PHOTO_REQUEST_CUT);
    }

}
