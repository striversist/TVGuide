package com.tools.tvguide.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.managers.ContentManager.LoadListener;
import com.tools.tvguide.views.SearchHotwordsView;

import android.content.Context;

public class SearchWordsManager
{
    private Context mContext;
    private List<String> mPopSearchList;
    public abstract interface UpdateListener
    {
        public void onUpdateFinish(List<String> result);
    }
    
    public SearchWordsManager(Context context)
    {
        assert (context != null);
        mContext = context;
        mPopSearchList = new ArrayList<String>();
        load();
    }
    
    public List<String> getPopSearch()
    {
        List<String> popSearchList = new ArrayList<String>();
        popSearchList.addAll(mPopSearchList);
        return popSearchList;
    }
    
    public void updatePopSearch(final UpdateListener listener)
    {
        assert (listener != null);
        mPopSearchList.clear();
        AppEngine.getInstance().getContentManager().loadPopSearch(SearchHotwordsView.MAX_WORDS, mPopSearchList, new ContentManager.LoadListener() 
        {
            @Override
            public void onLoadFinish(int status) 
            {
                if (status == LoadListener.SUCCESS)
                {
                    listener.onUpdateFinish(mPopSearchList);
                    HashMap<String, List<String>> saveData = new HashMap<String, List<String>>();
                    saveData.put("pop_search", mPopSearchList);
                    AppEngine.getInstance().getCacheManager().saveSearchWords(saveData);
                }
            }
        });
    }
    
    private void load()
    {
        HashMap<String, List<String>> saveData = new HashMap<String, List<String>>();
        boolean success = AppEngine.getInstance().getCacheManager().loadSearchWords(saveData);
        if (success)
        {
            if (saveData.get("pop_search") != null)
            {
                mPopSearchList = saveData.get("pop_search");
            }
        }
    }
}
