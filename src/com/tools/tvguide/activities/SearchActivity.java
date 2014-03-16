package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ChannellistAdapter;
import com.tools.tvguide.adapters.ChannellistAdapter2;
import com.tools.tvguide.adapters.ResultPageAdapter;
import com.tools.tvguide.adapters.ResultProgramAdapter;
import com.tools.tvguide.adapters.ResultProgramAdapter.Item;
import com.tools.tvguide.adapters.ResultProgramAdapter.IListItem;
import com.tools.tvguide.adapters.ResultProgramAdapter.LabelItem;
import com.tools.tvguide.adapters.ResultProgramAdapter.ContentItem;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.data.SearchResultCategory;
import com.tools.tvguide.data.SearchResultDataEntry;
import com.tools.tvguide.data.SearchResultCategory.Type;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;
import com.tools.tvguide.managers.SearchHtmlManager.SearchResultCallback;
import com.tools.tvguide.managers.SearchWordsManager;
import com.tools.tvguide.utils.HtmlUtils;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;
import com.tools.tvguide.views.MyViewPagerIndicator;
import com.tools.tvguide.views.SearchHotwordsView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends Activity implements Callback 
{
    private EditText mSearchEditText;
    private boolean mIsSelectAll = false;
    private String mKeyword;
    private List<IListItem> mItemProgramDataList;
    private List<HashMap<String, Object>> mItemChannelDataList; 
    private List<HashMap<String, String>> mOnPlayingProgramList;            // Key: id, title
    private List<Channel> mChannelList = new ArrayList<Channel>();
    private LayoutInflater mInflater;
    private LinearLayout mContentLayout;
    private LinearLayout mOriginContentLayout;
    private LinearLayout mNoSearchResultLayout;
    private LinearLayout mClassifyResultLayout;
    private LinearLayout.LayoutParams mCenterLayoutParams;
    private RelativeLayout mCancelImage;
    private int mResultProgramsNum;
    private MyProgressDialog mProgressDialog;
    private ViewPager mViewPager;
    private ResultPageAdapter mResultPagerAdapter;
    private String mOriginChannelsFormatString;
    private String mOriginProgramsFormatString;
    private enum SelfMessage {MSG_SHOW_RESULT, MSG_REFRESH_ON_PLAYING_PROGRAM_LIST, MSG_SHOW_POP_SEARCH, MSG_SHOW_CATEGORY, MSG_SHOW_CHANNEL, MSG_SHOW_PROGRAM_SCHEDULE}
    private final int TAB_INDEX_CHANNELS = 0;
    private final int TAB_INDEX_PROGRAMS = 1;
    private List<String> mPopSearchList;
    private Handler mUiHandler;
    
    private List<SearchResultCategory> mCategoryList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        mInflater = LayoutInflater.from(this);
        mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
        mItemProgramDataList = new ArrayList<IListItem>();
        mItemChannelDataList = new ArrayList<HashMap<String,Object>>();
        mOnPlayingProgramList = new ArrayList<HashMap<String,String>>();
        mProgressDialog = new MyProgressDialog(this);
        mCancelImage = (RelativeLayout)findViewById(R.id.search_cancel_layout);
        mContentLayout = (LinearLayout)findViewById(R.id.search_content_layout);
        mOriginContentLayout = (LinearLayout)mInflater.inflate(R.layout.search_init_layout, null);
        mNoSearchResultLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null); 
        mClassifyResultLayout = (LinearLayout)mInflater.inflate(R.layout.search_result_tabs, null);
        mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ((TextView) mNoSearchResultLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.no_found_tips));
        mOriginChannelsFormatString = getResources().getString(R.string.channels);
        mOriginProgramsFormatString = getResources().getString(R.string.programs);
        mViewPager = (ViewPager) mClassifyResultLayout.findViewById(R.id.search_view_pager);
        mResultPagerAdapter = new ResultPageAdapter();
        mPopSearchList = new ArrayList<String>();
        mUiHandler = new Handler(this);
        mCategoryList = new ArrayList<SearchResultCategory>();
        
        // NOTE：Should follow the TAB INDEX order at the beginning of the class
//        ListView channelListView = (ListView) mInflater.inflate(R.layout.activity_channellist, null).findViewById(R.id.channel_list);
//        ListView programListView = (ListView) mInflater.inflate(R.layout.search_programs_layout, null).findViewById(R.id.program_list_view);
//        mResultPagerAdapter.addView(channelListView);
//        mResultPagerAdapter.addView(programListView);
//        mViewPager.setAdapter(mResultPagerAdapter);
        
        initContentLayout();
        ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.search_hotwords_view)).setOnItemClickListener(new SearchHotwordsView.OnItemClickListener() 
        {
            @Override
            public void onItemClick(String string) 
            {
                mKeyword = string;
                mSearchEditText.setText(mKeyword);
                mSearchEditText.setSelection(mSearchEditText.getText().length());
                updateSearchResult();
            }
        });
        ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.history_search_view)).setOnItemClickListener(new SearchHotwordsView.OnItemClickListener() 
        {
            @Override
            public void onItemClick(String string) 
            {
                mKeyword = string;
                mSearchEditText.setText(mKeyword);
                mSearchEditText.setSelection(mSearchEditText.getText().length());
                updateSearchResult();
            }
        });
        ((Button) mOriginContentLayout.findViewById(R.id.history_clear_btn)).setOnClickListener(new View.OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				AppEngine.getInstance().getSearchWordsManager().clearHistorySearch();
				updateHistorySearch();
			}
		});
        
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) 
            {
            }
            
            @Override
            public void afterTextChanged(Editable editable) 
            {
                if (editable.toString().trim().length() > 0)
                {
                    if (mCancelImage.getVisibility() != View.VISIBLE)
                        mCancelImage.setVisibility(View.VISIBLE);
                }
                else
                {
                    if (mCancelImage.getVisibility() == View.VISIBLE)
                        mCancelImage.setVisibility(View.GONE);
                    initContentLayout();
                }
            }
        });
        
        MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
        indicator.setOnTabClickListener(new MyViewPagerIndicator.OnTabClickListener() 
        {
            @Override
            public void onTabClick(int index, Object tag) 
            {
//                mViewPager.setCurrentItem(index);
            }
        });
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
                indicator.setCurrentTab(position);
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
            {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) 
            {
            }
        });
        
//        channelListView.setOnItemClickListener(new OnItemClickListener() 
//        {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
//            {
//                String channelId = (String) mItemChannelDataList.get(position).get("id");
//                String channelName = (String) mItemChannelDataList.get(position).get("name");
//                Intent intent = new Intent(SearchActivity.this, ChannelDetailActivity.class);
//                intent.putExtra("id", channelId);
//                intent.putExtra("name", channelName);
//                startActivity(intent);
//            }
//        });
        
        updateHistorySearch();
        updatePopSearch();
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
        updateSearchResult();
    }
    
    public void cancel(View view)
    {
        mSearchEditText.setText("");
        initContentLayout();
    }
    
    private void initContentLayout()
    {
        mContentLayout.removeAllViews();
        mContentLayout.addView(mOriginContentLayout, mCenterLayoutParams);
    }
    
    private void updateSearchResult()
    {
        AppEngine.getInstance().getSearchWordsManager().addSearchRecord(mKeyword);
        mItemProgramDataList.clear();
        mItemChannelDataList.clear();
        mChannelList.clear();
        mResultProgramsNum = 0;
        final List<Channel> channels = new ArrayList<Channel>();
        final List<HashMap<String, Object>> programs = new ArrayList<HashMap<String,Object>>();
//        AppEngine.getInstance().getContentManager().loadSearchResult(mKeyword, channels, programs, new ContentManager.LoadListener() 
//        {
//            @Override
//            public void onLoadFinish(int status) 
//            {
//                HashMap<String, HashMap<String, Object>> xmlChannelInfo = XmlParser.parseChannelInfo(SearchActivity.this);
//                for (int i=0; i<channels.size(); ++i)
//                {
//                    HashMap<String, Object> item = new HashMap<String, Object>();
//                    item.put("id", channels.get(i).id);
//                    item.put("name", channels.get(i).name);
//                    if (xmlChannelInfo.get(channels.get(i).id) != null)
//                    {
//                        item.put("image", Utility.getImage(SearchActivity.this, (String) xmlChannelInfo.get(channels.get(i).id).get("logo")));
//                    }
//                    mItemChannelDataList.add(item);
//                }
//                
//                for (int i=0; i<programs.size(); ++i)
//                {
//                    Channel channel = (Channel) programs.get(i).get("channel");
//                    List<Program> programList = (List<Program>) programs.get(i).get("programs");
//                    if (channel == null || programList == null)
//                        continue;
//                    
//                    mItemProgramDataList.add(new LabelItem(channel.name, R.layout.hot_channel_tvsou_item, R.id.hot_channel_name_tv));
//                    for (int j=0; j<programList.size(); ++j)
//                    {
//                        Item item = new Item();
//                        item.id = channel.id;
//                        item.name = channel.name;
//                        item.time = programList.get(j).time;
//                        item.title = programList.get(j).title;
//                        item.key = mKeyword;
//                        item.hasLink = false;
//                        mItemProgramDataList.add(new ContentItem(item, R.layout.hot_program_tvsou_item, R.id.hot_program_name_tv));
//                    }
//                    mResultProgramsNum += programList.size();
//                }
//                mUiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_RESULT.ordinal());
//            }
//        });
        AppEngine.getInstance().getSearchHtmlManager().search(0, mKeyword, new SearchResultCallback() 
        {
            @Override
            public void onCategoriesLoaded(int requestId, List<SearchResultCategory> categoryList) 
            {
                if (categoryList != null)
                {
                    mCategoryList.clear();
                    mCategoryList.addAll(categoryList);
                    mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_CATEGORY.ordinal()).sendToTarget();
                }
            }
            
            @Override
            public void onEntriesLoaded(int requestId, Type categoryType, List<SearchResultDataEntry> entryList) 
            {
                if (categoryType == Type.Channel)
                {
                    for (int i=0; i<entryList.size(); ++i)
                    {
//                        HashMap<String, Object> item = new HashMap<String, Object>();
//                        item.put("id", HtmlUtils.filterTvmaoId(entryList.get(i).detailLink));
//                        item.put("name", entryList.get(i).name);
//                        mItemChannelDataList.add(item);
                        Channel channel = new Channel();
                        channel.name = entryList.get(i).name;
                        channel.tvmaoId = HtmlUtils.filterTvmaoId(entryList.get(i).detailLink);
                        channel.tvmaoLink = entryList.get(i).detailLink;
                        channel.logoLink = entryList.get(i).imageLink;
                        mChannelList.add(channel);
                    }
                    mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_CHANNEL.ordinal()).sendToTarget();
                }
            }
            
            @Override
            public void onProgramScheduleLoadeded(int requestId, int pageIndex, List<HashMap<String, Object>> scheduleList) 
            {
                
            }
        });
        mProgressDialog.show();
    }
    
//    private void updateOnPlayingProgramList()
//    {
//        mOnPlayingProgramList.clear();
//        List<String> idList = new ArrayList<String>();
//        for (int i=0; i<mItemChannelDataList.size(); ++i)
//        {
//            idList.add((String) mItemChannelDataList.get(i).get("id"));
//        }
//        AppEngine.getInstance().getContentManager().loadOnPlayingPrograms(idList, mOnPlayingProgramList, new ContentManager.LoadListener() 
//        {    
//            @Override
//            public void onLoadFinish(int status) 
//            {
//                mUiHandler.sendEmptyMessage(SelfMessage.MSG_REFRESH_ON_PLAYING_PROGRAM_LIST.ordinal());
//            }
//        });
//    }
    
    private void updateHistorySearch()
    {
        SearchWordsManager manager = AppEngine.getInstance().getSearchWordsManager();
        SearchHotwordsView historySearchView = (SearchHotwordsView) mOriginContentLayout.findViewById(R.id.history_search_view);
        RelativeLayout historySearchTipsLayout = (RelativeLayout) mOriginContentLayout.findViewById(R.id.history_search_tips_layout);
        if (manager.getHistorySearch().isEmpty())
        {
        	historySearchTipsLayout.setVisibility(View.GONE);
            historySearchView.setVisibility(View.GONE);
        }
        else
        {
        	historySearchTipsLayout.setVisibility(View.VISIBLE);
            historySearchView.setVisibility(View.VISIBLE);
            historySearchView.setWords(manager.getHistorySearch().toArray(new String[0]));
        }
    }
    
    private void updatePopSearch()
    {
        mPopSearchList.clear();
        mPopSearchList = AppEngine.getInstance().getSearchWordsManager().getPopSearch();
        if (mPopSearchList.size() > 0)
        {
            mUiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
        }
        else
        {
            AppEngine.getInstance().getSearchWordsManager().updatePopSearch(new SearchWordsManager.UpdateListener() 
            {
                @Override
                public void onUpdateFinish(List<String> result) 
                {
                    mPopSearchList.addAll(result);
                    mUiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
                }
            });
        }
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

    @Override
    public boolean handleMessage(Message msg) 
    {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg) 
        {
//            case MSG_SHOW_RESULT:
//                mProgressDialog.dismiss();
//                // 数据拷贝，防止Crash: "Make sure the content of your adapter is not modified from a background thread, but only from the UI thread"
//                List<HashMap<String, Object>> itemChannelList = new ArrayList<HashMap<String,Object>>();
//                itemChannelList.addAll(mItemChannelDataList);
//                List<IListItem> itemProgramList = new ArrayList<ResultProgramAdapter.IListItem>();
//                itemProgramList.addAll(mItemProgramDataList);
//                
//                ((ListView)mResultPagerAdapter.getView(TAB_INDEX_CHANNELS)).setAdapter(new ChannellistAdapter(SearchActivity.this, itemChannelList));
//                ((ListView)mResultPagerAdapter.getView(TAB_INDEX_PROGRAMS)).setAdapter(new ResultProgramAdapter(SearchActivity.this, itemProgramList));
//                mSearchEditText.requestFocus();
//                if (itemChannelList.isEmpty() && itemProgramList.isEmpty())
//                {
//                    mContentLayout.removeAllViews();
//                    mContentLayout.addView(mNoSearchResultLayout, mCenterLayoutParams);
//                }
//                else
//                {
//                    MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
//                    indicator.reset();
//                    indicator.addTab(String.format(mOriginChannelsFormatString, mItemChannelDataList.size()), null);
//                    indicator.addTab(String.format(mOriginProgramsFormatString, mResultProgramsNum), null);
//                    
//                    if (!itemChannelList.isEmpty())
//                    {
//                        mViewPager.setCurrentItem(TAB_INDEX_CHANNELS);
//                        indicator.setCurrentTab(TAB_INDEX_CHANNELS);
//                    }
//                    else 
//                    {
//                        mViewPager.setCurrentItem(TAB_INDEX_PROGRAMS);
//                        indicator.setCurrentTab(TAB_INDEX_PROGRAMS);
//                    }
//                    
//                    mContentLayout.removeAllViews();
//                    mContentLayout.addView(mClassifyResultLayout, mCenterLayoutParams);
//                    
//                    if (!itemChannelList.isEmpty())
//                        updateOnPlayingProgramList();
//                }
//                updateHistorySearch();
//                break;
//            
//            case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
//                if (mOnPlayingProgramList != null)
//                {
//                    List<HashMap<String, Object>> itemChannelList1 = new ArrayList<HashMap<String,Object>>();
//                    itemChannelList1.addAll(mItemChannelDataList);
//                    for (int i=0; i<itemChannelList1.size(); ++i)
//                    {
//                        for (int j=0; j<mOnPlayingProgramList.size(); ++j)
//                        {
//                            if (itemChannelList1.get(i).get("id").equals(mOnPlayingProgramList.get(j).get("id")))
//                            {
//                                itemChannelList1.get(i).put("program", "正在播出：" + mOnPlayingProgramList.get(j).get("title"));
//                            }
//                        }
//                    }
//                    ((ListView)mResultPagerAdapter.getView(TAB_INDEX_CHANNELS)).setAdapter(new ChannellistAdapter(SearchActivity.this, itemChannelList1));
//                }
//                break;
//            case MSG_SHOW_POP_SEARCH:
//                ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.search_hotwords_view)).setWords(mPopSearchList.toArray(new String[0]));
//                break;
            case MSG_SHOW_CATEGORY:
                mProgressDialog.dismiss();
                MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
                indicator.reset();
                for (int i=0; i<mCategoryList.size(); ++i)
                {
                    indicator.addTab(mCategoryList.get(i).name, null);
                    ListView channelListView = (ListView) mInflater.inflate(R.layout.activity_channellist, null).findViewById(R.id.channel_list);
                    mResultPagerAdapter.addView(channelListView);
                }
                mViewPager.setAdapter(mResultPagerAdapter);
                mContentLayout.removeAllViews();
                mContentLayout.addView(mClassifyResultLayout, mCenterLayoutParams);
                updateHistorySearch();
                break;
            case MSG_SHOW_CHANNEL:
                // 数据拷贝，防止Crash: "Make sure the content of your adapter is not modified from a background thread, but only from the UI thread"
                List<Channel> channelList = new ArrayList<Channel>();
                channelList.addAll(mChannelList);
                int tabIndex = getCategoryTypeIndex(SearchResultCategory.Type.Channel);
                ((ListView)mResultPagerAdapter.getView(tabIndex)).setAdapter(new ChannellistAdapter2(SearchActivity.this, channelList));
                break;
            default:
                break;
        }
        return true;
    }
    
    private int getCategoryTypeIndex(SearchResultCategory.Type type)
    {
        int result = -1;
        for (int i=0; i<mCategoryList.size(); ++i)
        {
            if (mCategoryList.get(i).type == type)
            {
                result = i;
                break;
            }
        }
        return result;
    }
}
