package com.qx.wanke.a1clickbluetooth_1;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/3/10 0010.
 */

public class Devices extends DataSupport{
    private int id;
//    id本来是表的缺省栏，这里显式说明，是为了增加getter、setter方法，以便于将设备在数据库中的id传给activity，这样，读数据库时
//    不必用mac来匹配，findAll找出来列表List，再去取列表第一个，可以直接取id，得到唯一数据
    private String sys_label;
    private String label;
    private int exist;
    private int order1;
    private String mac;
    private byte[] dev_img;

    public byte[] getDev_img() {
        return dev_img;
    }

    public void setDev_img(byte[] dev_img) {
        this.dev_img = dev_img;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
}
