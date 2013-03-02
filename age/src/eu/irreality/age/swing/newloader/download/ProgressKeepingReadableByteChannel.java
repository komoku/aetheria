package eu.irreality.age.swing.newloader.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A version of a ReadableByteChannel that keeps track of the progress downloading from an URL,
 * and calls a method in a delegate to tell it.
 * @author carlos
 *
 */
public class ProgressKeepingReadableByteChannel implements ReadableByteChannel 
{
    private ProgressKeepingDelegate delegate;
    private long expectedSize;
    private ReadableByteChannel rbc;
    private long readSoFar;
    private String progressString;

    public ProgressKeepingReadableByteChannel( ReadableByteChannel rbc, long expectedSize, ProgressKeepingDelegate delegate , String progressString ) 
    {
        this.delegate = delegate;
        this.expectedSize = expectedSize;
        this.rbc = rbc;
        this.progressString = progressString;
    }

    public void close() throws IOException 
    { 
    	rbc.close(); 
    }
    
    public long getReadSoFar() 
    { 
    	return readSoFar; 
    }
    
    public boolean isOpen() 
    { 
    	return rbc.isOpen(); 
    }

    public int read( ByteBuffer bb ) throws IOException 
    {
        int n;
        double progress;

        if ( ( n = rbc.read( bb ) ) > 0 ) 
        {
            readSoFar += n;
            progress = expectedSize > 0 ? (double) readSoFar / (double) expectedSize /** 100.0*/ : -1.0;
            if ( delegate != null )
            	delegate.progressUpdate( progress , progressString );
        }
        return n;
    }
}