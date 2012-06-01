package com.feigdev.pind;

import com.feigdev.pind.PinItem.Comment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailsFragment extends Fragment {
	private Point imgSize;
	int mNum;
	private static PinItem item;
	private View curView;
	private DetailsActionListener prl;
	
	// Container Activity must implement this interface
    public interface DetailsActionListener {
        public void onLikeSelected(String item);
        public void onRepinSelected(String item, String curComment);
        public void onCommentSelected(String item);
        public void onDetailsFailed();
        public void onBrowserFailed();
    }
	
	public void setImgSize(int x, int y){
		imgSize.set(x, y);
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
    public static DetailsFragment newInstance(int index, PinItem pi) {
    	DetailsFragment f = new DetailsFragment();
    	item = pi;
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

	@Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);
        try {
            prl = (DetailsActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DetailsActionListener");
        }
    }

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null){
        	prl.onDetailsFailed();
        	return null;
        }
        if (null == item){
        	prl.onDetailsFailed();
        	return null;
        }
        
        curView = inflater.inflate(R.layout.details, container, false); 
        
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)((ScrollView)curView.findViewById(R.id.details_scroll_view)).getLayoutParams();
//        params.width = getActivity().getResources().getDisplayMetrics().widthPixels;
//        params.height = getActivity().getResources().getDisplayMetrics().heightPixels * 2 / 3;
//        ((ScrollView)curView.findViewById(R.id.details_scroll_view)).setLayoutParams(params);
        
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "desyrel.ttf");
        
        ((ImageView)curView.findViewById(R.id.user_thumb)).setImageBitmap(item.getUser().genImage());
        ((ImageView)curView.findViewById(R.id.user_thumb)).setOnClickListener(new OnClickListener(){
	        @Override
			public void onClick(View v) {
				openUrl("http://m.pinterest.com/" + item.getUser().getUsername());
			}
			
		});
        ((ImageView)curView.findViewById(R.id.details_image)).setImageURI(Uri.parse(item.getImages().getMob_loc()));
        Calculator.scaleImage( ((ImageView)curView.findViewById(R.id.details_image)), (getActivity().getResources().getDisplayMetrics().widthPixels - 50));
        if (null == ((ImageView)curView.findViewById(R.id.details_image)).getDrawable()){
        	prl.onDetailsFailed();
        	return null;
        }
        ((ImageView)curView.findViewById(R.id.details_image)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				openUrl(item.getSource());
			}
        	
        });
        
        ((TextView)curView.findViewById(R.id.details_user)).setText(item.getUser().getUsername());
        ((TextView)curView.findViewById(R.id.details_description)).setText(item.getDescription());
        ((TextView)curView.findViewById(R.id.details_description)).setTypeface(font);
//        ((TextView)curView.findViewById(R.id.details_source)).setText(item.getSource());
        
//        ((ListView)curView.findViewById(R.id.details_list)).setAdapter(new MessageAdapter(getActivity().getBaseContext()));
        
        ((ImageButton)curView.findViewById(R.id.pinitem_button)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				openUrl("http://m.pinterest.com/pin/" + item.getId());
			}
        	
        });
        
//        public void onLikeSelected(SmallPinItem item);
//        public void onRepinSelected(SmallPinItem item);
//        public void onCommentSelected(SmallPinItem item);

        
        ((LinearLayout)curView.findViewById(R.id.repin_layout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				prl.onRepinSelected(item.getId(), item.getDescription());
			}
        });
        ((LinearLayout)curView.findViewById(R.id.like_layout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				prl.onLikeSelected(item.getId());
			}
        });
        ((LinearLayout)curView.findViewById(R.id.comment_layout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				prl.onCommentSelected(item.getId());
			}
        });

        
        ((TextView)curView.findViewById(R.id.num_repins)).setText(Integer.toString(item.getCounts().getRepins()));
        ((TextView)curView.findViewById(R.id.num_likes)).setText(Integer.toString(item.getCounts().getLikes()));
        ((TextView)curView.findViewById(R.id.num_comments)).setText(Integer.toString(item.getCounts().getComments()));
        
        LinearLayout detailsLayout = (LinearLayout)curView.findViewById(R.id.details_linear_layout);
        
        View comView;
        for (final Comment comment : item.getComments()){
        	
			comView = inflater.inflate(R.layout.comment, null);
			
			((TextView)comView.findViewById(R.id.comment_user)).setText(comment.getUser().getUsername());
			((TextView)comView.findViewById(R.id.comment_text)).setText(Html.fromHtml(comment.getText()).toString());
			((ImageView)comView.findViewById(R.id.comment_icon)).setImageBitmap(comment.getUser().genImage());
			((ImageView)comView.findViewById(R.id.comment_icon)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					openUrl("http://m.pinterest.com/" + comment.getUser().getUsername());
				}
				
			});
			detailsLayout.addView(comView);
        }
        
        ((LinearLayout)curView.findViewById(R.id.details_ad_space)).setEnabled(false);
        ((LinearLayout)curView.findViewById(R.id.details_ad_space)).setVisibility(View.INVISIBLE);
        
        return curView;
    }

    private void openUrl(String url){
    	try{
	    	Intent i = new Intent(Intent.ACTION_VIEW);
	    	i.setData(Uri.parse(url));
	    	startActivity(i);
    	} catch(ActivityNotFoundException e){
    		prl.onBrowserFailed();
    	}
    }
    
    
    
}
	
