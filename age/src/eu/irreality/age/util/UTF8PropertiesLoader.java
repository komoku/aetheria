package eu.irreality.age.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
		ByteArrayInputStream bais = new ByteArrayInputStream(propertiesAsString.getBytes("ISO-8859-1"));
		p.load(bais);
	}
	
}
