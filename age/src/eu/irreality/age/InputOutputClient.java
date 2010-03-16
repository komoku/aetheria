/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

public interface InputOutputClient extends Output
{

	public boolean isColorEnabled();
	public boolean isMemoryEnabled();
	public boolean isLoggingEnabled();
	public boolean isTitleEnabled();
	
	//for remote ones
	public boolean isDisconnected();
	
	
		public String getInput(Player pl);
		public String getRealTimeInput(Player pl);
		
		public void waitKeyPress();
	
		/**
		 * @deprecated Use {@link #writeTitle(String)} instead
		 */
		public void escribirTitulo ( String s );
		public void writeTitle ( String s );
		/**
		 * @deprecated Use {@link #writeTitle(String,int)} instead
		 */
		public void escribirTitulo ( String s , int pos );
		public void writeTitle ( String s , int pos );
		
		/**
		 * @deprecated Use {@link #write(String)} instead
		 */
		public void escribir ( String s );
		public void write ( String s );
		
		/**
		 * @deprecated Use {@link #forceInput(String,boolean)} instead
		 */
		public void forzarEntrada ( String s , boolean output_enabled );
		public void forceInput ( String s , boolean output_enabled );
		
		//public void activatePressAnyKeyState ();
		
		/**
		 * @deprecated Use {@link #clearScreen()} instead
		 */
		public void borrarPantalla ( );
		public void clearScreen ( );
		
		/**
		 * Devuelve un código de color.
		 * @param name Por ejemplo action, description, denial.
		 */
		public String getColorCode ( String name );
		

	
}