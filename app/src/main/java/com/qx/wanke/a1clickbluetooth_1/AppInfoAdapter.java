package com.qx.wanke.a1clickbluetooth_1;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by cw on 2017/2/18.
 */

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.ViewHolder> {

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
