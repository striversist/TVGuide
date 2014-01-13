package com.tools.tvguide.data;

public class Program 
{
    public String time;
    public String title;
    public String trailer;
    public String link;
    public boolean equals(Program program)
    {
        assert (program != null);
        if (time != null && time.equals(program.time) 
            && title != null && title.equals(program.title))
            return true;
        return false;
    }
    
    public boolean hasTrailer()
    {
        if (trailer != null && trailer.trim().length() > 0)
            return true;
        return false;
    }
    
    public boolean hasLink()
    {
        if (link != null && link.trim().length() > 0)
            return true;
        return false;
    }
}
