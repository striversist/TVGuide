package com.tools.tvguide.data;

public class ChannelDate 
{
    public String name;
    public String link;
    
    private int day;            // From 1 to 14: 表示本周一至下周日
    
    public void setDay(int day)
    {
        assert (day >=1 && day <= 14);
        this.day = day;
    }
    
    public int getDay()
    {
        return this.day;
    }
}
