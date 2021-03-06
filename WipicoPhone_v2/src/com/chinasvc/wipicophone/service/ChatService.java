package com.chinasvc.wipicophone.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.chinasvc.wipicophone.R;
import com.chinasvc.wipicophone.WipicoApplication;
import com.chinasvc.wipicophone.util.FileTools;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

/**
 * 主要用来接收UDP消息，存储在messages中，当有消息来时，通知activity来获取
 */
public class ChatService extends Service implements OnUDPReceiveMessage {

	private String TAG = "ChatService";
	public boolean isDebug = false;

	private final MyBinder myBinder = new MyBinder();

	// 保存当前在线用户，键值为用户的ip
	final Map<String, User> users = new ConcurrentHashMap<String, User>();
	// 保存用户发的消息，每个ip都会开启一个消息队列来缓存消息
	final Map<String, Queue<UDPMessage>> messages = new ConcurrentHashMap<String, Queue<UDPMessage>>();
	// UDP消息监听接收器
	private UDPMessageListener listener = UDPMessageListener.getInstance(users, messages);

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			listener.setOnReceiveMessage(this);
			listener.open(); // 通知上线了
			new CheckUser().start();
			new HeartBeat().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected final void send(String msg, InetAddress destIp) {
		listener.send(msg, destIp);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	/**
	 * 自定义的Binder类， 通过这个类，让Activity获得与其绑定的Service对象
	 */
	public final class MyBinder extends Binder {
		/** 获得当前用户列表 */
		public Map<String, User> getUsers() {
			return users;
		}

		/** 获得当前缓存消息 */
		public Map<String, Queue<UDPMessage>> getMessages() {
			return messages;
		}

		/** 发送消息 */
		public void sendMsg(UDPMessage msg, InetAddress destIp) {
			send(msg.toString(), destIp);
		}

		/** 通知上线 */
		public void noticeOnline() {
			listener.noticeOnline();
		}

		/**
		 * 发送文件
		 * */
		public void sendFile(String path, User user) {
			send(path, user);
		}
	}

	private boolean isRunning = true;

	private class CheckUser extends Thread {
		Map<String, User> tempUser = new ConcurrentHashMap<String, User>();

		@Override
		public void run() {
			while (isRunning) {
				tempUser.clear();
				tempUser.putAll(users);
				Set<Entry<String, User>> set = tempUser.entrySet();
				for (Entry<String, User> entry : set) {
					long heart = System.currentTimeMillis() - Long.parseLong(entry.getValue().getHeartTime());
					if (heart >= 15000) {
						users.remove(entry.getKey());
						sendBroadcast(new Intent(Config.ACTION_REMOVE_USER));
					}
				}
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isHeartRunning = true;

	private class HeartBeat extends Thread {
		@Override
		public void run() {
			while (isHeartRunning) {
				listener.noticeOnline();
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		isRunning = false;
		isHeartRunning = false;
		if (fileListener != null && fileListener.isRunning()) {
			try {
				fileListener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			listener.close();// 通知下线
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onReceive(int type) {
		switch (type) {
		case Listener.LOGIN_SUCC:
			sendBroadcast(new Intent(Config.ACTION_LOGIN_SUCC));
			break;
		case Listener.ADD_USER:
			sendBroadcast(new Intent(Config.ACTION_ADD_USER));
			break;
		case Listener.REMOVE_USER:
			sendBroadcast(new Intent(Config.ACTION_REMOVE_USER));
			break;
		case Listener.ASK_VIDEO:
			break;
		case Listener.REPLAY_VIDEO_ALLOW:
			break;
		case Listener.REPLAY_VIDEO_NOT_ALLOW:
			break;
		case Listener.TO_ALL_MESSAGE:
			// 请求群发消息
			sendBroadcast(new Intent(Config.ACTION_NOTIFY_DATA));
			break;
		}
	}

	public static final String ACTION_ADD_SEND_TASK = "com.chinasvc.wipico.ACTION_ADD_SEND_TASK";
	public static final String ACTION_ADD_RECIVER_TASK = "com.chinasvc.wipico.ACTION_ADD_RECIVER_TASK";
	public static final String ACTION_CANCEL_RECEVIE_FILE = "com.chinasvc.wipico.ACTION_CANCEL_RECEVIE_FILE";
	public static final String ACTION_CANCEL_SEND_FILE = "com.chinasvc.wipico.ACTION_CANCEL_SEND_FILE";

	@Override
	public void onReceive(int type, UDPMessage udpMessage) {
		// TODO Auto-generated method stub
		switch (type) {
		case Listener.REPLAY_SEND_FILE:
			// 对方回复发送文件结果
			try {
				String msg = udpMessage.getMsg();
				FileMessage fileMessage = new FileMessage(new JSONObject(msg));
				if (FileMessage.ALLOW_SEND_FILE.equals(fileMessage.getAllow())) {
					// 同意发送文件
					// TODO 添加到任务队列
					Intent intent = new Intent(ACTION_ADD_SEND_TASK);
					intent.putExtra("user_name", udpMessage.getSenderName());
					intent.putExtra("user_ip", udpMessage.getIp());
					intent.putExtra("file_msg", udpMessage.getMsg());
					sendBroadcast(intent);
					// fileListener.sendFile(udpMessage.getIp(),
					// new File(fileMessage.getFilePath()),
					// WipicoApplication.appInstance.getFilePath(),
					// null);
					// 发送文件
				} else {
					// 不同意发送文件
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case Listener.ASK_SEND_FILE:
			// 对方请求发送文件
			try {
				final FileMessage fileMessage = new FileMessage(new JSONObject(udpMessage.getMsg()));
				// 接受发送文件
				if (FileTools.isExternalStorageAvailable() && FileTools.freeSpaceOnSd() > 1) {
					fileMessage.setAllow(FileMessage.ALLOW_SEND_FILE);
				} else {
					fileMessage.setAllow(FileMessage.NOT_ALLOW_SEND_FILE);
					Toast.makeText(getApplicationContext(), R.string.msg_insufficient_storage_space_in, Toast.LENGTH_SHORT).show();
				}
				String localPath = WipicoApplication.appInstance.getFilePath() + fileMessage.getName();
				if (FileTools.isFileExist(localPath)) {
					// 如果文件存在则删除
					FileTools.delete(new File(localPath));
				}
				fileListener = TCPFileListener.getInstance();
				if (!fileListener.isRunning()) {
					try {
						fileListener.open();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				fileMessage.setType(FileMessage.RECEIVE);
				UDPMessage message = WipicoApplication.appInstance.getMyUdpMessage(fileMessage.toString(), Listener.REPLAY_SEND_FILE);
				try {
					send(message.toString(), InetAddress.getByName(udpMessage.getIp()));
					fileMessage.setFilePath(localPath);
					// 设置为本地的保存路径
					// TODO 添加任务队列
					Intent intent = new Intent(ACTION_ADD_RECIVER_TASK);
					intent.putExtra("user_name", udpMessage.getSenderName());
					intent.putExtra("user_ip", udpMessage.getIp());
					intent.putExtra("file_msg", fileMessage.toString());
					sendBroadcast(intent);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case Listener.CANCEL_RECEIVE_FILE:
			Intent intent = new Intent(ACTION_CANCEL_RECEVIE_FILE);
			intent.putExtra("user_ip", udpMessage.getIp());
			intent.putExtra("file_msg", udpMessage.getMsg());
			sendBroadcast(intent);
			break;
		case Listener.CANCEL_SEND_FILE:
			Intent intent1 = new Intent(ACTION_CANCEL_SEND_FILE);
			intent1.putExtra("user_ip", udpMessage.getIp());
			intent1.putExtra("file_msg", udpMessage.getMsg());
			sendBroadcast(intent1);
			break;
		}
	}

	private TCPFileListener fileListener;

	/**
	 * 发送文件
	 * */
	private void send(String path, User user) {
		// TODO 请求发送文件
		File file = new File(path);
		if (!file.exists()) {
			Toast.makeText(this, R.string.msg_no_this_file, Toast.LENGTH_SHORT).show();
			return;
		}

		fileListener = TCPFileListener.getInstance();
		if (!fileListener.isRunning()) {
			try {
				fileListener.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileMessage fileMessage = new FileMessage(file);
		fileMessage.setType(FileMessage.SEND);
		UDPMessage msg = WipicoApplication.appInstance.getMyUdpMessage(fileMessage.toString(), Listener.ASK_SEND_FILE);
		try {
			// 发送UDP 文件发送请求
			send(msg.toString(), InetAddress.getByName(user.getIp()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendFailure() {
		// UDP发送失败
	}

}
