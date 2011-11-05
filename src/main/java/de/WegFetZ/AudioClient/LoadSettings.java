package main.java.de.WegFetZ.AudioClient;

import java.io.File;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class LoadSettings {
	static String name;
	static String ServerIP;
	static String PluginPort;
	
	static String inetCon;
	static String upConSpeed;
	
	static boolean debug;
	static boolean saveOnQuit;
	static boolean recon;
	static boolean startCon;

	public static void loadMain() {

		String propertiesFile = "AudioClient.properties";
		ClientProperties properties = new ClientProperties(propertiesFile);
		properties.load(); //load the properties file


		//get the values
			//general tab
		name = properties.getString("Minecraft-Name", "Herobrine");
		ServerIP = properties.getStringAndReplaceKey("Server-IP", "Minecraft-Server-IP", "127.0.0.1");	
		PluginPort = properties.getStringAndReplaceKey("Server-Port", "CustomMusic-Plugin-Port", "4224");//replace key to get rid of misleading formulation (changed Server-Port to Plugin-Port)
		startCon = properties.getBoolean("Connect-On-Startup", false);
		recon = properties.getBoolean("Reconnect-On-Disconnect", true);
		saveOnQuit = properties.getBoolean("Save-Settings-On-Quit", true);
		debug = properties.getBoolean("Debug-Mode", false);
			
			//music tab
		inetCon = properties.getString("Internet-Connection", "DSL 6000");
		upConSpeed = properties.getString("Upload-Rate", "80");
		
		//update gui with new information
			//general tab
		mainWindow.tf_name.setText(name);
		mainWindow.tf_ip.setText(ServerIP);
		mainWindow.tf_cmPort.setText(PluginPort);
		mainWindow.check_startCon.setSelected(startCon);
		mainWindow.check_reconnect.setSelected(recon);
		mainWindow.check_saveOnQuit.setSelected(saveOnQuit);
		mainWindow.check_debug.setSelected(debug);
		
			//music tab
		mainWindow.combo_connection.setSelectedItem(inetCon);
		mainWindow.tf_upConSpeed.setText(upConSpeed);
		
		System.out.println("Settings loaded.");

	}
	
	public static void saveMain() {
		String propertiesFile = "AudioClient.properties";
		ClientProperties properties = new ClientProperties(propertiesFile);
		
		new File("Music/").mkdir();
		
		if (Client.playername != null && Client.playername.length() > 0)
			new File("Music/craftplayer{name=" + Client.playername.toLowerCase() + "}/webradio/").mkdirs(); // create the Music folder and the players directory
		
		try {
			//put the information into the properties list
				//general tav
			properties.put("Minecraft-Name", mainWindow.tf_name.getText());	
			properties.put("Minecraft-Server-IP", mainWindow.tf_ip.getText());
			properties.put("CustomMusic-Plugin-Port", mainWindow.tf_cmPort.getText());
			properties.put("Connect-On-Startup", String.valueOf(mainWindow.check_startCon.isSelected()));
			properties.put("Reconnect-On-Disconnect", String.valueOf(mainWindow.check_reconnect.isSelected()));
			properties.put("Save-Settings-On-Quit", String.valueOf(mainWindow.check_saveOnQuit.isSelected()));
			properties.put("Debug-Mode", String.valueOf(mainWindow.check_debug.isSelected()));
			
				//music tab
			properties.put("Internet-Connection", (mainWindow.combo_connection.getSelectedItem()));
			properties.put("Upload-Rate", mainWindow.tf_upConSpeed.getText());
			
		} catch (NullPointerException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
		}
		
		properties.save("===MC-AudioClient configuration==="); //save the properties file
		System.out.println("Settings saved.");
	}

}