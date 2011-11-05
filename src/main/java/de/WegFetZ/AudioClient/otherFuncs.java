package main.java.de.WegFetZ.AudioClient;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

import main.java.de.WegFetZ.AudioClient.Utils.FileUtil;
import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class otherFuncs {

	public static void upload() {
		// uploads a song

		try {
			if (sendSongList() && !Client.exit) { // permission to upload

				String[] songList = FileUtil.recurseInDirFrom("Music/craftplayer{name=" + Client.playername.toLowerCase() + "}").split("\\|"); // get a list of songs


				for (int i = 0; i < songList.length; i++) {

					File file = new File(songList[i]); // the song file

						String extension = FileUtil.getExtension(file); // get
																				// extension

						if (!file.isDirectory() && file.exists() && (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid") || extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram"))) { // file is a mp3, midi or webradio file

							if (isPureAscii(file.getName())) {
								
								String sep = System.getProperty("file.separator");
								if ((file.toString().contains("webradio" + sep)) && (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid"))) {
									
									File moveFile = new File("Music/craftplayer{name=" + Client.playername.toLowerCase() + "}/" + file.getName());
									FileUtil.copyFile(file,moveFile, true);
									file = moveFile;
								} else if(!(file.toString().contains("webradio" + sep)) && (extension.equalsIgnoreCase(".pls") || extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".ram"))) {
									File moveFile = new File("Music/craftplayer{name=" + Client.playername.toLowerCase() + "}/webradio/" + file.getName());
									FileUtil.copyFile(file,moveFile, true);
									file = moveFile;
								} //==> sorting files to right directory
									
									if (file.exists()) {
									
										new songUpload(file).start(); // upload the
																		// song
		
										while (!songUpload.done) {
											Thread.sleep(100); // wait for upload to
																// complete
										}
		
										songUpload.done = false;
									}
							} else
								System.out.println("The file " + file.getName() + " contains non-ASCII characters and won't be uploaded!");
					}
					file = null;
				}
				songList = null;
			}
		} catch (InterruptedException e) {
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
		}
	}

	public static boolean sendSongList() {

		Socket SongListSocket = null;
		Boolean result = true;

		try {
			try {
				SongListSocket = new Socket(Client.ServerIP, (Integer.parseInt(Client.PluginPort)) + 3);
			} catch (UnknownHostException e) {
				System.err.println("Cannot connect to " + Client.ServerIP);
				if (mainWindow.check_debug.isSelected()) 
					e.printStackTrace();
				Client.noReconnect = true;
				Client.exit = true;;
				return false;
			} catch (NumberFormatException e) {
				System.err.println("Invalid port!");
				if (mainWindow.check_debug.isSelected()) 
					e.printStackTrace();
				Client.noReconnect = true;
				Client.exit = true;;
				return false;
			} catch (IOException e) {
				System.err.println("Cannot get the I/O for " + Client.ServerIP);
				System.err.println("Make sure that you have set the right port and that the ports are forwarded on the Server.");
				if (mainWindow.check_debug.isSelected()) 
					e.printStackTrace();
				Client.noReconnect = true;
				Client.exit = true;;
				return false;
			} // connect to CustomMusic plugin songDelete thread

			OutputStream output = SongListSocket.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(output);

			InputStream input = SongListSocket.getInputStream();
			InputStream inputStream = SongListSocket.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			// ==> the I/O streams

			dataOutputStream.writeUTF(Client.playername.toLowerCase());// write playername

			// wait for permission check
			if (dataInputStream.readUTF().equalsIgnoreCase("0")) {
				System.out.println("You don't have permission to upload any files!");
				result = false;
			} else {

				// send list of songs to compare on the server

				String[] songList = FileUtil.recurseInDirFrom("Music/craftplayer{name=" + Client.playername.toLowerCase() + "}").split("\\|"); // get
																															// list
																															// of
																															// songs

				dataOutputStream.writeUTF(String.valueOf(songList.length));// write
																			// length
																			// of
																			// list

				for (int n = 0; n < songList.length; n++) {
					if (!SongListSocket.isClosed()) {
						dataOutputStream.writeUTF(songList[n]);
						dataOutputStream.flush();
						input.read();
					}
				} // ==> send the list
				songList = null;
			}

			// get list of songs from server to compare with own songs

			int number = Integer.parseInt(dataInputStream.readUTF()); // get
																		// length
																		// of
																		// list
			String[] song = new String[number]; // create array of song paths

			int k = 0;
			for (int i = 0; i < number; i++) {
				if (!SongListSocket.isClosed()) {
					String songspath = dataInputStream.readUTF();
					String[] split = songspath.split("/");
					if (split.length > 4) {
						String filepath = songspath.substring((int) split[0].length() + split[1].length() + 2);
						song[k] = filepath;
						k++;
					}
					output.write(1);
				}
			} // receive songlist and put it into the array

			String[] songListe_get = FileUtil.recurseInDirFrom("Music").split("\\|"); // get list of own songs


			for (int n = 0; n < songListe_get.length; n++) {

				if (!Arrays.asList(song).contains(songListe_get[n])) { // if own song not on server


					String[] splitstring = songListe_get[n].split("/");
					if (splitstring.length > 2 && !splitstring[1].equalsIgnoreCase("craftplayer{name=" + Client.playername.toLowerCase() + "}")) {

						File file = new File(songListe_get[n]); // the file

						if (file.exists() && !file.isDirectory() && splitstring[1].contains("craftplayer")) {
							
								file.delete(); // delete the song
								System.out.println("Song removed: " + songListe_get[n]);
							
						}
						file = null;
					}
				}
			}
			songListe_get = null;

			try {
				SongListSocket.close();
				// close the I/O streams
			}catch (IOException e) {};

		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
		} catch (NumberFormatException ex) {
			if (mainWindow.check_debug.isSelected()) ex.printStackTrace();
		}
		return result;
	}

	public static boolean isPureAscii(String v) { // check if string contains
													// non-ASCII characters
		byte bytearray[] = v.getBytes();
		CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
		try {
			CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
			r.toString();
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}



}
