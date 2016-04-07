package com.youngsee.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.text.TextUtils;
import android.util.Log;

public class ElectricManager {
	private final long DEFAULT_READTHREAD_PERIOD = 1000;
	private final long DEFAULT_WRITETHREAD_PERIOD = 1 * 3600 * 1000;
	
	private final int BAUTRATE = 1200;
	private final String DEVFILE_SERIALPORT = "/dev/ttyS3";
	private SerialPort mSerialPort = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;
	
	private byte[] mSendBuffer = { (byte) 0xFE, (byte) 0x68, (byte) 0x99,
			(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
			(byte) 0x99, (byte) 0x68, (byte) 0x01, (byte) 0x02,
			(byte) 0x43, (byte) 0xC3, (byte) 0x6F, (byte) 0x16 };

	private ReadThread mReadThread = null;
	private WriteThread mWriteThread = null;
	
	private ElectricManager() {
		try {
			mSerialPort = new SerialPort(new File(DEVFILE_SERIALPORT), BAUTRATE, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class ElectricManagerHolder {
        static final ElectricManager INSTANCE = new ElectricManager();
    }
	
	public static ElectricManager getInstance() {
		return ElectricManagerHolder.INSTANCE;
	}

	/*
	 * 转换16进制
	 */
	private static String toHex(byte b) {
		String result = Integer.toHexString(b & 0xFF);
		if (result.length() == 1) {
			result = '0' + result;
		}
		return result;
	}

	private void onDataReceived(final byte[] buffer, final int size) {
		if (buffer == null) {
			Log.i("ElectricManager@onDataReceived()", "Buffer is null.");
			return;
		} else if (size < 20) {
			Log.i("ElectricManager@onDataReceived()", "Size is invalid, size = " + size + ".");
			return;
		}
		
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

		String strElectic =  String.valueOf(Double.parseDouble(sb.toString()));
		if (!TextUtils.isEmpty(strElectic))
		{
			DbHelper.getInstance().setrElectricToDB(strElectic);
		}
	}
	
	public void cancelTimingGetElectric() {
		if (mWriteThread != null) {
			mWriteThread.cancel();
			mWriteThread = null;
		}
		
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mOutputStream = null;
		}
		
		if (mInputStream != null)
		{
			try {
				mInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mInputStream = null;
		}
	}

	public void startTimingGetElectric() {
		if (mWriteThread != null)
		{
			mWriteThread.cancel();
			mWriteThread = null;
		}
		mWriteThread = new WriteThread();
    	mWriteThread.start();
    	
    	if (mReadThread != null)
    	{
    		mReadThread.cancel();
    		mReadThread = null;
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
					if (mInputStream.available() > 0) {
						receiveSize = mInputStream.read(receiveBuffer);
						onDataReceived(receiveBuffer, receiveSize);
					}
					Thread.sleep(DEFAULT_READTHREAD_PERIOD);
				} catch (IOException e) {
					e.printStackTrace();
				}catch (InterruptedException e) {
					break;
				}
			}

			Log.i("ElectricManager ReadThread", "Read thread is safely terminated, id is: " + currentThread().getId());
		}
	}
	
	private final class WriteThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			Log.i("ElectricManager ReadThread", "A new monitor thread is started. Thread id is " + getId() + ".");

			while (!mIsCanceled) {
				if (mOutputStream == null) {
					Log.i("ElectricManager ReadThread", "Output stream is null.");
					return;
				}
				
				try {
					mOutputStream.write(mSendBuffer);
					mOutputStream.flush();
					Thread.sleep(DEFAULT_READTHREAD_PERIOD); //DEFAULT_WRITETHREAD_PERIOD);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					break;
				}
            }
            
            Log.i("ElectricManager ReadThread", "Monitor thread is safely terminated, id is: " + currentThread().getId());
		}
	}
}
