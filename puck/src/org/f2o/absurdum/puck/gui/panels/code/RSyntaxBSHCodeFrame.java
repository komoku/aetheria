package org.f2o.absurdum.puck.gui.panels.code;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import jsyntaxpane.actions.DocumentSearchData;
import jsyntaxpane.components.Markers;

import org.f2o.absurdum.puck.gui.codeassist.CodeAssistMenuHandler;
import org.f2o.absurdum.puck.gui.codeassist.CodeInsertActionBuilder;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class RSyntaxBSHCodeFrame extends JFrame
{
	
	/**The code editing component in this frame.*/
	private RSyntaxTextArea theTextArea;
	
	/**The code editing component in the form code panel associated with this frame.*/
	private RSyntaxTextArea externalTextArea;
	
	/**Context attribute: specifies which type of panel it is, used to know which code templates are available on menus*/
	private String context;
	
	/**Save button*/
	private JButton savButton = new JButton(UIMessages.getInstance().getMessage("button.sav"));
	
	/**Cancel button*/
	private JButton canButton = new JButton(UIMessages.getInstance().getMessage("button.can"));
	
	/**The associated form code panel*/
	private RSyntaxBSHCodePanel codePanel = null;
	
	
	public String getContext()
	{
		return context;
	}
	
	public void refresh()
	{
		updateFontSize();
		theTextArea.setDocument(externalTextArea.getDocument());
		//restoreSearchDialogs(); //jsyntax-specific
		if ( codePanel != null ) this.setTitle(codePanel.getPanelName());
	}
	
	/**
	 * Label for line numbers
	 */
	private final JLabel lineNumLabel = new JLabel(" : ");
	
	
	/**
	 * Gets the caret row to show in line number label
	 * @param comp
	 * @return
	 */
	public static int getCaretRowPosition(JTextComponent comp) 
	{
		try 
		{
			Rectangle r = comp.modelToView(comp.getCaretPosition());
			if ( r == null ) return 0;
			int y = r.y;
			int line = y; ///getRowHeight(comp);
			int lineHeight = comp.getFontMetrics(comp.getFont()).getHeight();
			int posLine = (y / lineHeight);
			return posLine;
		} 
		catch (BadLocationException e) {}
		return -1;
	}
 
	/**
	 * Gets the caret column to shown in column number label
	 * @param comp
	 * @return
	 */
	public static int getCaretColumnPosition(JTextComponent comp) 
	{
		int offset = comp.getCaretPosition();
		int column;
		try 
		{
			int rowStart = Utilities.getRowStart(comp, offset);
			if ( rowStart < 0 ) return 0;
			column = offset - rowStart;
		} 
		catch (BadLocationException e) 
		{
			column = -1;
		}
		return column;
	}
	
	/**
	 * Updates the line number label
	 */
	private void updateLineNumberLabel()
	{
		int line = getCaretRowPosition(theTextArea);
		int column = getCaretColumnPosition(theTextArea);
		lineNumLabel.setText((line+1) + " : " + (column+1));
	}
	
	/**
	 * This instance list will be used to close all code frames on closing a world.
	 */
	private static List instances = new ArrayList();
	
	/**
	 * Closes all open RSyntaxBSHCodeFrames
	 */
	public static void closeAllInstances()
	{
		for ( int i = 0 ; i < instances.size() ; i++ )
		{
			RSyntaxBSHCodeFrame bcf = (RSyntaxBSHCodeFrame) instances.get(i);
			bcf.codePanel.unsetCodeFrame();
			bcf.dispose();
		}
		instances.clear();
	}
	
	/**
	 * Obtains all the instances. Used for validation.
	 * @return
	 */
	public static List getAllInstances() //used for validation
	{
		return instances;
	}
	
	//public JSyntaxBSHCodeFrame( String title , JEditorPane toWriteTo )
	public RSyntaxBSHCodeFrame( String title , RSyntaxTextArea toWriteTo , String context , RSyntaxBSHCodePanel rSyntaxBSHCodePanel )
	{
		this.codePanel = rSyntaxBSHCodePanel;
		this.context = context;
		instances.add(this);
		setTitle(title);
		setSize(600,600);
		externalTextArea = toWriteTo;
		theTextArea = RSyntaxTextAreaRegistry.getInstance().createLargeTextArea();
		RTextScrollPane scrPane = new RTextScrollPane(theTextArea);
		scrPane.setFoldIndicatorEnabled(true);
		refresh();
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add ( scrPane , BorderLayout.CENTER );
		JPanel southPanel = new JPanel(); //( new GridLayout(1,5) );
		updateLineNumberLabel();
		southPanel.setLayout(new BorderLayout());
		
		JPanel lineNumPanel = new JPanel( );
		lineNumPanel.add(lineNumLabel);
		//lineNumPanel.setMinimumSize(new Dimension(200,100));
		//lineNumPanel.setPreferredSize(new Dimension(40,1));
		southPanel.add(lineNumLabel,BorderLayout.EAST);
		
		//restore this for save, cancel buttons:
		//southPanel.add(savButton);
		//southPanel.add(canButton);
		//southPanel.add(new JPanel());
		//southPanel.add(new JPanel());
		
		getContentPane().add ( southPanel , BorderLayout.SOUTH );
		savButton.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						//Markers.removeMarkers(theTextArea); //this was for jsyntax
						externalTextArea.setDocument(theTextArea.getDocument());
						//saveSearchDialogs(); //this was for jsyntax
						//RSyntaxBSHCodeFrame.this.codePanel.restoreSearchDialogs(); //this was for jsyntax
						setVisible(false);
					}
				}
		);
		canButton.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						//saveSearchDialogs(); //this was for jsyntax
						setVisible(false);
					}
				}
		);
				
		//jep.addPopupMenu(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)));
		
		theTextArea.addCaretListener ( new CaretListener() 
				{

					public void caretUpdate(CaretEvent e) 
					{
						updateLineNumberLabel();
					}
			
				}
		);
		
		

		setDefaults ( theTextArea );
		
		
		updateFontSize();
		theTextArea.add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(theTextArea)));
		
		
		configureMenus ( theTextArea , context );
		
		
		JMenuItem moreFontSize = new JMenuItem(UIMessages.getInstance().getMessage("menu.font.more"));
		JMenuItem lessFontSize = new JMenuItem(UIMessages.getInstance().getMessage("menu.font.less"));
		JMenu fontSize = new JMenu(UIMessages.getInstance().getMessage("menu.font.size"));
		fontSize.add(moreFontSize);
		fontSize.add(lessFontSize);
		moreFontSize.addActionListener(new ActionListener(){
			public void actionPerformed ( ActionEvent evt )
			{
				incrementFontSize();
			}
		});
		lessFontSize.addActionListener(new ActionListener(){
			public void actionPerformed ( ActionEvent evt )
			{
				decrementFontSize();
			}
		});
		
		
		//theTextArea.getComponentPopupMenu().add(fontSize);
		theTextArea.getPopupMenu().add(new JSeparator());
		theTextArea.getPopupMenu().add(fontSize);
		
		updateFontSize();
		
	}
	
	/**
	 * Sets default options for an RSyntaxTextArea.
	 * @param theTextArea
	 */
	public static void setDefaults ( RSyntaxTextArea theTextArea )
	{
		//jep.setContentType("text/java");
		theTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		theTextArea.setCodeFoldingEnabled(true);
	    theTextArea.setAntiAliasingEnabled(true);
	    theTextArea.setMarkOccurrences(true);
	    theTextArea.setTabSize(4);
	    theTextArea.setLineWrap(RSyntaxOption.getInstanceFor("rsyntaxWordWrap").isOptionEnabled());
	    theTextArea.setPaintTabLines(RSyntaxOption.getInstanceFor("rsyntaxShowTabLines").isOptionEnabled());
	    theTextArea.setTabsEmulated(RSyntaxOption.getInstanceFor("rsyntaxTabsEmulated").isOptionEnabled());
	    //theTextArea.setTabsEmulated(true);
	}
	
	/**
	 * Adds the keybindings and menus to an RSyntaxTextArea.
	 * @param theTextArea
	 * @param context
	 */
	public static void configureMenus ( RSyntaxTextArea theTextArea , String context )
	{
	    Action findDialogAction = new RSyntaxShowFindDialogAction(theTextArea);
	    Action replaceDialogAction = new RSyntaxShowReplaceDialogAction(theTextArea);
	    Action findNextAction = new RSyntaxFindNextOrPrevAction(theTextArea,true);
	    Action findPrevAction = new RSyntaxFindNextOrPrevAction(theTextArea,false);
		
		 //find/replace keybindings
	    theTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_DOWN_MASK),"showFindDialog");
	    theTextArea.getActionMap().put("showFindDialog", findDialogAction);
	    theTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H,InputEvent.CTRL_DOWN_MASK),"showReplaceDialog");
	    theTextArea.getActionMap().put("showReplaceDialog", replaceDialogAction);  
	    theTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0),"findNext");
	    theTextArea.getActionMap().put("findNext", findNextAction);
	    theTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F3,InputEvent.SHIFT_DOWN_MASK),"findPrev");
	    theTextArea.getActionMap().put("findPrev", findPrevAction);  
		
		//popup menu options
		theTextArea.getPopupMenu().add(new JSeparator());
		theTextArea.getPopupMenu().add(findDialogAction);
		theTextArea.getPopupMenu().add(replaceDialogAction);
	    theTextArea.getPopupMenu().add(findNextAction);
	    theTextArea.getPopupMenu().add(findPrevAction);
		//getComponentPopupMenu() not supported by RSyntaxTextArea for java 1.4 compatibility reasons, so this won't fly:
		//theTextArea.getComponentPopupMenu().add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(theTextArea)),0);
		//theTextArea.getComponentPopupMenu().add(new JSeparator(),1);
		//and instead we use this:
		theTextArea.getPopupMenu().add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(theTextArea)),0);
		theTextArea.getPopupMenu().add(new JSeparator(),1);
	}
	
	/**
	 * Configure the view options affecting both areas at the same time.
	 * @param frameArea
	 * @param panelArea
	 */
	public static void configureSyncedMenus ( RSyntaxTextArea frameArea , RSyntaxTextArea panelArea )
	{
		
		RSyntaxOptionToggleAction wordWrapToggleAction = RSyntaxOptionToggleAction.getInstanceFor(UIMessages.getInstance().getMessage("rsyntax.wrap"), "rsyntaxWordWrap");
		RSyntaxOptionToggleAction tabLinesToggleAction = RSyntaxOptionToggleAction.getInstanceFor(UIMessages.getInstance().getMessage("rsyntax.tablines"), "rsyntaxShowTabLines");
		RSyntaxOptionToggleAction tabsEmulatedToggleAction = RSyntaxOptionToggleAction.getInstanceFor(UIMessages.getInstance().getMessage("rsyntax.tabs.emulated"), "rsyntaxTabsEmulated");
		
		String[] themeNames = RSyntaxOption.getThemeNames();
		RSyntaxOptionToggleAction[] themeToggleActions = new RSyntaxOptionToggleAction[themeNames.length];
		for ( int i = 0 ; i < themeNames.length ; i++ )
		{
			themeToggleActions[i] = RSyntaxOptionToggleAction.getInstanceFor(themeNames[i],"rsyntaxTheme"+themeNames[i]);
		}
		for ( int i = 0 ; i < themeNames.length ; i++ )
		{
			themeToggleActions[i].setGroup(themeToggleActions);
		}
		
		//No. This is wrong because it was assuming there are only two text areas, where actually there are two PER ENTITY. Need to re-think this.
		
		/*
		//RSyntaxWordWrapAction toggleWordWrapAction = new RSyntaxWordWrapAction ( frameArea , panelArea );
		RSyntaxOptionToggleAction wordWrapToggleAction = new RSyntaxOptionToggleAction ( frameArea , panelArea , "Word Wrap" , "rsyntaxWordWrap" , new RSyntaxOption(){
			public void setOptionEnabled(RSyntaxTextArea ta1,
					RSyntaxTextArea ta2, boolean enabled) 
			{
				System.err.println("Setting line wrap to " + enabled + " on " + ta1 + " and " + ta2);
				Thread.dumpStack();
				ta1.setLineWrap(enabled);
				ta2.setLineWrap(enabled);
			}

			@Override
			public boolean isOptionEnabled(RSyntaxTextArea ta1,
					RSyntaxTextArea ta2) {
				return ta1.getLineWrap();
			}
		});
		*/
		
		RSyntaxTextArea[] areas = new RSyntaxTextArea[] { frameArea , panelArea };
		
		for ( int i = 0 ; i < areas.length ; i++ )
		{
			areas[i].getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_DOWN_MASK),"toggleWordWrap");
			areas[i].getActionMap().put("toggleWordWrap", wordWrapToggleAction);
			
			areas[i].getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_DOWN_MASK),"showTabLines");
			areas[i].getActionMap().put("showTabLines", tabLinesToggleAction);
			
			areas[i].getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),"tabsToSpaces");
			areas[i].getActionMap().put("tabsToSpaces", tabsEmulatedToggleAction);
			
			areas[i].getPopupMenu().add(new JSeparator());
			
			JMenu viewMenu = new JMenu( UIMessages.getInstance().getMessage("rsyntax.view") );
			
			viewMenu.add(wordWrapToggleAction.getCheckBox());
			viewMenu.add(tabLinesToggleAction.getCheckBox());
			viewMenu.add(tabsEmulatedToggleAction.getCheckBox());
			
			JMenu themesMenu = new JMenu( UIMessages.getInstance().getMessage("rsyntax.themes") );
			
			for ( int j = 0 ; j < themeToggleActions.length ; j++ )
				themesMenu.add(themeToggleActions[j].getCheckBox());
			
			viewMenu.add(themesMenu);
			
			areas[i].getPopupMenu().add ( viewMenu );
		}
		
		
		
	}
	
	private void updateFontSize()
	{
		float codeFrameFontSize = getCodeFrameFontSize();
		if ( theTextArea.getFont().getSize() != (int)codeFrameFontSize )
			theTextArea.setFont(theTextArea.getFont().deriveFont((float)codeFrameFontSize));
	}
	
	private static void changeCodeFrameFontSize(float increment)
	{
		float currentSize = getCodeFrameFontSize();
		float newSize = currentSize + increment;
		PuckConfiguration.getInstance().setProperty("codeFrameFontSize", String.valueOf(newSize));
	}
	
	private static float getCodeFrameFontSize()
	{
		float codeFrameFontSize = (float) 18.0;
		String fontSizeProperty = PuckConfiguration.getInstance().getProperty("codeFrameFontSize");
		try
		{
			codeFrameFontSize = Float.valueOf(fontSizeProperty).floatValue();
		}
		catch ( NumberFormatException nfe )
		{
			System.err.println("Warning: invalid value for codeFrameFontSize property, defaulting to 18.0");
		}
		return codeFrameFontSize;
	}
	
	private void incrementFontSize()
	{
		changeCodeFrameFontSize((float)1.0);
		updateFontSize();
	}
	
	private void decrementFontSize()
	{
		changeCodeFrameFontSize((float)-1.0);
		updateFontSize();
	}
	
	public RSyntaxTextArea getTextArea()
	{
		return theTextArea;
	}
	
}