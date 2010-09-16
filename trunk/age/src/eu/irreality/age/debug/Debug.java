package eu.irreality.age.debug;

public class Debug 
{

	public static boolean DEBUG_OUTPUT = false;
	
	public static boolean codeDebugging = false;
	
	public static void setCodeDebugging ( boolean d )
	{
	    codeDebugging = d;
	}
	
	public static boolean getCodeDebugging ( )
	{
	    return codeDebugging;
	}
	
	
	public static void printCodeDebugging ( String s )
	{
	    if ( codeDebugging ) System.err.print(s);
	}
	
	public static void printlnCodeDebugging ( String s )
	{
	    if ( codeDebugging ) System.err.println(s);
	}
	
	public static void printlnCodeDebugging ( Object o )
	{
	    if ( codeDebugging ) System.err.println(o);
	}
	
	
	public static void print ( String s )
	{
		if ( DEBUG_OUTPUT )
			System.out.print(s);
	}
	
	public static void println ( String s )
	{
		if ( DEBUG_OUTPUT )
			System.out.println(s);
	}
	
	public static void println ( Object o )
	{
		if ( DEBUG_OUTPUT )
			System.out.println(o);
	}
	
	
	
}
