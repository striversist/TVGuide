package com.tools.tvguide.managers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;


public class ContentManager implements Shutter
{
    private static final String SHARE_PREFERENCES_NAME                      = "content_settings";
    private static final String KEY_CHANNEL_VERSION_FLAG                    = "key_channel_version_flag";
    private SharedPreferences   mPreference;
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
        mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
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
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_CATEGORIES) + "?type=" + type;
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
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
    
    public boolean loadChannelsByCategory(final String categoryId, final List<Channel> result, final LoadListener listener)
    {
        boolean loadFromCache = false;
        final List<HashMap<String, String>> channelMapList = new ArrayList<HashMap<String,String>>();
        loadFromCache = AppEngine.getInstance().getCacheManager().loadChannelsByCategory(categoryId, channelMapList);
        if (loadFromCache == true)
        {
            for (int i=0; i<channelMapList.size(); ++i)
            {
                Channel channel = new Channel();
                channel.id = channelMapList.get(i).get("id");
                channel.name = channelMapList.get(i).get("name");
                result.add(channel);
            }
            return true;    // sync loaded
        }
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_CHANNELS) + "?category=" + categoryId;
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray channelListArray = jsonRoot.getJSONArray("channels");
                        if (channelListArray != null)
                        {
                            for (int i=0; i<channelListArray.length(); ++i)
                            {
                                HashMap<String, String> map = new HashMap<String, String>();    // 为了存储
                                Channel channel = new Channel();                                // 返回数据
                                String id = channelListArray.getJSONObject(i).getString("id");
                                String name = channelListArray.getJSONObject(i).getString("name");
                                
                                map.put("id", id);
                                map.put("name", name);
                                channel.id = id;
                                channel.name = name;
                                
                                channelMapList.add(map);
                                result.add(channel);
                            }
                        }
                    }
                    listener.onLoadFinish(LoadListener.SUCCESS);
                    AppEngine.getInstance().getCacheManager().saveChannelsByCategory(categoryId, channelMapList);
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
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_CHOOSE) + "?channel=" + channelId + "&day=" + day;
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
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
    
    public boolean loadProgramsByChannel2(final String channelId, final int day, final List<HashMap<String, String>> programs, 
                final List<HashMap<String, String>> onPlayingProgram, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_CHOOSE) + "?channel=" + channelId + "&day=" + day + "&onplaying=1";
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
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
                                programs.add(map);
                            }
                        }
                        JSONObject jsonOnPlayingProgram = jsonRoot.getJSONObject("onplaying");
                        if (jsonOnPlayingProgram != null)
                        {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("time", jsonOnPlayingProgram.getString("time"));
                            map.put("title", jsonOnPlayingProgram.getString("title"));
                            map.put("day", jsonOnPlayingProgram.getString("day"));
                            onPlayingProgram.add(map);
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
    
    public boolean loadProgramsByChannelV3(final String channelId, final int day, final List<Program> programs, 
            final HashMap<String, Object> extra, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_CHOOSE) + "?channel=" + channelId + "&day=" + day + "&onplaying=1";
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String time = resultArray.getJSONObject(i).getString("time");
                                String title = resultArray.getJSONObject(i).getString("title");
                                
                                Program program = new Program();
                                program.time = time;
                                program.title = title;
                                programs.add(program);
                            }
                        }
                        
                        JSONObject jsonOnPlayingProgram = jsonRoot.getJSONObject("onplaying");
                        if (extra != null && jsonOnPlayingProgram != null)
                        {
                            Program program = new Program();
                            program.day = Integer.parseInt(jsonOnPlayingProgram.getString("day"));
                            program.time = jsonOnPlayingProgram.getString("time");
                            program.title = jsonOnPlayingProgram.getString("title");
                            
                            extra.put("onplaying", program);
                        }
                        
                        String days = jsonRoot.getString("days");
                        if (extra != null && days != null)
                        {
                            extra.put("days", days);
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
                
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_ON_PLAYING_PROGRAMS);
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(url);
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
                                map.put("day", resultArray.getJSONObject(i).getString("day"));
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
    
    public boolean loadOnPlayingProgramByChannel(final String channelId, final Program result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_ON_PLAYING_PROGRAM) + "?channel=" + channelId;
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        Program program = new Program();
                        program.day = Integer.parseInt(jsonRoot.getString("day"));
                        program.time = jsonRoot.getString("time");
                        program.title = jsonRoot.getString("title");
                        
                        result.copy(program);
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
    
    public boolean loadNowTimeFromProxy(final StringBuffer result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_QUERY) + "?nowtime";
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        String time = jsonRoot.getString("nowtime");
                        
                        if (result.length() > 0)
                            result.delete(0, result.length()-1);
                        
                        result.append(time);
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
    
    public void loadAllTvmaoIdFromProxy(final HashMap<String, String> result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable() 
        {
            @Override
            public void run() 
            {
                boolean success = false;
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_QUERY) + "?all_tvmao_id";
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        JSONArray idArray = jsonRoot.getJSONArray("all_tvmao_id");
                        if (idArray != null)
                        {
                            for (int i=0; i<idArray.length(); ++i)
                            {
                                String id = idArray.getJSONObject(i).getString("id");
                                String tvmaoId = idArray.getJSONObject(i).getString("tvmao_id");
                                
                                result.put(id, tvmaoId);
                            }
                            success = true;
                        }
                    }
                    
                    if (success)
                        listener.onLoadFinish(LoadListener.SUCCESS);
                    else
                        listener.onLoadFinish(LoadListener.FAIL);
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
    }
    
    public boolean loadSearchResult(final String keyword, final List<Channel> channels, final List<HashMap<String, Object>> programs, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable() 
        {
            @Override
            public void run() 
            {
                boolean success = false;
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_SEARCH) + "?keyword=" + keyword;
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    channels.clear();
                    programs.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray;
                        // Get result_channels
                        resultArray = jsonRoot.getJSONArray("result_channels");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String id = resultArray.getJSONObject(i).getString("id");
                                String name = resultArray.getJSONObject(i).getString("name");
                                
                                Channel channel = new Channel();
                                channel.id = id;
                                channel.name = name;
                                channels.add(channel);
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
                                Channel channel = new Channel();
                                channel.id = id;
                                channel.name = name;
                                
                                JSONArray programsArray = resultArray.getJSONObject(i).getJSONArray("programs");
                                if (programsArray != null)
                                {
                                    List<Program> programList = new ArrayList<Program>();
                                    for (int j=0; j<programsArray.length(); ++j)
                                    {
                                        String time = programsArray.getJSONObject(j).getString("time");
                                        String title = programsArray.getJSONObject(j).getString("title");    
                                        
                                        Program program = new Program();
                                        program.time = time;
                                        program.title = title;
                                        programList.add(program);
                                    }
                                    
                                    HashMap<String, Object> info = new HashMap<String, Object>();
                                    info.put("channel", channel);
                                    info.put("programs", programList);
                                    programs.add(info);
                                }
                            }
                        }
                        success = true;
                    }
                    
                    if (success)
                        listener.onLoadFinish(LoadListener.SUCCESS);
                    else
                        listener.onLoadFinish(LoadListener.FAIL);
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                    listener.onLoadFinish(LoadListener.FAIL);
                }
                catch (JSONException e) 
                {
                    e.printStackTrace();
                    listener.onLoadFinish(LoadListener.FAIL);
                }
            }
        });
        return false;
    }
    
    public String getTvmaoId(String channelId)
    {
        HashMap<String, String> tvmaoIdMap = new HashMap<String, String>();
        if (AppEngine.getInstance().getCacheManager().loadAllTvmaoIds(tvmaoIdMap))  // Success
            return tvmaoIdMap.get(channelId);
        return null;
    }

    @Override
    public void onShutDown()
    {
        // 如果当前频道的版本号大于等于最新的，则不用清除缓存以更新频道
        if (getCurrentChannelVersion() >= AppEngine.getInstance().getUpdateManager().getLatestChannelVersion())
            return;
        // 否则，需要清除缓存，以便在下次启动的时候重新拉去频道列表
        setChannelVersion(AppEngine.getInstance().getUpdateManager().getLatestChannelVersion());
        AppEngine.getInstance().getCacheManager().clear();
    }
    
    private int getCurrentChannelVersion()
    {
        return mPreference.getInt(KEY_CHANNEL_VERSION_FLAG, EnvironmentManager.defaultChannelVersion);
    }
    
    private void setChannelVersion(int version)
    {
        mPreference.edit().putInt(KEY_CHANNEL_VERSION_FLAG, version).commit();
    }
}
