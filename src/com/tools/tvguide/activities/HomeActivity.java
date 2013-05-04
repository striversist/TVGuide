package com.tools.tvguide.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends Activity 
{
    private ListView mCategoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCategoryList = (ListView)findViewById(R.id.category_list);
        
        String data[] = {"央视频道", "卫视频道", "高清频道", "地方台"};
        mCategoryList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data));
    }
}
