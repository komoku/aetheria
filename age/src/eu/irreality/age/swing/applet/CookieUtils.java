package eu.irreality.age.swing.applet;

import java.applet.Applet;
import java.util.StringTokenizer;

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

	public static void eraseCookie (Applet a , String name) {
	    createCookie(a , name, "", -1);
	}

}
