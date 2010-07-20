/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;


//for MOD
import micromod.*;
import micromod.resamplers.*;
import micromod.output.*;
import micromod.output.converters.*; 
import javax.sound.sampled.*;


import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import eu.irreality.age.debug.Debug;


//Viene a ser la API de sonido del AGE.


public class AGESoundClient implements SoundClient
{

    //MIDI
    private javax.sound.midi.Sequencer seqr;
    private javax.sound.midi.Sequence curseq;
    private java.util.Hashtable midiPreloaded = new java.util.Hashtable();

    private boolean on = true;


    public boolean isOn()
    {
	return on;
    }

    public void activate() { on = true; }
    public void deactivate() { stopAllSound(); on = false; }

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
	if ( !isOn() ) return;
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
	if ( !isOn() ) return;
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


    public void midiLoop ( int loopCount ) throws javax.sound.midi.InvalidMidiDataException
    {
	if ( !isOn() ) return;
	if ( seqr.isRunning() ) seqr.stop();
	seqr.setSequence ( curseq );
	seqr.setLoopCount( loopCount );
	seqr.start();
    }

    //starts looping the current file indefinitely.
    public void midiLoop ( ) throws javax.sound.midi.InvalidMidiDataException
    {
	if ( !isOn() ) return;
	midiLoop ( javax.sound.midi.Sequencer.LOOP_CONTINUOUSLY );
    }

    //starts playing the current file. Only works if we did a midiOpen() on the file.
    public void midiStart ( ) throws javax.sound.midi.InvalidMidiDataException
    {
	if ( !isOn() ) return;
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


    //AUDIO

    //a Clip is a DataLine whose audio gets preloaded.

    //javax.sound.midi.Sequencer seqr; <- no hay equivalente, Lines las abrimos ya sabiendo AudioFormat
    //javax.sound.sampled.AudioInputStream curStream;
    private java.util.Hashtable audioPreloaded = new java.util.Hashtable();


    public void audioPreload ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	javax.sound.sampled.AudioInputStream aii = javax.sound.sampled.AudioSystem.getAudioInputStream ( f );
	javax.sound.sampled.AudioFormat af = aii.getFormat();

	javax.sound.sampled.AudioFormat finalFormat = af; //will not be af if format needs to be decoded
	javax.sound.sampled.AudioInputStream finalStream = aii; //same as with the format


	if ( f.getAbsolutePath().toLowerCase().endsWith(".ogg") || f.getAbsolutePath().toLowerCase().endsWith(".mp3") )
	{
	    //boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);


	    AudioFormat baseFormat = aii.getFormat();
	    finalFormat = new AudioFormat(
		    AudioFormat.Encoding.PCM_SIGNED,
		    baseFormat.getSampleRate(),
		    16,
		    baseFormat.getChannels(),
		    baseFormat.getChannels() * 2,
		    baseFormat.getSampleRate(),
		    false);
	    //bigEndian);
	    // Get AudioInputStream that will be decoded by underlying VorbisSPI
	    finalStream = AudioSystem.getAudioInputStream(finalFormat, aii);
	    //}
	}


	//get a Clip
	javax.sound.sampled.Clip cl = ( javax.sound.sampled.Clip ) javax.sound.sampled.AudioSystem.getLine ( new javax.sound.sampled.DataLine.Info ( javax.sound.sampled.Clip.class , finalFormat ) );

	//load the AudioInputStream gotten from the file into the Clip
	cl.open (  finalStream );

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

    public void audioStartPreloaded ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	if ( !isOn() ) return;
	audioStartPreloaded ( f , 0 );
    }

    public void audioStartPreloaded ( File f , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	if ( !isOn() ) return;
	javax.sound.sampled.Clip cl = getPreloadedClip ( f );
	if ( cl == null )
	    audioPreload ( f );
	cl = getPreloadedClip ( f );
	//cl not null [or exception should have been thrown by audioPreload]
	cl.setFramePosition(0);
	cl.loop(loopTimes);
	//cl.start();
    }

    public void audioStopPreloaded ( File f )
    {
	javax.sound.sampled.Clip cl = getPreloadedClip ( f );
	if ( cl == null )
	    return;
	//cl not null
	cl.stop();
    }

    private Map basicPlayers = Collections.synchronizedMap(new HashMap());

    public void audioStartUnpreloaded ( final File f ) throws IOException
    {
	if ( !isOn() ) return;
	audioStartUnpreloaded(f,0);
    }

    public void audioStartUnpreloaded ( final File f , final int loopTimes ) throws IOException
    {
	if ( !isOn() ) return;
	/*
	    	Log theLog = LogFactory.getLog(BasicPlayer.class);
	    	if ( theLog instanceof Jdk14Logger )
	    	{
	    	    Jdk14Logger logToDisable = (Jdk14Logger)theLog;
	    	    logToDisable.
	    	}
	 */
	java.util.logging.Logger log = java.util.logging.Logger.getLogger("javazoom.jlgui.basicplayer.BasicPlayer");
	log.setLevel(Level.SEVERE);
	final BasicPlayer bp = new BasicPlayer();
	try
	{
	    bp.open(f);
	}
	catch ( BasicPlayerException bpe )
	{
	    bpe.printStackTrace();
	    throw new IOException(bpe);
	}
	basicPlayers.put(f.getAbsolutePath(),bp);
	bp.addBasicPlayerListener( new BasicPlayerListener()
	{
	    private int loopCount = loopTimes;

	    public void opened(Object arg0, Map arg1) {	}
	    public void progress(int arg0, long arg1, byte[] arg2, Map arg3) {}
	    public void setController(BasicController arg0) {}
	    public void stateUpdated(BasicPlayerEvent arg0) 
	    {
		if ( arg0.getCode() == BasicPlayerEvent.EOM )
		{
		    if ( loopCount < 0 ) //infinite loop
		    {
			try
			{
			    bp.seek(0);
			    bp.play();
			}
			catch ( BasicPlayerException bpe )
			{
			    bpe.printStackTrace();
			}
		    }
		    else if ( loopCount > 0 )
		    {
			loopCount--;
			try
			{
			    bp.seek(0);
			    bp.play();
			}
			catch ( BasicPlayerException bpe )
			{
			    bpe.printStackTrace();
			}
		    }
		    else
			basicPlayers.remove(f.getAbsolutePath());
		}
	    }

	}
	);
	try
	{
	    bp.play();
	}
	catch ( BasicPlayerException bpe )
	{
	    bpe.printStackTrace();
	    throw new IOException(bpe);
	}
    }

    public void audioStopUnpreloaded ( File f )
    {
	BasicPlayer bp = (BasicPlayer) basicPlayers.get(f.getAbsolutePath());
	if ( bp != null )
	{
	    try
	    {
		bp.stop();
	    }
	    catch ( BasicPlayerException bpe )
	    {
		bpe.printStackTrace();
	    }
	    basicPlayers.remove(f.getAbsolutePath());
	}
    }

    public void audioStart ( File f , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	javax.sound.sampled.Clip cl = getPreloadedClip ( f );
	if ( cl == null )
	    audioStartUnpreloaded ( f , loopTimes );
	else
	    audioStartPreloaded ( f , loopTimes );
    }

    public void audioStart ( File f ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	audioStart ( f , 0 );
    }

    public void audioStart ( String s ) throws UnsupportedAudioFileException, LineUnavailableException, IOException
    {
	audioStart ( s , 0 );
    }

    public void audioStart ( String s , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
    {
	audioStart ( new File ( s ) , loopTimes );
    }

    public void audioStop ( String s )
    {
	audioStopUnpreloaded ( new File ( s ) );
    }




//  for MOD	

    PlayerThread pt;

    //iRepeat: number of times to repeat (or -1 for infinite)
    public void playMOD( File f, int iRepeat ) 
    throws Exception
    {
	if ( !isOn() ) return;
	playMOD(new DataInputStream(new FileInputStream(f)),iRepeat);
    }
    
    public void playMOD ( URL u , int iRepeat ) throws Exception
    {
	if ( !isOn() ) return;
	playMOD(new DataInputStream(u.openStream()),iRepeat);
    }
    
    private void playMOD ( DataInput theInput , int iRepeat  ) throws Exception
    {
	if ( !isOn() ) return;
	MODThread mt;
	JavaSoundOutputDevice out =  
	    new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
	Module module = ModuleLoader.read(theInput);
	MicroMod microMod = new MicroMod(module, out, new LinearResampler());

	mt = new MODThread(microMod, out, iRepeat);
	pt = mt;
	pt.setVolume(0x10000);

	mt.start();
	return;
    }

    public void playMOD ( String s , int iRepeat ) throws Exception
    {
	if ( !isOn() ) return;
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



    /**
     * Method to stop all current sound, useful e.g. when closing an AGE window.
     */
    public void stopAllSound()
    {
	//stop basic player sound
	for (Iterator iterator = basicPlayers.values().iterator(); iterator.hasNext();) 
	{
	    BasicPlayer bp = (BasicPlayer) iterator.next();
	    try
	    {
		bp.stop();
	    }
	    catch ( BasicPlayerException bpe )
	    {
		bpe.printStackTrace();
	    }
	}
	basicPlayers.clear();

	//stop midi
	if ( seqr != null )
	    seqr.stop();

	//stop MOD
	try
	{
	    stopMOD();
	}
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
    }


}

//for MOD

interface PlayerThread extends Runnable
{
    public abstract void setVolume(int vol);
    public abstract void stopPlaying() throws Exception;
} 
