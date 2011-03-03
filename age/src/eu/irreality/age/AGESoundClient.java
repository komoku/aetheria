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

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.*;


import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.URLUtils;


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
	private javax.sound.midi.Sequence getPreloadedSequence ( URL u )
	{
		//try
		//{
			return (javax.sound.midi.Sequence) midiPreloaded.get ( u );
		//}
		//catch ( java.io.IOException ioe ) //of getCanonicalPath()
		//{
		//	return null;
		//}
	}

	//call midiInit before using midiXXX functions. Initializes the sequencer.
	public void midiInit ( ) throws javax.sound.midi.MidiUnavailableException
	{
		//pulseaudio-friendly mode:
		midiClose();
		
		if ( seqr == null )
		{
			seqr = javax.sound.midi.MidiSystem.getSequencer();
			seqr.open();
		}
	}

	//loads a MIDI file into memory (hashtable) so that you don't have to open it each time you play it.
	public void midiPreload ( URL midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence ( midfile );
		midiPreloaded.put ( midfile , seq );
	}

	//unloads a MIDI file from memory. (does nothing if file invalid)
	public void midiUnload ( URL midfile ) throws java.io.IOException
	{
		midiPreloaded.remove ( midfile );
	}

	public void midiUnload ( String s ) throws java.io.IOException
	{
		midiUnload ( URLUtils.stringToURL ( s ) );
	}

	//loads a MIDI file (or not if preloaded) and starts playing it. If the manager was playing another file, playback stops.
	public void midiStart ( URL midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException, MidiUnavailableException
	{
		if ( !isOn() ) return;
		//if ( seqr == null ) midiInit();
		curseq = getPreloadedSequence ( midfile );
		if ( curseq == null ) curseq = javax.sound.midi.MidiSystem.getSequence ( midfile );
		//if ( !seqr.isOpen() ) seqr.open();
		if ( seqr.isRunning() ) seqr.stop();
		
		seqr.setLoopCount ( 0 );
		seqr.setSequence ( curseq );
		
		//for fade-out, did not work
		/*
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        if (synthesizer.getDefaultSoundbank() == null) {
                seqr.getTransmitter().setReceiver(MidiSystem.getReceiver());
        } else {
                seqr.getTransmitter().setReceiver(synthesizer.getReceiver());
        }
        */
		
		seqr.start();
	}

	//loads a MIDI file (or not if preloaded) and starts playing it. If the manager was playing another file, playback stops.
	public void midiStart ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException, MidiUnavailableException
	{
		if ( !isOn() ) return;
		midiStart ( URLUtils.stringToURL ( f ) );
	}

	//loads a MIDI file into memory (hashtable) so that you don't have to open it each time you play it.
	public void midiPreload ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		midiPreload ( URLUtils.stringToURL ( f ) );
	}

	//sets the current file in the sequencer to the given file.
	public void midiOpen ( URL midfile ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		curseq = getPreloadedSequence ( midfile );
		if ( curseq == null ) curseq = javax.sound.midi.MidiSystem.getSequence ( midfile );
	}

	//sets the current file in the sequencer to the given file.
	public void midiOpen ( String f ) throws javax.sound.midi.InvalidMidiDataException , java.io.IOException
	{
		midiOpen ( URLUtils.stringToURL ( f ) );
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

	//sets the current file in the sequencer to none, and closes the sequencer.
	public void midiClose ( )
	{
		curseq = null;	
		if ( seqr != null && seqr.isOpen() ) seqr.close();
		seqr = null;
	}

	
	//DOES NOT WORK
	javax.sound.midi.Synthesizer synthesizer;
	javax.sound.midi.Synthesizer synthDevice;
	private static final int CHANGE_VOLUME = 7;
	
	//DOES NOT WORK
    public void midiResetGain( double gain )
    {
        // make sure the value for gain is valid (between 0 and 1)
        if( gain < 0.0d )
            gain = 0.0d;
        if( gain > 1.0d )
            gain = 1.0d;
        
        int midiVolume = (int) ( gain /* * SoundSystemConfig.getMasterGain() */
                                 /** (float) Math.abs( fadeOutGain ) * fadeInGain */
                        * 127.0d );
        
        System.err.println("Vol " + midiVolume);
        
        if( synthesizer != null )
        {
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            System.err.println("Channels: " + channels.length);
            for( int c = 0; channels != null && c < channels.length; c++ )
            {
            	System.err.println("cc " + midiVolume);
                channels[c].controlChange( CHANGE_VOLUME, midiVolume );
            }
        }
        else if( synthDevice != null )
        {
            try
            {
                ShortMessage volumeMessage = new ShortMessage();
                for( int i = 0; i < 16; i++ )
                {
                    volumeMessage.setMessage( ShortMessage.CONTROL_CHANGE, i,
                                              CHANGE_VOLUME, midiVolume );
                    synthDevice.getReceiver().send( volumeMessage, -1 );
                }
            }
            catch( Exception e )
            {
                System.err.println( "Error resetting gain on MIDI device" );
                e.printStackTrace();
            }
        }
        else if( seqr != null && seqr instanceof Synthesizer )
        {
            synthesizer = (javax.sound.midi.Synthesizer) seqr;
            javax.sound.midi.MidiChannel[] channels = synthesizer.getChannels();
            for( int c = 0; channels != null && c < channels.length; c++ )
            {
                channels[c].controlChange( CHANGE_VOLUME, midiVolume );
            }
        }
        else
        {
            try
            {
                Receiver receiver = MidiSystem.getReceiver();
                ShortMessage volumeMessage= new ShortMessage();
                for( int c = 0; c < 16; c++ )
                {
                    volumeMessage.setMessage( ShortMessage.CONTROL_CHANGE, c,
                                              CHANGE_VOLUME, midiVolume );
                    receiver.send( volumeMessage, -1 );
                }
            }
            catch( Exception e )
            {
                System.err.println( "Error resetting gain on MIDI device" );
                e.printStackTrace();
            }
        }
    }
	

	/*
	 * Did not work:
	 * public boolean setVolume(double value) {
                try {
                        Receiver receiver = MidiSystem.getReceiver();
                        ShortMessage volumeMessage= new ShortMessage();

                        for (int i = 0; i < 16; i++) {
                                volumeMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, (int)(value * 127.0));
                                receiver.send(volumeMessage, -1);
                        }
                        return true;
                } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                }
        }
	 */
	
    //DOES NOT WORK
    public void midiFadeOut() {
                double volume = 0.6;

                for (;;) {
                		
                        if (((volume - 0.05) < 0)) {
                                break;
                        }
                        midiResetGain(volume);
                        System.err.println("Gain = " + volume);
                        try {
                                Thread.sleep(150);
                        } catch (Exception exception) {
                        }
                        volume -= 0.025;
                }
                if (synthesizer != null) {
                        synthesizer.close();
                        synthesizer = null;
                }
                if (seqr != null) {
                        if (seqr.isOpen()) {
                                seqr.stop();
                        }
                        seqr.close();
                }
        }
	
	/* Associated with prev. two:
	 * public void startMidi() {
                String midiDir = getMidiFileName() + getMidiSaveDir();

                try {
                        if (sequencer != null) {
                                fadeOut();
                        }
                        sequencer = null;
                        sequence = null;
                        File file = new File(midiDir);

                        if (file.exists()) {
                                sequence = MidiSystem.getSequence(file);
                        }
                        sequencer = MidiSystem.getSequencer();
                        sequencer.setSequence(sequence);
                        synthesizer = MidiSystem.getSynthesizer();
                        synthesizer.open();

                        if (synthesizer.getDefaultSoundbank() == null) {
                                sequencer.getTransmitter().setReceiver(MidiSystem.getReceiver());
                        } else {
                                sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
                        }
                        sequencer.open();
                        sequencer.start();
                } catch (Exception exception) {
                        exception.printStackTrace();
                }
        }
	 */
	
	/*end MIDI*/





	/*begin AUDIO*/


	//AUDIO

	//a Clip is a DataLine whose audio gets preloaded.

	//javax.sound.midi.Sequencer seqr; <- no hay equivalente, Lines las abrimos ya sabiendo AudioFormat
	//javax.sound.sampled.AudioInputStream curStream;
	private java.util.Hashtable audioPreloaded = new java.util.Hashtable();


	public void audioPreload ( URL u ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		javax.sound.sampled.AudioInputStream aii = javax.sound.sampled.AudioSystem.getAudioInputStream ( u );
		javax.sound.sampled.AudioFormat af = aii.getFormat();

		javax.sound.sampled.AudioFormat finalFormat = af; //will not be af if format needs to be decoded
		javax.sound.sampled.AudioInputStream finalStream = aii; //same as with the format


		if ( u.getPath().toLowerCase().endsWith(".ogg") || u.getPath().toLowerCase().endsWith(".mp3") )
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
		audioPreloaded.put ( u , cl );

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
		audioPreload ( URLUtils.stringToURL ( s ) );
	}

	private javax.sound.sampled.Clip getPreloadedClip ( URL u )
	{
		//try
		//{
			return (javax.sound.sampled.Clip) audioPreloaded.get ( u );
		//}
		//catch ( java.io.IOException ioe ) //of getCanonicalPath()
		//{
		//	return null;
		//}
	}

	public void audioStartPreloaded ( URL u ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		if ( !isOn() ) return;
		audioStartPreloaded ( u , 0 );
	}

	public void audioStartPreloaded ( URL u , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		if ( !isOn() ) return;
		javax.sound.sampled.Clip cl = getPreloadedClip ( u );
		if ( cl == null )
			audioPreload ( u );
		cl = getPreloadedClip ( u );
		//cl not null [or exception should have been thrown by audioPreload]
		cl.setFramePosition(0);
		cl.loop(loopTimes);
		//cl.start();
	}

	public void audioStopPreloaded ( URL u )
	{
		javax.sound.sampled.Clip cl = getPreloadedClip ( u );
		if ( cl == null )
			return;
		//cl not null
		cl.stop();
	}

	private Map basicPlayers = Collections.synchronizedMap(new HashMap());

	public void audioStartUnpreloaded ( final URL u ) throws IOException
	{
		if ( !isOn() ) return;
		audioStartUnpreloaded(u,0);
	}

	public void audioStartUnpreloaded ( final URL u , final int loopTimes ) throws IOException
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
		
		try
		{
			java.util.logging.Logger log = java.util.logging.Logger.getLogger("javazoom.jlgui.basicplayer.BasicPlayer");
			log.setLevel(Level.SEVERE);
		}
		catch ( SecurityException se )
		{
			System.err.println("Restricted security environment, will not take logs of audio issues.");
		}
		
		final BasicPlayer bp = new BasicPlayer();
		try
		{
			InputStream theStream = u.openStream();
			if ( theStream.markSupported() )
			{
				bp.open(theStream);
			}
			else //in applets that read remote URLs, mark is not supported so we need to add an extra layer.
			{
				BufferedInputStream bib = new BufferedInputStream(theStream);
				bp.open(bib);
			}
		}
		catch ( BasicPlayerException bpe )
		{
			bpe.printStackTrace();
			throw new IOException(bpe);
		}
		basicPlayers.put(u,bp);
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
							/*
							System.err.println("Seek:");
							bp.seek(0);
							System.err.println("Play:");
							bp.play();
							*/
							if ( !isOn() ) return;
							bp.stop();
							bp.open(u.openStream());
							bp.play();
						}
						catch ( BasicPlayerException bpe )
						{
							bpe.printStackTrace();
						}
						catch ( IOException ioe )
						{
							ioe.printStackTrace();
						}
					}
					else if ( loopCount > 0 )
					{
						loopCount--;
						try
						{
							/*
							System.err.println("Seek:");
							bp.seek(0);
							System.err.println("Play:");
							bp.play();
							*/
							if ( !isOn() ) return;
							bp.stop();
							bp.open(u.openStream());
							bp.play();
						}
						catch ( BasicPlayerException bpe )
						{
							bpe.printStackTrace();
						}
						catch ( IOException ioe )
						{
							ioe.printStackTrace();
						}
					}
					else
						basicPlayers.remove(u);
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

	public void audioStopUnpreloaded ( URL u )
	{
		BasicPlayer bp = (BasicPlayer) basicPlayers.get(u);
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
			basicPlayers.remove(u);
		}
	}

	public void audioStart ( URL u , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		javax.sound.sampled.Clip cl = getPreloadedClip ( u );
		if ( cl == null )
			audioStartUnpreloaded ( u , loopTimes );
		else
			audioStartPreloaded ( u , loopTimes );
	}

	public void audioStart ( URL u ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		audioStart ( u , 0 );
	}

	public void audioStart ( String s ) throws UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		audioStart ( s , 0 );
	}

	public void audioStart ( String s , int loopTimes ) throws javax.sound.sampled.UnsupportedAudioFileException , javax.sound.sampled.LineUnavailableException , java.io.IOException
	{
		audioStart ( URLUtils.stringToURL ( s ) , loopTimes );
	}

	public void audioStop ( String s )
	{
		audioStopUnpreloaded ( URLUtils.stringToURL ( s ) );
	}
	
	public void audioFadeIn ( String s , int loopTimes , double seconds , double delay ) throws UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		audioFadeIn ( URLUtils.stringToURL ( s ) , loopTimes , seconds , delay );
	}
	
	public void audioFadeOut ( String s , double seconds )
	{
		audioFadeOut ( URLUtils.stringToURL ( s ) , seconds );
	}
	
	/**
	 * input: time (from 0.0 to 1.0)
	 * output: gain (from 1.0 to 0.0)
	 * contract: should output 0.0 or less for 1.0 or more
	 * @param time
	 */
	private double fadeOutFunction ( double time )
	{
		return expFade(time);
	}
	
	private double fadeInFunction ( double time )
	{
		return 1-expFade(time);
	}
	
	private double cosineFade ( double time )
	{
		double angle = time * Math.PI/2;
		if ( time >= 1.0 ) return 0.0;
		else
			return Math.cos(angle);
	}
	
	private double expFade ( double time )
	{
		if ( time >= 1.0 ) return 0.0;
		else
			return 1.0 / ((6*time+1.0)*(6*time+1.0));
	}
	
	public void audioFadeOut ( final URL u , final double seconds )
	{
		final BasicPlayer bp = (BasicPlayer) basicPlayers.get(u);
		if ( bp != null )
		{
			Thread thr = new Thread()
			{
				public void run()
				{
					double gain = 1.0;
					double iters = 100.0; //number of iters of fade-out
					double itersDone = 0.0; //iterations done
					int sleepTime = (int)(seconds * 1000.0 / iters);
					while ( gain > 0.0 )
					{
						itersDone += 1.0;
						gain = fadeOutFunction ( itersDone / (iters-1) );
						try 
						{
							bp.setGain(gain);
							//System.err.println("Gain now " + gain);
							//System.err.println("Gain min " + bp.getMinimumGain());
						} 
						catch (BasicPlayerException e1) 
						{
							e1.printStackTrace();
						}
						try 
						{
							sleep(sleepTime);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					}
					audioStopUnpreloaded(u);
				}
			};
			thr.start();
		}
	}
	
	public void audioFadeIn ( final URL u , final int loopTimes , final double seconds , final double delay ) throws UnsupportedAudioFileException, LineUnavailableException, IOException
	{

			Thread thr = new Thread()
			{
				public void run()
				{
					try 
					{
						sleep ( (int) delay * 1000 );
					} 
					catch (InterruptedException e2) 
					{
						e2.printStackTrace();
					}
					try 
					{
						audioStart ( u , loopTimes );
					} 
					catch (UnsupportedAudioFileException e2) 
					{
						e2.printStackTrace();
					} 
					catch (LineUnavailableException e2) 
					{
						e2.printStackTrace();
					} 
					catch (IOException e2) 
					{
						e2.printStackTrace();
					}
					final BasicPlayer bp = (BasicPlayer) basicPlayers.get(u);
					double gain = 0.0;
					double iters = 100.0; //number of iters of fade-in
					double itersDone = 0.0; //iterations done
					int sleepTime = (int)(seconds * 1000.0 / iters);
					while ( gain < 1.0 )
					{
						itersDone += 1.0;
						gain = fadeInFunction ( itersDone / (iters-1) );
						try 
						{
							bp.setGain(gain);
							//System.err.println("Gain now " + gain);
							//System.err.println("Gain min " + bp.getMinimumGain());
						} 
						catch (BasicPlayerException e1) 
						{
							e1.printStackTrace();
						}
						try 
						{
							sleep(sleepTime);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					}
				}
			};
			thr.start();
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
		if ( seqr != null && seqr.isOpen() )
			seqr.stop();
		
		if ( seqr != null )
			midiClose();

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
