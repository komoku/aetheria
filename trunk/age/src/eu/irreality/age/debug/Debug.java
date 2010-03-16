package eu.irreality.age.debug;

public class Debug 
{

	public static boolean DEBUG_OUTPUT = false;
	
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
