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
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ximoon.weichat.R;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.entity.SortModel;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.ImageLoader;
import com.ximoon.weichat.utils.ImageUtil;

public class SortAllAdapter extends BaseAdapter implements SectionIndexer {
	private List<SortModel> list = null;
	private Context mContext;

	public SortAllAdapter(Context mContext, List<SortModel> list) {
		this.mContext = mContext;
		this.list = list;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<SortModel> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder viewHolder = null;
		list.get(position);
		final SortModel mContent = list.get(position);
		if (convertView == null) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.lv_item_friends_all, null);
			viewHolder = new ViewHolder();
			view.setTag(viewHolder);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.all_catalog);
			viewHolder.item_tv_friends_all_icon = (ImageView) view
					.findViewById(R.id.item_tv_friends_all_icon);
			viewHolder.item_tv_friends_all_ip = (TextView) view
					.findViewById(R.id.item_tv_friends_all_ip);
			viewHolder.item_tv_friends_all_port = (TextView) view
					.findViewById(R.id.item_tv_friends_all_port);
			viewHolder.item_tv_friends_all_isonline = (TextView) view
					.findViewById(R.id.item_tv_friends_all_isonline);
		} else {
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		ImageLoadTask imageLoadTask = new ImageLoadTask();
		imageLoadTask.execute(viewHolder, "http://"
				+ ChatApplication.HOSTADDRESS + ":8080/WeiChat/images/"
				+ list.get(position).getClientInfo().headicon, position);// 执行异步任务
		ClientInfo info = list.get(position).getClientInfo();
		if (info.exname == null) {
			info.exname = "";
		}
		if (info.nickname == null) {
			info.nickname = "";
		}
		if (info.motto == null) {
			info.motto = "";
		}
		viewHolder.item_tv_friends_all_ip.setText(info.exname + "(" + info.nickname
				+ ")");
		viewHolder.item_tv_friends_all_port.setText(info.motto);
		if (info.isNew) {
			viewHolder.item_tv_friends_all_isonline.setVisibility(View.VISIBLE);
		} else {
			viewHolder.item_tv_friends_all_isonline.setVisibility(View.GONE);
		}
		return view;

	}

	private final static class ViewHolder {
		public TextView tvLetter;
		public ImageView item_tv_friends_all_icon;
		public TextView item_tv_friends_all_ip;
		public TextView item_tv_friends_all_port;
		public TextView item_tv_friends_all_isonline;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
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
			mHolder.item_tv_friends_all_icon.setImageBitmap(ImageUtil
					.getjustableBitmap(result, 50, 50));
		}

	}
}