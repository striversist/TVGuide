package com.tools.tvguide.managers;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.managers.ContentManager.LoadListener;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class DnsManager 
{
    private Context mContext;
    private Handler mUpdateHandler;
    
    public DnsManager(Context context)
    {
        mContext = context;
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    public String getIPAddress(String hostName) throws UnknownHostException
    {
        InetAddress addr = null;
        String ipAddress = getIPFrom_chinaz(hostName);
        if (ipAddress != null)
        {
            return ipAddress;
        }
        
        addr = InetAddress.getByName (hostName);
        ipAddress = addr.getHostAddress();
        return ipAddress;
    }
    
    private String getIPFrom_chinaz(final String hostName)
    {
        String ipAddress = null;
        String url = UrlManager.URL_CHINAZ_IP + "?IP=" + hostName;
        NetDataGetter getter;
        try 
        {
            getter = new NetDataGetter(url);
            String html = getter.getStringData();
            Pattern resultPattern = Pattern.compile("²éÑ¯½á¹û\\[1\\](.+)</");
            Matcher resultMatcher = resultPattern.matcher(html);
            
            if (resultMatcher.find())
            {
                String result = resultMatcher.group();
                Pattern ipPattern = Pattern.compile("((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|[1-9])");
                Matcher ipMatcher = ipPattern.matcher(result);
                if (ipMatcher.find()) 
                {
                    ipAddress = ipMatcher.group();
                }
            }
        }
        catch (MalformedURLException e) 
        {
            e.printStackTrace();
        }
        return ipAddress;
    }
}
