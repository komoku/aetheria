/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 26/07/2012 18:41:39
 */
package eu.irreality.age.swing.applet;

import java.applet.Applet;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import eu.irreality.age.util.compression.StringCompressor;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * @author carlos
 *
 */
public class LocalStorageUtils 
{

	public static void saveData (Applet a , String name, String value) 
	{
	    try 
	    {
	    	JSObject win = JSObject.getWindow(a);
	        win.eval("localStorage.setItem('" + name + "', '" + value + "');");
	    } 
	    catch (JSException ex) 
	    {
	        System.out.println(ex.getMessage());
	    }
	}
	
	public static String loadData (Applet a , String name) 
	{
	    try 
	    {
	        JSObject win = JSObject.getWindow(a);
	        String value = (String) win.eval("localStorage.getItem('" + name + "');");
	        System.out.println(value);
	        return value; //added by me. 
	    }
	    catch (JSException ex) 
	    {
	        System.out.println(ex.getMessage());
	    }
	    return null;
	}
	
	public static boolean isLocalStorageSupported ( Applet a )
	{
	    try 
	    {
	        JSObject win = JSObject.getWindow(a);
	        Boolean value = (Boolean) win.eval("(typeof(Storage)!=='undefined')");
	        return value.booleanValue(); //added by me. 
	    }
	    catch (JSException ex) 
	    {
	        System.out.println(ex.getMessage());
	    }
	    return false;
	}
	
	public static void saveCompressedData (Applet a , String name, String value, String encoding) throws UnsupportedEncodingException
	{
		String compressedValue = StringCompressor.compress(value, encoding);
		saveData ( a , name , compressedValue );
	}
	
	public static String loadCompressedData (Applet a , String name, String encoding) throws UnsupportedEncodingException, DataFormatException
	{
		String compressedValue = loadData ( a , name );
		return StringCompressor.decompress(compressedValue, encoding);
	}
	
	public static boolean saveAndValidateCompressedData (Applet a , String name, String value, String encoding) throws UnsupportedEncodingException, DataFormatException
	{
		String compressedValue = StringCompressor.compress(value, encoding);
		saveData ( a , name , compressedValue );
		String gottenValue = loadCompressedData ( a , name , encoding );
		return ( value.equals(gottenValue) );
	}
	
	public static void eraseData (Applet a , String name) {
	    saveData(a , name, "");
	}
	
}
