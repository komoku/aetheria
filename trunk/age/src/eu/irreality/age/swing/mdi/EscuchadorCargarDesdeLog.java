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
import eu.irreality.age.server.ServerHandler;

public class EscuchadorCargarDesdeLog implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeLog ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle("Selecciona el fichero de log");
		selector.setFileFilter ( new FiltroFicheroLog() ); 

		int returnVal = selector.showOpenDialog(thePanel);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			String worldFile;
			try
			{
				FileInputStream fis = new FileInputStream ( selector.getSelectedFile() );
				BufferedReader br = new BufferedReader ( Utility.getBestInputStreamReader ( fis ) );
				//primera linea del fichero de log: fichero de mundo
				worldFile = br.readLine(); 
			}
			catch ( Exception fnfe )
			{
				worldFile = "";
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);


			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( worldFile );
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread()
			{
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() , null ,selector.getSelectedFile().getAbsolutePath() , thePanel );
				}
			};
			thr.start();

			//new SwingAetheriaGameLoader( new File(worldFile).getParent() , thePanel , true , selector.getSelectedFile().getAbsolutePath() , null, true );

		}
	}

}
