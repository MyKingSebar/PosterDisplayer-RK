package com.youngsee.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.youngsee.logmanager.Logger;
import android.text.TextUtils;
import android.util.Log;

public class ElectricManager {
	private final long DEFAULT_READTHREAD_PERIOD = 1000;
	private final int BAUTRATE = 1200;
	private final String DEVFILE_SERIALPORT = "/dev/ttyS3";
	private final int DATABITS=8,STOPBITS=1,PARITY='e';
	private SerialPort mSerialPort = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	
	private byte[] mSendBuffer = { (byte) 0xFE, (byte) 0x68, (byte) 0x99,
			(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
			(byte) 0x99, (byte) 0x68, (byte) 0x01, (byte) 0x02,
			(byte) 0x43, (byte) 0xC3, (byte) 0x6F, (byte) 0x16 };
	
	private ReadThread mReadThread = null;
	private ElectricManager() {
	}

 	private static class ElectricManagerHolder {
        static final ElectricManager INSTANCE = new ElectricManager();
    }
	
	public static ElectricManager getInstance() {
		return ElectricManagerHolder.INSTANCE;
	}

	/**
	 * 转换16进制
	 */
	private static String toHex(byte b) {
		String result = Integer.toHexString(b & 0xFF);
		if (result.length() == 1) {
			result = '0' + result;
		}
		return result;
	}

	private boolean onDataReceived(final byte[] buffer, final int size) {
		if (buffer == null) {
			Log.i("ElectricManager@onDataReceived()", "Buffer is null.");
			return false;
		} else if (size < 19) {
			Log.i("ElectricManager@onDataReceived()", "Size is invalid, size = " + size + ".");
			return false;
		}
		
		String strElectic =  parseEletric(buffer);
		if (!TextUtils.isEmpty(strElectic))
		{
			DbHelper.getInstance().setrElectricToDB(strElectic);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param buffer 
	 * @return 把传入的buffer解析成一个电表的真实电量值
	 * @throws NumberFormatException Double解析字符串出现的异常
	 */ 
	private String parseEletric(byte[] buffer){
		byte[] electriArray = new byte[4];
		for (int i = 0, j = 16; i < electriArray.length; i++, j--) {
			electriArray[i] = (byte) (buffer[j] - 51);
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < electriArray.length; i++) {
			sb.append(toHex(electriArray[i]));
			if (i == 2) {
				sb.append(".");
				continue;
			}
		}
	
		String strElectric=null;
		try {
			strElectric=String.valueOf(Double.parseDouble(sb.toString()));
		} catch (NumberFormatException e) {
			strElectric="";
		}
			
		return strElectric;
	}
	
	/**
	 * 像电表发送数据
	 */
	private void writeToElecMeter() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(mSendBuffer);
				mOutputStream.flush();
				if (mReadThread == null) {
					mReadThread = new ReadThread();
					mReadThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} else {
			Logger.i("mOutputSteam is null!");
		}
	}
    
	public void stopGetElectric() {
		
		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}
		
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		
		if (mOutputStream != null)
		{
			try {
				mOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mOutputStream = null;
		}
		
		if (mInputStream != null)
		{
			try {
				mInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mInputStream = null;
		}
	}

	public void startGetElectric() {
		stopGetElectric();
		try {
			mSerialPort = new SerialPort(new File(DEVFILE_SERIALPORT),BAUTRATE, DATABITS, STOPBITS, PARITY);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mReadThread = new ReadThread();
		mReadThread.start();
	}
	
	private final class ReadThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			Log.i("ElectricManager ReadThread", "A new read thread is started. Thread id is " + getId() + ".");

			int MAX_BUF_SIZE = 32;
			int receiveSize = 0;
			byte[] receiveBuffer = new byte[MAX_BUF_SIZE];
			
			while (!mIsCanceled) {
				if (mInputStream == null) {
					Log.i("ElectricManager ReadThread", "Input stream is null.");
					return;
				}
				try {
					writeToElecMeter();
					Thread.sleep(DEFAULT_READTHREAD_PERIOD);// DEFAULT_WRITETHREAD_PERIOD);
					Arrays.fill(receiveBuffer, (byte) 0);
					if (mInputStream.available() > 0) {
						receiveSize = mInputStream.read(receiveBuffer);
						if(onDataReceived(receiveBuffer, receiveSize)){
							break;
						}
					}
					Thread.sleep(DEFAULT_READTHREAD_PERIOD);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					break;
				}
			}

			Log.i("ElectricManager ReadThread", "Read thread is safely terminated, id is: " + currentThread().getId());
		    stopGetElectric();
		}
	}

}
