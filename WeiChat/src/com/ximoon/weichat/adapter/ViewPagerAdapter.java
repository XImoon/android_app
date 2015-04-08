package com.ximoon.weichat.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;


public class ViewPagerAdapter extends PagerAdapter {
	private List<View> list;
	public ViewPagerAdapter(List<View> list){
		this.list=list;
	}
//	快捷键 shift+alt+j
	//总的数量  
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	
	public boolean isViewFromObject(View arg0, Object arg1 ) {
		// TODO Auto-generated method stub
		return arg0==arg1;
	}
//item销毁时
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.destroyItem(container, position, object);
		ViewPager viewPager=(ViewPager)container;
		View view=list.get(position);
		viewPager.removeView(view);
	}
//item被生成的时候
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		ViewPager viewPager= (ViewPager) container;
		View view=list.get(position);
		viewPager.addView(view);
		return view;
	}
	
	
}
