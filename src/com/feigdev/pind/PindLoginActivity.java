package com.feigdev.pind;

import com.feigdev.pind.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PindLoginActivity extends Fragment {
	private static PindService mBoundService;
	private View curView;
	private static PindResponseListener prl;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public static PindLoginActivity newInstance(int index, PindService ps, PindResponseListener pr){
    	PindLoginActivity f = new PindLoginActivity();
    	mBoundService = ps;
    	prl = pr;
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
    	curView = inflater.inflate(R.layout.login, container, false); 

        ((Button)curView.findViewById(R.id.login_button)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (null != mBoundService){
					String user = ((EditText)curView.findViewById(R.id.email_text)).getText().toString();
					String pass = ((EditText)curView.findViewById(R.id.password_text)).getText().toString();
					mBoundService.postLogin(user,pass);
				}
			}
        	
        });
        ((Button)curView.findViewById(R.id.cancel_login_button)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				prl.onLoginCancel();
			}
        	
        });
        return curView;
    }
    
}
