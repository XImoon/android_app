package com.ximoon.weichat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.ImageLoadTask;

public class InformationActivity extends Activity {

	private ClientInfo info = null;
	private int location = -1;
	private EditText message_et_nickname = null;
	private String exname = null;
	private TextView message_tv_username_show = null;
	private TextView message_tv_sex_show = null;
	private TextView message_tv_age_show = null;
	private TextView message_tv_sign_show = null;
	private ImageView message_iv_icon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		initView();
		init();
	}
	
	@Override
	protected void onStart() {
		new ImageLoadTask(message_iv_icon).execute(info.headicon);
		super.onStart();
	}

	public void initView() {
		message_et_nickname = (EditText) findViewById(R.id.message_et_nickname);
		message_tv_username_show = (TextView) findViewById(R.id.message_tv_username_show);
		message_tv_sex_show = (TextView) findViewById(R.id.message_tv_sex_show);
		message_tv_age_show = (TextView) findViewById(R.id.message_tv_age_show);
		message_tv_sign_show = (TextView) findViewById(R.id.message_tv_sign_show);
		message_iv_icon = (ImageView) findViewById(R.id.message_iv_icon);
	}

	public void init() {
		info = getIntent().getParcelableExtra("user");
		location = getIntent().getIntExtra("location", -1);
		message_tv_username_show.setText(info.username);
		message_et_nickname.setText(info.exname);
		message_tv_age_show.setText("保密");
		message_tv_sex_show.setText("保密");
		message_tv_sign_show.setText(info.motto);
	}

	public void back(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("user", info);
		intent.putExtra("location", getIntent().getIntExtra("location", -1));
		startActivity(intent);
		this.finish();
	}
	
	public void save_data(View view){
		exname = message_et_nickname.getText().toString();
		if (TextUtils.isEmpty(exname)) {
			return;
		}
		SaveNickName asyncTask = new SaveNickName();
		asyncTask.execute(info,exname);
	}
	
	private class SaveNickName extends AsyncTask<Object, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Object... params) {
			ClientInfo info = (ClientInfo) params[0];
			String exname = (String)params[1];
			HttpClient client = (HttpClient) new DefaultHttpClient();
			HttpPost request = new HttpPost("http://"
					+ ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/ChangeNicknameServlet");
			request.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			BasicNameValuePair fir_Pair = new BasicNameValuePair("first_id",
					ChatApplication.info._id + "");
			BasicNameValuePair sec_Pair = new BasicNameValuePair("second_id",
					info._id + "");
			BasicNameValuePair thi_Pair = new BasicNameValuePair("second_remark",
					exname);
			parameters.add(fir_Pair);
			parameters.add(sec_Pair);
			parameters.add(thi_Pair);
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						parameters, "UTF-8");
				request.setEntity(entity);
				HttpResponse response = client.execute(request);
				int code = response.getStatusLine().getStatusCode();
				if (code == 200) {
					InputStream is = response.getEntity().getContent();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(is));
					StringBuffer result = new StringBuffer();
					String record = in.readLine();
					while (record != null) {
						result.append(record);
						record = in.readLine();
					}
					return "ok".equals(result.toString().trim());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				info.exname = exname;
				ChatApplication.friends.set(location, info);
				Intent intent = new Intent(InformationActivity.this,ChatActivity.class);
				intent.putExtra("user", info);
				startActivity(intent);
				InformationActivity.this.finish();
			}else
				Toast.makeText(InformationActivity.this, "网络异常,请重新连接", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}
	
}