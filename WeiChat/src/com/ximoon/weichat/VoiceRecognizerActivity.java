package com.ximoon.weichat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.ximoon.weichat.service.MyAsyncTask;
import com.ximoon.weichat.utils.Constants;
import com.ximoon.weichat.utils.VoiceRecognizer;
import com.ximoon.weichat.view.UpDateView;

public class VoiceRecognizerActivity extends Activity implements
		OnClickListener {

	private Button bt_voice_recognizer;
	private Button bt_voice_send;
	// 讯飞的appid
	// public static final String XUNFEI_APPID = "5212ef0a";
	// private SpeechRecognizer speechRecognizer;
	String TAG = "MainActivity";
	private EditText et_voice_text;
	private ScrollView scrollView;
	private UpDateView upDateView;
	private String voicetext;
	private MyAsyncTask asynctask;
	private VoiceRecognizer recognizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voicerrcognizer);

		initViews();
		upDateView = new UpDateView(this, scrollView);
		asynctask = new MyAsyncTask(this, upDateView);
		registerBroadCast();

	}

	private void registerBroadCast() {
		// 打电话的广播注册
		IntentFilter filter = new IntentFilter();
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		filter.addAction(Constants.ACTIVITY_CALL_ACTION);
		registerReceiver(callreceiver, filter);
		// 注册发短息的广播
		IntentFilter sendMessagefilter = new IntentFilter();
		sendMessagefilter.addCategory(Intent.CATEGORY_DEFAULT);
		sendMessagefilter.addAction(Constants.ACTIVITY_SENDMESSAGE_ACTION);
		registerReceiver(sendmessagereceiver, sendMessagefilter);
		// 注册大冒险的广播
		// IntentFilter bravefilter = new IntentFilter();
		// bravefilter.addCategory(Intent.CATEGORY_DEFAULT);
		// bravefilter.addAction(Constants.ACTIVITY_TRUTHORBRAVE_ACTION);
		// registerReceiver(bravereceiver, bravefilter);
		IntentFilter sendCamerafilter = new IntentFilter();
		sendCamerafilter.addCategory(Intent.CATEGORY_DEFAULT);
		sendCamerafilter.addAction(Constants.ACTIVITY_SENDMESSAGE_ACTION);
		registerReceiver(sendcamerareceiver, sendCamerafilter);
	}

	// 接收打电话的广播
	private BroadcastReceiver callreceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Constants.ACTIVITY_CALL_ACTION)) {
				Intent callintent = new Intent();
				callintent.setAction(Intent.ACTION_CALL);
				callintent.setData(Uri.parse("tel:" + 110));
				startActivity(callintent);
			}
		}

	};
	// 接收发短息的广播
	private BroadcastReceiver sendmessagereceiver = new BroadcastReceiver() {

		@SuppressLint("UnlocalizedSms")
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction()
					.equals(Constants.ACTIVITY_SENDMESSAGE_ACTION)) {
				Uri smsToUri = Uri.parse("smsto:输入号码");
				Intent intent2 = new Intent(Intent.ACTION_SENDTO, smsToUri);
				intent2.putExtra("sms_body", "短息内容");
				startActivity(intent2);

			}
		}

	};
	// 接收大冒险的广播
	// private BroadcastReceiver bravereceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	//
	// if (intent.getAction().equals(Constants.ACTIVITY_TRUTHORBRAVE_ACTION)) {
	// Intent braveintent = new Intent(VoiceRecognizerActivity.this,
	// TruthOrBraveActivity.class);
	// startActivity(braveintent);
	// }
	// }
	//
	// };
	private BroadcastReceiver sendcamerareceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.ACTIVITY_CAMERA_ACTION)) {
				// 1.只要知道意图
				Intent intentcamera = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				startActivity(intent);

			}

		}

	};

	private void initViews() {
		bt_voice_recognizer = (Button) findViewById(R.id.bt_voice_recognizer);
		et_voice_text = (EditText) findViewById(R.id.et_voice_text);
		bt_voice_send = (Button) findViewById(R.id.bt_voice_send);

		recognizer = new VoiceRecognizer(this);

		bt_voice_recognizer.setOnClickListener(this);
		bt_voice_send.setOnClickListener(this);

		scrollView = (ScrollView) findViewById(R.id.scolllayout);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				et_voice_text.setText((String) msg.obj);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_voice_recognizer:
			recognizer.start();
			new Thread() {
				public void run() {
					String result = recognizer.getResult();
					Log.i("ximoon", result + "=======Thread=====");
					Message msg = handler.obtainMessage();
					msg.what = 0;
					msg.obj = result;
					handler.sendMessage(msg);
				};
			}.start();
			break;
		case R.id.bt_voice_send:
			// 点击按钮更新场景
			voicetext = et_voice_text.getText().toString();
			upDateView.UpDateMyView(voicetext);
			// 根据命令寻找答案
			// 延时操作
			Handler mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					asynctask.queryAnswer(voicetext);

				}
			}, 2000);
			break;

		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		if (callreceiver != null) {
			unregisterReceiver(callreceiver);
		}
		if (sendmessagereceiver != null) {
			unregisterReceiver(sendmessagereceiver);
		}
		if (sendcamerareceiver != null) {
			unregisterReceiver(sendcamerareceiver);
		}
		super.onDestroy();
	}

}
