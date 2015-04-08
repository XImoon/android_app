package com.ximoon.weichat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LoginUtil {
	
	public static SharedPreferences sp ;
	public static Editor editor;
	
	public void record(Context context,String username,String password,boolean islogin){
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		editor = sp.edit();
		editor.putBoolean("islogin", islogin);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
	
	public void setState(Context context,boolean islogin){
		editor = sp.edit();
		editor.putBoolean("islogin", islogin);
		editor.commit();
	}
	
	public boolean login(Context context){
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		return sp.getBoolean("islogin", false);
	}

}
