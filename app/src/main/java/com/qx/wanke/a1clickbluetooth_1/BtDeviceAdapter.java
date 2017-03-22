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
//    SharedPreferences pref;
//    int flag;

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
        //这里=号后写错成mOnA2dp……，应该是onA2dp……，导致点击媒体处，无法响应子view的点击事件。as这个平台不错，此处显示灰色，表示程序里没用到
//        mOnA2dp，仔细一想，发现是写错了。
    }
    public void setmOnHeadsetClickListener(OnHeadsetClickListener onHeadsetClickListener) {
        this.mOnHeadsetClickListener=onHeadsetClickListener;
    }

    public void setmOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.mOnItemLongClickListener = onItemLongClickListener;
    }
//    public void setOffItemLongClickListener(){
//        this.mOnItemLongClickListener=null;
//    }

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
//            devMacAdress=(TextView)view.findViewById(R.id.dev_mac);
        }
    }


    public BtDeviceAdapter(List<BtDevice> btDeviceList,MainActivity mainActivity){
        mBtList=btDeviceList;
        mActivity=mainActivity;
//        pref=mContext.getSharedPreferences("data",MODE_PRIVATE);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mBtList,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);

        for(int i=0;i<mBtList.size();i++){
            int newListId=mBtList.get(i).getBtId();
            Devices updateDevice=new Devices();
            updateDevice.setOrder1(i);
            updateDevice.update(newListId);
//            用id的进行数据库的操作，好处在于不必搜索该条目，搜出List，再选第一条。可以得到id后，直接用update更新该条目。
        }
    }

    @Override
    public void onItemDismiss(int position) {

        Intent intent = new Intent(mActivity, DeviceActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("dbId",mBtList.get(position).getBtId());
        mActivity.startActivityForResult(intent,1);
//        不想用个按钮来切换 长按修改 和 长按拖拽，改用dismiss来做修改设备信息的方法（有position参数就行），只是滑动会暂时删掉图标一下，看起来有点怪。
//        return; (这句return有什么作用？好象有没有都一回事啊。
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

        if (mOnImageClickListener != null) {
//            int position=holder.getLayoutPosition();
//              下面几个子view的点击都要用到这个position，但如果写在这里，就提示 position要在内部类里使用access from inner class，需要声明成final
            holder.devImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnImageClickListener.onImageClick(holder.devView,position);//这里写view报错，说要声明final，why？
//                    Toast.makeText(v.getContext(),"devImage is clicked ",Toast.LENGTH_SHORT).show();

                }
            });
        }
        if(mOnA2dpClickListener!=null){
            holder.devA2dp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getLayoutPosition();
                    mOnA2dpClickListener.onA2dpClick(position);
//                    Toast.makeText(v.getContext(),"devA2dp is clicked ",Toast.LENGTH_SHORT).show();
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

/*改用dismiss来修改设备信息，不再用按钮来切换长按修改。把下面这段很精彩的切换注释掉，不要长按修改了，长按只剩下拖拽功能。
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
                    Intent intent = new Intent(mActivity, DeviceActivity.class);
                    intent.putExtra("position",position);
                    intent.putExtra("dbId",mBtList.get(position).getBtId());
//                    键名开始写成dbid，而Devices.class里的接收键名写成dbId，导致运行时无法打开Devices的activity，闪退，这里改成dbId，正常了。
                    mActivity.startActivityForResult(intent,1);
//                    这里为什么不能用startActivityForResult()？
//                    notifyDataSetChanged();
//                    开始时，进入DeviceActivity后，改好各项目，用back键返回，设备的图标没有更新，要重新进入app，才能看到更新，
//                    在DeviceActivity的确定按钮上，返回MainActivity,再在这里加上notifyDataSetChanged()后，返回Main窗口，图标已更新。
//                    奇怪的是，在DeviceActivity里修改的都是数据库，没有直接对device列表或者列表子项进行修改，这里怎么知道要从数据库里重新取值，
//                    刷新显示的呢？
                    return true;
                }
            });
        }*/
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BtDevice btDevice = mBtList.get(position);
        holder.devImage.setImageBitmap(btDevice.getBtImage());
        holder.devName.setText(btDevice.getBtName());
        holder.devA2dp.setText(btDevice.getA2dp());
        holder.devHeadset.setText(btDevice.getHeadset());
//        holder.devMacAdress.setText(btDevice.getBtMacAdress());
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
