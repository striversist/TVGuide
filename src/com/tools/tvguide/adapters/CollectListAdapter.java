package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.UrlManager;
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
    private Context mContext;
    private RemoveItemCallback mRemoveCallback;
    private List<HashMap<String, String>> mItemList = new ArrayList<HashMap<String, String>>();
    
    public interface RemoveItemCallback {
        public void onRemove(int position, HashMap<String, String> item);
    }
    
    public CollectListAdapter(Context context, List<HashMap<String, String>> data) {
        assert (context != null);
        assert (data != null);
        
        mContext = context;
        mItemList.addAll(data);
    }
    
    public void updateOnPlayingProgram() {
        if (mItemList == null) 
            return;
        
        for (HashMap<String, String> item : mItemList) {
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
    
    public void add(int position, HashMap<String, String> item) {
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
        final HashMap<String, String> item = mItemList.get(position);
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
        
        if (item.containsKey(KEY_NAME)) {
            holder.channelNameTextView.setText(item.get(KEY_NAME));
        }
        
        final String tvmaoId = item.get(KEY_TVMAO_ID);
        String[] logoUrls = UrlManager.guessWebChannelLogoUrls(tvmaoId);
        if (logoUrls != null) {
            holder.channelLogoNetImageView.loadImage(new ImageLoadListener() {
                @Override
                public void onImageLoaded(String url, Bitmap bitmap) {
                    UrlManager.setWebChannelLogoUrl(tvmaoId, url);
                }
            } ,logoUrls);
        }
        
        if (item.containsKey(KEY_ON_PLAYING_PROGRAM)) {
            holder.onPlayingProgramTextView.setText(item.get(KEY_ON_PLAYING_PROGRAM));
        } else {
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
