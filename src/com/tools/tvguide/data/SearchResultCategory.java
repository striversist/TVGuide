package com.tools.tvguide.data;

import android.text.TextUtils;

public class SearchResultCategory 
{
    public String name;
    public String link;
    public enum Type {Channel, Tvcolumn, Drama, Movie, ProgramSchedule, Unknown};
    public Type type = Type.Unknown;
    
    public static Type getType(String name)
    {
        if (TextUtils.equals(name, "频道")) {
            return Type.Channel;
        } else if (TextUtils.equals(name, "综艺栏目")) {
            return Type.Tvcolumn;
        } else if (TextUtils.equals(name, "电视剧")) {
            return Type.Drama;
        } else if (TextUtils.equals(name, "电影")) {
            return Type.Movie;
        } else if (TextUtils.equals(name, "节目表")) {
            return Type.ProgramSchedule;
        } else {
            return Type.Unknown;
        }
    }
}
