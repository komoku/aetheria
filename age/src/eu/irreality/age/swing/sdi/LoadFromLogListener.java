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
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;
import eu.irreality.age.swing.FileSelectorDialogs;

class LoadFromLogListener implements ActionListener
{
	SwingSDIInterface window;

	public LoadFromLogListener ( SwingSDIInterface w )
	{
	    this.window = w;
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final String path = FileSelectorDialogs.showOpenLogDialog(window);

		if(path != null) 
		{
			String worldFile;
			try
			{
				FileInputStream fis = new FileInputStream ( path );
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

			window.startGame( worldFile , true , path , null );


			//new SwingAetheriaGameLoader( new File(worldFile).getParent() , thePanel , true , selector.getSelectedFile().getAbsolutePath() , null, true );

		}
	}

}
