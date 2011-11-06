package main.java.de.WegFetZ.AudioClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import main.java.de.WegFetZ.AudioClient.Utils.StringUtil;
import main.java.de.WegFetZ.AudioClient.gui.mainWindow;
import main.java.de.WegFetZ.BasicMP3Player.Mp3Player;
import main.java.de.WegFetZ.BasicMidiPlayer.MidiPlayer;

import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

class Player extends Thread {

	private String box;
	private String songString;
	private String number;
	private int priority;

	private float oldvolume = 0;
	private float volume = 0;
	private String[] songList;

	private Mp3Player player = null;
	private MidiPlayer midiplayer = null;

	private Boolean useOffset = true;

	public Player(String box, String number, int priority, String songString, Boolean useOffset) {
		super("Player");
		this.box = box;
		this.songString = songString;
		this.number = number;
		this.priority = priority;
		this.useOffset = useOffset;
	}

	public void run() {

		songList = songString.split(">>"); // put songs into an array

		String sep = System.getProperty("file.seperator");
		if (songString.contains("webradio" + sep)) {
			
			if (songList.length > 0) {
				File file = new File("Music/" + box + "/" + songList[0]);
				if (file.exists())
					createPlayer(file, 0, 0, 0); // start player at the first song at position 0
	
				if (priority == 11)
					CPlaying.globalPlaying = false; // was playing globally
			}
			
		} else {
			if (useOffset) {
	
				String[] File_Offset = getSongOffset(); // which song to play on
														// which position
	
				File file = new File("Music/" + box + "/" + songList[Integer.parseInt(File_Offset[0])]);
	
				if (file.exists() && !file.isDirectory())
					createPlayer(file, Long.parseLong(File_Offset[1]), Long.parseLong(File_Offset[2]), Integer.parseInt(File_Offset[0])); // start player for the song
	
				if (priority == 11)
					CPlaying.globalPlaying = false; // was playing globally
	
			} else {
	
				File file = new File("Music/" + box + "/" + songList[0]);
				if (file.exists())
					createPlayer(file, 0, 0, 0); // start player at the first song at position 0
	
				if (priority == 11)
					CPlaying.globalPlaying = false; // was playing globally
			}
		}
			
		
		CPlaying.Players.remove(songString + ":" + number);
		CPlaying.Playing.remove(songString + ":" + number);
		// ==> remove the player from the maps
		
		songList = null;
	}

	private void createPlayer(File file, long byteOffset, long timeOffset, int startNumber) {

		if (CPlaying.Players.containsKey(songString + ":" + number)) { // player is still in the map
																		
			try {
				volume = CPlaying.Players.get(songString + ":" + number); // get the current volume for the player
				
				if (CPlaying.Players.size() > 1) { // more than one player --> use priority

					volume = volume * ((float) priority / 10);

					if (CPlaying.globalPlaying && priority != 11) // priority 11 is used for playing globally and will set every other player to their half volume

						volume = volume * (float) 0.5;
					if (priority == 11)
						CPlaying.globalPlaying = true; // playing globally
				}

				int dotPos = file.getName().lastIndexOf(".");

				if (dotPos != -1) {
					String extension = file.getName().substring(dotPos); // get extension
					if (extension.equalsIgnoreCase(".mid") || extension.equalsIgnoreCase(".midi")) { // midi file
						createMidiPlayer(file, timeOffset, startNumber); // use the midiplayer method
						return;
					} else if (extension.equalsIgnoreCase(".asx") || extension.equalsIgnoreCase(".pls")|| extension.equalsIgnoreCase(".ram"))  { //webradio file
						startRadio(file, extension); //use the radio-stream player method
						return;
					}

					player = new Mp3Player();

					player.setVolume((float) volume / 670);

					oldvolume = volume;
					
					player.play(file, byteOffset); // start playing at position
					// byteOffset

					while (CPlaying.Players.containsKey(songString + ":" + number) && player.getState() == 0) { // still playing and in players map


						volume = CPlaying.Players.get(songString + ":" + number);

						if (CPlaying.Players.size() > 1) { // same as above
							volume = volume * ((float) priority / 10);
							if (CPlaying.globalPlaying && priority != 11)
								volume = volume * (float) 0.5;
							if (priority == 11)
								CPlaying.globalPlaying = true;
						}

						if (volume != oldvolume && volume != 0) { // change
																	// volume
							player.setVolume((float) volume / 670);
							oldvolume = volume;

						} else if (volume == 0)
							break;
						try {
							sleep(100);
						} catch (InterruptedException e) {
							if (mainWindow.check_debug.isSelected()) e.printStackTrace();
						}
					}

					player.stop(); // stop playing
					player = null;

				} else
					// couldn't get extension
					System.out.println("Error: Skipping Song: " + file.getName());

				if (volume != 0) {
					startNumber++; // index of next song in songList Array

					if (startNumber >= songList.length) { // last song played
						startNumber = 0;
						if (!useOffset)
							return; // don't repeat when using /cm play
					}

					File nextfile = new File("Music/" + box + "/" + songList[startNumber]); // new
																							// song

					if (nextfile.exists() && !file.isDirectory())
						createPlayer(nextfile, 0, 0, startNumber); // start player for new song to play from beginning

					else
						System.out.println("Missing MP3!");
				}
			} catch (BasicPlayerException e) {
				if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			}
		}
	}
	
	
	private void startRadio(File file, String ext) {
		List<URL> urlList = new ArrayList<URL>(); //list to collect urls
		
		if (!file.exists())
			return;
		
		try {
			FileReader fr = new FileReader(file);
			LineNumberReader ln = new LineNumberReader(fr);

			
			while (true) { //read file for stream url
				String line = ln.readLine();
				
				if (line != null) {
					URL tempURL = StringUtil.urlFromString(line, ext);		
					if (tempURL != null)
						 urlList.add(tempURL); //url found: add to list
				} else 
					break;//end of file
			}
			ln.close();
			
			URL[] url = new URL[urlList.size()];
			url = (URL[]) urlList.toArray(url); //put urls into an array
			
			if (url != null && url.length > 0) {
				
				int i = 0;
				
				while (CPlaying.Players.containsKey(songString + ":" + number) && CPlaying.Players.get(songString + ":" + number) != 0) { // still have to play the stream
					
					if (!createRadioPlayer(url[i])) {
						i++;
						
						if (i >= url.length) {//tried every url
							if (!CPlaying.dontReport.containsKey(file.getName())) { //don't spam the console
								System.out.println("Couldn't get a working URL from " + file.getName());
								CPlaying.dontReport.put(file.getName(), null);
							}
							break;
						}
					}
					try { Thread.sleep(50); }catch(Exception e) {}
				}
				
			} else
				System.out.println("Cannot play stream: " + file.getName());
			
		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
		}
	}

	private boolean createRadioPlayer(URL url) {
		
		try {
			player = new Mp3Player();

			player.setVolume((float) volume / 670);

		oldvolume = volume;
		
		player.play(url); // start playing at position
		// byteOffset

		while (CPlaying.Players.containsKey(songString + ":" + number) && player.getState() == 0) { // still playing and in players map


			volume = CPlaying.Players.get(songString + ":" + number);

			if (CPlaying.Players.size() > 1) { // same as above
				volume = volume * ((float) priority / 10);
				if (CPlaying.globalPlaying && priority != 11)
					volume = volume * (float) 0.5;
				if (priority == 11)
					CPlaying.globalPlaying = true;
			}

			if (volume != oldvolume && volume != 0) { // change
														// volume
				player.setVolume((float) volume / 670);
				oldvolume = volume;

			} else if (volume == 0)
				break;
			try {
				sleep(100);
			} catch (InterruptedException e) {
				if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			}
		}

		player.stop(); // stop playing
		player = null;		
		
		} catch (BasicPlayerException e) {
			if (mainWindow.check_debug.isSelected())
				e.printStackTrace();
			return false;
		}
		return true;
		
	}

	private void createMidiPlayer(File file, long offset, int startNumber) {
		// same as createPlayer but with using MidiPlayer instead of Mp3Player

		if (CPlaying.Players.containsKey(songString + ":" + number)) {

			try {
				midiplayer = new MidiPlayer(file);

				if (volume > 150)
					volume = 150;
				midiplayer.setVolume((int) Math.round((float) volume * 0.75));

				if (CPlaying.Players.size() > 1) {
					volume = volume * ((float) priority / 10);

					if (CPlaying.globalPlaying && priority != 11)
						volume = volume * (float) 0.5;

					if (priority == 11)
						CPlaying.globalPlaying = true;
				}

				oldvolume = volume;
				midiplayer.play(offset);

				while (CPlaying.Players.containsKey(songString + ":" + number) && midiplayer.isPlaying()) {

					volume = CPlaying.Players.get(songString + ":" + number);

					if (CPlaying.Players.size() > 1) {
						volume = volume * ((float) priority / 10);

						if (CPlaying.globalPlaying && priority != 11)
							volume = volume * (float) 0.5;

						if (priority == 11)
							CPlaying.globalPlaying = true;
					}

					if (volume != oldvolume && volume != 0) {
						if (volume > 150)
							volume = 150;
						midiplayer.setVolume((int) Math.round((float) volume * 0.75));
						oldvolume = volume;

					} else if (volume == 0)
						break;

					try {
						sleep(100);
					} catch (InterruptedException e) {
						if (mainWindow.check_debug.isSelected()) e.printStackTrace();
					}
				}

				midiplayer.stop(); //stop the player
				midiplayer = null;

				if (volume != 0) {
					startNumber++;
					if (startNumber >= songList.length) {
						startNumber = 0;
						if (!useOffset)
							return;
					}

					File nextfile = new File("Music/" + box + "/" + songList[startNumber]);

					if (nextfile.exists() && !file.isDirectory()) {
						createPlayer(nextfile, 0, 0, startNumber);
					} else
						System.out.println("Missing MP3!");
				}
			} catch (Exception e) {
				if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			}
		}

	}

	private String[] getSongOffset() {

		int curFileNumber = 0; // index of song that currently needs to be
								// played in songList array
		long byteOffset = 0; // offset for the song that currently needs to be
								// played in bytes
		long timeOffset = 0; // offset for the song that currently needs to be
								// played in microseconds

		long fileDuration = 0; // duration of a single file
		long timeDiff = CPlaying.serverTime; // the server time
		long entireDuration = 0; // entire duration of all songs in songList
									// played
		long helpDur = 0; // helps to calculate duration
		long resttime = 0; // helps to calculate duration

		for (int r = 0; r < songList.length; r++) {
			if (songList[r] != null) {
				fileDuration = readDuration("Music/" + box + "/" + songList[r]);

				entireDuration = entireDuration + fileDuration;
			}
		} // ==> get entire Duration

		fileDuration = 0;

		if (entireDuration != 0)
			resttime = ((timeDiff * 1000)) % (entireDuration); // position in
																// the entire
																// list of songs

		for (int i = 0; i < songList.length; i++) {

			if (songList[i] != null) {
				fileDuration = readDuration("Music/" + box + "/" + songList[i]);

				helpDur = helpDur + fileDuration; // add the duration of songs

				if (resttime <= helpDur) { // helpDur is after position in the
											// entire songlist --> the position
											// must be in this song
					curFileNumber = i;
					i = songList.length;
					timeOffset = fileDuration - (helpDur - resttime);
				}
			}
		}

		// get Byte offset
		File mp3file = new File("Music/" + box + "/" + songList[curFileNumber]);
		long bytes = mp3file.length();

		if (bytes > 0 && fileDuration > 0 && timeOffset > 0)
			byteOffset = (long) (((((double) bytes) / ((double) fileDuration))) * (double) timeOffset); // bytes/byteOffset
																										// =
																										// fileDuration/timeOffset

		String[] result = { String.valueOf(curFileNumber), String.valueOf(byteOffset), String.valueOf(timeOffset) };

		return result;
	}

	public long readDuration(String filename) { // returns the duration of a mp3
												// or midi file in microseconds
		long length = 0;
		AudioFileFormat baseFileFormat = null;
		String extension = null;

		try {
			File file = new File(filename);

			if (file.exists() && !file.isDirectory()) {
				int dotPos = file.getName().lastIndexOf(".");

				if (dotPos != -1) {
					extension = file.getName().substring(dotPos);

					// Get AudioFileFormat from given file.
					if (extension.equalsIgnoreCase(".mp3")) {
						baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);

					} else if (extension.equalsIgnoreCase(".midi") || extension.equalsIgnoreCase(".mid")) {
						midiplayer = new MidiPlayer(file); // new MidiPlayer
															// sequence
						length = (long) midiplayer.getDuration();// Length in
																	// microseconds
						midiplayer.stop();
						return length;
					}

					if (baseFileFormat instanceof TAudioFileFormat) {
						@SuppressWarnings("rawtypes")
						Map props = ((TAudioFileFormat) baseFileFormat).properties();
						length = (long) Math.round((((Long) props.get("duration")).longValue())); // Length
																									// in
																									// microseconds
					}
				}
			}

		} catch (UnsupportedAudioFileException ex) {
			if (mainWindow.check_debug.isSelected()) ex.printStackTrace();
			System.out.println("Error getting audio format");

		} catch (Exception e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
		}
		return length;
	}
}
