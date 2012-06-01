package com.feigdev.pind;

import com.feigdev.pind.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ImageScraper extends Fragment implements ImageScraperUpdater {
	private static final String TAG = "ImageScraper";
	private ArrayList<ResultsHolder> rh;
	private ArrayList<ResultsHolder> rh_full;
	private MessageAdapter listAdapter; 
	private ListView listView;
	int mNum;
	private View curView;
	private ImageScraperListener prl;
	private static String source_url;
    private ProgressDialog pDialog;
    private int curIndex = 0;
    
	// Container Activity must implement this interface
    public interface ImageScraperListener {
        public void onImageSelected(String src_url, String img_url, String img_loc);
        public void onCancelImageScraper();
        public void onImageScraperFailed();
        public void onImageScraperNoPin();
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
    public static ImageScraper newInstance(int index, String src_url) {
    	ImageScraper f = new ImageScraper();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        
        source_url = src_url;
        
        return f;
    }

	@Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        try {
            prl = (ImageScraperListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DetailsActionListener");
        }
    }

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!Constants.PV){
        	return null;
        }
    	if (null == container){
        	prl.onImageScraperFailed();
        	return null;
        }
        curView = inflater.inflate(R.layout.results_list, container, false); 
        rh = new ArrayList<ResultsHolder>();
        rh_full = new ArrayList<ResultsHolder>();

        listView = (ListView) curView.findViewById(R.id.results_list);
        listAdapter = new MessageAdapter(getActivity());
        listView.setAdapter(listAdapter);
		pDialog = ProgressDialog.show(curView.getContext(), "", "Loading. Please wait...", true);

		((Button)curView.findViewById(R.id.results_list_prev_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (curIndex >= 5){
					curIndex -= 5;
					if (curIndex < 0){
						curIndex = 0;
					}
					fillRh();
				}
			}
		});
		((Button)curView.findViewById(R.id.results_list_next_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (curIndex < rh_full.size()){
					curIndex += 5;
					if (curIndex >= rh_full.size()){
						curIndex = rh_full.size()-2;
					}
					fillRh();
				}
			}
		});
        
		final ImageScraperUpdater isu = ImageScraper.this;
		final String src_url  = source_url;
		
        new Thread(){
        	public void run(){
        		request(src_url,isu);
        	}
        }.start();
        
        return curView;
    }    
    
    public void fillRh(){
		rh.clear();
		for (int i = curIndex ; i < curIndex + 5; i++){
			if (i < rh_full.size() && i >= 0){
				rh.add(rh_full.get(i));
			}
			else {
				break;
			}
		}
		listAdapter.notifyDataSetInvalidated();
    	listAdapter.notifyDataSetChanged();
    }

    Handler handler = new Handler();

    @Override
    public void onNewImage(final ResultsHolder result){
    	handler.post(new Runnable(){
			@Override
			public void run() {
				rh.add(result);
		    	listAdapter.notifyDataSetChanged();
		    	pDialog.dismiss();
			}
		});
    }
    
	
    private void request(String url, ImageScraperUpdater isu){
    	if (url.toLowerCase().endsWith("jpg") || url.toLowerCase().endsWith("jpeg") 
    			|| url.toLowerCase().endsWith("png") || url.toLowerCase().endsWith("bmp") 
    			|| url.toLowerCase().endsWith("gif")){
    		ResultsHolder result;
			if (Constants.DEBUG){Log.d(TAG,"image: " + url);}
			result = new ResultsHolder(url, Calculator.getFileFromUrl(url));
			try {
				if (downloadFile(result.getUrl()) && new File(result.getImage()).exists() ){
						result.setSize(Calculator.getSizeOfImageFile(result.getImage()));
						rh_full.add(result);
						if (rh.size()< 10){
							isu.onNewImage(result);
						}
					}
				} catch (Error e){
					if (Constants.DEBUG){ System.err.print(e);}							
				} catch (Exception e){
					if (Constants.DEBUG){ System.err.print(e);}
				}
    	}
    	else {
			Document doc;
			try {
				doc = Jsoup.connect(url).get();
			} catch (SocketTimeoutException e1) {
				try {
					System.gc();
					doc = Jsoup.connect(url).get();
				} catch (IOException e) {
					isu.onFailed();
					if (Constants.DEBUG){e.printStackTrace();}
					return;
				}
			} catch (IOException e1) {
				try {
					System.gc();
					doc = Jsoup.connect(url).get();
				} catch (IOException e) {
					isu.onFailed();
					if (Constants.DEBUG){e.printStackTrace();}
					return;
				}
			}
			Elements nopins = doc.getElementsByTag("meta");
			for (Element nopin : nopins){
				if (nopin.attr("name").equals("pinterest") && nopin.attr("content").equals("nopin")){
					if (Constants.DEBUG){Log.d(TAG,"nopin found");}
					handler.post(new Runnable(){
						@Override
						public void run() {
							pDialog.dismiss();
							prl.onImageScraperNoPin();
						}
					});
					return;
				}
			}
			Elements images = doc.getElementsByTag("img");
			ResultsHolder result;
			for (Element image : images){
				String img_loc_tmp = image.attr("src").toString().split("\\?")[0];
				if (Constants.DEBUG){Log.d(TAG,"image: " + img_loc_tmp);}
				if (img_loc_tmp.startsWith("http") || img_loc_tmp.startsWith("ftp")){
					result = new ResultsHolder(image.attr("src"), Calculator.getFileFromUrl(img_loc_tmp));
					try {
						if (downloadFile(result.getUrl()) && new File(result.getImage()).exists() ){
							result.setSize(Calculator.getSizeOfImageFile(result.getImage()));
							rh_full.add(result);
							if (rh.size()< 10){
								isu.onNewImage(result);
							}
						}
					} catch (Error e){
						if (Constants.DEBUG){ System.err.print(e);}							
					} catch (Exception e){
						if (Constants.DEBUG){ System.err.print(e);}
					}
				}
				else {
					result = new ResultsHolder(url + "/" + img_loc_tmp, Calculator.getFileFromUrl(url + "/" + img_loc_tmp));
					try {
						if (downloadFile(result.getUrl()) && new File(result.getImage()).exists() ){
							result.setSize(Calculator.getSizeOfImageFile(result.getImage()));
							rh_full.add(result);
							if (rh.size()< 10){
								isu.onNewImage(result);
							}
						}
					} catch (Error e){
						if (Constants.DEBUG){ System.err.print(e);}							
					} catch (Exception e){
						if (Constants.DEBUG){ System.err.print(e);}
					}
				}
				
			}
    	}
		if (rh.size() < 1){
			isu.onFailed();
		}
    }
    
    private class MessageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		protected MessageViewHolder messageHolder;

		public MessageAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return rh.size();
		}
		 
		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.results_item, null);
			
			messageHolder = new MessageViewHolder();
			messageHolder.size = (TextView) convertView.findViewById(R.id.size_text);
			messageHolder.image = (ImageView) convertView.findViewById(R.id.result_image);
			
			convertView.setTag(messageHolder);
			
			if (!new File(rh.get(position).getImage()).exists()){
				return null;
			}
			
			try {
				messageHolder.image.setImageBitmap(Calculator.resizeImageFromFile(rh.get(position).getImage(),300));
			} catch (Exception e ){
				return null;
			} catch (Error e){
				return null;
			}
			
			messageHolder.size.setText(rh.get(position).getSize());
			
			convertView.setOnClickListener(new View.OnClickListener() {
            	
                @Override
                public void onClick(View view) {
        			if (Constants.DEBUG){Log.d(TAG,"item clicked: " + rh.get(position).getUrl());}
        			prl.onImageSelected(source_url, rh.get(position).getUrl(), rh.get(position).getImage());
                }

              });
			return convertView;
		}

	}
    
    protected class ResultsHolder{
    	private String image;
    	private String size;
    	private String url;
    	
    	public ResultsHolder(String url){
    		this.url =  url;
    		this.image = "";
    	}
    	public ResultsHolder(String url, String image){
    		this.url = url;
    		this.image = image;
    	}
    	
		public String getImage() {
			return image;
		}
		public String getSize() {
			return size;
		}
		public String getUrl() {
			return url;
		}
		public void setSize(Point p){
			size = p.x + "x" + p.y;
		}
    }
    
    protected class MessageViewHolder {
		TextView size;
		ImageView image;
	}
    
    private boolean downloadFile(String f_url){
    	try {
            URL url = new URL(f_url);
            File file = new File(Calculator.getFileFromUrl(f_url)); 
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
            return true;
         } catch (IOException e) {
              if (Constants.DEBUG){ Log.d("ImageManager", "Error: " + e);}
              return false;
         }
    }

	@Override
	public void onFailed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
		    	pDialog.dismiss();
				webErrorDialog("ImageScraper failed","Ok","Something went wrong with the image scraper.");
			}
		});
	}
    
	public void webErrorDialog(String title, String positive, String message){
		final AlertDialog.Builder builder = new AlertDialog.Builder(curView.getContext());
		builder.setTitle(title);
		builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setCancelable(false);
			    
		builder.setMessage(message);
		builder.create().show();
	}
}