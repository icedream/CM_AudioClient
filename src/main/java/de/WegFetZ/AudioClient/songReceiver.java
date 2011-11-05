package main.java.de.WegFetZ.AudioClient;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JProgressBar;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class songReceiver extends Thread {

	private InputStream inputStream;
	private DataInputStream dataInputStream;
	private Socket getSocket = null;
	
	private long current = 0;
	private long previous = 0;
	private long lastCalcTime = 0;


	public void run() {

		try {
			getSocket = new Socket(Client.ServerIP, Integer.parseInt(Client.PluginPort) + 1);
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to " + Client.ServerIP);
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			Client.noReconnect = true;
			Client.exit = true;;
			return;
		} catch (NumberFormatException e) {
			System.err.println("Invalid port!");
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			Client.noReconnect = true;
			Client.exit = true;;
			return;
		} catch (IOException e) {
			System.err.println("Cannot get the I/O for " + Client.ServerIP);
			System.err.println("Make sure that you have set the right port and that the ports are forwarded on the Server.");
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			Client.noReconnect = true;
			Client.exit = true;;
			return;
		} // try connect to the CustomMusic pluign songSender thread

		String input = null;

		try {
			OutputStream raus = getSocket.getOutputStream();
			inputStream = getSocket.getInputStream();
			dataInputStream = new DataInputStream(inputStream);
			// ==> the i/o streams

			while ((input = dataInputStream.readUTF()) != null && !Client.exit && !getSocket.isClosed()) { // get message from server


				String[] string = input.split(":");

				if (string.length > 1) {
					if (string[1].equals("error getting answer")) {
						System.out.println("failed to download songs!  songReceiver Thread closing");
						raus.write(-1);
						raus.flush();
						break;

					} else if (string[1].equals("ready")) {// no more songs
						break;

					} else if (string[1].equals("delete")) {// delete a song (user used /cm deletesong)


						String songspath = string[2];
						String[] split = songspath.split("/");

						if (split.length > 4) {
							String filepath = songspath.substring((int) split[0].length() + split[1].length() + 2);
							File deleteFile = new File(filepath);
							
							if (deleteFile.exists()) {
								deleteFile.delete(); // delete the song
								System.out.println("Deleted song: " + filepath);
							}
						}
						raus.write(1); // tell the server to go on sending the
										// songs
						raus.flush();

					} else {

						long songsize = Long.parseLong(string[2]); // size of
																	// the song
																	// on the
																	// server
						String songspath = string[1]; // path of the song on the
														// server
						String[] split = songspath.split("/");

						if (split.length < 5) {
							raus.write(1); // error, skip this song
							raus.flush();
						} else {

							if (!split[3].equalsIgnoreCase("craftplayer{name=" + Client.playername + "}")) { // not a song of the player's own directory

								String filepath = songspath.substring((int) split[0].length() + split[1].length() + 2);
								
								File file = new File(filepath);
								
								File dirFile = new File(file.getParent());
								dirFile.mkdirs(); // create the directory for the
													// song
								
								
								if (!file.exists() || Math.abs(file.length() - songsize) > 500) { // if song doesn't exist or isn't the same size as on the server

									if (file.exists())
										file.delete(); // delete the song
									if (file.createNewFile()) {
										raus.write(0); // tell the server to
														// send the song
										raus.flush();
										songReceive(dataInputStream, file, songsize); // donwload
																			// the
																			// song
										break;
									} else {
										System.out.println("couldn't create " + file.getName() + "! make sure you have writing permissions!");
										raus.write(1); // error, skip the song
										raus.flush();
										break;
									}
								} else {
									raus.write(1); // song exists, go on sending
													// the songs
									raus.flush();
								}
							} else {
								raus.write(1); // skip songs that are uploaded
												// by the player to not download
												// them after they were deleted
												// on the client
								raus.flush();
							}
						}
					}
				} else {
					System.out.println("Failed to get Songlist! songReceiver Thread closing");
					raus.write(-1); // tell the server an error has occured
					break;
				}
			}

			try {
				getSocket.close();
			} catch (IOException e) {}
			// close the I/O streams

		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
				System.err.println("songReceiver couldn't open InputStream: " + e);
			Client.exit = true;;
		}

		try {
			sleep(30000);
		} catch (InterruptedException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
		}

		Thread t = new songReceiver(); // run this thread every 30 seconds
		t.start();
	}

	private void songReceive(DataInputStream dataInputStream, File song, long size) {
		try {

			System.out.println("Downloading " + song.getName() + "...");
			current = 0;
			previous = 0;
			lastCalcTime = 0;
			mainWindow.l_downloadFile.setText(song.getName());

			byte[] buffer = new byte[65536];
			int bytesRead = -1;
					
			OutputStream FileOutputStream = new FileOutputStream(song); // the outputstrean to write the file

			JProgressBar progress = mainWindow.progress_download;
			 progress.setMaximum((int)size); //we're going to get this many bytes
			 progress.setValue(0); //we've gotten 0 bytes so far

			while ((bytesRead = dataInputStream.read(buffer)) != -1 && !Client.exit) {
				FileOutputStream.write(buffer, 0, bytesRead);
				current += bytesRead; //we've progressed a little so update current	        
		        progressGUI(progress);
		   }// ==> get the stream from server and write the song file

			FileOutputStream.close();

			mainWindow.l_downloadFile.setText("-");
			mainWindow.l_downloadSpeed.setText("0 KB/s");
			progress.setValue(0);
			System.out.println("Download done!");

		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			System.err.print("Couldn't get song: " + e);
		}
	}
	
	private void progressGUI(JProgressBar progress) {
		 				
		long time = System.currentTimeMillis();
		
		if ((time - lastCalcTime) >= 249) {
			long byteDiff = current - previous; //written bytes
			float timeDiff = (float) (time - lastCalcTime)/1000; //time in seconds needed to write the bytes
			float bytesPerSecond = (float) byteDiff/timeDiff;
			lastCalcTime = System.currentTimeMillis();
			previous = current;
			
			int KBperSecond = (int) (bytesPerSecond / 1000);	
			progress.setValue((int)current); //tell progress how far we are
			mainWindow.l_downloadSpeed.setText(String.valueOf(KBperSecond + " KB/s"));
		}

	}
}