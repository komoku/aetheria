/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 23-jul-2005 22:27:32
 * as file BSHCodePanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;

import jsyntaxpane.CompoundUndoMan;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.DocumentSearchData;
import jsyntaxpane.components.Markers;

import org.f2o.absurdum.puck.gui.codeassist.CodeAssistMenuHandler;
import org.f2o.absurdum.puck.gui.codeassist.CodeInsertActionBuilder;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJEditTextArea;
import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.JavaTokenMarker;
import org.jedit.syntax.SyntaxDocument;
import org.jedit.syntax.TextAreaDefaults;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


class BSHCodeFrame extends JFrame
{
	
	
	private JEditorPane jep;
	//private EnhancedJEditTextArea jep;
	

	
	//private JEditorPane externalJep;
	private JEditorPane externalJep;
	
	private JButton savButton = new JButton(Messages.getInstance().getMessage("button.sav"));
	private JButton canButton = new JButton(Messages.getInstance().getMessage("button.can"));
	
	//context attribute: specifies which type of panel it is, used to know which code templates are available on menus
	private String context;
	
	private BSHCodePanel codePanel = null;
	
	public String getContext()
	{
		return context;
	}
	
	public void refresh()
	{
		//jep.setText(externalJep.getText());
		updateFontSize();
		jep.setDocument(externalJep.getDocument());
		restoreSearchDialogs();
		if ( codePanel != null ) this.setTitle(codePanel.getPanelName());
		//jep.setText(externalJep.getText());
	}
	
	final JLabel lineNumLabel = new JLabel("line: ");
	
	
	private DocumentSearchData searchData = null;
	private void saveSearchDialogs()
	{
		searchData = (DocumentSearchData)jep.getDocument().getProperty("SearchData");
	}
	private void restoreSearchDialogs()
	{
		jep.getDocument().putProperty("SearchData",searchData);
	}
	
	public static int getCaretRowPosition(JTextComponent comp) {
		try {
			Rectangle r = comp.modelToView(comp.getCaretPosition());
			if ( r == null ) return 0;
			int y = r.y;
			int line = y; ///getRowHeight(comp);
			int lineHeight = comp.getFontMetrics(comp.getFont()).getHeight();
		    int posLine = (y / lineHeight);
		    return posLine;
		} catch (BadLocationException e) {
		}
		return -1;
	}
 
	public static int getCaretColumnPosition(JTextComponent comp) {
		int offset = comp.getCaretPosition();
		int column;
		try {
			column = offset - Utilities.getRowStart(comp, offset);
		} catch (BadLocationException e) {
			column = -1;
		}
		return column;
	}
	
	private void updateLineNumberLabel()
	{
		int line = getCaretRowPosition(jep); //jep. .getCaretLine();
		//int lineCount = 0; //getCaretColumnPosition(jep); //jep .getLineCount();
		int column = getCaretColumnPosition(jep); //jep.getCaretPosition() - jep .getLineStartOffset(line);
		lineNumLabel.setText((line+1) + " : " + (column+1));
	}
	
	//public BSHCodeFrame( String title , JEditorPane toWriteTo )
	public BSHCodeFrame( String title , JEditorPane toWriteTo , String context , BSHCodePanel codePanel )
	{
		//DefaultSyntaxKit.initKit();
		this.codePanel = codePanel;
		this.context = context;
		setTitle(title);
		setSize(600,600);
		externalJep = toWriteTo;
		//jep = new JEditorPane();
		jep = new JEditorPane();
		JScrollPane scrPane = new JScrollPane(jep);
		refresh();
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add ( scrPane , BorderLayout.CENTER );
		JPanel southPanel = new JPanel( new GridLayout(1,5) );
		southPanel.add(new JPanel());
		southPanel.add(new JPanel());
		updateLineNumberLabel();
		southPanel.add(lineNumLabel);
		southPanel.add(savButton);
		southPanel.add(canButton);
		//restore this for save, cancel buttons
		//getContentPane().add ( southPanel , BorderLayout.SOUTH );
		savButton.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						//externalJep.setText(jep.getText());
						Markers.removeMarkers(jep);
						externalJep.setDocument(jep.getDocument());
						//jep.getDocument().putProperty("SearchData",null); //with this we discard the find/replace dialog instances associated with the document.
						saveSearchDialogs();
						BSHCodeFrame.this.codePanel.restoreSearchDialogs();
						//TODO: remove the previous line when JSyntaxPane is updated so that dialogs are associated to (document,editor) pairs rather than to documents.
						setVisible(false);
					}
				}
		);
		canButton.addActionListener ( new ActionListener() 
				{
					public void actionPerformed ( ActionEvent evt )
					{
						saveSearchDialogs();
						setVisible(false);
					}
				}
		);
		
		System.out.println("The context: " + context);
		
		//jep.addPopupMenu(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)));
		
		jep.addCaretListener ( new CaretListener() 
				{

					public void caretUpdate(CaretEvent e) 
					{
						
						updateLineNumberLabel();
						//lineNumLabel.setText("line: " + (jep.getCaretLine()+1) + "/" + jep.getLineCount());
						
					}
			
				}
		);
		
		
		jep.setContentType("text/java");
		

		
		//PuckConfiguration.getInstance().setProperty("fontSizeProperty", fontSizeProperty);
		
		updateFontSize();
		jep.add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)));
		
		//jep.getEditorKit().
		
		jep.getComponentPopupMenu().add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)),0);
		jep.getComponentPopupMenu().add(new JSeparator(),1);
		
		
		JMenuItem moreFontSize = new JMenuItem(Messages.getInstance().getMessage("menu.font.more"));
		JMenuItem lessFontSize = new JMenuItem(Messages.getInstance().getMessage("menu.font.less"));
		JMenu fontSize = new JMenu(Messages.getInstance().getMessage("menu.font.size"));
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
		
		
		jep.getComponentPopupMenu().add(fontSize);
		

	}
	
	private void updateFontSize()
	{
		float codeFrameFontSize = getCodeFrameFontSize();
		if ( jep.getFont().getSize() != (int)codeFrameFontSize )
			jep.setFont(jep.getFont().deriveFont((float)codeFrameFontSize));
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
	
	
}

/**
 * @author carlos
 *
 * Created at regulus, 23-jul-2005 22:27:32
 */
public class BSHCodePanel extends JPanel
{

	//private JEditorPane jep;
	//private EnhancedJEditTextArea jep;
	private JEditorPane jep;
	private JButton enlButton = new JButton(Messages.getInstance().getMessage("button.enl"));
	private BSHCodeFrame auxFrame;
	
	//context attribute: specifies which type of panel it is, used to know which code templates are available on menus
	private String context;
	
	//entity panel corresponding to this code panel, if any.
	private EntityPanel entityPanel;
	
	final JLabel lineNumLabel = new JLabel("line: ");
	
	/*
	private void updateLineNumberLabel()
	{
		int line = jep.getCaretLine();
		int lineCount = jep.getLineCount();
		int column =  jep.getCaretPosition() - jep.getLineStartOffset(line);
		lineNumLabel.setText((line+1) + " : " + (column+1));
		lineNumLabel.setMinimumSize(new Dimension(200,0));
	}
	*/
	
	
	private void updateLineNumberLabel()
	{
		int line = BSHCodeFrame.getCaretRowPosition(jep); //jep. .getCaretLine();
		//int lineCount = 0; //getCaretColumnPosition(jep); //jep .getLineCount();
		int column = BSHCodeFrame.getCaretColumnPosition(jep); //jep.getCaretPosition() - jep .getLineStartOffset(line);
		lineNumLabel.setText((line+1) + " : " + (column+1));
	}
	
	public String getContext()
	{
		return context;
	}
	
	public BSHCodePanel ( )
	{
		this ( "noContext" );
	}
	
	private DocumentSearchData searchData = null;
	private void saveSearchDialogs()
	{
		searchData = (DocumentSearchData)jep.getDocument().getProperty("SearchData");
	}
	void restoreSearchDialogs()
	{
		jep.getDocument().putProperty("SearchData",searchData);
	}
	
	public BSHCodePanel ( String context )
	{
		this ( context, null );
	}
	
	public String getPanelName()
	{
		if ( entityPanel == null )
			return "Code editing";
		else
			return entityPanel.getName() + " - " + "Code editing";
	}
	
	public BSHCodePanel ( String context , EntityPanel ep )
	{
		
		this.context = context;
		this.entityPanel = ep;
		
		setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("bsh.code")));
		//jep = new JEditorPane();
		
		TextAreaDefaults tad = new TextAreaDefaults();
		tad.rows = 25;
		tad.cols = 80;
		
		DefaultSyntaxKit.initKit();
		
		jep = new JEditorPane();
		//jep = new JEditTextArea(tad);
		
		//jep.setDocument(new SyntaxDocument());
		//jep.setTokenMarker(new JavaTokenMarker()); 
		setLayout(new BorderLayout());
		//JScrollPane jsp = new JScrollPane(jep);
		auxFrame = new BSHCodeFrame(getPanelName(),jep,context,this);
		//jsp.setPreferredSize(new Dimension(120,70));
		//add(jsp,BorderLayout.CENTER);
		
		//jep.setPreferredSize(new Dimension(120,70));
		
		JScrollPane jsp = new JScrollPane(jep);
		jsp.setPreferredSize(new Dimension(120,70));
		
		add(jsp,BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(enlButton,BorderLayout.CENTER);
		
		JPanel lineNumPanel = new JPanel();
		lineNumPanel.add(lineNumLabel);
		lineNumPanel.setMinimumSize(new Dimension(200,100));
		lineNumPanel.setPreferredSize(new Dimension(40,1));
		
		southPanel.add(lineNumPanel,BorderLayout.EAST);
		
		add(southPanel,BorderLayout.SOUTH);
		
		//add(enlButton,BorderLayout.SOUTH);
		
		
		enlButton.addActionListener ( new ActionListener()
				{
					public void actionPerformed ( ActionEvent evt )
					{
						Markers.removeMarkers(jep);
						//jep.getDocument().putProperty("SearchData",null); //with this we discard the find/replace dialog instances associated with the document.
						saveSearchDialogs();
						//TODO: remove the previous line when JSyntaxPane is updated so that dialogs are associated to (document,editor) pairs rather than to documents.
						auxFrame.refresh();
						auxFrame.setVisible(true);
					}
				}
				);
		
		//jep.addPopupMenu(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)));
		
		jep.addCaretListener ( new CaretListener() 
				{

					public void caretUpdate(CaretEvent e) 
					{
						
						updateLineNumberLabel();
						//lineNumLabel.setText("line: " + (jep.getCaretLine()+1) + "/" + jep.getLineCount());
						
					}
			
				}
		);
		
		jep.setContentType("text/java");
		//jep.setFont(jep.getFont().deriveFont((float)18.0));
		jep.getComponentPopupMenu().add(CodeAssistMenuHandler.getInstance().getMenuForContext(context, new CodeInsertActionBuilder(jep)),0);
		jep.getComponentPopupMenu().add(new JSeparator(),1);
		
	}
	
	public Node getXML ( Document d )
	{
		
		if (jep.getText() == null || jep.getText().length() == 0)
			return null;
		
		Element result = d.createElement("Code");
		
		result.setAttribute("language","BeanShell");
		
		/*
		System.err.println(this);
		System.err.println(jep.getText());
		
		String text = jep.getText();
		System.err.println("t1: " + text.indexOf("\n"));
		System.err.println("t2: " + text.indexOf("\n\r"));
		System.err.println("t3: " + text.indexOf("\r"));
		System.err.println("t4: " +text.indexOf("\r\n"));
		System.err.println("t5: " +text.indexOf("\n\n"));
		try
		{
		text = jep.getDocument().getText(0, jep.getDocument().getLength()-1);
		}
		catch ( BadLocationException ble ) {}
		System.err.println("t1: " + text.indexOf("\n"));
		System.err.println("t2: " + text.indexOf("\n\r"));
		System.err.println("t3: " + text.indexOf("\r"));
		System.err.println("t4: " +text.indexOf("\r\n"));
		System.err.println("t5: " +text.indexOf("\n\n"));
		*/
		
		//result.appendChild(d.createCDATASection(jep.getText()));
		
		try
		{
		result.appendChild(d.createCDATASection(jep.getDocument().getText(0, jep.getDocument().getLength())));
		}
		catch ( BadLocationException ble ) { ble.printStackTrace(); }
		
		return result;
		
	}
	
	//from Code node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		NodeList nl = n.getChildNodes();
		String theCode = "";
		//jep.setText("");
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Node child = nl.item(i);
			if ( child instanceof Text )
			{
				theCode = theCode + child.getNodeValue();
				//jep.setText(jep.getText()+child.getNodeValue());
			}
		}
		jep.setText(theCode.trim());
		
		//jep.setContentType("text/java");
		
		/*
		EditorKit ek = jep.getEditorKit();
		ek.deinstall(jep);
		ek.install(jep);
		*/
		((jsyntaxpane.SyntaxDocument)jep.getDocument()).clearUndos();
		/*
		jsyntaxpane.SyntaxDocument doc = (jsyntaxpane.SyntaxDocument)jep.getDocument();
		UndoManager um = (UndoManager) doc.getUndoableEditListeners()[0];
		doc.removeUndoableEditListener(um);
		doc.addUndoableEditListener(new CompoundUndoMan(doc));
		*/
		
	}
	
}
