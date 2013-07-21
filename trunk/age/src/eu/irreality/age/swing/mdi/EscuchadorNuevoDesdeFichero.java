package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.FileSelectorDialogs;

public class EscuchadorNuevoDesdeFichero implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorNuevoDesdeFichero ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		String path = FileSelectorDialogs.showOpenWorldDialog(thePanel);
		if ( path != null )
			new SwingAetheriaGameLoader (path , thePanel , false , null , null, false );
	}

}