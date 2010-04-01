/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;


public interface SoundClient
{

	public void midiInit ( ) throws javax.sound.midi.MidiUnavailableException;
	//public void midiPreload ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;	
	public void midiPreload ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;
	//public void midiStart ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;
	public void midiStart ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;	
	public void midiStart ( ) throws javax.sound.midi.InvalidMidiDataException;
	public void midiLoop ( ) throws javax.sound.midi.InvalidMidiDataException;
	//public void midiOpen ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;
	public void midiOpen ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException;	
	public void midiStop ( );
	public void midiClose ( );
	//public void midiUnload ( java.io.File midfile ) throws java.io.IOException;
	public void midiUnload ( String s ) throws java.io.IOException;
	
	
	public void audioPreload ( String s ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException;
	//public void audioPreload ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException;
	//public void audioUnload ( java.io.File afile ) throws java.io.IOException;		
	public void audioUnload ( String s ) throws java.io.IOException;
	//public void audioStart ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException;
	public void audioStart ( String s ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException;
	public void audioStop ( String s );
	//public void audioStop ( File f );
	
	
	public void stopAllSound();
	
}
