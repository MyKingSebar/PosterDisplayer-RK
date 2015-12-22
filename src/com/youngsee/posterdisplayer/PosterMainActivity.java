/*
 * Copyright (C) 2013 poster PCE YoungSee Inc. 
 * All Rights Reserved Proprietary and Confidential.
 * 
 * @author LiLiang-Ping
 */

package com.youngsee.posterdisplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.youngsee.authorization.AuthorizationManager;
import com.youngsee.common.Contants;
import com.youngsee.common.DensityUtil;
import com.youngsee.common.LogUtils;
import com.youngsee.common.Logger;
import com.youngsee.common.PackageInstaller;
import com.youngsee.common.SubWindowInfoRef;
import com.youngsee.common.SysParamManager;
import com.youngsee.customview.YSHorizontalScrollView;
import com.youngsee.envmnt.EnvMntManager;
import com.youngsee.osd.UDiskUpdata;
import com.youngsee.posterdisplayer.ApplicationSelector.AppInfo;
import com.youngsee.posterdisplayer.ApplicationSelector.ItemSelectListener;
import com.youngsee.posterdisplayer.R;
import com.youngsee.power.PowerOnOffManager;
import com.youngsee.screenmanager.ProgramFragment;
import com.youngsee.screenmanager.ScreenManager;
import com.youngsee.update.APKUpdateManager;
import com.youngsee.webservices.WsClient;

@SuppressLint("Wakelock")
public class PosterMainActivity extends Activity implements ItemSelectListener{
	private WakeLock mWklk = null;
	private PopupWindow mOsdPupupWindow = null; // OSD 弹出菜单

	private Intent popService = null;
	private boolean isPopServiceRunning = false;
	
	private Dialog mUpdateProgramDialog = null;
	private InternalReceiver mInternalReceiver = null;

    private ApplicationSelector      mSelector;
    private List<AppInfo>            mAppInfo;
    private YSHorizontalScrollView   mScrollView;
	
	public static PosterMainActivity INSTANCE = null;
	private static final int EVENT_CHECK_SET_ONOFFTIME = 0;

	private EnvMntManager mEnvMntManager = null;

	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PosterApplication.setSystemBarVisible(this, false);
        setContentView(R.layout.activity_main);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);

		Logger.d("====>PosterMainActivity onCreate: " + getIntent().toString());
		
		INSTANCE = this;
		
		// 初始安装APK时，需拷贝YSSysCtroller.apk以及开机画面的动画包
		int versionCode = PosterApplication.getInstance().getVerCode();
        SharedPreferences sharedPreferences = getSharedPreferences("ys_poster_displayer", Activity.MODE_PRIVATE);
        int installed = sharedPreferences.getInt("monitorInstalled", 0);
        int installedVersion = sharedPreferences.getInt("versionCode", 0);
        if(installed == 0 || versionCode != installedVersion){
            // Copy system ctrl APK
            String controller = null;
            if (PosterApplication.getInstance().getConfiguration().hasEncryptionChip())
            {
                controller = retrieveApkFromAssets("YSSysController-ENC.apk");
            }
            else
            {
                controller = retrieveApkFromAssets("YSSysController.apk");
            }
            
            PackageInstaller install = new PackageInstaller();
            if (!TextUtils.isEmpty(controller))
            {
                // Thread thread = new Thread(new Runnable(){
                // @Override
                // public void run(){
                install.installSystemPkg(controller);
                // }
                // });
                // thread.start();
                
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("monitorInstalled", 1);
                editor.putInt("versionCode", versionCode);
                editor.commit();
            }
            
            // Copy boot animation
            if (PosterApplication.getInstance().getConfiguration().hasBootAnimation())
            {
                String bootAnimationVertical = retrieveApkFromAssets("bootanimation-vertical.zip");
                if (!TextUtils.isEmpty(bootAnimationVertical))
                {
                    install.installBootAnimation(bootAnimationVertical, "bootanimation-vertical.zip");
                }
                
                String bootAnimationHorizontal = retrieveApkFromAssets("bootanimation-horizontal.zip");
                if (!TextUtils.isEmpty(bootAnimationHorizontal))
                {
                    install.installBootAnimation(bootAnimationHorizontal, "bootanimation-horizontal.zip");
                }
            }
        }
		
		// 初始化背景颜色
		((LinearLayout) findViewById(R.id.root)).setBackgroundColor(Color.BLACK);

        if (mWklk == null)
        {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWklk = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "PosterMain");
        }

		// 唤醒屏幕
        if (mWklk != null)
        {
            mWklk.acquire();
        }

		// 初始化系统参数
		PosterApplication.getInstance().initAppParam();

		// 获取屏幕实际大小(以像素为单位)
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		PosterApplication.setScreenWidth(metric.widthPixels); // 屏幕宽度（像素）
		//PosterApplication.setScreenHeight(metric.heightPixels); // 屏幕高度（像素）
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			PosterApplication.setScreenHeight(1080);
		} else {
			PosterApplication.setScreenHeight(1920);
		}
		
		// 检测是否鉴权
        if (!AuthorizationManager.getInstance().checkAuthStatus(AuthorizationManager.MODE_IMMEDIATE))
        {
            AuthorizationManager.getInstance().startAuth();
        }
		
		// 启动屏幕管理线程
		if (ScreenManager.getInstance() == null) {
			ScreenManager.createInstance(this).startRun();
		}

		//在网络管理线程启动之前创建EnvMntManager实例，保证EnvMntManager中生成的handler运行在主线程
		//解决偶尔出现的在线程消息队列没有初始化前生成handler造成crash问题
        if (PosterApplication.getInstance().getConfiguration().hasEnvironmentMonitor()) {
            mEnvMntManager = EnvMntManager.getInstance();
        }
        
		// 启动网络管理线程
		if (WsClient.getInstance() == null) {
			WsClient.createInstance(this).startRun();
		}

		// 启动日志输出线程
		if (LogUtils.getInstance() == null) {
			LogUtils.createInstance(this).startRun();
		}

		initDockBar();

		// 定义OSD菜单弹出方式
		((LinearLayout) findViewById(R.id.root))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mOsdPupupWindow != null) {
							if (mOsdPupupWindow.isShowing()) {
								mOsdPupupWindow.dismiss();
							} else {
								mOsdPupupWindow.showAtLocation(v, Gravity.TOP | Gravity.LEFT, 0, 0);
								mHandler.postDelayed(rHideOsdPopWndDelay, 6000);
							}
						}
					}
				});

		// 初始化OSD菜单
		initOSD();

		// 启动定时器，定时清理文件和上传日志
		PosterApplication.getInstance().startTimingDel();
		PosterApplication.getInstance().startTimingUploadLog();

		// 检测定时开关机状态
		PowerOnOffManager.getInstance().checkAndSetOnOffTime(
				PowerOnOffManager.AUTOSCREENOFF_COMMON);

		// 检测是否需要升级新版本
		if (SysParamManager.getInstance().getAutoUpgrade() == 1) {
			APKUpdateManager.getInstance().startAutoDetector();
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		mInternalReceiver = new InternalReceiver();
		registerReceiver(mInternalReceiver, filter);
	}

	public void showOsd() {
		if (mOsdPupupWindow != null) {
			if (mOsdPupupWindow.isShowing()) {
				mOsdPupupWindow.dismiss();
			} else {
				mOsdPupupWindow.showAtLocation(findViewById(R.id.root), Gravity.TOP
						| Gravity.LEFT, 0, 0);
				mHandler.postDelayed(rHideOsdPopWndDelay, 30000);
			}
		}
	}
	
	@Override
    public void onStart(){
		super.onStart();
		initReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PowerOnOffManager.getInstance().getCurrentStatus() == PowerOnOffManager.STATUS_STANDBY) {
			PowerOnOffManager.getInstance().setCurrentStatus(
					PowerOnOffManager.STATUS_ONLINE);
			PowerOnOffManager.getInstance().checkAndSetOnOffTime(
					PowerOnOffManager.AUTOSCREENOFF_URGENT);
		}
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(rHideOsdPopWndDelay);
		if (mOsdPupupWindow.isShowing()) {
			mOsdPupupWindow.dismiss();
		}

		super.onPause();
	}
	
	@Override
    public void onStop(){
    	unregisterReceiver(mInternalReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mHandler.removeCallbacks(rHideOsdPopWndDelay);
		if (mOsdPupupWindow.isShowing()) {
			mOsdPupupWindow.dismiss();
		}

		synchronized (this) {
			if (isPopServiceRunning) {
				stopService(popService);
				isPopServiceRunning = false;
			}
		}

		// 结束屏幕管理线程
		if (ScreenManager.getInstance() != null) {
			ScreenManager.getInstance().stopRun();
		}

		// 结束网络管理线程
		if (WsClient.getInstance() != null) {
			WsClient.getInstance().stopRun();
		}

		// 结束日志输出线程
		if (LogUtils.getInstance() != null) {
			LogUtils.getInstance().stopRun();
		}

		// 结束APK更新
		if (APKUpdateManager.getInstance() != null) {
			APKUpdateManager.getInstance().destroy();
		}

		if (PowerOnOffManager.getInstance() != null) {
			PowerOnOffManager.getInstance().destroy();
		}
		
		if (AuthorizationManager.getInstance() != null) {
			AuthorizationManager.getInstance().destroy();
		}
		
		if (LogManager.getInstance() != null) {
			LogManager.getInstance().destroy();
		}

		// 终止定时器
		PosterApplication.getInstance().cancelTimingDel();
		PosterApplication.getInstance().cancelTimingUploadLog();

		dismissUpdateProgramDialog();

		if (mEnvMntManager != null) {
			mEnvMntManager.destroy();
			mEnvMntManager = null;
		}

		// 恢复屏幕
		if (mWklk != null) {
			mWklk.release();
			mWklk = null;
		}

		INSTANCE = null;
		super.onDestroy();
		System.exit(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
            if(mSelector != null){
                mSelector.dismiss();
                mSelector = null;
            }
			// Log.i("dddd", "back--");
			return true; // 不响应Back键

		case KeyEvent.KEYCODE_MENU:
			enterToOSD(PosterOsdActivity.OSD_MAIN_ID);
			return true; // 打开OSD主菜单

		case KeyEvent.KEYCODE_PAGE_UP:
			return true; // 主窗口中上一个素材

		case KeyEvent.KEYCODE_PAGE_DOWN:
			return true; // 主窗口中下一个素材

		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			return true; // 主窗口视频播放

		case KeyEvent.KEYCODE_MEDIA_STOP:
			return true; // 主窗口视频暂停
		}

		return super.onKeyDown(keyCode, event);
	}

	private void showUpdateProgramDialog() {
		if ((mUpdateProgramDialog != null) && mUpdateProgramDialog.isShowing()) {
			mUpdateProgramDialog.dismiss();
		}
		mUpdateProgramDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.udisk_update_pgm)
				.setMessage(R.string.udisk_content).setCancelable(true)
				.setPositiveButton(R.string.udisk_title, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UDiskUpdata diskUpdate = new UDiskUpdata(PosterMainActivity.this);
                        diskUpdate.updateProgram();
						mUpdateProgramDialog = null;
					}
				})
				.setNegativeButton(R.string.udisk_btn_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mUpdateProgramDialog = null;
					}
				}).create();
		mUpdateProgramDialog.show();
	}

	public void dismissUpdateProgramDialog() {
		if ((mUpdateProgramDialog != null)
				&& mUpdateProgramDialog.isShowing()) {
			mUpdateProgramDialog.dismiss();
			mUpdateProgramDialog = null;
		}
	}

    private String retrieveApkFromAssets(String packageName){
        File filePath = this.getFilesDir();
        StringBuilder cachePath = new StringBuilder();
        cachePath.append(filePath.getAbsolutePath());
        cachePath.append("/");
        cachePath.append(packageName);
        
        try{
            File file = new File(cachePath.toString());
            if(!file.exists()){
                file.createNewFile();
            }
            InputStream is = getAssets().open(packageName);
            FileOutputStream fos = new FileOutputStream(file);
    
            byte[] temp = new byte[1024];
            int i = 0;
            while((i = is.read(temp)) != -1){
                fos.write(temp, 0, i);
            }
            fos.flush();
            fos.close();
            is.close();
        }
        catch(IOException e){
            // Toast.makeText(mContext, e.getMessage(), 2000).show();
            Builder builder = new Builder(this);
            builder.setMessage(e.getMessage());
            builder.show();
            e.printStackTrace();
        }
        
        return cachePath.toString();
    }
	
	private class InternalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_REMOVED)
					|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
				String path = intent.getData().getPath();
				if (path.substring(5).startsWith(Contants.UDISK_NAME_PREFIX)) {
					if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
							&& PosterApplication.existsPgmInUdisk(path)) {
						showUpdateProgramDialog();
					} else {
						dismissUpdateProgramDialog();
					}
				}
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_CHECK_SET_ONOFFTIME:
				PowerOnOffManager.getInstance().checkAndSetOnOffTime(
						(msg.getData().getInt("type")));
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void checkAndSetOnOffTime(int type) {
		Bundle bundle = new Bundle();
		bundle.putInt("type", type);
		Message msg = mHandler.obtainMessage();
		msg.what = EVENT_CHECK_SET_ONOFFTIME;
		msg.setData(bundle);
		msg.sendToTarget();
	}

	// 加载新节目
	public void loadProgram(ArrayList<SubWindowInfoRef> subWndList) {
		// 准备参数
		Bundle bundle = new Bundle();
		bundle.putSerializable("SubWindowCollection", (Serializable) subWndList);

		// 传递参数
		ProgramFragment program = new ProgramFragment();
		program.setArguments(bundle);

		// 启动新的program Fragment
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_NONE); // 切换时无效果
		ft.replace(R.id.root, program, PosterApplication.getInstance().getNormalPgmTag())
				.commitAllowingStateLoss();
	}

	public void setPopServiceRunning(boolean isRunning) {
		synchronized (this) {
			isPopServiceRunning = isRunning;
		}
	}

	public void startPopSub(String text, int playSpeed, int duration,
			int number, String fontName, int fontColor) {
		synchronized (this) {
			if (isPopServiceRunning == true) {
				stopService(popService);
			}

			popService = new Intent(this, PopSubService.class);
			popService.putExtra(PopSubService.DURATION, duration);
			popService.putExtra(PopSubService.NUMBER, number);
			popService.putExtra(PopSubService.TEXT, text);
			popService.putExtra(PopSubService.FONTCOLOR, fontColor);
			popService.putExtra(
					PopSubService.FONTNAME,
					(fontName != null) ? fontName.substring(
							fontName.lastIndexOf(File.separator) + 1,
							fontName.lastIndexOf(".")) : null);
			popService.putExtra(PopSubService.SPEED, playSpeed);
			Logger.i("Start popService");
			startService(popService);
			isPopServiceRunning = true;
		}
	}

	public void startAudio() {
		ProgramFragment pf = (ProgramFragment) getFragmentManager()
				.findFragmentByTag(PosterApplication.getInstance().getNormalPgmTag());
		if (pf != null) {
			pf.startAudio();
		}
	}

	public void stopAudio() {
		ProgramFragment pf = (ProgramFragment) getFragmentManager()
				.findFragmentByTag(PosterApplication.getInstance().getNormalPgmTag());
		if (pf != null) {
			pf.stopAudio();
		}
	}

	// 初始化OSD弹出菜单
	private void initOSD() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View osdView = inflater.inflate(R.layout.osd_pop_menu_view, null);
		osdView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOsdPupupWindow != null) {
					if (mOsdPupupWindow.isShowing()) {
						mOsdPupupWindow.dismiss();
					} else {
						mOsdPupupWindow.showAtLocation(v, Gravity.TOP
								| Gravity.LEFT, 0, 0);
					}
				}
			}
		});
		
		mOsdPupupWindow = new PopupWindow(osdView, 100, LinearLayout.LayoutParams.MATCH_PARENT, true);
		mOsdPupupWindow.setAnimationStyle(R.style.osdAnimation);
		mOsdPupupWindow.setOutsideTouchable(false);
		mOsdPupupWindow.setFocusable(true);

		// 初始化点击动作
		((ImageView) osdView.findViewById(R.id.osd_mainmenu))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_MAIN_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_server))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_SERVER_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_clock))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_CLOCK_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_system))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_SYSTEM_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_filemanage))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_FILEMANAGER_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_tools))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_TOOL_ID);
					}
				});

		((ImageView) osdView.findViewById(R.id.osd_about))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						enterToOSD(PosterOsdActivity.OSD_ABOUT_ID);
					}
				});
	}

	private void enterToOSD(int menuId) {
		//PosterApplication.getInstance().initLanguage();
		SharedPreferences sharedPreferences = getSharedPreferences(
				PosterOsdActivity.OSD_CONFIG, MODE_PRIVATE);
		if (!sharedPreferences.getBoolean(PosterOsdActivity.OSD_ISMEMORY, false)) {
			Intent intent = new Intent(this, PosterOsdActivity.class);
			intent.putExtra("menuId", menuId);
			startActivity(intent);
		} else {
			switch (menuId) {
			case PosterOsdActivity.OSD_SYSTEM_ID:
				PosterApplication.startApplication(this, "com.android.settings");
				break;

			case PosterOsdActivity.OSD_FILEMANAGER_ID:
				PosterApplication.startApplication(this, Contants.FILEBROWSER_PACKAGENAME);
				break;

			case PosterOsdActivity.OSD_MAIN_ID:
			case PosterOsdActivity.OSD_SERVER_ID:
			case PosterOsdActivity.OSD_CLOCK_ID:
			case PosterOsdActivity.OSD_TOOL_ID:
			case PosterOsdActivity.OSD_ABOUT_ID:
				Intent intent = new Intent(this, PosterOsdActivity.class);
				intent.putExtra("menuId", menuId);
				startActivity(intent);
				break;
			}
		}

		if (mOsdPupupWindow.isShowing()) {
			mOsdPupupWindow.dismiss();
		}
	}

	private Runnable rHideOsdPopWndDelay = new Runnable() {
		@Override
		public void run() {
			mHandler.removeCallbacks(rHideOsdPopWndDelay);
			if (mOsdPupupWindow != null && mOsdPupupWindow.isShowing()) {
				mOsdPupupWindow.dismiss();
			}
		}
	};
    
    private void initDockBar(){
        if(PosterApplication.getInstance().getConfiguration().hasDockBar()){
            findViewById(R.id.LLDockBar).setVisibility(View.VISIBLE);
            mAppInfo = new ArrayList<AppInfo>();
            Button button = (Button)this.findViewById(R.id.BSelectApp);
            button.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    showAppSelector();
                }
            });
            loadAppInfo();
            showAppInfo();
        }
        else{
            findViewById(R.id.LLDockBar).setVisibility(View.GONE);
        }
    }
    
    private boolean showAppSelector(){
        if(mSelector != null && mSelector.isShowing()){
            mSelector.dismiss();
        }
        
        View contentView = LayoutInflater.from(this).inflate(R.layout.application_list, null);
        mSelector = new ApplicationSelector(this, contentView);
        mSelector.setItemSelectListener(this);
        mSelector.setFocusable(true);
        mSelector.setBackgroundDrawable(new BitmapDrawable(getResources()));
        
        // 重写onKeyListener
        contentView.setOnKeyListener(new OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    if(mSelector != null){
                        mSelector.dismiss();
                        mSelector = null;
                    }
                    return true;
                }
                return false;
            }
        });
        
        mSelector.showAtLocation(PosterMainActivity.this.findViewById(R.id.root), Gravity.TOP, 0, 0);
        return true;
    }
    
    @Override
    public void onItemSelected(AppInfo app) {
        if(mSelector != null){
            mSelector.dismiss();
            mSelector = null;
        }
        
        boolean found = false;
        for(int i = 0; i< mAppInfo.size(); i ++){
            if(mAppInfo.get(i).getPkgName().equals(app.getPkgName())){
                found = true;
                mAppInfo.set(i, app);
                break;
            }
        }
        
        if(!found){
            mAppInfo.add(app);
        }
        
        saveAppInfo();
        showAppInfo();
    }
    
    public void saveAppInfo() {  
        SharedPreferences sp= getSharedPreferences("applist", Context.MODE_PRIVATE);  
        SharedPreferences.Editor mEdit1= sp.edit();  
        mEdit1.putInt("app_size",mAppInfo.size()); /*sKey is an array*/   
      
        for(int i=0;i<mAppInfo.size();i++) {  
            mEdit1.remove("app_" + i);  
            mEdit1.putString("app_" + i, mAppInfo.get(i).getPkgName());    
        }  

        mEdit1.commit();       
    }  
    
    public void loadAppInfo() {    
        SharedPreferences sp= getSharedPreferences("applist", Context.MODE_PRIVATE);  
        int size = sp.getInt("app_size", 0);    
      
        List<String> list = new ArrayList<String>();
        for(int i=0;i<size;i++) {  
            list.add(sp.getString("app_" + i, null));
        }
        
        List<AppInfo> appList= new ArrayList<AppInfo>();
        queryAppInfo(appList);
        mAppInfo.clear();
        for(AppInfo info:appList){
            for(String pkg:list){
                if(pkg != null && pkg.equals(info.getPkgName())){
                    mAppInfo.add(info);
                    break;
                }
            }
        }
    }  
    
    public void showAppInfo() {
        mScrollView = (YSHorizontalScrollView)this.findViewById(R.id.HSVDockBar);
        mScrollView.setItemWidth(DensityUtil.dip2px(this, 250+20));
        mScrollView.setItemNumber(this.mAppInfo.size() +1);
        
        
        GridView gridView = (GridView) this.findViewById(R.id.gridview);        
        GridViewAdapter adapter = new GridViewAdapter();
        ViewGroup.LayoutParams para = gridView.getLayoutParams();
        
        float scale = this.getResources().getDisplayMetrics().density;
        para.width = (this.mAppInfo.size() +1) * (int)(270 * scale + 0.5f);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = mAppInfo.get(position).getIntent();
                startActivity(intent);
            }
        });
        
        gridView.setOnItemLongClickListener(new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mAppInfo.remove(position);
                saveAppInfo();
                showAppInfo();
                return true;
            }
            
        });
    }
    
    // 获得所有启动Activity的信息，类似于Launch界面  
    public void queryAppInfo(List<AppInfo> listAppInfo) {
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象  
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        // 调用系统排序 ， 根据name排序。
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序。
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
        if (listAppInfo != null) {
            listAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                ApplicationInfo info = reInfo.activityInfo.applicationInfo;
                if (((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0) &&
                        ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)) {
                    continue;
                }
                String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标  
                // 为应用程序的启动Activity 准备Intent
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkgName, activityName));
                // 创建一个AppInfo对象，并赋值
                AppInfo appInfo = new AppInfo();
                appInfo.setAppLabel(appLabel);
                appInfo.setPkgName(pkgName);
                appInfo.setAppIcon(icon);
                appInfo.setIntent(launchIntent);
                listAppInfo.add(appInfo); // 添加至列表中
            }  
        }  
    }
    
    final class GridViewAdapter extends BaseAdapter {  
        
        @Override  
        public int getCount() {  
            return mAppInfo.size();  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return mAppInfo.get(position);  
        }  
  
        @Override  
        public long getItemId(int position) {  
            return position;  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater)PosterMainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.app_item, parent, false);
            }

            TextView textView = (TextView)convertView.findViewById(R.id.item_textview);
            String name = mAppInfo.get(position).getAppLabel();
            if(name != null){
                textView.setText(name);
            }
            
            ImageView iv = (ImageView)convertView.findViewById(R.id.app_icon);
            iv.setImageDrawable(mAppInfo.get(position).getAppIcon());
            
            return convertView;
        }
  
    }
}
