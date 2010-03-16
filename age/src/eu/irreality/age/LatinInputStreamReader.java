/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

/*

import java.io.*;

public class LatinInputStreamReader extends InputStreamReader
{

	private InputStreamReader isr;

	public LatinInputStreamReader ( InputStream is )
	{
		try
		{
			isr = new InputStreamReader ( is , "ISO-8859-1" );
		}
		catch ( UnsupportedEncodingException uee )
		{
			System.out.println("DANGER: ISO-8859-1 encoding not supported.");
			isr = new InputStreamReader ( is );
		}
	}
	
	public LatinInputStreamReader ( InputStream is , String enc ) throws UnsupportedEncodingException
	{
		isr = super (is,enc);
	}
	
	public void close() throws IOException
	{
		isr.close();
	}
	
	public String getEncoding()
	{
		return isr.getEncoding();
	}
	
	public int read() throws IOException
	{
		return isr.read();
	}
	
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return isr.read ( cbuf , off , len );
	}
	
	public boolean ready() throws IOException
	{
		return isr.ready();
	}



}

*/