package com.ximoon.weichat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ximoon.weichat.adapter.SortAdapter;
import com.ximoon.weichat.adapter.SortAllAdapter;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.entity.RecordMsgInfo;
import com.ximoon.weichat.entity.SortModel;
import com.ximoon.weichat.service.DownloadThread;
import com.ximoon.weichat.service.ThreadService;
import com.ximoon.weichat.utils.CharacterParser;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.PinyinComparator;
import com.ximoon.weichat.view.ClearEditText;
import com.ximoon.weichat.view.SideBar;
import com.ximoon.weichat.view.SideBar.OnTouchingLetterChangedListener;

public class NavigationActivity extends Activity {

	private WeiChatReceiver receiver = null;
	private ListView navigation_lv_friends = null;
	private ListView navigation_lv_friends_all;
	private NotificationManager manager = null;
	private Notification notification = null;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private SortAllAdapter adapter_sort_all;
	private ClearEditText mClearEditText;
	// 汉字转换成拼音的类
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;
	private List<SortModel> SourceDateAll;
	// 根据拼音来排列ListView里面的数据类
	private PinyinComparator pinyinComparator;
	private IntentFilter filter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		receiver = new WeiChatReceiver();
		filter = new IntentFilter("com.weichat.online");
		registerReceiver(receiver, filter);
		initViews();
		init();
		getAllFriends();
	}

	@Override
	protected void onStart() {
		System.out.println("NavigationActivity.onStart()");
		if (ThreadService.thread == null) {
			ThreadService.thread = ThreadService.getDefaul(this);
		}
		ThreadService.thread.context = this;
		if (adapter != null) {
			SourceDateList = filledData(ChatApplication.friends);
			Collections.sort(SourceDateList, pinyinComparator);
			adapter.updateListView(SourceDateList);
		}
		super.onStart();
	}

	public void go(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void online(View view) {
		navigation_lv_friends_all.setVisibility(View.GONE);
		navigation_lv_friends.setVisibility(View.VISIBLE);
	}

	public void offline(View view) {
		navigation_lv_friends.setVisibility(View.GONE);
		navigation_lv_friends_all.setVisibility(View.VISIBLE);
		getAllFriends();
	}

	public void add(View view) {
		Intent intent = new Intent(this, AddFriendsActivity.class);
		startActivity(intent);
	}

	public void initViews() {
		navigation_lv_friends = (ListView) findViewById(R.id.navigation_lv_friends);
		navigation_lv_friends_all = (ListView) findViewById(R.id.navigation_lv_friends_all);
		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
	}

	public void init() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		sideBar.setTextView(dialog);
		if (adapter == null) {
			SourceDateList = filledData(ChatApplication.friends);
			// 根据a-z进行排序源数据
			Collections.sort(SourceDateList, pinyinComparator);
			adapter = new SortAdapter(this, SourceDateList);
			navigation_lv_friends.setAdapter(adapter);
		}
		if (adapter_sort_all == null) {
			SourceDateAll = filledData(ChatApplication.friends_all);
			// 根据a-z进行排序源数据
			Collections.sort(SourceDateAll, pinyinComparator);
			adapter_sort_all = new SortAllAdapter(this, SourceDateAll);
			navigation_lv_friends_all.setAdapter(adapter_sort_all);
		}
		navigation_lv_friends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ClientInfo user = ((SortModel) adapter.getItem(position))
						.getClientInfo();
				user.isNew = false;
				ChatApplication.friends.set(position, user);
				adapter.notifyDataSetChanged();
				Intent intent = new Intent(NavigationActivity.this,
						ChatActivity.class);
				intent.putExtra("user", user);
				intent.putExtra("location", position);
				startActivity(intent);
			}
		});
		manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
		sortInit();
	}

	public void sortInit() {
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					navigation_lv_friends.setSelection(position);
				}
			}
		});

		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	private List<SortModel> filledData(ArrayList<ClientInfo> date) {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		for (int i = 0; i < date.size(); i++) {
			SortModel sortModel = new SortModel();
			sortModel.setClientInfo(date.get(i));
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date.get(i).exname);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<SortModel> filterDateList = new ArrayList<SortModel>();
		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getClientInfo().exname;
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	@Override
	protected void onDestroy() {
		System.out.println("NavigationActivity.onDestroy()");
		String msg_exit = "exit@";
		ThreadService.thread.sendMessage(msg_exit);
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
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

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				ArrayList<ClientInfo> list_friends = (ArrayList<ClientInfo>) msg.obj;
				SourceDateAll = filledData(list_friends);
				ChatApplication.friends_all = list_friends;
				if (adapter_sort_all == null) {
					// 根据a-z进行排序源数据
					SourceDateAll = filledData(ChatApplication.friends_all);
					Collections.sort(SourceDateAll, pinyinComparator);
					adapter_sort_all = new SortAllAdapter(
							NavigationActivity.this, SourceDateAll);
					navigation_lv_friends_all.setAdapter(adapter_sort_all);
				} else {
					adapter_sort_all.updateListView(SourceDateAll);
				}
				break;

			default:
				break;
			}
		};
	};

	public void getAllFriends() {
		new Thread() {
			public void run() {
				HttpClient client = (HttpClient) new DefaultHttpClient();
				HttpPost request = new HttpPost("http://"
						+ ChatApplication.HOSTADDRESS
						+ ":8080/WeiChat/GainAllFriendsServlet");
				request.getParams().setIntParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				BasicNameValuePair nPair = new BasicNameValuePair("_id",
						ChatApplication.info._id + "");
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
						Message msg = mHandler.obtainMessage();
						msg.what = 0;
						msg.obj = lists;
						mHandler.sendMessage(msg);
					}
				} catch (Exception e) {

				}
			};
		}.start();
	}

	public class WeiChatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (pinyinComparator == null) {
				pinyinComparator = new PinyinComparator();
			}
			SourceDateList = filledData(ChatApplication.friends);
			// 根据a-z进行排序源数据
			Collections.sort(SourceDateList, pinyinComparator);
			adapter.setList(SourceDateList);
			adapter.notifyDataSetChanged();

			SourceDateAll = filledData(ChatApplication.friends_all);
			Collections.sort(SourceDateAll, pinyinComparator);
			if (adapter_sort_all == null) {
				SourceDateAll = filledData(ChatApplication.friends_all);
				adapter_sort_all = new SortAllAdapter(NavigationActivity.this,
						SourceDateAll);
				navigation_lv_friends.setAdapter(adapter);
			} else {
				adapter_sort_all.updateListView(SourceDateAll);
			}
			if ("com.weichat.online".equals(intent.getAction())) {
				String operation = intent.getStringExtra("operation");
				if ("chat".equals(operation)) {
					ClientInfo friend = intent.getParcelableExtra("friend");
					RecordMsgInfo msgInfo = (RecordMsgInfo) intent
							.getSerializableExtra("record");
					int location = intent.getIntExtra("location", -1);
					int _id = friend._id;
					intent = new Intent("com.weichat.receive");
					intent.putExtra("_id", _id);
					intent.putExtra("record", msgInfo);
					context.sendBroadcast(intent);
					notification = new Notification();
					notification.icon = R.drawable.oe;
					String msg = (msgInfo.msg == null || "".equals(msgInfo.msg)) ? "新的消息"
							: msgInfo.msg;
					notification.tickerText = "你有新的消息,来自:" + friend.exname
							+ "\n" + msg;
					notification.when = System.currentTimeMillis();
					Intent contentIntent = new Intent(context,
							ChatActivity.class);
					contentIntent.putExtra("user", friend);
					contentIntent.putExtra("location", location);
					PendingIntent pendingIntent = PendingIntent.getActivity(
							context, 100, contentIntent,
							PendingIntent.FLAG_ONE_SHOT);
					notification.setLatestEventInfo(context, "你有新的好友消息,来自"
							+ friend.exname, msg, pendingIntent);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					manager.notify(100, notification);
				} else if ("file".equals(operation)) {
					ClientInfo friendInfo = intent.getParcelableExtra("friend");
					final String filename = intent.getStringExtra("filename");
					File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					final File file = new File(dir, filename);
					new AlertDialog.Builder(NavigationActivity.this)
							.setIcon(R.drawable.dialog_icon)
							.setMessage(
									"您的好友" + friendInfo.exname + "向您发送文件"
											+ filename + ",是否接受?")
							.setPositiveButton("确定", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										download("http://"
												+ ChatApplication.HOSTADDRESS
												+ ":8080/WeiChat/"
												+ filename, file);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}).setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							}).show();
				}
			}
		}

		private int fileSize;
		private int threadSize = 3;

		public void download(String path, File file){
			FileThread threadFile = new FileThread(path,file);
			threadFile.start();
		}

		private class FileThread extends Thread {

			private String path = "";
			private File file = null;

			public FileThread(String path,File file) {
				this.path = path;
				this.file = file;
			}

			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("POST");
					conn.setConnectTimeout(5000);
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						fileSize = conn.getContentLength();
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");// 可读可写可删除
						int block = fileSize % threadSize == 0 ? fileSize / threadSize
								: fileSize / threadSize + 1;
						for (int i = 0; i < threadSize; i++) {
							new DownloadThread(i, block, path, file).start();// 启动
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}