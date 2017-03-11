package com.qx.wanke.a1clickbluetooth_1;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/3/10 0010.
 */

public class Devices extends DataSupport{
    private String sys_label;
    private String label;
    private int exist;
    private int order1;
    private String mac;
    private Byte icon;

    public String getSys_label() {
        return sys_label;
    }

    public void setSys_label(String sys_label) {
        this.sys_label = sys_label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public int getOrder1() {
        return order1;
    }

    public void setOrder1(int order1) {
        this.order1 = order1;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Byte getIcon() {
        return icon;
    }

    public void setIcon(Byte icon) {
        this.icon = icon;
    }
}
