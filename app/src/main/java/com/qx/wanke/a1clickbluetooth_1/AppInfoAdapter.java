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

        mAppList.remove(position);
        notifyItemRemoved(position);
    }
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mAppList,fromPosition,toPosition);
        notifyItemMoved(fromPosition, toPosition);

        for (int i=0;i<mAppList.size();i++){
            int newListId=mAppList.get(i).getId();
            Apps updateApp=new Apps();
            updateApp.setOrder1(i+1);
            updateApp.update(newListId);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list,parent,false);
        final ViewHolder holder=new ViewHolder(view);

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
