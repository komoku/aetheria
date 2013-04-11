package eu.irreality.age.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Properties;
import java.util.Scanner;

/**
 * In Java 1.6, there is a Properties.load(Reader) method that can be used to read .properties files
 * in encodings different from ISO-8859-1.
 * 
 * This class contains a cheap, ugly workaround to be able to do the same in Java 1.5.
 * @author carlos
 *
 */
public class UTF8PropertiesLoader 
{

	public static String convertStreamToString(InputStream is) 
	{ 
	    return new Scanner(is).useDelimiter("\\A").next();
	}
	
	
	/**
	 * Converts the given string to ISO-8859-1, in such a way that if a character of the source string is not representable in that charset, it is
	 * escaped. 
	 * @param src
	 * @return
	 */
	public static String toIsoWithEscapes( String src ) 
	{
	    //final String src = "Hallo הצü"; // this has to be read with the right encoding
	    final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); //US-ASCII, because if US-ASCII can encode it, then the char is the same in UTF as in ISO
	    final StringBuilder result = new StringBuilder();
	    char[] arr = src.toCharArray();
	    for ( int i = 0 ; i < arr.length ; i++ ) 
	    {
	    	char character = arr[i];
	    	
	        if (asciiEncoder.canEncode(character)) 
	        {
	            result.append(character); //character needs no modification
	        } 
	        else //just use an unicode escape
	        {
	            result.append("\\u");
	            result.append(Integer.toHexString(0x10000 | character).substring(1).toUpperCase());
	        }
	    	
	    	/*
	    	
	    	//directly from the internets
			if ((arr[i] > '')) //characters that aren't the same in ISO and in UTF-8 (i.e. non-US-ASCII chars). That char is char 127. \u007f
			{
			// wr\uddddte ?
				result.append('\\');
			    result.append('u');
			    String hex = Integer.toHexString(arr[i]);
			    StringBuffer hex4 = new StringBuffer(hex);
			    hex4.reverse();
			    int length = 4 - hex4.length();
			    for (int j = 0; j < length; j++) 
			    {
			    	hex4.append('0');
			    }
			    for (int j = 0; j < 4; j++) 
			    {
			    	result.append(hex4.charAt(3 - j));
			    }
			} 
			else
				result.append(arr[i]);
				
			*/
	    	
	    }
	    return result.toString();
	 }
	
	
	
	/**
	 * Loads properties from a custom encoding properties file, in an 1.5-compatible way.
	 * @param p The Properties object to store properties into.
	 * @param is The InputStream to get the properties from.
	 * @param encoding The encoding associated with the data to get from the InputStream.
	 * @throws IOException
	 */
	public static void loadProperties ( Properties p , InputStream is , String encoding ) throws IOException
	{
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, encoding);
		int read;
		do {
		  read = in.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		String propertiesAsString = out.toString();
		//ByteArrayInputStream bais = new ByteArrayInputStream(propertiesAsString.getBytes("ISO-8859-1"));
		ByteArrayInputStream bais = new ByteArrayInputStream(toIsoWithEscapes(propertiesAsString).getBytes());
		p.load(bais);
		bais.close();
		in.close();
	}
	
}
