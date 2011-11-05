package main.java.de.WegFetZ.AudioClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class CPlaying extends Thread {

	private Socket cSocket;
	public final static HashMap<String, Float> Players = new HashMap<String, Float>(); // map of all players
	public final static HashMap<String, Boolean> Playing = new HashMap<String, Boolean>(); // map of players that are running
	public final static HashMap<String,Boolean> dontReport = new HashMap<String,Boolean>();//don't report errors to console for this file
	public static long serverTime = 0;
	public static Boolean globalPlaying = false; // is a song playing globally?

	public CPlaying(Socket socket) {
		super("CPlaying");
		this.cSocket = socket;
	}

	public void run() {

		String inLine = null;
		String volume = null;
		String box = null;
		String songString = null;
		String number;
		int priority;
		Boolean offset = true;

		try {
			OutputStream out = cSocket.getOutputStream();
			BufferedReader inputs = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
			// ==> the I/O streams

			while (!Client.exit && !cSocket.isClosed() && ((inLine = inputs.readLine()) != null)) {

				if (inLine.equals("bye")) {
					Client.exit = true;
					Players.clear();
					break;

				} else if (inLine.equals("stopall")) {
					Players.clear();

				} else if (inLine.equals("check")) {
					out.write(13);
					out.flush();

				} else {
					String[] value = inLine.split(":");

					if (value.length > 5) { // new player
						volume = value[0];
						box = value[1];
						number = value[2];
						priority = Integer.parseInt(value[3]);
						songString = value[4]; // string of songs to be played
												// (seperated by '>>')
						serverTime = Long.parseLong(value[5]);

						if (number.equalsIgnoreCase("-1") || number.equalsIgnoreCase("-2"))
							offset = false;
						else
							offset = true; // play from beginnig (user used /cm
											// play)

						try {
							if (songString != null && songString.length() > 2) {
								if (!Players.containsKey(songString + ":" + number) || !Playing.containsKey(songString + ":" + number)) {
									Players.put(songString + ":" + number, Float.parseFloat(volume));
									Playing.put(songString + ":" + number, true);
									new Player(box.toLowerCase(), number, priority, songString, offset).start();
									sleep(250);
									// ==> start new player
	
								} else {
									if (volume.equals("0")) {
										Players.remove(songString + ":" + number); // stop
																					// playing
										sleep(250);
	
									} else {
										Players.put(songString + ":" + number, Float.parseFloat(volume)); // only
																											// change
																											// volume
									}
								}
							}
						} catch (InterruptedException e) {
							if (mainWindow.check_debug.isSelected()) e.printStackTrace();
						}
					}
				}
			}


		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			Client.exit = true;
		} catch (NumberFormatException ex) {
			if (mainWindow.check_debug.isSelected()) 
				ex.printStackTrace();
		}
		clearMaps();
		
		if (Client.exit)
			Client.closeCons();
	}
	
	private void clearMaps() {
		Players.clear();
		Playing.clear();
		dontReport.clear();
	}

}
