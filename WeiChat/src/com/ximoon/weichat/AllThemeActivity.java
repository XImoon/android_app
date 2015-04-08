package com.ximoon.weichat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.ximoon.weichat.entity.PostInfo;
import com.ximoon.weichat.service.PersonSevice;
import com.ximoon.weichat.utils.ThemeConstants;

public class AllThemeActivity extends Activity {

	private AutoCompleteTextView theme_search;
	private ArrayAdapter<String> adapter;
	private String[] titles;
	protected List<PostInfo> persons;
	private String path2 = "http://192.168.191.1:8080/WeiChat/ParseToGsonServlet";
	private static final int NET_SUCCESS = 0;
	private static final int NET_FAIL = 1;
	private static final int NET_NOT_DATA = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alltheme);
		init();
		getTitles();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void getTitles() {
		final PersonSevice sevice = new PersonSevice();
		new Thread() {
			public void run() {
				persons = new ArrayList<PostInfo>();
				try {
					// persons = sevice.getPersons(path, "utf-8");
					persons = sevice.getPostsFromGson(path2);
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
				setAutoText(persons);
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

		private void setAutoText(List<PostInfo> persons) {
			titles = new String[persons.size()];
			for (int i = 0; i < persons.size(); i++) {
				titles[i] = persons.get(i).title;
			}
			// 1.创建适配器
			adapter = new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_spinner_item, // xml
					android.R.id.text1, // id
					titles);
			// 2.绑定
			theme_search.setAdapter(adapter);
		}

	};
	private String title;

	private void init() {
		theme_search = (AutoCompleteTextView) findViewById(R.id.theme_search);
		
		
	}
	public void search_iv_go(View view) {
		title = theme_search.getText().toString();
		if (!"搜贴".equals(title) ) {
			for (PostInfo userInfo : persons) {
				if (title.equals(userInfo.title)) {
					Intent intent = new Intent(getApplicationContext(),
							ReplyActivity.class);
					intent.putExtra("userInfo", userInfo);
					startActivity(intent);
					break;
				}
			}

		} else {
			Toast.makeText(getApplicationContext(), "请填写搜索内容", 1).show();
		}

	}

	public void goSuperstar(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.SUPERSTAR_THEME);
		startActivity(intent);

	}

	public void goNews(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.NEWS_THEME);
		startActivity(intent);

	}

	public void goHandsome(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.HANDSOME_THEME);
		startActivity(intent);

	}

	public void goPolicy(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.POLICY_THEME);
		startActivity(intent);

	}

	public void goJoke(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.JOKE_THEME);
		startActivity(intent);

	}

	public void goEconomy(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.ECONOMY_THEME);
		startActivity(intent);

	}

	public void goBeauty(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.BEAUTY_THEME);
		startActivity(intent);

	}

	public void goPE(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.PE_THEME);
		startActivity(intent);

	}

	public void goKaty(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.KATY_THEME);
		startActivity(intent);

	}

	public void goShayne(View view) {
		Intent intent = new Intent(this, ThemeActivity.class);
		intent.putExtra("theme", ThemeConstants.SHAYNE_THEME);
		startActivity(intent);

	}

}
