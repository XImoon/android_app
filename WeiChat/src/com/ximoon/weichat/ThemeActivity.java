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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.ximoon.weichat.adapter.PersonBaseAdapter;
import com.ximoon.weichat.entity.PostInfo;
import com.ximoon.weichat.service.PersonSevice;
import com.ximoon.weichat.utils.DensityUtil;
import com.ximoon.weichat.utils.ThemeConstants;

public class ThemeActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private ListView lv_persons;
	private PersonBaseAdapter adapter;
	private String path2 = "http://192.168.191.1:8080/WeiChat/ParseToGsonServlet";
	private Button main_bt_theme;
	private LinearLayout subMenu;
	private LinearLayout allinfoes_layout;
	private Button main_back_btn;
	private Animation topUpAnim;
	private int headerViewsCount;
	private LinearLayout more_layout;
	private Intent intent;
	private String theme;
	protected List<PostInfo> persons;
	protected List<PostInfo> list_theme;
	private LayoutInflater mInflater;
	private Button theme_bt_add;
	private View footView;
	private ImageView theme_iv_themepic;
	private View headView;
	private static final int NET_SUCCESS = 0;
	private static final int NET_FAIL = 1;
	private static final int NET_NOT_DATA = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setContentView(R.layout.activity_theme);
		initViews();
		
		topDownAnim = AnimationUtils.loadAnimation(this, R.anim.top_down);
		topUpAnim = AnimationUtils.loadAnimation(this, R.anim.top_up);
		topDownAnim.setAnimationListener(topDownAnimListener);
		topUpAnim.setAnimationListener(topUpAnimListener);
		getData();
		// adapter.setLists(lists);
		lv_persons.setAdapter(adapter);
		footView=mInflater.inflate(R.layout.lv_foot_layout, null);
//		
		
		lv_persons.addFooterView(footView);
		((ViewGroup)lv_persons).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		theme_bt_add=(Button)footView.findViewById(R.id.theme_bt_add);
		theme_bt_add.setOnClickListener(this);
			
		
	}

	private void selectPic(ImageView theme_iv_themepic2, String theme2) {
		if(theme2.equals(ThemeConstants.SUPERSTAR_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry1);
		}else if(theme2.equals( ThemeConstants.NEWS_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry2);
		}else if(theme2.equals(ThemeConstants.HANDSOME_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry3);
		}else if(theme2.equals(ThemeConstants.POLICY_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry4);
		}else if(theme2.equals(ThemeConstants.JOKE_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry5);
		}else if(theme2.equals(ThemeConstants.ECONOMY_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry6);
		}else if(theme2.equals( ThemeConstants.BEAUTY_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry7);
		}else if(theme2.equals( ThemeConstants.PE_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry8);
		}else if(theme2.equals( ThemeConstants.KATY_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry9);
		}else if(theme2.equals( ThemeConstants.SHAYNE_THEME)){
			theme_iv_themepic2.setImageResource(R.drawable.perry10);
		}
		
	}

	private void getData() {
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

	private void initViews() {
		intent = getIntent();
		theme = intent.getStringExtra("theme");
		main_bt_theme = (Button) findViewById(R.id.theme_bt_theme);
		main_back_btn = (Button) findViewById(R.id.theme_back_btn);
		main_bt_theme.setText(theme + "吧");
		lv_persons = (ListView) findViewById(R.id.lv_persons);
		mInflater = LayoutInflater.from(getApplicationContext());
		headView=mInflater.inflate(R.layout.lv_head_layout,
				null);
		theme_iv_themepic=(ImageView)headView.findViewById(R.id.theme_iv_themepic);
		selectPic(theme_iv_themepic,theme);
		lv_persons.addHeaderView(headView);
		headerViewsCount = lv_persons.getHeaderViewsCount();
		adapter = new PersonBaseAdapter(this, headerViewsCount);
		lv_persons.setOnItemClickListener(this);
		main_back_btn.setOnClickListener(this);
		main_bt_theme.setOnClickListener(this);
		subMenu = (LinearLayout) findViewById(R.id.sub_menu);
		allinfoes_layout = (LinearLayout) findViewById(R.id.allinfoes_layout);
		allinfoes_layout.setOnClickListener(this);
		more_layout = (LinearLayout) findViewById(R.id.more_layout);
		more_layout.setOnClickListener(this);
		
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NET_SUCCESS:
				List<PostInfo> persons = (List<PostInfo>) msg.obj;
				doTheme(persons);
				adapter.setLists(list_theme);
				// 刷新
				adapter.notifyDataSetChanged();
				break;
			case NET_NOT_DATA:
				Toast.makeText(getApplicationContext(), "没有数据", 1).show();
				break;
			case NET_FAIL:
				Toast.makeText(getApplicationContext(), "网络连接错误", 1).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

		private void doTheme(List<PostInfo> persons) {
			
			list_theme = new ArrayList<PostInfo>();
			for (PostInfo userInfo : persons) {
				if (userInfo.theme.equals(theme)) {
					list_theme.add(userInfo);
				}
			}
		}
	};
	private boolean animState = true;
	private Animation topDownAnim;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.theme_bt_theme:
			//
			if (animState) {
				//
				animState = false;
				subMenu.setVisibility(View.VISIBLE);
				subMenu.startAnimation(topDownAnim);

			} else {
				//
				animState = true;
				subMenu.startAnimation(topUpAnim);
			}

			break;
		case R.id.allinfoes_layout:
			Intent intent =new Intent(getApplicationContext(),RecommandActivity.class);
			startActivity(intent);
			break;
		case R.id.more_layout:
			Toast.makeText(getApplicationContext(), "more layout 待开发", 0).show();
			break;
		case R.id.theme_back_btn:
			intent = new Intent(getApplicationContext(),
					ViewPageActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.theme_bt_add:
			intent = new Intent(getApplicationContext(),
					AddThemeActivity.class);
			intent.putExtra("theme", theme);
			startActivity(intent);
			break;
		
		

		default:
			break;
		}

	}

	private AnimationListener topDownAnimListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			/**
			 * 
			 */
			animation.setFillAfter(true);//
			//

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
							getApplicationContext(), 60));
			//
			params.setMargins(0,
					DensityUtil.dip2px(getApplicationContext(), 50), 0, 0);
			subMenu.clearAnimation();//
			subMenu.setLayoutParams(params);//

		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

	};
	private AnimationListener topUpAnimListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			animation.setFillAfter(true);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
							getApplicationContext(), 50));
			params.setMargins(0, 0, 0, 0);
			subMenu.clearAnimation();//
			subMenu.setLayoutParams(params);//
			subMenu.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

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
