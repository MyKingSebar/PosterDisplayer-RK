package com.youngsee.customview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.tencent.smtt.sdk.WebView;
import com.youngsee.common.X5WebView;
import com.youngsee.logmanager.Logger;
import com.youngsee.posterdisplayer.PosterMainActivity;
import com.youngsee.posterdisplayer.R;

/**
 * Created by WeiYanGeorge on 17/3/23.
 */

public class YsWebViewSpecial extends PosterBaseView{

    private X5WebView mWv = null;

    private static int MAX_CLICK_CNTS    = 5;
    private long mLastClickTime          = 0;
    private static int mCurrentClickCnts = 0;

    private float mPosX = 0;
    private float mPosY = 0;
    private float mCurPosX = 0;
    private float mCurPosY = 0;

    private SharedPreferences sharedPreferences = mContext.getSharedPreferences("reload_for_priod", Activity.MODE_PRIVATE);
    private int timeForPeriod=0 ;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWv !=null) {
                timeForPeriod = sharedPreferences.getInt("time_period", 60);
                if (sharedPreferences.getBoolean("isReload", false)) {
                    try {
                        Logger.i("reload for web");
                        mWv.reload();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mHandler.postDelayed(mRunnable, timeForPeriod * 1000);
            }
        }
    };

    public YsWebViewSpecial(Context context)
    {
        super(context);
        initView(context);

    }

    public YsWebViewSpecial(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        initView(context);

    }

    public YsWebViewSpecial(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_web, this);
        mWv = (X5WebView) findViewById(R.id.wv);
        mWv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(mCurPosX - mPosX > 0 &&
                                Math.abs(mCurPosX - mPosX)>500){
                            PosterMainActivity.INSTANCE.showOsd();
                            mPosX = 0;
                            mCurPosX = 0;
                            return true;
                        }
                        if(mCurPosY - mPosY >0 &&
                        		Math.abs(mCurPosY - mPosY)>300){
                        	mWv.reload();
                        	mPosY = 0;
                        	mCurPosY = 0;
                        	return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP)
                        {
                        	long clickTime = SystemClock.uptimeMillis();
            				long dtime = clickTime - mLastClickTime;
            				if (mLastClickTime == 0 || dtime < 3000) {
            					mCurrentClickCnts++;
            					mLastClickTime = clickTime;
            				} else {
            					mLastClickTime = 0;
            					mCurrentClickCnts = 0;
            				}

            				// When click times is more than 5, then popup the tools bar
            				if (mCurrentClickCnts > MAX_CLICK_CNTS) {
                                PosterMainActivity.INSTANCE.showOsd();
            					mLastClickTime = 0;
            					mCurrentClickCnts = 0;
            					return true;
            				}
                        }
                        
                }
                return false;
            }
        });
        
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.hasFocus())
                {
                    v.requestFocus();
                }
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWv.canGoBack()){
            mWv.goBack();
            return true;
        }

        return false;
    }

    private void setUrl(final String url){
        if(mWv != null)
        {
            mWv.loadUrl(url);
            mHandler.postDelayed(mRunnable,timeForPeriod*2000);
        }
    }

    @Override
    public void onViewDestroy() {
        if(mHandler !=null)
        {
            mHandler.removeCallbacks(mRunnable);
        }

        if (mWv != null)
        {
            mWv.destroy();
        }
        this.removeAllViews();
    }

    @Override
    public void onViewPause() {

    }

    @Override
    public void onViewResume() {

    }

    @Override
    public void startWork() {
        if (mMediaList == null) {
            Logger.i("Media list is null");
            return;
        } else if (mMediaList.isEmpty()) {
            Logger.i("No media in the list.");
            return;
        }

        mCurrentIdx = 0;
        mCurrentMedia = mMediaList.get(mCurrentIdx);
        setUrl(mCurrentMedia.filePath);
        //setUrl("http://400.mengniu.cn/MNITSM/ReportServer?formlet=pro%2FITSM%5B5de5%5D%5B5355%5D%5B5927%5D%5B5c4f%5D%5B770b%5D%5B677f%5D-%5B5c0f%5D%5B7c73%5D%5B76d2%5D%5B5b50%5DUC.frm");
    }
    @Override
    public void stopWork() {

    }
}
