package com.tools.tvguide.data;

public class Program 
{
    public String time;
    public String title;
    public boolean equals(Program program)
    {
        assert (program != null);
        if (time.equals(program.time) && title.equals(program.title))
            return true;
        return false;
    }
}
