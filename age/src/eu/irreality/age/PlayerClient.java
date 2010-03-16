/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

public interface PlayerClient
{

	public boolean isColorEnabled();
	public boolean isMemoryEnabled();
	public boolean isLoggingEnabled();
	public boolean isTitleEnabled();
	
		public String requestInput(Player pl);
	
		public void escribirTitulo ( String s );
		public void escribirTitulo ( String s , int pos );
		
		public void escribir ( String s );
		
		public void forzarEntrada ( String s , boolean output_enabled );
		
		public void activatePressAnyKeyState ();
		
		public void borrarPantalla ( );
		
		public String getColorCode ( String s);
	
}