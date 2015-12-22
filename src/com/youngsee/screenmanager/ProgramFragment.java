/*
 * Copyright (C) 2013 poster PCE YoungSee Inc.
 * All Rights Reserved Proprietary and Confidential.
 * 
 * @author LiLiang-Ping
 */

package com.youngsee.screenmanager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.youngsee.common.Contants;
import com.youngsee.common.FileUtils;
import com.youngsee.common.LogUtils;
import com.youngsee.common.Logger;
import com.youngsee.common.MediaInfoRef;
import com.youngsee.common.SubWindowInfoRef;
import com.youngsee.customview.AudioView;
import com.youngsee.customview.DateTimeView;
import com.youngsee.customview.GalleryView;
import com.youngsee.customview.MultiMediaView;
import com.youngsee.customview.PosterBaseView;
import com.youngsee.customview.TimerView;
import com.youngsee.customview.YSWebView;
import com.youngsee.customview.MarqueeView;
import com.youngsee.posterdisplayer.R;
import com.youngsee.posterdisplayer.PosterApplication;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ProgramFragment extends Fragment
{
    private FrameLayout                 mMainLayout         = null;
    private final Handler               mHandler            = new Handler();
    
    // 屏幕布局信息
    private Set<PosterBaseView>         mSubWndCollection   = null;
    private ArrayList<SubWindowInfoRef> mSubWndInfoList     = null;
    
    // 背景图片信息
    private MediaInfoRef                mBgImgInfo          = null;
    private Bitmap                      mBgBmp              = null;
    private Bitmap                      mStandbyBmp         = null;
                                                   
    /**
     * During creation, if arguments have been supplied to the fragment then parse those out.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Bundle args = getArguments();
        if (args != null)
        {
            mSubWndInfoList = (ArrayList<SubWindowInfoRef>) args.getSerializable("SubWindowCollection");
        }
    }
    
    /**
     * Create the view for this fragment, using the arguments given to it.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // 不能将Fragment的视图附加到此回调的容器元素，因此attachToRoot参数必须为false
        return inflater.inflate(R.layout.fragment_program, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        // Get the Main View
        mMainLayout = (FrameLayout) getActivity().findViewById(R.id.programroot);

        // create subwindows
        if (mSubWndInfoList != null)
        {
            Logger.i("Window number is: " + mSubWndInfoList.size());
            
            // initialize
            int xPos = 0;
            int yPos = 0;
            int width = 0;
            int height = 0;
            String wndName = null;
            String wndType = null;
            List<MediaInfoRef> mediaList = null;
            
            PosterBaseView tempSubWnd = null;
            SubWindowInfoRef subWndInfo = null;
            mSubWndCollection = new HashSet<PosterBaseView>();
            
            // Through the sub window list, and create the correct view for it.
            for (int i = 0; i < mSubWndInfoList.size(); i++)
            {
                tempSubWnd = null;
                subWndInfo = mSubWndInfoList.get(i);
                
                // 窗体类型和名称
                if ((wndType = subWndInfo.getSubWindowType()) == null)
                {
                    continue;
                }
                wndName = subWndInfo.getSubWindowName();
                
                // 窗体位置
                xPos = subWndInfo.getXPos();
                yPos = subWndInfo.getYPos();
                width = subWndInfo.getWidth();
                height = subWndInfo.getHeight();
                
                // 素材
                mediaList = subWndInfo.getSubWndMediaList();
                
                // 创建窗口
                if (wndType.contains("StandbyScreen"))
                {
                    setStandbyScreen();
                    continue;
                }
                else if (wndType.contains("Background"))
                {
                    // 背景图片
                    if (mediaList != null && mediaList.size() > 0 && "File".equals(mediaList.get(0).source))
                    {
                        mBgImgInfo = mediaList.get(0);
                        setWindowBackgroud();
                    }
                    continue;
                }
                else if (wndType.contains("Main") || wndType.contains("Image") || wndType.contains("Weather"))
                {
                    tempSubWnd = new MultiMediaView(getActivity());
                }
                else if (wndType.contains("Audio"))
                {
                    tempSubWnd = new AudioView(getActivity());
                }
                else if (wndType.contains("Scroll"))
                {
                    tempSubWnd = new MarqueeView(getActivity());
                }
                else if (wndType.contains("Clock"))
                {
                    tempSubWnd = new DateTimeView(getActivity());
                }
                else if (wndType.contains("Gallery"))
                {
                    tempSubWnd = new GalleryView(getActivity());
                }
                else if (wndType.contains("Web"))
                {
                    tempSubWnd = new YSWebView(getActivity());
                }
                else if (wndType.contains("Timer"))
                {
                    tempSubWnd = new TimerView(getActivity());
                }
                
                // 设置窗口参数，并添加
                if (tempSubWnd != null)
                {
                    tempSubWnd.setViewName(wndName);
                    tempSubWnd.setViewType(wndType);
                    tempSubWnd.setMediaList(mediaList);
                    tempSubWnd.setViewPosition(xPos, yPos);
                    tempSubWnd.setViewSize(width, height);
                    mMainLayout.addView(tempSubWnd);
                    mSubWndCollection.add(tempSubWnd);
                }
            }
        }
        
        // Start work after all view is ready
        Looper.myQueue().addIdleHandler(new IdleHandler() {
            @Override
            public boolean queueIdle()
            {
                if (mSubWndCollection != null)
                {
                    for (PosterBaseView wnd : mSubWndCollection)
                    {
                        wnd.startWork();
                    }
                }
                return false;
            }
        });
    }
    
    @Override
    public void onResume()
    {
        if (mSubWndCollection != null)
        {
            for (PosterBaseView wnd : mSubWndCollection)
            {
                wnd.onViewResume();
            }
        }
        
        if (TextUtils.isEmpty(ScreenManager.getInstance().getPlayingPgmId()))
        {
            LogUtils.getInstance().toAddPLog(0, Contants.PlayProgramStart, ScreenManager.getInstance().getPlayingPgmId(), "", "");
        }
        
        super.onResume();
    }
    
    @Override
    public void onPause()
    {
        mHandler.removeCallbacks(rSetWndBgDelay);
        mHandler.removeCallbacks(rSetStandbyDelay);

        if (mSubWndCollection != null)
        {
            for (PosterBaseView wnd : mSubWndCollection)
            {
                wnd.onViewPause();
            }
        }

        if (!TextUtils.isEmpty(ScreenManager.getInstance().getPlayingPgmId()))
        {
            LogUtils.getInstance().toAddPLog(0, Contants.PlayProgramEnd, ScreenManager.getInstance().getPlayingPgmId(), "", "");
        }
        
        super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
        mHandler.removeCallbacks(rSetWndBgDelay);
        mHandler.removeCallbacks(rSetStandbyDelay);

        if (mBgBmp != null && !mBgBmp.isRecycled())
        {
            mBgBmp.recycle();
            mBgBmp = null;
        }
        
        if (mStandbyBmp != null && !mStandbyBmp.isRecycled())
        {
            mStandbyBmp.recycle();
            mStandbyBmp = null;
        }
        
        if (mSubWndCollection != null)
        {
            for (PosterBaseView wnd : mSubWndCollection)
            {
                wnd.onViewDestroy();
            }
        }
        
        // 清空上一个节目的缓存
        PosterApplication.clearMemoryCache(getActivity());
        
        super.onDestroy();
    }

    /**
     * Set the picture for standby screen when has no program.
     */
    private boolean setStandbyScreen()
    {
        mHandler.removeCallbacks(rSetStandbyDelay);
        
        mStandbyBmp = PosterApplication.getInstance().getStandbyScreenImage();
        if (mMainLayout == null || mStandbyBmp == null)
        {
            mHandler.postDelayed(rSetStandbyDelay, 500);
            return false;
        }
        else
        {
            mMainLayout.setBackground(new BitmapDrawable(getResources(), mStandbyBmp));
        }
        
        return true;
    }
    
    /**
     * Set the background picture of the window.
     */
    private boolean setWindowBackgroud()
    {
        mHandler.removeCallbacks(rSetWndBgDelay);
        
        if (mMainLayout == null)
        {
            Logger.i("Main layout didn't ready, can't load background image.");
            mHandler.postDelayed(rSetWndBgDelay, 500);
            return false;
        }
        else if (!FileUtils.isExist(mBgImgInfo.filePath))
        {
            Logger.i("Background Image [" + mBgImgInfo.filePath + "] didn't exist.");
            PosterBaseView.downloadMedia(mBgImgInfo);
            mHandler.postDelayed(rSetWndBgDelay, 500);
            return false;
        }
        else if (!PosterBaseView.md5IsCorrect(mBgImgInfo))
        {
            Logger.i("Background Image [" + mBgImgInfo.filePath + "] verifycode is wrong.");
            PosterBaseView.downloadMedia(mBgImgInfo);
            mHandler.postDelayed(rSetWndBgDelay, 500);
            return false;
        }

        // 读取图片
        mBgBmp = loadBgPicture(mBgImgInfo);
        
        // 图片生成失败
        if (mBgBmp == null)
        {
            mHandler.postDelayed(rSetWndBgDelay, 500);
            return false;
        }
        else
        {
            // 设置背景
            mMainLayout.setBackground(new BitmapDrawable(getResources(), mBgBmp));
        }
        
        return true;
    }
    
    private Bitmap loadBgPicture(final MediaInfoRef picInfo)
    {
        Bitmap srcBmp = null;

        try
        {
            if (picInfo == null || FileUtils.mediaIsPicFromNet(picInfo))
            {
                Log.e("load picture error", "picture is come from network");
                return null;
            }

            // Create the Stream
            InputStream isImgBuff = PosterBaseView.createImgInputStream(picInfo);

            try
            {
                if (isImgBuff != null)
                {
                    // Create the bitmap for BitmapFactory
                    srcBmp = BitmapFactory.decodeStream(isImgBuff, null, PosterBaseView.setBitmapOption(picInfo));
                }
            }
            catch (java.lang.OutOfMemoryError e)
            {
                Logger.e("picture is too big, out of memory!");

                if (srcBmp != null && !srcBmp.isRecycled())
                {
                    srcBmp.recycle();
                    srcBmp = null;
                }
                
                System.gc();
            }
            finally
            {
                if (isImgBuff != null)
                {
                    isImgBuff.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return srcBmp;
    }

    public void startAudio()
    {
    	if (mSubWndCollection != null)
        {
            for (PosterBaseView wnd : mSubWndCollection)
            {
                if (wnd.getViewName().startsWith("Audio"))
                {
                	wnd.onViewResume();
                }
            }
        }
    }
    
    public void stopAudio()
    {
    	if (mSubWndCollection != null)
        {
            for (PosterBaseView wnd : mSubWndCollection)
            {
                if (wnd.getViewName().startsWith("Audio"))
                {
                	wnd.onViewPause();
                }
            }
        }
    }

    /**
     * 如果背景图片不存在，则轮循检测图片文件是否下载完成.
     */
    private Runnable rSetWndBgDelay   = new Runnable() {
                                          @Override
                                          public void run()
                                          {
                                              setWindowBackgroud();
                                          }
                                      };
    
    private Runnable rSetStandbyDelay = new Runnable() {
                                          @Override
                                          public void run()
                                          {
                                              setStandbyScreen();
                                          }
                                      };
    
    public Bitmap combineScreenCap(Bitmap bitmap)
    {
        for (PosterBaseView wnd : mSubWndCollection)
        {
            if (wnd.getViewName().startsWith("Main") && wnd instanceof MultiMediaView)
            {
                Bitmap videoCap = ((MultiMediaView) wnd).getVideoCap();
                if (videoCap != null)
                {
                    Bitmap newb = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                    Canvas cv = new Canvas(newb);
                    cv.drawBitmap(bitmap, 0, 0, null);
                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    cv.drawBitmap(videoCap, wnd.getXPos(), wnd.getYPos(), paint);
                    cv.save(Canvas.ALL_SAVE_FLAG);
                    cv.restore();
                    return newb;
                }
            }
        }
        
        return bitmap;
    }
}
