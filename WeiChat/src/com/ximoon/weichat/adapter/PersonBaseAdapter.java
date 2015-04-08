package com.ximoon.weichat.adapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ximoon.weichat.R;
import com.ximoon.weichat.entity.PostInfo;
import com.ximoon.weichat.service.PersonSevice;

public class PersonBaseAdapter extends BaseAdapter {
	private int headViewCount;
	private LayoutInflater mInflater ;
	private List<PostInfo> lists = new ArrayList<PostInfo>();
	private ViewHolder mHolder;
	private View view;
	public PersonBaseAdapter(Context context,int headViewCount) {
		mInflater = LayoutInflater.from(context);
		this.headViewCount=headViewCount;
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
		return lists.get(position-headViewCount);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position-headViewCount;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		mHolder=null;
		view=null;
		if(null==convertView){
			mHolder=new ViewHolder();
			view = mInflater.inflate(R.layout.lv_item_persons, null);
			mHolder.title = (TextView) view.findViewById(R.id.title);
			mHolder.layer = (TextView) view.findViewById(R.id.layer);
			mHolder.content = (TextView) view.findViewById(R.id.content);
			mHolder.time = (TextView) view.findViewById(R.id.time);
			mHolder.name = (TextView) view.findViewById(R.id.name);
			mHolder.pic = (ImageView) view.findViewById(R.id.pic);
			view.setTag(mHolder);
		}else{
			view=convertView;
			mHolder=(ViewHolder) view.getTag();
		}
		//数据
		PostInfo info = lists.get(position);
		mHolder.title.setText(info.title);
		mHolder.layer.setText(info.layer+"个回复");
		mHolder.content.setText(info.content);
		mHolder.time.setText(info.time);
		mHolder.name.setText(info.name);
		String TitleBase64=info.pic;
		byte[] base64Bytes=Base64.decodeBase64(TitleBase64.getBytes());
		ByteArrayInputStream bais=new ByteArrayInputStream(base64Bytes);
		mHolder.pic.setImageDrawable(Drawable.createFromStream(bais, "titleimage"));
		//图片,异步
//		LoadImageAsyncTask task = new LoadImageAsyncTask();
//		Object[] params = new Object[]{info.pic,mHolder.pic};
//		task.execute(params);
		return view;
	}
	public static class ViewHolder{
		public TextView title;
		public TextView layer;
		public TextView content;
		public TextView time;
		public TextView name;
		public ImageView pic;
	}
	private class LoadImageAsyncTask extends AsyncTask<Object, Object, Object>{

		@Override
		protected Object doInBackground(Object... params) {
			String path = "http://192.168.191.1:8080/microtwitter"+(String) params[0];
			ImageView pic=(ImageView)params[1];
			Bitmap bitmap=null;
			try {
				bitmap = PersonSevice.getBitMapFromPath(path);
				publishProgress(new Object[]{bitmap,pic});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			//iv_pic 从values中来
			Bitmap bitmap = (Bitmap) values[0];
			ImageView pic = (ImageView) values[1];
			//图片
			pic.setImageBitmap(bitmap);//异步任务 
			super.onProgressUpdate(values);
		}
		
	}

}





