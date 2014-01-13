package com.tools.tvguide.utils;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.CacheManager;

public class HtmlUtils 
{
    public static Document getDocument(String url) throws IOException
    {
        return getDocument(url, "utf-8");
    }
    
    public static Document getDocument(String url, String charset) throws IOException
    {
        CacheManager cacheManager = AppEngine.getInstance().getCacheManager();
        String html = cacheManager.get(url);
        Document doc;
        if (html == null)
        {
            NetDataGetter getter = new DefaultNetDataGetter(url);
            for (int i=0; i<2; ++i)
            {
                html = getter.getStringData(charset);
                if (html != null)
                    break;
            }
            if (html == null)
                throw new IOException("Failed to get html from network");
            doc = Jsoup.parse(html);
            cacheManager.set(url, doc.html());
        }
        else
        {
            doc = Jsoup.parse(html);
        }
        return doc;
    }
}
