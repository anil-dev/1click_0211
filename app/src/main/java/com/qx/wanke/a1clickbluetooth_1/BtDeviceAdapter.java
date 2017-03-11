package com.qx.wanke.a1clickbluetooth_1;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by cw on 2017/1/25.
 */

public class BtDeviceAdapter extends RecyclerView.Adapter<BtDeviceAdapter.ViewHolder>{

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
//            devMacAdress=(TextView)view.findViewById(R.id.dev_mac);
        }
    }

    private Context mContext;
    public BtDeviceAdapter(List<BtDevice> btDeviceList,Context context){
        mBtList=btDeviceList;
        mContext=context;
    }

//    @Override
//    public void onItemMove(int fromPosition, int toPosition) {
//        Collections.swap(mBtList,fromPosition,toPosition);
//        notifyItemMoved(fromPosition,toPosition);
//
//    }

//    @Override
//    public void onItemDismiss(int position) {
//////        在adpater里无法启动activity，只好在BtDeviceAdapter的构造函数里，把MainActivity.this传入mContext
//////        运行后，可以开启修改DeviceActivity，但android自动加上了去掉被滑动图标的动作，导致那里空了一块，再启动app时才重新出现
//////        发现长按和长按拖拽不冲突，拖拽能实现，长按也能弹出toast，所以打算不用onItemDismiss，改用长按实现启动Activity的工作。
////        修改后，发现长按虽然弹出toast并能拖动，但如果加上开窗口，就不行了，直接开窗口，不能拖动了。还是取消ItemTouchHelper的实现，
////        用长按来修改图标和排序吧。
////        Intent intent = new Intent(mContext, DeviceActivity.class);
////        mContext.startActivity(intent);
//    }

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
                    Intent intent = new Intent(mContext, DeviceActivity.class);
                    mContext.startActivity(intent);
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
//        holder.devMacAdress.setText(btDevice.getBtMacAdress());
    }

    @Override
    public int getItemCount() {
        return mBtList.size();
    }
}
