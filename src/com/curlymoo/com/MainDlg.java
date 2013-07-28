package com.curlymoo.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import listeners.DimmerChangeListener;
import listeners.SwitchActionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ToggleButton;
import classes.Config;
import classes.config.Device;
import classes.config.Location;

public class MainDlg extends Activity {
    
	private static Runnable onCreatedFunc = null;
	private static Runnable onCloseFunc = null;
	private static HashMap componentMap = new HashMap<String,View>();
	private static JSONObject updateJson = null;
	
    private RelativeLayout.LayoutParams rowParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams lblParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams spnParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private RelativeLayout.LayoutParams valParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	private int id = 0;
	
    private static MainDlg singleton = null;	

	public static MainDlg getInstance() {
		if(singleton == null) {
			singleton = new MainDlg();
		}
		return singleton;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dlg);   


		lblParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lblParams.setMargins(5, 12, 0, 5);
		lblParams.width = 180;
		
        /* Switch */		
		btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnParams.setMargins(0, 5, 5, 5); 
		btnParams.width = 60;
        /* End switch */
	
        /* Spinner */		
		spnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		spnParams.setMargins(0, 5, 5, 5); 
		spnParams.width = 60;
        /* End spinner */		
	
        /* Value */		
		valParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		valParams.setMargins(0, 5, 5, 5); 
		valParams.width = 60;
        /* End value */		
		
		ScrollView frame = (ScrollView)findViewById(android.R.id.tabcontent);
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();        

		boolean state = false;        
        for(Map.Entry<String, Location> lentry : Config.getConfig().entrySet()) {
        	
			Location location = (Location)lentry.getValue();
	       
			LinearLayout tab = new LinearLayout(frame.getContext());
			tab.setOrientation(LinearLayout.VERTICAL);
	        tab.setId(id++);
			registerComponent(tab, "tab", new String().valueOf(id));
	        for(Map.Entry<String, Device> dentry : location.getDevices().entrySet()) {
	        
		        Device device = (Device)dentry.getValue();
	
				switch(Integer.parseInt(device.getSettings().get("type").get(0))){
					case 1:
						if(new String("on").equals(device.getSettings().get("state").get(0))) {
							state = true;
						} else {
							state = false;
						}
						createSwitchElement(tab, lentry.getKey().toString(), dentry.getKey().toString(), device.getName(), state);
					break;
					case 2:
						ArrayList<String> values = device.getSettings().get("values"); 
						createDimmerElement(tab, lentry.getKey().toString(), dentry.getKey().toString(), device.getName(), values.toArray(new String[values.size()]), device.getSettings().get("state").get(0).toString());
					break;
					case 3:
						state = true;
						createWeatherElement(tab, lentry.getKey().toString(), dentry.getKey().toString(), device.getName(), state);
					break;
				}
	        }

			frame.addView(tab);
	
	        TabSpec spec = tabHost.newTabSpec(location.getName());
	        spec.setContent(tab.getId());
	        TextView txt=new TextView(this);
	        txt.setText(location.getName());
	        txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
	        txt.setBackgroundColor(Color.DKGRAY);
	        txt.setHeight(30);
	        spec.setIndicator(txt);
	        tabHost.addTab(spec);
		}

		onCreatedFunc.run();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add("Settings");
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().toString().equals("Settings")) {
	    	Intent i = new Intent(this, SettingsDlg.getInstance().getClass());
	    	startActivity(i);
	    	this.finish();
		}
     	return super.onOptionsItemSelected(item);
    }
    
	public void setOnCreated(Runnable func) {
		onCreatedFunc = func;
	}
	
	public void update(JSONObject json) {
		updateJson = json;
		this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
				if(updateJson.has("values") && updateJson.has("origin") && updateJson.has("devices") && updateJson.has("type")) {
					try {
						if(new String("config").equals(updateJson.getString("origin"))) {
							JSONObject values = updateJson.optJSONObject("values");
							int type = updateJson.getInt("type");
							JSONObject locations = updateJson.optJSONObject("devices");
							
							Iterator<?> lit = locations.keys();
							while(lit.hasNext()) {
								String location = (String)lit.next().toString();
								JSONArray devices = (JSONArray)locations.optJSONArray(location);
								for(Short i=0; i<devices.length(); i++) {
									String device = (String)devices.get(i).toString();
									Iterator<?> vit;
									switch(type) {
										case 1:
									        ToggleButton button = (ToggleButton)getComponentByName(location, device);
											vit = values.keys();
											while(vit.hasNext()) {
												String key = (String)vit.next().toString();
												try {
													button.setOnCheckedChangeListener(null);
													if(new String("state").equals(key)) {
														if(new String("off").equals(values.getString(key))) {
															button.setChecked(false);
														} else {
															button.setChecked(true);
														}
													}
													button.setOnCheckedChangeListener(new SwitchActionListener(location, device, button.isChecked()));
												} catch(JSONException e) {
												}
											}
										break;
										case 2:
											Spinner spinner = (Spinner)getComponentByName(location, device);
											vit = values.keys();
											while(vit.hasNext()) {
												String key = (String)vit.next().toString();
												if(new String("state").equals(key)) {
													ArrayAdapter adapter = (ArrayAdapter)spinner.getAdapter();
													try {
														spinner.setOnItemSelectedListener(null);
														spinner.setSelection((adapter.getPosition(values.getString(key))));
														spinner.setOnItemSelectedListener(new DimmerChangeListener(location, device));
													} catch(JSONException e) {
													}
												}
											}
										break;
										case 3:
											TextView lblTemp = (TextView)getComponentByName(location, device+"_temp");
											TextView lblHumi = (TextView)getComponentByName(location, device+"_humi");
											TextView lblBatt = (TextView)getComponentByName(location, device+"_batt");
			
											vit = values.keys();
											while(vit.hasNext()) {
												try {									
													String key = (String)vit.next().toString();
													if(new String("humidity").equals(key)) {
														lblHumi.setText(values.getString(key));
													}
													if(new String("battery").equals(key)) {
														lblBatt.setText(values.getString(key));
													}
													if(new String("temperature").equals(key)) {
														lblTemp.setText(values.getString(key));
													}
												} catch(JSONException e) {
												}
											}
										break;
									}
								}
							}
						}
					} catch(JSONException e) {
					}
				}
            }
		});
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
    
	private void createSwitchElement(LinearLayout panel, String lid, String did, String dtext, boolean status) {

		RelativeLayout row = new RelativeLayout(panel.getContext());
		row.setLayoutParams(rowParams);
		
		TextView txtLabel = new TextView(row.getContext());
		txtLabel.setLayoutParams(lblParams);
		txtLabel.setId(id++);
		txtLabel.setText(dtext);
		row.addView(txtLabel);

		ToggleButton button = new ToggleButton(row.getContext());
		button.setLayoutParams(btnParams);
		button.setChecked(status);
		button.setOnCheckedChangeListener(new SwitchActionListener(lid, did, status));
		registerComponent(button, lid, did);		
		row.addView(button);
		
		panel.addView(row);
	}
	
	private void createDimmerElement(LinearLayout panel, String lid, String did, String dtext, String values[], String state) {
		RelativeLayout row = new RelativeLayout(panel.getContext());
		row.setLayoutParams(rowParams);
		
		TextView txtLabel = new TextView(row.getContext());
		txtLabel.setLayoutParams(lblParams);
		txtLabel.setText(dtext);
		row.addView(txtLabel);
		
		Spinner spinner = new Spinner(row.getContext());
		spinner.setLayoutParams(spnParams);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, values);

		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getPosition(state));
		spinner.setId(id++);
		spinner.setOnItemSelectedListener(new DimmerChangeListener(lid, did));
		registerComponent(spinner, lid, did);		
		row.addView(spinner);
		
		panel.addView(row);	
	}
	
	private void createWeatherElement(LinearLayout panel, String lid, String did, String dtext, boolean status) {
		RelativeLayout row1 = new RelativeLayout(panel.getContext());
		row1.setLayoutParams(rowParams);
		
		TextView txtLabel = new TextView(row1.getContext());
		txtLabel.setLayoutParams(lblParams);
		txtLabel.setText(dtext);
		row1.addView(txtLabel);
		panel.addView(row1);
		
		RelativeLayout row2 = new RelativeLayout(panel.getContext());
		row2.setLayoutParams(rowParams);
		
		TextView tempNameLabel = new TextView(row2.getContext());
		tempNameLabel.setLayoutParams(lblParams);
		tempNameLabel.setText("- Temperature:");
		row2.addView(tempNameLabel);

		TextView tempValLabel = new TextView(row2.getContext());
		tempValLabel.setLayoutParams(valParams);
		tempValLabel.setText("?");
		tempValLabel.setId(id++);
		row2.addView(tempValLabel);
		panel.addView(row2);
		registerComponent(tempValLabel, lid, did+"_temp");
		
		RelativeLayout row3 = new RelativeLayout(panel.getContext());
		row3.setLayoutParams(rowParams);
		
		TextView humiNameLabel = new TextView(row3.getContext());
		humiNameLabel.setLayoutParams(lblParams);
		humiNameLabel.setText("- Humidity:");
		row3.addView(humiNameLabel);

		TextView humiValLabel = new TextView(row3.getContext());
		humiValLabel.setLayoutParams(valParams);
		humiValLabel.setText("?");
		humiNameLabel.setId(id++);	
		row3.addView(humiValLabel);
		registerComponent(humiNameLabel, lid, did+"_humi");
		panel.addView(row3);
		
		RelativeLayout row4 = new RelativeLayout(panel.getContext());
		row4.setLayoutParams(rowParams);		
		
		TextView battNameLabel = new TextView(row4.getContext());
		battNameLabel.setLayoutParams(lblParams);
		battNameLabel.setText("- Battery:");
		row4.addView(battNameLabel);

		TextView battValLabel = new TextView(row4.getContext());
		battValLabel.setLayoutParams(valParams);
		battValLabel.setText("?");
		battValLabel.setId(id++);
		row4.addView(battValLabel);
		registerComponent(battValLabel, lid, did+"_batt");
		panel.addView(row4);
	}	
	
	private void registerComponent(View component, String location, String id) {
		if(componentMap.get(location+"_"+id) == null) {
			componentMap.put(location+"_"+id, component);
		}
	}

	public View getComponentByName(String location, String id) {	
	    if(componentMap.get(location+"_"+id) != null) {
	    	return (View)componentMap.get(location+"_"+id);
	    } else { 
	    	return null;
	    }
	}	
}
