package com.ximoon.weichat;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ximoon.weichat.adapter.SdcardFileAdapter;
import com.ximoon.weichat.entity.ClientInfo;

public class FileActivity extends Activity{
	
	public TextView titleFilePath;
    private ListView fileList;
    private File currentFile;
    private BaseAdapter fileAdapter;
    private Context context;
    private String mSdcardPath;
    private ClientInfo info = null;
    private int location = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        context = this;
        initView();
        initFile();
    }
	
	private void initView(){
		titleFilePath = (TextView) findViewById(R.id.tvpath);
		fileList = (ListView) findViewById(R.id.file_list);
	}

	private void initFile(){
		info = getIntent().getParcelableExtra("user");
		location = getIntent().getIntExtra("location", -1);
	    mSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File root = null;
		if(!TextUtils.isEmpty(mSdcardPath) && new File(mSdcardPath).canRead()){
			root  = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		}
		 currentFile = root;
		 fileAdapter = new SdcardFileAdapter(context, currentFile,info,location);
		 fileList.setAdapter(fileAdapter);
	}
	
	public void backParent(View view){
		File current = ((SdcardFileAdapter) fileAdapter).getCurrentParent();
		try {
			if(!current.getCanonicalPath().equals(mSdcardPath)){
				currentFile = current.getParentFile();
				((SdcardFileAdapter) fileAdapter).setData(currentFile);
				((SdcardFileAdapter) fileAdapter).notifyDataSetChanged();
			}
		} catch (Exception e) {
		}
	}
	
	private void back(View view){
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra("user", info);
		intent.putExtra("location", location);
		startActivity(intent);
	}

}
