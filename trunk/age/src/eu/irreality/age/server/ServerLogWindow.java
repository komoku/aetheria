package eu.irreality.age.server;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.NullInputOutputClient;
import eu.irreality.age.debug.Debug;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.windowing.AGELoggingWindow;

public class ServerLogWindow extends JInternalFrame implements AGELoggingWindow
{

	java.util.List panesPartidas = new ArrayList();

	JTabbedPane tabbed;
	
	JTextPane tpGeneral;

	public ServerLogWindow()
	{
	
		super(UIMessages.getInstance().getMessage("serverlog.title"),true,true,true,true);
		
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	
		tabbed = new JTabbedPane();
		JPanel tabGeneral = new JPanel();
		tabGeneral.setLayout ( new java.awt.GridLayout(1,1) );
		tpGeneral = new JTextPane();
		tpGeneral.setBackground(java.awt.Color.black);
		tpGeneral.setForeground(java.awt.Color.white);
		tabGeneral.add(new JScrollPane(tpGeneral));
		tpGeneral.setText(UIMessages.getInstance().getMessage("serverlog.globallog")); //Log global:
	
		tabbed.addTab ( "General" , tabGeneral );
	
		getContentPane().add(tabbed);
		
		
		setSize(400,400);
		setVisible(true);
	
	}
	
	//(los logs de partida escriben mediante InputOutputClients)
	public void writeGeneral ( String s ) //escribe sólo en el general
	{
		//prefijar líneas con "general" y colorearlas de amarillo
		MutableAttributeSet atributos = new SimpleAttributeSet();
		try
		{
			StyleConstants.setForeground( atributos , new Color ( Integer.parseInt("FFFF00",16 ) ) );
		}
		catch ( NumberFormatException nfe )
		{
						//unrecognized
					
		}

		
		/*
		String newText = tpGeneral.getText();
		if ( newText.length() > 0 && ( newText.charAt ( newText.length() - 1 ) != '\n' ) )
			newText += "\n";
		*/
		String toAppend = "[General] " + s.trim();
		//newText = newText + toAppend;		
		//tpGeneral.setText( newText ); 		
	
		try
		{
			String curText = tpGeneral.getText();
			if ( curText.length() > 0 && ( curText.charAt( curText.length()-1 ) != '\n' ) )
				tpGeneral.getDocument().insertString(tpGeneral.getText().length(),"\n",null);
			tpGeneral.getDocument().insertString(tpGeneral.getText().length(),toAppend,atributos);
			Debug.println("BY ORBITAL\n");
		}
		catch ( BadLocationException ble )
		{
			System.err.println(ble);
		}
		
	}
	
	public InputOutputClient addTab (  ) //devuelve una E/S para esa partida
	{
		final JTextPane panePartida = new JTextPane();
		panePartida.setBackground(java.awt.Color.black);
		panePartida.setForeground(java.awt.Color.white);
		panesPartidas.add ( panePartida );
		JPanel tabPartida = new JPanel();
		tabPartida.setLayout ( new java.awt.GridLayout(1,1) );
		tabPartida.add ( new JScrollPane(panePartida) );
		tabbed.add ( UIMessages.getInstance().getMessage("server.addgame.game") /*Partida*/ + " " + panesPartidas.size() , tabPartida );
		
		return ( new NullInputOutputClient()
		{
			int id = panesPartidas.size();
			/**
			 * @deprecated Use {@link #write(String)} instead
			 */
			public void escribir ( String s )
			{
				write(s);
			}
			public void write ( String s )
			{
				panePartida.setText( panePartida.getText() + s );
				
				//text pane general: prefijar líneas con la partida
			
				/*
				String newText = tpGeneral.getText();
				if ( newText.charAt ( newText.length() - 1 ) != '\n' )
					newText += "\n";
				
				String toAppend = "[Partida " + id + "] " + s.trim();
				
				newText = newText + toAppend;
				
				tpGeneral.setText( newText ); 
				*/
				
				String toAppend = "[Partida " + id + "] " + s.trim();
				try
				{
					String curText = tpGeneral.getText();
					if ( curText.length() > 0 && ( curText.charAt( curText.length()-1 ) != '\n' ) )
						tpGeneral.getDocument().insertString(tpGeneral.getText().length(),"\n",null);
					tpGeneral.getDocument().insertString(tpGeneral.getText().length(),toAppend,null);
				}
				catch ( BadLocationException ble )
				{
					System.err.println(ble);
				}
			}
		} );
		
	}

	public JMenuBar getTheJMenuBar()
	{
	    return this.getJMenuBar();
	}

	public void setTheJMenuBar(JMenuBar jmb)
	{
	    this.setJMenuBar(jmb);
	}


}
