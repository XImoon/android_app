package com.ximoon.weichat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ximoon.weichat.service.PersonSevice;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.DateUtil;

public class AddThemeActivity extends Activity implements OnClickListener {

	private String theme;
	private EditText et_title;
	private EditText et_content;
	private Button addtheme_back_btn;
	private Button addtheme_btn_done;
	private String title;
	private String content;
	private String time;
	private int layer;
	private int user_id;
	private String userName;
	private String path = "http://192.168.191.1:8080/WeiChat/AddTitleServlet";
	private Button add_bt_pic;
	private ImageView iv_show;
	private Button add_bt_take;
	private static final int SELECT_IMAGE_REQUEST_CODE = 0;
	private static final int NET_FAIL = 0;
	private static final int NET_SUCCESS = 1;
	private static final int NET_NOT_DATA = 2;
	private static final int CAMERA_REQUESTCODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_theme);
		init();
		Intent intent = getIntent();
		theme = intent.getStringExtra("theme");

	}

	private void init() {
		et_title = (EditText) findViewById(R.id.et_title);
		iv_show = (ImageView) findViewById(R.id.iv_show);
		iv_show.setVisibility(View.GONE);
		et_content = (EditText) findViewById(R.id.et_content);
		addtheme_back_btn = (Button) findViewById(R.id.addtheme_back_btn);
		addtheme_btn_done = (Button) findViewById(R.id.addtheme_btn_done);
		add_bt_pic = (Button) findViewById(R.id.add_bt_pic);
		add_bt_take = (Button) findViewById(R.id.add_bt_take);
		addtheme_back_btn.setOnClickListener(this);
		addtheme_btn_done.setOnClickListener(this);
		add_bt_pic.setOnClickListener(this);
		add_bt_take.setOnClickListener(this);
	}

	private Uri uri;
	private String picPath = null;
	private String pic;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addtheme_back_btn:
			Intent intent = new Intent(getApplicationContext(),
					ThemeActivity.class);
			intent.putExtra("theme", theme);
			/* 加个对话框，询问一下是否放弃编辑 */
			startActivity(intent);
			this.finish();
			break;
		case R.id.add_bt_pic:
			intent = new Intent(Intent.ACTION_PICK);
			intent.putExtra("theme", theme);
			intent.setType("vnd.android.cursor.dir/image");
			startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
			/* 加个对话框，询问一下是否放弃编辑 */
			// startActivity(intent);
			break;
		case R.id.add_bt_take:
			file_camera = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"34.jpg");
			if (file_camera != null) {
				if (file_camera.exists()) {
					file_camera.delete();
				}
			}
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri uri = Uri.fromFile(file_camera);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(intent, CAMERA_REQUESTCODE);
			break;
		case R.id.addtheme_btn_done:
			if (picPath == null) {
				alert();
			} else if (null != bitmap) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// ((BitmapDrawable)
				// iv_show.getDrawable()).getBitmap().compress(CompressFormat.JPEG,50,baos);
				bitmap.compress(CompressFormat.JPEG, 50, baos);
				String imageBase64 = new String(Base64.encodeBase64(baos
						.toByteArray()));
				// System.out.println(file.getAbsolutePath());
				title = et_title.getText().toString();
				layer = 0;
				content = et_content.getText().toString();
				time = DateUtil.getCurrentTime();
				user_id = ChatApplication.info._id;
				userName = ChatApplication.info.nickname;
				pic = imageBase64;
				if (null != title && null != content) {
					new Thread() {
						public void run() {
							PersonSevice service = new PersonSevice();
							Map<String, String> map = new HashMap<String, String>();
							map.put("theme", theme);
							map.put("title", title);
							map.put("layer", layer + "");
							map.put("content", content);
							map.put("time", time);
							map.put("user_id", user_id + "");
							map.put("userName", userName);
							map.put("pic", pic);
							try {
								Boolean flag_title = service.insertReplyLayer(
										path, map);
								if (flag_title) {
									// Toast.makeText(getApplicationContext(),
									// "数据插入成功", 1).show();
									mHandler.sendEmptyMessage(NET_SUCCESS);
								} else {
									mHandler.sendEmptyMessage(NET_NOT_DATA);
								}
							} catch (Exception e) {
								mHandler.sendEmptyMessage(NET_FAIL);
								e.printStackTrace();
							}
						};
					}.start();
					intent = new Intent(getApplicationContext(),
							ThemeActivity.class);
					intent.putExtra("theme", theme);
					startActivity(intent);
				}
			} else {
				alert();
			}
			break;

		default:
			break;
		}

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络异常",
						Toast.LENGTH_LONG).show();
				break;
			case NET_SUCCESS:
				Toast.makeText(getApplicationContext(), "图片插入成功",
						Toast.LENGTH_LONG).show();
				break;
			case NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "数据插入出错",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};
	private Handler picHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络异常",
						Toast.LENGTH_LONG).show();
				break;
			case NET_SUCCESS:

				Toast.makeText(getApplicationContext(), "图片插入成功",
						Toast.LENGTH_LONG).show();
				break;
			case NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "图片插入出错",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};
	private File file;
	private Bitmap bitmap;
	private File file_camera;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 拍照回传
		if (requestCode == CAMERA_REQUESTCODE) {
//			file_camera = new File(
//					getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//					"34.jpg");
			if (!file_camera.exists()) {
				return;
			}
			picPath = file_camera.getAbsolutePath();
			bitmap = BitmapFactory.decodeFile(file_camera.getAbsolutePath());
			// 内存溢出，对图片进行压缩
			bitmap = zoomBitmap(bitmap, 200, 200);
			iv_show.setImageBitmap(bitmap);
			iv_show.setVisibility(View.VISIBLE);

		} else if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
			if (data == null || data.getData() == null) {
				return;
			}
			uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };

			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
			if (actualimagecursor != null) {
				int actual_image_column_index = actualimagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

				actualimagecursor.moveToFirst();

				picPath = actualimagecursor
						.getString(actual_image_column_index);
			}
			file = new File(picPath);
			try {
				bitmap = MediaStore.Images.Media.getBitmap(
						getContentResolver(), uri);
				// bitmap = zoomBitmap(bitmap,200,200);
				iv_show.setImageBitmap(bitmap);
				iv_show.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 作业：裁剪

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void alert() {
		Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
				.setMessage("您选择的不是有效的图片")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						picPath = null;
					}
				}).create();
		dialog.show();
	}

	// 对图片压缩
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

}
