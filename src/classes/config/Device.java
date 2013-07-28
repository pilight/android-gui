package classes.config;

import java.util.ArrayList;
import java.util.HashMap;

public class Device {
	private String name;
	private HashMap<String, ArrayList<String>> settings = new HashMap<String, ArrayList<String>>();

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, ArrayList<String>> getSettings() {
		return this.settings;
	}

	public void setSettings(HashMap<String, ArrayList<String>> settings) {
		this.settings = settings;
	}
}