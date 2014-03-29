package com.tools.tvguide;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class SupportActivity extends Activity 
{

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
    }

    public void back(View view)
    {
        if (view instanceof Button)
        {
            // The same effect with press back key
            finish();
        }
    }

}
