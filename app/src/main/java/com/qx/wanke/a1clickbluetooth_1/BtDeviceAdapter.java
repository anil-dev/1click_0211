package com.qx.wanke.a1clickbluetooth_1;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    SharedPreferences pref;
    int flag;

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
            this.mOnItemLongClickListener = onItemLongClickListener;
    }
    public void setOffItemLongClickListener(){
        this.mOnItemLongClickListener=null;
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
        pref=mContext.getSharedPreferences("data",MODE_PRIVATE);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mBtList,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);

    }

    @Override
    public void onItemDismiss(int position) {
        return;
//////        在adpater里无法启动activity，只好在BtDeviceAdapter的构造函数里，把MainActivity.this传入mContext
//////        运行后，可以开启修改DeviceActivity，但android自动加上了去掉被滑动图标的动作，导致那里空了一块，再启动app时才重新出现
//////        发现长按和长按拖拽不冲突，拖拽能实现，长按也能弹出toast，所以打算不用onItemDismiss，改用长按实现启动Activity的工作。
////        修改后，发现长按虽然弹出toast并能拖动，但如果加上开窗口，就不行了，直接开窗口，不能拖动了。还是取消ItemTouchHelper的实现，
////        用长按来修改图标和排序吧。
////        Intent intent = new Intent(mContext, DeviceActivity.class);
////        mContext.startActivity(intent);
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

//            flag=pref.getInt("flag",2);
//            Log.d("anil", "onCreateViewHolder: flag="+String.valueOf(flag));

//        if (mOnItemLongClickListener != null && pref.getInt("flag",2)==0) {
//        2017.3.12 18:47终于搞定长按的2种方式，setOffItemLongClick()精彩，但开始错写成setOffItemClick()，居然取消了单击，但可以切换长按
//        的两种方式，改写LongClick反而没反应，只能进一种拖动方式。再仔细检查，原来是这里的flag捣乱，去掉flag条件后，搞定。
        if(mOnItemLongClickListener!=null){
//            奇怪，flag只取0和1，如果判断==0就一直进拖动流程、写==0，都进入下面流程，修改设备细节

            holder.devView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(holder.devView,position);
                    Intent intent = new Intent(mContext, DeviceActivity.class);
                    intent.putExtra("position",position);
                    intent.putExtra("dbId",mBtList.get(position).getBtId());
//                    键名开始写成dbid，而Devices.class里的接收键名写成dbId，导致运行时无法打开Devices的activity，闪退，这里改成dbId，正常了。
                    mContext.startActivity(intent);
                    notifyDataSetChanged();
//                    开始时，进入DeviceActivity后，改好各项目，用back键返回，设备的图标没有更新，要重新进入app，才能看到更新，
//                    在DeviceActivity的确定按钮上，返回MainActivity,再在这里加上notifyDataSetChanged()后，返回Main窗口，图标已更新。
//                    奇怪的是，在DeviceActivity里修改的都是数据库，没有直接对device列表或者列表子项进行修改，这里怎么知道要从数据库里重新取值，
//                    刷新显示的呢？
                    return true;
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BtDevice btDevice=mBtList.get(position);
        holder.devImage.setImageBitmap(btDevice.getBtImage());
        holder.devName.setText(btDevice.getBtName());
//        holder.devMacAdress.setText(btDevice.getBtMacAdress());
    }

    @Override
    public int getItemCount() {
        return mBtList.size();
    }
}
