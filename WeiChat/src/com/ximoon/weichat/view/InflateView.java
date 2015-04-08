package com.ximoon.weichat.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ximoon.weichat.R;

public class InflateView {

	public static View inflateMyView(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_say_me_item, null);
		return view;
	}
	/*public static View inflateSheView(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_say_she_item, null);
		return view;
	}
	public static View inflateMMView(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_say_she_mm_item, null);
		return view;
	}*/
	
}
