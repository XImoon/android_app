package com.ximoon.weichat.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ximoon.weichat.R;

public class InflaterView {
	public static View inflaterMyView(Context context){
		View view=LayoutInflater.from(context).inflate(R.layout.list_say_me_item, null);
		return view;
	}
	public static View inflaterSheView(Context context){
		View view=LayoutInflater.from(context).inflate(R.layout.list_say_she_item, null);
		return view;
	}
}
