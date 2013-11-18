package com.tools.tvguide.adapters;

import java.util.List;
import java.util.Map;

import com.tools.tvguide.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class ChannellistAdapter extends SimpleAdapter 
{
    public ChannellistAdapter(Context context, List<? extends Map<String, ?>> data) 
    {
        super(context, data, R.layout.channellist_item, new String[]{"image", "name", "program"}, new int[]{R.id.itemImage, R.id.itemChannel, R.id.itemProgram});
        setViewBinder(new MyViewBinder());
    }

    class MyViewBinder implements ViewBinder
    {
        public boolean setViewValue(View view, Object data,
                String textRepresentation)
        {
            if((view instanceof ImageView) && (data instanceof Bitmap))
            {
                ImageView iv = (ImageView)view;
                Bitmap bm = (Bitmap)data;
                iv.setImageBitmap(bm);
                return true;
            }
            return false;
        }
    }
}
