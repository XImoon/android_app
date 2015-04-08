package com.ximoon.weichat.view;

import java.sql.Date;

import com.ximoon.weichat.R;
import com.ximoon.weichat.utils.DateUtil;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UpDateView {
	private Context context;
	private ScrollView scrollView;
	private LinearLayout base_Layout;
	//场景更新
	public UpDateView(Context context,ScrollView scrollView){
		this.context=context;
		this.scrollView=scrollView;
		base_Layout=(LinearLayout) scrollView.findViewById(R.id.base_layout);
	
	
	} 
	public void UpDateMyView(String text){
		View myView = InflaterView.inflaterMyView(context);
		TextView date=(TextView) myView.findViewById(R.id.messagedetail_row_date);
		TextView myTv=(TextView) myView.findViewById(R.id.messagedetail_row_text);
		
		date.setText(DateUtil.getCurrentTime());
		myTv.setText(text);
		System.out.println(text+"===");
		base_Layout.addView(myView);
	} 
	public void UpDateSheView(String text){
		View sheView = InflaterView.inflaterSheView(context);
		TextView shedate=(TextView) sheView.findViewById(R.id.messagedetail_row_date);
		TextView sheTv=(TextView) sheView.findViewById(R.id.messagedetail_row_text);
		
		shedate.setText(DateUtil.getCurrentTime());
		sheTv.setText(text);
		System.out.println(text+"===");
		base_Layout.addView(sheView);
	} 
	
}
