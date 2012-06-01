package com.feigdev.pind;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.feigdev.pind.GridContent.SmallPinItem;
import com.feigdev.pind.PinItem.Comment;

public class PindJsonParser {
	public static final String TAG = "PindJsonParser";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String PINS = "pins";
	public static final String DOMAIN = "domain";
	public static final String DESCRIPTION = "description";
	public static final String USER = "user";
	public static final String IMAGES = "images";
	public static final String COUNTS = "counts";
	public static final String ID = "id";
	public static final String SIZES = "sizes";
	public static final String CREATED_AT = "created_at";
	public static final String COMMENTS = "comments";
	public static final String IS_REPIN = "is_repin";
	public static final String SOURCE = "source";
	public static final String BOARD = "board";
	public static final String BOARDS = "boards";
	public static final String IS_VIDEO = "is_video";
	public static final String USERNAME = "username";
	public static final String IMAGE_URL = "image_url";
	public static final String IMAGE_LARGE_URL = "image_large_url";
	public static final String FULL_NAME = "full_name";
	public static final String MOBILE = "mobile";
	public static final String CLOSEUP = "closeup";
	public static final String THUMBNAIL = "thumbnail";
	public static final String REPINS = "repins";
	public static final String LIKES = "likes";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String USER_ID = "user_id";
	public static final String NAME = "name";
	public static final String PAGINATION = "pagination";
	public static final String NEXT = "next";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	
	public static int parseLoginResponse(String obj, Prefs prefs){
		try {
			JSONObject jObj = new JSONObject(obj);
			prefs.setOAuth(jObj.getString(ACCESS_TOKEN));
			return 0;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	public static SmallPinItem buildSPI(JSONObject pItem, int rownum, GridContent gc) throws JSONException{
		return gc.new SmallPinItem(pItem.optString(ID), 
				rownum, 
				Calculator.getFileFromUrl(pItem.optJSONObject(IMAGES).optString(THUMBNAIL)),
				pItem.optJSONObject(IMAGES).optString(THUMBNAIL));
	}
	
	public static GridContent miniParse(String obj){
		GridContent gc = new GridContent();
		try {
			JSONObject jObj = new JSONObject(obj);
			gc.setNext_page_url(jObj.getJSONObject(PAGINATION).optString(NEXT));
			JSONArray pins = jObj.getJSONArray(PINS);
			JSONObject pItem;
			for (int i=0; i<pins.length(); i++){
				pItem = pins.getJSONObject(i);
				gc.add(gc.new SmallPinItem(pItem.optString(ID), 
						0, 
						Calculator.getFileFromUrl(pItem.optJSONObject(IMAGES).optString(THUMBNAIL)),
						pItem.optJSONObject(IMAGES).optString(THUMBNAIL)) );
			}
			
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		} finally {
			System.gc();
		}
		
		return gc;
	}
	
	public static boolean parsePinWall(String obj, HashMap<Integer,GridContent> gridContent, HashMap<String,PinItem> hashPins, HashMap<String,Boolean> hashImages, int view, PindService ps){
		PinItem pi;
		try {
			JSONObject jObj = new JSONObject(obj);
			if (jObj.optString("error").equals("Invalid OAuth token")){
				if (ps.getPrl() != null){ 
					ps.getPrl().onUnAuthenticated(); 
				}
				else { return false; }
			}
			JSONArray pins = jObj.getJSONArray(PINS);
			if (null == gridContent.get(view)){
				gridContent.put(view, new GridContent());
			}
			if (pins.length() < 1){
				ps.getPrl().onSearchFailure();
			}
			for (int i=0; i<pins.length(); i++){
				pi = genPinItem(pins.getJSONObject(i));
				if (!hashImages.containsKey(pi.getThumb())){
					hashImages.put(pi.getThumb(), true);
					if (pi.hasImages()){
						hashPins.put(pi.getId(), pi);
						SmallPinItem spi = gridContent.get(view).new SmallPinItem(pi.getId(), 
								0, 
								pi.getImages().getThumb_loc(), 
								pi.getThumb());
						
						gridContent.get(view).add(spi);
						if (null == ps.getPrl()){ return false; }
					}
					else if (Constants.DEBUG){ Log.d(TAG, "pin has no images " + pi.getId()); }
				}
				else if (Constants.DEBUG){ Log.d(TAG, "Discarding duplicate pin " + pi.getId()); }
			}
			
		} catch (JSONException e){
			e.printStackTrace();
			return false;
		} finally {
			System.gc();
		}
		
		return true;
	}
	public static PinItem genPinItem(JSONObject pItem){
		PinItem pi;
		try {
			JSONArray comObj;
			JSONObject commentObj;
			Comment comment;
			pi = new PinItem();
			pi.setPinItem(pItem);
			pi.getImages().setThumb_loc(Calculator.getFileFromUrl(pItem.optJSONObject(IMAGES).optString(THUMBNAIL)));
			pi.getImages().setMob_loc(Calculator.getFileFromUrl(pItem.optJSONObject(IMAGES).optString(MOBILE)));
			pi.getUser().setJUser(pItem.getJSONObject(USER));
			pi.getBoard().setBoard(pItem.getJSONObject(BOARD));
			comObj = pItem.optJSONArray(COMMENTS);
			if (null != comObj){
				for (int j=0; j<comObj.length(); j++){
					commentObj = comObj.getJSONObject(j);
					comment = pi.newComment(commentObj);
					comment.getUser().setJUser(commentObj.getJSONObject(USER)); 
					pi.getComments().add(comment);
				}
			}
			
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		} finally {
			System.gc();
		}
		
		return pi;
	}
	
	public static HashMap<String,String> parseBoards(String response){
		HashMap<String,String> b = new HashMap<String,String>();
		try {
			JSONObject boards = new JSONObject(response);
			JSONArray bA = boards.getJSONArray(BOARDS);
			for (int i=0; i< bA.length(); i++ ){
				b.put(bA.getJSONObject(i).getString(NAME), bA.getJSONObject(i).getString(ID));
			}
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		
		return b;
	}
	
}
