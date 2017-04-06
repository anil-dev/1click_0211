package com.qx.wanke.a1clickbluetooth_1;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cw on 2017/1/25.
 */

public class BtDeviceAdapter extends RecyclerView.Adapter<BtDeviceAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private String TAG="anil";
    private List<BtDevice> mBtList;
    private MainActivity mActivity;

    public interface OnImageClickListener{
        void onImageClick(View view,int position);
    }

    public interface OnA2dpClickListener{
        void onA2dpClick(int position);
    }
    public interface OnHeadsetClickListener{
        void onHeadsetClick(int positon);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    private OnImageClickListener mOnImageClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private OnA2dpClickListener mOnA2dpClickListener;
    private OnHeadsetClickListener mOnHeadsetClickListener;

    public void setmOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.mOnImageClickListener=onImageClickListener;
    }
    public void setmOnA2dpClickListener(OnA2dpClickListener onA2dpClickListener){
        this.mOnA2dpClickListener=onA2dpClickListener;
    }
    public void setmOnHeadsetClickListener(OnHeadsetClickListener onHeadsetClickListener) {
        this.mOnHeadsetClickListener=onHeadsetClickListener;
    }

    public void setmOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.mOnItemLongClickListener = onItemLongClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View devView;
        ImageView devImage;
        TextView devName;
        TextView devMacAdress;
        TextView devA2dp;
        TextView devHeadset;

        public ViewHolder(View view){
            super(view);
            devView=view;
            devImage=(ImageView)view.findViewById(R.id.dev_img);
            devName=(TextView)view.findViewById(R.id.dev_name);
            devA2dp=(TextView)view.findViewById(R.id.tv_a2dp);
            devHeadset=(TextView)view.findViewById(R.id.tv_headset);
        }
    }


    public BtDeviceAdapter(List<BtDevice> btDeviceList,MainActivity mainActivity){
        mBtList=btDeviceList;
        mActivity=mainActivity;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mBtList,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);

        for(int i=0;i<mBtList.size();i++){
            int newListId=mBtList.get(i).getBtId();
            Devices updateDevice=new Devices();
            updateDevice.setOrder1(i+1);
            updateDevice.update(newListId);
        }
    }

    @Override
    public void onItemDismiss(int position) {

        Intent intent = new Intent(mActivity, DeviceActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("dbId",mBtList.get(position).getBtId());
        mActivity.startActivityForResult(intent,1);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dev_list_v,parent,false);
        final ViewHolder holder=new ViewHolder(view);

        if (mOnImageClickListener != null) {
            holder.devImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnImageClickListener.onImageClick(holder.devView,position);//这里写view报错，说要声明final，why？

                }
            });
        }
        if(mOnA2dpClickListener!=null){
            holder.devA2dp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnA2dpClickListener.onA2dpClick(position);
                }
            });
        }
        if(mOnHeadsetClickListener!=null){
            holder.devHeadset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnHeadsetClickListener.onHeadsetClick(position);
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BtDevice btDevice = mBtList.get(position);
        holder.devImage.setImageBitmap(btDevice.getBtImage());
        holder.devName.setText(btDevice.getBtName());
        holder.devA2dp.setText(btDevice.getA2dp());
        holder.devHeadset.setText(btDevice.getHeadset());
        if (btDevice.getA2dp() != null && btDevice.getA2dpConn() == 0) {
            holder.devA2dp.setTextColor(Color.parseColor("#f6aa3e"));
        } else {
            if (btDevice.getA2dp() != null && btDevice.getA2dpConn() == 1) {
                holder.devA2dp.setTextColor(Color.BLUE);
            }
        }
        if(btDevice.getHeadset()!=null && btDevice.getHeadsetConn()==0){
            holder.devHeadset.setTextColor(Color.LTGRAY);
        }else{
            if (btDevice.getHeadset()!=null && btDevice.getHeadsetConn()==1){
                holder.devHeadset.setTextColor(Color.BLUE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mBtList.size();
    }
}
