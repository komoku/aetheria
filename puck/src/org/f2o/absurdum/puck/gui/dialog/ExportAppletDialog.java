package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.i18n.UIMessages;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.util.file.FileUtils;
import eu.irreality.age.windowing.DialogUtils;

public class ExportAppletDialog extends JDialog
{
	
	//the directory to store the libraries in the exported applet
	private static String targetLibDirName = "lib";

	private JButton bNext = new JButton(UIMessages.getInstance().getMessage("button.con"));
	private JButton bClose = new JButton(UIMessages.getInstance().getMessage("button.clo"));
	
	private PuckFrame frame;
	
	private JCheckBox cbMp3 = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.mp3"));
	private JCheckBox cbOgg = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.ogg"));
	private JCheckBox cbSpx = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.spx"));
	private JCheckBox cbMod = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.mod"));
	private JCheckBox cbSvg = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.svg"));
	private JCheckBox cbFrames = new JCheckBox(UIMessages.getInstance().getMessage("cb.use.frames"));
	
	public ExportAppletDialog ( PuckFrame pf )
	{
		super(pf);
		this.frame = pf;
		this.setModal(true);
		this.setResizable(false);
		setTitle(UIMessages.getInstance().getMessage("export.applet.dialogtitle"));
		
		//lay out the components
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		JLabel infoLabel = new JLabel(UIMessages.getInstance().getMessage("applet.multimedia.usage"));
		infoLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		infoLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		getContentPane().add(infoLabel);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0,5)));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new GridLayout(6,1) );
		mainPanel.add(cbMp3);
		mainPanel.add(cbOgg);
		mainPanel.add(cbSpx);
		mainPanel.add(cbMod);
		mainPanel.add(cbSvg);
		mainPanel.add(cbFrames);
		//mainPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("applet.multimedia.usage")));
		
		getContentPane().add(mainPanel);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.LINE_AXIS));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(bNext);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(bClose);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		getContentPane().add(buttonsPanel);
		
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		
		pack();
		this.setLocationRelativeTo(null);
		
		bClose.addActionListener ( new ActionListener() 
		{		
			public void actionPerformed(ActionEvent e) 
			{
				dispose();
			}
		} );
		
		bNext.addActionListener( new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser jfc = new JFileChooser(".");
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int opt = jfc.showSaveDialog(ExportAppletDialog.this);
				if ( opt == JFileChooser.APPROVE_OPTION )
				{
					File f = jfc.getSelectedFile();
					try
					{
						doExport(f);
					}
					catch ( Exception exc )
					{
						JOptionPane.showMessageDialog(ExportAppletDialog.this,exc.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					}
				}
				else
					dispose();	
			}
		} );
		
		DialogUtils.registerEscapeAction(this);
		pack();
		
	}
	
	
	/**
	 * Copies a file with the given name from the AGE library directory to the given target folder.
	 * @param targetFolder Directory to copy the library to.
	 * @param libFileName Name of the library file to copy.
	 * @throws IOException
	 */
	private void copyLibFile ( File targetFolder , String libFileName ) throws IOException
	{
		File libDir = new File (Paths.getWorkingDirectory(),"lib");
		File theSourceFile = new File(libDir,libFileName);
		File theTargetFile = new File(targetFolder,libFileName);
		FileUtils.copyFile(theSourceFile, theTargetFile);
	}
	
	
	/**
	 * Copies a library file and appends the corresponding string to the HTML string specifying libraries.
	 * @param targetFolder
	 * @param libFileName
	 * @param libHtmlString
	 */
	private void exportLib ( File targetFolder , String libFileName , StringBuffer libHtmlStringBuffer ) throws IOException
	{
		copyLibFile ( targetFolder , libFileName );
		libHtmlStringBuffer.append ( "," );
		libHtmlStringBuffer.append ( targetLibDirName );
		libHtmlStringBuffer.append ( "/" );
		libHtmlStringBuffer.append ( libFileName );
	}
	
	/**
	 * Finds a library file whose name starts with a given string.
	 * Throws exception if none matches.
	 * @param folder
	 * @param filenameStart
	 * @return
	 */
	private String findLibFileStartingWith ( final String filenameStart ) throws FileNotFoundException
	{
		File folder = new File (Paths.getWorkingDirectory(),"lib");
		File[] foundFiles = folder.listFiles( new FilenameFilter() 
		{
			public boolean accept(File dir, String name) 
			{
				if ( !name.endsWith(".jar") ) return false;
				if ( !name.startsWith(filenameStart) ) return false;
				return true;
			}
		} );
		if ( foundFiles.length < 1 ) throw new FileNotFoundException(UIMessages.getInstance().getMessage("lib.not.found")+": " +filenameStart);
		else return foundFiles[0].getName();
	}
	
	/**
	 * Export the current world as an applet to be played online.
	 * For this to work, f should either point to a directory, or to a path where a directory can be created.
	 * @param f
	 */
	public void doExport ( File f ) throws Exception
	{
		if ( !f.isDirectory() && f.exists() )
		{
			throw new IOException(UIMessages.getInstance().getMessage("dir.expected.file.found"));
		}
		if ( !f.exists() ) f.mkdir();
		
		//save the world file
		File worldFile = new File(f,"world.xml");
		frame.saveToFile(worldFile);
		
		//copy the needed library files
		File targetLibFile = new File(f,targetLibDirName);
		if ( !targetLibFile.exists() ) targetLibFile.mkdir();
		StringBuffer libHtmlString = new StringBuffer();
		
		exportLib(targetLibFile,findLibFileStartingWith("bsh"),libHtmlString);
		exportLib(targetLibFile,findLibFileStartingWith("commons-cli"),libHtmlString);
		exportLib(targetLibFile,findLibFileStartingWith("basicplayer"),libHtmlString);
		if ( cbMp3.isSelected() || cbOgg.isSelected() || cbSpx.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("commons-logging-api"),libHtmlString);
			exportLib(targetLibFile,findLibFileStartingWith("jl"),libHtmlString);
			exportLib(targetLibFile,findLibFileStartingWith("tritonus-share"),libHtmlString);
		}
		if ( cbOgg.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("jogg"),libHtmlString);
			exportLib(targetLibFile,findLibFileStartingWith("jorbis"),libHtmlString);
			exportLib(targetLibFile,findLibFileStartingWith("vorbisspi"),libHtmlString);
		}
		if ( cbSpx.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("jspeex"),libHtmlString);
		}
		if ( cbMod.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("micromod"),libHtmlString);
		}
		if ( cbMp3.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("mp3spi"),libHtmlString);
		}
		if ( cbSvg.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("svgSalamander"),libHtmlString);
		}
		if ( cbFrames.isSelected() )
		{
			exportLib(targetLibFile,findLibFileStartingWith("miglayout-core"),libHtmlString);
			exportLib(targetLibFile,findLibFileStartingWith("miglayout-swing"),libHtmlString);
		}
		
		//copy the AgeCore.jar file
		File ageCoreSource = new File ( Paths.getWorkingDirectory() , "AgeCore.jar" );
		File ageCoreTarget = new File ( f , "AgeCore.jar" );
		FileUtils.copyFile(ageCoreSource,ageCoreTarget);
		
		//create the html index
		//TODO Continue here.
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/html/applet-template.html");
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader ( new InputStreamReader ( stream , "UTF-8" ) );
		String linea = "";
		while ( linea != null )
		{
			linea = br.readLine();
			if ( linea != null )
			{
				linea = linea.replace("$LIBRARIES",libHtmlString.toString());
				sb.append(linea+"\n");
			}
		}
		File htmlFile = new File( f , "index.html" );
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(htmlFile));
		osw.write(sb.toString());
		osw.close();
		dispose();
	}
	
}
