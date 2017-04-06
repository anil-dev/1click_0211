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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
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
    private CheckBox chk_a2dp;
    private CheckBox chk_headset;
    private String TAG="anil1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail2);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("蓝牙设备细节修改页面");

        Intent intent=getIntent();
        final int position=intent.getIntExtra("position",0);
        final int dbId=intent.getIntExtra("dbId",0);

        Button shoot=(Button)findViewById(R.id.btn_shoot);
        Button select=(Button)findViewById(R.id.btn_select);
        Button origin=(Button)findViewById(R.id.btn_origin);
        FloatingActionButton confirm=(FloatingActionButton) findViewById(R.id.btn_ok);
        Button originName=(Button)findViewById(R.id.btn_origin_name);
        final CheckBox chk_a2dp=(CheckBox)findViewById(R.id.chk_a2dp);
        final CheckBox chk_headset=(CheckBox)findViewById(R.id.chk_headset);

        final Devices devices= DataSupport.find(Devices.class,dbId);
        String dbName=devices.getLabel();
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
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap=((BitmapDrawable)icon.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                img=baos.toByteArray();
                updateDevice.setDev_img(img);

                if(chk_a2dp.isChecked()){
                    updateDevice.setA2dp("音频");
                }else{
                    updateDevice.setToDefault("a2dp");
                }
                if(chk_headset.isChecked()){
                    updateDevice.setHeadset("|电话");
                }else{
                    updateDevice.setToDefault("headset");
                }

                updateDevice.update(dbId);

                Intent intent=new Intent();
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
                icon.setImageResource(R.drawable.bluetooth);
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
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap bitmap2=ThumbnailUtils.extractThumbnail(bitmap, 320, 320);
                        this.icon.setImageBitmap(bitmap2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case SELECT:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                    crop(imageUri);
                }
                break;

            case PHOTO_REQUEST_CUT:
                if(data!=null){
                    Bitmap bitmap=data.getParcelableExtra("data");
                    this.icon.setImageBitmap(bitmap);
                }

            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        imageUri=data.getData();
        if(DocumentsContract.isDocumentUri(this,imageUri)){
            String docId=DocumentsContract.getDocumentId(imageUri);
            if("com.android.providers.media.documents".equals(imageUri.getAuthority())){
                String id=docId.split(":")[1];
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
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",160);
        intent.putExtra("outputY",160);

        intent.putExtra("outputFormat","JPEG"); //图片格式
        intent.putExtra("noFaceDetection",true); //取消人脸识别
        intent.putExtra("return-data",true);
        Log.d(TAG, "crop: "+String.valueOf(uri));
        startActivityForResult(intent,PHOTO_REQUEST_CUT);
    }

}
