package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.data.Category;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.CacheControl;
import com.tools.tvguide.views.NetImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChannellistAdapter extends BaseAdapter 
{
    private Context mContext;
    private List<HashMap<String, Object>> mItemList = new ArrayList<HashMap<String,Object>>();
    
    public ChannellistAdapter(Context context, List<HashMap<String, Object>> data) 
    {
        assert (context != null);
        assert (data != null);
        
        mContext = context;
        mItemList.addAll(data);
    }
    
    public void setItemList(List<HashMap<String, Object>> data)
    {
        mItemList.clear();
        mItemList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() 
    {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) 
    {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) 
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        HashMap<String, Object> item = mItemList.get(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.channellist_item, parent, false);
        
        TextView channelNameTextView = (TextView) convertView.findViewById(R.id.channel_name_tv);
        NetImageView channelLogoNetImageView = (NetImageView) convertView.findViewById(R.id.channel_logo_niv);
        TextView onplayingProgramTextView = (TextView) convertView.findViewById(R.id.on_playing_program_tv);
        
        String channelName = (String) item.get("name");
        if (channelName != null)
            channelNameTextView.setText(channelName);
        
        String onplayingProgram = (String) item.get("program");
        if (onplayingProgram != null)
            onplayingProgramTextView.setText(onplayingProgram);
        
        String tvmaoId = (String) item.get("tvmao_id");
        Category category = (Category) item.get("category");
        if (tvmaoId != null && category != null && category.name != null)
        {
        	String logoUrl = UrlManager.getWebChannelLogoUrl(category.name, tvmaoId);
        	if (logoUrl != null)
        	{
        		channelLogoNetImageView.setCacheControl(CacheControl.Disk);
        		channelLogoNetImageView.loadImage(logoUrl);
        	}
        }
        
        return convertView;
    }
}
