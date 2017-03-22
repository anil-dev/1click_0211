package com.qx.wanke.a1clickbluetooth_1;

import android.graphics.Bitmap;

/**
 * Created by cw on 2017/1/24.
 */

public class BtDevice {
    private String btName;
    private String btMacAdress;
    private Bitmap btImage;
    private String a2dp;
    private int a2dpConn;
    private String headset;
    private int headsetConn;
    private int btId;

    public BtDevice(String btName,String btMacAdress,Bitmap btImage,String a2dp,int a2dpConn,String headset,int headsetConn,int btId){
        this.btName=btName;
        this.btMacAdress=btMacAdress;
        this.btImage=btImage;
        this.a2dp=a2dp;
        this.a2dpConn=a2dpConn;
        this.headset=headset;
        this.headsetConn=headsetConn;
        this.btId=btId;
    }

    public String getA2dp() {
        return a2dp;
    }

    public String getHeadset() {
        return headset;
    }

    public int getA2dpConn() {
        return a2dpConn;
    }

    public int getHeadsetConn() {
        return headsetConn;
    }

    public String getBtName(){return btName;}
    public String getBtMacAdress(){return btMacAdress;}
    public Bitmap getBtImage(){return btImage;}
    public int getBtId(){return btId;}
}
