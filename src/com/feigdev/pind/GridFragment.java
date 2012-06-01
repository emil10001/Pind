package com.feigdev.pind;

import com.feigdev.pind.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ConcurrentModificationException;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.util.ByteArrayBuffer;

import com.feigdev.pind.GridContent.SmallPinItem;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class GridFragment extends Fragment implements GridListener {
	private static final String TAG = "Grid";
	private Point imgSize;
	int mNum;
	private OnItemSelectedListener mListener;
	private View curView;
	private GridView gView;
	private static GridContent gridContent;
	private int refreshCount = 0;
	private int spiCount = 0;
	
	// Container Activity must implement this interface
    public interface OnItemSelectedListener {
        public void onItemSelected(SmallPinItem item);
        public GridContent getGridContent();
        public void requestRefresh();
		public void hideGreeting();
    }
    
	public void setImgSize(int x, int y){
		imgSize.set(x, y);
	}
	
	@Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
    
	
    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }
    
	/**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static GridFragment newInstance(int index, GridContent gc) {
    	GridFragment f = new GridFragment();
    	gridContent = gc ;
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null){
        	return null;
        }
        
        curView = inflater.inflate(R.layout.grid, container, false); 
        gView = (GridView) curView.findViewById(R.id.myGrid);
        if (null == gridContent){
        	gridContent = new GridContent();
        }

        gView.setAdapter(new ImageAdapter(getActivity(), gridContent, this));
        
        if (gridContent.size() > 1){
        	mListener.hideGreeting();
        }
		
        ((LinearLayout)curView.findViewById(R.id.grid_ad_space)).setEnabled(false);
        ((LinearLayout)curView.findViewById(R.id.grid_ad_space)).setVisibility(View.INVISIBLE);
        
        imgSize = new Point();
        int side;
        try{
        	side = getActivity().getResources().getDisplayMetrics().widthPixels / 3 ;
        } catch (Exception ex){
        	if (Constants.DEBUG){System.err.print(ex);}
        	side = 100;
        }
       	setImgSize(side,side);
        return curView;
    }
    
    public class ImageAdapter extends BaseAdapter  {
    	GridContent gridContent;
    	GridListener gl;
    	int lastGrabbed = 0;
    	
        public ImageAdapter(Context c, GridContent gc, GridListener gl) {
        	if (Constants.DEBUG){Log.w(TAG, "ImageAdapter called");}
            mContext = c;
            gridContent = gc;
            this.gl = gl;
        }

        public GridContent getGC(){
        	return gridContent;
        }
        
        public int getCount() {
        	if (Constants.DEBUG){Log.w(TAG, "ImageAdapter.getCount called: " + gridContent.size());}
        	return gridContent.size();
        }

        public Object getItem(int position) {
        	if (Constants.DEBUG){Log.w(TAG, "ImageAdapter.getItem called: " + position);}
            return position;
        }

        public long getItemId(int position) {
        	if (Constants.DEBUG){Log.w(TAG, "ImageAdapter.getItemId called: " + position);}
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
        	if (null == gridContent.get(position)){
        		final ProgressBar pb;
        		pb = new ProgressBar(mContext);
	        	pb.setLayoutParams(new GridView.LayoutParams(imgSize.x,imgSize.y));
	        	pb.setPadding(10,10,10,10);
	        	return pb;
        	}
        	else if (new File(gridContent.get(position).getThumb_loc()).exists()){
	        	if (Constants.DEBUG){Log.w(TAG, "ImageAdapter.getView called");}
	            final ImageView imageView;
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(imgSize.x,imgSize.y));
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10,10,10,10);
            
	            imageView.setImageURI(Uri.parse(gridContent.get(position).getThumb_loc()));
	            if (Constants.DEBUG){Log.w(TAG, "ImageAdapter.getView.setBitmap called for: " + gridContent.get(position).getThumb_loc());}
	        
	        
	            imageView.setOnClickListener(new View.OnClickListener() {
	        	
	            @Override
	            public void onClick(View view) {
	            	if (Constants.DEBUG){ Log.d(TAG, "an item was clicked  - " + gridContent.get(position).getId());}
	    				mListener.onItemSelected(gridContent.get(position));
	            	}
	            });
	            return imageView;
	        }
	        else {
	        	try{
		        	final ProgressBar pb;
		        	new DownloadFilesTask().execute(gridContent.get(position));
	        		pb = new ProgressBar(mContext);
		        	pb.setLayoutParams(new GridView.LayoutParams(imgSize.x,imgSize.y));
		        	pb.setPadding(10,10,10,10);
		        	return pb;
	        	} catch(RejectedExecutionException e){
	        		return null;
	        	}
	        }
        }

        private Context mContext;

    }
    
    

	@Override
	public void onGridUpdate(GridContent gc) {
		gridContent = gc;
		
		if (null == gView){
			if (Constants.DEBUG){Log.w(TAG, "gView is null");}
		}
		if (null == gc){
			if (Constants.DEBUG){Log.w(TAG, "gc is null");}
		}
		if (getActivity() == null){
			if (Constants.DEBUG){Log.w(TAG, "getActivity is null");}
		}
		if (null == gView || null == ((ImageAdapter)gView.getAdapter())){
			if (Constants.DEBUG){Log.d(TAG,"GridFragment.addItem - gView is null or something?");}
			return;
		}
		if (null == ((ImageAdapter)gView.getAdapter()).getGC()){
			if (Constants.DEBUG){Log.d(TAG,"GridFragment.addItem - gc was null creating new one");}
			gView.setAdapter(new ImageAdapter(getActivity(), new GridContent(), this));
		}
		if (((ImageAdapter)gView.getAdapter()).getGC().size() > 1){
        	mListener.hideGreeting();
        }
		try{
			for (SmallPinItem pin : gc.getSpiList()){
				if (((ImageAdapter)gView.getAdapter()).getGC().size() > 180){
					return;
				}
				try {
					if (!new File(pin.getThumb_loc()).exists()){
						gc.remove(pin);
						new DownloadFilesTask().execute(pin);
					}
					else if (!((ImageAdapter)gView.getAdapter()).getGC().contains(pin)){
						((ImageAdapter)gView.getAdapter()).getGC().add(pin);
					}
				} catch (ConcurrentModificationException e){
					if (Constants.DEBUG){Log.d(TAG, "Couldn't check spi because of ConcurrentModificationException");}
					return;	
				}
			}
		} catch (ConcurrentModificationException e){
			if (Constants.DEBUG){System.err.print(e);}
		}
		((ImageAdapter) gView.getAdapter()).notifyDataSetChanged();
	}

	private void updateGC(SmallPinItem spi){
		if (null == spi){
			return;
		}
		if (null == spi.getThumb_loc()){
			return;
		}
		if (!new File(spi.getThumb_loc()).exists()){
			return;
		}
		try {
			if (!((ImageAdapter)gView.getAdapter()).getGC().contains(spi)){
				((ImageAdapter)gView.getAdapter()).getGC().add(spi);
			}
		} catch (ConcurrentModificationException e){
			if (Constants.DEBUG){Log.d(TAG, "Couldn't check spi because of ConcurrentModificationException");}
			return;	
		}
		spiCount++;
		if (0 == spiCount % 4){
			if (((ImageAdapter)gView.getAdapter()).getGC().size() > 1){
	        	mListener.hideGreeting();
	        }
			((ImageAdapter) gView.getAdapter()).notifyDataSetChanged();
		}
	}
	
	@Override
	public void requestRefresh() {
		if (refreshCount > 7){
			return;
		}
		refreshCount++;
		mListener.requestRefresh();
	}


	 private class DownloadFilesTask extends AsyncTask<SmallPinItem, Integer, SmallPinItem> {
	     protected SmallPinItem doInBackground(SmallPinItem... params) {
    		 SmallPinItem spi = params[0];
	    	 try {
                 URL url = new URL(spi.getThumb_url());
                 File file = new File(spi.getThumb_loc());
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
	         } catch(OutOfMemoryError e){
	        	 System.gc();
	        	 if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	         } catch (IOException e) {
	             if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	         } catch (NullPointerException e){
	             if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	         } catch (ConcurrentModificationException e){
	             if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
	         }
	    	 return spi;
	     }

	     protected void onPostExecute(SmallPinItem result) {
	         updateGC(result);
	     }

	 }
}
	
