/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.io.*;
import javax.swing.*;

import eu.irreality.age.i18n.UIMessages;

import java.util.*;
public class FiltroFicheroEstado extends javax.swing.filechooser.FileFilter
{
	
	public boolean accept ( File f )
	{
	
		String nombre = f.getName();
		String token;
		StringTokenizer ftok = new StringTokenizer ( nombre , "." );
		token = nombre;
		
		if ( !f.isFile() ) return true;
		
		while ( ftok.hasMoreTokens() )
		{
			token = ftok.nextToken();
		}
		if ( token.equalsIgnoreCase("asf") ) return true;
		else return false;
	}
	
	public String getDescription()
	{
		return UIMessages.getInstance().getMessage("filter.statefile"); 
		//"Ficheros de estado de AGE (*.asf)";
	}
	
}