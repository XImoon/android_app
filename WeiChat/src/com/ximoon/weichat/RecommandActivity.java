package com.ximoon.weichat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ximoon.weichat.adapter.PersonBaseAdapter;
import com.ximoon.weichat.entity.PostInfo;
import com.ximoon.weichat.service.PersonSevice;

public class RecommandActivity extends Activity implements OnClickListener,OnItemClickListener{
	private static final int NET_SUCCESS = 0;
	private static final int NET_FAIL = 1;
	private static final int NET_NOT_DATA = 2;
	private String path2 = "http://192.168.191.1:8080/WeiChat/RecommandServlet";
	protected List<PostInfo> persons;
	private ListView lv_recommands;
	private PersonBaseAdapter adapter;
	private Button recommands_back_btn;
	private LayoutInflater mInflater;
	private ListView lv_persons;
	private int headerViewsCount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommand);
		init();
		getData();
		mInflater = LayoutInflater.from(getApplicationContext());
		lv_recommands.addHeaderView(mInflater.inflate(R.layout.lv_head_layout,
				null));
		headerViewsCount = lv_recommands.getHeaderViewsCount();
		adapter = new PersonBaseAdapter(this, headerViewsCount);
		lv_recommands.setAdapter(adapter);
		lv_recommands.setOnItemClickListener(this);
	}
	private void init() {
		lv_recommands=(ListView)findViewById(R.id.lv_recommands);
		recommands_back_btn=(Button)findViewById(R.id.recommands_back_btn);
		recommands_back_btn.setOnClickListener(this);
	}
	
	private void getData() {
		final PersonSevice sevice = new PersonSevice();
		new Thread() {
			public void run() {
				persons = new ArrayList<PostInfo>();
				try {
					// persons = sevice.getPersons(path, "utf-8");
					persons = sevice.getPostsFromGson(path2);
					//System.out.println(persons);
					if (persons != null) {
						// NET_SUCCESS
						Message message = mHandler.obtainMessage();

						message.what = NET_SUCCESS;
						message.obj = persons;
						mHandler.sendMessage(message);
					} else {
						mHandler.sendEmptyMessage(NET_NOT_DATA);
					}
				} catch (Exception e) {
					mHandler.sendEmptyMessage(NET_FAIL);
					e.printStackTrace();
				}

			};
		}.start();
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NET_SUCCESS:
				List<PostInfo> persons = (List<PostInfo>) msg.obj;
//				doTheme(persons);
				adapter.setLists(persons);
//				 刷新
				adapter.notifyDataSetChanged();
				break;
			case NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "没有数据", 1).show();
				break;
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络异常", 1).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.recommands_back_btn:
			Intent intent=new Intent(getApplicationContext(),ViewPageActivity.class);
			startActivity(intent);
			this.finish();
			this.finish();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position >= headerViewsCount) {
			PostInfo userInfo = (PostInfo) adapter.getItem(position);
			Intent intent = new Intent(getApplicationContext(),
					ReplyActivity.class);
			intent.putExtra("userInfo", userInfo);
			startActivity(intent);
		}
	}
}