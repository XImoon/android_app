package com.ximoon.weichat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ximoon.weichat.adapter.RelpyBaseAdapter;
import com.ximoon.weichat.entity.PostInfo;
import com.ximoon.weichat.service.PersonSevice;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.DateUtil;

public class ReplyActivity extends Activity {

	private TextView title;
	private TextView layer;
	private int answer_layer;
	private TextView time;
	private TextView content;
	private TextView name;
	private PostInfo userInfo;
	private EditText reply;
	private String path="http://192.168.191.1:8080/WeiChat/ReplyServlet";
	private String path2="http://192.168.191.1:8080/WeiChat/QueryReplyServlet";
	
	private LinearLayout baseLayout;
	private ScrollView scrollView;
	private String theme;
	private String title_reply;
	protected List<PostInfo> persons;
	protected List<PostInfo> list_title;
	private RelpyBaseAdapter adapter;
	private String reply_time;
	private String userName;
	private int user_id;
	private static final int NET_FAIL=0;
	private static final int NET_SUCCESS=1;
	private static final int NET_NOT_DATA=2;
	private static final int OLD_NET_SUCCESS = 0;
	private static final int OLD_NET_FAIL = 1;
	private static final int OLD_NET_NOT_DATA = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reply);
		init();
		setData();
		adapter=new RelpyBaseAdapter(getApplicationContext());
		lv_reply.setAdapter(adapter);
	}

	public void setReplyData(View view) {
		reply_time=DateUtil.getCurrentTime();
		final String reply_layer=reply.getText().toString();
		reply.setText("");
		user_id=ChatApplication.info._id;
		userName=ChatApplication.info.nickname;
		new Thread(){
			public void run() {
				PersonSevice service = new PersonSevice();
				Map<String, String> map = new HashMap<String,String>();
				map.put("reply_layer", reply_layer);
				map.put("answer_layer", answer_layer+"");
				map.put("theme", theme);
				map.put("title_reply", title_reply);
				map.put("reply_time", reply_time);
				map.put("user_id", user_id+"");
				map.put("userName", userName);
				try {
					Boolean flag_reply = service.insertReplyLayer(path, map);
					++answer_layer;
					if(flag_reply){
						Message msg = mHandler.obtainMessage();
						msg.what= NET_SUCCESS;
						msg.obj = reply_layer;
						mHandler.sendMessage(msg);
					}else{
						mHandler.sendEmptyMessage(NET_NOT_DATA);
					}
				} catch (Exception e) {
					mHandler.sendEmptyMessage(NET_FAIL);
					e.printStackTrace();
				}
			};
		}.start();
		
	}

	private void setData() {
		Intent intent=getIntent();
		userInfo=(PostInfo)intent.getSerializableExtra("userInfo");
		theme=userInfo.theme;
		title_reply=userInfo.title;
		title.setText(userInfo.title);
		content.setText(userInfo.content);
		time.setText(userInfo.time);
		name.setText(userInfo.name);
		String TitleBase64=userInfo.pic;
		byte[] base64Bytes=Base64.decodeBase64(TitleBase64.getBytes());
		ByteArrayInputStream bais=new ByteArrayInputStream(base64Bytes);
		pic.setImageDrawable(Drawable.createFromStream(bais, "titleimage"));
		getOldData();
		
	}

	private void getOldData() {
		final PersonSevice sevice = new PersonSevice();
		new Thread() {
			public void run() {
				persons=new ArrayList<PostInfo>();
				try {
					// persons = sevice.getPersons(path, "utf-8");
					persons = sevice.getPostsFromGson(path2);
					if (persons != null) {
						// NET_SUCCESS
						Message message = replayHandler.obtainMessage();
						message.what = OLD_NET_SUCCESS;
						message.obj = persons;
						replayHandler.sendMessage(message);
					} else {
						replayHandler.sendEmptyMessage(OLD_NET_NOT_DATA);
					}
				} catch (Exception e) {
					replayHandler.sendEmptyMessage(OLD_NET_FAIL);
					e.printStackTrace();
				}

			};
		}.start();
		
	}
	private Handler replayHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OLD_NET_SUCCESS:
				List<PostInfo> persons = (List<PostInfo>) msg.obj;
				doTitle(persons);
				adapter.setLists(list_title);
				// 成功 刷新
				answer_layer=list_title.size()+1;
				adapter.notifyDataSetChanged();
				break;
			case OLD_NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "没有数据", 1).show();
				break;
			case OLD_NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络连接失败", 1).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

		private void doTitle(List<PostInfo> persons) {
			list_title=new ArrayList<PostInfo>();
			for (PostInfo userInfo : persons) {
				if(userInfo.title.equals(title_reply)){
					list_title.add(userInfo);
				}
			}
		}
	};
	private ListView lv_reply;
	private ImageView pic;

	public int getRepliesCount(String title){
		getOldData();
		return list_title.size();
	}
	private void init() {
		lv_reply = (ListView) findViewById(R.id.lv_reply);
		title = (TextView) findViewById(R.id.title);
		content = (TextView) findViewById(R.id.content);
		time = (TextView) findViewById(R.id.time);
		name = (TextView) findViewById(R.id.name);
		reply=(EditText)findViewById(R.id.reply);
		pic=(ImageView)findViewById(R.id.pic);
		
	}
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_LONG).show();
				break;
			case NET_SUCCESS:
				String reply_layer = (String)msg.obj;
				getOldData();
				Toast.makeText(getApplicationContext(), "发帖成功", Toast.LENGTH_LONG).show();
				break;
			case NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "没有数据", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};
	
}