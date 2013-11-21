package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ChannellistAdapter;
import com.tools.tvguide.adapters.ResultPageAdapter;
import com.tools.tvguide.adapters.ResultProgramAdapter;
import com.tools.tvguide.adapters.ResultProgramAdapter.Item;
import com.tools.tvguide.adapters.ResultProgramAdapter.IListItem;
import com.tools.tvguide.adapters.ResultProgramAdapter.LabelItem;
import com.tools.tvguide.adapters.ResultProgramAdapter.ContentItem;
import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity 
{
    private EditText mSearchEditText;
    private boolean mIsSelectAll = false;
    private ListView mProgramListView;
    private ListView mChannelListView;
    private String mKeyword;
    private List<IListItem> mItemProgramList;
    private List<HashMap<String, Object>> mItemChannelList; 
    private List<HashMap<String, String>> mOnPlayingProgramList;            // Key: id, title
    private LayoutInflater mInflater;
    private LinearLayout mContentLayout;
    private LinearLayout mNoSearchResultLayout;
    private LinearLayout mClassifyResultLayout;
    private LinearLayout.LayoutParams mCenterLayoutParams;
    private Handler mUpdateHandler;
    private MyProgressDialog mProgressDialog;
    private enum SelfMessage {MSG_SHOW_RESULT, MSG_REFRESH_ON_PLAYING_PROGRAM_LIST}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        mInflater = LayoutInflater.from(this);
        mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
        mProgramListView = (ListView)findViewById(R.id.search_list_view);
        mChannelListView = (ListView) mInflater.inflate(R.layout.activity_channellist, null).findViewById(R.id.channel_list);
        mItemProgramList = new ArrayList<IListItem>();
        mItemChannelList = new ArrayList<HashMap<String,Object>>();
        mOnPlayingProgramList = new ArrayList<HashMap<String,String>>();
        mProgressDialog = new MyProgressDialog(this);
        mContentLayout = (LinearLayout)findViewById(R.id.search_content_layout);
        mNoSearchResultLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null); 
        mClassifyResultLayout = (LinearLayout)mInflater.inflate(R.layout.search_result_tabs, null);
        mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ((TextView) mNoSearchResultLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.no_found_tips));
        createUpdateThreadAndHandler();
        
        mSearchEditText.setOnTouchListener(new View.OnTouchListener() 
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) 
            {
                if (v.getId() == mSearchEditText.getId())
                {
                    if (event.getAction() != MotionEvent.ACTION_DOWN)
                    {
                        return true;
                    }
                    
                    if (mIsSelectAll == true)
                    {
                        mSearchEditText.setSelection(mSearchEditText.getText().length());
                        mIsSelectAll = false;
                    }
                    else if (mSearchEditText.getText().length() > 0)
                    {
                        mSearchEditText.selectAll();
                        mIsSelectAll = true;
                    }
                    showInputKeyboard();
                    return true;
                }
                return false;
            }
        });
        
        mSearchEditText.setOnKeyListener(new View.OnKeyListener() 
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) 
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    search(v);
                    return true;
                }
                return false;
            }
        });
        
        mSearchEditText.addTextChangedListener(new TextWatcher() 
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) 
            {
                mIsSelectAll = false;                
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    public void search(View view)
    {
        hideInputKeyboard();
        if (mSearchEditText.getText().toString().trim().equals(""))
        {
            Toast.makeText(this, "请输入搜索关键字！", Toast.LENGTH_SHORT).show();
            return;
        }
        mKeyword = mSearchEditText.getText().toString().trim().split(" ")[0];
        updateResult();
    }
    
    private void updateResult()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_SEARCH) + "?keyword=" + mKeyword;
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    mItemProgramList.clear();
                    mItemChannelList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray;
                        // Get result_channels
                        resultArray = jsonRoot.getJSONArray("result_channels");
                        if (resultArray != null)
                        {
                            HashMap<String, HashMap<String, Object>> xmlChannelInfo = XmlParser.parseChannelInfo(SearchActivity.this);
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String id = resultArray.getJSONObject(i).getString("id");
                                String name = resultArray.getJSONObject(i).getString("name");
                                HashMap<String, Object> item = new HashMap<String, Object>();
                                item.put("id", id);
                                item.put("name", name);
                                if (xmlChannelInfo.get(id) != null)
                                {
                                    item.put("image", Utility.getImage(SearchActivity.this, (String) xmlChannelInfo.get(id).get("logo")));
                                }
                                mItemChannelList.add(item);
                            }
                        }
                        
                        // Get result_programs
                        resultArray = jsonRoot.getJSONArray("result_programs");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String id = resultArray.getJSONObject(i).getString("id");
                                String name = resultArray.getJSONObject(i).getString("name");
                                JSONArray programsArray = resultArray.getJSONObject(i).getJSONArray("programs");
                                
                                mItemProgramList.add(new LabelItem(name));
                                if (programsArray != null)
                                {
                                    for (int j=0; j<programsArray.length(); ++j)
                                    {
                                        String time = programsArray.getJSONObject(j).getString("time");
                                        String title = programsArray.getJSONObject(j).getString("title");    
                                        Item item = new Item();
                                        item.id = id;
                                        item.name = name;
                                        item.time = time;
                                        item.title = title;
                                        item.key = mKeyword;
                                        mItemProgramList.add(new ContentItem(item));
                                    }
                                }
                            }
                        }
                    }
                    uiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_RESULT.ordinal());
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    e.printStackTrace();
                }
            }
        });
        mProgressDialog.show();
    }
    
    private void updateOnPlayingProgramList()
    {
        mOnPlayingProgramList.clear();
        List<String> idList = new ArrayList<String>();
        for (int i=0; i<mItemChannelList.size(); ++i)
        {
            idList.add((String) mItemChannelList.get(i).get("id"));
        }
        AppEngine.getInstance().getContentManager().loadOnPlayingPrograms(idList, mOnPlayingProgramList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(SelfMessage.MSG_REFRESH_ON_PLAYING_PROGRAM_LIST.ordinal());
            }
        });
    }
    
    private void showInputKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchEditText, 0);
    }
    
    private void hideInputKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            SelfMessage selfMsg = SelfMessage.values()[msg.what];
            switch (selfMsg) 
            {
			    case MSG_SHOW_RESULT:
					mProgressDialog.dismiss();
		            mProgramListView.setAdapter(new ResultProgramAdapter(SearchActivity.this, mItemProgramList));
		            mChannelListView.setAdapter(new ChannellistAdapter(SearchActivity.this, mItemChannelList));
		            mSearchEditText.requestFocus();
		            if (mItemProgramList.isEmpty())
		            {
		                mContentLayout.removeAllViews();
		                mContentLayout.addView(mNoSearchResultLayout, mCenterLayoutParams);
		            }
		            else
		            {
//		                mContentLayout.removeAllViews();
//		                mContentLayout.addView(mProgramListView);
		                
		                ViewPager viewPager = (ViewPager) mClassifyResultLayout.findViewById(R.id.search_view_pager);
		                ResultPageAdapter adapter = new ResultPageAdapter();
//		                View layout1 = mInflater.inflate(R.layout.category_list1, null);
//		                View layout2 = mInflater.inflate(R.layout.category_list2, null);
//		                adapter.addView(layout1);
//		                adapter.addView(layout2);
		                
		                adapter.addView(mChannelListView);
		                adapter.addView(mProgramListView);
		                viewPager.setAdapter(adapter);
		                viewPager.setCurrentItem(0);
		                
		                mContentLayout.removeAllViews();
		                mContentLayout.addView(mClassifyResultLayout, mCenterLayoutParams);
		                
		                if (!mItemChannelList.isEmpty())
		                	updateOnPlayingProgramList();
		            }
					break;
				
				case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
					if (mOnPlayingProgramList != null)
                    {
                        for (int i=0; i<mItemChannelList.size(); ++i)
                        {
                            for (int j=0; j<mOnPlayingProgramList.size(); ++j)
                            {
                                if (mItemChannelList.get(i).get("id").equals(mOnPlayingProgramList.get(j).get("id")))
                                {
                                	mItemChannelList.get(i).put("program", "正在播出：" + mOnPlayingProgramList.get(j).get("title"));
                                }
                            }
                        }
                        mChannelListView.setAdapter(new ChannellistAdapter(SearchActivity.this, mItemChannelList));
                    }
					break;
				default:
					break;
			}
        }
    };
    
    
}
