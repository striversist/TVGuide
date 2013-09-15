package com.tools.tvguide.components;

import com.tools.tvguide.R;
import com.tools.tvguide.activities.MainActivity;
import com.tools.tvguide.managers.AppEngine;

import android.content.Context;
import android.content.Intent;

public class ShortcutInstaller 
{
    private static String       TAG                         = "ShortcutInstaller";
    private static final String ACTION_INSTALL_SHORTCUT     = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String EXTRA_SHORTCUT_DUPLICATE    = "duplicate";

    private Context             mContext;

    public ShortcutInstaller(Context context)
    {
        mContext = context;
    }
    
    /**
     * 强行创建桌面图标，不会给用户任何的通知
     * 
     * @param context
     */
    public void createShortCut()
    {
        if (null != mContext)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(mContext, MainActivity.class);
            Intent shortcut = new Intent(ACTION_INSTALL_SHORTCUT);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, mContext.getString(R.string.app_name));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(mContext, R.drawable.application));
            shortcut.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);

            mContext.sendBroadcast(shortcut);
        }
    }
}
