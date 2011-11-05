package main.java.de.WegFetZ.AudioClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class ClientProperties extends Properties {
	static final long serialVersionUID = 0L;
	private String fileName;

	public ClientProperties(String file) {
		this.fileName = file;
	}

	public void load() {
		File file = new File(this.fileName);
		if (file.exists()) {
			try {
				load(new FileInputStream(this.fileName)); // read the property
															// list
			} catch (IOException ex) {
				if (mainWindow.check_debug.isSelected()) ex.printStackTrace();
			}
		}
	}

	public void save(String start) {
		try {
			store(new FileOutputStream(this.fileName), start); // save the
																// property list
		} catch (IOException ex) {
			if (mainWindow.check_debug.isSelected()) ex.printStackTrace();
		}
	}

	public String getString(String key, String value) {
		if (containsKey(key)) {
			return String.valueOf(getProperty(key)); // get a string value
														// from the property
														// file
		}
		return value;
	}
	
	public Boolean getBoolean(String key, boolean value) {
		if (containsKey(key)) {
			return Boolean.valueOf(getProperty(key)); // get a boolean value
														// from the property
														// file
		}
		return value;
	}

	public String getStringAndReplaceKey(String oldKey, String newKey, String value) {
		if (containsKey(newKey)) {
			return String.valueOf(getProperty(newKey)); // get string value
														// from the property
														// file
		} else if(containsKey(oldKey)) {
			value = String.valueOf(getProperty(oldKey));
			remove(oldKey); //remove the old key and value
			put(newKey, String.valueOf(value)); //add the new key and the value
		}
		return value;
	}
	
	public void addString(String key, String value) {
		put(key, String.valueOf(value));
	}
	
}
