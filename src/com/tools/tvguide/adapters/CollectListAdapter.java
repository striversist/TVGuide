package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.views.NetImageView;
import com.tools.tvguide.views.OnPlayingProgramTextView;
import com.tools.tvguide.views.NetImageView.ImageLoadListener;
import com.tools.tvguide.views.OnPlayingProgramTextView.UpdateCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


public class CollectListAdapter extends BaseAdapter implements OnClickListener {
    
    private static final String KEY_TVMAO_ID = "tvmao_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ON_PLAYING_PROGRAM = "onplaying_program";
    private static final String KEY_CHANNEL_LOGO = "image";
    private Context mContext;
    private RemoveItemCallback mRemoveCallback;
    private List<HashMap<String, Object>> mItemList = new ArrayList<HashMap<String, Object>>();
    
    public interface RemoveItemCallback {
        public void onRemove(int position, HashMap<String, Object> item);
    }
    
    public CollectListAdapter(Context context, List<HashMap<String, Object>> data) {
        assert (context != null);
        assert (data != null);
        
        mContext = context;
        mItemList.addAll(data);
    }
    
    public void update(List<HashMap<String, Object>> itemList) {
        if (itemList == null)
            return;
        
        mItemList.clear();
        mItemList.addAll(itemList);
        notifyDataSetChanged();
    }
    
    public void updateOnPlayingProgram() {
        if (mItemList == null) 
            return;
        
        for (HashMap<String, Object> item : mItemList) {
            item.remove(KEY_ON_PLAYING_PROGRAM);
        }
        notifyDataSetChanged();
    }
    
    public Object remove(int position) {
        if (position < 0 || position >= getCount())
            return null;
        
        Object removeObject = getItem(position);
        mItemList.remove(position);
        notifyDataSetChanged();
        
        return removeObject;
    }
    
    public void add(int position, HashMap<String, Object> item) {
        if (position < 0 || position > getCount() || item == null)
            return;
        
        mItemList.add(position, item);
        notifyDataSetChanged();
    }
    
    public void setOnRemoveListener(RemoveItemCallback callback) {
        if (callback == null)
            return;
        mRemoveCallback = callback;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final HashMap<String, Object> item = mItemList.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.collect_list_item, parent, false);
            TextView channelNameTextView = (TextView) convertView.findViewById(R.id.channel_name_tv);
            NetImageView channelLogoNetImageView = (NetImageView) convertView.findViewById(R.id.channel_logo_niv);
            OnPlayingProgramTextView onplayingProgramTextView = (OnPlayingProgramTextView) convertView.findViewById(R.id.on_playing_program_tv);
            
            holder = new ViewHolder();
            holder.channelNameTextView = channelNameTextView;
            holder.channelLogoNetImageView = channelLogoNetImageView;
            holder.onPlayingProgramTextView = onplayingProgramTextView;
            holder.rmButton = (Button) convertView.findViewById(R.id.del_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.channelLogoNetImageView.setImageBitmap(null);
        }
        
        final String tvmaoId = (String)item.get(KEY_TVMAO_ID);
        
        if (item.containsKey(KEY_NAME)) {
            holder.channelNameTextView.setText((String)item.get(KEY_NAME));
        }
        
        if (item.containsKey(KEY_CHANNEL_LOGO)) {
            holder.channelLogoNetImageView.setImageBitmap((Bitmap)item.get(KEY_CHANNEL_LOGO));
        } else {
            String[] logoUrls = UrlManager.guessWebChannelLogoUrls(tvmaoId);
            if (logoUrls != null) {
                boolean found = false;
                for (String logoUrl : logoUrls) {
                    Bitmap bitmap = AppEngine.getInstance().getDiskCacheManager().getBitmap(Utility.guessFileNameByUrl(logoUrl));
                    if (bitmap != null) {   // 本地存在
                        holder.channelLogoNetImageView.setImageBitmap(bitmap);
                        item.put(tvmaoId, bitmap);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    holder.channelLogoNetImageView.loadImage(new ImageLoadListener() {
                        @Override
                        public void onImageLoaded(String url, Bitmap bitmap) {
                            UrlManager.setWebChannelLogoUrl(tvmaoId, url);
                            item.put(tvmaoId, bitmap);
                        }
                    } ,logoUrls);
                }
            }
        }
        
        if (item.containsKey(KEY_ON_PLAYING_PROGRAM)) {
            holder.onPlayingProgramTextView.setText((String)item.get(KEY_ON_PLAYING_PROGRAM));
        } else {
            holder.onPlayingProgramTextView.setText("");
            holder.onPlayingProgramTextView.update(tvmaoId, new UpdateCallback() {
                @Override
                public void onUpdate(TextView textView, final String text) {
                    if (textView == null)
                        return;
                    textView.post(new Runnable() {  
                        @Override
                        public void run() {
                            item.put(KEY_ON_PLAYING_PROGRAM, text);
                        }
                    });
                }
            });
        }
        
        holder.rmButton.setText(mContext.getResources().getString(R.string.delete));
        holder.rmButton.setTag(Integer.valueOf(position));
        holder.rmButton.setOnClickListener(this);
        
        return convertView;
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        if (mRemoveCallback != null) {
            mRemoveCallback.onRemove(position, mItemList.remove(position));
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView channelNameTextView;
        OnPlayingProgramTextView onPlayingProgramTextView;
        NetImageView channelLogoNetImageView;
        Button rmButton;
    }
}
