package eu.irreality.age.swing.sdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.FileSelectorDialogs;

public class NewFromFileListener implements ActionListener
{

	SwingSDIInterface window;

	public NewFromFileListener ( SwingSDIInterface w )
	{
		window = w;	
	}

	public void actionPerformed ( ActionEvent evt )
	{
		String path = FileSelectorDialogs.showOpenWorldDialog(window);
		if(path != null) 
		{			
			window.startGame( path , false , null , null );
		}
	}

}
	

