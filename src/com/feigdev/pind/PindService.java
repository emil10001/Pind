package com.feigdev.pind;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import com.feigdev.webcom.SimpleResponse;
import com.feigdev.webcom.WebComListener;
import com.feigdev.webcom.WebModel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class PindService extends Service implements WebComListener{
	private static final String TAG = "PindService";
	private PindResponseListener prl;
	private Prefs prefs;
	private static final int POST_LOGIN = 20124;
	private static final int POST_LIKE = 20126;
	private static final int POST_COMMENT = 20127;
	private static final int POST_REPIN = 20128;
	private static final int GET_BOARDS = 20129;
	private static final int POST_PIN = 20130;
	private static final String CLIENT_ID = "";
	private static final String CLIENT_SECRET = "";
	
	private static final int GRAB_COUNT = 24;
	private int page_count = 1;
	private String curSearch = "";
	private int curCategory = Constants.POPULAR;
	private Context context;
	private HashMap<String,String> availableBoards;
	private HashMap<Integer,GridContent> gridContent;
	private HashMap<String,PinItem> pins;
	private HashMap<String,Boolean> images;
	
	@Override
    public void onCreate() {
		super.onCreate();
		if (Constants.DEBUG){ Log.d(TAG,"onCreate called"); }
    }
	
	@Override
    public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (Constants.DEBUG){ Log.d(TAG,"onStart called");}
	}

    @Override
    public void onDestroy() {
    	if (Constants.DEBUG){ Log.d(TAG,"onDestroy called");}
//		cleanSD();
    	super.onDestroy();
    }
	
    public void init(Context c) {
    	context = c; 
    	availableBoards = new HashMap<String,String>();
		if (Constants.DEBUG){ Log.d(TAG,"constructor called"); }
		prefs = new Prefs(context);
		gridContent = new HashMap<Integer,GridContent>();
		pins = new HashMap<String,PinItem>();
		images = new HashMap<String,Boolean>();
		if (isLoggedIn()){
			getBoards();
			request(Constants.FOLLOWING);
		}
		else {
			request(Constants.POPULAR);
		}
	}
	
    public void cleanSD(){
    	try{
	    	deleteRecursive(new File(Calculator.getDefaultFilePath()));
	    } catch (NullPointerException e){
			if (Constants.DEBUG){ Log.d(TAG,"cleanSD failed with null pointer");}
		}
    }
    
    void deleteRecursive(File fileOrDirectory) {
    	try {
	        if (fileOrDirectory.isDirectory())
	            for (File child : fileOrDirectory.listFiles())
	                deleteRecursive(child);
	
	        fileOrDirectory.delete();
    	} catch (NullPointerException e){
    		if (Constants.DEBUG){ Log.d(TAG,"deleteRecursive failed with null pointer");}
    	}
    }
    
    public GridContent getGridContent(int view){
    	curCategory = view;
    	if (null == gridContent.get(view)){
    		gridContent.put(view,new GridContent());
    		request(view);
    	}
    	else if (gridContent.get(view).size() < 1){
    		request(view);
    	}
    	return gridContent.get(view);
    }
    
    public PindResponseListener getPrl(){
    	return prl;
    }
    
    public void setPrl(PindResponseListener prl){
    	this.prl = prl;
		init(PindService.this);
    }
    
    public void kill(){
    	prl = null;
    }
    
	Handler handler = new Handler();
	
	public PinItem getPinItem(String pinId){
		return pins.get(pinId);
	}
	
	public boolean isLoggedIn(){
		return prefs.isLoggedIn();
	}
	public boolean isFbShare(){
		return prefs.isFbShare();
	}
	public boolean isTwShare(){
		return prefs.isTwShare();
	}
	public void setFbShare(boolean b){
		prefs.setFbShare(b);
	}
	public void setTwShare(boolean b){
		prefs.setTwShare(b);
	}
	public void logout(){
		prefs.resetAll();
		if (null != prl){prl.onLogoutSuccess();}
	}

	public void postLogin(String email, String pass){
		if (Constants.DEBUG){Log.d(TAG,"postLogin called");}
		String url = "https://api.pinterest.com/v2/oauth/access_token?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
		WebModel wm = new WebModel(url, this, POST_LOGIN);
    	wm.setContentType("application/x-www-form-urlencoded");
    	wm.setRequestType(WebModel.POST_AUTH);
    	wm.setUsername(email);
    	prefs.setEmail(email);
    	wm.setPassword(pass);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("Accept-Encoding","gzip");
    	wm.addHeadParam("X-Requested-With", "XMLHttpRequest");
    	wm.addHeadParam("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
    	wm.addParameter("grant_type", "password");
    	wm.addParameter("scope","read_write");
    	wm.addParameter("redirect_uri","http://pinterest.com/about/iphone");
    	
    	wm.interact();
	}
	public void updateCategory(int category){
		if (category != curCategory){
			page_count = 1;
		}
		curCategory = category;
	}
	
	public void likePin(String spi, boolean likeIt){
		if (Constants.DEBUG){Log.d(TAG,"likePin called");}
		String url;
		if (likeIt){
			url = "https://api.pinterest.com/v2/pin/"+spi+"/like/";
		}
		else {
			url = "https://api.pinterest.com/v2/pin/"+spi+"/unlike/";
		}
		WebModel wm = new WebModel(url, this, POST_LIKE);
    	wm.setContentType("text/json");
    	wm.setRequestType(WebModel.POST);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("Accept-Encoding","gzip");
    	wm.addHeadParam("X-Requested-With", "XMLHttpRequest");
    	wm.addHeadParam("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
    	wm.addParameter("access_token", prefs.getAccessToken());
    	wm.interact();
	}
	public void commentPin(String spi, String comment){
		if (Constants.DEBUG){Log.d(TAG,"commentPin called");}
		String url = "https://api.pinterest.com/v2/pin/"+spi+"/comment/";
		
		WebModel wm = new WebModel(url, this, POST_COMMENT);
    	wm.setContentType("text/json");
    	wm.setRequestType(WebModel.POST);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("Accept-Encoding","gzip");
    	wm.addHeadParam("X-Requested-With", "XMLHttpRequest");
    	wm.addHeadParam("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
    	wm.addParameter("access_token", prefs.getAccessToken());
    	wm.addParameter("text", comment);
    	wm.interact();
	}
	public void rePin(String spi, String comment, String boardId){
		if (Constants.DEBUG){Log.d(TAG,"rePin called");}
		String url = "https://api.pinterest.com/v2/repin/"+spi+"/";
		
		WebModel wm = new WebModel(url, this, POST_REPIN);
    	wm.setContentType("text/json");
    	wm.setRequestType(WebModel.POST);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("Accept-Encoding","gzip");
    	wm.addHeadParam("X-Requested-With", "XMLHttpRequest");
    	wm.addHeadParam("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
    	wm.addParameter("access_token", prefs.getAccessToken());
    	wm.addParameter("board", boardId);
    	wm.addParameter("details", comment);
    	if (prefs.isFbShare()){
    		wm.addParameter("publish_to_facebook", "1");
    	}
    	if (prefs.isTwShare()){
    		wm.addParameter("publish_to_twitter", "1");
    	}
    	wm.interact();
	}
	public void pinItem(String img_url, String source_url, byte [] b, String comment, String boardId){
		if (Constants.DEBUG){Log.d(TAG,"pinItem called");}
    	if (null != source_url){
    		webPinItem(img_url,source_url,comment,boardId);
    		return;
    	}
		String url = "https://api.pinterest.com/v2/pin/";
		
		WebModel wm = new WebModel(url, this, POST_PIN);
    	wm.setContentType("text/json");
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("Accept-Encoding","gzip");
    	wm.addHeadParam("Proxy-Connection","keep-alive");
		wm.setRequestType(WebModel.POST_FILE);
		try {
			if (Constants.DEBUG){Log.d(TAG,"posting image = " + img_url);}
    		wm.addMParam("access_token", new StringBody(prefs.getAccessToken()));
        	wm.addMParam("board", new StringBody(getBoardId(boardId)));
        	wm.addMParam("details", new StringBody(comment));
        	
        	wm.addMParam("image", new ByteArrayBody(b, img_url + ".jpg"));

    		if (prefs.isFbShare()){
        		wm.addMParam("publish_to_facebook", new StringBody("1"));
        	}
        	if (prefs.isTwShare()){
        		wm.addMParam("publish_to_twitter", new StringBody("1"));
        	}
		} catch (Exception ex){
			if (Constants.DEBUG){Log.d(TAG,"Broke before making request");}
			prl.onPinFailure();
			return;
		}
    	if (prefs.isFbShare()){
    		wm.addParameter("publish_to_facebook", "1");
    	}
    	if (prefs.isTwShare()){
    		wm.addParameter("publish_to_twitter", "1");
    	}
    	wm.interact();
	}
	public void webPinItem(String img_url, String source_url, String comment, String boardId){
		if (Constants.DEBUG){Log.d(TAG,"pinItem called");}
		String url = "https://api.pinterest.com/v2/webpin/";
		
		WebModel wm = new WebModel(url, this, POST_PIN);
		wm.setContentType("text/json");
		wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
		wm.addHeadParam("Host", "api.pinterest.com");
		wm.addHeadParam("Connection", "keep-alive");
		wm.addHeadParam("Accept-Encoding","gzip");
		wm.addHeadParam("Proxy-Connection","keep-alive");
		wm.setRequestType(WebModel.POST);
		try {
			if (Constants.DEBUG){Log.d(TAG,"posting image = " + img_url);}
			wm.addParameter("access_token", prefs.getAccessToken());
	    	wm.addParameter("board", getBoardId(boardId));
        	wm.addParameter("details", comment);
    		wm.addParameter("source", source_url);
    		wm.addParameter("image_url", img_url);
    		wm.addParameter("cameraController", "0");
	
    		if (prefs.isFbShare()){
    			wm.addParameter("publish_to_facebook", "1");
    		}
    		if (prefs.isTwShare()){
    			wm.addParameter("publish_to_twitter", "1");
    		}
		} catch (Exception ex){
			if (Constants.DEBUG){Log.d(TAG,"Broke before making request");}
			prl.onPinFailure();
			return;
		}
		
		wm.interact();
	}
	
	public void getBoards(){
		if (Constants.DEBUG){Log.d(TAG,"postLogin called");}
		String url = "https://api.pinterest.com/v2/boards/?access_token="+prefs.getAccessToken();
		
		WebModel wm = new WebModel(url, this, GET_BOARDS);
    	wm.setContentType("text/json");
    	wm.setRequestType(WebModel.GET);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.interact();
	}
	public void refresh(){
		request(curCategory);
	}
	private void request(int category){
		if (null == prl){ return; }
		if (Constants.DEBUG){Log.d(TAG,"request(" + category +")");}
		
		if (category != curCategory){
			page_count = 1;
		}
		curCategory = category;
		
		if (null == gridContent.get(category)){
			gridContent.put(category, new GridContent());
		}
		if (1 == page_count){
			if (gridContent.get(category).size() > 1){
				prl.onUpdateCurView();
			}
		}
		String url = buildUrl(category);
		WebModel wm = new WebModel(url, this, category);
		
    	wm.setContentType("text/json");
    	wm.setRequestType(WebModel.GET);
    	wm.addHeadParam("User-Agent","Pinterest For iPhone / 1.5.1");
    	wm.addHeadParam("User-Agent","Pind");
    	wm.addHeadParam("Host", "api.pinterest.com");
    	wm.addHeadParam("Connection", "keep-alive");
    	wm.addHeadParam("X-Requested-With", "XMLHttpRequest");
    	wm.addHeadParam("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
    	wm.addParameter("api_type", "json");
    	wm.interact();
		if (Constants.DEBUG){Log.d(TAG,"request url: " + url);}

	}
	
	private String buildUrl(int category){
		String url = "https://api.pinterest.com/v2/";
		switch (category){
		case  Constants.ARCHITECTURE:
			url += "all/?category=architecture&"; 
			break;
	    case  Constants.ART:
			url += "all/?category=art&"; 
			break;
	    case  Constants.CARS_MOTORCYCLES:
			url += "all/?category=cars_motorcycles&"; 
	    	break;
	    case  Constants.DESIGN:
			url += "all/?category=design&"; 
	    	break;
	    case  Constants.DIY_CRAFTS:
			url += "all/?category=diy_crafts&"; 
	    	break;
	    case  Constants.EDUCATION:
			url += "all/?category=education&"; 
	    	break;
	    case  Constants.FILM_MUSIC_BOOKS:
			url += "all/?category=film_music_books&"; 
	    	break;
	    case  Constants.FITNESS:
			url += "all/?category=fitness&"; 
	    	break;
	    case  Constants.FOOD_DRINK:
			url += "all/?category=food_drink&"; 
	    	break;
	    case  Constants.GARDENING:
			url += "all/?category=gardening&"; 
	    	break;
	    case  Constants.GEEK:
			url += "all/?category=geek&"; 
	    	break;
	    case  Constants.HAIR_BEAUTY:
			url += "all/?category=hair_beauty&"; 
	    	break;
	    case  Constants.HISTORY:
			url += "all/?category=history&"; 
	    	break;
	    case  Constants.HOLIDAYS:
			url += "all/?category=holidays&"; 
	    	break;
	    case  Constants.HOME:
			url += "all/?category=home&"; 
	    	break;
	    case  Constants.HUMOR:
			url += "all/?category=humor&"; 
	    	break;
	    case  Constants.KIDS:
			url += "all/?category=kids&"; 
	    	break;
	    case  Constants.MYLIFE:
			url += "all/?category=mylife&"; 
	    	break;
	    case  Constants.WOMEN_APPERAL:
			url += "all/?category=women_apparel&"; 
	    	break;
	    case  Constants.MEN_APPERAL:
			url += "all/?category=men_apparel&"; 
	    	break;
	    case  Constants.OUTDOORS:
			url += "all/?category=outdoors&"; 
	    	break;
	    case  Constants.PEOPLE:
			url += "all/?category=people&"; 
	    	break;
	    case  Constants.PETS:
			url += "all/?category=pets&"; 
	    	break;
	    case  Constants.PHOTOGRAPHY:
			url += "all/?category=photography&"; 
	    	break;
	    case  Constants.SPORTS:
			url += "all/?category=sports&"; 
	    	break;
	    case  Constants.TECHNOLOGY:
			url += "all/?category=technology&"; 
	    	break;
	    case  Constants.TRAVEL_PLACES:
			url += "all/?category=travel_places&"; 
	    	break;
	    case  Constants.WEDDING_EVENTS:
			url += "all/?category=wedding_events&"; 
	    	break;
	    case  Constants.OTHER:
			url += "all/?category=other&"; 
	    	break;
	    case Constants.EVERYTHING:
	    	url += "all/?";
	    	break;
		case Constants.POPULAR:
			url += "popular/?"; 
    		break;
		case Constants.SEARCH:
			url += "search/pins/?query=" + curSearch +"&";
			break;
		case Constants.FOLLOWING:
			url += "home/?";
			break;
		case Constants.VIDEO:
			url += "video/?";
			break;
		case Constants.GIFTS:
			url += "gifts/?";
			break;
		default:
			url += "all/?";
			break;
		}
		url += "access_token=" + prefs.getAccessToken() + "&";

		url += "limit=" + GRAB_COUNT + "&";
		url += "page="  + page_count;

		page_count++;
		if (page_count > 5){
			page_count = 1;
		}
		
		return url;
	}
	
	public void requestSearch(String search) {
		page_count = 1;
		gridContent.put(Constants.SEARCH, new GridContent());
		try{ System.gc(); } catch (Exception e){}
		curSearch = search.replaceAll("\n","");
		curSearch = curSearch.replaceAll("\r","");
		curSearch = curSearch.replaceAll(" ","+");
		curSearch = curSearch.replaceAll(",","+");
		curSearch = curSearch.replaceAll("\\+{2,}+","+");
		request(Constants.SEARCH);
    }
	
	@Override
	public void onResponse(final SimpleResponse response) {
		if (null != response && null != prl){
    		switch (response.getId()){
    		case POST_LOGIN:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"Login failed");}
    				prl.onLoginFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"login response: " + response.getMessage());}
        			parseLogin(response.getMessage());
        			if (prefs.isLoggedIn()){
        				prl.onLoginSuccess();
        			}
        			else {
        				prl.onLoginFailure();
        			}
    			}
    			break;
    		case POST_LIKE:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"like failed");}
    				prl.onLikeFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"like response: " + response.getMessage());}
       				prl.onLikeSuccess();
    			}
    			break;
    		case POST_COMMENT:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"comment failed");}
    				prl.onCommentFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"comment response: " + response.getMessage());}
       				prl.onCommentSuccess();
    			}
    			break;
    		case POST_REPIN:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"repin failed");}
    				prl.onRepinFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"repin response: " + response.getMessage());}
       				prl.onRepinSuccess();
    			}
    			break;
    		case POST_PIN:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"pin failed");}
    				prl.onPinFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"pin response: " + response.getMessage());}
       				prl.onPinSuccess();
    			}
    			break;
    		case GET_BOARDS:
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (Constants.DEBUG){Log.d(TAG,"boards failed");}
    				prl.onBoardsFailure();
    			}
    			else {
    				if (Constants.DEBUG){Log.d(TAG,"boards response: " + response.getMessage());}
    				availableBoards = PindJsonParser.parseBoards(response.getMessage());
    				prl.onBoardsSuccess();
    			}
    			break;
    		default:
    			if (Constants.DEBUG){Log.d(TAG,"response " + response.getMessage());}
    			if (response.getStatus() == SimpleResponse.FAIL){
    				if (prl != null) {prl.onRequestFailure(response.getId());}
    				else { return; }
    			}
    			else {
    				if (Constants.DEBUG){ Log.d(TAG,"request " +curCategory+ " response.id:" + response.getId() + " response: " + response.getMessage()); }
    				if (!PindJsonParser.parsePinWall(response.getMessage(), gridContent, pins, images, response.getId(), this)){
    					if (Constants.DEBUG){ Log.d(TAG,"parsing failed");}
    				}
    				else {
    					if (prl != null && 0 == gridContent.get(curCategory).size()){
    						prl.onRequestFailure(response.getId());
    					}
    					else if (prl != null) {prl.onUpdateCurView();}
//    					if (Constants.DEBUG){ Log.d(TAG,"gridContent(" + curCategory + ").size = " + gridContent.get(curCategory).size());}
    				}
    			}
    	    	break;
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	// This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    public final IBinder mBinder = new LocalBinder();
	
	 /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	public PindService getService() {
            return PindService.this;
        }
    }

	public void setUserAgent(String userAgent) {
		prefs.setUserAgent(userAgent);
	}
	
	private void parseLogin(String obj){
		if (obj.contains("access_token")){
			prefs.setAccessToken(obj.replace("access_token=",""));
		}
	}

	public Set<String> getBoardKeys(){
		if (null == availableBoards){
			availableBoards = new HashMap<String,String>();
			return null;
		}
		if (null == availableBoards.keySet()){
			return null;
		}
		return availableBoards.keySet();
	}
	
	public String getBoardId(String key){
		return availableBoards.get(key);
	}
}
