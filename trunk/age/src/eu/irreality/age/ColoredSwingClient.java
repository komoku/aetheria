/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;



import javax.swing.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;
import java.awt.*;

import javax.swing.text.*;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.FancyAttributeSet;
import eu.irreality.age.swing.FancyJTextField;
import eu.irreality.age.swing.FancyJTextPane;
import eu.irreality.age.swing.IconLoader;
import eu.irreality.age.swing.ImagePanel;
import eu.irreality.age.windowing.AGEClientWindow;

import eu.irreality.age.swing.FancyStyledDocument;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;


public class ColoredSwingClient implements MultimediaInputOutputClient
{

	private FancyJTextField elCampoTexto;
	private FancyJTextPane elAreaTexto;
	private JScrollPane elScrolling;
	private SwingEditBoxListener elEscuchador;	
	private Vector gameLog;
	private AGEClientWindow laVentana;

	//for colored output
	private Document doc;
	private MutableAttributeSet atributos = new FancyAttributeSet();
	
	private Color textFieldForeground = Color.black; //colour of the text field when it's responding. the text field will turn to the inactive colour sometimes in real time, then go back to this colour
	private Color textFieldInactiveForeground = Color.red; //colour of the text field when it's not responding, because stuff is loading, or for example in real-time mode it's not our time to enter another command yet.
	private Color keyRequestForeground = Color.black; //colour of the text field in the "waitkeypress" state.
	

	//if deactivated, all input-getting methods will automatically return no input
	//use this only when exiting the client
	private boolean deactivated = false;
	
	
	private String currentInput;
	
	//memoria (doskey)
	private Stack back = new Stack();
	private Stack forward = new Stack();
	
	private VisualConfiguration vc;
	
	private SoundClient sonido;
	
	private Hashtable colorCodesTable = new Hashtable();
	
	/**
	 * If set to true, the text area won't autoscroll to the bottom when text is added.
	 * Autoscrolling to the bottom is nice for sighted used, but not so for the blind using screen readers, 
	 * since they have to manually scroll back so that their readers read the new text.
	 */
	private boolean accessibleScrollMode = false;
	
	public boolean isSoundEnabled()
	{
		return true; //más tarde dar opciones, sound on/off, etc.
	}
	
	//en proxys de clientes remotos, p.ej, se devolvería un SoundClientProxy.
	public SoundClient getSoundClient()
	{
		if ( sonido == null )
			sonido = new AGESoundClient(); //hacerlo Singleton?
		return sonido;	
	}
	
	public FancyJTextPane getTextArea()
	{
		return elAreaTexto;
	}
	
	/**
	 * Will be disconnected if the window is closing.
	 */
	public boolean isDisconnected()
	{
		return deactivated;
	}
	
	public String getColorCode ( String colorType )
	{
		if ( colorType == null ) return "";
		String code = (String) colorCodesTable.get(colorType);
		if ( code == null ) return "";
		else return code;
	}
	
	/**
	 * Deactivate the client, from now on it will always return null immediately on input requests.
	 * This happens when the window is closed.
	 */
	public synchronized void exit()
	{
		deactivated = true;
		if ( this.getSoundClient() != null )
		{
			this.getSoundClient().stopAllSound();
			if ( this.getSoundClient() instanceof AGESoundClient )
			{
			    ((AGESoundClient)this.getSoundClient()).deactivate();			
			}
		}
		notifyAll(); //if we are waiting for input, we just exit
	}
	
	
	private void reformatAllText() //into default foreground color, sadly
	{
		try
		{
			String text = elAreaTexto.getText();
			elAreaTexto.getDocument().remove(0,elAreaTexto.getDocument().getLength());
			write(text);
		}
		catch ( BadLocationException ble )
		{
			System.out.println(ble);
		}
	}
	
	
	public void setDefaultConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","%00CC00%");
		colorCodesTable.put("important","%FFFF00%");
		colorCodesTable.put("information","%FFFF00%");
		colorCodesTable.put("action","%9999FF%");
		colorCodesTable.put("denial","%CC0000%");
		colorCodesTable.put("error","%FF0000%");
		colorCodesTable.put("story","%FFFFFF%");
		colorCodesTable.put("default","%FFFFFF%");
		colorCodesTable.put("input","%AAAAAA%");
		colorCodesTable.put("reset","% %");
		elAreaTexto.setBackground ( Color.black );
		laVentana.getMainPanel().setBackground( elAreaTexto.getBackground() );
		elAreaTexto.setForeground ( Color.white );
		StyleConstants.setForeground(atributos,Color.black);
		StyleConstants.setForeground(atributos,Color.white);
		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
		elCampoTexto.setFont(SwingAetheriaGameLoaderInterface.font.deriveFont((float)24.0));
		
		reformatAllText();
		
		elAreaTexto.repaint();
	}
	



	public void setPergaminoConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","%006600%");
		colorCodesTable.put("important","%555500%");
		colorCodesTable.put("information","%555500%");
		colorCodesTable.put("action","%0000FF%");
		colorCodesTable.put("denial","%C00000%");
		colorCodesTable.put("error","%800000%");
		colorCodesTable.put("story","%000001%");
		colorCodesTable.put("default","%000001%");
		colorCodesTable.put("input","%555555%");
		colorCodesTable.put("reset"," ");
		elAreaTexto.setBackground ( new Color ( 255 , 255 , 211 ) );
		laVentana.getMainPanel().setBackground( elAreaTexto.getBackground() );
		elAreaTexto.setForeground ( Color.black );
		StyleConstants.setForeground(atributos,Color.black);
		
		
		//set goudy medieval where available
		
		String fontName = "Lucida Handwriting Cursiva";
		float fontSize = (float) 16.0;
		Font laFuente = SwingAetheriaGameLoaderInterface.font;
		laFuente = new Font("Serif",Font.PLAIN,(int)fontSize);
			Font[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			Font fuenteElegida;
			for ( int f = 0 ; f < fuentes.length ; f++ )
			{
				System.out.println("FONT: " + fuentes[f].getFontName());
				if ( fuentes[f].getFontName().equalsIgnoreCase(fontName) )
				{
					laFuente = fuentes[f].deriveFont((float)fontSize);
					break;
				}
			}
		elAreaTexto.setFont(laFuente);
		elCampoTexto.setFont(laFuente.deriveFont((float)24.0));
		
		reformatAllText();
		
		elAreaTexto.repaint();
	}
	
	public void setVisualConfiguration ( VisualConfiguration c )
	{
		Enumeration keys = c.getColorKeys();
		while ( keys.hasMoreElements() )
		{
			String key = (String) keys.nextElement();
			colorCodesTable.put(key,c.getColorCode(key));
			System.out.println(getClass()+" putting " + " " + key + " " + c.getColorCode(key));
		}
		elAreaTexto.setBackground ( c.getBackgroundColor() );
		elAreaTexto.setForeground ( c.getForegroundColor() );
		laVentana.getMainPanel().setBackground ( c.getBackgroundColor() ); 
		StyleConstants.setForeground(atributos,c.getForegroundColor());
		elAreaTexto.repaint();
		Font laFuente = c.getFont();
		elAreaTexto.setFont(laFuente);
		//System.err.println("Size matters: " + c.getFont().getSize());
		//elAreaTexto.setFont(c.getFont().deriveFont((float)24.0));
		//elAreaTexto.repaint();
		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(laFuente);
		setCurrentOutputFont(laFuente);
		StyleConstants.setFontFamily((MutableAttributeSet)atributos,laFuente.getFamily());
		StyleConstants.setFontSize((MutableAttributeSet)atributos,laFuente.getSize());
		elCampoTexto.setFont(laFuente.deriveFont((float)24.0));
		this.vc = c;
	}
	
	public VisualConfiguration getVisualConfiguration ( )
	{
		if ( vc != null ) return vc;
		else return new VisualConfiguration();
	}
	
	
	public void addToBackStack ( String s )
	{
		//System.out.println("Addin' string " + s + ": " + ( !(back.isEmpty() || s.equalsIgnoreCase((String)back.peek())) && !s.equalsIgnoreCase("") ) );
		if ( ! ( !back.isEmpty() && s.equalsIgnoreCase((String)back.peek()) ) )
			back.push(s);
	}
	
	public void addToForwardStack ( String s )
	{
		if ( !( !forward.isEmpty() && s.equalsIgnoreCase((String)back.peek())) )
			forward.push(s);
	}
	
	public void forwardStackIntoBackStack ( )
	{
		while ( !forward.isEmpty() )
		{
			Object o = forward.pop();
			if ( !o.equals("") )
				back.push(o);
		}
	}
	
	public void goBack (  )
	{
		if ( ! back.isEmpty() )
		{
			//System.out.println("Back is not empty");
			addToForwardStack ( elCampoTexto.getText() );
			elCampoTexto.setText( (String)back.pop() );
		}
	}
	
	public void goForward (  )
	{
		if ( ! forward.isEmpty() )
		{
			addToBackStack ( elCampoTexto.getText() );
			elCampoTexto.setText( (String)forward.pop() );
		}
	}
	
	public void refreshFocus()
	{
		SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	          elCampoTexto.requestFocus();
	    }
		});
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo ( String s )
	{
		writeTitle(s);
	}

	public void writeTitle ( String s )
	{
		laVentana.setTitle(s);
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo ( String s , int pos )
	{
		writeTitle(s, pos);
	}

	public void writeTitle ( String s , int pos )
	{
		String titAct = laVentana.getTitle();
		StringTokenizer st = new StringTokenizer ( titAct, "-" );
		int npos = 1;
		String result="";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			if ( npos == pos )
			{
				result += s;
			}
			else
			{
				result += tok.trim();
			}
			npos++;
			if ( st.hasMoreTokens() ) result += " - ";
		}
		if ( npos == pos )
		{
			result += (" - " + s);
		}
		laVentana.setTitle(result);
	
	}
	
	public boolean isColorEnabled()
	{
		return true;
	}
	
	public boolean isMemoryEnabled()
	{
		return true;
	}
	
	public boolean isLoggingEnabled()
	{
		return true;
	}
	
	public boolean isTitleEnabled()
	{
		return true;
	}
	
	public ColoredSwingClient ( AGEClientWindow window , FancyJTextField nCampo , JScrollPane scrolling, FancyJTextPane nArea , Vector gameLog )
	{
		laVentana = window;
		elCampoTexto=nCampo;
		elAreaTexto=nArea;	
		elScrolling=scrolling;
		this.gameLog = gameLog;
		elEscuchador = new SwingEditBoxListener ( elCampoTexto ,  gameLog , this );
		elCampoTexto.addActionListener ( elEscuchador );
		elCampoTexto.addKeyListener ( elEscuchador );
		doc = elAreaTexto.getDocument();
		this.setDefaultConfiguration();
		
		initClientMenu(laVentana);

		
		
		
	}
	
	public void uninitClientMenu ( final AGEClientWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doUninitClientMenu(window);
		else
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doUninitClientMenu(window);
								window.repaint();
							}
						}
				);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public void initClientMenu ( final AGEClientWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doInitClientMenu(window);
		else
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doInitClientMenu(window);
								window.repaint();
							}
						}
				);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public void doUninitClientMenu ( final AGEClientWindow window )
	{
		JMenuBar mb = window.getTheJMenuBar();
		mb.remove(clientConfigurationMenu);
	}
	
	private JMenu clientConfigurationMenu = new JMenu( UIMessages.getInstance().getMessage("csclient.pres") );
	
	//llamado por los constructores
	public void doInitClientMenu ( final AGEClientWindow window )
	{
		JMenu colorConfigurationMenu = new JMenu( UIMessages.getInstance().getMessage("csclient.pres.colorthemes") );
		final JCheckBoxMenuItem fullScreenOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.fullscreen") ,window.isFullScreenMode());
		final JCheckBoxMenuItem soundOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.sound") ,true);
		final JCheckBoxMenuItem accessibleScrollOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.blindacc") ,false);
		JMenuBar mb = window.getTheJMenuBar();
		window.setTheJMenuBar(mb); //nótese el "the", es para que la tenga como atributo. Si luego se quita para el modo fullscreen se puede volver a poner.
		clientConfigurationMenu.add ( colorConfigurationMenu );
		if ( window.supportsFullScreen() )
			clientConfigurationMenu.add ( fullScreenOption );
		clientConfigurationMenu.add(soundOption);
		clientConfigurationMenu.add(accessibleScrollOption);
		JRadioButtonMenuItem itemDefaultJuego = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.game") ,true);
		JRadioButtonMenuItem itemDefault = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.age") ,false);
		JRadioButtonMenuItem itemPergamino = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.parchment") ,false);
		ButtonGroup bg = new ButtonGroup();
		bg.add ( itemDefaultJuego );
		bg.add ( itemDefault );
		bg.add ( itemPergamino );
		itemDefault.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setDefaultConfiguration();
			}
		} );
		itemDefaultJuego.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( window.getMundo() != null )
				{
					VisualConfiguration vc = ( window.getMundo().getVisualConfiguration() );
					if ( vc != null ) 
					{
						setVisualConfiguration ( vc );
						reformatAllText();
					}
				}
			}
		} );
		itemPergamino.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setPergaminoConfiguration();
			}
		} );
		fullScreenOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( fullScreenOption.isSelected() )
				{
					window.setFullScreenMode(true);
				}
				else
				{
					window.setFullScreenMode(false);
				}
			}
		} );
		soundOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( soundOption.isSelected() )
				{
					AGESoundClient asc = (AGESoundClient)getSoundClient();
					asc.activate();
				}
				else
				{
					AGESoundClient asc = (AGESoundClient)getSoundClient();
					asc.deactivate();
				}
			}
		} );
		accessibleScrollOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( accessibleScrollOption.isSelected() )
				{
					accessibleScrollMode = true;
				}
				else
				{
					accessibleScrollMode = false;
				}
			}
		} );
		colorConfigurationMenu.add ( itemDefaultJuego );
		colorConfigurationMenu.add ( itemDefault );
		colorConfigurationMenu.add ( itemPergamino );
		mb.add ( clientConfigurationMenu );
		window.repaint();
	}
	
	//crea él los componentes, añadiéndolos a la ventana S.A.G.L. dada
	public ColoredSwingClient ( AGEClientWindow window , Vector gameLog )
	{
		laVentana = window;
		this.gameLog = gameLog;
		
		//crear todo lo necesario para el cliente, agregandolo a la ventana
		
		laVentana.getMainPanel().setLayout ( new BorderLayout() );
		
		laVentana.update(laVentana.getGraphics());
		laVentana.repaint();
		laVentana.updateNow();
					
		//Thread.yield();

		elAreaTexto = new FancyJTextPane();
					
		laVentana.updateNow();

		elScrolling = new JScrollPane ( elAreaTexto );
		elScrolling.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		//elScrolling.setHorizontalScrollBarPolicy ( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		elScrolling.setBorder(BorderFactory.createEmptyBorder());
		elScrolling.setViewportBorder(BorderFactory.createEmptyBorder());
		elAreaTexto.setForeground(java.awt.Color.white);
		elAreaTexto.setBackground(java.awt.Color.black);
		laVentana.getMainPanel().setBackground(java.awt.Color.black);
			
		//elAreaTexto.setContentType("text/html; charset=iso-8859-1");			

		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
		elAreaTexto.setMargin(new Insets(20,20,10,20));			
					
		elAreaTexto.setVisible(true);
		elScrolling.setVisible(true);
				

		laVentana.getMainPanel().add(elScrolling,BorderLayout.CENTER);
					
		elCampoTexto = new FancyJTextField(200);
		
		/*
		JPanel promptPanel = new JPanel();
		JLabel promptLabel = new JLabel("Prompt: ");
		promptPanel.add(promptLabel);
		promptPanel.setOpaque(false);
		elCampoTexto.add(promptPanel);
		promptPanel.setVisible(true);
		promptLabel.setVisible(true);
		System.err.println("Prompt created");
		*/

		elCampoTexto.setFont(SwingAetheriaGameLoaderInterface.font.deriveFont((float)24.0));
		
		elCampoTexto.setVisible(true);
		laVentana.getMainPanel().add(elCampoTexto,"South");
					
		elCampoTexto.requestFocus();
					
		elCampoTexto.setCaretColor(Color.red);
					
		laVentana.addFocusListener( new FocusListener( )
		{
		    public void focusGained ( FocusEvent evt )
			{
				elCampoTexto.requestFocus();
			}
			public void focusLost ( FocusEvent evt )
			{
							;
			}
		}
		);
		
		elAreaTexto.setEditable(false);
		
		/*
		elAreaTexto.addFocusListener ( new FocusListener ( ) 
		{
			public void focusGained ( FocusEvent evt )
			{
			    Thread thr = new Thread()
			    {
				public void run()
				{
				    try
				    {
					Thread.sleep(2000);
				    } 
				    catch (InterruptedException e)
				    {
					e.printStackTrace();
				    }
				    SwingUtilities.invokeLater( new Thread() 
				    {
					public void run()
					{
					    elCampoTexto.requestFocus();
					}
				    }
				    );
				}
			    };
			    thr.start();
			}
			public void focusLost ( FocusEvent evt )
			{
							;
			}
		});
		*/
		
		elAreaTexto.addMouseListener( new MouseAdapter()
		{
			public void mouseReleased ( MouseEvent evt )
			{
				
				try
				{
					Clipboard clipboard = elAreaTexto.getToolkit().getSystemClipboard();
					String selection = elAreaTexto.getSelectedText();
					clipboard.setContents(new StringSelection(selection),null);	
				}
				catch ( SecurityException se )
				{
					//Running on an applet, can't access the clipboard. Not a big deal.
					;
				}
				
				elCampoTexto.requestFocus();
			}
		}
		);
		
		
		
				
		//addMenus(laVentana);
					
		laVentana.setVisible(true);			
		laVentana.repaint();
		laVentana.updateNow();

		//Thread.yield();
					
		//gameLog = new Vector(); //init game log
		
		elEscuchador = new SwingEditBoxListener ( elCampoTexto ,  gameLog , this );
		elCampoTexto.addActionListener ( elEscuchador );
		elCampoTexto.addKeyListener ( elEscuchador );
		elAreaTexto.setText("--");
		doc = elAreaTexto.getDocument();
		
		
		initClientMenu(laVentana);
		
		laVentana.updateNow();
		
	}
	

	


	//pasa strings de tipo "000000" o "#000000" a color
	//null si no reconocido
	/*
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean;
			if ( colorString.length() > 0 && colorString.charAt(0) == '#' )
				colorClean = colorString.substring(1);
				else colorClean = colorString;
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}
	*/
	
	//gets a Color from a #HHHHHH, HHHHHH, %#HHHHHH% or %HHHHHH% format string
	//null if string unrecognized
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean = colorString;
			if ( colorClean.charAt(0) == '%')
				colorClean = colorClean.substring(1,colorClean.length()-1);
			if ( colorClean.length() > 0 && colorClean.charAt(0) == '#' )
				colorClean = colorClean.substring(1);
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}

	public static String colorToString ( Color color )
	{
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		return (Integer.toString(red,16) + Integer.toString(green,16) + Integer.toString(blue,16));
		
	}


	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}

	
	//private StringBuffer rawText = new StringBuffer(""); //this attribute stores all the text written to the text area WITH color codes included
	
	public void write ( String s )
	{
	    
	    //System.err.println("Selectionan gaems " + elAreaTexto.getSelectionStart() + " " + elAreaTexto.getSelectionEnd() );
		
		if ( s == null )
		{
			write("null");
			return;
		}
		
		//rawText.append(s);
	
		//parse color codes
		StringTokenizer st = new StringTokenizer ( s , "%" );
		boolean iscode = (s.length()>0 && s.charAt(0)=='%');
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			//System.out.println("Token: " + tok );
			if ( iscode )
			{
			//	System.out.println("Code token " + tok );
				if ( tok.equalsIgnoreCase("red") )
					StyleConstants.setForeground(atributos,Color.red);
				else if ( tok.equalsIgnoreCase("green") )
					StyleConstants.setForeground(atributos,Color.green);
				else if ( tok.equalsIgnoreCase("white") )
					StyleConstants.setForeground(atributos,Color.white);
				else if ( tok.equalsIgnoreCase("blue") )
					StyleConstants.setForeground(atributos,Color.blue);	
				else if ( tok.equalsIgnoreCase("yellow") )
					StyleConstants.setForeground(atributos,Color.yellow);
				else if ( tok.equalsIgnoreCase("lightgray") )
					StyleConstants.setForeground(atributos,Color.lightGray);
				else if ( tok.equalsIgnoreCase("magenta") )
					StyleConstants.setForeground(atributos,Color.magenta);	
				else if ( tok.trim().equalsIgnoreCase("") )
				{
					System.out.println(getColorCode("default"));
					System.out.println(stringToColor(getColorCode("default")));
					StyleConstants.setForeground(atributos,stringToColor(getColorCode("default"))); //default
				}
				else
				{
					try 
					{
						String colortok;
						if ( tok.length() > 0 && tok.charAt(0) == '#' )
							colortok = tok.substring(1);
						else colortok = tok;
						int ncolor = Integer.parseInt(colortok,16);
						StyleConstants.setForeground( atributos , new Color ( ncolor ) );
						//System.out.println("Ncolor" + ncolor);
					}
					catch ( NumberFormatException nfe )
					{
						//unrecognized
					
					}
				}
		
		
			}
			else
			{
				try
				{	
					int savedCaret = 0;
					
					if ( accessibleScrollMode )
					{
						savedCaret = elAreaTexto.getCaretPosition(); //save caret position
						elAreaTexto.setCaretPosition(elAreaTexto.getText().length()); //move caret to end of document (in non-accessible mode it will already be at end w/o doing this)
					}	
						
					doc.insertString(elAreaTexto.getText().length(),tok,atributos);
					
					if ( accessibleScrollMode ) elAreaTexto.setCaretPosition(savedCaret); //restore saved caret position for screen readers																									
				}
				catch ( Exception ble )
				{
					System.out.println(ble);
				}
			}
			iscode = !iscode;
		}
		
		
		/*
	
		old, noncolored output
	
		elAreaTexto.append(s);
		//elScrolling.getVerticalScrollBar().setValue(elScrolling.getVerticalScrollBar().getMaximum());	
		*/
		
		elAreaTexto.setCaretColor(Color.red);
		
		//System.out.println(elAreaTexto.getText());
		
		//elAreaTexto.moveCaretPosition(elAreaTexto.getText().length());
		
		//the old scrolling - worked until 2011-09-20, but had some problems with margins
		//elAreaTexto.setCaretPosition(elAreaTexto.getText().length()); //this doesn't select
		
		//better scrolling
		//elScrolling.revalidate();
		
		//if ( !accessibleScrollMode )
		//{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					elAreaTexto.scrollRectToVisible(new Rectangle(0,(int)elAreaTexto.getPreferredSize().getHeight(),10,10));
					//elAreaTexto.setVisible(true);
					elAreaTexto.repaint();
				}
			});
		//}
		

		
		//System.err.println("Selectionan gaems " + elAreaTexto.getSelectionStart() + " " + elAreaTexto.getSelectionEnd() );
	
	}
	
	public void insertIcon ( Icon icon )
	{
	
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
					
		System.out.println("Icon Insert.\n");
		elAreaTexto.insertIcon ( icon ); //I think java auto-preloads.
	
	}
	
	public void insertIcon ( URL location ) { insertIcon ( IconLoader.loadIcon(location) ); }
	public void insertIcon ( String filename ) { insertIcon ( new ImageIcon(filename) ); }
	
	public void insertCenteredIcon ( Icon icon )
	{
		
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
	
		JPanel jp = new JPanel();
		jp.setBackground ( elAreaTexto.getBackground() );
		FlowLayout fl = new FlowLayout();
		fl.setAlignment( FlowLayout.CENTER );
		jp.add ( new JLabel ( icon ) );
		elAreaTexto.insertComponent(jp);
	
	}
	
	public void insertCenteredIcon ( URL location ) { if ( location == null ) return; insertCenteredIcon ( IconLoader.loadIcon( location ) ); }
	public void insertCenteredIcon ( String fileName ) { if ( fileName == null ) return; insertCenteredIcon ( new ImageIcon ( fileName ) ); }
	
	public boolean isGraphicsEnabled()
	{
		return true;
	}
	

	public void loguear ( String s )
	{
		gameLog.addElement ( s );
	}
	
	/**
	 * @deprecated Use {@link #forceInput(String,boolean)} instead
	 */
	public void forzarEntrada ( String s , boolean output_enabled )
	{
		forceInput(s, output_enabled);
	}

	public void forceInput ( String s , boolean output_enabled )
	{
		//2010-06-25: We add this if, behind-the-scenes forced commands should not be added to logs.
		//(e.g. if "leer cartel" forces "mirar cartel", we only need to log "leer cartel",
		//not both - "mirar cartel" will be executed behind the scenes anyway):
		if ( output_enabled )
			gameLog.addElement ( s );
		
		elEscuchador.countCommand();
		if ( output_enabled )
		{	
			write("\n");
			write( getColorCode("input") + echoText + s.trim() + getColorCode("reset") + "\n" );
		}
	}
	
	
	
	public synchronized void waitKeyPress ()
	{
		if ( deactivated ) return;
		//System.err.println("[DN] waitKeyPress() called");
		
		//System.out.println("Keywait");
		elEscuchador.setPressAnyKeyState ( true );
		//System.out.println("Keywait flag set");
		//ponemos un color de fondo algo mas claro
		//Color c1 = elAreaTexto.getBackground();
		//Color c2 = c1.brighter();
		
		//elAreaTexto.setBackground( c2  );

		//System.err.println("[DN] PAK state set: " + elEscuchador.press_any_key + ", going to wait");
		
		try
		{
			//System.out.println("Keywait I said");
			wait();
			//System.out.println("Keywait I yelled");
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);	
		}
		//notified
		
		//System.err.println("[DN] waitKeyPress() returning");
		//elAreaTexto.setBackground( c1 );
		
	}
	
	
	//bloqueante.
	public synchronized String getInput(Player pl)
	{
	
		//System.err.println("[DN] getInput called");
		if ( deactivated ) return null;
		showAfterLogLoad();
		
		setActiveColor();
	
		try
		{
			//System.err.println("[DN] getInput going to wait");
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
		}
		//notified, y cambiado currentInput
		
		//System.err.println("[DN] getInput no longer waiting");
		
		String temp = currentInput ;
		
		currentInput = null;
		
		setInactiveColor();
		
		//System.err.println("[DN] getInput returning " + temp);
		return temp;
		
		
	}
	
	private void setTextFieldForeground ( final Color color )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elCampoTexto.setForeground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elCampoTexto.setForeground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setActiveColor()
	{
		setTextFieldForeground(textFieldForeground);
	}
	
	public void setInactiveColor()
	{
		setTextFieldForeground(textFieldInactiveForeground);
	}
	
	/**
	 * Returns colour in which the "press any key" text is to be drawn.
	 * @return
	 */
	public Color getKeyRequestForeground ( )
	{
		return keyRequestForeground;
	}
	
	/**
	 * Sets colour in which the "press any key" text is to be drawn.
	 */
	public void setKeyRequestForeground ( Color color )
	{
		keyRequestForeground = color;
	}	

	/**
	 * Returns the colour that the input field text is configured to assume when inactive.
	 */
	public Color getInputFieldInactiveForeground ( )
	{
		return textFieldInactiveForeground;
	}
	
	/**
	 * Sets the colour that the input field text should assume when inactive.
	 * This method never changes the field text's colour immediately, changes will only take place in the UI next time the field becomes inactive
	 * (i.e. when the game engine thread is simulating things). 
	 */
	public void setInputFieldInactiveForeground ( final Color color )
	{
		textFieldInactiveForeground = color;
	}
	
	//no bloqueante.
	public synchronized String getRealTimeInput ( Player pl)  
	{
		if ( deactivated ) return null;
		showAfterLogLoad();
		
		String temp = currentInput;
		setActiveColor();
		currentInput = null;
		if ( temp == null ) //log a noop
			loguear("");
		else
			setInactiveColor();
		return temp;
	}
	
	public synchronized void setInputString ( String s )
	{
		currentInput = s;
		notify();
	}
	
	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla ( )
	{
		clearScreen();
	}

	public void clearScreen ( )
	{
		elAreaTexto.setText("");
	}
	
	
	
	//echo configuration
	private boolean echoEnabled = true;
	private String echoText = "Tu orden:  ";
	
	public boolean isEchoEnabled()
	{
		return echoEnabled;
	}
	
	public void setEchoEnabled ( boolean echoEnabled )
	{
		this.echoEnabled = echoEnabled;
	}
	
	public String getEchoText()
	{
		return echoText;
	}
	
	public void setEchoText( String echoText )
	{
		this.echoText = echoText;
	}
	
	//key press text
	private String keyRequestText = UIMessages.getInstance().getMessage("csclient.keyrequest");
		//"Pulsa cualquier tecla...";
	
	public String getKeyRequestText()
	{
		return keyRequestText;
	}
	
	public void setKeyRequestText ( String newText )
	{
		keyRequestText = newText;
	}
	
	//frame management
	private ImagePanel topFrame = null;
	private ImagePanel bottomFrame = null;
	private ImagePanel leftFrame = null;
	private ImagePanel rightFrame = null;
	
	public void removeFrames()
	{
		try {
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					doRemoveFrames();
				}
			}
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void doRemoveFrames()
	{
		topFrame = null;
		bottomFrame = null;
		leftFrame = null;
		rightFrame = null;
		
		//get innermost panel
		Container current = elAreaTexto.getParent();
		while ( !(current instanceof JPanel) )
			current = current.getParent();
		laVentana.setMainPanel((JPanel)current);
		
		laVentana.getMainPanel().revalidate();
		refreshFocus();
	}
	
	public void addFrame ( final int position , final int size )
	{
		try {
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					doAddFrame(position,size);
				}
			}
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void doAddFrame ( int position , int size )
	{
		if ( position == ImageConstants.TOP )
		{
			JPanel newMainPanel = new JPanel();
			topFrame = new ImagePanel();
			topFrame.setBackground(elAreaTexto.getBackground());
			topFrame.setPreferredSize(new Dimension(laVentana.getMainPanel().getWidth(),size));
			topFrame.setMaximumSize(new Dimension(10000,size));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.PAGE_AXIS));
			newMainPanel.add(topFrame);
			newMainPanel.add(laVentana.getMainPanel());
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(topFrame,BorderLayout.NORTH);
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			laVentana.setMainPanel(newMainPanel);
			
		}
		else if ( position == ImageConstants.BOTTOM )
		{
			JPanel newMainPanel = new JPanel();
			bottomFrame = new ImagePanel();
			bottomFrame.setBackground(elAreaTexto.getBackground());
			bottomFrame.setPreferredSize(new Dimension(laVentana.getMainPanel().getWidth(),size));
			bottomFrame.setMaximumSize(new Dimension(10000,size));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.PAGE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(bottomFrame);
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			newMainPanel.add(bottomFrame,BorderLayout.SOUTH);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.LEFT )
		{
			JPanel newMainPanel = new JPanel();
			leftFrame = new ImagePanel();
			leftFrame.setBackground(elAreaTexto.getBackground());
			leftFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			leftFrame.setMaximumSize(new Dimension(size,100000));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(leftFrame);
			newMainPanel.add(laVentana.getMainPanel());
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(leftFrame,BorderLayout.WEST);
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.RIGHT )
		{
			JPanel newMainPanel = new JPanel();
			rightFrame = new ImagePanel();
			rightFrame.setBackground(elAreaTexto.getBackground());
			rightFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			rightFrame.setMaximumSize(new Dimension(size,100000));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(rightFrame);
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			newMainPanel.add(rightFrame,BorderLayout.EAST);
			laVentana.setMainPanel(newMainPanel);
		}
		laVentana.getMainPanel().revalidate();
		//((JComponent)laVentana).revalidate();
		refreshFocus();
	}
	
	/**
	 * Obtains the image frame at a given position.
	 * @param position
	 * @return
	 */
	public ImagePanel getFrame ( int position )
	{
		switch ( position )
		{
			case ImageConstants.TOP : return topFrame;
			case ImageConstants.BOTTOM : return bottomFrame;
			case ImageConstants.LEFT : return leftFrame;
			case ImageConstants.RIGHT : return rightFrame;
			default : return null;
		}
	}
	
	public void showImageInFrame ( String fileName , int position )
	{
		showImageInFrame ( fileName , position , ImagePanel.NO_SCALING );
	}
	public void showImageInFrame ( URL location , int position )
	{
		showImageInFrame ( location , position , ImagePanel.NO_SCALING );
	}
	
	public void showImageInFrame ( Icon icon , int position , int scalingMode )
	{
		ImagePanel theFrame = getFrame ( position );
		if ( theFrame == null ) addFrame ( position , 200 );
		if ( icon == null )
		{
			System.err.println("Called showImageInFrame on a null image. Tried to open nonexistent image file?");
			return;
		}
		theFrame = getFrame ( position ); //{not null}
		theFrame.setImage(icon);
		theFrame.setScalingMode(scalingMode);
		theFrame.repaint();
	}
	
	public void showImageInFrame ( String filename , int position , int scalingMode )
	{
	    //TODO: support svg here (IconLoader.loadIcon).
		showImageInFrame ( new ImageIcon(filename) , position , scalingMode );
	}
	public void showImageInFrame ( URL location , int position , int scalingMode )
	{
		showImageInFrame ( IconLoader.loadIcon(location) , position , scalingMode );
	}
	
	public void showImageInBackground ( String fileName )
	{
		if ( fileName == null ) 
			elAreaTexto.setBackgroundImage(null);
		else
			//TODO: g.setClip(rect.x,rect.y,rect.width,getMargin().top);
			elAreaTexto.setBackgroundImage(new ImageIcon(fileName));
	}
	
	public void showImageInBackground ( URL location )
	{
		if ( location == null ) 
			elAreaTexto.setBackgroundImage(null);
		else
			elAreaTexto.setBackgroundImage(IconLoader.loadIcon(location));
	}
	
	
	
	public void useImage ( URL url , int mode , int location , int scaling )
	{
		if ( mode == ImageConstants.INLINE )
		{
			if ( location == ImageConstants.LEFT )
				insertIcon(url);
			else 
				insertCenteredIcon(url);
		}
		else if ( mode == ImageConstants.BACKGROUND )
			showImageInBackground(url);
		else if ( mode == ImageConstants.FRAME )
			showImageInFrame(url,location,scaling);
	}
	public void useImage ( String fileName , int mode , int location , int scaling )
	{
		useImage ( fileToURL(fileName) , mode , location , scaling );
	}
	
	public void useImage ( String fileName , int mode , int location ) 
	{ 
		useImage(fileName,mode,location,ImageConstants.NO_SCALING);
	}
	public void useImage ( URL url , int mode , int location ) 
	{ 
		useImage(url,mode,location,ImageConstants.NO_SCALING);
	}
	
	public void useImage ( String fileName , int mode )
	{
		useImage(fileToURL(fileName),mode);
	}
	public void useImage ( URL url , int mode )
	{
		if ( mode == ImageConstants.FRAME )
			useImage(url,mode,ImageConstants.TOP);
		else
			useImage(url,mode,ImageConstants.CENTER);
	}
	
	public static URL fileToURL ( String filename )
	{
		try 
		{
			return new File(filename).toURI().toURL();
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void setPrompts ( final String left , final String right )
	{
		try 
		{
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					elCampoTexto.setPrompts(left,right);
				}
			}
			);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static Color parseColor ( String colorString )
	{
		String colorHex;
		if ( colorString.length() > 0 && colorString.charAt(0) == '#' )
			colorHex = colorString.substring(1);
		else colorHex = colorString;
		int ncolor = Integer.parseInt(colorHex,16);
		return new Color ( ncolor );
	}	
	
	/**
	 * High-level method that sets the default colour for the input field.
	 * Do not confuse with setTextFieldForeground which momentarily changes the colour (ignoring the default colour if necessary).
	 * @param color
	 */
	public void setInputFieldForeground ( final Color color )
	{
		textFieldForeground = color;
		setTextFieldForeground(color);
	}
	
	public void setInputFieldBackground ( final Color color )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elCampoTexto.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elCampoTexto.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setOutputAreaBackground ( final Color color )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elAreaTexto.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elAreaTexto.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setScrollBarBackground ( final Color color )
	{
		final JScrollBar jsb = elScrolling.getVerticalScrollBar();
		if ( SwingUtilities.isEventDispatchThread() )
		{
			jsb.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { jsb.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Changes the font to use for the output area.
	 * @param f New font to use for the output area.
	 */
	public void setOutputAreaFont ( final Font f )
	{
		execInDispatchThread ( new Runnable() { public void run() { elAreaTexto.setFont(f); } } );
	}
	
	/**
	 * Changes the font to use for new text outputted to the output area.
	 *  @param f New font to use for new text outputted to the output area.
	 */
	public void setCurrentOutputFont ( final Font f )
	{
		//write("Setting current output font: " + f + "\n");
		//new Throwable().printStackTrace();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
		execInDispatchThread ( new Runnable() { public void run() { 
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
			((FancyAttributeSet)atributos).setFont(f);
			StyleConstants.setFontFamily((MutableAttributeSet)atributos,f.getFamily());
			StyleConstants.setFontSize((MutableAttributeSet)atributos,f.getSize());
			//write("Done the setting to " + f);
			} } );
	}
	
	/**
	 * Sets the output area's font to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setOutputAreaFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			setOutputAreaFont ( fuente.deriveFont((float)fontSize) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the font for new output to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the new output to the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setCurrentOutputFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			Font derived = fuente.deriveFont((float)fontSize);
			setCurrentOutputFont ( derived );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Changes the font to use for the text input field.
	 * @param f New font to use for the text input field.
	 */
	public void setInputFieldFont ( final Font f )
	{
		execInDispatchThread ( new Runnable() { public void run() { elCampoTexto.setFont(f); } } );
	}
	
	/**
	 * Sets the input field's font to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the input field.
	 * @param fontSize Size of the font to use.
	 */
	public void setInputFieldFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			setInputFieldFont ( fuente.deriveFont((float)fontSize) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the input field's font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the input field.
	 * @param fontSize Size of the font to use.
	 */
	public void setInputFieldFont ( URL u , int fontSize )
	{
		try
		{
			setInputFieldFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Sets the output area's font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setOutputAreaFont ( URL u , int fontSize )
	{
		try
		{
			setOutputAreaFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Sets the output area's new output font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the text that we output to the output area from now.
	 * @param fontSize Size of the font to use.
	 */
	public void setCurrentOutputFont ( URL u , int fontSize )
	{
		try
		{
			setCurrentOutputFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public static void execInDispatchThread ( Runnable r )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			r.run();
		}
		else
		{
			try
			{
				//changed from invokeLater, 2011-06-09.
				SwingUtilities.invokeAndWait(r);
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setMargins ( final int top , final int left , final int bottom , final int right , final boolean marginsOnView )
	{
		execInDispatchThread(new Runnable() 
		{ 
			public void run() 
			{ 
				elAreaTexto.setMargin(new Insets(top,left,bottom,right)); /*elCampoTexto.setMargin(new Insets(0,left,0,right));*/ 
				elAreaTexto.setMarginsOnViewableArea(marginsOnView);
			} 
		});
	}
	
	public void setMargins ( final int top , final int left , final int bottom , final int right )
	{
		setMargins ( top , left , bottom , right , true );
	}
	
	
	/*
	public void setOutputAreaForeground ( final Color c )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			StyleConstants.setForeground(atributos,c);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { StyleConstants.setForeground(atributos,c); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	*/
	
	public void setInputFieldBackground ( String s ) { setInputFieldBackground(parseColor(s)); }
	public void setInputFieldForeground ( String s ) { setInputFieldForeground(parseColor(s)); }
	public void setOutputAreaBackground ( String s ) { setOutputAreaBackground(parseColor(s)); }
	//public void setOutputAreaForeground ( String s ) { setOutputAreaForeground(parseColor(s)); }

	public void setScrollBarBackground ( String s ) { setScrollBarBackground(parseColor(s)); }
	
	public void setKeyRequestForeground ( String s ) { setKeyRequestForeground(parseColor(s)); }
	
	
	private boolean processingLog = false;
	
	
	

	
	public void hideForLogLoad()
	{
		processingLog = true;
		if ( laVentana instanceof JFrame )
		{
			//JPanel glass = (JPanel)((JFrame)laVentana).getGlassPane();
			JPanel glass = new JPanel();
			glass.setBackground(Color.WHITE);
			glass.setOpaque(true);
			((JFrame)laVentana).setGlassPane(glass);
			
			glass.removeAll();
			glass.setLayout(new GridLayout(1,1));
			glass.setBackground(Color.WHITE);
			JLabel loadingLabel = new JLabel( UIMessages.getInstance().getMessage("csclient.loadinglog") ,JLabel.CENTER);
			loadingLabel.setBorder(BorderFactory.createLineBorder(Color.black,4));
			loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			loadingLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
			loadingLabel.setHorizontalTextPosition(JLabel.CENTER);
			loadingLabel.setVerticalTextPosition(JLabel.CENTER);
			//loadingLabel.setForeground(this.getVisualConfiguration().getForegroundColor());
			//loadingLabel.setBackground(this.getVisualConfiguration().getBackgroundColor());
			loadingLabel.setFont(loadingLabel.getFont().deriveFont(25.0f));
			glass.add(loadingLabel);
			glass.setVisible(true);
			//laVentana.getMainPanel().setVisible(false);
		}
	}
	
	public void showAfterLogLoad()
	{
		if ( processingLog )
		{
			processingLog = false;
			if ( laVentana instanceof JFrame )
			{
				//laVentana.getMainPanel().setVisible(true);
				JPanel glass = (JPanel)((JFrame)laVentana).getGlassPane();
				glass.setVisible(false);
			}
		}
	}
	
}
