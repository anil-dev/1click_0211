package com.qx.wanke.a1clickbluetooth_1;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by cw on 2017/2/18.
 */

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private List<AppInfo> mAppList;
    public AppInfoAdapter(List<AppInfo> appList){mAppList=appList;}

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener=onItemClickListener;
    }
    public void setmOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        this.mOnItemLongClickListener=onItemLongClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View appView;
        ImageView appImage;
        TextView appLabel;

        public ViewHolder(View view){
            super(view);
            appView=view;
            appImage=(ImageView)view.findViewById(R.id.app_icon);
            appLabel = (TextView) view.findViewById(R.id.app_name);
    }
    }

    @Override
    public void onItemDismiss(int position) {
        Apps updateApp=new Apps();
        updateApp.setToDefault("order1");
        updateApp.updateAll("label=?",mAppList.get(position).getAppLable());

//        updateApp.updateAll("package_name=?",mAppList.get(position).getPkgName());
//        Log.d("anil", "onItemDismiss: pacName "+mAppList.get(position).getPkgName());
//        用这句更新数据库，想使dismiss的app不再显示在列表里，结果去除后，再次打开，仍在列表里。
//        log一下，才发现，pacName是null，因为mAppList只取了图标和label.

        mAppList.remove(position);
        notifyItemRemoved(position);
    }
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Log.d("anil", "onItemMove: "+String.valueOf(fromPosition)+" "+String.valueOf(toPosition));

/*      下面这段本来是为了用toPosition找到该位置的app的label，再用label去数据库里找对应app的order1，更新它。
        但target.get(0).getOrder1()总是等于1，再次打开app无法固定上次的移动。想改成用id来查找对应app，在
        AppInfo和MainActivity里的初始化applist增加了id元素。
        for (int i=0;i<mAppList.size();i++){

        List<Apps> target = DataSupport.where("label=?", mAppList.get(toPosition).getAppLable())
//                .limit(1)
                .find(Apps.class);
        int targetOrder=target.get(0).getOrder1();
//        写target.get(1).getOrder1()这句后一直闪退，报java.lang.IndexOutOfBoundsException: Invalid index 1, size is 1
//          开始以为是下面的for循环数组越界。后来仔细看出错代码，才想起来是target只有一个元素，要get(0).才对。
        Log.d("anil", "onItemMove: targetOrder is "+String.valueOf(targetOrder));
//        为什么这里的log没法显示？MainActivity里的显示正常——因为get(1)错误闪退，所以这句log根本没运行到。
//        litepal查询某列，要这么麻烦吗？先查找label的名字，得到List，再用List取里面的order1值。为什么不能直接查找label是某值所对应的order1呢？
        for(int i=toPosition;i<mAppList.size()-1;i++){
            Log.d("anil", "onItemMove: i is "+String.valueOf(i));
            Apps updateApp=new Apps();
            updateApp.setOrder1(updateApp.getOrder1()+1);
            updateApp.updateAll("label=?",mAppList.get(i).getAppLable());
        }
        Apps updateApp=new Apps();
        updateApp.setOrder1(targetOrder);
        updateApp.updateAll("label=?", mAppList.get(fromPosition).getAppLable());*/

        Collections.swap(mAppList,fromPosition,toPosition);
//        这个swap有点意思，按我的想法，移动一个元素到toPosition，并不是交换swap，而是要把toPosition后面的元素顺序号都加一（到fromPosition即可）
        notifyItemMoved(fromPosition, toPosition);

        for (int i=0;i<mAppList.size();i++){
            int newListId=mAppList.get(i).getId();
            Apps updateApp=new Apps();
//          下面这句写成setOrder1(i+1)是因为，set某列值时不允许等于0，因为0是默认值，要用setToDefault，所以每次移动后序列（包括mAppList
//          和mBtList）重新赋顺序值时，总是前两个数据库里的order1都是1，导致在移动第二个图标到第一个时，总是不成功。
            updateApp.setOrder1(i+1);
            updateApp.update(newListId);
        }
//        这个for循环写得爽。本来打算在mAppList交换前，更新数据库，要考虑fromPosition和toPosition的大小，移动的方向，
//        移动后保留被移动图标的位置，从toPosition开始循环到列表结尾，用position找到name或者id，再去数据库里找对应的app，再更新它的order1，
//        再想一想，可以在列表更新好后，再来更新数据库啊。找到新列表里各个app的id，去数据库里把对应的app的order1设置成新列表的顺序号即可。
//          写好后试运行，一次成功。爽！左拖右拖，都没有问题，再次打开，位置都被固定。
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list,parent,false);
        final ViewHolder holder=new ViewHolder(view);

//        holder.appView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position=holder.getAdapterPosition();
//                Intent intent=mAppList.get(position).getIntent();
//                .startActivity(intent);
//            }
//        });

        if (mOnItemClickListener!=null){
            holder.appView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(holder.appView,position);
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo appInfo=mAppList.get(position);
        holder.appImage.setImageDrawable(appInfo.getAppIcon());
        holder.appLabel.setText(appInfo.getAppLable());
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }
}
