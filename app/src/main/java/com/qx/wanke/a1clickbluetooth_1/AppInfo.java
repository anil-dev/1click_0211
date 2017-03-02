package com.qx.wanke.a1clickbluetooth_1;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by cw on 2017/2/18.
 */

public class AppInfo {
    private String pkgName;
    private String appLable;
    private Drawable appIcon;
    private Intent intent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    public String getAppLable() {
        return appLable;
    }

    public void setAppLable(String appLable) {
        this.appLable = appLable;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
