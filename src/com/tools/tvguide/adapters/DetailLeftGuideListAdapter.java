package com.tools.tvguide.adapters;

import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.data.Channel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailLeftGuideListAdapter extends BaseAdapter 
{
    private Context mContext;
    private List<Channel> mChannelList;
    private int mCurIndex;

    public DetailLeftGuideListAdapter(Context context, List<Channel> channelList)
    {
        assert (context != null && channelList != null);
        
        mContext = context;
        mChannelList = channelList;
        mCurIndex = 0;
    }
    
    public void setCurrentIndex(int position)
    {
        assert(mChannelList != null && position >= 0);
        if (position >= mChannelList.size())
            return;
        mCurIndex = position;
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() 
    {
        assert (mChannelList != null);
        return mChannelList.size();
    }

    @Override
    public Object getItem(int position) 
    {
        assert (mChannelList != null);
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
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row, parent, false);
        }
        
        ImageView iconImageView = ((ImageView) convertView.findViewById(R.id.row_icon));
        TextView titleTextView = ((TextView) convertView.findViewById(R.id.row_title));
        titleTextView.setText(mChannelList.get(position).name);

        if (position == mCurIndex)
        {
            iconImageView.setVisibility(View.VISIBLE);
            titleTextView.setTextColor(mContext.getResources().getColor(R.color.dark_blue));
        }
        else
        {
            iconImageView.setVisibility(View.GONE);
            titleTextView.setTextColor(mContext.getResources().getColor(R.color.darkgray));
        }
        
        return convertView;
    }

}
