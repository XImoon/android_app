package com.ximoon.weichat.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ximoon.weichat.R;
import com.ximoon.weichat.entity.PostInfo;

public class RelpyBaseAdapter extends BaseAdapter {
	private LayoutInflater mInflater ;
	private List<PostInfo> lists = new ArrayList<PostInfo>();
	public RelpyBaseAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	//设值
	public void setLists(List<PostInfo> lists) {
		this.lists = lists;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder=null;
		View view=null;
		if(null==convertView){
			mHolder=new ViewHolder();
			view = mInflater.inflate(R.layout.list_say_bar_me_item, null);
			mHolder.username = (TextView) view.findViewById(R.id.username);
			mHolder.messagedetail_row_text = (TextView) view.findViewById(R.id.messagedetail_row_text);
			mHolder.messagedetail_row_layer = (TextView) view.findViewById(R.id.messagedetail_row_layer);
			mHolder.messagedetail_row_date = (TextView) view.findViewById(R.id.messagedetail_row_date);
			view.setTag(mHolder);
		}else{
			view=convertView;
			mHolder=(ViewHolder) view.getTag();
		}
		//数据
		PostInfo info = lists.get(position);
		mHolder.username.setText(info.name);
		mHolder.messagedetail_row_text.setText(info.content);
		mHolder.messagedetail_row_layer.setText("第"+info.layer+"楼");
		mHolder.messagedetail_row_date.setText(info.time);
		//图片,异步
		
		
		return view;
	}
	public static class ViewHolder{
		public TextView username;
		public TextView messagedetail_row_text;
		public TextView messagedetail_row_layer;
		public TextView messagedetail_row_date;
	}

}





