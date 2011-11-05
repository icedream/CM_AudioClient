package main.java.de.WegFetZ.AudioClient.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class StringUtil {

	public static URL urlFromString(String string, String ext) {
		// separete input by spaces ( URLs don't have spaces )
		String[] parts = string.split("\\s");

		// Attempt to convert each item into an URL.
		for (String item : parts)
			try {
				String cutted = cutToURL(item, ext);
				URL url = new URL(cutted);
				// if this is possible then return the item

				return url;
			} catch (MalformedURLException e) {
				// there was an URL that was not it!...
			}

		return null;
	}

	private static String cutToURL(String item, String ext) {
		String result = null;

		if (item.length() < 5)
			return result;
		
		if (ext.equalsIgnoreCase(".asx")) {
			if (item.toLowerCase().contains("href") && item.contains("=")) {
				String[] s = item.split("\"");
				if (s.length > 1)
					result = s[1];
			}
		} else if (ext.equalsIgnoreCase(".pls")) {
			if (item.toLowerCase().contains("File") && item.contains("="))
				result = item.substring(6);
		} else if (ext.equalsIgnoreCase(".ram")) {
			result = item;
		} else
			result = item;
		
		return result;
	}
	
}
