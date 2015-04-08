package com.ximoon.weichat;

import java.net.Socket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ximoon.weichat.service.ThreadService;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.LoginUtil;

public class LoginActivity extends Activity {

	private EditText main_et_username = null;
	private EditText main_et_password = null;
	private Button main_bt_board = null;
	private Socket socket = null;
	private ThreadService thread;
	private LoginReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		init();
	}
	
	@Override
	protected void onStart() {
		IntentFilter filter = new IntentFilter("com.chat.login");
		registerReceiver(receiver, filter);
		super.onStart();
	}

	public void initViews() {
		main_et_username = (EditText) findViewById(R.id.main_et_username);
		main_et_password = (EditText) findViewById(R.id.main_et_password);
		main_bt_board = (Button) findViewById(R.id.main_bt_board);
	}

	public void init() {
		receiver = new LoginReceiver();
		 new Thread() {
			public void run() {
				try {
					if (socket == null || socket.isClosed() || !socket.isConnected()) {
						socket = new Socket(ChatApplication.HOSTADDRESS,
								ChatApplication.PORT);
						handler.sendEmptyMessage(0);
					}
				} catch (Exception e) {
					handler.sendEmptyMessage(1);
					e.printStackTrace();
				}
			}
		}.start();
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		ChatApplication.window_width = metrics.widthPixels;
		ChatApplication.window_height = metrics.heightPixels;
	}

	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				thread = new ThreadService(socket);
				thread.context = LoginActivity.this;
				main_bt_board.setClickable(true);
				break;
			case 1:
				Toast.makeText(LoginActivity.this, "网络异常,请检查网络连接",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	public void login(View view) {
		thread.context = this;
		String username = main_et_username.getText().toString();
		String password = main_et_password.getText().toString();
		if (TextUtils.isEmpty(username) || TextUtils.isDigitsOnly(password)) {
			Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String msg_login = "login@" + username + "@" + password;
		thread.sendMessage(msg_login);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(receiver);
		super.onStop();
	}

	public void register(View view) {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivityForResult(intent, 200);
	}
	
	private class LoginReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String operation = intent.getStringExtra("operation");
			if (operation.equals("succeed")) {
				intent = new Intent(LoginActivity.this,ViewPageActivity.class);
				LoginActivity.this.startActivity(intent);
				LoginActivity.this.finish();
			}else if (operation.equals("error")) {
				Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
			}else if (operation.equals("ok")) {
				Toast.makeText(getApplicationContext(), "注册成功,请登录", Toast.LENGTH_SHORT).show();
				intent = new Intent(context,LoginActivity.class);
				startActivity(intent);
			}else if (operation.equals("faile")) {
				Toast.makeText(getApplicationContext(), "注册失败,请重新注册", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
}