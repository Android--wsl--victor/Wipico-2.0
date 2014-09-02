package com.chinasvc.wipicophone;

import com.chinasvc.wipico.type.Control;
import com.chinasvc.wipicophone.widget.DirectionView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.MotionEvent.PointerCoords;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class OfficeControlActivity extends ControlBaseActivity implements OnTouchListener, OnClickListener {

	/** 触摸板 */
	private DirectionView touchArea;

	private int sendid = 0;
	private int resultX = 0;
	private int resultY = 0;

	private static final int DEVICE_WIDTH = 854;
	private static final int DEVICE_HEIGHT = 480;

	private Button prePage, nextPage;

	private ImageView btnStop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_office_control);
		((TextView) findViewById(R.id.actionbar_title)).setText(R.string.actionbar_title_office_control);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initWidget();
		// 初始化监听处理Handler
		initThreadHandler();
	}

	private void initWidget() {
		touchArea = (DirectionView) findViewById(R.id.touch_area);
		touchArea.setOnTouchListener(this);

		btnStop = (ImageView) findViewById(R.id.actionbar_stop);
		btnStop.setOnClickListener(this);

		prePage = (Button) findViewById(R.id.btnPrev);
		prePage.setOnClickListener(this);
		nextPage = (Button) findViewById(R.id.btnNext);
		nextPage.setOnClickListener(this);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float fX = event.getX();
		float fY = event.getY();
		int pointers = event.getPointerCount();
		Message mMessage = new Message();
		mMessage.what = TOUCH;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchArea.onViewTouch(fX, fY);
			mMessage.arg1 = Control.TOUCH_DOWN;
			sendid = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			touchArea.onViewTouch(fX, fY);
			mMessage.arg1 = Control.TOUCH_MOVE;
			break;
		case MotionEvent.ACTION_UP:
			touchArea.onViewTouch(-100, -100);
			mMessage.arg1 = Control.TOUCH_UP;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mMessage.arg1 = Control.TOUCH_POINTUP;
			break;
		default:
			break;
		}
		sendid++;
		Bundle mBundle = new Bundle();
		mMessage.arg2 = pointers + (sendid << 8);
		if (getAndroidSDKVersion() > 8) {
			PointerCoords pointerCoords = new PointerCoords();
			for (int i = 0; i < pointers; i++) {
				event.getPointerCoords(i, pointerCoords);
				resultX = (int) (((pointerCoords.x) / touchArea.getWidth()) * DEVICE_WIDTH);
				resultY = (int) (((pointerCoords.y - touchArea.getTop()) / touchArea.getHeight()) * DEVICE_HEIGHT);
				mBundle.putInt("x" + i, resultX);
				mBundle.putInt("y" + i, resultY);
				mBundle.putInt("id" + i, (int) (event.getPointerId(i)));
				mBundle.putInt("thm" + i, (int) (pointerCoords.touchMajor) / 10);
			}
		} else {
			resultX = (int) (((event.getX()) / touchArea.getWidth()) * DEVICE_WIDTH);
			resultY = (int) (((event.getY() - touchArea.getTop()) / touchArea.getHeight()) * DEVICE_HEIGHT);
			mBundle.putInt("x0", resultX);
			mBundle.putInt("y0", resultY);
			mBundle.putInt("id0", 0);
			mBundle.putInt("thm0", 1);
		}
		mMessage.setData(mBundle);
		mThreadHandler.sendMessage(mMessage);
		return true;
	}

	public static int getAndroidSDKVersion() {
		int version = 0;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return version;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == prePage) {
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT);
			mThreadHandler.obtainMessage(KEY, keyEvent).sendToTarget();
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.SOUND).sendToTarget();
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.VIBRATOR).sendToTarget();
		} else if (v == nextPage) {
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT);
			mThreadHandler.obtainMessage(KEY, keyEvent).sendToTarget();
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.SOUND).sendToTarget();
			((MainActivity) getParent()).mHandler.obtainMessage(MainActivity.VIBRATOR).sendToTarget();
		} else if (v == btnStop) {
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
			mThreadHandler.obtainMessage(KEY, keyEvent).sendToTarget();

			((MainActivity) getParent()).setNav(MainActivity.TAB_BUSINESS);
			((MainActivity) getParent()).mTabHost.setCurrentTabByTag(MainActivity.TAB_BUSINESS);

		}
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}

}
