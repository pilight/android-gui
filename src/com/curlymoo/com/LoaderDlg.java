package com.curlymoo.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class LoaderDlg extends Activity {

	private static TextView lblMessage;
	private static ProgressBar progressBar;
    
	private static Runnable onCreatedFunc = null;
	private static Runnable onCloseFunc = null;
	
    private static LoaderDlg singleton = null;	
	
	public static LoaderDlg getInstance() {
		if(singleton == null) {
			singleton = new LoaderDlg();
		}
		return singleton;
	}	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_dlg);     
        
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.container);        
        
        lblMessage = new TextView(this);
        RelativeLayout.LayoutParams lblParams = new RelativeLayout.LayoutParams((int)LayoutParams.WRAP_CONTENT, (int)LayoutParams.WRAP_CONTENT);
        lblParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lblParams.addRule(RelativeLayout.CENTER_VERTICAL);

		lblMessage.setLayoutParams(lblParams);
		lblMessage.setId(1);
		lblMessage.setPadding(5, 0, 0, 5);

        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams((int)LayoutParams.WRAP_CONTENT, (int)LayoutParams.WRAP_CONTENT);
        progressParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressParams.addRule(RelativeLayout.CENTER_VERTICAL);
        progressParams.addRule(RelativeLayout.BELOW, lblMessage.getId());
		
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setProgress(0);
		progressBar.setLayoutParams(progressParams);
		progressBar.getLayoutParams().width = 200;
		progressBar.setPadding(5, 0, 0, 5);
		relativeLayout.addView(lblMessage);
		relativeLayout.addView(progressBar);
		onCreatedFunc.run();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add("Settings");
		menu.add("Restart");
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().toString().equals("Settings")) {
	    	Intent i = new Intent(this, SettingsDlg.getInstance().getClass());
	    	startActivity(i);
	    	this.finish();
		} else if(item.getTitle().toString().equals("Restart")) {
			onCreatedFunc.run();
		}		
     	return super.onOptionsItemSelected(item);
    }    
    
    public void update(final int percent, final String message) {
    	this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	lblMessage.setText(message);
            	progressBar.setProgress(percent);
            }
        });
    }
    
	public void setOnCreated(Runnable func) {
		onCreatedFunc = func;
	}
	
	public void setOnClose(Runnable func) {
		onCloseFunc = func;
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            onCloseFunc.run();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
