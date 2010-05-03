package eu.irreality.age.swing.sdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.server.ServerHandler;

class LoadFromLogListener implements ActionListener
{
	SwingSDIInterface window;

	public LoadFromLogListener ( SwingSDIInterface w )
	{
	    this.window = w;
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle("Selecciona el fichero de log");
		selector.setFileFilter ( new FiltroFicheroLog() ); 

		int returnVal = selector.showOpenDialog(window);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
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

			window.startGame( worldFile , true , selector.getSelectedFile().getAbsolutePath() , null );


			//new SwingAetheriaGameLoader( new File(worldFile).getParent() , thePanel , true , selector.getSelectedFile().getAbsolutePath() , null, true );

		}
	}

}
