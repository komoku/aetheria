package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.GameInfo;
import eu.irreality.age.PartidaEntry;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;

public class EscuchadorCargarDesdeEstado implements ActionListener
{
	JDesktopPane thePanel;

	public EscuchadorCargarDesdeEstado ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.state.title") );
		selector.setFileFilter ( new FiltroFicheroEstado() ); 

		int returnVal = selector.showOpenDialog(thePanel);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			System.out.println("Nombre: " + selector.getSelectedFile().getAbsolutePath() );

			String moduledir;
			try
			{

				//Obtener fichero de módulo.
				//Warning: hacemos todo el árbol XML para obtener una chorrada.
				//LENTÍSIMO.
				//Menos mal que, en el futuro, diff y al c..ajo...

				org.w3c.dom.Document d = null;


				BufferedReader br = new BufferedReader ( new InputStreamReader ( new FileInputStream (  selector.getSelectedFile()  ) , "ISO-8859-1" ) );
				InputSource is = new InputSource(br);
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				//io.escribir(io.getColorCode("information") + "Obteniendo árbol DOM de los datos XML...\n" + io.getColorCode("reset") );
				d = db.parse(is);

				//obtain the DOM tree root
				org.w3c.dom.Element n = d.getDocumentElement();
				//obtain the information
				if ( n.hasAttribute("worldDir") )
					moduledir = n.getAttribute("worldDir");
				else  throw ( new Exception() ) ;			
			}

			catch ( Exception fnfe )
			{
				moduledir = "";
			}

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			//String worldFile = moduledir + "/world.xml"; //TODO Doesn't have to be world.xml now!
			String worldFile = selector.getSelectedFile().getAbsolutePath();

			//test:
			GameInfo gi = GameInfo.getGameInfoFromFile ( worldFile );
			final PartidaEntry pe = new PartidaEntry ( gi , "noname" , 200 , null , true , true , true );

			Thread thr = new Thread() {
				public void run()
				{
					ServerHandler.getInstance().initPartidaLocal ( pe , ServerHandler.getInstance().getLogWindow() ,selector.getSelectedFile().getAbsolutePath() , null , thePanel );
				}
			};
			thr.start();

			//working:
			//new SwingAetheriaGameLoader( moduledir , thePanel , false , null , selector.getSelectedFile().getAbsolutePath(), true ); //not client is true? yep: don't assign players until second load, stateload (will change w/diff)

		}
	}

}