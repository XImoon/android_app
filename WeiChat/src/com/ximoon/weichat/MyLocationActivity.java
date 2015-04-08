package com.ximoon.weichat;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKRoutePlan;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.ximoon.weichat.utils.Constants;

public class MyLocationActivity extends Activity {
		// 声明控件
		private TextView request;
		private Toast mToast = null;
		private BMapManager mBMapManager = null;
		private MapView mMapView = null; // MapView 是地图主控件
		private MapController mMapController = null;// 用MapController完成地图控制

		private LocationClient mLocClient;
		public LocationData mLocData = null;

		private LocationOverlay myLocationOverlay = null;// 定位图层
		private boolean isRequest = false;// 是否手动触发请求定位
		private boolean isFirstLoc = true;// 是否首次定位

		private PopupOverlay mPopupOverlay = null;// 弹出泡泡图层，浏览节点时使用
		private View viewCache = null;
		public BDLocation location = new BDLocation();

		// 画线
		private MKSearch mMKSearch = null;
		private GeoPoint startGp, endGp;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			checkNet();
			setContentView(R.layout.activity_mylocation); // activity的布局
			// 初始化
			init();
			// 单击事件
			click();
		}
		
		private void checkNet() {
			// **使用地图sdk前需先初始化BMapManager，这个必须在setContentView()先初始化
			mBMapManager = new BMapManager(this);
			// 第一个参数是API key, 第二个参数是常用事件监听，用来处理通常的网络错误，授权验证错误等，你也可以不添加这个回调接口
			mBMapManager.init(Constants.MAP_KEY, new MKGeneralListener() {
				// 授权错误的时候调用的回调函数
				@Override
				public void onGetPermissionState(int iError) {
					if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
						showToast("百度KEY错误, 请检查！");
					}
				}

				// 一些网络状态的错误处理回调函数
				@Override
				public void onGetNetworkState(int iError) {
					if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
						Toast.makeText(getApplication(), "网络出问题啦！",
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		// * 显示Toast消息
		private void showToast(String msg) {
			if (mToast == null) {
				mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
			} else {
				mToast.setText(msg);
				mToast.setDuration(Toast.LENGTH_SHORT);
			}
			mToast.show();
		}

		private void click() {
			request.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					requestLocation();
				}
			});

		}

		@Override
		protected void onResume() {
			// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
			mMapView.onResume();
			mBMapManager.start();// 重新启动
			super.onResume();
		}

		@Override
		protected void onPause() {
			// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
			mMapView.onPause();
			super.onPause();
		}

		private void init() {
			System.out.println("===出来");
			// 使用自定义的title，注意顺序
			//这里是添加自己定义的titlebtn.xml
			// 通过id找到他们
			mMapView =(MapView) findViewById(R.id.mapView);
			mMapController = mMapView.getController(); // 获取地图控制器
			mMapController.enableClick(true); // 设置地图是否响应点击事件
			request = (TextView) findViewById(R.id.send_location);

			viewCache = LayoutInflater.from(this)
					.inflate(R.layout.pop_layout, null);
			mPopupOverlay = new PopupOverlay(mMapView, new PopupClickListener() {// *
																		// 点击弹出窗口图层回调的方法
						@Override
						public void onClickedPopup(int arg0) {
							// 隐藏弹出窗口图层
							mPopupOverlay.hidePop();

						}
					});

			mMapController.enableClick(true); // * 设置地图是否响应点击事件 .
			mMapController.setZoom(12); // * 设置地图缩放级别
			mMapView.setBuiltInZoomControls(true); // * 显示内置缩放控件
			mMapView.setTraffic(true);//交通图

			mLocData = new LocationData();

			
			mLocClient = new LocationClient(getApplicationContext()); // * 定位SDK的核心类
			System.out.println("hahhhhahah======");
			// 实例化定位服务，LocationClient类必须在主线程中声明
			mLocClient.registerLocationListener(new BDLocationListenerImpl());// 注册定位监听接口
			/**
			 * 设置定位参数
			 */
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true); // 打开GPRS
			option.setAddrType("all");// 返回的定位结果包含地址信息
			option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
			option.setScanSpan(3000); // 设置发起定位请求的间隔时间为3000ms
			option.disableCache(false);// 禁止启用缓存定位
			option.setPoiNumber(5); // 最多返回POI个数
			option.setPoiDistance(3000); // poi查询距离
			option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息

			mLocClient.setLocOption(option);
			mLocClient.start(); // 调用此方法开始定位

			myLocationOverlay = new LocationOverlay(mMapView);// 定位图层初始化

			// 将定位数据设置到定位图层里

			myLocationOverlay.setMarker(getResources().getDrawable(R.drawable.set));
			// 添加定位图层
			mMapView.getOverlays().add(myLocationOverlay);
			myLocationOverlay.enableCompass();

			// 更新图层数据执行刷新后生效
			mMapView.refresh();

		mMapView.regMapViewListener(mBMapManager, new MKMapViewListener() {
				private MKPlanNode endNode;
				private MKPlanNode startNode;

			// * 地图移动完成时会回调此接口 方法
				@Override
			public void onMapMoveFinish() {
					showToast("地图移动中……");
				}

				// * 地图加载完毕回调此接口方法
				@Override
				public void onMapLoadFinish() {
					showToast("地图载入完毕！");
				}

				// * 地图完成带动画的操作（如: animationTo()）后，此回调被触发
				@Override
				public void onMapAnimationFinish() {

				}

				// 当调用过 mMapView.getCurrentMap()后，此回调会被触发 可在此保存截图至存储设备
				@Override
				public void onGetCurrentMap(Bitmap arg0) {
				}

				// * 点击地图上被标记的点回调此方法
				@Override
				public void onClickMapPoi(MapPoi end) {
					if (end != null) {

						// 起点
						startGp = new GeoPoint(
								(int) (location.getLatitude() * 1e6),
								(int) (location.getLongitude() * 1e6));
						startNode = new MKPlanNode();
						startNode.name = location.getAddrStr();
						startNode.pt = startGp;

						// 终点
						endGp = end.geoPt;
						endNode = new MKPlanNode();
						endNode.name = end.strText;
						endNode.pt = endGp;

						showToast(endNode.name + "这是终点!");
						System.out.println(endNode.name
								+ "这是终点!=========================");

						mMKSearch = new MKSearch();
						mMKSearch.init(mBMapManager, new MKSearchListener() {

							@Override
							public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
							}

							@Override
							public void onGetBusDetailResult(MKBusLineResult arg0,
									int arg1) {
							}

							// 驾车方案
							@Override
							public void onGetDrivingRouteResult(
									MKDrivingRouteResult result, int iError) {
								showToast("进入画线==");
								System.out.println("进入画线"
										+ "========================");
								// 画路线
								RouteOverlay routeOverlay = new RouteOverlay(
										MyLocationActivity.this, mMapView);

								int numPlan = result.getNumPlan();
								if (result != null && numPlan > 0) {
									MKRoutePlan plan = result.getPlan(0);
									MKRoute route = plan.getRoute(0);
									routeOverlay.setData(route);
									mMapView.getOverlays().add(routeOverlay);
									mMapView.refresh();
								}

								mMKSearch.setDrivingPolicy(MKSearch.ECAR_DIS_FIRST);
								mMKSearch.drivingSearch(startNode.name, startNode,
										endNode.name, endNode);
								showToast("完成路线啦====");
								System.out.println("完成路线啦"
										+ "========================");

							}

							@Override
							public void onGetPoiDetailSearchResult(int arg0,
									int arg1) {
							}

							@Override
							public void onGetPoiResult(MKPoiResult arg0, int arg1,
									int arg2) {
							}

							@Override
							public void onGetShareUrlResult(MKShareUrlResult arg0,
									int arg1, int arg2) {
							}

							@Override
							public void onGetSuggestionResult(
									MKSuggestionResult arg0, int arg1) {
							}

							@Override
							public void onGetTransitRouteResult(
									MKTransitRouteResult arg0, int arg1) {
							}

							@Override
							public void onGetWalkingRouteResult(
									MKWalkingRouteResult arg0, int arg1) {
							}

						});

					}
				}

			});

		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			CreateMenu(menu);
			return true;
		}

		private void CreateMenu(Menu menu) {
			MenuItem mnu1 = menu.add(0, 0, 0, "显示卫星地图");
			{
				mnu1.setAlphabeticShortcut('a');// 设置快捷键
				// mnu1.serIcon(R.drawable.icon);//设置图片
			}
			MenuItem mnu2 = menu.add(0, 1, 1, "显示街道地图");
			{
				mnu2.setAlphabeticShortcut('b');// 设置快捷键
				// mnu1.serIcon(R.drawable.icon);//设置图片
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == 0) {
				mMapView.setSatellite(true); // 设置显示为卫星地图：
				mMapView.setTraffic(false);
			} else if (item.getItemId() == 1) {
				mMapView.setTraffic(true); // 显示街道地图
				mMapView.setSatellite(false);
			}
			return true;
		}

		public class BDLocationListenerImpl implements BDLocationListener {
			// * 接收异步返回的定位结果，参数是BDLocation类型参数
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null) {
					return;
				}
				MyLocationActivity.this.location = location;

				mLocData.latitude = location.getLatitude();
				mLocData.longitude = location.getLongitude();
				// 如果不显示定位精度圈，将accuracy赋值为0即可
				mLocData.accuracy = location.getRadius();
				mLocData.direction = location.getDerect();

				// 将定位数据设置到定位图层里
				myLocationOverlay.setData(mLocData);
				// 更新图层数据执行刷新后生效
				mMapView.refresh();

				if (isFirstLoc || isRequest) {
					// 将给定的位置点以动画形式移动至地图中心
					mMapController.animateTo(new GeoPoint((int) (location
							.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6)));
					showPopupOverlay(location); // 载入时候就弹出
					isRequest = false;
				}
				isFirstLoc = false;

			}

			// 接收异步返回的POI查询结果，参数是BDLocation类型参数
			@Override
			public void onReceivePoi(BDLocation poiLocation) {

			}

		}

		private void requestLocation() {
			isRequest = true;
			if (mLocClient != null && mLocClient.isStarted()) {
				Toast.makeText(getApplicationContext(), "定位中", 0).show();
				mLocClient.requestLocation();
			}
		}

		// 继承MyLocationOverlay重写dispatchTap方法
		private class LocationOverlay extends MyLocationOverlay {

			public LocationOverlay(MapView arg0) {
				super(arg0);
			}

			// * 在“我的位置”坐标上处理点击事件。
			@Override
			protected boolean dispatchTap() {
				// 点击我的位置显示PopupOverlay
				showPopupOverlay(location);
				return super.dispatchTap();
			}

			@Override
			public void setMarker(Drawable arg0) {
				super.setMarker(arg0);
			}

		}

		@Override
		protected void onDestroy() {
			// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
			mMapView.destroy();

			// 退出应用调用BMapManager的destroy()方法
			if (mBMapManager != null) {
				mBMapManager.destroy();
				mBMapManager = null;
			}

			// 退出时销毁定位
			if (mLocClient != null) {
				mLocClient.stop();
			}

			super.onDestroy();
		}

		// * 显示弹出窗口图层PopupOverlay
		private void showPopupOverlay(BDLocation location) {
			TextView popText = ((TextView) viewCache
					.findViewById(R.id.location_tips));
			popText.setText("[我的位置]\n" + location.getAddrStr());
			mPopupOverlay.showPopup(getBitmapFromView(popText),
					new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6)), 15);

		}

		// * 将View转换成Bitmap的方法
		public static Bitmap getBitmapFromView(View view) {
			view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
			view.buildDrawingCache();
			Bitmap bitmap = view.getDrawingCache();
			return bitmap;
		}
}
