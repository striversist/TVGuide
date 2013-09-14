package com.tools.tvguide.components;

import java.net.MalformedURLException;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.utils.NetDataGetter;

public class DefaultNetDataGetter extends NetDataGetter 
{
    public DefaultNetDataGetter()
    {
        super();
        setCommonHeaders();
    }
    
    public DefaultNetDataGetter(String url) throws MalformedURLException
    {
        super(url);
        setCommonHeaders();
    }
    
    private void setCommonHeaders()
    {
        String guid = AppEngine.getInstance().getUpdateManager().getGUID();
        String version = AppEngine.getInstance().getUpdateManager().currentVersionName();
        setHeader("GUID", guid);
        setHeader("Version", version);
    }
}
