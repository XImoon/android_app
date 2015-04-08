package com.ximoon.weichat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.ximoon.weichat.R;
import com.ximoon.weichat.ViewPageActivity;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.entity.RecordMsgInfo;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.LoginUtil;

public class ThreadService extends Thread {

	private Socket socket = null;
	private PrintWriter writer = null;
	private BufferedReader reader = null;
	public Context context = null;
	public static ThreadService thread = null;

	public ThreadService(Socket s) {
		try {
			socket = s;
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			start();
			thread = this;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ThreadService getDefaul(Context context) {
		if (thread != null) {
			thread.context = context;
		}
		return thread;
	}

	public String getIp() {
		return socket.getInetAddress().getHostAddress();
	}

	public int getPort() {
		return socket.getPort();
	}

	public boolean isConnection() {
		return socket.isConnected() && !socket.isClosed();
	}

	public void sendMessage(String msg) {
		writer.println(msg);
		writer.flush();
	}

	@Override
	public void run() {
		super.run();
		while (socket.isConnected() && !socket.isClosed()) {
			String message = null;
			try {
				message = reader.readLine();
				if (message != null) {
					String msg[] = message.split("@");
					if (msg[0].equals("succeed") || msg[0].equals("ok")
							|| msg[0].equals("faile") || msg[0].equals("error")) {
						Intent intent_login = new Intent("com.chat.login");
						if (msg[0].equals("succeed")) {
							// 登陆成功
							ChatApplication.info = new ClientInfo(getIp(),
									getPort(), Integer.parseInt(msg[1]),
									msg[2], msg[3], msg[4], msg[5], false);
							ChatApplication.info.birth = msg[6];
							ChatApplication.info.phone = msg[7];
							ChatApplication.info.sex = msg[8];
							intent_login.putExtra("operation", "succeed");
						} else if (msg[0].equals("ok")) {
							// 注册成功
							intent_login.putExtra("operation", "ok");
						} else if (msg[0].equals("faile")) {
							// 注册失败
							intent_login.putExtra("operation", "faile");
						} else if (msg[0].equals("error")) {
							// 登陆失败
							intent_login.putExtra("operation", "error");
						}
						context.sendBroadcast(intent_login);
					} else {
						Intent intent = new Intent("com.weichat.online");// com.weichat.online
						String operation = "operation";
						if (msg[0].equals("online")) {
							operation = "online";
							// 发送在线好友
							ClientInfo info = new ClientInfo(msg[1], // ip
									Integer.parseInt(msg[2]), // port
									Integer.parseInt(msg[3]), // _id
									msg[4], // username
									msg[5], // nickname
									msg[6], // mottoo
									msg[7], // headicon
									false);
							info.exname = msg[8];
							info.isonline = true;
							ChatApplication.friends.add(info);
							for (int i = 0; i < ChatApplication.friends.size() - 1; i++) {
								if (ChatApplication.friends.get(i)._id == info._id
										&& i != ChatApplication.friends.size()) {
									ChatApplication.friends.set(i, info);
									ChatApplication.friends
											.remove(ChatApplication.friends
													.size() - 1);
								}
							}
							ChatApplication.friends_all.add(info);
							for (int i = 0; i < ChatApplication.friends_all
									.size() - 1; i++) {
								if (ChatApplication.friends_all.get(i)._id == info._id) {
									ChatApplication.friends_all.set(i, info);
									ChatApplication.friends_all
											.remove(ChatApplication.friends_all
													.size() - 1);
								}
							}
						} else if (msg[0].equals("exit")) {
							operation = "offline";
							// 发送离线好友
							int _id = Integer.parseInt(msg[1]);
							// 读写数据库操作
							for (ClientInfo info : ChatApplication.friends) {
								if (_id == info._id) {
									ChatApplication.friends.remove(info);
								}
							}
							int i = 0;
							for (ClientInfo clientInfo : ChatApplication.friends_all) {
								i++;
								if (clientInfo._id == _id) {
									clientInfo.isonline = false;
									ChatApplication.friends_all.set(i - 1,
											clientInfo);
									break;
								}
							}
						} else if (msg[0].equals("chat")) {
							operation = "chat";
							int _id = Integer.parseInt(msg[2]);
							RecordMsgInfo msgInfo = new RecordMsgInfo();
							msgInfo.emoji = -1;
							msgInfo.flag = _id + "@" + ChatApplication.info._id;
							String choose = msg[3];
							int j = 0;
							ClientInfo friendInfo = null;
							for (ClientInfo friend : ChatApplication.friends) {
								if (_id == friend._id) {
									friend.isNew = true;
									friendInfo = friend;
									ChatApplication.friends.set(j, friend);
									break;
								}
								j++;
							}
							// 聊天
							if ("text".equals(choose)) {
								msgInfo.msg = "";
								for (int i = 4; i < msg.length; i++) {
									msgInfo.msg = msgInfo.msg + msg[i] + "@";
								}
								msgInfo.msg = msgInfo.msg.substring(0,
										msgInfo.msg.length() - 1);
							} else if ("img".equals(choose)) {
								msgInfo.img = "";
								for (int i = 4; i < msg.length; i++) {
									msgInfo.img = msgInfo.img + msg[i] + "@";
								}
								msgInfo.img = msgInfo.img.substring(0,
										msgInfo.img.length() - 1);
							} else if ("voice".equals(choose)) {
								msgInfo.voice = "";
								for (int i = 4; i < msg.length; i++) {
									msgInfo.voice = msgInfo.voice + msg[i]
											+ "@";
								}
								msgInfo.voice = msgInfo.voice.substring(0,
										msgInfo.voice.length() - 1);
							} else if ("emoji".equals(choose)) {
								msgInfo.emoji = Integer.parseInt(msg[4]);
							} else if ("file".equals(choose)) {
								String name = "";
								for (int i = 4; i < msg.length; i++) {
									name = name + msg[i]
											+ "@";
								}
								name = name.substring(0,name.length() - 1);
								intent.putExtra("filename", name);
								operation = "file";
							}
							intent.putExtra("friend", friendInfo);
							intent.putExtra("record", msgInfo);
							intent.putExtra("location", j);
						}
						intent.putExtra("operation", operation);
						context.sendBroadcast(intent);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}