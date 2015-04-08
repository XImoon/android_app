package com.ximoon.weichat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.ximoon.weichat.adapter.ViewPagerAdapter;
import com.ximoon.weichat.service.ThreadService;
import com.ximoon.weichat.utils.ChatApplication;
import com.ximoon.weichat.utils.ImageLoader;

public class ViewPageActivity extends Activity {
	
	private static final String TAG = "PageActivity";
	private ViewPager viewpager;
	private SlidingMenu sm; 
	private LocalActivityManager lam;
	private TextView sliding_iv_qqname = null;
	private TextView sliding_tv_sign = null;
	private ImageView sliding_iv_headimage = null;
	private View activity_slidingmenu = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_viewpage);
		initViews();
		viewpager.setOnPageChangeListener(onPageChangeListener);
		lam=new LocalActivityManager(this, true);
		lam.dispatchCreate(savedInstanceState);
		initActivities();
		initSlidingMenu();
	}
	
	@Override
	protected void onStart() {
		sliding_iv_qqname.setText(ChatApplication.info.nickname == null ? ChatApplication.info.username : ChatApplication.info.nickname);
		sliding_tv_sign.setText(ChatApplication.info.motto == null ? "" : ChatApplication.info.motto);
		super.onStart();
	}

	public void initViews() {
		activity_slidingmenu = LayoutInflater.from(this).inflate(R.layout.activity_slidingmenu, null);
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		sliding_iv_qqname = (TextView) activity_slidingmenu.findViewById(R.id.sliding_iv_qqname);
		sliding_tv_sign = (TextView) activity_slidingmenu.findViewById(R.id.sliding_tv_sign);
		sliding_iv_headimage = (ImageView) activity_slidingmenu.findViewById(R.id.sliding_iv_headimage);
	}
	private ViewPager.OnPageChangeListener onPageChangeListener=new ViewPager.OnPageChangeListener(){
		//让viewpage第一页时滑动出slidingmenu,第二页时滑动懂到第一页
		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0://只有在首页才能滑出slidingmenu
				sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				break;
			default:
				sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
			}
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

	};
	private void initSlidingMenu() {
		//实例化一个SlidingMenu
		sm=new SlidingMenu(this);
		//SlidingMenu出现的方向为左边
		sm.setMode(SlidingMenu.LEFT);
		//设置主页面和滑动页面间的阴影图片
		sm.setShadowDrawable(R.drawable.shadow);
		//设置阴影的宽度
		sm.setShadowWidth(R.dimen.shadow_width);
		//设置主页面的宽度
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		//当前的activity作为activity 的一部分
		sm.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		//slidingmenu显示的内容
		sm.setMenu(activity_slidingmenu);
		new ImageLoadTask().execute(ChatApplication.info.headicon);
	} 
	//滑动的activity装成viewpager
	private void initActivities() {
		List<View> viewlists=new ArrayList<View>();
		
		Intent intent1=new Intent(this,NavigationActivity.class);
		View mainview = lam.startActivity("NavigationActivity", intent1).getDecorView();
		viewlists.add(mainview);
		
		Intent intent2=new Intent(this,AllThemeActivity.class);
		View iaskview = lam.startActivity("AllThemeActivity", intent2).getDecorView();
		viewlists.add(iaskview);
		
//		view放到viewpager的适配器中去显示
		ViewPagerAdapter adapter=new ViewPagerAdapter(viewlists);
//		Log.i(TAG, viewlists+"======");
		viewpager.setAdapter(adapter);
	}
	
	public void changeSign(View view){
		Toast.makeText(this, "请连接网络", 0).show();
	}
	
	public void setMyMessage(View view){
		Intent intent=new Intent(this, MyMessageActivity.class);
		startActivity(intent);
	}
	
	public void mySetting(View view){
		Intent intent=new Intent(this, MySettingActivity.class);
		startActivity(intent);
	}
	
	public void uploadFile(View view){
		Intent intent=new Intent(this,BarCodeTestActivity.class);
		startActivity(intent);
	}
	
	public void setMyLocation(View view){
		Intent intent=new Intent(this, MyLocationActivity.class);
		startActivity(intent);
	}
	
	public void voiceRecognizer(View view){
		Intent intent=new Intent(this, VoiceRecognizerActivity.class);
		startActivity(intent);
	}
	
	public void exit(View view){
		String msg_exit = "exit@";
		ThreadService.thread.sendMessage(msg_exit);
	}

	@Override
	protected void onDestroy() {
		if (ThreadService.thread == null) {
			ThreadService.thread = ThreadService.getDefaul(this);
		}
		ThreadService.thread.context = this;
		String msg_exit = "exit@";
		ThreadService.thread.sendMessage(msg_exit);
		super.onDestroy();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = "http://" + ChatApplication.HOSTADDRESS +":8080/WeiChat/images/" + params[0];
			Bitmap drawable = ImageLoader.loadImage(url);// 获取网络图片
			return drawable;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}
			sliding_iv_headimage.setImageBitmap(result);
		}

	}
}
