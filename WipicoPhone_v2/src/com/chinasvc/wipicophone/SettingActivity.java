package com.chinasvc.wipicophone;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chinasvc.wipico.bean.WifiBean;
import com.chinasvc.wipico.util.WifiAdmin;
import com.chinasvc.wipicophone.config.BroadcastConfig;
import com.chinasvc.wipicophone.dialog.ConfirmDialog;
import com.chinasvc.wipicophone.dialog.PasswordDialog;
import com.chinasvc.wipicophone.dialog.WlanDialog;
import com.chinasvc.wipicophone.dialog.ConfirmDialog.DialogClickListener;
import com.chinasvc.wipicophone.dialog.WlanDialog.ConnectDialogListener;
import com.chinasvc.wipicophone.util.PreferenceUtil;
import com.chinasvc.wipicophone.widget.SlipButton;
import com.chinasvc.wipicophone.widget.SlipButton.OnChangedListener;

public class SettingActivity extends ControlBaseActivity implements OnClickListener, OnChangedListener {

	private View setting_connect, setting_disconnect, setting_wlan, setting_device_wlan;// setting_device_wlan
	private SlipButton voice, vibration;
	private View setting_help, setting_about;

	private TextView setting_connect_value;

	private ImageView actionbar_scan, actionbar_state;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		initThreadHandler();

		((TextView) findViewById(R.id.actionbar_title)).setText(R.string.actionbar_title_settings);

		initWidget();
		initValue();
		initView();

		registerReceiver();
	}

	private void initWidget() {
		setting_connect = findViewById(R.id.setting_connect);
		setting_connect.setOnClickListener(this);
		setting_disconnect = findViewById(R.id.setting_disconnect);
		setting_disconnect.setOnClickListener(this);
		setting_wlan = findViewById(R.id.setting_wlan);
		setting_wlan.setOnClickListener(this);
		setting_device_wlan = findViewById(R.id.setting_device_wlan);
		setting_device_wlan.setOnClickListener(this);

		setting_help = findViewById(R.id.setting_help);
		setting_help.setOnClickListener(this);
		setting_about = findViewById(R.id.setting_about);
		setting_about.setOnClickListener(this);

		voice = (SlipButton) findViewById(R.id.voice);
		voice.setOnChangedListener(this);
		vibration = (SlipButton) findViewById(R.id.vibration);
		vibration.setOnChangedListener(this);

		setting_connect_value = (TextView) findViewById(R.id.setting_connect_value);

		actionbar_scan = (ImageView) findViewById(R.id.actionbar_scan);
		actionbar_scan.setOnClickListener(this);
		actionbar_state = (ImageView) findViewById(R.id.actionbar_state);
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

	private void initValue() {
	}

	private void initView() {
		if (PreferenceUtil.getInstance(getApplicationContext()).getSound() == 1) {
			voice.setCheck(true);
		} else {
			voice.setCheck(false);
		}

		if (PreferenceUtil.getInstance(getApplicationContext()).getVibration() == 1) {
			vibration.setCheck(true);
		} else {
			vibration.setCheck(false);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == setting_connect || v == actionbar_scan) {
			Intent intent = new Intent();
			intent.setAction(BroadcastConfig.BROADCAST_DEVICE_CONNECTING);
			sendBroadcast(intent);
		} else if (v == setting_disconnect) {
			if (MainActivity.mDevice != null) {
				ConfirmDialog confirm = new ConfirmDialog(this);
				confirm.setCustomTitle(R.string.dialog_title_note);
				confirm.setMessage(getString(R.string.dialog_msg_disconnect) + " " + MainActivity.mDevice.getDeviceName() + "？");
				confirm.setClickListener(new DialogClickListener() {
					@Override
					public void onConfirmClickListener() {
						Intent intent = new Intent();
						intent.setAction(BroadcastConfig.BROADCAST_DEVICE_DISCONNECTED);
						sendBroadcast(intent);
					}

					@Override
					public void onCancelClickListener() {
					}
				});
				confirm.show();
			} else {
				PreferenceUtil.getInstance(getApplicationContext()).setDevice("");
				Toast.makeText(this, R.string.msg_no_device, Toast.LENGTH_SHORT).show();
			}
		} else if (v == setting_wlan) {
			startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
		} else if (v == setting_help) {
			List<ScanResult> lists = WifiAdmin.getInstance(SettingActivity.this).getScanResults();
			List<WifiBean> listWifis = new ArrayList<WifiBean>();
			try {
				JSONArray jsonArray = new JSONArray();
				for (ScanResult scanResult : lists) {
					listWifis.add(new WifiBean(scanResult.SSID, scanResult.capabilities));
					WifiBean bean = new WifiBean(scanResult.SSID, scanResult.capabilities);
					JSONObject jsonObject = new JSONObject(bean.toString());
					jsonArray.put(jsonObject);
				}
				// 转换数据格式
				String json = jsonArray.toString();
				Log.i("", "请求Wifi列表>>>>>>" + json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (v == setting_about) {
			Intent intent = new Intent();
			intent.setClass(this, AboutActivity.class);
			startActivity(intent);
		} else if (v == setting_device_wlan) {
			if (WifiAdmin.getInstance(this).isWifiEnable()) {
				if (MainActivity.mDevice != null) {
					WlanDialog dialog = new WlanDialog(SettingActivity.this, 0);
					dialog.setCustomTitle(R.string.setting_item_device_wlan);
					dialog.setConnectDialogListener(new ConnectDialogListener() {
						@Override
						public void onConfirmClickListener(ScanResult scanResult) {
							if (scanResult.capabilities.toLowerCase().indexOf("wep") != -1 || scanResult.capabilities.toLowerCase().indexOf("wpa") != -1) {
								// 有锁,弹出对话框输入密码
								PasswordDialog passwordDialog = new PasswordDialog(SettingActivity.this, scanResult);
								passwordDialog.setCustomTitle("请输入密码");
								passwordDialog.setClickListener(new PasswordDialog.DialogClickListener() {
									@Override
									public void onConfirmClickListener(String password, ScanResult scanResult) {
										Message message = new Message();
										message.what = CHANGE_WIFI;
										Bundle bundle = new Bundle();
										bundle.putString("password", password);
										message.setData(bundle);
										message.obj = scanResult;
										mThreadHandler.sendMessage(message);
									}

									@Override
									public void onCancelClickListener() {
									}
								});
								passwordDialog.show();
							} else {
								// 无锁,直接连接
								Message message = new Message();
								message.what = CHANGE_WIFI;
								Bundle bundle = new Bundle();
								bundle.putString("password", "");
								message.setData(bundle);
								message.obj = scanResult;
								mThreadHandler.sendMessage(message);
							}
						}

						@Override
						public void onCancelClickListener() {
						}
					});
					dialog.show();
				} else {
					Toast.makeText(this, R.string.msg_no_device, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, R.string.msg_no_net, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void OnChanged(boolean CheckState, View v) {
		if (v == voice) {
			if (CheckState) {
				PreferenceUtil.getInstance(getApplicationContext()).setSound(1);
			} else {
				PreferenceUtil.getInstance(getApplicationContext()).setSound(0);
			}
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.SOUND).sendToTarget();
		} else if (v == vibration) {
			if (CheckState) {
				PreferenceUtil.getInstance(getApplicationContext()).setVibration(1);
			} else {
				PreferenceUtil.getInstance(getApplicationContext()).setVibration(0);
			}
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.VIBRATOR).sendToTarget();
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}

}