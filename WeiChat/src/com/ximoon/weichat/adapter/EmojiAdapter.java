package com.ximoon.weichat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ximoon.weichat.R;
import com.ximoon.weichat.utils.PicService;

public class EmojiAdapter extends BaseAdapter{
	
	private LayoutInflater inflater = null;
	private int[] emojis = PicService.getPic();
	
	public EmojiAdapter(Context context){
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return emojis.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return emojis[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.gv_item_emoji, null);
			mHolder = new ViewHolder();
			mHolder.item_iv_emoji = (ImageView) convertView.findViewById(R.id.item_iv_emoji);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.item_iv_emoji.setImageResource(emojis[position]);
		return convertView;
	}
	
	private final class ViewHolder{
		ImageView item_iv_emoji;
	}

}
