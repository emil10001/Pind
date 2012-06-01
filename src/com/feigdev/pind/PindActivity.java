package com.feigdev.pind;

import com.feigdev.pind.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import com.feigdev.pind.CreatePin.CreatePinListener;
import com.feigdev.pind.DetailsFragment.DetailsActionListener;
import com.feigdev.pind.GridContent.SmallPinItem;
import com.feigdev.pind.GridFragment.OnItemSelectedListener;
import com.feigdev.pind.ImageScraper.ImageScraperListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class PindActivity extends FragmentActivity 
	implements PindResponseListener, DetailsActionListener, ImageScraperListener,
	CreatePinListener, OnItemSelectedListener {
	
	private static final String TAG = "PindActivity";
	private PindService mBoundService;
	private int curView = 0;
	private int prevView = 0;
	private GridListener gListener;
	private int curCategory = 0;
	private static final int CAMERA_PIC_REQUEST = 1337; 
	private static final int ACTIVITY_SELECT_IMAGE = 1338;
	private File _photoFile;
	private Uri _fileUri;
    private ProgressDialog pDialog;
    
    public static ContentResolver PACKAGE_RESOLVER;
    public static String PACKAGE_NAME;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String storageState = Environment.getExternalStorageState();
        if(!storageState.equals(Environment.MEDIA_MOUNTED)) {
        	webErrorDialog("ERROR: No SD Card","Ok","Exit","No SD card found. This app stores all of the downloaded images to the SD card. Please exit the app and make sure that your SD card is available before launching again.");
        }
        PACKAGE_NAME = getApplicationContext().getPackageName();
        PACKAGE_RESOLVER = getContentResolver();
        
        startService(new Intent(PindActivity.this, PindService.class));
        doBindService();
		pDialog = ProgressDialog.show(PindActivity.this, "", "Loading. Please wait...", true);
		pDialog.dismiss();

        if (0==curView){
        	curView = Constants.POPULAR;
        }
        if (0==prevView){
        	prevView = Constants.POPULAR;
        }
        if (0==curCategory){
        	curCategory = Constants.ARCHITECTURE;
        }
        
    	getSupportActionBar().setDisplayUseLogoEnabled(false);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	
        showGreeting();
    }
    

    /***
	 * Initiates a connection to the RedditMailService
	 * 
	 * registers the local handler with the service
	 * 
	 * registers the remote handler with this class
	 *   
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
	    @Override
		public void onServiceConnected(ComponentName className, IBinder service) {
	    	if (Constants.DEBUG){ Log.i(TAG, "onServiceConnected called");}
	        mBoundService = ((PindService.LocalBinder)service).getService();
			mBoundService.setPrl(PindActivity.this);
			if (mBoundService != null){
				if (Constants.DEBUG){ Log.i(TAG, "Service connected");}
					handler.post(new Runnable(){
						public void run(){
							onOpen();
						}
					});
			}
	    }

	    @Override
		public void onServiceDisconnected(ComponentName className) {
	        mBoundService = null;
	        if (Constants.DEBUG){ Log.i(TAG, "Service disconnected");}
	    }
	};

	void doBindService() {
		if (Constants.DEBUG){ Log.i(TAG, "doBindService called");}
		Intent intent = new Intent(PindActivity.this, PindService.class);
		if (!getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)){
			if (Constants.DEBUG){ Log.i(TAG, "doBindService failed");}
			throw new IllegalStateException("binding to service failed" + intent);
		}
		else {
			if (Constants.DEBUG){ Log.i(TAG, "doBindService succeeded");}
		}
	}

	void doUnbindService() {
	    if (null != mBoundService) {
	    	mBoundService.kill();
	    	getApplicationContext().unbindService(mConnection);
	    	stopService(new Intent(PindActivity.this, PindService.class));
	    	if (Constants.DEBUG){ Log.i(TAG,"Unbinding");}
	    	mBoundService = null;
	    }
	}
	
	@Override
	public void onStop(){
		doUnbindService();
		super.onStop();
	}
    
    @Override
    public void onDestroy() {
    	if (Constants.DEBUG){ Log.d(TAG,"onDestroy called");}
    	doUnbindService();
    	System.gc();
    	super.onDestroy();
    }
    
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (null == intent.getType()){
          	 return;
           }
        if (Constants.PV){
        	if (Constants.DEBUG) {Log.d(TAG, "onNewIntent called: " + intent.toString());}

            if (Constants.DEBUG) {
            	Log.d(TAG, "onNewIntent called: " + intent.toString());
    	        Log.d(TAG, "Intent type: " + intent.getType());
    	        Log.d(TAG, "Intent categories: " + intent.getCategories());}
            
            // from the browser
            if (intent.getType().equals("text/plain")){
            	if (null != intent.getStringExtra(Intent.EXTRA_TEXT) ){
            		handler.post(new Runnable(){
						public void run(){
							String intentUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
							if (intentUrl.contains("\n")){
								intentUrl = intentUrl.split("\n")[1];
							}
							if (Constants.DEBUG){Log.d(TAG,"onNewIntent.getStringExtra: " + intentUrl);}
							imgScrapeView(intentUrl);
						}
					});
            		return;
            	}
            	else if (null != intent.getExtras().get(Intent.EXTRA_STREAM)){
    			    handler.post(new Runnable(){
    					public void run(){
							if (Constants.DEBUG){Log.d(TAG,"onNewIntent.intent.getExtras().get(Intent.EXTRA_STREAM).toString(): " + intent.getExtras().get(Intent.EXTRA_STREAM).toString());}
    						imgScrapeView(intent.getExtras().get(Intent.EXTRA_STREAM).toString());
        			    }
    				});
    			}
            	else {
            		notifyFailed("Share from web failed", "Something went wrong processing the page. Please try again.");
            		return;
            	}
            }
            // from the gallery
            else if (null != intent.getExtras().get(Intent.EXTRA_STREAM)){
				final String img_loc = ((Uri)intent.getExtras().get(Intent.EXTRA_STREAM)).toString();
			    handler.post(new Runnable(){
					public void run(){
						createPinView(null, intent.getExtras().get(Intent.EXTRA_STREAM).toString(), img_loc,0);
    			    }
				});
			}
        }
    }
    
    private void notifyFailed(final String title, final String message){
    	handler.post(new Runnable(){
			@Override
			public void run() {
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
				getSupportFragmentManager().popBackStack();
				showGreeting();
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				}
				webErrorDialog(title, "Ok", "Exit", message);
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
    	if (mBoundService != null){
    		if (Constants.DEBUG){ Log.i(TAG, "Service connected");}
			if (mBoundService.isLoggedIn()){
				menu.getItem(3).setVisible(false);
				menu.getItem(4).setVisible(true);
				menu.getItem(5).setVisible(true);
				if (Constants.PV){
					menu.getItem(6).setVisible(true);
					menu.getItem(7).setVisible(true);
				}
				else {
					menu.getItem(6).setVisible(false);
					menu.getItem(7).setVisible(false);
				}
			}
			else {
				menu.getItem(3).setVisible(true);
				menu.getItem(4).setVisible(false);
				menu.getItem(5).setVisible(false);
				menu.getItem(6).setVisible(false);
				menu.getItem(7).setVisible(false);
			}
    	}
    	return true;
    }
    
    private void refreshMenu(){
		if (Constants.DEBUG){ Log.d(TAG, "refreshMenu() called");}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
        } 
	}

    private void loginView(){
		updateCurPrevView(Constants.LOGIN,curView,curCategory);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
		hideGreetingForDetails();
		ft.addToBackStack(Constants.LOGIN_STACK)
		.add(android.R.id.content, PindLoginActivity.newInstance(0,mBoundService,this), 
				Constants.LOGIN_STACK)
		.commitAllowingStateLoss();
    }
    
    private void imgScrapeView(String url){
		updateCurPrevView(Constants.IMG_SCRAPE,curView,curCategory);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
		hideGreetingForDetails();
		ft.addToBackStack(Constants.IMG_SCRAPE_STACK)
		.add(android.R.id.content, ImageScraper.newInstance(0,url), 
				Constants.IMG_SCRAPE_STACK)
		.commitAllowingStateLoss();
    }
    
    private void createPinView(final String source_url, final String img_url, final String img_loc, final int recursiveCount){
    	updateCurPrevView(Constants.CREATE_PIN,curView,curCategory);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
		hideGreetingForDetails();
		ArrayList<String> boards = new ArrayList<String>();
		if (recursiveCount > 4){
			toastText("No boards found");
			updateCurPrevView(prevView,Constants.POPULAR,curCategory);
			return;
		}
		if (null == mBoundService){
			doBindService();
			//FIXME - only do this once or twice
			handler.postDelayed(new Runnable(){
				public void run(){
					createPinView(source_url, img_url, img_loc, recursiveCount +1);
			    }
			}, 1000);
			return;
		}
		if (null == mBoundService.getBoardKeys()){
			mBoundService.getBoards();
			//FIXME - only do this once or twice
			handler.postDelayed(new Runnable(){
				public void run(){
					createPinView(source_url, img_url, img_loc, recursiveCount +1);
			    }
			}, 1000);
			return;
		}
		if (0 == mBoundService.getBoardKeys().size()){
			mBoundService.getBoards();
			//FIXME - only do this once or twice
			handler.postDelayed(new Runnable(){
				public void run(){
					createPinView(source_url, img_url, img_loc, recursiveCount +1);
			    }
			}, 1000);
			return;
		}
		for (String board : mBoundService.getBoardKeys()){
			boards.add(board);
		}
		ft.addToBackStack(Constants.PIN_STACK)
		.add(android.R.id.content, CreatePin.newInstance(0,img_url,source_url,img_loc,boards), 
				Constants.PIN_STACK)
		.commitAllowingStateLoss();
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (Constants.DEBUG){ Log.d(TAG, "item was clicked: " + item.getTitle() + "\ncurView = " + curView);}
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			toastText("Refreshing current view");
			mBoundService.refresh();
			break;
		case R.id.menu_search:
			handler.post(new Runnable(){
				@Override
				public void run() {
					buildSearchDialog();
				}
			});
			break;
		case R.id.menu_categories:
			break;
		case R.id.menu_login:
			loginView();
			break;
		case R.id.menu_logout:
			mBoundService.logout();
			break;
		case R.id.menu_sharing:
			handler.post(new Runnable(){
				@Override
				public void run() {
					buildShareDialog();
				}
			});
			break;
		case android.R.id.home:
			if (Constants.DETAILS == curView || Constants.LOGIN == curView){
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
				getSupportFragmentManager().popBackStack();
			}
			else if (mBoundService.isLoggedIn()){
				if (Constants.FOLLOWING != curView){
					gListener = null;
					try{ System.gc(); } catch (Exception e){}
					updateCurPrevView(Constants.FOLLOWING,Constants.FOLLOWING,curCategory);
		    		updateViewWithGc();
				}
			}
			else if (Constants.POPULAR != curView ){
				gListener = null;
				try{ System.gc(); } catch (Exception e){}
				updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
	    		updateViewWithGc();
			}
			break;
		case R.id.menu_category_popular:
			gListener = null;
			try{ System.gc(); } catch (Exception e){}
			updateCurPrevView(Constants.POPULAR,curView,curCategory);

			if (Constants.DEBUG) { Log.d(TAG, "item: " + item.getTitle() + "\nint: " + curView);}
			handler.post(new Runnable(){
    			public void run(){
    				showGreeting();
    				GridContent gc = getGridContent();
					if (gc.size() >= 1){
						updateViewWithGc();
					}
				}
    		});
			break;
		case R.id.menu_category_following:
			gListener = null;
			try{ System.gc(); } catch (Exception e){}
			updateCurPrevView(Constants.FOLLOWING,curView,curCategory);

			if (!mBoundService.isLoggedIn()){
				toastText("Please log in first");
				loginView();
				break;
			}
			if (Constants.DEBUG) { Log.d(TAG, "item: " + item.getTitle() + "\nint: " + curView);}
			handler.post(new Runnable(){
    			public void run(){
    				showGreeting();
    				GridContent gc = getGridContent();
					if (gc.size() >= 1){
						updateViewWithGc();
					}
				}
    		});
			break;
		case R.id.menu_category_all:
		case R.id.menu_category_architecture:
		case R.id.menu_category_art:
		case R.id.menu_category_cars_motorcycles:
		case R.id.menu_category_design:
		case R.id.menu_category_diy_crafts:
		case R.id.menu_category_education:
		case R.id.menu_category_film_music_books:
		case R.id.menu_category_fitness:
		case R.id.menu_category_food_drink:
		case R.id.menu_category_gardening:
		case R.id.menu_category_geek:
		case R.id.menu_category_hair_beauty:
		case R.id.menu_category_history:
		case R.id.menu_category_holidays:
		case R.id.menu_category_home:
		case R.id.menu_category_humor:
		case R.id.menu_category_kids:
		case R.id.menu_category_mylife:
		case R.id.menu_category_women_apparel:
		case R.id.menu_category_men_apparel:
		case R.id.menu_category_outdoors:
		case R.id.menu_category_people:
		case R.id.menu_category_pets:
		case R.id.menu_category_photography:
		case R.id.menu_category_sports:
		case R.id.menu_category_technology:
		case R.id.menu_category_travel_places:
		case R.id.menu_category_wedding_events:
		case R.id.menu_category_other:
			if (Constants.CATEGORY != curView  || PindCategory.get(item.getTitle()) != curCategory){
				if (Constants.DEBUG) { Log.d(TAG, "nulling gListener");}
				gListener = null;
				try{ System.gc(); } catch (Exception e){}
			}
			else {
				if (Constants.DEBUG) { Log.d(TAG, "item clicked Category: 10234, curView: " + curView + "\ncurCategory: " + curCategory + ", catMap: " + PindCategory.get(item.getTitle()));}
			}

			updateCurPrevView(Constants.CATEGORY,curView,PindCategory.get(item.getTitle()));

			if (Constants.DEBUG) { Log.d(TAG, "item: " + item.getTitle() + "\nint: " + curCategory);}
			handler.postDelayed(new Runnable(){
    			public void run(){
    				showGreeting();
					GridContent gc = getGridContent();
					if (gc.size() >= 1){
						updateViewWithGc();
					}
				}
    		},500);
			break;
		case R.id.menu_camera:
			if (Constants.PV){
				handler.post(new Runnable(){
	    			public void run(){
	    				prepCamera();
					}
	    		});
			}
			break;
		case R.id.menu_gallery:
			if (Constants.PV){
				handler.post(new Runnable(){
	    			public void run(){
	    				Intent i = new Intent(Intent.ACTION_PICK,
	    			               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	    				i.setType("image/*");
	    				startActivityForResult(i, ACTIVITY_SELECT_IMAGE); 
	    			}
	    		});
			}
			break;
		}
		return super.onOptionsItemSelected(item);
    }
    
	
	private void prepCamera(){
		String storageState = Environment.getExternalStorageState();
        if(storageState.equals(Environment.MEDIA_MOUNTED)) {
        	// http://stackoverflow.com/a/5054673/974800
            String path = Calculator.getDefaultFilePath()  
            		+ "files/" + System.currentTimeMillis() + ".jpg";
            _photoFile = new File(path);
            try {
                if(_photoFile.exists() == false) {
                    _photoFile.getParentFile().mkdirs();
                    _photoFile.createNewFile();
                }

            } catch (IOException e) {
                Log.e(TAG, "Could not create file.", e);
            }
            Log.i(TAG, path);

            _fileUri = Uri.fromFile(_photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
            intent.putExtra( MediaStore.EXTRA_OUTPUT, _fileUri);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }   else {
            new AlertDialog.Builder(PindActivity.this)
            .setMessage("External Storeage (SD Card) is required.\n\nCurrent state: " + storageState)
            .setCancelable(true).create().show();
        }
	}
	
	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	private void copyCameraTo(File dst) throws IOException {
	    InputStream in = getContentResolver().openInputStream(_fileUri);
	    OutputStream out = new FileOutputStream(dst);
		if (Constants.DEBUG){Log.d(TAG,"copyCameraTo - " + dst.toString());}

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null != data){
			if (Constants.DEBUG){Log.d(TAG,"onActivityResult: " + requestCode + "," + resultCode + "," + data.getDataString());}
		}
		if (!Constants.PV){
			return;
		}
		if (CAMERA_PIC_REQUEST == requestCode) {
			updateCurPrevView(Constants.CREATE_PIN,curView,curCategory);
			if (Constants.DEBUG){Log.d(TAG,"onActivityResult - back from camera, image: " + _fileUri.toString());}

			File f = new File(Calculator.getDefaultFilePath() + System.currentTimeMillis() + ".jpg");
			try {
				f.getParentFile().mkdirs();
				f.createNewFile();
				copyCameraTo(f);
			} catch (IOException e) {
				if (Constants.DEBUG){ e.printStackTrace(); }
				return;
			}
			final String fPath = Uri.fromFile(f).toString();
			handler.post(new Runnable(){
    			public void run(){
    				if (Constants.DEBUG){Log.d(TAG,"onActivityResult - createPinView: " + fPath);}
    				createPinView(null,fPath,fPath,0);
    			}
    		});
		}
		else if (ACTIVITY_SELECT_IMAGE == requestCode){
			if (null == data){
				webErrorDialog("Null data","Ok","Exit","data is null from gallery, can't handle it.");
				return;
			}
			updateCurPrevView(Constants.CREATE_PIN,curView,curCategory);
			if (Constants.DEBUG){Log.d(TAG,"onActivityResult - back from gallery");}
			Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String filePath = "file://" + cursor.getString(columnIndex);
            cursor.close();
        	
            handler.post(new Runnable(){
    			public void run(){
    				if (Constants.DEBUG){Log.d(TAG,"onActivityResult - createView with gallery image");}
    				createPinView(null,filePath,filePath,0);
    			}
    		});
		}
    }
	
    @Override
    public void onStart(){
    	super.onStart();
    	doBindService();
    	if (this.getIntent() != null){
    		onNewIntent(this.getIntent());
    	}
    	
    }

	public void webErrorDialog(String title, String positive, String negative, String message){
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		if (negative.equals("") || negative == null){
			builder.setCancelable(false);
		}
		else {
			builder.setCancelable(true);
			builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
			    }
			});	
		}
			    
		builder.setMessage(message);
		builder.create().show();
	}

	
    
    
    public void buildSearchDialog(){
	    final Dialog dialog = new Dialog(this);

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	dialog.setContentView(R.layout.search);
	    }
	    else {
	    	WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		    lp.copyFrom(dialog.getWindow().getAttributes());
		    lp.horizontalMargin = 10;
		    lp.width = WindowManager.LayoutParams.FILL_PARENT;
		    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	
		    dialog.setContentView(R.layout.search);
		    dialog.getWindow().setAttributes(lp);
	    }
	    
	    final EditText et =  (EditText)dialog.findViewById(R.id.search_content);
	    dialog.setTitle(R.string.search);

	    dialog.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
				gListener = null;
				try{ System.gc(); } catch (Exception e){}
	    		updateCurPrevView(Constants.SEARCH,curView,curCategory);
	    		mBoundService.requestSearch(et.getText().toString());
	    		handler.post(new Runnable(){
	    			public void run(){
	    				showGreeting();
	    			}
	    		});
	    		dialog.cancel();
	        }
		});
	    dialog.show();
	}
	
    public void buildShareDialog(){
	    final Dialog dialog = new Dialog(this);

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	dialog.setContentView(R.layout.share);
	    }
	    else {
	    	WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		    lp.copyFrom(dialog.getWindow().getAttributes());
		    lp.horizontalMargin = 10;
		    lp.width = WindowManager.LayoutParams.FILL_PARENT;
		    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	
		    dialog.setContentView(R.layout.share);
		    dialog.getWindow().setAttributes(lp);
	    }
	    dialog.setTitle("Share settings");
	    
	    final CheckBox fby = (CheckBox)dialog.findViewById(R.id.fb_toggle);
	    final CheckBox twy = (CheckBox)dialog.findViewById(R.id.tw_toggle);
	    
	    fby.setChecked(mBoundService.isFbShare());
    	twy.setChecked(mBoundService.isTwShare());
	    
	    dialog.findViewById(R.id.ok_share_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
    			mBoundService.setFbShare(fby.isChecked());
		    	mBoundService.setTwShare(twy.isChecked());
	    		dialog.cancel();
	        }
		});
	    dialog.show();
	}
    
	private void onOpen(){
		if (Constants.DEBUG){ Log.d(TAG,"onOpen"); }
		if (null != mBoundService){
//			if (Constants.POPULAR == curView || Constants.FOLLOWING == curView){
//				updateViewWithGc();
//			}
			if (mBoundService.isLoggedIn()){
				login();
			}
			else {
				updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				};
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_HOME == keyCode){
    		mBoundService.cleanSD();
    		hideGreeting();
    		finish();
		}
		if ((keyCode != KeyEvent.KEYCODE_BACK)) {
	    	return super.onKeyDown(keyCode, event);
	    }
		else if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			hideGreeting();
	    	switch (curView){
	    	case Constants.CATEGORY:
	    	case Constants.SEARCH:
	    		gListener = null;
	    		try{ System.gc(); } catch (Exception e){}
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		updateViewWithGc();
	    		break;
	    	case Constants.LOGIN:
	    		updateCurPrevView(Constants.POPULAR,curView,curCategory);
	    		if (null != getSupportFragmentManager().findFragmentByTag(Constants.LOGIN_STACK)){
	    			getSupportFragmentManager().popBackStack(Constants.LOGIN_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
	    		}
	    		break;
	    	case Constants.CREATE_PIN:
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		if (null != getSupportFragmentManager().findFragmentByTag(Constants.PIN_STACK)){
	    			getSupportFragmentManager().popBackStack(Constants.PIN_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
	    		}
	    		break;
	    	case Constants.IMG_SCRAPE:
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		if (null != getSupportFragmentManager().findFragmentByTag(Constants.IMG_SCRAPE_STACK)){
	    			getSupportFragmentManager().popBackStack(Constants.PIN_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
	    		}
	    		break;
	    	case Constants.DETAILS:
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		if (null != getSupportFragmentManager().findFragmentByTag(Constants.DETAILS_STACK)){
	    			getSupportFragmentManager().popBackStack(Constants.DETAILS_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
	    		}
	    		break;
	    	default:
	    		if (null != mBoundService){mBoundService.cleanSD();}
	    		hideGreeting();
	    		finish();
	    		break;
	    	}
	    }
	    
	    return true;
	}
	
	Handler handler = new Handler();
	
	@Override
	public void onLoginSuccess() {
		if (Constants.DEBUG){ Log.d(TAG,"onLoginSuccess called");}
		handler.post(new Runnable(){
			public void run(){
				if (Constants.LOGIN == curView){
					getSupportFragmentManager().popBackStack();
				}
				toastText("logged in");
				login();
			}
		});
	}
	
	public void login(){
		refreshMenu();
		mBoundService.getBoards();
		if (Constants.IMG_SCRAPE != curView && Constants.CREATE_PIN != curView){
    		updateCurPrevView(Constants.FOLLOWING,curView,curCategory);
    		showGreeting();
			GridContent gc = getGridContent();
			if (gc.size() >= 1){
				updateViewWithGc();
			}
		}
	}

	private void toastText(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onItemSelected(SmallPinItem item) {
		if (null == item){
			this.notifyFailed("Get Pin Failed","Something went wrong retrieving the pin.");
			return;
		}
		updateCurPrevView(Constants.DETAILS,curView,curCategory);

		pDialog.show();
		PinItem pi = null;
		pi = mBoundService.getPinItem(item.getId());
		if (null == pi){
			this.notifyFailed("Get Pin Failed","Something went wrong retrieving the pin.");
			updateCurPrevView(prevView,prevView,curCategory);
			return;
		}
		new DownloadMobileImageTask().execute(pi);
		
	}
	
	private void displayDetails(PinItem pi){
		if (null == pi){
			if (Constants.DEBUG){Log.d(TAG,"displayDetails failed");}
			if (null != pDialog){pDialog.dismiss();}
			webErrorDialog("Details failed", "Ok", "Exit", "Failed to download the details image.");
			return;
		}
		if (null == pi.getImages().getMob_loc()){
			if (Constants.DEBUG){Log.d(TAG,"displayDetails failed");}
			if (null != pDialog){pDialog.dismiss();}
			webErrorDialog("Details failed", "Ok", "Exit", "Failed to download the details image.");
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG, "displayDetails - " + pi.getPinItem().toString());}
		if (!new File(pi.getImages().getMob_loc()).exists()){
			if (Constants.DEBUG){Log.d(TAG,"displayDetails failed");}
			if (null != pDialog){pDialog.dismiss();}
			webErrorDialog("Details failed", "Ok", "Exit", "Failed to download the details image.");
			return;
		}
		try {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
			if (null != pDialog){pDialog.dismiss();}
			ft.addToBackStack(Constants.DETAILS_STACK)
			.add(android.R.id.content, DetailsFragment.newInstance(0,pi),Constants.DETAILS_STACK)
			.commitAllowingStateLoss();
		} catch(IllegalArgumentException ex){
			if (Constants.DEBUG){Log.d(TAG,"Couldn't get details! IllegalArgumentException");}
			if (null != pDialog){pDialog.dismiss();}
			webErrorDialog("Details failed", "Ok", "Exit", "Failed to download the details image.");
			return;
		}
	}
		
	@Override
	public void onPopularUpdate() {
		if (Constants.POPULAR != curView){
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG,"onPopularUpdate"); }
		handler.post(new Runnable(){
			public void run(){
				updateViewWithGc();
			}
		});
	}
	
	@Override
	public void onEverythingUpdate() {
		if (Constants.EVERYTHING != curView){
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG,"onEverythingUpdate"); }
		handler.post(new Runnable(){
			public void run(){
				updateViewWithGc();
			}
		});
	}

	@Override 
	public void onSearchUpdate(){
		if (Constants.SEARCH != curView){
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG,"onEverythingUpdate"); }
		handler.post(new Runnable(){
			public void run(){
				GridContent gc = mBoundService.getGridContent(Constants.SEARCH);
				if (gc.size() < 1){
					toastText("no reseults");
//		    		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
				}
				updateViewWithGc();
			}
		});
	}

	@Override 
	public void onCategoryUpdate(){
		if (Constants.CATEGORY != curView){
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG,"onCategoryUpdate"); }
		handler.post(new Runnable(){
			public void run(){
				updateViewWithGc();
			}
		});
	}

	@Override 
	public void onUpdateCurView(){
		if (Constants.DETAILS == curView || Constants.LOGIN == curView 
				|| Constants.CREATE_PIN == curView || Constants.IMG_SCRAPE == curView){
			return;
		}
		if (Constants.DEBUG){ Log.d(TAG,"onUpdateCurView"); }
		handler.post(new Runnable(){
			public void run(){
				updateViewWithGc();
			}
		});
	}
	
	@Override
	public GridContent getGridContent(){
		if (null == mBoundService){
			return null;
		}
		switch (curView){
		case Constants.CATEGORY:
			return mBoundService.getGridContent(curCategory);
		default:
			return mBoundService.getGridContent(curView);	
		}
	}

	
	protected void updateViewWithGc() {
		if (Constants.DEBUG){ Log.d(TAG,"updateViewWithGc: " + curView); }
		if (getSupportFragmentManager().getBackStackEntryCount() > 1){
			getSupportFragmentManager().popBackStack();
		}
		GridContent gc = getGridContent();
		if (Constants.DEBUG){ Log.d(TAG,"updateViewWithGc: gc.size() = " + gc.size()); }
		if (gc.size() < 1){
			if (Constants.DEBUG){ Log.d(TAG,"updateViewWithGc: showGreeting"); }
			showGreeting();
		}
		else {
			hideGreeting();
		}
		if (gListener != null ){
			gListener.onGridUpdate(gc);
		}
		else {
			gListener = (GridFragment.newInstance(0, gc));
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down);
			ft.replace(android.R.id.content, (Fragment) gListener).commitAllowingStateLoss();
			try{ System.gc(); } catch (Exception e){}
		}
	}

	private void updateCurPrevView(int cur, int prev, int category){
		curView = cur;
		prevView = prev;
		curCategory = category;
		if (null != mBoundService){
			switch (curView){
			case Constants.CATEGORY:
				mBoundService.updateCategory(curCategory);
				break;
			default:
				mBoundService.updateCategory(curView);	
				break;
			}
		}
	}

	@Override
	public void onCategoryFailure() {
		handler.post(new Runnable(){
			public void run(){
	    		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);

				updateViewWithGc();
				webErrorDialog("Error grabbing category", "Ok", "Exit", "There was an error downloading the selected category. Try again later.");
			}
		});
	}

	@Override
	public void onPopularFailure() {
		handler.post(new Runnable(){
			public void run(){
	    		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
				updateViewWithGc();
				webErrorDialog("Error grabbing category", "Ok", "Exit", "There was an error downloading the selected category. Try again later.");
			}
		});
	}

	@Override
	public void onEverythingFailure() {
		handler.post(new Runnable(){
			public void run(){
	    		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
				updateViewWithGc();
				webErrorDialog("Error grabbing category", "Ok", "Exit", "There was an error downloading the selected category. Try again later.");
			}
		});
	}

	@Override
	public void onSearchFailure() {
		handler.post(new Runnable(){
			public void run(){
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
				updateViewWithGc();
				webErrorDialog("Error grabbing category", "Ok", "Exit", "There was an error with the search, or there were no results. Try again later.");
			}
		});
	}
	
	@Override
	public void onRequestFailure(int curCategory) {
		handler.post(new Runnable(){
			public void run(){
	    		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,Constants.ARCHITECTURE);
				updateViewWithGc();
				webErrorDialog("Error downloading data", "Ok", "Exit", "There was an error downloading the selected category. Try again later.");
			}
		});
	}
	@Override
	public void requestRefresh() {
		if (null == mBoundService){
			return;
		}
		toastText("Auto-refreshing current view");
		mBoundService.refresh();
	}
	
	@Override
	public void onContentUpdate(final GridContent gc) {
		if (Constants.CATEGORY != curView){
			if (Constants.DEBUG){ Log.d(TAG,"onEverythingUpdate"); }
			handler.post(new Runnable(){
				public void run(){
					updateViewWithGc();
				}
			});
		}
		if (Constants.DEBUG){ Log.d(TAG,"onCategoryUpdate"); }
		handler.post(new Runnable(){
			public void run(){
				updateViewWithGc();
			}
		});
	}

	public void showGreeting() {
		if (null == getSupportFragmentManager()){
			return;
		}
		if (null != getSupportFragmentManager().findFragmentByTag(Constants.GREETING_STACK)){
			if (Constants.DEBUG){Log.d(TAG,"showGreeting already exists, returning");}
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(Constants.GREETING_STACK)
		.add(android.R.id.content, GreetingFragment.newInstance(0), Constants.GREETING_STACK).commitAllowingStateLoss();
		if (Constants.DEBUG){Log.d(TAG,"showGreeting");}
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				if (null != getSupportFragmentManager().findFragmentByTag(Constants.GREETING_STACK)){
					if (Constants.DEBUG){Log.d(TAG,"showGreeting timeout");}
					webErrorDialog("Request Timeout", "Ok", "Exit", "Your request was taking too long, you can try again if you'd like.");
				}
			}
		}, 50000);
	}	
	
	@Override
	public void hideGreeting() {
		if (Constants.DETAILS == curView){
			return;
		}
		if (null != getSupportFragmentManager().findFragmentByTag(Constants.GREETING_STACK)){
			if (Constants.DEBUG){Log.d(TAG,"hideGreeting");}
			try {
				getSupportFragmentManager().popBackStack(Constants.GREETING_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
			} catch (IllegalStateException e){
				if (Constants.DEBUG){Log.d(TAG, "hidegreeting failed due to IllegalStateException");}
			}
		}
		else {
			if (Constants.DEBUG){Log.d(TAG,"hideGreeting nothing to hide");}
		}
	}
	
	public void hideGreetingForDetails() {
		if (null != getSupportFragmentManager().findFragmentByTag(Constants.GREETING_STACK)){
			if (Constants.DEBUG){Log.d(TAG,"hideGreetingForDetails");}
			getSupportFragmentManager().popBackStack(Constants.GREETING_STACK, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		else {
			if (Constants.DEBUG){Log.d(TAG,"hideGreetingForDetails nothing to hide");}
		}
	}

	@Override
	public void onUnAuthenticated() {
		updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
		showGreeting();
		GridContent gc = getGridContent();
		if (gc.size() >= 1){
			updateViewWithGc();
		}
		webErrorDialog("Not logged in", "Ok", "Exit", "Your request could not be completed because you are not logged into the service. Please log in and try again.");
	}

	@Override
	public void onLoginFailure() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				refreshMenu();
				webErrorDialog("Login Failed", "Ok", "Exit", "You were not logged into the service. Please try again.");
			}
		});
	}

	@Override
	public void onLogoutSuccess() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("Logged in");
				refreshMenu();
			}
		});
	}

	@Override
	public void onLoginCancel() {
		handler.post(new Runnable(){
			@Override
			public void run() {
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);

				getSupportFragmentManager().popBackStack();
				showGreeting();
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				}
			}
		});
	}
	@Override
	public void onLoginFinish() {
		handler.post(new Runnable(){
			@Override
			public void run() {
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
				getSupportFragmentManager().popBackStack();
			}
		});
	}

	@Override
	public void onLikeFailure() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("failed to like");
			}
		});
	}

	@Override
	public void onLikeSuccess() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("liked pin");
			}
		});
	}

	@Override
	public void onCommentFailure() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("failed to comment");
			}
		});
	}

	@Override
	public void onCommentSuccess() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("commented on pin");
			}
		});
	}

	@Override
	public void onRepinFailure() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("failed to repin");
			}
		});
	}

	@Override
	public void onRepinSuccess() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("repinned item");
			}
		});
	}

	@Override
	public void onBoardsFailure() {
	}

	@Override
	public void onBoardsSuccess() {
	}

	@Override
	public void onLikeSelected(final String item) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (mBoundService.isLoggedIn()){
					likeVerify(item);
				}
			}
		});
	}

	@Override
	public void onRepinSelected(final String item, final String curComment) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (mBoundService.isLoggedIn()){
					if (mBoundService.isLoggedIn()){
						buildRePinDialog(item,curComment);
					}
				}
			}
		});
	}

	@Override
	public void onCommentSelected(final String item) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (mBoundService.isLoggedIn()){
					if (mBoundService.isLoggedIn()){
						buildEnterTextDialog(item);
					}
				}
			}
		});

	}
	

	public void likeVerify(final String pId){
		final String positive = "Like";
		final String negative = "Cancel";
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Like this item?");
		builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mBoundService.likePin(pId, true);
			}
		});
		builder.setCancelable(true);
		builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		    }
		});	
			    
		builder.create().show();
	}

	public void buildEnterTextDialog(final String spi){
	    final Dialog dialog = new Dialog(this);

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	dialog.setContentView(R.layout.enter_text);
	    }
	    else {
	    	WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		    lp.copyFrom(dialog.getWindow().getAttributes());
		    lp.horizontalMargin = 10;
		    lp.width = WindowManager.LayoutParams.FILL_PARENT;
		    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	
		    dialog.setContentView(R.layout.enter_text);
		    dialog.getWindow().setAttributes(lp);
	    }
	    
	    String titleString = "Comment on pin";
	    if (Constants.DEBUG){ Log.d(TAG, titleString);}
	    dialog.setTitle(titleString);
	    final EditText et =  (EditText)dialog.findViewById(R.id.enter_text_content);

	    dialog.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
	    		mBoundService.commentPin(spi, et.getText().toString());
	    		dialog.cancel();
	        }
		});
	    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
	    		dialog.cancel();
	        }
		});
	    dialog.show();
	}

	public void buildRePinDialog(final String pid, final String cur_comment){
	    final Dialog dialog = new Dialog(this);

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	dialog.setContentView(R.layout.repin);
	    }
	    else {
	    	WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		    lp.copyFrom(dialog.getWindow().getAttributes());
		    lp.horizontalMargin = 10;
		    lp.width = WindowManager.LayoutParams.FILL_PARENT;
		    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	
		    dialog.setContentView(R.layout.repin);
		    dialog.getWindow().setAttributes(lp);
	    }
	    
	    String titleString = "Repin item";
	    if (Constants.DEBUG){ Log.d(TAG, titleString);}
	    dialog.setTitle(titleString);
	    final EditText et =  (EditText)dialog.findViewById(R.id.repin_comment);
	    et.setText(cur_comment);

	    final Spinner boards = (Spinner)dialog.findViewById(R.id.boards_spinner);
	    ArrayList<String> list = new ArrayList<String>();
	    for (String board : mBoundService.getBoardKeys()){
	    	list.add(board);
	    }
		final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(dialog.getContext(),
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		boards.setAdapter(dataAdapter);
		
	    dialog.findViewById(R.id.repin_ok_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
	    		mBoundService.rePin(pid, et.getText().toString(), mBoundService.getBoardId((String)boards.getSelectedItem()));
	    		dialog.cancel();
	        }
		});
	    dialog.findViewById(R.id.repin_cancel_button).setOnClickListener(new View.OnClickListener() {
	    	@Override
	        public void onClick(View view) {
	        	dialog.cancel();
	        }
		});
	    dialog.show();
	}

	@Override
	public void onDetailsFailed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
	    		updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
				webErrorDialog("Pin Details Failed", "Ok", "Exit", "Something went wrong opening that pin. Please try again.");
			}
		});
	}

	@Override
	public void onPinFailure() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				pDialog.dismiss();
				webErrorDialog("Pin Failed", "Ok", "Exit", "Something went wrong trying to pin the item. Please try again.");
			}
		});
	}

	@Override
	public void onPinSuccess() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				pDialog.dismiss();
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		toastText("pin sent");
				getSupportFragmentManager().popBackStack();
				showGreeting();
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				}
			}
		});
	}

	@Override
	public void onImageSelected(final String src_url,final String img_url, final String image_loc) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
				createPinView(src_url,img_url,"file:///" + image_loc,0);
			}
		});
	}

	@Override
	public void onCancelImageScraper() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
	    		showGreeting();
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				}
			}
		});
	}

	@Override
	public void onImageScraperFailed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
				webErrorDialog("Image Scraper Failed", "Ok", "Exit", "Something went wrong scraping this website. Please try again.");
			}
		});
	}

	@Override
	public void onImageScraperNoPin() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				updateCurPrevView(Constants.POPULAR,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
				webErrorDialog("Image Scraper Failed", "Ok", "Exit", "The site you are trying to pin from has a nopin flag. We are not allowed to pin from there.");
			}
		});
	}
	
	@Override
	public void onCreatePinFailed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				pDialog.dismiss();
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		webErrorDialog("Pin Failed", "Ok", "Exit", "Something went wrong creating that pin. Please try again.");
			}
		});
	}


	@Override
	public void onPostNewPin(String img_url, String src_url, byte[] b,
			String comment, String board_id) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				pDialog.show();
			}
		});
		mBoundService.pinItem(img_url, src_url, b, comment, board_id);
	}

	@Override
	public void onCancelNewPin() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				pDialog.dismiss();
				updateCurPrevView(prevView,Constants.POPULAR,curCategory);
	    		getSupportFragmentManager().popBackStack();
	    		showGreeting();
				GridContent gc = getGridContent();
				if (gc.size() >= 1){
					updateViewWithGc();
				}
			}
		});
	}

	@Override
	public void onBrowserFailed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				toastText("could not open browser");
			}
		});
	}

	private class DownloadMobileImageTask extends AsyncTask<PinItem, Integer, PinItem> {
	    protected PinItem doInBackground(PinItem... params) {
	    	PinItem pi = params[0];
	   	 try {
	           URL url = new URL(pi.getImage());
	           File file = new File(pi.getImages().getMob_loc());
	           if(file.exists() == false) {
              	 file.getParentFile().mkdirs();
              	 file.createNewFile();
               }
	           long startTime = System.currentTimeMillis();
	           if (Constants.DEBUG){
	                Log.d("ImageManager", "download begining");
	                Log.d("ImageManager", "download url:" + url);
	                Log.d("ImageManager", "downloaded file name:" + file.getAbsolutePath());
	           }
	           /* Open a connection to that URL. */
	           URLConnection ucon = url.openConnection();

	           /*
	            * Define InputStreams to read from the URLConnection.
	            */
	           InputStream is = ucon.getInputStream();
	           BufferedInputStream bis = new BufferedInputStream(is);

	           /*
	            * Read bytes to the Buffer until there is nothing more to read(-1).
	            */
	           ByteArrayBuffer baf = new ByteArrayBuffer(50);
	           int current = 0;
	           while ((current = bis.read()) != -1) {
	                   baf.append((byte) current);
	           }

	           /* Convert the Bytes read to a String. */
	           FileOutputStream fos = new FileOutputStream(file);
	           fos.write(baf.toByteArray());
	           fos.close();
	           if (Constants.DEBUG){
	           Log.d("ImageManager", "download ready in"
	                           + ((System.currentTimeMillis() - startTime) / 1000)
	                           + " sec");
	           }
	        } catch (IOException e) {
	             if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	        } catch (NullPointerException e){
	             if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	        }
	   	 return pi;
	    }

	    protected void onPostExecute(PinItem result) {
	        displayDetails(result);
	    }

	}
}
