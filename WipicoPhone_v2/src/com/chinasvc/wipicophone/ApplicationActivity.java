package com.chinasvc.wipicophone;

import java.util.ArrayList;
import java.util.List;

import com.chinasvc.wipicophone.adapter.GameIndexAdapter;
import com.chinasvc.wipicophone.adapter.VideoIndexAdapter;
import com.chinasvc.wipicophone.adapter.ViewFlowAdapter;
import com.chinasvc.wipicophone.bean.AppInfo;
import com.chinasvc.wipicophone.bean.GameBean;
import com.chinasvc.wipicophone.config.BroadcastConfig;
import com.chinasvc.wipicophone.db.GameDao;
import com.chinasvc.wipicophone.widget.CircleFlowIndicator;
import com.chinasvc.wipicophone.widget.ScrollGridView;
import com.chinasvc.wipicophone.widget.ViewFlow;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ApplicationActivity extends ControlBaseActivity implements OnItemClickListener, OnClickListener {

	private ViewFlow viewFlow; // 进行图片轮播的viewFlow

	private List<AppInfo> listDatas = null;
	private ScrollGridView mVideoGridView;
	private ScrollGridView mGameGridView;

	private List<GameBean> listGames = new ArrayList<GameBean>();

	private GameDao gameDao = new GameDao();;

	private ImageView actionbar_scan, actionbar_state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_application);
		((TextView) findViewById(R.id.actionbar_title)).setText(R.string.actionbar_title_application);

		initThreadHandler();

		registerReceiver();
		initData();

		initView();
	}

	private void initData() {
		listGames = gameDao.fetchAll();
		initVideoData();
	}

	private void initVideoData() {
		listDatas = new ArrayList<AppInfo>();
		AppInfo youku = new AppInfo();
		youku.setName(getString(R.string.application_video_youku));
		youku.setInstall(checkApkExist("com.youku.phone"));
		listDatas.add(youku);

		AppInfo pps = new AppInfo();
		pps.setName(getString(R.string.application_video_pps));
		pps.setInstall(checkApkExist("tv.pps.mobile"));
		listDatas.add(pps);

		AppInfo tencent = new AppInfo();
		tencent.setName(getString(R.string.application_video_tx));
		tencent.setInstall(checkApkExist("com.tencent.qqlive"));
		listDatas.add(tencent);

		AppInfo pptv = new AppInfo();
		pptv.setName(getString(R.string.application_video_pptv));
		pptv.setInstall(checkApkExist("com.pplive.androidphone"));
		listDatas.add(pptv);

		AppInfo sohu = new AppInfo();
		sohu.setName(getString(R.string.application_video_sohu));
		sohu.setInstall(checkApkExist("com.sohu.sohuvideo"));
		listDatas.add(sohu);
	}

	private VideoIndexAdapter mVideoIndexAdapter;

	private void initView() {
		actionbar_scan = (ImageView) findViewById(R.id.actionbar_scan);
		actionbar_scan.setOnClickListener(this);
		actionbar_state = (ImageView) findViewById(R.id.actionbar_state);

		mVideoGridView = (ScrollGridView) findViewById(R.id.mVideoGridView);
		mVideoGridView.setOnItemClickListener(this);
		mVideoIndexAdapter = new VideoIndexAdapter(this, listDatas);
		mVideoGridView.setAdapter(mVideoIndexAdapter);

		mGameGridView = (ScrollGridView) findViewById(R.id.mGameGridView);
		mGameGridView.setOnItemClickListener(this);
		mGameGridView.setAdapter(new GameIndexAdapter(this, listGames));

		viewFlow = (ViewFlow) findViewById(R.id.viewflow);// 获得viewFlow对象
		viewFlow.setAdapter(new ViewFlowAdapter(getApplicationContext())); // 对viewFlow添加图片
		viewFlow.setmSideBuffer(3);
		CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic); // viewFlow下的indic
		viewFlow.setFlowIndicator(indic);

		viewFlow.setTimeSpan(3000);
		viewFlow.setSelection(3 * 1000); // 设置初始位置
		viewFlow.startAutoFlowTimer(); // 启动自动播放
	}

	/** 注册广播 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastConfig.ACTION_DEVICE_CONNECTED);
		filter.addAction(BroadcastConfig.ACTION_DEVICE_DISCONNECTED);
		registerReceiver(broadcastReceiver, filter);
	}

	/** 注销广播 */
	private void unregisterReceiver() {
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onResume() {
		if (MainActivity.mDevice != null) {
			actionbar_state.setImageResource(R.drawable.ic_ab_scan_connected);
		} else {
			actionbar_state.setImageResource(R.drawable.ic_ab_scan_disconnected);
		}
		super.onResume();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == BroadcastConfig.ACTION_DEVICE_CONNECTED) {
				actionbar_state.setImageResource(R.drawable.ic_ab_scan_connected);
			} else if (intent.getAction() == BroadcastConfig.ACTION_DEVICE_DISCONNECTED) {
				actionbar_state.setImageResource(R.drawable.ic_ab_scan_disconnected);
			}
		}
	};

	private boolean checkApkExist(String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (arg0 == mVideoGridView) {
			AppInfo bean = listDatas.get(position);
			switch (position) {
			case 0:
				if (bean.isInstall()) {
					try {
						Intent mIntent = new Intent();
						ComponentName comp = new ComponentName("com.youku.phone", "com.youku.phone.ActivityWelcome");
						mIntent.setComponent(comp);
						mIntent.setAction("android.intent.action.VIEW");
						startActivity(mIntent);
					} catch (ActivityNotFoundException e) {
						// TODO: handle exception
					}
				} else {
					Uri webpage = Uri.parse("http://mobile.youku.com/index/wireless");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
				}
				break;
			case 1:
				if (bean.isInstall()) {
					try {
						Intent mIntent = new Intent();
						ComponentName comp = new ComponentName("tv.pps.mobile", "tv.pps.mobile.WelcomeActivity");
						mIntent.setComponent(comp);
						mIntent.setAction("android.intent.action.VIEW");
						startActivity(mIntent);
					} catch (ActivityNotFoundException e) {
						// TODO: handle exception
					}
				} else {
					Uri webpage = Uri.parse("http://dl.pps.tv/pps_android_download.html");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
				}
				break;
			case 2:
				if (bean.isInstall()) {
					try {
						Intent mIntent = new Intent();
						ComponentName comp = new ComponentName("com.tencent.qqlive", "com.tencent.qqlive.activity.WelcomeActivity");
						mIntent.setComponent(comp);
						mIntent.setAction("android.intent.action.VIEW");
						startActivity(mIntent);
					} catch (ActivityNotFoundException e) {
						// TODO: handle exception
					}
				} else {
					Uri webpage = Uri.parse("http://v.qq.com/download_mobile.html");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
				}
				break;
			case 3:
				if (bean.isInstall()) {
					try {
						Intent mIntent = new Intent();
						ComponentName comp = new ComponentName("com.pplive.androidphone", "com.pplive.androidphone.ui.FirstActivity");
						mIntent.setComponent(comp);
						mIntent.setAction("android.intent.action.VIEW");
						startActivity(mIntent);
					} catch (ActivityNotFoundException e) {
						// TODO: handle exception
					}
				} else {
					Uri webpage = Uri.parse("http://app.pptv.com/android/");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
				}
				break;
			case 4:
				if (bean.isInstall()) {
					try {
						Intent mIntent = new Intent();
						ComponentName comp = new ComponentName("com.sohu.sohuvideo", "com.sohu.sohuvideo.FirstNavigationActivityGroup");
						mIntent.setComponent(comp);
						mIntent.setAction("android.intent.action.VIEW");
						startActivity(mIntent);
					} catch (ActivityNotFoundException e) {
						// TODO: handle exception
					}
				} else {
					Uri webpage = Uri.parse("http://app.yule.sohu.com/tv-android.html");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
				}
				break;
			}
		} else if (arg0 == mGameGridView) {
			if (MainActivity.mDevice != null) {
				mThreadHandler.obtainMessage(HOME).sendToTarget();
				Message message = new Message();
				message.what = START_GAME;
				message.obj = listGames.get(position).getGamePackage();
				mThreadHandler.sendMessageDelayed(message, 1000);

				Intent intent = new Intent();
				intent.setClass(this, GameControlActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.msg_no_device, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == actionbar_scan) {
			Intent intent = new Intent();
			intent.setAction(BroadcastConfig.BROADCAST_DEVICE_CONNECTING);
			sendBroadcast(intent);
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}
}
