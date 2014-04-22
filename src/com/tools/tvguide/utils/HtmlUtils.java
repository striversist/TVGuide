package com.tools.tvguide.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.text.TextUtils;

import com.tools.tvguide.components.UANetDataGetter;
import com.tools.tvguide.data.TimestampString;
import com.tools.tvguide.managers.AppEngine;

public class HtmlUtils 
{
    public static Document getDocument(String url, CacheControl control) throws IOException
    {
        return getDocument(url, "utf-8", null, control);
    }
    
    public static Document getDocument(String url, String charset, CacheControl control) throws IOException
    {
        return getDocument(url, charset, null, control);
    }
    
    public static Document getDocument(String url, String charset, List<BasicNameValuePair> pairs, CacheControl control) throws IOException
    {
        String html = null;
        Document doc = null;
        String key = url + getExtraKey(pairs);
        if (control == CacheControl.Memory) {
            html = AppEngine.getInstance().getCacheManager().getHtml(key);
        } else if (control == CacheControl.Disk) {
            html = AppEngine.getInstance().getDiskCacheManager().getString(key);
        } else if (control == CacheControl.DiskToday) {
        	TimestampString tString = AppEngine.getInstance().getDiskCacheManager().getTimestampString(key);
        	if (!isExpired(tString.getDate())) {
        		html = tString.getString();
        	}
        }
        
        if (html == null)
        {
            NetDataGetter getter = new UANetDataGetter(url);
            for (int i=0; i<2; ++i)
            {
                if (pairs != null) {
                    html = getter.getStringData(pairs, charset);
                } else {
                    html = getter.getStringData(charset);
                }
                if (html != null)
                    break;
            }
            if (html == null)
                throw new IOException("Failed to get html from network");
            doc = Jsoup.parse(html);
            
            if (control == CacheControl.Memory) {
                AppEngine.getInstance().getCacheManager().setHtml(key, html);
            } else if (control == CacheControl.Disk) {
                AppEngine.getInstance().getDiskCacheManager().setString(key, html);
            } else if (control == CacheControl.DiskToday) {
            	TimestampString tString = new TimestampString(System.currentTimeMillis(), html);
            	AppEngine.getInstance().getDiskCacheManager().setTimestampString(key, tString);
            }
        }
        else
        {
            doc = Jsoup.parse(html);
        }
        return doc;
    }
    
    private static String getExtraKey(List<BasicNameValuePair> pairs)
    {
        if (pairs == null)
            return "";
        
        return "_" + String.valueOf(pairs.toString().hashCode());
    }
    
    public static String omitHtmlElement(String html)
    {
        String regEx = "<.+?>";
        Pattern p = Pattern.compile(regEx);    
        Matcher m=p.matcher(html);  
        String result = m.replaceAll("");
        
        return result;
    }
    
    public static String filterTvmaoId(String url)
    {
        if (url == null || url.trim().length() == 0)
            return "";
        
        String id = "";
        Pattern resultPattern = Pattern.compile("program/(.+)");
        Matcher resultMatcher = resultPattern.matcher(url);
        if (resultMatcher.find())
        {
            String result = resultMatcher.group(1);
            id = result.replace('/', '-');
        }
        if (id.contains("-w"))  // 包含星期，需要过滤
        {
            Pattern weekPattern = Pattern.compile("(.*?)-w.+");
            Matcher weekMatcher = weekPattern.matcher(id);
            if (weekMatcher.find())
            {
                id = weekMatcher.group(1);  
            }
        }
        
        return id;
    }
    
    private static boolean isExpired(long date) {
		return isTheSameDay(date, System.currentTimeMillis());
	}
    
    private static boolean isTheSameDay(long date1, long date2) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		String date1String = sf.format(new Date(date1));
		String date2String = sf.format(new Date(date2));
		
		return TextUtils.equals(date1String, date2String);
	}
}
