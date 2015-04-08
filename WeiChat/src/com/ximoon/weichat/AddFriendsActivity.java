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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ximoon.weichat.adapter.FriendsAdapter;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.view.ClearEditText;

public class AddFriendsActivity extends Activity {
	
	private ClearEditText add_ce_search = null;
	private ListView add_lv_friends = null;
	private FriendsAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friends);
		initViews();
		init();
	}
	
	public void initViews(){
		add_ce_search = (ClearEditText) findViewById(R.id.add_ce_search);
		add_lv_friends = (ListView) findViewById(R.id.add_lv_friends);
	}
	
	public void init(){
		add_lv_friends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final ClientInfo info = (ClientInfo) adapter.getItem(position);
				String msg = "是否确定添加用户" + info.nickname + "为好友?";
				new AlertDialog.Builder(AddFriendsActivity.this).setIcon(R.drawable.dialog_icon).setMessage(msg).setPositiveButton("确定", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AddFriendsAsyncTask asyncTask = new AddFriendsAsyncTask();
						asyncTask.execute(info._id);
					}
				}).setNegativeButton("取消", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
			}
		});
	}

	public void back(View view){
		Intent intent = new Intent(this,ViewPageActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	public void find(View view){
		String msg = add_ce_search.getText().toString();
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		//查找好友
		SearchFriendsAsyncTask asyncTask = new SearchFriendsAsyncTask();
		asyncTask.execute(msg);
	}
	
	private class SearchFriendsAsyncTask extends AsyncTask<String, Void, List<ClientInfo>>{

		@Override
		protected List<ClientInfo> doInBackground(String... params) {
			String account = params[0];
			HttpClient client = (HttpClient) new DefaultHttpClient();
			HttpPost request = new HttpPost("http://"
					+ ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/SearchFriendsServlet");
			request.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			BasicNameValuePair nPair = new BasicNameValuePair("account",
					account);
			parameters.add(nPair);
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
					Gson gson = new Gson();
					TypeToken<List<ClientInfo>> token = new TypeToken<List<ClientInfo>>() {
					};
					List<ClientInfo> lists = gson.fromJson(result
							.toString().trim(), token.getType());
					return lists;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		@Override
		protected void onPostExecute(List<ClientInfo> result) {
			if (null != result) {
				if (adapter == null) {
					adapter = new FriendsAdapter(AddFriendsActivity.this, result);
					add_lv_friends.setAdapter(adapter);
				}else {
					adapter.setList(result);
					adapter.notifyDataSetChanged();
				}
			}else {
				Toast.makeText(AddFriendsActivity.this, "网络连接错误,请重新尝试", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
	
	private class AddFriendsAsyncTask extends AsyncTask<Integer, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Integer... params) {
			int friends_id = params[0];
			HttpClient client = (HttpClient) new DefaultHttpClient();
			HttpPost request = new HttpPost("http://"
					+ ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/AddFriendsServlet");
			request.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			BasicNameValuePair fir_Pair = new BasicNameValuePair("_id",friends_id+"");
			BasicNameValuePair sec_Pair = new BasicNameValuePair("friend_id",ChatApplication.info._id+"");
			parameters.add(fir_Pair);
			parameters.add(sec_Pair);
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
					return "ok".equals(result.toString().trim()) ? true : false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				new AlertDialog.Builder(AddFriendsActivity.this).setMessage("添加成功").create().show();
			}else {
				new AlertDialog.Builder(AddFriendsActivity.this).setMessage("添加失败,该用户已经是您的好友!").create().show();
			}
			super.onPostExecute(result);
		}
		
	}
	
}
