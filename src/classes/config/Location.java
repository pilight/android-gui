package classes.config;

import java.util.HashMap;

public class Location {

	private String name;
	private HashMap<String, Device> devices = new HashMap<String, Device>();
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HashMap<String, Device> getDevices() {
		return this.devices;
	}
	public void setDevices(HashMap<String, Device> devices) {
		this.devices = devices;
	}
}