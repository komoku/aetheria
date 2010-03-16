/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;


class VersatileBufferedInputStream extends BufferedInputStream
{

	public VersatileBufferedInputStream ( InputStream is )
	{
		super ( is );
	}
	
	public VersatileBufferedInputStream ( InputStream is , int bufsize )
	{
		super ( is , bufsize );
	}
	
	public String readLine() throws IOException
	{
		
		int temp_markpos = markpos;
		int temp_marklimit = marklimit;
		
		mark(marklimit+65536);
		int nbytes = 0;
		byte cur;	
		while ( (cur=(byte)read()) != (byte)'\r' ) 
		{
			nbytes++;
		}
		nbytes++; //\r
		if ( read() == '\n' ) nbytes++;
	
		byte[] barr = new byte[nbytes];
		reset();
		int leido = 0;
		while ( leido < nbytes )
		{
			leido += read ( barr , leido , nbytes-leido );
		}
		
		String resultado;
		if ( barr[nbytes-1] == '\n' )
			resultado = new String ( barr , 0 , nbytes-2 );
		else
			resultado = new String ( barr , 0 , nbytes-1 );	
	
		markpos = temp_markpos;
		marklimit = temp_marklimit;
		
		return resultado;
	
	}
	

}