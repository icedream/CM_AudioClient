package main.java.de.WegFetZ.BasicMP3Player;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

public class Mp3Player implements BasicPlayerListener {

	private BasicPlayer player = null;

	private float volume = 0.3F;
	private long curPos = 0;
	

	public Mp3Player() {

		player = new BasicPlayer();
		player.addBasicPlayerListener(this);
		BasicPlayer.debug = mainWindow.check_debug.isSelected();
	}

	public void play(File f) throws BasicPlayerException {
		play(f, 0);
	}

	public void play(File f, long byteOffset) throws BasicPlayerException {

		if (getState() != BasicPlayer.STOPPED)
			stop();
		player.open(f);
		
		setPosition(byteOffset);
		setCurVolume();
		
		player.play();
		
		fadeIn();
	}
	
	public void play(URL url) throws BasicPlayerException {
		if (getState() != BasicPlayer.STOPPED)
			stop();
		player.open(url);
		
		setCurVolume();
		
		player.play();
		
		fadeIn();
	}

	public void play(InputStream stream) throws BasicPlayerException {
		if (getState() != BasicPlayer.STOPPED)
			stop();
		player.open(stream);
		
		setCurVolume();
		
		player.play();
		
		fadeIn();
	}

	public void stop() throws BasicPlayerException {

		if (player != null && player.getStatus() != BasicPlayer.STOPPED) {
			fadeOut();
			player.stop();
		}
	}

	public void setVolume(float volume) throws BasicPlayerException {

		this.volume = volume;
		setCurVolume();
	}

	public void setPosition(long pos) throws BasicPlayerException {

		if (player != null && player.getStatus() != BasicPlayer.STOPPED) {
			player.seek(pos);
		}
	}

	private void setCurVolume() throws BasicPlayerException {

		if (player != null && player.getStatus() != BasicPlayer.STOPPED && player.hasGainControl()) {
			player.setGain(volume);
		}
	}
	
	private void fadeIn() throws BasicPlayerException {
		if (player != null && player.getStatus() != BasicPlayer.STOPPED && player.hasGainControl()) {
			for (float i = 0.0F; i < this.volume; i = i + 0.01F) {
		        try {
					player.setGain(i);			
					Thread.sleep(50);
					
		        } catch (InterruptedException e) {
		        	if (mainWindow.check_debug.isSelected()) e.printStackTrace();
				}
		    }
		}
	}
	
	private void fadeOut() throws BasicPlayerException {
		if (player != null && player.getStatus() != BasicPlayer.STOPPED && player.hasGainControl()) {
			for (double i = this .volume; i > 0.0F; i = i - 0.01F) {
		        try {
					player.setGain(i);			
					Thread.sleep(50);
					
		        } catch (InterruptedException e) {
		        	if (mainWindow.check_debug.isSelected()) e.printStackTrace();
				}
		    }
		}
	}

	public long getPosition() {
		return curPos;
	}

	public int getState() {
		return player.getStatus();
	}

	@SuppressWarnings("rawtypes")
	public void opened(Object arg0, Map arg1) {

	}

    @SuppressWarnings("rawtypes")
	public void progress(int bytesread, long microseconds,
            byte[] pcmdata, Map properties) {
    	
    }

	public void stateUpdated(BasicPlayerEvent event) {
		

	}

	public void setController(BasicController arg0) {

	}
}
