package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.SwingAetheriaGameLoader;
import eu.irreality.age.filemanagement.Paths;

public class EscuchadorNuevoDesdeFichero implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorNuevoDesdeFichero ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		selector.setDialogTitle("Selecciona el directorio del juego o el fichero world.dat");
		selector.setFileFilter ( new FiltroFicheroMundo() );

		int returnVal = selector.showOpenDialog(thePanel);
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			//if ( selector.getSelectedFile().isFile() )
			//{
			//new SwingAetheriaGameLoader( selector.getSelectedFile().getParent() , thePanel , false ,null , null, true );
			//}
			//else
			//{
			//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			new SwingAetheriaGameLoader
			(selector.getSelectedFile().getAbsolutePath() , thePanel , false , null , null, false );
			//}
		}
	}

}