package com.tools.tvguide.data;

import java.util.List;

public class Category 
{
    public String name = "";
    public String link = "";
    
    public enum Next { CategoryList, ChannelList, None };
    public Next next = Next.None;
    public List<Category> categoryList;
    public List<Channel> channelList;
}
