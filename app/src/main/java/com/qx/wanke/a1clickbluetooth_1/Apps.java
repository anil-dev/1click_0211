package com.qx.wanke.a1clickbluetooth_1;

import org.litepal.crud.DataSupport;

/**
 * Created by cw on 2017/2/14.
 */

public class Apps extends DataSupport{
    private int id;
    private String label;
    private String package_name;
//    开始写packagename，提示有问题typo。改加上下划线
//    private String intent;
    private int order1;
    private int exist;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }
        public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

//    public String getIntent() {
//        return intent;
//    }

//    public void setIntent(String intent) {
//        this.intent = intent;
//    }

    public int getOrder1() {
        return order1;
    }

    public void setOrder1(int order1) {
        this.order1 = order1;
    }
}
