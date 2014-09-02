package com.chinasvc.wipico.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import com.chinasvc.wipico.bean.ImageTransform;
import com.chinasvc.wipico.type.Keyboard;
import com.chinasvc.wipico.type.ShortCut;
import com.chinasvc.wipico.util.WipicoConstant;

/**
 * 操作发送类
 * 
 * @since 1.0.0
 * */
public class ActionSender {

	private static int SERVER_PORT = 32220;

	/**
	 * 发送数据
	 * 
	 * @param data
	 *                要发送的数据
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendData(byte data[], String ip) {
		InetAddress destIPAddress;
		try {
			destIPAddress = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			destIPAddress = null;
			return;
		}
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, destIPAddress, SERVER_PORT);
		DatagramSocket sendSocket = null;
		try {
			sendSocket = new DatagramSocket();
			sendSocket.setSoTimeout(2000);
			sendSocket.send(sendPacket);
		} catch (SocketException e) {
			// e.printStackTrace();
		} catch (SocketTimeoutException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
		if (sendSocket != null) {
			sendSocket.close();
		}
	}

	/**
	 * 发送快捷方式
	 * 
	 * @param shortCutsId
	 *                {@link ShortCut}
	 * @param ip
	 *                发往的ip地址
	 * */
	protected static void sendShortCuts(int shortCutsId, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(shortCutsId);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送键码
	 * 
	 * @param mkeycode
	 *                要发送的键码
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendCmd(int cmd, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送键盘
	 * 
	 * @param eventType
	 *                要发送的键码类型
	 * @param value
	 *                要发送的值
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendKeyBoard(int eventType, String value, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(Keyboard.CONTROL_KEYBOARD);
			oos.writeInt(eventType);
			oos.writeUTF(value);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送屏幕调整
	 * 
	 * @param eventType
	 *                要发送的调整类型
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendScreen(int eventType, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(WipicoConstant.SERVER_CMD_SCREEN);
			oos.writeInt(eventType);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送分辨率调整
	 * 
	 * @param eventType
	 *                要发送的调整类型
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendResolution(int eventType, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(WipicoConstant.SERVER_CMD_RESOLUTION);
			oos.writeInt(eventType);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 恢复出厂设置
	 * 
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendRecovery(String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(WipicoConstant.SERVER_CMD_RECOVERY);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送
	 * 
	 * @param cmd
	 *                指令
	 * @param path
	 *                /mnt/sdcard/PPT/xxx.ppt
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendOffice(int cmd, String path, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(path);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送媒体操作
	 * 
	 * @param cmd
	 *                指令
	 * @param itemCmd
	 *                附件指令
	 * @param value
	 *                值
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendMediaOperate(int cmd, int itmeCmd, int value, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeInt(itmeCmd);
			oos.writeInt(value);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送媒体操作
	 * 
	 * @param cmd
	 *                指令
	 * @param itemCmd
	 *                附件指令
	 * @param value
	 *                值
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendMediaOperate(int cmd, int itmeCmd, String value, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeInt(itmeCmd);

			String path = "";
			try {
				path = URLEncoder.encode(value, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			oos.writeUTF(path);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 图片变换
	 * 
	 * @param cmd
	 *                指令
	 * @param itemCmd
	 *                附件指令
	 * @param imageTransform
	 *                <{@link ImageTransform}>
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendImageTransform(int cmd, int itemCmd, ImageTransform imageTransform, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeInt(itemCmd);

			oos.writeInt(imageTransform.getFlag());// bundle.getInt(Image.BUNDLE_IMAGE_ACTION_FLAG)

			oos.writeFloat(imageTransform.getMiddleX());// bundle.getFloat(Image.BUNDLE_IMAGE_CURRENT_MIDDLEX)
			oos.writeFloat(imageTransform.getMiddleY());// bundle.getFloat(Image.BUNDLE_IMAGE_CURRENT_MIDDLEY)
			oos.writeFloat(imageTransform.getScale());// bundle.getFloat(Image.BUNDLE_IMAGE_CURRENT_SCALE)

			oos.writeFloat(imageTransform.getDistanceX());// bundle.getFloat(Image.BUNDLE_IMAGE_DISTANCE_X)
			oos.writeFloat(imageTransform.getDistanceY());// bundle.getFloat(Image.BUNDLE_IMAGE_DISTANCE_Y)

			oos.writeFloat(imageTransform.getEventX());// bundle.getFloat(Image.BUNDLE_IMAGE_EVENT_X)
			oos.writeFloat(imageTransform.getEventY());// bundle.getFloat(Image.BUNDLE_IMAGE_EVENT_Y)
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送Wifi网络
	 * 
	 * @param cmd
	 *                指令
	 * @param SSID
	 *                Wifi SSID
	 * @param wifi_password
	 *                wifi密码
	 * @param capabilities
	 *                加密方式
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendSSID(int cmd, String SSID, String wifi_password, String capabilities, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(SSID);
			oos.writeUTF(wifi_password);
			oos.writeUTF(capabilities);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
	}

	/**
	 * 发送忘记的Wifi网络
	 * 
	 * @param cmd
	 *                指令
	 * @param SSID
	 *                要发送的验证码
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendForgetSSID(int cmd, String SSID, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(SSID);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
	}

	/**
	 * 发送Ftp服务器打开文件的操作
	 * 
	 * @param cmd
	 *                指令
	 * @param filePath
	 *                要发送文件路径
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendFile(int cmd, String filePath, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(filePath);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 打开APK
	 * 
	 * @param cmd
	 *                指令
	 * @param mPackage
	 *                包名
	 * @param mMainClass
	 *                主类名
	 * @param flag
	 *                intent flag
	 * @param ip
	 *                发往的IP
	 * */
	protected static void openApk(int cmd, String mPackage, String mMainClass, int flag, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(mPackage);
			oos.writeUTF(mMainClass);
			oos.writeInt(flag);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 打开游戏
	 * 
	 * @param cmd
	 *                指令
	 * @param mPackage
	 *                包名
	 * @param ip
	 *                发往的IP
	 * */
	protected static void openGame(int cmd, String mPackage, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(mPackage);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 安装游戏
	 * 
	 * @param cmd
	 *                指令
	 * @param mPackage
	 *                游戏包名
	 * @param url
	 *                游戏下载链接
	 * @param ip
	 *                发往的IP
	 * */
	protected static void installGame(int cmd, String mPackage, String url, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(mPackage);
			oos.writeUTF(url);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 卸载游戏
	 * 
	 * @param cmd
	 *                指令
	 * @param mPackage
	 *                游戏包名
	 * @param ip
	 *                发往的IP
	 * */
	protected static void uninstallGame(int cmd, String mPackage, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(mPackage);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送验证码
	 * 
	 * @param cmd
	 *                指令
	 * @param flag
	 *                标记，请求/成功
	 * @param deviceName
	 *                设备名称
	 * @param verification
	 *                要发送的验证码
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendVerification(int cmd, int flag, String deviceName, String verification, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeInt(flag);
			oos.writeUTF(deviceName);
			oos.writeUTF(verification);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 发送脉搏
	 * 
	 * @param cmd
	 *                指令
	 * @param deviceName
	 *                设备名称
	 * @param ip
	 *                发往的IP
	 * */
	protected static void sendPulse(int cmd, String deviceName, String ip) {
		ByteArrayOutputStream bos = null;
		DataOutputStream oos = null;
		if (ip == null) {
			return;
		}
		try {
			bos = new ByteArrayOutputStream(64);
			oos = new DataOutputStream(bos);
			oos.writeInt(cmd);
			oos.writeUTF(deviceName);
			oos.flush();
			sendData(bos.toByteArray(), ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
