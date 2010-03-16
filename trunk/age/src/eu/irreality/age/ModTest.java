/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import micromod.*;
import micromod.resamplers.*;
import micromod.output.*;
import micromod.output.converters.*; 
import java.io.*;
import javax.sound.sampled.*;
 
/*
  interface PlayerThread extends Runnable
  {
    public abstract void setVolume(int vol);

    public abstract void stopPlaying() throws Exception;
  } 
 */
 
public class ModTest
{


	PlayerThread pt;
 
  boolean playMOD(File f, int iRepeat, int soundId, int iNotify) 
    throws Exception
  {
    MODThread mt;
    JavaSoundOutputDevice out =  
      new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
    Module module = ModuleLoader.read(new DataInputStream(new FileInputStream(f)));
    MicroMod microMod = new MicroMod(module, out, new LinearResampler());

    mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
    pt = mt;
    pt.setVolume(0x10000);


    mt.start();
    return true;
  }
  
  
  
	/*inner*/	  class MODThread extends Thread implements PlayerThread
		{
		   	int soundId;
		    int iNotify;
		    int iRepeat;
		    boolean running;
		    boolean stopped;
		    JavaSoundOutputDevice out;
		    MicroMod mm;

		    MODThread(MicroMod mm, JavaSoundOutputDevice out, 
		              int iRepeat, int soundId, int iNotify)
		    {
		      this.mm = mm;
		      this.out = out;
		      this.soundId = soundId;
		      this.iNotify = iNotify;
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
							System.out.println("Real Time.");
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

/*

boolean playSONG(BlorbFile bf,
                   BlorbFile.Chunk c, int iRepeat, int soundId, int iNotify)
    throws Exception
  {
    MODThread mt;
    MicroMod microMod;
    JavaSoundOutputDevice out =  
      new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
    Module module = ModuleLoader.read(new DataInputStream(c.getData()));
    
    // for some reason, the first two samples are always blanked out in mods
    for (int i = 2; i < 32; i++)
    {
      Instrument inst = module.getInstrument(i);
      if (inst != null)
      {
        String name = inst.name.trim();
        if ("".equals(name))
          continue;

        if (name.length() < 4)
          return false;
        int id = Integer.parseInt(name.substring(3, name.length()));
        BlorbFile.Chunk ac = bf.getByUsage(BlorbFile.SND, id);
        Aiff aiff = new Aiff(ac);
        byte[] data = aiff.getSoundData();
        int numChannels = aiff.getNumChannels();
        int sampleSize = aiff.getSampleSize();
        int numSampleFrames = aiff.getNumSampleFrames();
        int offset = aiff.getOffset();
        ByteBuffer newData = ByteBuffer.allocate(numSampleFrames * 2);

        inst.looped = (aiff.getSustainLoopPlayMode() != 0);
        inst.sampleSize = 16;

        if (numChannels == 1 && sampleSize <= 8)
        {
          for (int j = 0; j < numSampleFrames; j++)
            newData.putShort((short) (data[j + offset] << 8));
        }
        else
        {
          int k = offset;
          for (int ix = 0; ix < numSampleFrames; ix++)
          {
            for (int jx = 0; jx < numChannels; jx++)
            {
              int sample;
              if (sampleSize <= 8)
                sample = data[k++] << 24;
              else if (sampleSize <= 16)
                sample = 
                  ((data[k++] << 8) | (data[k++] & 0xff)) << 16;
              else if (sampleSize <= 24)
                sample =
                  ((data[k++] << 16) | ((data[k++] << 8) & 0xff) |
                   (data[k++] & 0xff)) << 8;
              else
                sample = (data[k++] << 24) |
                  ((data[k++] << 16) & 0xff) |
                  ((data[k++] << 8) & 0xff) |
                  (data[k++] & 0xff);
              if (jx == 0)
                newData.putShort((short) (sample >>> 16));
            }
          }
        }
        inst.data = newData.array();
        if (inst.looped)
        {
          inst.loopStart = aiff.getMarkerPos(aiff.getSustainLoopBegin());
          inst.sampleEnd = inst.loopStart + 
            (aiff.getMarkerPos(aiff.getSustainLoopEnd()) - 
             aiff.getMarkerPos(aiff.getSustainLoopBegin()));
        }
        else
        {
          inst.loopStart = 0;
          inst.sampleEnd = numSampleFrames;
        }
      }
    }
    microMod = new MicroMod(module, out, new LinearResampler());
    
    mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
    pt = mt;
    pt.setVolume(vol);
    
    mt.start();
    return true;
  }
*/




