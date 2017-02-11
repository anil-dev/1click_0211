package com.qx.wanke.a1clickbluetooth_1;


import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by cw on 2017/1/25.
 */

public class BtDeviceAdapter extends RecyclerView.Adapter<BtDeviceAdapter.ViewHolder> {

    private String TAG="anil";
    private List<BtDevice> mBtList;

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener=onItemClickListener;
    }

    public void setmOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener=onItemLongClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View devView;
        ImageView devImage;
        TextView devName;
        TextView devMacAdress;

        public ViewHolder(View view){
            super(view);
            devView=view;
            devImage=(ImageView)view.findViewById(R.id.dev_img);
            devName=(TextView)view.findViewById(R.id.dev_name);
            devMacAdress=(TextView)view.findViewById(R.id.dev_mac);
        }
    }

    public BtDeviceAdapter(List<BtDevice> btDeviceList){
        mBtList=btDeviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dev_list_v,parent,false);
        final ViewHolder holder=new ViewHolder(view);

        if (mOnItemClickListener != null) {
            holder.devView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.devView,position);          //这里写view报错，说要声明final，why？
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.devView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(holder.devView,position);
                    return true;
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BtDevice btDevice=mBtList.get(position);
        holder.devImage.setImageResource(btDevice.getBtImageId());
        holder.devName.setText(btDevice.getBtName());
        holder.devMacAdress.setText(btDevice.getBtMacAdress());
    }

    @Override
    public int getItemCount() {
        return mBtList.size();
    }
}
