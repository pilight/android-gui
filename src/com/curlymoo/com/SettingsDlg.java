package com.curlymoo.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsDlg extends Activity {

    private RelativeLayout.LayoutParams rowParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams lblParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams txtParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
	private static Runnable onCreatedFunc = null;    
    private static Runnable onFuncSave = null;
    private static EditText txtPort;
    private static EditText txtServer;
    
    private static SettingsDlg singleton = null;
    
    private String serverFlt = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
	private String ipPattern = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";   
	
	public static SettingsDlg getInstance() {
		if(singleton == null) {
			singleton = new SettingsDlg();
		}
		return singleton;
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_dlg);
		singleton = this;
		
		lblParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lblParams.setMargins(5, 12, 0, 5);
		lblParams.width = 50;
		
        /* Text */		
		txtParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		txtParams.setMargins(0, 5, 5, 5); 
		txtParams.width = 150;
        /* End switch */		

        /* Switch */		
		btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnParams.setMargins(0, 5, 5, 5); 
		btnParams.width = 50;
        /* End switch */
		
		ScrollView scrollVw = (ScrollView)findViewById(R.id.container);
		LinearLayout container = new LinearLayout(scrollVw.getContext());
		container.setOrientation(LinearLayout.VERTICAL);		

		RelativeLayout row = new RelativeLayout(container.getContext());
		row.setLayoutParams(rowParams);
		
		TextView lblServer = new TextView(row.getContext());
		lblServer.setLayoutParams(lblParams);
		lblServer.setText("Server:");
		row.addView(lblServer);

		txtServer = new EditText(row.getContext());
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start,
                    int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                    source.subSequence(start, end) +
                    destTxt.substring(dend);
                    if (!resultingTxt.matches(serverFlt)) { 
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
            return null;
            }
        };
        txtServer.setFilters(filters);
		txtServer.setLayoutParams(txtParams);		
		row.addView(txtServer);
		
		RelativeLayout row1 = new RelativeLayout(container.getContext());
		row1.setLayoutParams(rowParams);
		
		TextView lblPort = new TextView(row1.getContext());
		lblPort.setLayoutParams(lblParams);
		lblPort.setText("Port:");
		row1.addView(lblPort);

		txtPort = new EditText(row1.getContext());
		txtPort.setInputType(InputType.TYPE_CLASS_NUMBER);
		txtPort.setLayoutParams(txtParams);		
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(5);
		txtPort.setFilters(FilterArray);
		row1.addView(txtPort);	
		
		RelativeLayout row2 = new RelativeLayout(container.getContext());
		row2.setLayoutParams(rowParams);
		
		Button btnSave = new Button(row2.getContext());
		btnSave.setLayoutParams(btnParams);
		btnSave.setText("Save");
		btnSave.setOnClickListener(onSaveClicked);
		row2.addView(btnSave);			
		
		container.addView(row);
		container.addView(row1);
		container.addView(row2);
		scrollVw.addView(container);
		onCreatedFunc.run();
	}
	
	private OnClickListener onSaveClicked = new OnClickListener() {
		public void onClick(View button) {
			if(txtServer.getText().toString().length() == 0 || txtPort.getText().toString().length() == 0) {
				Toast.makeText(singleton, "Please fill in all fields", Toast.LENGTH_SHORT).show();				
			} else if(!txtServer.getText().toString().matches(ipPattern)) {
				Toast.makeText(singleton, "Not a valid ip address", Toast.LENGTH_SHORT).show();
			} else if(!txtPort.getText().toString().matches("^([0-9]{1,5})$")) {
				Toast.makeText(singleton, "Not a valid port", Toast.LENGTH_SHORT).show();
			} else {
				onSave();
			}
		}
	};
	
	public void setServer(String server) {
		txtServer.setText(server);
	}
	
	public void setPort(int port) {
		txtPort.setText(new String().valueOf(port));
	}
	
	public String getServer() {
		return txtServer.getText().toString();
	}
	
	public Integer getPort() {
		return Integer.valueOf(txtPort.getText().toString());
	}		
	
	public void setOnSave(Runnable func) {
		this.onFuncSave = func;
	}
	
	public void onSave() {
		this.onFuncSave.run();
		this.finish();
	}
	
	public void setOnCreated(Runnable func) {
		onCreatedFunc = func;
	}
	
}
