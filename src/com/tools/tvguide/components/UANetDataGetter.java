package com.tools.tvguide.components;

import java.net.MalformedURLException;

import com.tools.tvguide.data.GlobalData;
import com.tools.tvguide.utils.NetDataGetter;

public class UANetDataGetter extends NetDataGetter
{
    public UANetDataGetter()
    {
        super();
        setCommonHeaders();
    }
    
    public UANetDataGetter(String url) throws MalformedURLException
    {
        super(url);
        setCommonHeaders();
    }
    
    private void setCommonHeaders()
    {
        setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        setHeader("Accept-Encoding", "gzip");
        setHeader("Accept-Language", "zh-CN, en-US");
        setHeader("Cache-Control", "max-age=0");
        setHeader("Connection", "keep-alive");
        setHeader("Upgrade-Insecure-Requests", "1");
        setHeader("User-Agent", GlobalData.ChromeUserAgent);
    }
}
