package listeners;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.CompoundButton;
import classes.NSocket;

public class SwitchActionListener implements CompoundButton.OnCheckedChangeListener {
	private String location;
	private String device;
	private boolean status;
	
	public SwitchActionListener(String location, String device, boolean status) {
		this.location = location;
		this.device = device;
		this.status = status;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		this.status = isChecked;
		try {
			JSONObject json = new JSONObject();
			JSONObject code = new JSONObject();
			json.put("message", "send");
			code.put("location", this.location);
			code.put("device", this.device);
			code.put("state", this.status);
			json.put("code", code);
			NSocket.write(json.toString());
		} catch(JSONException e) {
		}
	}
}