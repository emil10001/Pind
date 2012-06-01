package com.feigdev.pind;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
	private SharedPreferences app_preferences;
	private SharedPreferences.Editor editor;
	private Context context;
	public static final String OAUTH = "oauth";
	public static final String EMAIL = "email";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String PING_TIME = "ping_time";
	public static final String USER_AGENT = "user_agent";
	public static final String FB_SHARE = "facebook_share";
	public static final String TW_SHARE = "twitter_share";
	
	/***
	 * Constructor for the class. This will initialize any null variables
	 *  
	 * @param context Need to pass in the current context, usually 'this'
	 */
	public Prefs(Context context){
		this.context = context;
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = app_preferences.edit();
        if (!app_preferences.contains(OAUTH)){
        	this.resetOAuth();
        }
        if (!app_preferences.contains(EMAIL)){
        	this.resetEmail();
        }
        if (!app_preferences.contains(ACCESS_TOKEN)){
        	this.resetAccessToken();
        }
        if (!app_preferences.contains(USER_AGENT)){
        	this.resetUserAgent();
        }
        if (!app_preferences.contains(PING_TIME)){
        	this.setPingTime(1000*60);
        }
        if (!app_preferences.contains(FB_SHARE)){
        	this.resetFbShare();
        }
        if (!app_preferences.contains(TW_SHARE)){
        	this.resetTwShare();
        }
	}
	
	public void resetAll(){
		this.resetOAuth();
		this.resetEmail();
		this.resetAccessToken();
		this.resetUserAgent();
		this.setPingTime(1000*60);
		this.resetFbShare();
		this.resetTwShare();
	}

	public boolean isLoggedIn() {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (!ACCESS_TOKEN.equals(app_preferences.getString(ACCESS_TOKEN, ACCESS_TOKEN))){
			return true;
		}
		return false;
	}
	public boolean isFbShare() {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getBoolean(FB_SHARE, false);
	}
	public boolean isTwShare() {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getBoolean(TW_SHARE, false);
	}
	public void setFbShare(boolean b){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(FB_SHARE)){
        	editor.remove(FB_SHARE);
        }
		editor.putBoolean(FB_SHARE, b);
        editor.commit();
	}
	public void setTwShare(boolean b){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(TW_SHARE)){
        	editor.remove(TW_SHARE);
        }
		editor.putBoolean(TW_SHARE, b);
        editor.commit();
	}
	public void resetFbShare(){
		setFbShare(false);
	}

	public void resetTwShare(){
		setTwShare(false);
	}

	public String getOAuth(){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getString(OAUTH, OAUTH);
	}
	public String getEmail(){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getString(EMAIL, EMAIL);
	}
	public String getAccessToken(){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getString(ACCESS_TOKEN, ACCESS_TOKEN);
	}
	public String getUserAgent(){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getString(USER_AGENT, USER_AGENT);
	}
	public int getPingTime(){
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return app_preferences.getInt(PING_TIME, 300000);
	}
	
	public void setPingTime(int sIn) {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(PING_TIME)){
        	editor.remove(PING_TIME);
        }
        editor.putInt(PING_TIME, sIn);
        editor.commit(); 
    }
	public void setEmail(String sIn) {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(EMAIL)){
        	editor.remove(EMAIL);
        }
        editor.putString(EMAIL, sIn);
        editor.commit(); 
    }
	public void setOAuth(String sIn) {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(OAUTH)){
        	editor.remove(OAUTH);
        }
        editor.putString(OAUTH, sIn);
        editor.commit(); 
    }
	public void setUserAgent(String sIn) {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(USER_AGENT)){
        	editor.remove(USER_AGENT);
        }
        editor.putString(USER_AGENT, sIn);
        editor.commit(); 
    }
	public void resetOAuth() {
		setOAuth(OAUTH);
    }
	public void resetEmail() {
		setEmail(EMAIL);
    }
	public void resetUserAgent() {
		setUserAgent(USER_AGENT);
    }

	public void setAccessToken(String sIn) {
		app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (app_preferences.contains(ACCESS_TOKEN)){
        	editor.remove(ACCESS_TOKEN);
        }
        editor.putString(ACCESS_TOKEN, sIn);
        editor.commit(); 
    }
	
	public void resetAccessToken() {
		setAccessToken(ACCESS_TOKEN);
    }
	
}