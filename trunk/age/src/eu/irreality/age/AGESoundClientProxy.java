/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.net.*;
import java.io.*;
import java.util.*;

import javax.sound.midi.InvalidMidiDataException;

public class AGESoundClientProxy implements ARSPConstants , SoundClient
{

	Socket sock;
	BufferedReader br;
	PrintWriter pw;
	InputStream is;
	OutputStream os;

	public AGESoundClientProxy ( java.net.Socket s )
	{
		this.sock = s;
		try 
		{
      		if (s != null) 
			{
        		br = new BufferedReader(new InputStreamReader((is=new BufferedInputStream(sock.getInputStream(),100000))));         
        		pw = new PrintWriter(new OutputStreamWriter((os=new BufferedOutputStream(sock.getOutputStream(),100000))));         
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
    	}		
	}

	public void midiInit()
	{
		pw.println(MIDI_INIT);
		pw.flush();
	}
	public void midiPreload ( String f )
	{
		pw.println(MIDI_PRELOAD + " " + f);
		pw.flush();
	}
	public void midiStart ( String f )
	{
		pw.println(MIDI_START + " " + f);
		pw.flush();
	}
	public void midiOpen ( String f )
	{
		pw.println(MIDI_OPEN + " " + f);
		pw.flush();
	}
	public void midiStop ( )
	{
		pw.println(MIDI_STOP);
		pw.flush();
	}
	public void midiClose ( )
	{
		pw.println(MIDI_CLOSE);
		pw.flush();
	}
	public void midiStart ( )
	{
		pw.println(MIDI_START);
		pw.flush();
	}
	public void midiUnload ( String f )
	{
		pw.println(MIDI_UNLOAD + " " + f);
		pw.flush();
	}
	
	
	public void audioPreload ( String f )
	{
		pw.println(AUDIO_PRELOAD + " " + f );
		pw.flush();
	}
	public void audioUnload ( String f )
	{
		pw.println(AUDIO_UNLOAD + " " + f );
		pw.flush();
	}
	public void audioStart ( String f )
	{
		pw.println(AUDIO_START + " " + f );
		pw.flush();
	}
	public void audioStart ( String f , int loopTimes )
	{
		pw.println(AUDIO_START + loopTimes + " " + f );
		pw.flush();
	}
	public void audioStop ( String f )
	{
		pw.println(AUDIO_STOP + " " + f );
		pw.flush();
	}
	public void midiLoop() throws InvalidMidiDataException 
	{
		pw.println(MIDI_START);
		pw.flush();
	}
	public void midiLoop(int times) throws InvalidMidiDataException 
	{
		pw.println(MIDI_START);
		pw.flush();
	}
	
	
	public void stopAllSound()
	{
		pw.println(STOP_ALL_SOUND);
		pw.flush();
	}
	

}
