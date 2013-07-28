package listeners;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.AdapterView;
import classes.NSocket;

public class DimmerChangeListener implements AdapterView.OnItemSelectedListener {
	private String location;
	private String device;
	private boolean initialized = false;
	
	public DimmerChangeListener(String location, String device) {
		this.location = location;
		this.device = device;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		try {
			if(!initialized) {
				initialized = true;
		    } else {			
		    	JSONObject json = new JSONObject();
		    	JSONObject code = new JSONObject();
		    	json.put("message", "send");
		    	code.put("location", this.location);
		    	code.put("device", this.device);
		    	code.put("state", parent.getItemAtPosition(pos).toString());
		    	json.put("code", code);
		    	NSocket.write(json.toString());
		    }
		} catch(JSONException e) {
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
}