package com.ximoon.weichat.utils;

import java.util.ArrayList;

import android.app.Application;

import com.ximoon.weichat.entity.ClientInfo;

public class ChatApplication extends Application {
	
	public static String HOSTADDRESS = "192.168.191.1";
	public static int PORT = 8282;
	public static int TIMEOUT = 600000;
	public static ClientInfo info = null;
	public static ArrayList<ClientInfo> friends = new ArrayList<ClientInfo>();
	public static ArrayList<ClientInfo> friends_all = new ArrayList<ClientInfo>();
	public static int window_width = 0;
	public static int window_height = 0;

}