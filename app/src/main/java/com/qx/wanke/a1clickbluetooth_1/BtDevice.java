package com.qx.wanke.a1clickbluetooth_1;

/**
 * Created by cw on 2017/1/24.
 */

public class BtDevice {
    private String btName;
    private String btMacAdress;
    private int btImageId;

    public BtDevice(String btName,String btMacAdress,int btImageId){
        this.btName=btName;
        this.btMacAdress=btMacAdress;
        this.btImageId=btImageId;
    }

    public String getBtName(){return btName;}
    public String getBtMacAdress(){return btMacAdress;}
    public int getBtImageId(){return btImageId;}
}
