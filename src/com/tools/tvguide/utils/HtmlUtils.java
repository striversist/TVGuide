package com.tools.tvguide.utils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tools.tvguide.components.UANetDataGetter;
import com.tools.tvguide.managers.AppEngine;

public class HtmlUtils 
{
    public static Document getDocument(String url) throws IOException
    {
        return getDocument(url, "utf-8", null, true);
    }
    
    public static Document getDocument(String url, String charset) throws IOException
    {
        return getDocument(url, charset, null, true);
    }
    
    public static Document getDocument(String url, String charset, List<BasicNameValuePair> pairs) throws IOException
    {
        return getDocument(url, charset, pairs, true);
    }
    
    public static Document getDocument(String url, String charset, List<BasicNameValuePair> pairs, boolean cacheable) throws IOException
    {
        String html = null;
        Document doc = null;
        if (cacheable)
        {
            html = AppEngine.getInstance().getCacheManager().getHtml(url + getExtraKey(pairs));
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
            
            if (cacheable)
            {
                AppEngine.getInstance().getCacheManager().setHtml(url + getExtraKey(pairs), doc.html());
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
}
