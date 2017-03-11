package eu.irreality.age.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import eu.irreality.age.FiltroFicheroEstado;
import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;

public class FileSelectorDialogs 
{
	
	//TODO Do the same for log and state files.

	/**
	 * Shows the user a dialog to open a world file.
	 * Returns the chosen path, or null if the dialog was cancelled.
	 * @param parent
	 * @return
	 */
	public static String showOpenWorldDialog( Component parent )
	{
		JFileChooser selector = new JFileChooser( Paths.WORLD_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.new.title") );
		selector.setFileFilter ( new FiltroFicheroMundo() );
		int returnVal = selector.showOpenDialog(parent);
		if ( returnVal == JFileChooser.APPROVE_OPTION ) 
			return selector.getSelectedFile().getAbsolutePath();
		else
			return null;
	}	
	
	/**
	 * Shows the user a dialog to open a log file.
	 * Returns the chosen path, or null if the dialog was cancelled.
	 * @param parent
	 * @return
	 */
	public static String showOpenLogDialog( Component parent )
	{
		File savePath = new File(Paths.SAVE_PATH);
		if ( !savePath.exists() ) savePath.mkdirs();
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.log.title") );
		selector.setFileFilter ( new FiltroFicheroLog() ); 
		int returnVal = selector.showOpenDialog(parent);
		if ( returnVal == JFileChooser.APPROVE_OPTION ) 
			return selector.getSelectedFile().getAbsolutePath();
		else
			return null;
	}	
	
	
	
	/**
	 * Shows the user a dialog to open a state file.
	 * Returns the chosen path, or null if the dialog was cancelled.
	 * @param parent
	 * @return
	 */
	public static String showOpenStateDialog( Component parent )
	{
		File savePath = new File(Paths.SAVE_PATH);
		if ( !savePath.exists() ) savePath.mkdirs();
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle( UIMessages.getInstance().getMessage("dialog.state.title") );
		selector.setFileFilter ( new FiltroFicheroEstado() ); 
		int returnVal = selector.showOpenDialog(parent);
		if ( returnVal == JFileChooser.APPROVE_OPTION ) 
			return selector.getSelectedFile().getAbsolutePath();
		else
			return null;
	}	
	
	
	
	
}
