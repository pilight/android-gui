package com.curlymoo.com;

import java.util.prefs.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.widget.Toast;
import classes.Config;
import classes.NSocket;

public class MainController extends Activity {
    /** Called when the activity is first created. */

	private static SharedPreferences prefs = null;
	
	private static String server = null;
	private static int port = 0;
	
	private static boolean mainRun = false;	
	private static boolean loaderActive = false;
	private static boolean loop = true;

	private static MainController singleton;

	private static Steps steps = Steps.WELCOME;
	
	private enum Steps {
		WELCOME,
		IDENTIFY,
		REJECT,
		REQUEST,
		CONFIG,
		SYNC
	}	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleton = this;
        
        this.prefs = this.getSharedPreferences("com.curlymoo.com", Context.MODE_PRIVATE);
        
        /* Start connecting to server when loader
           dialog has been created */
       if(isNetworkConnectionAvailable()) {
	        LoaderDlg.getInstance().setOnCreated(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			waitForSync();
			    			connect();
			    		}
			    	}).start();
				}
			});
	        
	        LoaderDlg.getInstance().setOnClose(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			onClose();
			    		}
			    	}).start();
				}
			});

	        SettingsDlg.getInstance().setOnSave(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			prefs.edit().putString("server",  SettingsDlg.getInstance().getServer()).commit();
			    	    	prefs.edit().putInt("port",  SettingsDlg.getInstance().getPort()).commit();
			    	    	loop = false;
				    		singleton.showLoaderDlg();
			    		}
			    	}).start();
				}
			});	        

	        SettingsDlg.getInstance().setOnCreated(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			 SettingsDlg.getInstance().setServer(prefs.getString("server", ""));
				    		if(prefs.getInt("port", 0) > 0)
				    			 SettingsDlg.getInstance().setPort(prefs.getInt("port", 0));
			    		}
			    	}).start();
				}
			});
	        
	    	/* Check if the main dialog was created */
			MainDlg.getInstance().setOnCreated(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			mainRun = true;
			    		}
			    	}).start();
				}
			});
			
			MainDlg.getInstance().setOnClose(new Runnable() {
				public void run() {
					new Thread(new Runnable() {
			    		public void run() {
			    			onClose();
			    		}
			    	}).start();
				}
			});

    		if(prefs.getString("server", "").length() == 0 || prefs.getInt("port", 0) == 0) {
	    		Intent i = new Intent(singleton,  SettingsDlg.getInstance().getClass());
	    		startActivity(i);	
			} else {
				showLoaderDlg();
			}
	    	
        } else {
        	Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_LONG).show();
        	new Thread(new Runnable() {
        		public void run() {
                    try {
                        Thread.sleep(3500);
                        singleton.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }        			
        		}
        	}).start();
        }
    }
    
    public void onClose() {
    }
    
    public void waitForSync() {
    	/* Start a non-blocking thread */
        new Thread(new Runnable() {
        	public void run() {
        		/* Wait until we have reached the SYNC step */
            	while(steps != Steps.SYNC) {
	        		try {
                        Thread.sleep(10);	
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            	}
            	/* Show the main dialog */
        		singleton.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
    	        		showMainDlg();
                    }
                });
        	}
        }).start();
    }
    
    boolean isNetworkConnectionAvailable() {  
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();     
        if (info == null) return false;
        State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }
    
    public void showLoaderDlg() {
		server = prefs.getString("server", "");
		port = Integer.valueOf(prefs.getInt("port", 0));			
		
		steps = Steps.WELCOME;

        /* Switch to loader dialog */
    	Intent i = new Intent(this, LoaderDlg.getInstance().getClass());
    	startActivity(i);
    }

    public void showMainDlg() {
        /* Switch to main dialog */
    	Intent i = new Intent(this, MainDlg.getInstance().getClass());
    	startActivity(i);
    	loaderActive = false;
    }
    
    public static void connect() {
    	JSONObject json = null;
    	String message = null;
    	boolean isConnected = false;

    	loop = true;
    	Boolean has_data = false;
    	int size = 1025;
    	int timeout = 0;
    	
    	LoaderDlg.getInstance().update(0, "Trying to connect to server...");

    	NSocket.close();
    	NSocket.connect(server, port);
    	steps = Steps.WELCOME;
    	while(loop) {
    		if(NSocket.connected()) {
	    		has_data = false;
	    		timeout = 0;
	
	    		if(steps == Steps.CONFIG) {
	    			size = 102500;
	    		} else {
	    			size = 1025;
	    		}
	
	    		if(NSocket.read(size)) {
	    			try {
	    				json = new JSONObject(NSocket.getLine());
	
	    				if(json.has("message")) {
	    					message = json.getString("message");
	    				}
	    				has_data = true;
	    			} catch(JSONException e) {
	    				has_data = false;
	    			}
				}
	    		if(has_data) {
	    			if(new String("reject client").equals(message)) {
						loop = false;
						LoaderDlg.getInstance().update(100, "Rejected by server...");
						NSocket.close();
						break;
					}

	    			switch(steps) {
	    				case WELCOME:
	    					if(new String("accept connection").equals(message)) {
	    		        		isConnected = true;
	    						NSocket.write("{\"message\":\"client gui\"}");
	    						steps = Steps.IDENTIFY;
	    						LoaderDlg.getInstance().update(25, "Identifying...");
	    					}
	    				break;
	    				case IDENTIFY:
	    					if(new String("accept client").equals(message)) {
	    						steps = Steps.REQUEST;
	    						LoaderDlg.getInstance().update(50, "Accepted by server...");
	    		    		}
	    				case REQUEST:
	    					NSocket.write("{\"message\":\"request config\"}");
	    					LoaderDlg.getInstance().update(75, "Retrieving configuration...");
							steps = Steps.CONFIG;
						break;
	    				case CONFIG:
	    					if(json.has("config")) {
	    						try {
	    							Config.parse(json.getJSONObject("config"));
	
	    							LoaderDlg.getInstance().update(100, "Building main window...");
	    						} catch(JSONException e) {
	    						}
	    						steps = Steps.SYNC;	
	    					}
	    				case SYNC:
	    					try {
	    						if(json.has("origin") && new String("config").equals(json.getString("origin"))) {
	    							if(mainRun) {
	    								loaderActive = false;
	    								MainDlg.getInstance().update(json);
	    							}
	    						}
    						} catch(JSONException e) {
    						}	    						
	    				break;
	    				default:	
	    				break;
	    			}
	    		}
    		} else {
    			if(timeout < 8) {
        			timeout++;
    			} else {
    				if(!isConnected) {
		    			LoaderDlg.getInstance().update(100, "Could not connect to server");
    				} else {
    					steps = Steps.WELCOME;
    					if(!loaderActive) {    						
    						Intent i = new Intent(singleton, LoaderDlg.getInstance().getClass());
    				    	singleton.startActivity(i);
    					}
    				}
					loaderActive = true;
					loop = false;
					break;
    			}
    		}
    		try {
				Thread.sleep(250);
			} catch(InterruptedException e) {
			}
    	}
	}
}