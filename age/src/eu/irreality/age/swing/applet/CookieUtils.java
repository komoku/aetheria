package eu.irreality.age.swing.applet;

import java.applet.Applet;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import eu.irreality.age.util.compression.StringCompressor;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class CookieUtils 
{
	
	
	public static void createCookie (Applet a , String name, String value, long days) {
	    try {
	        JSObject win = JSObject.getWindow(a);
	        long time = days * 24 * 60 * 60 * 1000;
	        String cmd = "var date = new Date();" + "date.setTime(date.getTime() + " +
	                     String.valueOf(time) + ");" + "date.toGMTString();";
	        String expires = "; expires=" + (String) win.eval(cmd);
	        value = (String) win.eval("escape('" + value + "');");
	        win.eval("document.cookie = '" + name + "=" + value + expires + "; path=/';");
	    } catch (JSException ex) {
	        System.out.println(ex.getMessage());
	    }
	}
	
	public static void createCompressedCookie (Applet a , String name, String value, String encoding, long days) throws UnsupportedEncodingException
	{
		String compressedValue = StringCompressor.compress(value, encoding);
		createCookie ( a , name , compressedValue , days );
	}
	
	public static boolean createAndValidateCompressedCookie (Applet a , String name, String value, String encoding, long days) throws UnsupportedEncodingException, DataFormatException
	{
		String compressedValue = StringCompressor.compress(value, encoding);
		createCookie ( a , name , compressedValue , days );
		String gottenValue = readCompressedCookie ( a , name , encoding );
		//System.err.println("Created cookie: " + value);
		return ( value.equals(gottenValue) );
	}
	

	public static String readCookie (Applet a , String name) {
	    try {
	        JSObject win = JSObject.getWindow(a);
	        StringTokenizer st = new StringTokenizer((String) win.eval("document.cookie"), ";", false);
	        String line, value;
	        int eqpos;
	        while(st.hasMoreTokens()) {
	            line = st.nextToken().trim();
	            eqpos = line.indexOf('=');
	            if(line.substring(0, eqpos).compareTo(name) == 0) {
	                value = line.substring(eqpos+1, line.length());
	                value = (String) win.eval("unescape('" + value + "');");
	                System.out.println(value);
	                return value; //added by me.
	            }
	        }
	    } catch (JSException ex) {
	        System.out.println(ex.getMessage());
	    }
	    return null;
	}
	
	public static String readCompressedCookie (Applet a , String name, String encoding) throws UnsupportedEncodingException, DataFormatException
	{
		String compressedValue = readCookie ( a , name );
		//System.err.println("Read  cookie: " + StringCompressor.decompress(compressedValue, encoding));
		if ( compressedValue == null ) return null;
		return StringCompressor.decompress(compressedValue, encoding);
	}
	
	

	public static void eraseCookie (Applet a , String name) {
	    createCookie(a , name, "", -1);
	}

}
