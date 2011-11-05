package main.java.de.WegFetZ.AudioClient.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class FileUtil {

	public static String[] listOnlyFiles(String path) { // returns an array of
														// all files in a
														// directory but no
														// subdirectories

		File dirFile = new File(path);

		if (!dirFile.exists())
			return null;

		String[] list = dirFile.list();
		List<String> resultList = new ArrayList<String>();

		for (int i = 0; i < list.length; i++) {
			if (!(new File(path + list[i]).isDirectory()))
				resultList.add(list[i]);
		}

		String[] result = new String[resultList.size()];
		for (int i = 0; i < resultList.size(); i++) {
			result[i] = resultList.get(i);
		}

		return result;
	}

	public static String recurseInDirFrom(String dirItem) { // returns a string
		// of files with
		// their fullpath
		File file;
		String list[], result;

		result = dirItem;

		file = new File(dirItem);
		if (file.isDirectory()) {
			list = file.list();
			for (int i = 0; i < list.length; i++)
				result = result + "|" + recurseInDirFrom(dirItem + "/" + list[i]);
		}
		return result;
	}

	public int getNumberOfFiles(String path, String extension) {
		int filecount = 0;
		File[] Files = new File(path).listFiles();
		for (int i = 0; i < Files.length; i++) {
			int dotPos = Files[i].getName().lastIndexOf(".");
			if (dotPos != -1) {
				String ext = Files[i].getName().substring(dotPos);
				if (extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid")) {
					if (ext.equalsIgnoreCase(".midi") || ext.equalsIgnoreCase(".mid"))
						filecount++;
				} else if (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram")) {
					if (ext.equalsIgnoreCase(".pls") || ext.equalsIgnoreCase(".asx") || ext.equalsIgnoreCase(".ram"))
						filecount++;
				} else if (ext.equalsIgnoreCase(extension)) {
					filecount++;
				}
			}
		}
		return filecount;
	}

	public static String getExtension(File file) {
		String result = null;

		if (file.exists()) {
			int dotPos = file.getName().lastIndexOf(".");
			if (dotPos != -1) {
				result = file.getName().substring(dotPos);
			}
		}

		return result;
	}

	public static boolean copyFile(File inputFile, File outputFile, Boolean moveFile) {

		if (!inputFile.exists())
			return false;

		if (outputFile.exists())
			outputFile.delete();

		InputStream in;

		try {

			in = new FileInputStream(inputFile);
			OutputStream out = new FileOutputStream(outputFile);

			byte[] buffer = new byte[65536];
			int bytesRead = -1;

			while ((bytesRead = in.read(buffer)) != -1)
				out.write(buffer, 0, bytesRead);

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
			return false;
		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
			return false;
		}
		
		if (moveFile)
			inputFile.delete();

		return true;

	}
	
	public static String getTextContent(URL url) throws IOException {
	    Scanner s = new Scanner(url.openStream()).useDelimiter("\\Z");;
	    String content = s.next();
	    return content;
	}

	public static String getPlsTitle(File file) {
		String result = file.getName();
		
		if (!file.exists())
			return file.getName();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String inLine;
			while ((inLine = br.readLine()) != null) {
				if (inLine.toLowerCase().contains("title") && inLine.length() > 7) {
					if (inLine.contains("(#") && inLine.contains(" - ") && inLine.contains("/") && inLine.contains(") ")) {
						String[] split = inLine.split("\\) "); //remove number of listeners information
						if (split != null && split.length > 1) {
							result = split[1];
							break;
						}
					} else {
							result = inLine.substring(7);
							break;
					}
				}
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
		}
		
		return result;
		
	}

}
