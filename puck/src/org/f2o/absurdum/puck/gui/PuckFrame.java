/*
 * (c) 2005-2009 Carlos Gómez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 18:54:05
 * as file PuckFrame.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.f2o.absurdum.puck.bsh.BeanShellCodeValidator;
import org.f2o.absurdum.puck.gui.clipboard.CopyAction;
import org.f2o.absurdum.puck.gui.clipboard.CutAction;
import org.f2o.absurdum.puck.gui.clipboard.PasteAction;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.dialog.BeanShellErrorsDialog;
import org.f2o.absurdum.puck.gui.dialog.DocumentationLinkDialog;
import org.f2o.absurdum.puck.gui.dialog.ExecuteDialog;
import org.f2o.absurdum.puck.gui.dialog.ExportAppletDialog;
import org.f2o.absurdum.puck.gui.dialog.FindEntityDialog;
import org.f2o.absurdum.puck.gui.dialog.IconSizesDialog;
import org.f2o.absurdum.puck.gui.dialog.MapColorsDialog;
import org.f2o.absurdum.puck.gui.dialog.ShowHideDialog;
import org.f2o.absurdum.puck.gui.dialog.VerbListFrame;
import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.gui.graph.WorldNode;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.gui.panels.code.JSyntaxBSHCodeFrame;
import org.f2o.absurdum.puck.gui.skin.ImageManager;
import org.f2o.absurdum.puck.gui.templates.WorldTemplateMenuBuilder;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.w3c.dom.Document;

import eu.irreality.age.FiltroFicheroMundo;
import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.windowing.MenuMnemonicOnTheFly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 18:54:05
 */
public class PuckFrame extends JFrame
{

	private JPanel left; 
	private JPanel right; 
	private JSplitPane split; 
	private PuckToolBar tools;
	private GraphEditingPanel graphPanel;
	private PropertiesPanel propPanel;
	private JMenu openRecentMenu;
	private JMenuItem saveMenuItem;
	
	private String editingFileName = null;
	
	private ExecuteDialog ed = null;
	private FindEntityDialog fed = null;
		
	
	/**
	 * Maximizes this frame if supported by the platform.
	 */
	private void maximizeIfPossible()
	{
		int state = getExtendedState();	    
	    state |= Frame.MAXIMIZED_BOTH;    
	    setExtendedState(state);
	}
	
	public void refreshTitle()
	{
		if ( editingFileName != null )
			setTitle(UIMessages.getInstance().getMessage("frame.title") + " ["+editingFileName+"]");
		else
			setTitle(UIMessages.getInstance().getMessage("frame.title") + " [untitled file]");
	}
	
	/**
	 * Call if skin has been changed, to refresh the icons.
	 */
	private void refreshToolBar()
	{
		if ( tools != null )
		{
			tools.unloadActiveTool();
			left.remove(tools);
		}
		tools = new PuckToolBar(graphPanel , propPanel , this);
		left.add(tools,BorderLayout.WEST);
		left.revalidate();
	}
	
	public void setSkin ( String skinName )
	{
		ImageManager.getInstance().setSkin(skinName);
		refreshToolBar();
		CharacterNode.refreshIcon();
		AbstractEntityNode.refreshIcon();
		ItemNode.refreshIcon();
		RoomNode.refreshIcon();
		SpellNode.refreshIcon();
		repaint();
		PuckConfiguration.getInstance().setProperty("skin", skinName);
	}
	
	public void runCurrentFileInAge ( )
	{
	    if ( ed == null )
	    	ed = new ExecuteDialog(this);
	    ed.setVisible(true);
	    
	    /*
		if ( askForSaveOrCancel() )
		{
			String[] str = new String[1];
			str[0] = editingFileName;
			eu.irreality.age.SwingAetheriaGameLoaderInterface.showIfAlreadyOpen(); //could be closed by user, re-show
			eu.irreality.age.SwingAetheriaGameLoaderInterface.main(str);
			eu.irreality.age.SwingAetheriaGameLoaderInterface.setStandalone(false);
		}
		*/
	}
	
	public void runCurrentFileInAge ( boolean onMdi , String withLog )
	{
		    List arguments = new ArrayList();
		    arguments.add("--worldfile");
		    arguments.add(editingFileName);
		    if ( withLog != null )
		    {
			arguments.add("--logfile");
			arguments.add(withLog);
		    }
		    if ( !onMdi )
		    {
			arguments.add("-sdi");
		    }
		    String[] str = (String[]) arguments.toArray(new String[0]);
		    
		    if ( onMdi )
			eu.irreality.age.SwingAetheriaGameLoaderInterface.showIfAlreadyOpen(); //could be closed by user, re-show
		    eu.irreality.age.SwingAetheriaGameLoaderInterface.main(str);
		    if ( onMdi )
			eu.irreality.age.SwingAetheriaGameLoaderInterface.setStandalone(false);
	}
	
	
	/**
	 * Saves the changes in the file the user are editing, provide that the user has already assigned a path to it.
	 * It CANNOT be used if editingFileName (path of the file user is editing) has not set.
	 */
	public void saveChangesInCurrentFile ( ) throws Exception
	{
		File f = new File(editingFileName);
		saveToFile(f);
		editingFileName = f.toString();
		refreshTitle();	
		addRecentFile(f);
	}
	
	private void addRecentFile ( File f )
	{
		PuckConfiguration.getInstance().addRecentFile(f);
		updateRecentMenu();
	}
	
	
	/**
	 * Save the PUCK world to a given file. This is used by saveAs().
	 * @param f Path to the file where the world will be saved.
	 * @throws Exception IO and XML-related exceptions.
	 */
	public void saveToFile ( File f ) throws Exception
	{
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT,"yes");
		Source s = new DOMSource(d);
		Result r = new StreamResult(f);
		t.transform(s,r);
	}
	
	/**
	 * "Save As..." functionality
	 */
	public boolean saveAs (  ) throws Exception
	{
		JFileChooser jfc = new JFileChooser(".");
		FiltroFicheroMundo filter = new FiltroFicheroMundo();
		jfc.setFileFilter(filter);
		int opt = jfc.showSaveDialog(PuckFrame.this);
		if ( opt == JFileChooser.APPROVE_OPTION )
		{
			File f = jfc.getSelectedFile();
			
			if ( jfc.getFileFilter() == filter && !filter.acceptFilename(f) )
			{
				String fileName = f.getAbsolutePath();
			    fileName += ".agw";
			    f = new File(fileName);
			}
			
			saveToFile(f);
			editingFileName = f.toString();
			refreshTitle();
			addRecentFile(f);	
			return true;
		}
		else
			return false;
	}
	
	
	public boolean saveOrSaveAs()
	{
		try
		{
			boolean done;
			if ( editingFileName != null )
			{
				saveChangesInCurrentFile ( );
				done = true;
			}
			else
			{
				done = saveAs();
			}
			return done;
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Asks the user to save the file being edited (before doing an action that needs this). 
	 * @return true if the file has been saved and action can be undertaken, false if the file has not been saved (because user chose not to save it, or because of an exception) and action cannot be undertaken.
	 */
	public boolean askForSaveOrCancel ( )
	{
		int option = JOptionPane.showConfirmDialog(PuckFrame.this,UIMessages.getInstance().getMessage("confirm.save.text"),UIMessages.getInstance().getMessage("confirm.save.title"),JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
		if ( option == JOptionPane.YES_OPTION ) 
		{
		    return saveOrSaveAs();
		}
		else
		{
			return false;
		}
	}
	
	
	/**
	 * What should be run if the user wants to exit PUCK: ask for unmodified file saving confirmation if needed, ask for exit confirmation if needed.
	 */
	public void userExit()
	{
		askSaveExitCancel();
	}
	
	
	/**
	 * Asks the user if she wishes to save the file before exiting PUCK.
	 */
	public void askSaveExitCancel()
	{
		int option = JOptionPane.showConfirmDialog(PuckFrame.this,UIMessages.getInstance().getMessage("confirm.saveonexit.text"),UIMessages.getInstance().getMessage("confirm.saveonexit.title"),JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if ( option == JOptionPane.YES_OPTION ) 
		{
			try
			{
				boolean done;
				if ( editingFileName != null )
				{
					saveChangesInCurrentFile ( );
					done = true;
				}
				else
				{
					done = saveAs();
				}
				if ( done )
					exit();
			}
			catch ( Exception e )
			{
				JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		else if ( option == JOptionPane.NO_OPTION )
		{
			exit();
		}
	}
	
	
	class RecentFileMenuItem extends JMenuItem
	{

		public RecentFileMenuItem ( String path )
		{
			super(path);
			final String thePath = path;
			this.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent evt)
				{
					
					File f = new File(thePath);
					try
					{
						openFile(f);
					}
					catch ( Exception e )
					{
						JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					
				}
				
			});
		}
		
	}
	
	private void updateRecentMenu()
	{
		openRecentMenu.removeAll();
		List recentFiles = PuckConfiguration.getInstance().getRecentFiles();
		boolean thereIsSomething = false;
		for (Iterator iter = recentFiles.iterator(); iter.hasNext();) 
		{
			String element = (String) iter.next();
			openRecentMenu.add(new RecentFileMenuItem(element));
			thereIsSomething = true;
		}
		openRecentMenu.setEnabled(thereIsSomething);
	}
	
	public void showFindEntityDialog ( )
	{
	    if ( fed == null )
	    	fed = new FindEntityDialog(this,false);
	    fed.requestFocus();
	    fed.setVisible(true);
	}
	
	/**
	 * Sets the L&F to:
	 * The default cross-platform look and feel if the passed string is (case-insensitive) "Default",
	 * The system look and feel if the passed string is (case-insensitive) "System",
	 * The first look and feel found containing (case-insensitive) the string in its name (e.g. "Nimbus"), if any.
	 * If the L&F specified is the current L&F, nothing will be done.
	 * @param lookAndFeel
	 */
	public void setLookAndFeel ( String lookAndFeel )
	{
		boolean changed = false;
		try 
		{
			if ( "default".equals(lookAndFeel.toLowerCase()) 
					&& !UIManager.getLookAndFeel().getClass().getName().equals( UIManager.getCrossPlatformLookAndFeelClassName() ) )
			{
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				changed = true;
			}
			else if ( "system".equals(lookAndFeel.toLowerCase()) 
					&& !UIManager.getLookAndFeel().getClass().getName().equals( UIManager.getSystemLookAndFeelClassName() ) )
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
			}
			else
			{
				LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
				for ( int i = 0 ; i < lfs.length ; i++ )
				{
					if ( lfs[i].getName().toLowerCase().contains(lookAndFeel.toLowerCase()) 
						&& !UIManager.getLookAndFeel().getName().toLowerCase().contains(lookAndFeel.toLowerCase())	)
					{
							UIManager.setLookAndFeel(lfs[i].getClassName());
							changed = true;
							break;
					}
				}
			}
		} 
		catch (Exception e) //class not found, instantiation exception, etc. (shouldn't happen)
		{
			e.printStackTrace();
		}
		if ( changed )
		{
			SwingUtilities.updateComponentTreeUI(this);
			PuckConfiguration.getInstance().setProperty("look", lookAndFeel);
		}
	}
	
	/**
	 * Resets the memory of the file that PUCK is currently editing (which determines the name to use for the "Save" menu action
	 * and the availability of that action).
	 */
	public void resetCurrentlyEditingFile()
	{
		editingFileName = null;
		saveMenuItem.setEnabled(false);
	}
	
	/**
	 * Instances and shows Puck's main frame.
	 */
	public PuckFrame ()
	{
				
		super();
		
		setLookAndFeel(PuckConfiguration.getInstance().getProperty("look"));
		
		/*
		LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
		for ( int i = 0 ; i < lfs.length ; i++ )
		{
			if ( lfs[i].getName().toLowerCase().contains("nimbus") )
			{
				try 
				{
					UIManager.setLookAndFeel(lfs[i].getClassName());
				} 
				catch (Exception e) //class not found, instantiation exception, etc. (shouldn't happen)
				{
					e.printStackTrace();
				}

			}
		}
		*/
		
		setSize(PuckConfiguration.getInstance().getIntegerProperty("windowWidth"),PuckConfiguration.getInstance().getIntegerProperty("windowHeight"));
		setLocation(PuckConfiguration.getInstance().getIntegerProperty("windowLocationX"),PuckConfiguration.getInstance().getIntegerProperty("windowLocationY"));
		//setSize(600,600);
		if ( PuckConfiguration.getInstance().getBooleanProperty("windowMaximized") )
			maximizeIfPossible();
		//setTitle(Messages.getInstance().getMessage("frame.title"));
		refreshTitle();
		left = new JPanel();
		right = new JPanel();
		JScrollPane rightScroll = new JScrollPane(right);
		rightScroll.getVerticalScrollBar().setUnitIncrement(16); //faster scrollbar (by default it was very slow, maybe because component inside is not text component!)
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,rightScroll)
		{
			//dynamic resizing of right panel
			/*
			public void setDividerLocation ( int pixels )
			{
					if ( propPanel != null )
					{
						double rightPartSize = getContentPane().getWidth() - pixels - 15;
						System.out.println("rps=" + rightPartSize);
						System.out.println("mnw=" + this.getMinimumSize().getWidth());
						Dimension propPanSize = propPanel.getSize();
						int propPanHeight = 0;
						if (propPanSize != null) propPanHeight = (int) propPanSize.getHeight();
						//propPanel.revalidate();
						System.out.println("h " + propPanHeight);
						//if ( rightPartSize >= propPanel.getMinimumSize().getWidth() )
							propPanel.setPreferredSize(new Dimension((int)rightPartSize,propPanHeight));
							//propPanel.setMinimumSize(new Dimension((int)rightPartSize,propPanHeight));
							//propPanel.setMaximumSize(new Dimension((int)rightPartSize,propPanHeight));
							//propPanel.setSize(new Dimension((int)rightPartSize,propPanHeight));
							propPanel.revalidate();
					}
					super.setDividerLocation(pixels);
			}
			*/
			
		};
		split.setContinuousLayout(true);
		split.setResizeWeight(0.60);
		final int dividerLoc = PuckConfiguration.getInstance().getIntegerProperty("dividerLocation",0);
		
		/*
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
		{
		*/

		/*	
		}
		});
		*/
		split.setOneTouchExpandable(true);
		getContentPane().add(split);


		
		System.out.println(Toolkit.getDefaultToolkit().getBestCursorSize(20,20));
		//it's 32x32. Will have to do it.
		
		//Image img = Toolkit.getDefaultToolkit().createImage( getClass().getResource("addCursor32.png") );
		
		//Image img = Toolkit.getDefaultToolkit().createImage("addCursor32.png");

		
		
		left.setLayout(new BorderLayout());
		
		//right.setLayout(new BoxLayout(right,BoxLayout.LINE_AXIS));
		if ( PuckConfiguration.getInstance().getBooleanProperty("dynamicFormResizing") )
			right.setLayout(new BorderLayout());
		else
			right.setLayout(new FlowLayout());
		
		propPanel = new PropertiesPanel();
		right.add(propPanel);
		
		graphPanel = new GraphEditingPanel(propPanel);
		graphPanel.setGrid(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("showGrid")).booleanValue());
		graphPanel.setSnapToGrid(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("snapToGrid")).booleanValue());
		propPanel.setGraphEditingPanel(graphPanel);
		tools = new PuckToolBar(graphPanel , propPanel , this);
		left.add(tools,BorderLayout.WEST);
		left.add(graphPanel,BorderLayout.CENTER);
		
		
		/*
		Action testAction = 
				new AbstractAction()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						System.out.println("Puck!");
					}
				}
		;
		testAction.putValue(Action.NAME,"Print Puck");
		
		tools.add(testAction);
		*/
		

		/*
		public void saveChanges ( )
		{
			if ( editingFileName == null )
			{
				//save as... code
			}
			else
			{
				File f = new File(editingFileName);
				try
				{
					Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
					Transformer t = TransformerFactory.newInstance().newTransformer();
					Source s = new DOMSource(d);
					Result r = new StreamResult(f);
					t.transform(s,r);
					editingFileName = f.toString();
					refreshTitle();
				}
				catch ( Exception e )
				{
					JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		}
		*/
		

		
		
		JMenuBar mainMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(UIMessages.getInstance().getMessage("menu.file"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		saveMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.save"));
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		saveMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						if ( editingFileName == null ) JOptionPane.showMessageDialog(PuckFrame.this,"File has no name!","Whoops!",JOptionPane.ERROR_MESSAGE);
						/*
						File f = new File(editingFileName);
						try
						{
							Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
							d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
							Transformer t = TransformerFactory.newInstance().newTransformer();
							Source s = new DOMSource(d);
							Result r = new StreamResult(f);
							t.transform(s,r);
							editingFileName = f.toString();
							refreshTitle();
						}
						catch ( Exception e )
						{
							JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						*/
						try
						{
							saveChangesInCurrentFile ( );
						}
						catch ( Exception e )
						{
							JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}
				}
				);
		JMenu newMenu = new JMenu(UIMessages.getInstance().getMessage("menu.file.new"));
		JMenuItem newBlankMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.new.blank"));
		//newBlankMenuItem.setMnemonic(KeyEvent.VK_N);
		newBlankMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						GraphElementPanel.emptyQueue(); //stop deferred loads
						graphPanel.clear();
						propPanel.clear();
						JSyntaxBSHCodeFrame.closeAllInstances();
						WorldPanel wp = new WorldPanel(graphPanel);
						WorldNode wn = new WorldNode(wp);
						graphPanel.setWorldNode(wn);
						propPanel.show(graphPanel.getWorldNode());
						resetCurrentlyEditingFile();
						refreshTitle();
						//revalidate(); //only since java 1.7
						//invalidate();
						//validate();
						split.revalidate(); //JComponents do have it before java 1.7 (not JFrame)
					}
				}
		);
		newMenu.add(newBlankMenuItem);
		JMenu templateMenus = new WorldTemplateMenuBuilder(this).getMenu();
		if ( templateMenus != null )
		{
			for ( int i = 0 ; i < templateMenus.getItemCount() ; i++ )
			{
				if ( i == 0 ) newMenu.add(new JSeparator());
				newMenu.add(templateMenus.getItem(i));
			}
		}
		JMenuItem saveAsMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.saveas"));
		saveAsMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						/*
						JFileChooser jfc = new JFileChooser(".");
						int opt = jfc.showSaveDialog(PuckFrame.this);
						if ( opt == JFileChooser.APPROVE_OPTION )
						{
							File f = jfc.getSelectedFile();
							try
							{
								Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
								d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
								Transformer t = TransformerFactory.newInstance().newTransformer();
								Source s = new DOMSource(d);
								Result r = new StreamResult(f);
								t.transform(s,r);
								editingFileName = f.toString();
								saveMenuItem.setEnabled(true);
								refreshTitle();
							}
							catch ( Exception e )
							{
								JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
						}
						*/
						try
						{
							saveAs();
							saveMenuItem.setEnabled(true);
						}
						catch ( Exception e )
						{
							JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						//saveAs(saveMenuItem);
					}
				}
				);
		JMenuItem openMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.open"));
		openMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						//graphPanel.setVisible(false);
						//propPanel.setVisible(false);
						
						JFileChooser jfc = new JFileChooser(".");
						jfc.setFileFilter( new FiltroFicheroMundo() );
						int opt = jfc.showOpenDialog(PuckFrame.this);
						if ( opt == JFileChooser.APPROVE_OPTION )
						{
							File f = jfc.getSelectedFile();
							openFileOrShowError(f);
						}
						
						//graphPanel.setVisible(true);
						//propPanel.setVisible(true);
					}
				}
				);
		openRecentMenu = new JMenu(UIMessages.getInstance().getMessage("menu.file.recent"));
		JMenuItem exitMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.exit"));
		exitMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						
						/*
						int opt = JOptionPane.showConfirmDialog(PuckFrame.this,Messages.getInstance().getMessage("exit.sure.text"),Messages.getInstance().getMessage("exit.sure.title"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
						
						if ( opt == JOptionPane.YES_OPTION )
							System.exit(0);
						*/
						userExit();
						
					}
				}
				);
		JMenu exportMenu = new JMenu(UIMessages.getInstance().getMessage("menu.file.export"));
		JMenuItem exportAppletMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.file.export.applet"));
		exportAppletMenuItem.addActionListener ( new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						ExportAppletDialog dial = new ExportAppletDialog ( PuckFrame.this );
						dial.setVisible(true);
					}
				}
				);
		exportMenu.add(exportAppletMenuItem);
		
		fileMenu.add(newMenu);
		fileMenu.add(openMenuItem);
		fileMenu.add(openRecentMenu);
		updateRecentMenu();
		fileMenu.add(new JSeparator());
		saveMenuItem.setEnabled(false);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(exportMenu);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitMenuItem);
		
		mainMenuBar.add(fileMenu);
		
		
	    /**
	     * Create an Edit menu to support cut/copy/paste.
	     */
		


	        JMenu editMenu = new JMenu(UIMessages.getInstance().getMessage("menu.edit"));
	        editMenu.setMnemonic(KeyEvent.VK_E);
	        
	        JMenuItem findMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.find.entity"));
	        findMenuItem.addActionListener(new ActionListener()
	        {
	        	public void actionPerformed ( ActionEvent e )
	        	{
	        		showFindEntityDialog();
	        	}
	        }
	        );
	        editMenu.add(findMenuItem);
	        editMenu.add(new JSeparator());

	        JMenuItem aMenuItem = new JMenuItem(new CutAction());
	        aMenuItem.setText(UIMessages.getInstance().getMessage("menuaction.cut"));
	        aMenuItem.setMnemonic(KeyEvent.VK_T);
	        editMenu.add(aMenuItem);

	        aMenuItem = new JMenuItem(new CopyAction());
	        aMenuItem.setText(UIMessages.getInstance().getMessage("menuaction.copy"));
	        aMenuItem.setMnemonic(KeyEvent.VK_C);
	        editMenu.add(aMenuItem);

	        aMenuItem = new JMenuItem(new PasteAction());
	        aMenuItem.setText(UIMessages.getInstance().getMessage("menuaction.paste"));
	        aMenuItem.setMnemonic(KeyEvent.VK_P);
	        editMenu.add(aMenuItem);

	    mainMenuBar.add(editMenu);

		
		
		JMenu optionsMenu = new JMenu(UIMessages.getInstance().getMessage("menu.options"));
		JMenu gridMenu = new JMenu(UIMessages.getInstance().getMessage("menu.options.grid"));
		optionsMenu.add(gridMenu);
		final JCheckBoxMenuItem showGridItem = new JCheckBoxMenuItem(UIMessages.getInstance().getMessage("menu.options.grid.show"));
		showGridItem.setSelected(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("showGrid")).booleanValue());
		gridMenu.add(showGridItem);
		showGridItem.addItemListener ( new ItemListener() 
				{
					public void itemStateChanged ( ItemEvent e )
					{
						if ( e.getStateChange() == ItemEvent.SELECTED )
						{
							graphPanel.setGrid(true);
							PuckConfiguration.getInstance().setProperty("showGrid", "true");
						}
						else if ( e.getStateChange() == ItemEvent.DESELECTED )
						{
							graphPanel.setGrid(false);
							PuckConfiguration.getInstance().setProperty("showGrid", "false");
						}
						graphPanel.repaint();
					}
				});
		final JCheckBoxMenuItem snapToGridItem = new JCheckBoxMenuItem(UIMessages.getInstance().getMessage("menu.options.grid.snap"));
		snapToGridItem.setSelected(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("snapToGrid")).booleanValue());
		gridMenu.add(snapToGridItem);
		snapToGridItem.addItemListener ( new ItemListener() 
				{
					public void itemStateChanged ( ItemEvent e )
					{
						if ( e.getStateChange() == ItemEvent.SELECTED )
						{
							graphPanel.setSnapToGrid(true);
							PuckConfiguration.getInstance().setProperty("snapToGrid", "true");
						}
						else if ( e.getStateChange() == ItemEvent.DESELECTED )
						{
							graphPanel.setSnapToGrid(false);
							PuckConfiguration.getInstance().setProperty("snapToGrid", "false");
						}
						graphPanel.repaint();
					}
				});
		
		JMenuItem translationModeMenu = new JMenu(UIMessages.getInstance().getMessage("menu.options.translation"));
		ButtonGroup translationGroup = new ButtonGroup();
		final JRadioButtonMenuItem holdMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.options.translation.hold"));
		final JRadioButtonMenuItem pushMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.options.translation.push"));
		pushMenuItem.setSelected("push".equals(PuckConfiguration.getInstance().getProperty("translateMode")));
		if (!pushMenuItem.isSelected()) holdMenuItem.setSelected(true);
		translationGroup.add(holdMenuItem);
		translationGroup.add(pushMenuItem);
		holdMenuItem.addItemListener ( new ItemListener() 
		{
			public void itemStateChanged(ItemEvent arg0) 
			{
				if ( holdMenuItem.isSelected() ) PuckConfiguration.getInstance().setProperty("translateMode", "hold");
				else PuckConfiguration.getInstance().setProperty("translateMode", "push");
			}	
		}
		);
		translationModeMenu.add(holdMenuItem);
		translationModeMenu.add(pushMenuItem);
		optionsMenu.add(translationModeMenu);
		
		JMenuItem toolSelectionModeMenu = new JMenu(UIMessages.getInstance().getMessage("menu.options.toolselection"));
		ButtonGroup toolSelectionGroup = new ButtonGroup();
		final JRadioButtonMenuItem oneUseMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.options.toolselection.oneuse"));
		final JRadioButtonMenuItem multipleUseMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.options.toolselection.multipleuse"));
		multipleUseMenuItem.setSelected("multipleUse".equalsIgnoreCase(PuckConfiguration.getInstance().getProperty("toolSelectionMode")));
		if (!multipleUseMenuItem.isSelected()) oneUseMenuItem.setSelected(true);
		toolSelectionGroup.add(oneUseMenuItem);
		toolSelectionGroup.add(multipleUseMenuItem);
		oneUseMenuItem.addItemListener ( new ItemListener() 
		{
			public void itemStateChanged(ItemEvent arg0) 
			{
				if ( oneUseMenuItem.isSelected() ) PuckConfiguration.getInstance().setProperty("toolSelectionMode", "oneUse");
				else PuckConfiguration.getInstance().setProperty("toolSelectionMode", "multipleUse");
			}	
		}
		);
		toolSelectionModeMenu.add(oneUseMenuItem);
		toolSelectionModeMenu.add(multipleUseMenuItem);
		optionsMenu.add(toolSelectionModeMenu);
		
		JMenuItem sizesMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.options.iconsizes"));
		sizesMenuItem.addActionListener( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent e )
					{
						IconSizesDialog dial = new IconSizesDialog ( PuckFrame.this , true );
						dial.setVisible(true);
					}
				}
		);
		optionsMenu.add(sizesMenuItem);
		
		JMenuItem showHideMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.options.showhide"));
		showHideMenuItem.addActionListener( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent e )
					{
						ShowHideDialog dial = new ShowHideDialog ( PuckFrame.this , true );
						dial.setVisible(true);
					}
				}
		);
		optionsMenu.add(showHideMenuItem);
		
		JMenuItem mapColorsMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.options.mapcolors"));
		mapColorsMenuItem.addActionListener( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent e )
					{
						MapColorsDialog dial = new MapColorsDialog ( PuckFrame.this , true );
						dial.setVisible(true);
					}
				}
		);
		optionsMenu.add(mapColorsMenuItem);
		
		String skinList = PuckConfiguration.getInstance().getProperty("availableSkins");
		if ( skinList != null && skinList.trim().length() > 0 )
		{
			JMenu skinsMenu = new JMenu(UIMessages.getInstance().getMessage("menu.skins"));
			StringTokenizer st = new StringTokenizer(skinList,", ");
			ButtonGroup skinButtons = new ButtonGroup();
			while ( st.hasMoreTokens() )
			{
				final String nextSkin = st.nextToken();
				final JRadioButtonMenuItem skinMenuItem = new JRadioButtonMenuItem(nextSkin);
				skinMenuItem.addActionListener( new ActionListener()
				{
					public void actionPerformed ( ActionEvent e )
					{
						setSkin(nextSkin);
						skinMenuItem.setSelected(true);
					}
				}
				);
				if ( nextSkin.equals(PuckConfiguration.getInstance().getProperty("skin")) ) skinMenuItem.setSelected(true);
				skinsMenu.add(skinMenuItem);
				skinButtons.add(skinMenuItem);
			}
			optionsMenu.add(skinsMenu);
		}
		
		JMenu lookFeelMenu = new JMenu(UIMessages.getInstance().getMessage("menu.looks"));
		ButtonGroup lookButtons = new ButtonGroup();
		final JRadioButtonMenuItem defaultLookMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.looks.default"));
		if ( "default".equals(PuckConfiguration.getInstance().getProperty("look")) ) 
		{
			defaultLookMenuItem.setSelected(true);
		}
		lookFeelMenu.add(defaultLookMenuItem);
		lookButtons.add(defaultLookMenuItem);
		defaultLookMenuItem.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				setLookAndFeel("default");
				defaultLookMenuItem.setSelected(true);
			}
		}
		);
		final JRadioButtonMenuItem systemLookMenuItem = new JRadioButtonMenuItem(UIMessages.getInstance().getMessage("menu.looks.system"));
		if ( "system".equals(PuckConfiguration.getInstance().getProperty("look")) ) 
		{
			systemLookMenuItem.setSelected(true);
		}
		lookFeelMenu.add(systemLookMenuItem);
		lookButtons.add(systemLookMenuItem);
		systemLookMenuItem.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				setLookAndFeel("system");
				systemLookMenuItem.setSelected(true);
			}
		}
		);
		String additionalLookList = PuckConfiguration.getInstance().getProperty("additionalLooks");
		if ( additionalLookList != null && additionalLookList.trim().length() > 0 )
		{
			StringTokenizer st = new StringTokenizer(additionalLookList,", ");
			while ( st.hasMoreTokens() )
			{
				final String nextLook = st.nextToken();
				final JRadioButtonMenuItem lookMenuItem = new JRadioButtonMenuItem(nextLook);
				lookMenuItem.addActionListener( new ActionListener()
				{
					public void actionPerformed ( ActionEvent e )
					{
						setLookAndFeel(nextLook);
						lookMenuItem.setSelected(true);
					}
				}
				);
				if ( nextLook.equals(PuckConfiguration.getInstance().getProperty("look")) ) 
				{
					lookMenuItem.setSelected(true);					
				}
				lookFeelMenu.add(lookMenuItem);
				lookButtons.add(lookMenuItem);
			}
		}
		optionsMenu.add(lookFeelMenu);
		
		
		optionsMenu.add(new UILanguageSelectionMenu(this));
		
		mainMenuBar.add(optionsMenu);
		
		
		JMenu toolsMenu = new JMenu(UIMessages.getInstance().getMessage("menu.tools"));
		
		final JMenuItem verbListMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.tools.verblist"));
		verbListMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				WorldPanel wp = (WorldPanel) graphPanel.getWorldNode().getAssociatedPanel();
				VerbListFrame vlf = VerbListFrame.getInstance( wp.getSelectedLanguageCode() );
				vlf.setVisible(true);
			}
		}
		);
		toolsMenu.add(verbListMenuItem);
		
		final JMenuItem validateMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.tools.validatebsh"));
		validateMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				BeanShellCodeValidator bscv = new BeanShellCodeValidator(graphPanel);
				if ( !bscv.validate() )
				{
					BeanShellErrorsDialog bsed = new BeanShellErrorsDialog (PuckFrame.this, bscv.getErrorText());
					bsed.setVisible(true);
					//JOptionPane.showMessageDialog(PuckFrame.this, bscv.getErrorText());
				}
				else
				{
					JOptionPane.showMessageDialog(PuckFrame.this, UIMessages.getInstance().getMessage("bsh.code.ok"), "OK!", JOptionPane.INFORMATION_MESSAGE);
					//JOptionPane.showMessageDialog(PuckFrame.this, bscv.getErrorText());
				}
			}
		}
		);
		toolsMenu.add(validateMenuItem);
		
		mainMenuBar.add(toolsMenu);
		
		
		JMenu helpMenu = new JMenu(UIMessages.getInstance().getMessage("menu.help"));
		//JHelpAction.startHelpWorker("help/PUCKHelp.hs");
		//JHelpAction helpTocAction = JHelpAction.getShowHelpInstance(Messages.getInstance().getMessage("menu.help.toc"));
		//JHelpAction helpContextSensitiveAction = JHelpAction.getTrackInstance(Messages.getInstance().getMessage("menu.help.context"));
		//final JMenuItem helpTocMenuItem = new JMenuItem(helpTocAction);
		//final JMenuItem helpContextSensitiveMenuItem = new JMenuItem(helpContextSensitiveAction);
		//helpMenu.add(helpTocMenuItem);
		//helpMenu.add(helpContextSensitiveMenuItem);
		
		final JMenuItem helpMenuItem = new JMenuItem(UIMessages.getInstance().getMessage("menu.help.toc"));
		helpMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				DocumentationLinkDialog dial = new DocumentationLinkDialog ( PuckFrame.this , true );
				dial.setVisible(true);
			}
		}
		);
		helpMenu.add(helpMenuItem);
		
		
		mainMenuBar.add(helpMenu);
		
		
		MenuMnemonicOnTheFly.setMnemonics(mainMenuBar);
		
		this.setJMenuBar(mainMenuBar);
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener ( new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{
				userExit();
			}
		}
		);
		
		
		propPanel.show(graphPanel.getWorldNode());

		
		setVisible(true);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
		if ( dividerLoc > 0 )
			split.setDividerLocation(dividerLoc);
		else
			split.setDividerLocation(0.60);
			}
		}
		);
		
	}
	
	/**
	 * Shows exit confirmation dialog and exits PUCK if user selects "Yes".
	 */
	public void exitIfConfirmed ( )
	{
		
		int opt = JOptionPane.showConfirmDialog(PuckFrame.this,UIMessages.getInstance().getMessage("exit.sure.text"),UIMessages.getInstance().getMessage("exit.sure.title"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		
		if ( opt == JOptionPane.YES_OPTION )
		{
			exit();
		}
	}
	
	public double getDividerProportionalLocation()
	{
		double max = (double) split.getMaximumDividerLocation();
		double cur = (double) split.getDividerLocation();
		return cur/max;
	}
	
	/**
	 * Exits PUCK, saving the configuration first.
	 */
	public void exit ( )
	{
		try
		{
			if ( (this.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH )
			{
				PuckConfiguration.getInstance().setProperty("windowWidth",String.valueOf(this.getWidth()));
				PuckConfiguration.getInstance().setProperty("windowHeight",String.valueOf(this.getHeight()));
				PuckConfiguration.getInstance().setProperty("windowMaximized","false");
				PuckConfiguration.getInstance().setProperty("windowLocationX",String.valueOf(this.getX()));
				PuckConfiguration.getInstance().setProperty("windowLocationY",String.valueOf(this.getY()));
			}
			else
			{
				PuckConfiguration.getInstance().setProperty("windowMaximized","true");
			}
			PuckConfiguration.getInstance().setProperty("dividerLocation",String.valueOf(split.getDividerLocation()));
			PuckConfiguration.getInstance().storeProperties();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	

	private void openSource ( StreamSource s ) throws TransformerException
	{
		Transformer t = TransformerFactory.newInstance().newTransformer();
		DOMResult r = new DOMResult();
		t.transform(s,r);
		GraphElementPanel.emptyQueue();
		graphPanel.clear();
		propPanel.clear();
		JSyntaxBSHCodeFrame.closeAllInstances();
		WorldPanel wp = new WorldPanel(graphPanel);
		WorldNode wn = new WorldNode(wp);
		graphPanel.setWorldNode(wn);
		wp.initFromXML(((Document)r.getNode()).getFirstChild());	
		//revalidate(); //only since java 1.7
		//invalidate();
		//validate();
		split.revalidate(); //JComponents do have it before java 1.7 (not JFrame)
	}
	
	public void openStream ( InputStream is ) throws TransformerException
	{
		if ( is == null ) throw new NullPointerException("Null stream passed to openStream()");
		StreamSource s = new StreamSource(is);
		openSource(s);
	}
	
	public void openFile ( File f ) throws TransformerException, FileNotFoundException
	{
		/**
		 * Important note: it's important that the StreamSource is obtained directly from the File object and not from a FileInputStream taken from the File.
		 * With the second alternative, systemId is not set and files with XML "includes" don't work.
		 */
		StreamSource s = new StreamSource(f);
		openSource(s);
		editingFileName = f.toString();
		addRecentFile(f);
		saveMenuItem.setEnabled(true);
		refreshTitle();
	}
	
	/**
	 * Tries to open a world file, showing an error dialog if this is not possible.
	 * @param f
	 * @return true if the file was successfully opened, false if there was a problem.
	 */
	public boolean openFileOrShowError ( File f )
	{
		try
		{
			openFile(f);
			return true;
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean openStreamOrShowError ( InputStream is )
	{
		try
		{
			openStream(is);
			return true;
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog(PuckFrame.this,e.getLocalizedMessage(),"Whoops!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	public GraphEditingPanel getGraphEditingPanel()
	{
		return graphPanel;
	}
	
	
	public static void redirectStandardError ( String file )
	{
		File f = new File(file);
		if ( !f.exists() )
		{
			if ( !f.getParentFile().exists() )
			{
				if ( !f.getParentFile().mkdirs() )
				{
					System.err.println("Could not redirect standard error to " + file + ": unable to create directories.");
					return;
				}
			}
		}
		//{f.getParentFile().exists()
		try 
		{
			System.setErr ( new PrintStream ( new FileOutputStream(f,true) ) );
			System.err.println("[" + new Date() + "]");
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("Could not redirect standard error to " + file + ":");
			e.printStackTrace();
		}
	}
	
	
	public static void processCommandLineArguments ( String[] args )
	{
		//parse command line
		
		Option errorLog = OptionBuilder.withArgName( "errorlog" )
        .hasArg()
        .withDescription(  "A file to append the error output to" )
        .withLongOpt( "errorlog" )
        .create( "e" );
		
		Options options = new Options();
		options.addOption( errorLog );
		
		CommandLineParser parser = new GnuParser();

		try
		{
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        String errorLogFile = null;
	        
	        if ( line.hasOption("errorlog") ) errorLogFile = line.getOptionValue("errorlog");
	        
	        //redirect std. error if necessary
	        if ( errorLogFile != null ) redirectStandardError(errorLogFile);
		}
		catch( ParseException exp ) 
		{
	        // oops, something went wrong
	        System.err.println( "Option parsing failed.  Reason: " + exp.getMessage() );
	    }
		
	}
	
	
	public static void main(String[] args) 
	{
		if ( args.length > 0 )
		{
			processCommandLineArguments(args);
		}
		new PuckFrame();
	}
}
