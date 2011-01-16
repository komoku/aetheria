package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
import org.f2o.absurdum.puck.i18n.Messages;

public class BSHCodeFrame extends JFrame
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
	
	final JLabel lineNumLabel = new JLabel(" : ");
	
	
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
	
	private void updateLineNumberLabel()
	{
		int line = getCaretRowPosition(jep); //jep. .getCaretLine();
		//int lineCount = 0; //getCaretColumnPosition(jep); //jep .getLineCount();
		int column = getCaretColumnPosition(jep); //jep.getCaretPosition() - jep .getLineStartOffset(line);
		lineNumLabel.setText((line+1) + " : " + (column+1));
	}
	
	/**
	 * This instance list will be used to close all code frames on closing a world.
	 */
	private static List instances = new ArrayList();
	
	public static void closeAllInstances()
	{
		for ( int i = 0 ; i < instances.size() ; i++ )
		{
			BSHCodeFrame bcf = (BSHCodeFrame) instances.get(i);
			bcf.codePanel.unsetCodeFrame();
			bcf.dispose();
		}
		instances.clear();
	}
	
	//public BSHCodeFrame( String title , JEditorPane toWriteTo )
	public BSHCodeFrame( String title , JEditorPane toWriteTo , String context , BSHCodePanel codePanel )
	{
		//DefaultSyntaxKit.initKit();
		this.codePanel = codePanel;
		this.context = context;
		instances.add(this);
		setTitle(title);
		setSize(600,600);
		externalJep = toWriteTo;
		//jep = new JEditorPane();
		jep = new JEditorPane();
		JScrollPane scrPane = new JScrollPane(jep);
		refresh();
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add ( scrPane , BorderLayout.CENTER );
		JPanel southPanel = new JPanel(); //( new GridLayout(1,5) );
		//southPanel.add(new JPanel());
		//southPanel.add(new JPanel());
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