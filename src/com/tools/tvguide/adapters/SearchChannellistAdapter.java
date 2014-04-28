package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.utils.CacheControl;
import com.tools.tvguide.views.NetImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchChannellistAdapter extends BaseAdapter
{
    private Context mContext;
    private List<Channel> mChannelList = new ArrayList<Channel>();
    
    public SearchChannellistAdapter(Context context, List<Channel> channelList)
    {
        assert (context != null);
        assert (mChannelList != null);
        
        mContext = context;
        mChannelList.addAll(channelList);
    }
    
    @Override
    public int getCount() 
    {
        return mChannelList.size();
    }

    @Override
    public Object getItem(int position) 
    {
        return mChannelList.get(position);
    }

    @Override
    public long getItemId(int position) 
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        Channel channel = mChannelList.get(position);
        ViewHolder holder = null;
        if (convertView == null) 
        {
        	convertView = LayoutInflater.from(mContext).inflate(R.layout.channellist_item2, parent, false);
        	TextView channelNameTextView = (TextView) convertView.findViewById(R.id.channel_name_tv);
            NetImageView channelLogoNetImageView = (NetImageView) convertView.findViewById(R.id.channel_logo_niv);
            
            holder = new ViewHolder();
            holder.channelNameTextView = channelNameTextView;
            holder.channelLogoNetImageView = channelLogoNetImageView;
            convertView.setTag(holder);
        }
        else
        {
        	holder = (ViewHolder) convertView.getTag();
        }
        
        holder.channelNameTextView.setText(channel.name);
        holder.channelLogoNetImageView.setImageBitmap(null);
        holder.channelLogoNetImageView.setCacheControl(CacheControl.Disk);
        holder.channelLogoNetImageView.loadImage(channel.logoLink);
        
        return convertView;
    }
    
    private class ViewHolder
    {
        TextView channelNameTextView;
        NetImageView channelLogoNetImageView;
    }

}
