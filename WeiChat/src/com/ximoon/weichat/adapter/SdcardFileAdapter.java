package com.ximoon.weichat.adapter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ximoon.weichat.ChatActivity;
import com.ximoon.weichat.FileActivity;
import com.ximoon.weichat.R;
import com.ximoon.weichat.entity.ClientInfo;
import com.ximoon.weichat.service.ThreadService;
import com.ximoon.weichat.utils.ChatApplication;

public class SdcardFileAdapter extends BaseAdapter {

	private Context context;
	private File current;
	private File[] files;
	private ClientInfo info;
	private int location = -1;

	public SdcardFileAdapter(Context context, File current, ClientInfo info,
			int location) {
		this.context = context;
		this.current = current;
		this.info = info;
		this.location = location;
		setData(current);
	}

	public File getCurrentParent() {
		return current;
	}

	public void setData(File current) {

		files = current.listFiles();
		this.current = current;
	}

	@Override
	public int getCount() {
		if (files == null) {
			return 0;
		}
		return files.length;
	}

	@Override
	public Object getItem(int position) {
		if (files == null) {
			return null;
		}
		return files[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView fileName;
		ImageView fileIcon;
		LinearLayout clickView;
		if (convertView == null) {
			view = inflater.inflate(R.layout.list_item_file, null);
		} else {
			view = convertView;
		}

		fileIcon = (ImageView) view.findViewById(R.id.icon);
		fileName = (TextView) view.findViewById(R.id.file_name);
		clickView = (LinearLayout) view.findViewById(R.id.itemView);

		File file = (File) getItem(position);
		if (file.isDirectory()) {
			fileIcon.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.folder));
		} else {
			fileIcon.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.file));
		}
		fileName.setText(file.getName());
		try {
			((FileActivity) context).titleFilePath.setText("当前路径为:"
					+ current.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		clickView.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (files[position].isFile()) {
					final File file = files[position];
					Toast.makeText(context, file.getName(), 0).show();
					String msg = "确定发送文件: " + files[position].getName() + " 吗?";
					new AlertDialog.Builder(context)
							.setIcon(R.drawable.dialog_icon).setMessage(msg)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									FileAsyncTask asyncTask = new FileAsyncTask(file);
									asyncTask.execute(file, info);
								}
							}).setNegativeButton("取消", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(context,
											ChatActivity.class);
									intent.putExtra("user", info);
									intent.putExtra("location", location);
									context.startActivity(intent);
								}
							}).show();
				} else {
					current = files[position];
					files = files[position].listFiles();
					notifyDataSetChanged();
				}
				try {
					((FileActivity) context).titleFilePath.setText("当前路径为:"
							+ current.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return view;
	}

	private class FileAsyncTask extends AsyncTask<Object, Void, Boolean> {
		
		private File file = null;
		
		public FileAsyncTask(File file){
			this.file = file;
		}

		private boolean upload(File targetFile) {
			String targetURL = "http://" + ChatApplication.HOSTADDRESS
					+ ":8080/WeiChat/FileServlet"; // TODO 指定URL
			PostMethod filePost = new PostMethod(targetURL);
			try {
				// 通过以下方法可以模拟页面参数提交
//				filePost.setParameter("name", file.getName());
//				filePost.setParameter("pass", "1234");
				Part[] parts = { new FilePart(targetFile.getName(), targetFile) };
				filePost.setRequestEntity(new MultipartRequestEntity(parts,
						filePost.getParams()));
				HttpClient client = new HttpClient();
				client.getHttpConnectionManager().getParams()
						.setConnectionTimeout(5000);
				int status = client.executeMethod(filePost);
				if (status == HttpStatus.SC_OK) {
					System.out.println("上传成功");
					return true;
				} else {
					System.out.println("上传失败");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				filePost.releaseConnection();
			}
			return false;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			file = (File) params[0];
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().build());
			return upload(file);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (true) {
				Toast.makeText(context, "上传成功", 0).show();
				String msg = "chat@" + info._id + "@"+ ChatApplication.info._id + "@file@" + file.getName();
				ThreadService.thread.sendMessage(msg);
			}else {
				Toast.makeText(context, "上传失败", 0).show();
			}
			super.onPostExecute(result);
		}

	}
}
