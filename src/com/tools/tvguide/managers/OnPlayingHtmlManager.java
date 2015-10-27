package com.tools.tvguide.managers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.TextUtils;

import com.tools.tvguide.data.Category;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.utils.CacheControl;
import com.tools.tvguide.utils.HtmlUtils;

public class OnPlayingHtmlManager 
{
    public static final String CATEGORY_ENTRY_URL = "http://m.tvmao.com/program/playing";
    private Context mContext;
    
    public OnPlayingHtmlManager(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
    public interface CategoryEntriesCallback
    {
        void onCategoryEntriesLoaded(int requestId, List<Category> categories);
    }
    
    public interface OnPlayingCallback
    {
        void onChannelsLoaded(int requestId, List<Channel> channels);
        void onProgramsLoaded(int requestId, HashMap<String, String> programs);  // programs: key(channel id), value(program name)
    }
    
    public void getCategoryEntries(final int requestId, final CategoryEntriesCallback callback)
    {
        assert (callback != null);
        new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    Document doc = HtmlUtils.getDocument(CATEGORY_ENTRY_URL, CacheControl.Disk);
                    
                    // 返回结果
                    List<Category> categoryList = new ArrayList<Category>();
                    
                    // 央视、卫视、数字
                    Category cctv = new Category();
                    cctv.name = "央视频道";
                    cctv.link = getAbsoluteUrl("/program/playing/cctv/");
                    cctv.tvmaoId = "cctv";
                    categoryList.add(cctv);
                    
                    Category satellite = new Category();
                    satellite.name = "卫视频道";
                    satellite.link = getAbsoluteUrl("/program/playing/satellite/");
                    satellite.tvmaoId = "satellite";
                    categoryList.add(satellite);
                    
                    Category digital = new Category();
                    digital.name = "数字频道";
                    digital.link = getAbsoluteUrl("/program/playing/digital/");
                    digital.tvmaoId = "digital";
                    categoryList.add(digital);
                    
                    // 其它地区
                    Elements others = doc.select("form[name=locchg] ul li a");
                    for (int i=0; i<others.size(); ++i) {
                        Element other = others.get(i);
                        String name = other.ownText();
                        String tvmaoId = other.attr("prov");
                        if (TextUtils.equals(tvmaoId, "HK")) {
                            tvmaoId = "honkong";
                        } else if (TextUtils.equals(tvmaoId, "TW")) {
                            tvmaoId = "taiwan";
                        } else if (TextUtils.equals(tvmaoId, "MO")) {
                            tvmaoId = "macau";
                        } else if (TextUtils.equals(tvmaoId, "US")) {
                            tvmaoId = "foreign";
                        }
                        String link = getAbsoluteUrl(CATEGORY_ENTRY_URL + "/" + tvmaoId);
                        
                        Category category = new Category();
                        category.name = name;
                        category.tvmaoId = tvmaoId;
                        category.link = link;
                        categoryList.add(category);
                    }
                    
                    callback.onCategoryEntriesLoaded(requestId, categoryList);
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public void getOnPlayingChannels(final int requestId, final Category category, final OnPlayingCallback callback)
    {
        assert (callback != null);
        if (category == null || category.link == null)
            return;
        
        new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    Document doc = null;
                    // 返回结果
                    List<Channel> channelList = new ArrayList<Channel>();
                    HashMap<String, String> programs = new HashMap<String, String>();
                    
                    // 尝试从缓存中加载
                    doc = getDocumentByCategory(category, CacheControl.Disk);
                    if (doc == null)
                        return;
                    getPlayingChannelPrograms(doc, channelList, programs);
                    callback.onChannelsLoaded(requestId, channelList);
                    
                    // 实时获取
                    doc = getDocumentByCategory(category, CacheControl.Never);
                    if (doc == null)
                        return;
                    getPlayingChannelPrograms(doc, channelList, programs);
                    callback.onProgramsLoaded(requestId, programs);
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private Document getDocumentByCategory(Category category, CacheControl control) throws IOException
    {
        Document doc = HtmlUtils.getDocument(category.link, control);
        return doc;
    }
    
    private void getPlayingChannelPrograms(Document doc, List<Channel> channels, HashMap<String, String> programs)
    {
        if (doc == null || channels == null || programs == null)
            return;
        
        Elements onplayingElements = doc.select("table.playing tbody tr");
        for (int i=0; i<onplayingElements.size(); ++i)
        {
            Element onplayingElement = onplayingElements.get(i);
            if (TextUtils.equals(onplayingElement.attr("class"), "trhld")) {
                // 无效信息
                continue;
            }
            Element channelElement = onplayingElement.select("td").first();
            if (channelElement != null)
            {          
                Element linkElement = channelElement.select("a").first();
                if (linkElement != null)
                {
                    Channel channel = new Channel();
                    channel.name = linkElement.ownText();
                    channel.tvmaoId = HtmlUtils.filterTvmaoId(linkElement.attr("href"));
                    channel.tvmaoLink = getAbsoluteUrl(linkElement.attr("href"));
                    channels.add(channel);
                    
                    Element timeElement = channelElement.nextElementSibling();
                    Element programElement = null;
                    String text = "";
                    if (timeElement != null) {
                        programElement = timeElement.nextElementSibling();
                        text = timeElement.text() + " ";
                    } else {
                        programElement = channelElement.nextElementSibling();
                    }
                    if (programElement != null) {
                        text += programElement.text();
                    }
                    
                    programs.put(channel.tvmaoId, text);
                }
            }
        }
    }
    
    private String getAbsoluteUrlByOptionValue(String value)
    {
        String prefix = "http://m.tvmao.com/program/playing/";
        return prefix + value;
    }
    
    private String getAbsoluteUrl(String url)
    {
        if (url == null)
            return null;
        
        try 
        {
            String protocol = new URL(CATEGORY_ENTRY_URL).getProtocol();
            String host = new URL(CATEGORY_ENTRY_URL).getHost();
            String prefix = protocol + "://" + host;
            
            if (!url.contains("http://"))
                url = prefix + url;
        } 
        catch (MalformedURLException e) 
        {
            e.printStackTrace();
        }
        
        return url;
    }
}
