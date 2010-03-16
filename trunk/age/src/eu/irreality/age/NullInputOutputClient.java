/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
public class NullInputOutputClient implements InputOutputClient
{

	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}
	public void write ( String s )
	{
	}
	public String getInput ( Player p )
	{
		return null;
	}
	public String getRealTimeInput ( Player p )
	{
		return null;
	}
	
	public boolean isDisconnected()
	{
		return false;
	}

	public void waitKeyPress () //esta pulsando tecla todo el tiempo, el tio :)
	{
	}
	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla ( )
	{
		clearScreen();
	}
	public void clearScreen ( )
	{
	}
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo ( String s )
	{
		writeTitle(s);
	}
	public void writeTitle ( String s )
	{
	}
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo ( String s , int pos )
	{
		writeTitle(s, pos);
	}
	public void writeTitle ( String s , int pos )
	{
	}


	//color functions. If I/O is noncolored, just return "", no code, no color!
	public String getColorCode(String s)
	{
		return "";
	}


	public boolean isColorEnabled()
	{
		return false;
	}
	public boolean isMemoryEnabled()
	{
		return false;
	}
	public boolean isLoggingEnabled()
	{
		return false;
	}
	public boolean isTitleEnabled()
	{
		return false;
	}

	/**
	 * @deprecated Use {@link #forceInput(String,boolean)} instead
	 */
	public void forzarEntrada ( String s , boolean output_enabled )
	{
		forceInput(s, output_enabled);
	}
	public void forceInput ( String s , boolean output_enabled )
	{
		
	}

}
