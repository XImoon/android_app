package com.ximoon.weichat.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
	
	private ImageView imageView = null;
	
	public ImageLoadTask(ImageView imageView){
		this.imageView = imageView;
	}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = "http://" + ChatApplication.HOSTADDRESS +":8080/WeiChat/images/" + params[0];
			Bitmap drawable = ImageLoader.loadImage(url);// 获取网络图片
			return drawable;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}
			imageView.setImageBitmap(result);
		}

	}