package com.ximoon.weichat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.ImageLoadTask;

public class MyMessageActivity extends Activity {
	
	private ImageView setmessage_iv_icon = null;
	private EditText setmessage_et_nickname = null;
	private EditText setmessage_et_phone_show = null;
	private EditText setmessage_et_sign_show = null;
	private TextView setmessage_tv_username_show = null;
	private TextView setmessage_tv_age_show = null;
	private RadioGroup setmessage_rg_sex = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mysetting_message);
		initViews();
		init();
	}
	
	public void initViews(){
		setmessage_iv_icon = (ImageView) findViewById(R.id.setmessage_iv_icon);
		setmessage_et_nickname = (EditText) findViewById(R.id.setmessage_et_nickname);
		setmessage_et_phone_show = (EditText) findViewById(R.id.setmessage_et_phone_show);
		setmessage_et_sign_show = (EditText) findViewById(R.id.setmessage_et_sign_show);
		setmessage_tv_username_show = (TextView) findViewById(R.id.setmessage_tv_username_show);
		setmessage_tv_age_show = (TextView) findViewById(R.id.setmessage_tv_age_show);
		setmessage_rg_sex = (RadioGroup) findViewById(R.id.setmessage_rg_sex);
	}
	
	public void init(){
		setmessage_tv_username_show.setText(ChatApplication.info.username);
		setmessage_et_nickname.setText(ChatApplication.info.nickname);
		setmessage_et_phone_show.setText(ChatApplication.info.phone);
		setmessage_et_sign_show.setText(ChatApplication.info.motto);
		int age = Integer.parseInt((String) new DateFormat().format("yyyy", new Date())) - Integer.parseInt(ChatApplication.info.birth)/10000;
		setmessage_tv_age_show.setText(age +"岁");
		new ImageLoadTask(setmessage_iv_icon).execute(ChatApplication.info.headicon);
	}
	
	public void cancelSetMessage(View view){
		Intent intent = new Intent(this,ViewPageActivity.class);
		startActivity(intent);
	}
	
	public void completeSetMessage(View view){
		String nickname = setmessage_et_nickname.getText().toString();
		String phone = setmessage_et_phone_show.getText().toString();
		String motto = setmessage_et_sign_show.getText().toString();
		String sex = "男";
		int id = setmessage_rg_sex.getCheckedRadioButtonId();
		if (id == R.id.setmessage_rb_girl) {
			sex = "女";
		}
		ClientInfo clientInfo = ChatApplication.info;
		clientInfo.nickname = nickname;
		clientInfo.phone = phone;
		clientInfo.motto = motto;
		clientInfo.sex = sex;
		new SaveMessageInformation().execute(clientInfo);
	}
	
	private class SaveMessageInformation extends AsyncTask<ClientInfo, Void, ClientInfo>{

		@Override
		protected ClientInfo doInBackground(ClientInfo... params) {
			ClientInfo info =  params[0];
			HttpClient client = (HttpClient) new DefaultHttpClient();
			HttpPost request = new HttpPost("http://"
					+ ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/SetMessageServlet");
			request.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			BasicNameValuePair fir_Pair = new BasicNameValuePair("_id", info._id+"");
			BasicNameValuePair sec_Pair = new BasicNameValuePair("nickname", info.nickname);
			BasicNameValuePair thir_Pair = new BasicNameValuePair("phone", info.phone);
			BasicNameValuePair four_Pair = new BasicNameValuePair("motto", info.motto);
			BasicNameValuePair five_Pair = new BasicNameValuePair("sex", info.sex);
			parameters.add(fir_Pair);
			parameters.add(sec_Pair);
			parameters.add(thir_Pair);
			parameters.add(four_Pair);
			parameters.add(five_Pair);
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
					return "ok".equals(result.toString().trim()) ? info : null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		@Override
		protected void onPostExecute(ClientInfo result) {
			if (result != null) {
				ChatApplication.info.nickname = result.nickname;
				ChatApplication.info.phone = result.phone;
				ChatApplication.info.motto = result.motto;
				Toast.makeText(MyMessageActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
			}else
				Toast.makeText(MyMessageActivity.this, "网络异常,请重新连接", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}
}
