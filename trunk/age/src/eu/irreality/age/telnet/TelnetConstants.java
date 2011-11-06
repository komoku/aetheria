/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.telnet;
public interface TelnetConstants
{
	public static char SE = 240;
	public static char EC = 247;
	public static char EL = 248;
	public static char SB = 250;
	public static char WILL = 251;
	public static char WONT = 252;
	public static char DO = 253;
	public static char DONT = 254;
	public static char IAC = 255;
	
	public static char TELOPT_TTYPE = 36;
	public static char TEL_QUAL_SEND = 1;
}