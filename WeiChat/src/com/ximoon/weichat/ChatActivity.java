package com.ximoon.weichat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ximoon.weichat.adapter.EmojiAdapter;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.entity.RecordMsgInfo;
import com.ximoon.weichat.service.ThreadService;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.PicService;

public class ChatActivity extends Activity {

	private TextView chat_tv_user = null;
	private ClientInfo info = null;
	private EditText chat_et_send_text = null;
	private ImageView chat_iv_send_more = null;
	private LinearLayout chat_rl_chatmsg = null;
	private View view_send_msg = null;
	private View view_receive_msg = null;
	private TextView msg_tv_content = null;
	private TextView msg_tv_receive = null;
	private PastRecord pastRecord = null;
	private FlushHistory receiver = null;
	private ScrollView chat_sv_content = null;
	private Handler mHandler = null;
	private Runnable r = null;
	private TextView msg_tv_send_time = null;
	private TextView msg_tv_receive_time = null;
	private int location = -1;
	private LinearLayout chat_ll_more_operation = null;
	private View view_send_img = null;
	private ImageView img_iv_content = null;
	private TextView img_tv_send_time = null;
	private View view_receive_img = null;
	private ImageView img_iv_receive = null;
	private TextView img_tv_receive_time = null;
	private static final int CAMERA_REQUESTCODE = 100;
	private static final int SELECT_IMAGE_REQUEST_CODE = 0;
	private String time_name = "";
	private String time_name_audio = "";
	private ImageView chat_iv_send_voice = null;
	private int time = 0;
	private MediaRecorder recorder = null;
	private File dir = null;
	private FileOutputStream fos;
	private GridView chat_gv_emoji = null;
	private EmojiAdapter emoji_adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new FlushHistory();
		setContentView(R.layout.activity_chat);
		initViews();
		init();
	}

	@Override
	protected void onStart() {
		IntentFilter filter = new IntentFilter("com.weichat.receive");
		registerReceiver(receiver, filter);
		super.onStart();
	}

	public void initViews() {
		chat_tv_user = (TextView) findViewById(R.id.chat_tv_user);
		chat_et_send_text = (EditText) findViewById(R.id.chat_et_send_text);
		chat_iv_send_more = (ImageView) findViewById(R.id.chat_iv_send_more);
		chat_rl_chatmsg = (LinearLayout) findViewById(R.id.chat_rl_chatmsg);
		chat_sv_content = (ScrollView) findViewById(R.id.chat_sv_content);
		chat_ll_more_operation = (LinearLayout) findViewById(R.id.chat_ll_more_operation);
		chat_iv_send_voice = (ImageView) findViewById(R.id.chat_iv_send_voice);
		chat_gv_emoji = (GridView) findViewById(R.id.chat_gv_emoji);
	}

	public void init() {
		dir = getExternalFilesDir(null);
		Intent intent = getIntent();
		info = intent.getParcelableExtra("user");
		location = intent.getIntExtra("location", -1);
		chat_tv_user.setText(info.exname);
		pastRecord = new PastRecord();
		pastRecord.execute(info._id + "@" + ChatApplication.info._id);
		chat_et_send_text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (TextUtils.isEmpty(chat_et_send_text.getText())) {
					chat_iv_send_more
							.setImageResource(R.drawable.more_operation);
				} else {
					chat_iv_send_more.setImageResource(R.drawable.chating);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mHandler = new Handler();
		r = new Runnable() {
			public void run() {
				int offset = chat_rl_chatmsg.getMeasuredHeight()
						- chat_sv_content.getHeight();
				if (offset < 0) {
					offset = 0;
				}
				chat_sv_content.scrollTo(0, offset);
			}
		};
		chat_iv_send_voice.setOnTouchListener(new OnTouchListener() {

			private Timer timer = null;
			private TimerTask task = null;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					time_name_audio = DateFormat.format("yyyyMMddhhmmss",
							Calendar.getInstance(Locale.CHINA)) + ".3gp";
					recorder = new MediaRecorder();
					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					recorder.setOutputFile(new File(dir, time_name_audio)
							.getAbsolutePath());
					try {
						recorder.prepare();
						recorder.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					timer = new Timer();
					time = 0;
					task = new TimerTask() {
						@Override
						public void run() {
							while (true) {
								SystemClock.sleep(1000);
								time++;
							}
						}
					};
					timer.schedule(task, new Date());
					break;
				case MotionEvent.ACTION_UP:
					// SystemClock.sleep(200);
					recorder.stop();
					recorder.reset();
					recorder.release();
					recorder = null;
					timer.cancel();
					if (time > 0) {
						// 发送语音
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						final File file = new File(dir, time_name_audio);
						byte[] buffer = new byte[1024];
						int iLength = 0;
						FileInputStream fis;
						try {
							fis = new FileInputStream(file);
							while ((iLength = fis.read(buffer)) != -1) {
								baos.write(buffer, 0, iLength);
							}
							baos.flush();
							baos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						String voice = new String(Base64.encodeBase64(baos
								.toByteArray()));
						String msg = "chat@" + info._id + "@"
								+ ChatApplication.info._id + "@voice" + "@"
								+ voice;
						ThreadService.thread.sendMessage(msg);
						addSendVoiceView(System.currentTimeMillis(), file);

						chat_et_send_text.setText(null);
						mHandler.post(r);
					} else {
						// 取消语音

					}
					break;
				default:
					break;
				}
				return true;
			}
		});
		
		emoji_adapter = new EmojiAdapter(this);
		chat_gv_emoji.setAdapter(emoji_adapter);
		chat_gv_emoji.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String msg = "chat@" + info._id + "@" + ChatApplication.info._id + "@emoji"
						+ "@" + position;
				ThreadService.thread.sendMessage(msg);
				view_send_img = getLayoutInflater().inflate(
						R.layout.view_send_img, null);
				img_iv_content = (ImageView) view_send_img
						.findViewById(R.id.img_iv_content);
				img_tv_send_time = (TextView) view_send_img
						.findViewById(R.id.img_tv_send_time);
				img_iv_content.setImageResource(PicService.getPic()[position]);
				img_tv_send_time.setText(new DateFormat().format(
						"yyyy年MM月dd日 hh:mm:ss", new Date()));
				chat_rl_chatmsg.addView(view_send_img);
				chat_gv_emoji.setVisibility(View.GONE);
				mHandler.post(r);
			}
		});
	}

	/**
	 * 退出聊天界面
	 * 
	 * @param view
	 */
	public void back(View view) {
		Intent intent = new Intent(this, ViewPageActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (location != -1) {
			info.isNew = false;
			ChatApplication.friends.set(location, info);
			Intent intent_receiver = new Intent("com.weichat.online");
			sendBroadcast(intent_receiver);
		}
		startActivity(intent);
	}

	/**
	 * 查看详细资料
	 * 
	 * @param view
	 */
	public void information(View view) {
		Intent intent = new Intent(this,InformationActivity.class);
		intent.putExtra("user", info);
		intent.putExtra("location", location);
		startActivity(intent);
	}

	/**
	 * 操作选择:(如果有文本信息则发送消息,否则弹出更多选择)
	 * 
	 * @param view
	 */
	public void get_more(View view) {
		view_send_msg = getLayoutInflater().inflate(R.layout.view_send_msg,
				null);
		msg_tv_send_time = (TextView) view_send_msg
				.findViewById(R.id.msg_tv_send_time);
		msg_tv_content = (TextView) view_send_msg
				.findViewById(R.id.msg_tv_content);
		String msg = chat_et_send_text.getText().toString();
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(ChatActivity.this.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		if (TextUtils.isEmpty(msg)) {
			// 选择其他操作
			if (chat_ll_more_operation.getVisibility() == View.GONE) {
				chat_ll_more_operation.setVisibility(View.VISIBLE);
			} else {
				chat_ll_more_operation.setVisibility(View.GONE);
			}
		} else {
			// 发送文本信息
			// chat sender_id receiver_id text msg img src voice src
			msg = "chat@" + info._id + "@" + ChatApplication.info._id + "@text"
					+ "@" + msg;
			ThreadService.thread.sendMessage(msg);
			msg_tv_content.setText(msg.split("@")[4]);
			msg_tv_send_time.setText(new DateFormat().format(
					"yyyy年MM月dd日 hh:mm:ss", new Date()));
			chat_rl_chatmsg.addView(view_send_msg);
			chat_et_send_text.setText(null);
			mHandler.post(r);

		}
	}

	/**
	 * 发送表情
	 * 
	 * @param view
	 */
	public void send_emoji(View view) {
		if (chat_gv_emoji.getVisibility() == View.GONE) {
			chat_gv_emoji.setVisibility(View.VISIBLE);
		}else {
			chat_gv_emoji.setVisibility(View.GONE);
		}
	}

	/**
	 * 发送位置信息
	 * 
	 * @param view
	 */
	public void more_location(View view) {

	}

	/**
	 * 传送文件
	 * 
	 * @param view
	 */
	public void more_file(View view) {
		Intent intent = new Intent(this,FileActivity.class);
		intent.putExtra("user", info);
		intent.putExtra("location", location);
		startActivity(intent);
	}

	/**
	 * 照相
	 * 
	 * @param view
	 */
	public void more_camera(View view) {
		time_name = DateFormat.format("yyyyMMddhhmmss",
				Calendar.getInstance(Locale.CHINA))
				+ ".jpg";
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri uri = Uri.fromFile(new File(dir, time_name));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, CAMERA_REQUESTCODE);

	}

	/**
	 * 图库选择
	 * 
	 * @param view
	 */
	public void more_picture(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("vnd.android.cursor.dir/image");
		startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
	}

	/**
	 * 得到回传的图片
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bitmap bitmap = null;
		view_send_img = getLayoutInflater().inflate(R.layout.view_send_img,
				null);
		img_iv_content = (ImageView) view_send_img
				.findViewById(R.id.img_iv_content);
		img_tv_send_time = (TextView) view_send_img
				.findViewById(R.id.img_tv_send_time);
		img_tv_send_time.setText(new DateFormat().format(
				"yyyy年MM月dd日 hh:mm:ss", new Date()));
		if (requestCode == CAMERA_REQUESTCODE) {
			File file = new File(dir, time_name);
			if (!file.exists()) {
				return;
			}
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			bitmap = zoomBitmap(bitmap, 200, 200);
			
		} else if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
			if (data == null || data.getData() == null) {
				return;
			}
			Uri uri = data.getData();
			try {
				bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		img_iv_content.setImageBitmap(bitmap);
		chat_rl_chatmsg.addView(view_send_img);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 50, baos);
		String img = new String(Base64.encodeBase64(baos.toByteArray()));
		String msg = "chat@" + info._id + "@" + ChatApplication.info._id
				+ "@img" + "@" + img;
		ThreadService.thread.sendMessage(msg);
		mHandler.post(r);
		chat_ll_more_operation.setVisibility(View.GONE);
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 对图片压缩
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		// 比例
		float sx = ((float) width) / w;
		float sy = ((float) height) / h;
		// 矩阵
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sy);
		// 重新创建
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return bitmap;
	}

	/**
	 * 接触广播接收者的注册
	 */
	@Override
	protected void onStop() {
		unregisterReceiver(receiver);
		super.onStop();
	}

	/**
	 * 异步加载聊天记录
	 * 
	 * @author Ximoon
	 * 
	 */
	private class PastRecord extends
			AsyncTask<String, Void, List<RecordMsgInfo>> {

		/**
		 * 联网获取消息记录
		 */
		@Override
		protected List<RecordMsgInfo> doInBackground(String... params) {
			HttpClient client = (HttpClient) new DefaultHttpClient();
			HttpPost request = new HttpPost("http://"
					+ ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/MsgRecordServlet");
			request.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			BasicNameValuePair nPair = new BasicNameValuePair("flag",
					ChatApplication.info._id + "@" + info._id);
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
					TypeToken<List<RecordMsgInfo>> token = new TypeToken<List<RecordMsgInfo>>() {
					};
					return gson.fromJson(result.toString().trim(),
							token.getType());
				}
			} catch (Exception e) {
			}
			return null;
		}

		/**
		 * 处理信息,更新UI界面
		 */
		@Override
		protected void onPostExecute(List<RecordMsgInfo> result) {
			if (result != null) {
				for (RecordMsgInfo msgInfo : result) {
					long milles = Long.parseLong(msgInfo.time);
					if (msgInfo.flag.startsWith(ChatApplication.info._id + "@")) {
						view_send_msg = getLayoutInflater().inflate(
								R.layout.view_send_msg, null);
						msg_tv_send_time = (TextView) view_send_msg
								.findViewById(R.id.msg_tv_send_time);
						msg_tv_content = (TextView) view_send_msg
								.findViewById(R.id.msg_tv_content);
						if (msgInfo.msg != null && !msgInfo.msg.equals("")) {
							msg_tv_send_time.setText(new DateFormat().format(
									"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
							msg_tv_content.setText(msgInfo.msg);
							chat_rl_chatmsg.addView(view_send_msg);
						} else if (msgInfo.img != null && !msgInfo.img.equals("")) {
							view_send_img = getLayoutInflater().inflate(
									R.layout.view_send_img, null);
							img_iv_content = (ImageView) view_send_img
									.findViewById(R.id.img_iv_content);
							img_tv_send_time = (TextView) view_send_img
									.findViewById(R.id.img_tv_send_time);
							byte[] base64Bytes = Base64
									.decodeBase64(msgInfo.img.getBytes());
							Bitmap bitmap = BitmapFactory.decodeByteArray(
									base64Bytes, 0, base64Bytes.length);
							img_iv_content.setImageBitmap(bitmap);
							img_tv_send_time.setText(new DateFormat().format(
									"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
							chat_rl_chatmsg.addView(view_send_img);
						} else if(msgInfo.voice != null && !msgInfo.voice.equals("")){
							File file = new File(dir, DateFormat.format(
									"yyyyMMddhhmmss", new Date(milles))
									+ ".3gp");
							if (!file.exists() || !file.canRead()) {
								try {
									file.createNewFile();
									file = new File(dir, DateFormat.format(
											"yyyyMMddhhmmss", new Date(milles))
											+ ".3gp");
									FileOutputStream fos = new FileOutputStream(
											file);
									fos.write(Base64.decodeBase64(msgInfo.voice
											.getBytes()));
									fos.flush();
									fos.close();
								} catch (Exception e) {
									Toast.makeText(ChatActivity.this, "请检查SD卡",
											Toast.LENGTH_SHORT).show();
								}
							}
							addSendVoiceView(milles, file);
						}else {
							view_send_img = getLayoutInflater().inflate(
									R.layout.view_send_img, null);
							img_iv_content = (ImageView) view_send_img
									.findViewById(R.id.img_iv_content);
							img_tv_send_time = (TextView) view_send_img
									.findViewById(R.id.img_tv_send_time);
							img_iv_content.setImageResource(PicService.getPic()[msgInfo.emoji]);
							img_tv_send_time.setText(new DateFormat().format(
									"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
							chat_rl_chatmsg.addView(view_send_img);
						}
					} else {
						view_receive_msg = getLayoutInflater().inflate(
								R.layout.view_receive_msg, null);
						msg_tv_receive = (TextView) view_receive_msg
								.findViewById(R.id.msg_tv_receive);
						msg_tv_receive_time = (TextView) view_receive_msg
								.findViewById(R.id.msg_tv_receive_time);
						msg_tv_receive_time.setText(new DateFormat().format(
								"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
						if (msgInfo.msg != null && !msgInfo.msg.equals("")) {
							msg_tv_receive.setText(msgInfo.msg);
							chat_rl_chatmsg.addView(view_receive_msg);
						} else if (msgInfo.img != null && msgInfo.img != "") {
							view_receive_img = getLayoutInflater().inflate(
									R.layout.view_receive_img, null);
							img_iv_receive = (ImageView) view_receive_img
									.findViewById(R.id.img_iv_receive);
							img_tv_receive_time = (TextView) view_receive_img
									.findViewById(R.id.img_tv_receive_time);
							byte[] base64Bytes = Base64
									.decodeBase64(msgInfo.img.getBytes());
							Bitmap bitmap = BitmapFactory.decodeByteArray(
									base64Bytes, 0, base64Bytes.length);
							img_iv_receive.setImageBitmap(bitmap);
							img_iv_receive
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											// 全屏看图片
										}
									});
							img_tv_receive_time.setText(new DateFormat()
									.format("yyyy年MM月dd日 hh:mm:ss", new Date(
											milles)));
							chat_rl_chatmsg.addView(view_receive_img);
						} else if(msgInfo.voice != null && !msgInfo.voice.equals("")) {
							addVoiceView(msgInfo, milles);
						}else {
							view_receive_img = getLayoutInflater().inflate(
									R.layout.view_receive_img, null);
							img_iv_receive = (ImageView) view_receive_img
									.findViewById(R.id.img_iv_receive);
							img_tv_receive_time = (TextView) view_receive_img
									.findViewById(R.id.img_tv_receive_time);
							img_iv_receive.setImageResource(PicService.getPic()[msgInfo.emoji]);
							img_tv_receive_time.setText(new DateFormat()
							.format("yyyy年MM月dd日 hh:mm:ss", new Date(
									milles)));
							chat_rl_chatmsg.addView(view_receive_img);
						}
					}
				}
				mHandler.post(r);
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * 添加发送语音消息界面
	 * 
	 * @param milles
	 * @param file
	 */
	private void addSendVoiceView(long milles, File file) {
		view_send_msg = ChatActivity.this.getLayoutInflater().inflate(
				R.layout.view_send_msg, null);
		msg_tv_send_time = (TextView) view_send_msg
				.findViewById(R.id.msg_tv_send_time);
		msg_tv_content = (TextView) view_send_msg
				.findViewById(R.id.msg_tv_content);
		msg_tv_content.setText("语音消息");
		msg_tv_content.setTextColor(Color.GREEN);
		msg_tv_send_time.setText(new DateFormat().format(
				"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
		msg_tv_content.setClickable(true);
		msg_tv_content.setOnClickListener(new VoiceView(file));
		chat_rl_chatmsg.addView(view_send_msg);
	}

	/**
	 * 添加接受语音消息界面
	 * 
	 * @param msgInfo
	 * @param milles
	 */
	private void addVoiceView(RecordMsgInfo msgInfo, Long milles) {
		File file = new File(dir, DateFormat.format("yyyyMMddhhmmss", new Date(
				milles))
				+ ".3gp");
		SystemClock.sleep(200);
		if (!file.exists() || !file.canRead()) {
			try {
				file.createNewFile();
				fos = new FileOutputStream(file);
				fos.write(Base64.decodeBase64(msgInfo.voice.getBytes()));
				fos.flush();
				fos.close();
			} catch (Exception e1) {
				Toast.makeText(ChatActivity.this, "请检查SD卡", Toast.LENGTH_SHORT)
						.show();
			}
		}
		view_receive_msg = getLayoutInflater().inflate(
				R.layout.view_receive_msg, null);
		msg_tv_receive_time = (TextView) view_receive_msg
				.findViewById(R.id.msg_tv_receive_time);
		msg_tv_receive = (TextView) view_receive_msg
				.findViewById(R.id.msg_tv_receive);
		msg_tv_receive.setText("语音消息");
		msg_tv_receive.setTextColor(Color.GREEN);
		msg_tv_receive_time.setText(new DateFormat().format(
				"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
		msg_tv_receive.setClickable(true);
		msg_tv_receive.setOnClickListener(new VoiceView(file));
		chat_rl_chatmsg.addView(view_receive_msg);
	}

	/**
	 * 广播接收者,刷新即时消息,更新UI界面
	 * 
	 * @author Ximoon
	 * 
	 */
	private class FlushHistory extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final RecordMsgInfo msgInfo = (RecordMsgInfo) intent.getSerializableExtra("record");
			int _id = intent.getIntExtra("_id", 0);
			if (_id == info._id) {
				final Long milles = System.currentTimeMillis();
				if (msgInfo.msg != null && !msgInfo.msg.equals("")) {
					view_receive_msg = getLayoutInflater().inflate(
							R.layout.view_receive_msg, null);
					msg_tv_receive = (TextView) view_receive_msg
							.findViewById(R.id.msg_tv_receive);
					msg_tv_receive_time = (TextView) view_receive_msg
							.findViewById(R.id.msg_tv_receive_time);
					msg_tv_receive.setText(msgInfo.msg);
					msg_tv_receive_time.setText(new DateFormat().format(
							"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
					chat_rl_chatmsg.addView(view_receive_msg);
				} else if (msgInfo.img != null && !msgInfo.img.equals("")) {
					view_receive_img = getLayoutInflater().inflate(
							R.layout.view_receive_img, null);
					img_iv_receive = (ImageView) view_receive_img
							.findViewById(R.id.img_iv_receive);
					img_tv_receive_time = (TextView) view_receive_img
							.findViewById(R.id.img_tv_receive_time);
					byte[] base64Bytes = Base64.decodeBase64(msgInfo.img
							.getBytes());
					Bitmap bitmap = BitmapFactory.decodeByteArray(base64Bytes,
							0, base64Bytes.length);
					img_iv_receive.setImageBitmap(bitmap);
					img_iv_receive.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// 全屏看图片
						}
					});
					img_tv_receive_time.setText(new DateFormat().format(
							"yyyy年MM月dd日 hh:mm:ss", new Date(milles)));
					chat_rl_chatmsg.addView(view_receive_img);
				} else if(msgInfo.voice != null && !msgInfo.voice.equals("")) {
					addVoiceView(msgInfo, milles);
				}else if(msgInfo.emoji != -1 ){
					view_receive_img = getLayoutInflater().inflate(
							R.layout.view_receive_img, null);
					img_iv_receive = (ImageView) view_receive_img
							.findViewById(R.id.img_iv_receive);
					img_tv_receive_time = (TextView) view_receive_img
							.findViewById(R.id.img_tv_receive_time);
					img_tv_receive_time.setText(new DateFormat()
					.format("yyyy年MM月dd日 hh:mm:ss", new Date(
							milles)));
					img_iv_receive.setImageResource(PicService.getPic()[msgInfo.emoji]);
					chat_rl_chatmsg.addView(view_receive_img);
				}
				mHandler.post(r);
			}
		}

	}

	/**
	 * 语音消息的点击播放事件
	 * 
	 * @author Ximoon
	 * 
	 */
	private class VoiceView implements OnClickListener {

		private MediaPlayer player = null;
		private File file = null;

		public VoiceView(File file) {
			this.file = file;
		}

		@Override
		public void onClick(View v) {
			Toast.makeText(ChatActivity.this, "正在播放", Toast.LENGTH_SHORT)
					.show();
			if (player != null) {
				player.stop();
				player.reset();
				player.release();
				player = null;
			}
			player = new MediaPlayer();
			Uri uri = Uri.fromFile(file);
			player.reset();
			try {
				player.setDataSource(ChatActivity.this, uri);
				player.prepare();
				player.start();
				Toast.makeText(ChatActivity.this, "播放完毕", Toast.LENGTH_SHORT)
						.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}