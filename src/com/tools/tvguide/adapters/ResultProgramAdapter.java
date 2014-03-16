package com.tools.tvguide.adapters;

import java.util.ArrayList;
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
import android.widget.ImageView;
import android.widget.TextView;

public class ResultProgramAdapter extends BaseAdapter 
{
    private Context mContext;
    private List<IListItem> mItemList = new ArrayList<ResultProgramAdapter.IListItem>();
    
    public ResultProgramAdapter(Context context, List<IListItem> itemList)
    {
        mContext = context;
        mItemList.addAll(itemList);
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
        public void setItemView(IItemView itemView);
        public void setExtraInfo(HashMap<String, ?> extraInfo);
        public HashMap<String, ?> getExtraInfo();
    }
    
    public interface IItemView
    {
        public View getView(Context context, View convertView, LayoutInflater inflater);
    }

    public static class LabelItem implements IListItem 
    {
        private String mLabel;
        private boolean mIsClickable;
        private HashMap<String, ?> mExtraInfo;
        private int mLayout;
        private int mItemId;
        private IItemView mItemView;
        public LabelItem(String label, int layout, int id)
        {
            mLabel = label;
            mIsClickable = false;
            mLayout = layout;
            mItemId = id;
        }
        
        @Override
        public int getLayout() 
        {
            return mLayout;
        }

        @Override
        public boolean isClickable() 
        {
            return mIsClickable;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            if (mItemView != null)
                return mItemView.getView(context, convertView, inflater);
            
            convertView = inflater.inflate(getLayout(), null);
            TextView title = (TextView) convertView.findViewById(mItemId);
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

        @Override
        public void setItemView(IItemView itemView) 
        {
            mItemView = itemView;
        }
    }

    public static class Item
    {
        public String id;
        public String name;
        public String time;
        public String title;
        public String key;
        public boolean hasLink;
    }

    public static class ContentItem implements IListItem 
    {
        private String SEPERATOR = ":　";
        private Item mItem;
        private boolean mIsClickable;
        private HashMap<String, ?> mExtraInfo;
        private int mLayout;
        private int mItemId;
        private IItemView mItemView;
        public ContentItem(Item item, int layout, int id)
        {
            mItem = item;
            mIsClickable = false;
            mLayout = layout;
            mItemId = id;
        }
        
        @Override
        public int getLayout() 
        {
            return mLayout;
        }

        @Override
        public boolean isClickable() 
        {
            return mIsClickable;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            if (mItemView != null)
                return mItemView.getView(context, convertView, inflater);

            convertView = inflater.inflate(getLayout(), null);
            TextView tv = (TextView) convertView.findViewById(mItemId);
            ImageView indicator = (ImageView) convertView.findViewById(R.id.hot_program_tag_iv);
            
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
            
            if (mItem.hasLink)
            	indicator.setVisibility(View.VISIBLE);
            else
            	indicator.setVisibility(View.INVISIBLE);
            
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

        @Override
        public void setItemView(IItemView itemView) 
        {
            mItemView = itemView;
        }
    }
}