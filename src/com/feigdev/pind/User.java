package com.feigdev.pind;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class User {
	Bitmap image = null;
	JSONObject jUser = null;
	
	public User (JSONObject obj){
		jUser = obj;
	}
	
	public void setJUser(JSONObject obj){
		jUser = obj;
	}
	public JSONObject getJUser(){
		return jUser;
	}
	
	public String getUsername() {
		return jUser.optString(PindJsonParser.USERNAME);
	}
	public String getImage_url() {
		return jUser.optString(PindJsonParser.IMAGE_URL);
	}
	public String getImage_large_url() {
		return jUser.optString(PindJsonParser.IMAGE_LARGE_URL);
	}
	public Bitmap genImage(){
		if (null == jUser){
			return null;
		}
		if (null == jUser.optString(PindJsonParser.IMAGE_URL)){
			try {
				jUser.put(PindJsonParser.IMAGE_URL, "http://files.feigdev.com/pind_icon.png");
			} catch (JSONException e) {
				return null;
			}
		}
		Thread t = new Thread(new Runnable() {
	        public void run() {
				image = Calculator.generateBmp(jUser.optString(PindJsonParser.IMAGE_URL));
	        }
	    });
		t.start();
		while (t.isAlive()){}
		return image;
	}
	public void setImage(Bitmap img){
		image = img;
	}
	public Bitmap getImage(){
		return image;
	}
	public String getFull_name() {
		return jUser.optString(PindJsonParser.FULL_NAME);
	}
	public String getId() {
		return jUser.optString(PindJsonParser.ID);
	}
	
}
