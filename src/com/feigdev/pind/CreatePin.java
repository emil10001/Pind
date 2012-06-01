package com.feigdev.pind;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class CreatePin extends Fragment {
	private static final String TAG = "CreatePin";
	int mNum;
	private View curView;
	private CreatePinListener prl;
	private static String source_url;
	private static String image_url;
	private static String image_loc;
	private static ArrayList<String> board_list;
	
	// Container Activity must implement this interface
    public interface CreatePinListener {
        public void onCreatePinFailed();
        public void onPostNewPin(String img_url, String src_url, byte [] b, String comment, String board_id);
        public void onCancelNewPin();
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
    public static CreatePin newInstance(int index, String url, String src_url, String img_loc, ArrayList<String> boardList) {
    	CreatePin f = new CreatePin();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        source_url = src_url;
        image_url = url;
        
        image_loc = img_loc;
        board_list = boardList;
        
        return f;
    }
    
	@Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        try {
            prl = (CreatePinListener) activity;
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
    	if (container == null){
        	prl.onCreatePinFailed();
        	return null;
        }

        curView = inflater.inflate(R.layout.pin_image, container, false); 
        
        final Spinner boards = (Spinner)curView.findViewById(R.id.pin_spinner);
	    
		final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),
			android.R.layout.simple_spinner_item, board_list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		boards.setAdapter(dataAdapter);
		
        
        ((Button)curView.findViewById(R.id.pin_image_button)).setOnClickListener(new OnClickListener(){
	        @Override
			public void onClick(View v) {
        		try {
					prl.onPostNewPin(image_url,source_url,Calculator.readFile(image_loc),((EditText)curView.findViewById(R.id.create_pin_text)).getText().toString(),boards.getSelectedItem().toString());
				} catch (IOException e) {
					if (Constants.DEBUG){e.printStackTrace();}
				}
			}
			
		});
        
        if (Constants.DEBUG){ Log.d(TAG,"image: " + image_loc); }

        //(getActivity().getResources().getDisplayMetrics().widthPixels - 50)
        
        Bitmap bmp = Calculator.generateLocalBmpPrev(image_loc);
        if (null == bmp){
        	if (Constants.DEBUG){ Log.d(TAG,"bmp is null :("); }
       		return null;
        	
        }
        ((ImageView)curView.findViewById(R.id.pin_image_preview)).setImageBitmap(bmp);
        Calculator.scaleImage( ((ImageView)curView.findViewById(R.id.pin_image_preview)), (getActivity().getResources().getDisplayMetrics().widthPixels - 50));
        if (null == ((ImageView)curView.findViewById(R.id.pin_image_preview)).getDrawable()){
        	return null;
        }
        
        ((Button)curView.findViewById(R.id.pin_image_cancel_button)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				prl.onCancelNewPin();
			}
        	
        });
        
        return curView;
    }
    
}
