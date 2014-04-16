package com.tools.tvguide.managers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.components.Shutter;
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
    
    public boolean loadNowTimeFromNetwork(final StringBuffer result, final LoadListener listener)
    {
    	mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
            	URL url;
				try {
					url = new URL("http://m.bjtime.cn");
					URLConnection uc = url.openConnection();
					uc.setConnectTimeout(15 * 1000);
					uc.connect();
					long ld = uc.getDate(); 	// 取得网站日期时间
					result.append(ld);
					listener.onLoadFinish(LoadListener.SUCCESS);
				} catch (MalformedURLException e) {
					listener.onLoadFinish(LoadListener.FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					listener.onLoadFinish(LoadListener.FAIL);
					e.printStackTrace();
				}
            }
        });
        return false;
    }
    
    public boolean loadPopSearch(final int num, final List<String> result, final LoadListener listener)
    {
        mUpdateHandler.post(new Runnable() 
        {
            @Override
            public void run() 
            {
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.ProxyUrl.Query) + "?pop_search=" + String.valueOf(num);
                NetDataGetter getter;
                boolean success = false;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    if (jsonRoot != null)
                    {
                        result.clear();
                        JSONArray resultArray = jsonRoot.getJSONArray("pop_search");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                result.add(resultArray.getString(i));
                            }
                        }
                        
                        success = true;
                        if (success)
                            listener.onLoadFinish(LoadListener.SUCCESS);
                        else
                            listener.onLoadFinish(LoadListener.FAIL);
                    }
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

    @Override
    public void onShutDown()
    {
        // 如果当前频道的版本号大于等于最新的，则不用清除缓存以更新频道
        if (getCurrentChannelVersion() >= AppEngine.getInstance().getUpdateManager().getLatestChannelVersion())
            return;
        // 否则，需要清除缓存，以便在下次启动的时候重新拉去频道列表
        setChannelVersion(AppEngine.getInstance().getUpdateManager().getLatestChannelVersion());
        
        // 第一次启动时，频道都是最新的，所以不用再清除缓存
        if (!AppEngine.getInstance().getBootManager().isFirstStart())
        {
            AppEngine.getInstance().getCacheManager().clear();
            AppEngine.getInstance().getDiskCacheManager().clearAll();
        }
    }
    
    private int getCurrentChannelVersion()
    {
        return mPreference.getInt(KEY_CHANNEL_VERSION_FLAG, 0);
    }
    
    private void setChannelVersion(int version)
    {
        mPreference.edit().putInt(KEY_CHANNEL_VERSION_FLAG, version).commit();
    }
}
