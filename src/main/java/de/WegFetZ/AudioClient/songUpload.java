package main.java.de.WegFetZ.AudioClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JProgressBar;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class songUpload extends Thread {

	private File song;
	private int error = -1;

	public static boolean done = false;

	private long current = 0;
	private long previous = 0;
	private long lastCalcTime = 0;

	
	public songUpload(File mp3) {
		super("songUpload");
		this.song = mp3;
	}

	public void run() {

		done = false;

		Socket upSocket = null;

		try {
			upSocket = new Socket(Client.ServerIP, (Integer.parseInt(Client.PluginPort)) + 2);
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
		} // try connect to the CustomMusic pluign uploadListener thread

		if (song != null && song.exists()) {
			try {

				InputStream byteInput = upSocket.getInputStream();
				OutputStream outputStream = upSocket.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
				PrintWriter printWriter = new PrintWriter(outputStream, true);
				// ==> the I/O streams

				printWriter.println(Client.playername.toLowerCase()); // send playername

				printWriter.println(song.getName()); // send name of song

				String extension = ".mp3";
				int dotPos = song.getName().lastIndexOf(".");
				if (dotPos != -1) {
					extension = song.getName().substring(dotPos); // get
																	// extension
				}

				printWriter.println(song.length()); // send size of song
				printWriter.flush();

				error = byteInput.read(); // read the server's answer

				if (error == 0) {// ready to upload

					mainWindow.l_uploadFile.setText(song.getName());

					current = 0;
					previous = 0;
					lastCalcTime = 0;
					
					byte[] buffer = new byte[16384];
					int bytesRead = -1;


					System.out.println("Uploading " + song.getName() + "..."); 
					JProgressBar progress = mainWindow.progress_upload;
					 progress.setMaximum((int) song.length()); //we're going to get this many bytes
					 progress.setValue(0); //we've gotten 0 bytes so far
					
					
					InputStream inputStream = new FileInputStream(song); // the stream to read the song file

					// flush()
					while ((bytesRead = inputStream.read(buffer)) > 0 && !Client.exit) {
						dataOutputStream.write(buffer, 0, bytesRead);
						current += bytesRead; //we've progressed a little so update current
				        progressGUI(progress);
					}
					dataOutputStream.flush();
					// ==> read the song file and send the stream to the server

					inputStream.close();

					mainWindow.l_uploadFile.setText("-");
					mainWindow.l_uploadSpeed.setText("0 KB/s");
					progress.setValue(0); 
					System.out.println("Upload done!");
					
				} else if (error == 3) {// max files reached
					int maxfiles = byteInput.read();
					System.out.println("You can only upload " + maxfiles + " " + extension + "! The remaining " + extension + " will be ignored.");
					done = true;

				} else if (error == 4) {// song already uploaded
					done = true;

				} else if (error == 5) {// song too big
					int maxsize = byteInput.read();
					System.out.println("Upload of " + song.getName() + " failed: Maximum filesize: " + maxsize + "MB");
					done = true;

				} else if (error == 6) {// no permission to upload
					System.out.println("You don't have permission to upload " + extension + "!");
					done = true;

				} else
					System.out.println("Unknown error while uploading " + song.getName() + " (code: " + error + ")");

				try {
					upSocket.close();
				}catch (IOException e) {}
				// ==> close the I/O streams

			} catch (IOException e) {
				if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			}
		} else
			System.err.println("Upload failed: File not found!");
		done = true;
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
			mainWindow.l_uploadSpeed.setText(String.valueOf(KBperSecond + " KB/s"));
		}

	}

}
