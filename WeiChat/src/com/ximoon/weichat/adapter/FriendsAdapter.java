package com.ximoon.weichat.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ximoon.weichat.R;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.ImageLoader;
import com.ximoon.weichat.utils.ImageUtil;

public class FriendsAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<ClientInfo> infos = null;

	public FriendsAdapter(Context context, List<ClientInfo> infos) {
		super();
		this.inflater = LayoutInflater.from(context);
		this.infos = infos;
	}

	public void setList(List<ClientInfo> infos) {
		this.infos = infos;
	}

	@Override
	public int getCount() {
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		return infos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ClientInfo info = infos.get(position);
		View view = null;
		ViewHolder viewHolder = null;
		if (convertView == null) {
			view = inflater.inflate(R.layout.lv_item_friends, null);
			viewHolder = new ViewHolder();
			view.setTag(viewHolder);
			viewHolder.item_tv_friends_icon = (ImageView) view
					.findViewById(R.id.item_tv_friends_icon);
			viewHolder.item_tv_friends_ip = (TextView) view
					.findViewById(R.id.item_tv_friends_ip);
			viewHolder.item_tv_friends_port = (TextView) view
					.findViewById(R.id.item_tv_friends_port);
			viewHolder.item_tv_friends_new = (TextView) view
					.findViewById(R.id.item_tv_friends_new);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		ImageLoadTask imageLoadTask = new ImageLoadTask();
		imageLoadTask.execute(
				viewHolder,
				"http://"+ChatApplication.HOSTADDRESS+":8080/WeiChat/images/"
						+ infos.get(position).headicon, position);// 执行异步任务
		if (info.exname == null) {
			info.exname = "";
		}
		if (info.nickname == null) {
			info.nickname = "";
		}
		if (info.motto == null) {
			info.motto = "";
		}
		viewHolder.item_tv_friends_ip.setText(info.exname + "(" + info.nickname + ")");
		viewHolder.item_tv_friends_port.setText(info.motto);
		if (info.isNew) {
			viewHolder.item_tv_friends_new.setVisibility(View.VISIBLE);
		} else {
			viewHolder.item_tv_friends_new.setVisibility(View.GONE);
		}
		return view;
	}

	private static class ViewHolder {
		public ImageView item_tv_friends_icon;
		public TextView item_tv_friends_ip;
		public TextView item_tv_friends_port;
		public TextView item_tv_friends_new;
	}

	class ImageLoadTask extends AsyncTask<Object, Void, Bitmap> {

		ViewHolder mHolder;
		int position;

		@Override
		protected Bitmap doInBackground(Object... params) {
			mHolder = (ViewHolder) params[0];
			String url = (String) params[1];
			position = (Integer) params[2];
			Bitmap drawable = ImageLoader.loadImage(url);// 获取网络图片
			return drawable;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}
			mHolder.item_tv_friends_icon.setImageBitmap(ImageUtil
					.getjustableBitmap(result, 50, 50));
		}

	}

}
