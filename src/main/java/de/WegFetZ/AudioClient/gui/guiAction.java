package main.java.de.WegFetZ.AudioClient.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import main.java.de.WegFetZ.AudioClient.Client;
import main.java.de.WegFetZ.AudioClient.LoadSettings;
import main.java.de.WegFetZ.AudioClient.otherFuncs;
import main.java.de.WegFetZ.AudioClient.Utils.FileUtil;
import main.java.de.WegFetZ.AudioClient.Utils.StringUtil;
import main.java.de.WegFetZ.BasicMP3Player.Mp3Player;

public class guiAction extends Thread {

	private String event;

	public guiAction(String event) {
		super("guiAction");
		this.event = event;
	}

	public void run() {
		if (event.equalsIgnoreCase("formClose"))
			formClose();
		else if (event.equalsIgnoreCase("list_musicSelected"))
			list_musicSelected();
		else if (event.equalsIgnoreCase("tabbedPanel"))
			tabbedPanel();
		else if (event.equalsIgnoreCase("tf_upConSpeed"))
			combo_connection();
		else if (event.equalsIgnoreCase("b_connect"))
			b_connect();
		else if (event.equalsIgnoreCase("combo_connection"))
			combo_connection();
		else if (event.equalsIgnoreCase("b_save"))
			b_save();
		else if (event.equalsIgnoreCase("check_debug"))
			check_debug();
		else if (event.equalsIgnoreCase("b_refreshMusicList"))
			b_refreshMusicList();
		else if (event.equalsIgnoreCase("synchronize"))
			synchronize();
		else if (event.equalsIgnoreCase("b_deleteMusic"))
			b_deleteMusic();
		else if (event.equalsIgnoreCase("b_addMusic"))
			b_addMusic();
		else if (event.equalsIgnoreCase("b_addRadio"))
			b_addRadio();
		else if (event.equalsIgnoreCase("b_deleteRadio"))
			b_deleteRadio();
		else if (event.equalsIgnoreCase("b_playOwnRadio"))
			b_playOwnRadio();
		else if (event.equalsIgnoreCase("b_refreshStreamList"))
			b_refreshStreamList();
		else if (event.equalsIgnoreCase("b_searchShoutcast"))
			b_searchShoutcast();
		else if (event.equalsIgnoreCase("b_downloadStream"))
			b_downloadStream();
	}

	private void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	private void tabbedPanel() {
		JTabbedPane tabPane = mainWindow.tabbedPanel;

		int index = tabPane.getSelectedIndex();

		if (index != -1) {
			if (index == 1)
				b_refreshMusicList();
			else if (index == 2) {
				b_refreshOwnRadioList();
				if (mainWindow.list_shoutcastRadioModel.getSize() < 1)
					b_refreshStreamList();
			}
		}

	}

	public static void formClose() {

		if (confirmClose("quit")) {

			if (mainWindow.check_saveOnQuit.isSelected()) {
				if (mainWindow.tf_name.getText().length() > 0 && mainWindow.tf_ip.getText().length() > 0 && mainWindow.tf_cmPort.getText().length() > 0)
					LoadSettings.saveMain();
				else
					System.out.println("Couldn't save settings: no name, ip or port entered!");
			}

			if (mainWindow.b_connect.getText().equalsIgnoreCase("Disconnect")) {
				System.out.println("Closing connections. Audioclient will close in a few seconds...");
				Client.noReconnect = true;
				Client.closeCons();
			}

			System.out.println("Bye");
			System.exit(0);
		}
	}

	public static void b_connect() {

		if (mainWindow.b_connect.getText() == "Connect") {

			if (mainWindow.tf_name.getText().length() > 0 && mainWindow.tf_ip.getText().length() > 0 && mainWindow.tf_cmPort.getText().length() > 0) {

				System.out.println();
				Client.exit = false;
				mainWindow.b_connect.setText("Disconnect");
				Client.connect(mainWindow.tf_name.getText().toLowerCase(), mainWindow.tf_ip.getText(), mainWindow.tf_cmPort.getText());

			} else
				System.out.println("You need to enter a name, ip and port first!");
		} else {
			if (confirmClose("disconnect")) {
				Client.noReconnect = true;
				Client.closeCons();
				mainWindow.b_connect.setText("Connect");
			}
		}
	}

	private static boolean confirmClose(String type) {

		int n = JOptionPane.showOptionDialog(null, "This will cancel all up- and downloads.\n" + "Canceled uploads and downloads \n" + "must be restarted from the beginning.\n ", "Are you sure you want to " + type + "?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (n == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}

	// synchronizes the songlist with the server
	public void synchronize() {
		if (mainWindow.b_connect.getText().equals("Disconnect"))
			otherFuncs.upload();
	}

	public static void b_save() {
		if (mainWindow.tf_name.getText().length() > 0 && mainWindow.tf_ip.getText().length() > 0 && mainWindow.tf_cmPort.getText().length() > 0)
			LoadSettings.saveMain();
		else
			System.out.println("You need to enter a name, ip and port first!");
	}

	public static void check_debug() {
		BasicPlayer.debug = mainWindow.check_debug.isSelected();
	}

	private void b_refreshMusicList() {

		String cuttedSizeMB = "-";

		int seconds = 0;
		int minutes = 0;
		int hours = 0;

		guiData.music_midiCount = 0;
		guiData.music_mp3Count = 0;
		guiData.music_totalSize = 0;

		String playername = mainWindow.tf_name.getText().toLowerCase();

		DefaultListModel listModel = mainWindow.list_musicModel;

		listModel.removeAllElements();

		if (playername == null || playername.length() < 1) {
			listModel.addElement("Cannot load music list: no name entered in general tab.");
			return;
		}

		String[] fileList = FileUtil.listOnlyFiles("Music/craftplayer{name=" + playername + "}/");// get
																									// a
																									// list
																									// of
																									// files
																									// without
																									// directory
		if (fileList != null && fileList.length > 0) {

			for (int i = 0; i < fileList.length; i++) {
				File tempFile = new File("Music/craftplayer{name=" + playername + "}/" + fileList[i]);

				if (tempFile.exists()) {

					String sizeMB = String.valueOf((float) tempFile.length() / 1048576);
					cuttedSizeMB = sizeMB + " MB";
					int dotpos = sizeMB.lastIndexOf(".");
					if (dotpos != -1 && dotpos + 3 < sizeMB.length())
						cuttedSizeMB = sizeMB.substring(0, dotpos + 3) + " MB";// get
																				// size
																				// in
																				// mb

					listModel.addElement(cuttedSizeMB + " | " + tempFile.getName());// add
																					// element
																					// to
																					// list
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {}

					String ext = FileUtil.getExtension(tempFile); // update mp3/
																	// midi
																	// count

					if (ext.equalsIgnoreCase(".mp3"))
						guiData.music_mp3Count++;
					else if (ext.equalsIgnoreCase(".midi") || ext.equalsIgnoreCase(".mid"))
						guiData.music_midiCount++;

					guiData.music_totalSize = guiData.music_totalSize + tempFile.length(); // update
																							// total
																							// size
				} else
					System.out.println("error");
			}

			// get total size in mb
			String sizeStringMB = String.valueOf((float) guiData.music_totalSize / 1048576);
			cuttedSizeMB = sizeStringMB + " MB";

			int dotpos = sizeStringMB.lastIndexOf(".");
			if (dotpos != -1 && dotpos + 3 < sizeStringMB.length())
				cuttedSizeMB = sizeStringMB.substring(0, dotpos + 3) + " MB";

			// get internet upload speed:
			String connection = mainWindow.combo_connection.getSelectedItem().toString();
			int speed = 0;

			if (connection.equalsIgnoreCase("Use own:")) {
				try {
					speed = Integer.parseInt(mainWindow.tf_upConSpeed.getText());
				} catch (NumberFormatException e) {
				} catch (NullPointerException e) {
				}
			} else if (connection.equalsIgnoreCase("DSL 384"))
				speed = 8;
			else if (connection.equalsIgnoreCase("DSL 1000"))
				speed = 16;
			else if (connection.equalsIgnoreCase("DSL 2000"))
				speed = 24;
			else if (connection.equalsIgnoreCase("DSL 3000"))
				speed = 40;
			else if (connection.equalsIgnoreCase("DSL 6000"))
				speed = 80;
			else if (connection.equalsIgnoreCase("DSL 16000"))
				speed = 128;
			else if (connection.equalsIgnoreCase("VDSL 25"))
				speed = 625;
			else if (connection.equalsIgnoreCase("VDSL 50"))
				speed = 1250;

			if (speed != 0) {
				// calculate upload time
				long totalSizeKB = Math.round(guiData.music_totalSize / 1024);
				float totalsecs = (float) totalSizeKB / speed;
				float totalmins = (float) totalsecs / 60;
				seconds = (int) totalsecs % 60;
				minutes = (int) totalmins % 60;
				hours = (int) (totalmins / 60);
			}

		} else
			listModel.addElement("No music found for " + playername + ".");

		// set the text of the labels
		mainWindow.l_fileCount.setText(String.valueOf(guiData.music_mp3Count + guiData.music_midiCount));
		mainWindow.l_mp3Count.setText(String.valueOf(guiData.music_mp3Count));
		mainWindow.l_midiCount.setText(String.valueOf(guiData.music_midiCount));
		mainWindow.l_size.setText(cuttedSizeMB);
		mainWindow.l_estTime.setText(String.valueOf(hours + "h : " + minutes + "m : " + seconds + "s"));

	}

	public void combo_connection() {

		int seconds = 0;
		int minutes = 0;
		int hours = 0;

		// get internet upload speed:
		String connection = mainWindow.combo_connection.getSelectedItem().toString();

		if (connection.equalsIgnoreCase("Use own:")) {
			if (!mainWindow.tf_upConSpeed.isEditable())
				mainWindow.tf_upConSpeed.setEditable(true);
			try {
				guiData.music_speed = Integer.parseInt(mainWindow.tf_upConSpeed.getText());
			} catch (NumberFormatException e) {
			} catch (NullPointerException e) {
			}
		} else {
			if (mainWindow.tf_upConSpeed.isEditable())
				mainWindow.tf_upConSpeed.setEditable(false);

			if (connection.equalsIgnoreCase("DSL 384"))
				guiData.music_speed = 8;
			else if (connection.equalsIgnoreCase("DSL 1000"))
				guiData.music_speed = 16;
			else if (connection.equalsIgnoreCase("DSL 2000"))
				guiData.music_speed = 24;
			else if (connection.equalsIgnoreCase("DSL 3000"))
				guiData.music_speed = 40;
			else if (connection.equalsIgnoreCase("DSL 6000"))
				guiData.music_speed = 80;
			else if (connection.equalsIgnoreCase("DSL 16000"))
				guiData.music_speed = 128;
			else if (connection.equalsIgnoreCase("VDSL 25"))
				guiData.music_speed = 625;
			else if (connection.equalsIgnoreCase("VDSL 50"))
				guiData.music_speed = 1250;

			mainWindow.tf_upConSpeed.setText(String.valueOf(guiData.music_speed));
		}

		long totalSize = 0;
		if (mainWindow.list_music.getSelectedIndices() != null || mainWindow.list_music.getSelectedIndices().length < 1)
			totalSize = guiData.music_totalSelectedSize;
		else
			totalSize = guiData.music_totalSize;

		// calculate upload time
		if (guiData.music_speed != 0) {
			long totalSizeKB = Math.round(totalSize / 1024);
			float totalsecs = (float) totalSizeKB / guiData.music_speed;
			float totalmins = (float) totalsecs / 60;
			seconds = (int) totalsecs % 60;
			minutes = (int) totalmins % 60;
			hours = (int) (totalmins / 60);
		}

		// write to label
		mainWindow.l_estTime.setText(String.valueOf(hours + "h : " + minutes + "m : " + seconds + "s"));
	}

	public void list_musicSelected() {

		String cuttedSizeMB = "-";
		boolean noSelection = false;
		int number = 0;

		int seconds = 0;
		int minutes = 0;
		int hours = 0;
		int loops = 0;

		guiData.music_midiSelectedCount = 0;
		guiData.music_mp3SelectedCount = 0;
		guiData.music_totalSelectedSize = 0;

		JList list = mainWindow.list_music;
		DefaultListModel listModel = mainWindow.list_musicModel;

		String playername = mainWindow.tf_name.getText().toLowerCase();

		if (playername == null || playername.length() < 1)
			return;

		int[] index = list.getSelectedIndices(); // get all selected elements

		if (index == null || index.length < 1) {

			noSelection = true; // calculate with all files
			loops = listModel.getSize();
		} else
			loops = index.length;

		for (int i = 0; i < loops; i++) {

			if (noSelection)
				number = i;
			else
				number = index[i];

			if (number < listModel.getSize()) {

				String element = (String) listModel.get(number);// get the
																// element
																// text

				String[] split = element.split(" \\| "); // name of the file
				// without
				// "size | "

				if (split != null && split.length > 1) {

					File file = new File("Music/craftplayer{name=" + playername + "}/" + split[1]);

					if (file.exists()) {
						// add file size to total size
						guiData.music_totalSelectedSize = guiData.music_totalSelectedSize + file.length();

						String ext = FileUtil.getExtension(file); // update mp3/
						// midi
						// count

						if (ext.equalsIgnoreCase(".mp3"))
							guiData.music_mp3SelectedCount++;
						else if (ext.equalsIgnoreCase(".midi") || ext.equalsIgnoreCase(".mid"))
							guiData.music_midiSelectedCount++;
					}
				}
			}

		}

		// get total size in mb
		String sizeStringMB = String.valueOf((float) guiData.music_totalSelectedSize / 1048576);
		cuttedSizeMB = sizeStringMB + " MB";

		int dotpos = sizeStringMB.lastIndexOf(".");
		if (dotpos != -1 && dotpos + 3 < sizeStringMB.length())
			cuttedSizeMB = sizeStringMB.substring(0, dotpos + 3) + " MB";

		// calculate upload time
		if (guiData.music_speed != 0) {
			long totalSizeKB = Math.round(guiData.music_totalSelectedSize / 1024);
			float totalsecs = (float) totalSizeKB / guiData.music_speed;
			float totalmins = (float) totalsecs / 60;
			seconds = (int) totalsecs % 60;
			minutes = (int) totalmins % 60;
			hours = (int) (totalmins / 60);
		}

		// write information to labels
		mainWindow.l_fileCount.setText(String.valueOf(guiData.music_mp3SelectedCount + guiData.music_midiSelectedCount));
		mainWindow.l_mp3Count.setText(String.valueOf(guiData.music_mp3SelectedCount));
		mainWindow.l_midiCount.setText(String.valueOf(guiData.music_midiSelectedCount));
		mainWindow.l_size.setText(cuttedSizeMB);
		mainWindow.l_estTime.setText(String.valueOf(hours + "h : " + minutes + "m : " + seconds + "s"));
	}

	public void b_addMusic() {
		String playername = mainWindow.tf_name.getText().toLowerCase();
		if (playername == null || playername.length() < 1) {
			showMessage("You need to enter a name in the 'General' tab, before you can add songs.");
			return;
		}
		
		new File("Music/craftplayer{name=" + playername + "}/webradio/").mkdirs();

		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter fileFilter = new SimpleFileFilter("mp3,midi,mid");

		fc.setFileFilter(fileFilter);

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File[] file = fc.getSelectedFiles(); // get the selected files

			if (file == null)
				return;

			for (int i = 0; i < file.length; i++) { // for each selected file

				if (file[i].exists()) {

					String ext = FileUtil.getExtension(file[i]); // get
																	// extension

					if (ext.equalsIgnoreCase(".mp3") || ext.equalsIgnoreCase(".midi") || ext.equalsIgnoreCase(".mid")) {

						mainWindow.list_musicModel.addElement("Copying " + file[i].getName() + " to music directory..."); // add
																															// to
																															// music
																															// list

						if (!FileUtil.copyFile(file[i], new File("Music/craftplayer{name=" + playername + "}/" + file[i].getName()), false)) // copy
																																		// the
																																		// file
							showMessage("Cannot add file to the music list.");
					} else
						showMessage("You cannot add other files than .mp3 or .midi to the music list.");

				} else
					showMessage("File not found: " + file[i].toString());
			}

			b_refreshMusicList();
		}
	}

	public void b_deleteMusic() {
		JList list = mainWindow.list_music;
		DefaultListModel listModel = mainWindow.list_musicModel;

		String playername = mainWindow.tf_name.getText().toLowerCase();

		if (playername == null || playername.length() < 1)
			return;

		int[] index = list.getSelectedIndices(); // get all selected elements

		if (index != null) {

			for (int i = 0; i < index.length; i++) {

				String element = (String) listModel.get(index[i]);// add element
																	// to
																	// array
				String[] split = element.split(" \\| "); // name of the file
															// without
															// "size | "

				if (split != null && split.length > 1) {

					File file = new File("Music/craftplayer{name=" + playername + "}/" + split[1]);

					if (file.exists())// delete the file
						file.delete();
				}
			}

			b_refreshMusicList(); // refresh the list
		}
	}

	private void b_refreshOwnRadioList() {

		String playername = mainWindow.tf_name.getText().toLowerCase();

		DefaultListModel listModel = mainWindow.list_ownRadioModel;

		listModel.removeAllElements();
		guiData.ownRadioStreams.clear();

		if (playername == null || playername.length() < 1) {
			listModel.addElement("Cannot load webradio station list: no name entered in general tab.");
			return;
		}

		String[] fileList = FileUtil.listOnlyFiles("Music/craftplayer{name=" + playername + "}/webradio/");// get
		// a
		// list
		// of
		// files
		// without
		// directory
		if (fileList != null && fileList.length > 0) {

			for (int i = 0; i < fileList.length; i++) {
				File tempFile = new File("Music/craftplayer{name=" + playername + "}/webradio/" + fileList[i]);

				if (tempFile.exists()) {

					
					String title = tempFile.getName();
					
					if (FileUtil.getExtension(tempFile).equalsIgnoreCase(".pls")) //read the title from pls file
						title = FileUtil.getPlsTitle(tempFile);
					
						listModel.addElement(title);// add element to
																// list
						guiData.ownRadioStreams.put(title, tempFile.getName());
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {}

				} else
					System.out.println("error");
			}

		} else
			listModel.addElement("No webradio stations found for " + playername + ".");
	}

	public void b_addRadio() {
		String playername = mainWindow.tf_name.getText().toLowerCase();
		if (playername == null || playername.length() < 1) {
			showMessage("You need to enter a name in the 'General' tab, before you can add webradio stations.");
			return;
		}

		new File("Music/craftplayer{name=" + playername + "}/webradio/").mkdirs();
		
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter fileFilter = new SimpleFileFilter("asx,pls,ram");

		fc.setFileFilter(fileFilter);

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File[] file = fc.getSelectedFiles(); // get the selected files

			if (file == null)
				return;

			for (int i = 0; i < file.length; i++) { // for each selected file

				if (file[i].exists()) {

					String ext = FileUtil.getExtension(file[i]); // get
																	// extension

					if (ext.equalsIgnoreCase(".asx") || ext.equalsIgnoreCase(".pls") || ext.equalsIgnoreCase(".ram")) {

						mainWindow.list_ownRadioModel.addElement("Copying " + file[i].getName() + " to webradio directory..."); // add
						// to
						// music
						// list

						if (!FileUtil.copyFile(file[i], new File("Music/craftplayer{name=" + playername + "}/webradio/" + file[i].getName()), false)) // copy
							// the
							// file
							showMessage("Cannot add file to the webradio list.");
					} else
						showMessage("You cannot add other files than .asx, .pls or .ram to the webradio list.");

				} else
					showMessage("File not found: " + file[i].toString());
			}

			b_refreshOwnRadioList();
		}
	}

	public void b_deleteRadio() {
		JList list = mainWindow.list_ownRadio;
		DefaultListModel listModel = mainWindow.list_ownRadioModel;

		String playername = mainWindow.tf_name.getText().toLowerCase();

		if (playername == null || playername.length() < 1)
			return;

		int[] index = list.getSelectedIndices(); // get all selected elements

		if (index != null) {

			for (int i = 0; i < index.length; i++) {

				String element = (String) listModel.get(index[i]);//title of the element
				
				File file = new File("Music/craftplayer{name=" + playername + "}/webradio/" + guiData.ownRadioStreams.get(element));//path to the filee

				if (file.exists())// delete the file
					file.delete();
			}

			b_refreshOwnRadioList(); // refresh the list
		}
	}

	public void b_playOwnRadio() {
		if (guiData.streamPlaying) {
			guiData.streamPlaying = false; // stop playback if already playing
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			;
		}

		if (mainWindow.b_playOwnRadio.getText().equalsIgnoreCase("Stop")) {
			mainWindow.b_playOwnRadio.setText("Play");
			return;
		}

		int index = mainWindow.list_ownRadio.getSelectedIndex(); // get selected
																	// item
		if (index != -1) {
			String playername = mainWindow.tf_name.getText().toLowerCase();

			if (playername != null && playername.length() > 0) {

				String element = (String) mainWindow.list_ownRadioModel.get(index);
				//title of the file to play

				File playFile = new File("Music/craftplayer{name=" + playername + "}/webradio/" + guiData.ownRadioStreams.get(element));//full path to the file

				if (playFile.exists()) {
					guiData.streamPlaying = true;
					mainWindow.b_playOwnRadio.setText("Stop");

					List<URL> urlList = new ArrayList<URL>(); // list to collect
																// urls

					try {
						FileReader fr = new FileReader(playFile);
						LineNumberReader ln = new LineNumberReader(fr);

						while (true) { // read file for stream url
							String line = ln.readLine();

							if (line != null) {
								URL tempURL = StringUtil.urlFromString(line, FileUtil.getExtension(playFile));
								if (tempURL != null)
									urlList.add(tempURL); // url found: add to
															// list
							} else
								break;// end of file
						}
						ln.close();

						URL[] url = new URL[urlList.size()];
						url = (URL[]) urlList.toArray(url); // put urls into an
															// array

						if (url != null && url.length > 0) {

							guiData.dontReport = false;
							int i = 0;

							while (guiData.streamPlaying) { // still have to
															// play the stream
								if (!createRadioPlayer(url[i])) {
									i++;

									if (i >= url.length) {// tried every url
										if (!guiData.dontReport) { // don't spam
																	// the
																	// console
											showMessage("Couldn't get a working URL from " + playFile.getName());
											guiData.dontReport = true;
										}
										break;
									}
								}

								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
								}

							}

						} else
							showMessage("Cannot play stream: " + playFile.getName());

					} catch (IOException e) {
						if (mainWindow.check_debug.isSelected())
							e.printStackTrace();
					}

				} else
					showMessage("File not found: " + element);
			} else
				showMessage("You need to enter a name in the 'General' tab first.");
		}

	}

	private boolean createRadioPlayer(URL url) {

		try {
			Mp3Player player = new Mp3Player();

			player.play(url); // start playing at position

			while (guiData.streamPlaying && player.getState() == 0) {

				try {
					sleep(100);
				} catch (InterruptedException e) {
					if (mainWindow.check_debug.isSelected())
						e.printStackTrace();
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

	private void b_refreshStreamList() {

		DefaultListModel lm = mainWindow.list_shoutcastRadioModel;

		lm.removeAllElements(); // clear the list
		guiData.radioStreams.clear();

		String url = "http://api.shoutcast.com/legacy/Top500?k=" + guiData.dev_id + "&limit=100";

		try {

			lm.addElement("Fetching SHOUTcast.com Top 100...");

			Document doc = Jsoup.connect(url).get(); // get the source from
														// shoutcast.com
			Elements links = doc.select("station"); // search for elements with
													// '<a'

			for (Element link : links) {
				if (link.toString().contains("station")) { // link to stream
					String name = link.attr("name"); // name of stream
					if (name.length() > 33) {
						lm.addElement(name.substring(0, (name.length() - 33))); // add
																				// station
																				// to
																				// the
																				// list
						guiData.radioStreams.put(name.substring(0, (name.length() - 33)), link.attr("id"));// put
						// name
						// and
						// link
						// into
						// hashmap
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {}
					}
				}
			}

			lm.removeElementAt(0); // Fetching... element

		} catch (IOException e) {
			lm.addElement("Cannot load the webradio list from shoutcast.com");
		}
	}

	private void b_searchShoutcast() {

		DefaultListModel lm = mainWindow.list_shoutcastRadioModel;

		String searchString = mainWindow.tf_searchShoutcast.getText();

		if (searchString == null || searchString.length() < 1)
			return;

		lm.removeAllElements(); // clear the list
		guiData.radioStreams.clear();

		String formattedSearchString = searchString.replace(" ", "+");// replaye
																		// spaces
																		// with
																		// +

		String url = "http://api.shoutcast.com/legacy/stationsearch?k=" + guiData.dev_id + "&search=" + formattedSearchString + "&limit=50";

		try {

			lm.addElement("Searching for " + searchString);

			Document doc = Jsoup.connect(url).get(); // get the source from
														// shoutcast.com
			Elements links = doc.select("station"); // search for elements with
													// '<a'

			for (Element link : links) {
				if (link.toString().contains("station")) { // link to stream
					String name = link.attr("name"); // name of stream
					if (name.length() > 33) {
						lm.addElement(name.substring(0, (name.length() - 33))); // add
																				// station
																				// to
																				// the
																				// list
						guiData.radioStreams.put(name.substring(0, (name.length() - 33)), link.attr("id"));// put
						// name
						// and
						// link
						// into
						// hashmap
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {
						}
					}
				}
			}

			lm.removeElementAt(0); // Fetching... element

			if (lm.getSize() < 1)
				lm.addElement("No matches for " + searchString);

		} catch (IOException e) {
			lm.addElement("Cannot load the webradio list from shoutcast.com");
		}
	}

	private void b_downloadStream() {

		int[] index = mainWindow.list_shoutcastRadio.getSelectedIndices(); // get
																			// selected
																			// item
		if (index != null && index.length > 0) {

			String playername = mainWindow.tf_name.getText().toLowerCase();

			if (playername != null && playername.length() > 0) {

				for (int i = 0; i < index.length; i++) {

					String element = (String) mainWindow.list_shoutcastRadioModel.get(index[i]);
					// get the name of the stream to play

					String id = guiData.radioStreams.get(element.toString());

					if (id == null) {
						showMessage("Cannot download " + element + "\n ID not found.");
						return;
					}

					File file = new File("Music/craftplayer{name=" + playername + "}/webradio/" + id + ".pls");
					// create the File

					URL url;

					try {
						url = new URL("http://yp.shoutcast.com/sbin/tunein-station.pls?id=" + id);
					} catch (MalformedURLException e) {
						showMessage("Cannot download " + element + "\n Couldn't load pls.");
						if (mainWindow.check_debug.isSelected())
							e.printStackTrace();
						return;
					}

					try {
						mainWindow.list_ownRadioModel.addElement("Downloading " + element + "...");
						
						String plsString = FileUtil.getTextContent(url);

						if (file.exists())
							file.delete();

						PrintWriter pw = new PrintWriter(new FileWriter(file, true));

						pw.print(plsString); // write the plsString to the new
												// file
						pw.close();

					} catch (IOException e) {
						file.delete();
						showMessage("Unknown error while writing the file.");
						if (mainWindow.check_debug.isSelected())
							e.printStackTrace();
					}
					b_refreshOwnRadioList(); // refresh the list
				}

			} else
				showMessage("You need to enter a name in the 'General' tab first.");
		}

	}

}
