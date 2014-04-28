package com.tools.tvguide.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class StatManager {
    public static enum ClickModule { TabMore, TabMoreSupportUs };
    private static final String SHARE_PREFERENCES_NAME                  = "statistics";
    private static final String KEY_CLICK_TAB_MORE_TIMES                = "key_click_tab_more_times";
    private static final String KEY_CLICK_SUPPORT_US_TIMES              = "key_click_support_us_times";
    @SuppressWarnings("unused")
    private Context mContext;
    private SharedPreferences   mPreference;
    
    public StatManager(Context context) {
        assert (context != null);
        mContext = context;
        mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 点击MoreActivity的时候调用
     * @return：之前已经被点击的次数
     */
    public int clickModule(ClickModule module) {
        int times = 0;
        String flag = null;
        switch (module) {
            case TabMore:
                flag = KEY_CLICK_TAB_MORE_TIMES;
                break;
            case TabMoreSupportUs:
                flag = KEY_CLICK_SUPPORT_US_TIMES;
                break;
            default:
                break;
        }
        
        if (flag != null) {
            times = mPreference.getInt(appendVersion(flag), 0);
            mPreference.edit().putInt(appendVersion(flag), times + 1).commit();
        }
        return times;
    }
    
    public int getClickTimes(ClickModule module) {
        int times = 0;
        String flag = null;
        switch (module) {
            case TabMore:
                flag = KEY_CLICK_TAB_MORE_TIMES;
                break;
            case TabMoreSupportUs:
                flag = KEY_CLICK_SUPPORT_US_TIMES;
                break;
            default:
                break;
        }
        
        if (flag != null) {
            times = mPreference.getInt(appendVersion(flag), 0);
        }
        return times;
    }
    
    private String appendVersion(String flag) {
        return flag + "_" + AppEngine.getInstance().getUpdateManager().getCurrentVersionName();
    }
}
