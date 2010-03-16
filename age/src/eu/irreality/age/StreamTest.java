/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;

public class StreamTest
{

	public static void main ( String[] args ) throws Throwable
	{

	PipedOutputStream os = new PipedOutputStream();
	PipedInputStream is = new PipedInputStream(os);
	
	BufferedReader br = new BufferedReader ( new InputStreamReader ( is ) );
	PrintWriter pw = new PrintWriter ( new OutputStreamWriter ( os ) );
	
	pw.println("L�nea 1");
	pw.flush();
	System.out.println(br.readLine());
	byte[] hola = 
	{ 'h','o','l','a'
	};
	os.write( hola,0,4 );
	os.flush();
	System.out.println(is.read());
	System.out.println(is.read());
	System.out.println(is.read());
	//System.out.println(is.read());
	System.out.println(br.readLine());
	pw.println("L�nea 2");
	pw.flush();


	}

}
