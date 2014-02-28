package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.R;
import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.managers.ContentManager.LoadListener;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends Activity 
{
    private EditText mFeedbackText;
    private EditText mEmailText;
    private Handler mUpdateHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
        mFeedbackText = (EditText)findViewById(R.id.feedback_edit);
        mEmailText = (EditText)findViewById(R.id.email_edit);
    }

    public void back(View view)
    {
        if (view instanceof Button)
        {
            // The same effect with press back key
            finish();
        }
    }
    
    public void submit(View view)
    {
        if (mFeedbackText.getText().toString().trim().equals(""))
        {
            Toast.makeText(FeedbackActivity.this, "您是不是忘了写什么？", Toast.LENGTH_LONG).show();
            return;
        }
        
        postData();
        Toast.makeText(FeedbackActivity.this, "已提交，感谢您的建议！", Toast.LENGTH_LONG).show();
        mFeedbackText.setText("");
    }
    
    private void postData()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                pairs.add(new BasicNameValuePair("feedback", mFeedbackText.getText().toString()));
                String email = mEmailText.getText().toString().trim();
                if (email.length() > 0)
                    pairs.add(new BasicNameValuePair("email", email));
                
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.ProxyUrl.Feedback);
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(url);
                    getter.getJSONsObject(pairs);
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
