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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.f2o.absurdum.puck.gui.clipboard.CopyAction;
import org.f2o.absurdum.puck.gui.clipboard.CutAction;
import org.f2o.absurdum.puck.gui.clipboard.PasteAction;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.dialog.ExecuteDialog;
import org.f2o.absurdum.puck.gui.dialog.FindEntityDialog;
import org.f2o.absurdum.puck.gui.dialog.IconSizesDialog;
import org.f2o.absurdum.puck.gui.dialog.ShowHideDialog;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.WorldNode;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;

import com.jstatcom.component.JHelpAction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private JToolBar tools;
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
	
	private void refreshTitle()
	{
		if ( editingFileName != null )
			setTitle(Messages.getInstance().getMessage("frame.title") + " ["+editingFileName+"]");
		else
			setTitle(Messages.getInstance().getMessage("frame.title") + " [untitled file]");
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
		    arguments.add("-worldfile");
		    arguments.add(editingFileName);
		    if ( withLog != null )
		    {
			arguments.add("-logfile");
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

			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT,"yes");
			Source s = new DOMSource(d);
			Result r = new StreamResult(f);
			t.transform(s,r);
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
	 * "Save As..." functionality
	 */
	public boolean saveAs (  ) throws Exception
	{
		JFileChooser jfc = new JFileChooser(".");
		int opt = jfc.showSaveDialog(PuckFrame.this);
		if ( opt == JFileChooser.APPROVE_OPTION )
		{
			File f = jfc.getSelectedFile();
			//try
			//{
				Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				d.appendChild(graphPanel.getWorldNode().getAssociatedPanel().getXML(d));
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.INDENT,"yes");
				Source s = new DOMSource(d);
				Result r = new StreamResult(f);
				t.transform(s,r);
				editingFileName = f.toString();
			//	saveMenuItem.setEnabled(true);
				refreshTitle();
			//}
			//catch ( Exception e )
			//{
			//	JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
			//	e.printStackTrace();
			//}
				
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
			JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
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
		int option = JOptionPane.showConfirmDialog(PuckFrame.this,Messages.getInstance().getMessage("confirm.save.text"),Messages.getInstance().getMessage("confirm.save.title"),JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
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
		int option = JOptionPane.showConfirmDialog(PuckFrame.this,Messages.getInstance().getMessage("confirm.saveonexit.text"),Messages.getInstance().getMessage("confirm.saveonexit.title"),JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
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
				JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
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
						JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
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
	 * Instances and shows Puck's main frame.
	 */
	public PuckFrame ()
	{
		super();
		setSize(PuckConfiguration.getInstance().getIntegerProperty("windowWidth"),PuckConfiguration.getInstance().getIntegerProperty("windowHeight"));
		//setSize(600,600);
		maximizeIfPossible();
		//setTitle(Messages.getInstance().getMessage("frame.title"));
		refreshTitle();
		left = new JPanel();
		right = new JPanel();
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,new JScrollPane(right))
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
		split.setDividerLocation(0.60);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.60);
		getContentPane().add(split);


		
		System.out.println(Toolkit.getDefaultToolkit().getBestCursorSize(20,20));
		//it's 32x32. Will have to do it.
		
		//Image img = Toolkit.getDefaultToolkit().createImage( getClass().getResource("addCursor32.png") );
		
		//Image img = Toolkit.getDefaultToolkit().createImage("addCursor32.png");

		
		
		left.setLayout(new BorderLayout());
		
		//right.setLayout(new BoxLayout(right,BoxLayout.LINE_AXIS));
		right.setLayout(new FlowLayout());
		
		propPanel = new PropertiesPanel();
		right.add(propPanel);
		
		graphPanel = new GraphEditingPanel(propPanel);
		graphPanel.setGrid(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("showGrid")).booleanValue());
		graphPanel.setSnapToGrid(Boolean.valueOf(PuckConfiguration.getInstance().getProperty("snapToGrid")).booleanValue());
		propPanel.setGraphEditingPanel(graphPanel);
		tools = new PuckToolBar(graphPanel , propPanel , this);
		left.add(tools,BorderLayout.NORTH);
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
		JMenu fileMenu = new JMenu(Messages.getInstance().getMessage("menu.file"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		saveMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.file.save"));
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
							JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}
				}
				);
		JMenuItem newMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.file.new"));
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		newMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						graphPanel.clear();
						propPanel.clear();
						propPanel.show(graphPanel.getWorldNode());
						editingFileName = null;
						saveMenuItem.setEnabled(false);
						refreshTitle();
					}
				}
		);
		JMenuItem saveAsMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.file.saveas"));
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
							JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						//saveAs(saveMenuItem);
					}
				}
				);
		JMenuItem openMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.file.open"));
		openMenuItem.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						//graphPanel.setVisible(false);
						//propPanel.setVisible(false);
						
						JFileChooser jfc = new JFileChooser(".");
						int opt = jfc.showOpenDialog(PuckFrame.this);
						if ( opt == JFileChooser.APPROVE_OPTION )
						{
							File f = jfc.getSelectedFile();
							try
							{
								openFile(f);
								//propPanel.loadWithou(wn);
							}
							catch ( Exception e )
							{
								JOptionPane.showMessageDialog(PuckFrame.this,e,"Whoops!",JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
						}
						
						//graphPanel.setVisible(true);
						//propPanel.setVisible(true);
					}
				}
				);
		openRecentMenu = new JMenu(Messages.getInstance().getMessage("menu.file.recent"));
		JMenuItem exitMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.file.exit"));
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
		fileMenu.add(newMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(openRecentMenu);
		updateRecentMenu();
		fileMenu.add(new JSeparator());
		saveMenuItem.setEnabled(false);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitMenuItem);
		
		mainMenuBar.add(fileMenu);
		
		
	    /**
	     * Create an Edit menu to support cut/copy/paste.
	     */
		


	        JMenu editMenu = new JMenu(Messages.getInstance().getMessage("menu.edit"));
	        editMenu.setMnemonic(KeyEvent.VK_E);
	        
	        JMenuItem findMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.find.entity"));
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
	        aMenuItem.setText(Messages.getInstance().getMessage("menuaction.cut"));
	        aMenuItem.setMnemonic(KeyEvent.VK_T);
	        editMenu.add(aMenuItem);

	        aMenuItem = new JMenuItem(new CopyAction());
	        aMenuItem.setText(Messages.getInstance().getMessage("menuaction.copy"));
	        aMenuItem.setMnemonic(KeyEvent.VK_C);
	        editMenu.add(aMenuItem);

	        aMenuItem = new JMenuItem(new PasteAction());
	        aMenuItem.setText(Messages.getInstance().getMessage("menuaction.paste"));
	        aMenuItem.setMnemonic(KeyEvent.VK_P);
	        editMenu.add(aMenuItem);

	    mainMenuBar.add(editMenu);

		
		
		JMenu optionsMenu = new JMenu(Messages.getInstance().getMessage("menu.options"));
		JMenu gridMenu = new JMenu(Messages.getInstance().getMessage("menu.options.grid"));
		optionsMenu.add(gridMenu);
		final JCheckBoxMenuItem showGridItem = new JCheckBoxMenuItem(Messages.getInstance().getMessage("menu.options.grid.show"));
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
		final JCheckBoxMenuItem snapToGridItem = new JCheckBoxMenuItem(Messages.getInstance().getMessage("menu.options.grid.snap"));
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
		
		JMenuItem translationModeMenu = new JMenu(Messages.getInstance().getMessage("menu.options.translation"));
		ButtonGroup translationGroup = new ButtonGroup();
		final JRadioButtonMenuItem holdMenuItem = new JRadioButtonMenuItem(Messages.getInstance().getMessage("menu.options.translation.hold"));
		final JRadioButtonMenuItem pushMenuItem = new JRadioButtonMenuItem(Messages.getInstance().getMessage("menu.options.translation.push"));
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
		
		JMenuItem sizesMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.options.iconsizes"));
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
		
		JMenuItem showHideMenuItem = new JMenuItem(Messages.getInstance().getMessage("menu.options.showhide"));
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
		
		mainMenuBar.add(optionsMenu);
		
		
		
		JMenu helpMenu = new JMenu(Messages.getInstance().getMessage("menu.help"));
		JHelpAction.startHelpWorker("help/PUCKHelp.hs");
		JHelpAction helpTocAction = JHelpAction.getShowHelpInstance(Messages.getInstance().getMessage("menu.help.toc"));
		JHelpAction helpContextSensitiveAction = JHelpAction.getTrackInstance(Messages.getInstance().getMessage("menu.help.context"));
		final JMenuItem helpTocMenuItem = new JMenuItem(helpTocAction);
		final JMenuItem helpContextSensitiveMenuItem = new JMenuItem(helpContextSensitiveAction);
		helpMenu.add(helpTocMenuItem);
		helpMenu.add(helpContextSensitiveMenuItem);
		
		mainMenuBar.add(helpMenu);
		
		
		
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
		
		
	}
	
	/**
	 * Shows exit confirmation dialog and exits PUCK if user selects "Yes".
	 */
	public void exitIfConfirmed ( )
	{
		
		int opt = JOptionPane.showConfirmDialog(PuckFrame.this,Messages.getInstance().getMessage("exit.sure.text"),Messages.getInstance().getMessage("exit.sure.title"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		
		if ( opt == JOptionPane.YES_OPTION )
		{
			exit();
		}
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
			}
			PuckConfiguration.getInstance().storeProperties();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void openFile ( File f ) throws TransformerException
	{
		Transformer t = TransformerFactory.newInstance().newTransformer();
		Source s = new StreamSource(f);
		DOMResult r = new DOMResult();
		t.transform(s,r);
		GraphElementPanel.emptyQueue();
		graphPanel.clear();
		propPanel.clear();
		WorldPanel wp = new WorldPanel(graphPanel);
		WorldNode wn = new WorldNode(wp);
		graphPanel.setWorldNode(wn);
		wp.initFromXML(((Document)r.getNode()).getFirstChild());			
		editingFileName = f.toString();
		addRecentFile(f);
		saveMenuItem.setEnabled(true);
		refreshTitle();
	}
	
	public GraphEditingPanel getGraphEditingPanel()
	{
		return graphPanel;
	}
	
	
	public static void main(String[] args) 
	{
		new PuckFrame();
	}
}
