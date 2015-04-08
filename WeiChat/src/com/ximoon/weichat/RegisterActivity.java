package com.ximoon.weichat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ximoon.weichat.service.ThreadService;

public class RegisterActivity extends Activity {

	private EditText register_et_username;
	private EditText register_et_password;
	private EditText register_et_conpassword;

	// private EditText register_et_nickname;
	// private EditText register_et_motto;
	// private EditText register_et_phone;
	// private RadioGroup register_rg_sex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initViews();
	}

	private void initViews() {
//		this.getActionBar().hide();
		register_et_username = (EditText) findViewById(R.id.register_et_username);
		register_et_password = (EditText) findViewById(R.id.register_et_password);
		register_et_conpassword = (EditText) findViewById(R.id.register_et_conpassword);
	}

	public void back(View view) {
		Intent intent = new Intent(this,LoginActivity.class);
		startActivity(intent);
		this.finish();
	}

	public void register(View view) {
		String username = register_et_username.getText().toString();
		String password = register_et_password.getText().toString();
		String conpassword = register_et_conpassword.getText().toString();
		if (TextUtils.isEmpty(username) || TextUtils.isDigitsOnly(password) || TextUtils.isEmpty(conpassword)) {
			Toast.makeText(this, "注册信息不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!password.equals(conpassword)) {
			Toast.makeText(this, "输入的密码不一致,请重新输入", Toast.LENGTH_SHORT).show();
			return;
		}
		String msg = "register@" + username + "@" + password;
		ThreadService.thread.context = this;
		ThreadService.thread.sendMessage(msg);
	}
	
	@Override
	protected void onStop() {
		this.finish();
		super.onStop();
	}

}
