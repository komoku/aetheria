package eu.irreality.age;

import java.io.File;

import eu.irreality.age.i18n.UIMessages;

public class FiltroFicheroMundo extends javax.swing.filechooser.FileFilter
{
	
	public boolean accept ( File f )
	{
		if ( f.getName().equalsIgnoreCase("world.dat") || f.getName().equalsIgnoreCase("world.xml") ) return true;
		// Aunque esta condición (equivalente a *world*.xml) captura también el caso de world.xml, dejamos la anterior como "fast path".
		else if ( f.getName().endsWith(".xml") && f.getName().toLowerCase().contains("world") ) return true;
		else if ( f.getName().endsWith(".agw") ) return true;
		else if ( f.getName().endsWith(".agz") ) return true;
		else if ( !f.isFile() ) return true;
		else return false;
	}
	
	public String getDescription()
	{
		return UIMessages.getInstance().getMessage("filter.worldfile");
		//"Ficheros de mundo de AGE (world.xml,*.agz)";
	}
	
}