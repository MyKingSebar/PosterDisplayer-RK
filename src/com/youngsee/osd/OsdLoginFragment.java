/*
 * Copyright (C) 2013 poster PCE YoungSee Inc.
 * All Rights Reserved Proprietary and Confidential.
 * 
 * @author LiLiang-Ping
 */

package com.youngsee.osd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.youngsee.common.SysParamManager;
import com.youngsee.common.YSConfiguration;
import com.youngsee.posterdisplayer.PosterApplication;
import com.youngsee.posterdisplayer.R;
import com.youngsee.posterdisplayer.PosterOsdActivity;

public class OsdLoginFragment extends Fragment
{
	static final String OSD_DEFAULT_UN    = "ehualu";
    static final String OSD_DEFAULT_PWD    = "ehualu$888";
    
    private Editor              mEditor            = null;
    private SharedPreferences   mSharedPreferences = null;
    private LinearLayout        mOsdLayout         = null;
    private ImageView           mOsdLoginBtn       = null;
    private EditText            mEnterUn           = null;
    private EditText            mEnterPwd          = null;
    private int                 mOsdMenuId         = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null)
        {
            mOsdMenuId = args.getInt("osdMenuId");
        }
    }
    
    /**
     * Create the view for this fragment, using the arguments given to it.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // 不能将Fragment的视图附加到此回调的容器元素，因此attachToRoot参数必须为false
        View view = inflater.inflate(R.layout.fragment_osd_login, container, false);
        String code = PosterApplication.getInstance().getConfiguration().getFeatureCode();
        if(code != null && code.equalsIgnoreCase(YSConfiguration.FEATURE_CODE_YUESHI)){
            view.findViewById(R.id.RLLoginRoot).setBackgroundResource(R.drawable.welcome1_ys);
        }
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        initLoginOsdFragment();
        
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    
    /*
     * Some of the initialization operation
     */
    private void initLoginOsdFragment()
    {
        mSharedPreferences = getActivity().getSharedPreferences(PosterOsdActivity.OSD_CONFIG, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        if (mSharedPreferences.getString(PosterOsdActivity.OSD_PASSWORD, null) == null)
        {
            String spwd = SysParamManager.getInstance().getSysPasswd();
            mEditor.putString(OSD_DEFAULT_PWD, spwd);
            mEditor.commit();
        }
        
        mOsdLayout = (LinearLayout) getActivity().findViewById(R.id.osd_layout);
        ViewTreeObserver vto = getView().getViewTreeObserver();  
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){ 
            @SuppressWarnings("deprecation")
            @Override 
		    public void onGlobalLayout() { 
		    	getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
		    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			    	RelativeLayout.LayoutParams osdLayoutParams = new RelativeLayout.LayoutParams(
							(int)(getView().getWidth()*(15.6f/47.8f)), (int)(getView().getHeight()*(8f/27f)));
			    	mOsdLayout.setLayoutParams(osdLayoutParams);
			    	mOsdLayout.setX(getView().getWidth()*(16.1f/47.8f));
			    	mOsdLayout.setY(getView().getHeight()*(14.2f/27f));
		    	} else {
		    		RelativeLayout.LayoutParams osdLayoutParams = new RelativeLayout.LayoutParams(
							(int)(getView().getWidth()*(17.3f/27f)), (int)(getView().getHeight()*(9.3f/47.8f)));
			    	mOsdLayout.setLayoutParams(osdLayoutParams);
			    	mOsdLayout.setX(getView().getWidth()*(4.85f/27f));
			    	mOsdLayout.setY(getView().getHeight()*(20.2f/47.8f));
		    	}
		    }  
		});
        
        mOsdLoginBtn = (ImageView) getActivity().findViewById(R.id.osd_login);
        mEnterPwd = (EditText) getActivity().findViewById(R.id.osd_password);
        mEnterUn = (EditText) getActivity().findViewById(R.id.osd_username);
        
        mOsdLoginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
            	if (mSharedPreferences.getString(PosterOsdActivity.OSD_USERNAME, OSD_DEFAULT_UN).equals(mEnterUn.getText().toString())
                		&& mSharedPreferences.getString(PosterOsdActivity.OSD_PASSWORD, OSD_DEFAULT_PWD).equals(mEnterPwd.getText().toString()))
                {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (imm.isActive())
                    {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    
                    if (getActivity() instanceof PosterOsdActivity)
                    {
                        ((PosterOsdActivity) getActivity()).startOsdMenuFragment(mOsdMenuId);
                    }
                }
                else if (mEnterUn.getText().toString().trim().equals("")
                		|| mEnterPwd.getText().toString().trim().equals(""))
                {
                    Toast.makeText(getActivity(), R.string.login_dialog_unnullmsg, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), R.string.login_dialog_unmsgerror, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
