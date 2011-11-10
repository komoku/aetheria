package eu.irreality.age.swing.sdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;

public class NewFromFileListener implements ActionListener
{

	SwingSDIInterface window;

	public NewFromFileListener ( SwingSDIInterface w )
	{
		window = w;	
	}

	public void actionPerformed ( ActionEvent evt )
	{
		JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.new.title") );
		selector.setFileFilter ( new FiltroFicheroMundo() );
		int returnVal = selector.showOpenDialog(window);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );
			
			window.startGame( selector.getSelectedFile().getAbsolutePath() , false , null , null );
		}
	}

}
	

