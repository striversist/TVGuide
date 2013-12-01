package com.tools.tvguide.adapters;

import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResultProgramAdapter extends BaseAdapter 
{
    private Context mContext;
    private List<IListItem> mItemList;
    
    public ResultProgramAdapter(Context context, List<IListItem> itemList)
    {
        mContext = context;
        mItemList = itemList;
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
    public boolean isEnabled(int position) 
    {
        return mItemList.get(position).isClickable();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        return mItemList.get(position).getView(mContext, convertView, LayoutInflater.from(mContext));
    }
    
    public interface IListItem
    {
        public int getLayout();
        public boolean isClickable();
        public View getView(Context context, View convertView, LayoutInflater inflater);
        public void setExtraInfo(HashMap<String, ?> extraInfo);
        public HashMap<String, ?> getExtraInfo();
    }

    public static class LabelItem implements IListItem 
    {
        private String mLabel;
        private boolean mIsClickable;
        private HashMap<String, ?> mExtraInfo;
        public LabelItem(String label)
        {
            mLabel = label;
            mIsClickable = false;
        }
        
        @Override
        public int getLayout() 
        {
            return R.layout.search_list_label_item;
        }

        @Override
        public boolean isClickable() 
        {
            return mIsClickable;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            convertView = inflater.inflate(getLayout(), null);
            TextView title = (TextView) convertView.findViewById(R.id.search_item_label_text_view);
            title.setText(mLabel);
            return convertView;
        }
        
        @Override
        public HashMap<String, ?> getExtraInfo() 
        {
            return mExtraInfo;
        }

        @Override
        public void setExtraInfo(HashMap<String, ?> extraInfo) 
        {
            mExtraInfo = extraInfo;
        }
        
        public void setClickable(boolean clickable)
        {
            mIsClickable = clickable;
        }
    }

    public static class Item
    {
        public String id;
        public String name;
        public String time;
        public String title;
        public String key;
    }

    public static class ContentItem implements IListItem 
    {
        private String SEPERATOR = ": ";
        private Item mItem;
        private boolean mIsClickable;
        private HashMap<String, ?> mExtraInfo;
        public ContentItem(Item item)
        {
            mItem = item;
            mIsClickable = false;
        }
        
        @Override
        public int getLayout() 
        {
            return R.layout.search_list_content_item;
        }

        @Override
        public boolean isClickable() 
        {
            return mIsClickable;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            convertView = inflater.inflate(getLayout(), null);
            TextView tv = (TextView) convertView.findViewById(R.id.search_item_content_text_view);
            SpannableString ss;
            if (mItem.time != null)
                ss = new SpannableString(mItem.time + SEPERATOR + mItem.title);
            else
                ss = new SpannableString(mItem.title);
            
            if (mItem.key != null)
            {
                int start = ss.toString().indexOf(mItem.key);
                if (start != -1)
                {
                    ss.setSpan(new ForegroundColorSpan(Color.RED), start, start + mItem.key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            tv.setText(ss);
            return convertView;
        }
        
        @Override
        public HashMap<String, ?> getExtraInfo() 
        {
            return mExtraInfo;
        }

        @Override
        public void setExtraInfo(HashMap<String, ?> extraInfo) 
        {
            mExtraInfo = extraInfo;
        }
        
        public void setClickable(boolean clickable)
        {
            mIsClickable = clickable;
        }
    }
}