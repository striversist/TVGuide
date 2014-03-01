package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.views.NetImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HotProgramListAdapter extends BaseAdapter
{
    private static final String TAG = "HotProgramListAdapter";
    private Context mContext;
    private List<HashMap<String, String>> mProgramInfoList;
    
    public HotProgramListAdapter(Context context, List<HashMap<String, String>> programInfoList)
    {
        assert (context != null);
        assert (programInfoList != null);
        
        mProgramInfoList = new ArrayList<HashMap<String,String>>();
        
        mContext = context;
        mProgramInfoList.addAll(programInfoList);
    }
    
    public void updateItems(List<HashMap<String, String>> programInfoList)
    {
        if (programInfoList == null)
            return;
        mProgramInfoList.clear();
        mProgramInfoList.addAll(programInfoList);
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() 
    {
        return mProgramInfoList.size();
    }

    @Override
    public Object getItem(int position) 
    {
        return mProgramInfoList.get(position);
    }

    @Override
    public long getItemId(int position) 
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        ViewHolder holder;
        HashMap<String, String> programInfo = mProgramInfoList.get(position);
        
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hot_program_item, parent, false);
            TextView nameTextView = (TextView) convertView.findViewById(R.id.classify_item_name_tv);
            TextView profileTextView = (TextView) convertView.findViewById(R.id.classify_item_text_tv);
            NetImageView netImageView = (NetImageView) convertView.findViewById(R.id.classify_item_net_iv);
            
            nameTextView.setText(programInfo.get("name"));
            profileTextView.setText(programInfo.get("profile"));
            netImageView.loadImage(programInfo.get("picture_link"));
            
            holder = new ViewHolder();
            holder.nameTextView = nameTextView;
            holder.profileTextView = profileTextView;
            holder.netImageView = netImageView;
            convertView.setTag(holder);
        }
        else 
        {
            holder = (ViewHolder) convertView.getTag();
            holder.nameTextView.setText(programInfo.get("name"));
            holder.profileTextView.setText(programInfo.get("profile"));
            holder.netImageView.loadImage(programInfo.get("picture_link"));
        }        
        
        return convertView;
    }
    
    private class ViewHolder
    {
        TextView nameTextView;
        TextView profileTextView;
        NetImageView netImageView;
    }

}
