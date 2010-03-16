package eu.irreality.age;

import java.io.File;

public class FiltroFicheroMundo extends javax.swing.filechooser.FileFilter
{
	
	public boolean accept ( File f )
	{
		if ( f.getName().equalsIgnoreCase("world.dat") || f.getName().equalsIgnoreCase("world.xml") ) return true;
		else if ( !f.isFile() ) return true;
		else return false;
	}
	
	public String getDescription()
	{
		return "Ficheros de mundo de AGE (world.dat, world.xml)";
	}
	
}