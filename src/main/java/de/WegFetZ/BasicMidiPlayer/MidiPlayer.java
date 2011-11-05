package main.java.de.WegFetZ.BasicMidiPlayer;

/*
 * Copyright (c) 2004 David Flanagan.  All rights reserved.
 * This code is from the book Java Examples in a Nutshell, 3nd Edition.
 * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
 * You may study, use, and modify it for any non-commercial purpose,
 * including teaching and use in open-source projects.
 * You may distribute it non-commercially as long as you retain this notice.
 * For a commercial use license, or to purchase the book, 
 * please visit http://www.davidflanagan.com/javaexamples3.
 */

import java.io.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;

import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

/**
 * This class is a Swing component that can load and play a sound clip,
 * displaying progress and controls. The main() method is a test program. This
 * component can play sampled audio or MIDI files, but handles them differently.
 * For sampled audio, time is reported in microseconds, tracked in milliseconds
 * and displayed in seconds and tenths of seconds. For midi files time is
 * reported, tracked, and displayed in MIDI "ticks". This program does no
 * transcoding, so it can only play sound files that use the PCM encoding.
 */
public class MidiPlayer implements MetaEventListener {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	private Sequence sequence = null; // The contents of a MIDI file
	private Sequencer sequencer = null; // We play MIDI Sequences with a Sequencer
	private Receiver receiver = null;
	private boolean playing = false; // whether the sound is current playing

	// Length and position of the sound are measured in milliseconds for
	// sampled sounds and MIDI "ticks" for MIDI sounds
	private long audioLength = 0; // Length of the sound.
	private long audioDuration = 0; // Duration of song (in microseconds)

	// Create an SoundPlayer component for the specified file.
	public MidiPlayer(File f) throws IOException, UnsupportedAudioFileException, LineUnavailableException, MidiUnavailableException, InvalidMidiDataException {

		// First, get a Sequencer to play sequences of MIDI events
		// That is, to send events to a Synthesizer at the right time.
		sequencer = MidiSystem.getSequencer(false); // Used to play sequences
		receiver = MidiSystem.getReceiver(); // get the receiver
		sequencer.addMetaEventListener(this); // register the event listener
		sequencer.open(); // Turn it on.
		sequencer.getTransmitter().setReceiver(receiver); // wire it up with the
															// receiver

		// Read the sequence from the file and tell the sequencer about it
		sequence = MidiSystem.getSequence(f);
		sequencer.setSequence(sequence);
		audioLength = (long) sequence.getTickLength(); // Get sequence length
		audioDuration = (long) sequencer.getMicrosecondLength(); // Get duration
																	// in
																	// microseconds
	}

	/** Stop playing the sound */
	public void play(long offset) {
		setPosition(offset);
		sequencer.start();
		playing = true;
	}

	/** Stop playing the sound */
	public void stop() {
		if (sequencer != null && sequencer.isOpen())
			sequencer.close();
		playing = false;
		sequencer.setTickPosition(0);
	}

	// MetaEventListener role
	public void meta(MetaMessage event) {
		if (event.getType() == 47) { // end of stream
			stop();
		}
	}

	/** Skip to the specified position */
	public void setPosition(long offset) {
		long position = (long) (audioLength * offset) / audioDuration;
		if (position < 0 || position > audioLength)
			return;
		sequencer.setTickPosition(position);
	}

	/** Set the volume */
	public void setVolume(int volume) {
		if (sequencer != null && sequencer.isOpen()) {
			ShortMessage volMessage = new ShortMessage();
			for (int i = 0; i < 16; i++) {
				try {
					volMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, volume);
				} catch (InvalidMidiDataException e) {
					if (mainWindow.check_debug.isSelected()) e.printStackTrace();
				}
				receiver.send(volMessage, -1);
			}
		}
	}

	/** Return the duration in microseconds */
	public long getDuration() {
		return audioDuration;
	}

	/** Return the state of the player */
	public boolean isPlaying() {
		return playing;
	}

}