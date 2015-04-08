package com.ximoon.weichat.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtils {
	public  static void myToast(Activity activity,String msg){
		Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
	}
}
