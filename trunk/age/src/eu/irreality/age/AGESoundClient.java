/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import java.util.*;


//for MOD
import micromod.*;
import micromod.resamplers.*;
import micromod.output.*;
import micromod.output.converters.*; 
import javax.sound.sampled.*;

import eu.irreality.age.debug.Debug;


//Viene a ser la API de sonido del AGE.


public class AGESoundClient implements SoundClient
{

	//MIDI
	private javax.sound.midi.Sequencer seqr;
	private javax.sound.midi.Sequence curseq;
	private java.util.Hashtable midiPreloaded = new java.util.Hashtable();
	
	
	//gets a MIDI sequence which has been preloaded.
	//return null if it isn't preloaded.
	private javax.sound.midi.Sequence getPreloadedSequence ( java.io.File f )
	{
		try
		{
			return (javax.sound.midi.Sequence) midiPreloaded.get ( f.getCanonicalPath() );
		}
		catch ( java.io.IOException ioe ) //of getCanonicalPath()
		{
			return null;
		}
	}
	
	//call midiInit before using midiXXX functions. Initializes the sequencer.
	public void midiInit ( ) throws javax.sound.midi.MidiUnavailableException
	{
		if ( seqr == null )
		{
			seqr = javax.sound.midi.MidiSystem.getSequencer();
			seqr.open();
		}
	}
	
	//loads a MIDI file into memory (hashtable) so that you don't have to open it each time you play it.
	public void midiPreload ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence ( midfile );
		midiPreloaded.put ( midfile.getCanonicalPath() , seq );
	}
	
	//unloads a MIDI file from memory. (does nothing if file invalid)
	public void midiUnload ( java.io.File midfile ) throws java.io.IOException
	{
		midiPreloaded.remove ( midfile.getCanonicalPath() );
	}
	
	public void midiUnload ( String s ) throws java.io.IOException
	{
		midiUnload ( new File ( s ) );
	}
	
	//loads a MIDI file (or not if preloaded) and starts playing it. If the manager was playing another file, playback stops.
	public void midiStart ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		//if ( seqr == null ) midiInit();
		curseq = getPreloadedSequence ( midfile );
		if ( curseq == null ) curseq = javax.sound.midi.MidiSystem.getSequence ( midfile );
		//if ( !seqr.isOpen() ) seqr.open();
		if ( seqr.isRunning() ) seqr.stop();
		seqr.setLoopCount ( 0 );
		seqr.setSequence ( curseq );
		seqr.start();
	}
	
	//loads a MIDI file (or not if preloaded) and starts playing it. If the manager was playing another file, playback stops.
	public void midiStart ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		midiStart ( new File ( f ) );
	}
	
	//loads a MIDI file into memory (hashtable) so that you don't have to open it each time you play it.
	public void midiPreload ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		midiPreload ( new File ( f ) );
	}
	
	//sets the current file in the sequencer to the given file.
	public void midiOpen ( java.io.File midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		curseq = getPreloadedSequence ( midfile );
		if ( curseq == null ) curseq = javax.sound.midi.MidiSystem.getSequence ( midfile );
	}
	
	//sets the current file in the sequencer to the given file.
	public void midiOpen ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		midiOpen ( new File ( f ) );
	}	
	
	//starts looping the current file indefinitely.
	public void midiLoop ( ) throws javax.sound.midi.InvalidMidiDataException
	{
		if ( seqr.isRunning() ) seqr.stop();
		seqr.setSequence ( curseq );
		seqr.setLoopCount( javax.sound.midi.Sequencer.LOOP_CONTINUOUSLY );
		seqr.start();
	}
	
	//starts playing the current file. Only works if we did a midiOpen() on the file.
	public void midiStart ( ) throws javax.sound.midi.InvalidMidiDataException
	{
		//if ( seqr == null ) midiInit();
		//if ( !seqr.isOpen() ) seqr.open();
		if ( seqr.isRunning() ) seqr.stop();
		seqr.setSequence ( curseq );
		seqr.setLoopCount ( 0 );
		seqr.start();
	}
	
	//stops playback.
	public void midiStop ( )
	{
		//if ( seqr == null ) midiInit();
		seqr.stop();
	}
	
	//sets the current file in the sequencer to none.
	public void midiClose ( )
	{
		curseq = null;	
		//if ( seqr.isOpen() ) seqr.close();
	}
	
	
	/*end MIDI*/
	
	
	
	
	
	/*begin AUDIO*/
	
	
	
	
	
	
	
	
	
	


/*

		//prueba MIDI
		
		try
		{
			javax.sound.midi.Sequencer seqr = javax.sound.midi.MidiSystem.getSequencer();
			javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence ( new java.io.File ( "prueba.mid" ) );
			seqr.open();
			seqr.setSequence(seq);
			//seqr.setTempoFactor((float)20.0);
			//seqr.start();
		}
		catch ( Exception exc )
		{	
			System.out.println(exc);
		}
		
		
		
		//prueba WAV
		
		try
		{
			javax.sound.sampled.AudioInputStream aii = javax.sound.sampled.AudioSystem.getAudioInputStream ( new java.io.File("prueba.wav") );
			javax.sound.sampled.AudioFormat af = aii.getFormat();
			
			
			//cogemos una l�nea [getline] que soporte el formato [af]
			javax.sound.sampled.Clip cl = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine ( new javax.sound.sampled.DataLine.Info ( javax.sound.sampled.Clip.class , af ) ); 
			
			
			cl.open ( aii );
		}
		catch ( Exception exc )
		{
			System.out.println(exc);
		}

*/

	//AUDIO
	
	//a Clip is a DataLine whose audio gets preloaded.
	
	//javax.sound.midi.Sequencer seqr; <- no hay equivalente, Lines las abrimos ya sabiendo AudioFormat
	//javax.sound.sampled.AudioInputStream curStream;
	private java.util.Hashtable audioPreloaded = new java.util.Hashtable();


	public void audioPreload ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		javax.sound.sampled.AudioInputStream aii = javax.sound.sampled.AudioSystem.getAudioInputStream ( f );
		javax.sound.sampled.AudioFormat af = aii.getFormat();
		
		//get a Clip
		javax.sound.sampled.Clip cl = ( javax.sound.sampled.Clip ) javax.sound.sampled.AudioSystem.getLine ( new javax.sound.sampled.DataLine.Info ( javax.sound.sampled.Clip.class , af ) );

		//load the AudioInputStream gotten from the file into the Clip
		cl.open ( aii );

		//put Clip into hashtable
		audioPreloaded.put ( f.getCanonicalPath() , cl );
		
	}
	
	//unloads an audio clip from memory. (does nothing if file invalid)
	public void audioUnload ( java.io.File afile ) throws java.io.IOException
	{
		audioPreloaded.remove ( afile.getCanonicalPath() );
	}
	
	public void audioUnload ( String s ) throws java.io.IOException
	{
		audioUnload ( new File ( s ) );
	}
	
	public void audioPreload ( String s ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		audioPreload ( new File ( s ) );
	}
	
	private javax.sound.sampled.Clip getPreloadedClip ( java.io.File f )
	{
		try
		{
			return (javax.sound.sampled.Clip) audioPreloaded.get ( f.getCanonicalPath() );
		}
		catch ( java.io.IOException ioe ) //of getCanonicalPath()
		{
			return null;
		}
	}
	
	public void audioStart ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		javax.sound.sampled.Clip cl = getPreloadedClip ( f );
		if ( cl == null )
				audioPreload ( f );
		//cl not null [or exception should have been thrown by audioPreload]
		cl.start();	
	}
	
	public void audioStop ( File f )
	{
		javax.sound.sampled.Clip cl = getPreloadedClip ( f );
		if ( cl == null )
				return;
		//cl not null
		cl.stop();
	}
	
	public void audioStart ( String s ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		audioStart ( new File ( s ) );
	}
	
	public void audioStop ( String s )
	{
		audioStop ( new File ( s ) );
	}
	
		
	
	
//for MOD	
	
	PlayerThread pt;
 
 	//iRepeat: number of times to repeat (or -1 for infinite)
  void playMOD( File f, int iRepeat ) 
    throws Exception
  {
    MODThread mt;
    JavaSoundOutputDevice out =  
      new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
    Module module = ModuleLoader.read(new DataInputStream(new FileInputStream(f)));
    MicroMod microMod = new MicroMod(module, out, new LinearResampler());

    mt = new MODThread(microMod, out, iRepeat);
    pt = mt;
    pt.setVolume(0x10000);


    mt.start();
    return;
  }
  
  void playMOD ( String s , int iRepeat ) throws Exception
  {
  	playMOD ( new File(s) , iRepeat );
  }
  
  public void stopMOD() throws Exception
  {
    if (pt != null)
      pt.stopPlaying();
  }
  
	/*inner*/	  class MODThread extends Thread implements PlayerThread
		{
		   	int soundId;
		    boolean running;
		    boolean stopped;
		    JavaSoundOutputDevice out;
		    MicroMod mm;
			int iRepeat;

		    MODThread(MicroMod mm, JavaSoundOutputDevice out, 
		              int iRepeat)
		    {
		      this.mm = mm;
		      this.out = out;
		      this.iRepeat = iRepeat;

		      running = false;
		      stopped = false;
		    }

		    public synchronized void setVolume(int vol)
		    {
		      Line l = out.getLine();
		      FloatControl ctl = 
		        (FloatControl) l.getControl(FloatControl.Type.MASTER_GAIN);
		      double gain = (double) vol / (double) 0x10000;
		      float dB = (float) (Math.log(gain) / Math.log(10.0) * 20);

		      if (ctl != null)
		        ctl.setValue(dB);
		    }

		    public synchronized void stopPlaying()
		    {
		      	if (!stopped)
		      	{
			        running = false;
			        stopped = true;
			        out.stop();
			        out.close();
			        //donePlaying();
		      	}
		    }
			
			synchronized void donePlaying()
		  	{
		    	pt = null;
		  	}

		    public void run()
		    {
		      	out.start();

		      	for (int i = 0; !stopped && (iRepeat == -1 || i < iRepeat); i++)
		      	{
		       		running = true;
		        	mm.setCurrentPatternPos(0);

		        	while (running && mm.getSequenceLoopCount() == 0)
		        	{     
		          		synchronized (this)
		          		{
							Debug.println("Real Time.");
		            		mm.doRealTimePlayback();
		            		try 
							{
		              			Thread.sleep(20);
		            		} 
							catch (InterruptedException e) {}
		          		}
		        	}
		      	}

		      	synchronized (this)
		      	{
		        	if (!stopped)
		        	{        
		          		running = false;
		          		out.stop();
		          		out.close();
		          		donePlaying();
		        	}

					/*
			        if (iNotify != 0)
			        {
			        	Glk.GlkEvent e = new Glk.GlkEvent();
			          	e.type = Glk.EVTYPE_SOUND_NOTIFY;
			          	e.win = null;
			          	e.val1 = soundId;
			          	e.val2 = iNotify;
			          
			          	Glk.addEvent(e);
			        }
					*/
		      	}
		    }
		}
	
	
	
	
	
	

}

//for MOD
	
interface PlayerThread extends Runnable
{
   	public abstract void setVolume(int vol);
   	public abstract void stopPlaying() throws Exception;
} 
