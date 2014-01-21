package com.tools.tvguide.managers;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tools.tvguide.components.UANetDataGetter;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.Utility;

import android.content.Context;

public class DnsManager 
{
    private Context mContext;
    private String  mDeviceIpAddress;
    private String  mDeviceLocation;
    
    /** DNS缓冲总数量  **/
    private static final int     DNS_CAPACITY    = 200;
    
    /** DNS内存缓存,减少查询数据库的次数 **/
    private Hashtable<String,String> mDnsCacheTable = new Hashtable<String,String>(DNS_CAPACITY);
    
    public DnsManager(Context context)
    {
        mContext = context;
    }
    
    public void preloadFrequentlyUsedDns()
    {
        List<String> hostList = new ArrayList<String>();
        hostList.add("m.tvsou.com");
        
        for (int i=0; i<hostList.size(); ++i)
        {
            if (mDnsCacheTable.contains(hostList.get(i)))
                continue;
            
            try 
            {
                String ipAddress = getIpAddress(hostList.get(i));
                if (ipAddress != null)
                    mDnsCacheTable.put(hostList.get(i), ipAddress);
            } 
            catch (UnknownHostException e) 
            {
                continue;
            }
        }
    }
    
    public String getProxyIPAddress(String hostName) throws UnknownHostException
    {
        if (hostName == null)
            return null;
        
        if (mDnsCacheTable.containsKey(hostName))
            return mDnsCacheTable.get(hostName);
        
        String ipAddress;
        ipAddress= getIPFrom_chinaz(hostName);
        if (ipAddress != null)
        {
            mDnsCacheTable.put(hostName, ipAddress);
            return ipAddress;
        }
        
        ipAddress= getIPFrom_IPCN(hostName);
        if (ipAddress != null)
        {
            mDnsCacheTable.put(hostName, ipAddress);
            return ipAddress;
        }
        
        InetAddress addr = InetAddress.getByName(hostName);
        ipAddress = addr.getHostAddress();
        mDnsCacheTable.put(hostName, ipAddress);
        return ipAddress;
    }
    
    public String getIpAddress(String hostName) throws UnknownHostException
    {
        if (hostName == null)
            return null;
        
        if (mDnsCacheTable.containsKey(hostName))
            return mDnsCacheTable.get(hostName);
        
        InetAddress addr = InetAddress.getByName(hostName);
        String ipAddress = addr.getHostAddress();
        mDnsCacheTable.put(hostName, ipAddress);
        return ipAddress;
    }
    
    public String getDeviceIpAddress()
    {
        return mDeviceIpAddress;
    }
    
    public String getDeviceLocation()
    {
        return mDeviceLocation;
    }
       
    private String getIPFrom_chinaz(final String hostName)
    {
        String ipAddress = null;
        String url = UrlManager.URL_CHINAZ_IP + "?IP=" + hostName;
        NetDataGetter getter;
        try 
        {
            getter = new UANetDataGetter(url);
            String html = getter.getStringData();
            if (html == null)
                return null;

            // 匹配HostName对应的IP地址
            Pattern resultPattern = Pattern.compile("查询结果\\[1\\](.+)</");       // (.+)为贪婪匹配
            Matcher resultMatcher = resultPattern.matcher(html);
            if (resultMatcher.find())
            {
                String result = resultMatcher.group();
                ipAddress = getMatchedIP(result);
            }
            
            // 匹配当前设备的IP地址
            Pattern userIpPattern = Pattern.compile("您的IP(.*?)</");             // (.*?) 为非贪婪匹配
            Matcher userIpMatcher = userIpPattern.matcher(html);
            if (userIpMatcher.find())
            {
                String result = userIpMatcher.group();
                mDeviceIpAddress = getMatchedIP(result);
            }
            
            // 匹配当前设备所在的地理位置
            Pattern userLocatioPattern = Pattern.compile("来自(.*?)</");          // (.*?) 为非贪婪匹配
            Matcher userLocationMatcher = userLocatioPattern.matcher(html);
            if (userLocationMatcher.find())
            {
                String result = userLocationMatcher.group(1);
                mDeviceLocation = getMatchedChinese(result);
            }
        }
        catch (MalformedURLException e) 
        {
            e.printStackTrace();
        }
        
        if (!Utility.isIPAddress(ipAddress))
            return null;
        return ipAddress;
    }
    
    private String getIPFrom_IPCN(final String hostName)
    {
        String ipAddress = null;
        String url = UrlManager.URL_IPCN + "/getip.php?action=queryip&ip_url=" + hostName + "&from=web";
        NetDataGetter getter;
        try
        {
            getter = new UANetDataGetter(url);
            String html = getter.getStringData();
            if (html == null)
                return null;

            // 匹配HostName对应的IP地址
            Pattern resultPattern = Pattern.compile("查询的 IP：(.+)");             // (.+)为贪婪匹配
            Matcher resultMatcher = resultPattern.matcher(html);
            if (resultMatcher.find())
            {
                String result = resultMatcher.group();
                ipAddress = getMatchedIP(result);
            }
            
            // 获取当前位置信息
            url = UrlManager.URL_IPCN + "/getip.php?action=getip&ip_url=&from=web";
            getter = new UANetDataGetter(url);
            html = getter.getStringData();
            if (html != null)
            {
                // 匹配当前设备的IP地址
                Pattern userIpPattern = Pattern.compile("当前(.*?)</");               // (.*?) 为非贪婪匹配
                Matcher userIpMatcher = userIpPattern.matcher(html);
                if (userIpMatcher.find())
                {
                    String result = userIpMatcher.group();
                    mDeviceIpAddress = getMatchedIP(result);
                }
                
                // 匹配当前设备所在的地理位置
                Pattern userLocatioPattern = Pattern.compile("来自(.*?)</");          // (.*?) 为非贪婪匹配
                Matcher userLocationMatcher = userLocatioPattern.matcher(html);
                if (userLocationMatcher.find())
                {
                    String result = userLocationMatcher.group(1);
                    mDeviceLocation = getMatchedChinese(result);
                }
            }
        }
        catch (MalformedURLException e) 
        {
            e.printStackTrace();
        }
        
        if (!Utility.isIPAddress(ipAddress))
            return null;
        return ipAddress;
    }
    
    /*
     * 用正则表达式匹配IP地址
     */
    private String getMatchedIP(String text)
    {
        String ip = "";
        Pattern ipPattern = Pattern.compile("((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|[1-9])");
        Matcher ipMatcher = ipPattern.matcher(text);
        if (ipMatcher.find()) 
        {
            ip = ipMatcher.group();
        }
        return ip;
    }
    
    private String getMatchedChinese(String text)
    {
        String chinese = "";
        Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5].+");
        Matcher chineseMatcher = chinesePattern.matcher(text);
        if (chineseMatcher.find())
        {
            chinese = chineseMatcher.group();
        }
        return chinese;
    }
}
