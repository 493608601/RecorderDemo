package com.gxsn.recorderdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gxsn.recorderdemo.MainActivity;
import com.gxsn.recorderdemo.PlayActivity;
import com.gxsn.recorderdemo.R;
import com.gxsn.recorderdemo.entity.Resource;

import java.util.List;

/**
 * Created by Administrator on 2017/4/6.
 */

public class ListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private List<Resource> resources;
    private Context context;

    public ListAdapter(Context context, List<Resource> resources) {
        this.resources = resources;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return resources.size();
    }

    @Override
    public Object getItem(int i) {
        return resources.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.ll = (LinearLayout) view.findViewById(R.id.ll);
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.duration = (TextView) view.findViewById(R.id.duration);
            holder.time = (TextView) view.findViewById(R.id.time);
            holder.type = (TextView) view.findViewById(R.id.type);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(resources.get(i).getName());
        holder.time.setText(resources.get(i).getTime());

        holder.duration.setText(resources.get(i).getDuration() / 1000 + "秒");
        if(resources.get(i).getType()==Resource.TYPE_AUDIO){
            holder.type.setText("音频");
        }else {
            holder.type.setText("视频");
        }



        return view;
    }

    /**
     * 存放控件
     */
    public final class ViewHolder {
        public TextView name;
        public TextView duration;
        public TextView type;
        public TextView time;
        public LinearLayout ll;
    }
}
