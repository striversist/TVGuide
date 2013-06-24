package com.tools.tvguide.managers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.content.Context;
import android.os.Handler;


public class ContentManager 
{
    private Context mContext;
    private Handler mUpdateHandler;
    
    public static abstract interface LoadListener
    {
        public static final int SUCCESS = 0;
        public static final int FAIL    = -1;
        abstract void onLoadFinish(int status);
    }
    
    public ContentManager(Context context)
    {
        mContext = context;
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    public boolean loadCategoriesByType(final String type, final List<HashMap<String, String>> result, final LoadListener listener)
    {
        boolean loadFromCache = false;
        loadFromCache = AppEngine.getInstance().getCacheManager().loadCategoriesByType(type, result);
        if (loadFromCache == true)
        {
            return true;    // sync loaded
        }
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = UrlManager.URL_CATEGORIES + "?type=" + type;
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray categoryArray = jsonRoot.getJSONArray("categories");
                        if (categoryArray != null)
                        {
                            for (int i=0; i<categoryArray.length(); ++i)
                            {
                                HashMap<String, String> category = new HashMap<String, String>();
                                category.put("id", categoryArray.getJSONObject(i).getString("id"));
                                category.put("name", categoryArray.getJSONObject(i).getString("name"));
                                category.put("has_sub_category", categoryArray.getJSONObject(i).getString("has_sub_category"));
                                result.add(category);
                            }
                        }
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                    AppEngine.getInstance().getCacheManager().saveCatgegoriesByType(type, result);
                }
                catch (MalformedURLException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
            }
        });
        return false;
    }
    
    public boolean loadChannelsByCategory(final String categoryId, final List<HashMap<String, String>> result, final LoadListener listener)
    {
        boolean loadFromCache = false;
        loadFromCache = AppEngine.getInstance().getCacheManager().loadChannelsByCategory(categoryId, result);
        if (loadFromCache == true)
        {
            return true;    // sync loaded
        }
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = UrlManager.URL_CHANNELS + "?category=" + categoryId;
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray channelListArray = jsonRoot.getJSONArray("channels");
                        if (channelListArray != null)
                        {
                            for (int i=0; i<channelListArray.length(); ++i)
                            {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("id", channelListArray.getJSONObject(i).getString("id"));
                                map.put("name", channelListArray.getJSONObject(i).getString("name"));
                                result.add(map);
                            }
                        }
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                    AppEngine.getInstance().getCacheManager().saveChannelsByCategory(categoryId, result);
                }
                catch (MalformedURLException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
            }
        });
        return false;
    }
    
    public boolean loadProgramsByChannel(final String channelId, final int day, final List<HashMap<String, String>> result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = UrlManager.URL_CHOOSE + "?channel=" + channelId + "&day=" + day;
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("time", resultArray.getJSONObject(i).getString("time"));
                                map.put("title", resultArray.getJSONObject(i).getString("title"));
                                result.add(map);
                            }
                        }
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                }
                catch (MalformedURLException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
            }
        });
        return false;
    }
    
    public boolean loadOnPlayingPrograms(final List<String> idList, final List<HashMap<String, String>> result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                //String test = "{\"channels\":[\"cctv1\", \"cctv3\"]}";
                String idArray = "[";
                for (int i=0; i<idList.size(); ++i)
                {
                    idArray += "\"" + idList.get(i) + "\"";
                    if (i < (idList.size() - 1))
                    {
                        idArray += ",";
                    }
                }
                idArray += "]";
                pairs.add(new BasicNameValuePair("channels", "{\"channels\":" + idArray + "}"));
                
                String url = UrlManager.URL_ON_PLAYING_PROGRAMS;
                try 
                {
                    NetDataGetter getter;
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject(pairs);
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("id", resultArray.getJSONObject(i).getString("id"));
                                map.put("title", resultArray.getJSONObject(i).getString("title"));
                                result.add(map);
                            }
                        }
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                }
                catch (MalformedURLException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
            }
        });
        return false;
    }
    
    public boolean loadOnPlayingProgramByChannel(final String channelId, final List<HashMap<String, String>> result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = UrlManager.URL_ON_PLAYING_PROGRAM + "?channel=" + channelId;
                try 
                {
                    NetDataGetter getter;
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("time", jsonRoot.getString("time"));
                        map.put("title", jsonRoot.getString("title"));
                        result.add(map);
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                }
                catch (MalformedURLException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    listener.onLoadFinish(LoadListener.FAIL);
                    e.printStackTrace();
                }
            }
        });
        return false;
    }

}