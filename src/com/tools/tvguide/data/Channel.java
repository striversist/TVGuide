package com.tools.tvguide.data;

import java.io.Serializable;

public class Channel implements Serializable 
{
    private static final long serialVersionUID = 1L;
    @Deprecated
    public String id        = "";
    public String name      = "";
    public String tvmaoId   = "";
    public String tvmaoLink = "";
    public String logoLink  = "";
}
