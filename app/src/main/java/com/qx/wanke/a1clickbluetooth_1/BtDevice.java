package com.qx.wanke.a1clickbluetooth_1;

import android.graphics.Bitmap;

/**
 * Created by cw on 2017/1/24.
 */

public class BtDevice {
    private String btName;
    private String btMacAdress;
    private Bitmap btImage;
    private int btId;

    public BtDevice(String btName,String btMacAdress,Bitmap btImage,int btId){
        this.btName=btName;
        this.btMacAdress=btMacAdress;
        this.btImage=btImage;
        this.btId=btId;
    }

    public String getBtName(){return btName;}
    public String getBtMacAdress(){return btMacAdress;}
    public Bitmap getBtImage(){return btImage;}
    public int getBtId(){return btId;}
}
