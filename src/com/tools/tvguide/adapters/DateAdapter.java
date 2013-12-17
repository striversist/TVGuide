package com.tools.tvguide.adapters;

import java.util.List;

import com.tools.tvguide.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DateAdapter extends BaseAdapter
{
    public static class DateData
    {
        public String mDate;
        public DateData(String date)
        {
            mDate = date;
        }
    }
    
    private Context mContext;
    private List<DateData> mDataList;
    private int mCurIndex;
    
    public DateAdapter(Context context, List<DateData> list)
    {
        mContext = context;
        mDataList = list;
        mCurIndex = 0;
    }
    
    public void setCurrentIndex(int position)
    {
        assert(position >= 0);
        if (position >= mDataList.size())
            return;
        mCurIndex = position;
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() 
    {
        assert(mDataList != null);
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) 
    {
        assert(mDataList != null);
        return mDataList.get(position);
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
        titleTextView.setText(mDataList.get(position).mDate);

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
