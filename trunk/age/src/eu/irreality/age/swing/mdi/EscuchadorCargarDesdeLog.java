package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.swing.FileSelectorDialogs;

public class EscuchadorCargarDesdeLog implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeLog ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final String path = FileSelectorDialogs.showOpenLogDialog(thePanel);

		if( path != null ) 
		{
			String worldFile;
			try
			{
				FileInputStream fis = new FileInputStream ( path );
				BufferedReader br = new BufferedReader ( Utility.getBestInputStreamReader ( fis ) );
				//primera linea del fichero de log: fichero de mundo
				worldFile = br.readLine(); 
				if ( worldFile == null ) worldFile="";
			}
			catch ( Exception fnfe )
			{
				worldFile = "";
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( worldFile ); //this returns null for world not found
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread()
			{
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null , path , thePanel );
				}
			};
			thr.start();

			//new SwingAetheriaGameLoader( new File(worldFile).getParent() , thePanel , true , selector.getSelectedFile().getAbsolutePath() , null, true );

		}
	}

}
