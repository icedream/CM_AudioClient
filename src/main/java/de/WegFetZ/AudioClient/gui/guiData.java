package main.java.de.WegFetZ.AudioClient.gui;

import java.util.HashMap;

public class guiData {
	
	//music tab
	public static long music_totalSize = 0;
	public static int music_mp3Count = 0;
	public static int music_midiCount = 0;
	public static long music_totalSelectedSize = 0;
	public static int music_mp3SelectedCount = 0;
	public static int music_midiSelectedCount = 0;
	public static int music_speed = 0;
        
    public static boolean streamPlaying = false;
    public static boolean dontReport = false;
    
    //webradio tab
    public static String dev_id = "mw1jbuJXp-Qf9qmD";//i censored this. you can get your own shoutcast dev_id on dev.aol.com
    public static HashMap<String, String> ownRadioStreams = new HashMap<String,String>();
    public static HashMap<String, String> radioStreams = new HashMap<String,String>();

}
