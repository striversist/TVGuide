package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HotProgramListAdapter extends BaseAdapter
{
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
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.classify_item, parent, false);
        }
        
        HashMap<String, String> programInfo = mProgramInfoList.get(position);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.classify_item_name_tv);
        TextView profileTextView = (TextView) convertView.findViewById(R.id.classify_item_text_tv);
        
        nameTextView.setText(programInfo.get("name"));
        profileTextView.setText(programInfo.get("profile"));
        
        return convertView;
    }

}
