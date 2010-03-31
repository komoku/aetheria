/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;



import javax.swing.*;

import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.text.*;
import java.awt.*;
import javax.swing.text.*;

import eu.irreality.age.swing.ImagePanel;
import eu.irreality.age.windowing.AGEClientWindow;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;


public class ColoredSwingClient implements MultimediaInputOutputClient
{

	private JTextField elCampoTexto;
	private JTextPane elAreaTexto;
	private JScrollPane elScrolling;
	private SwingEditBoxListener elEscuchador;	
	private Vector gameLog;
	private AGEClientWindow laVentana;

	//for colored output
	private Document doc;
	private MutableAttributeSet atributos = new SimpleAttributeSet();
	

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
	
	public boolean isDisconnected()
	{
		return false;
	}
	public String getColorCode ( String colorType )
	{
		String code = (String) colorCodesTable.get(colorType);
		if ( code == null ) return "";
		else return code;
	}
	
	public synchronized void exit()
	{
		deactivated = true;
		notifyAll(); //if we are waiting for input, we just exit
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
		elAreaTexto.setForeground ( Color.white );
		StyleConstants.setForeground(atributos,Color.black);
		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
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
		elAreaTexto.setForeground ( Color.black );
		StyleConstants.setForeground(atributos,Color.white);
		
		
		//set goudy medieval where available
		
		String fontName = "Lucida Handwriting Cursiva";
		float fontSize = (float) 16.0;
		Font laFuente = SwingAetheriaGameLoaderInterface.font;
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
		//laVentana.getMainPanel().setBackground ( c.getBackgroundColor() ); 
		StyleConstants.setForeground(atributos,c.getForegroundColor());
		elAreaTexto.repaint();
		elAreaTexto.setFont(c.getFont());
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
	
	public ColoredSwingClient ( AGEClientWindow window , JTextField nCampo , JScrollPane scrolling, JTextPane nArea , Vector gameLog )
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
	
	private JMenu clientConfigurationMenu = new JMenu("Ver");
	
	//llamado por los constructores
	public void doInitClientMenu ( final AGEClientWindow window )
	{
		JMenu colorConfigurationMenu = new JMenu("Temas de color");
		final JCheckBoxMenuItem fullScreenOption = new JCheckBoxMenuItem("Pantalla completa",window.isFullScreenMode());
		JMenuBar mb = window.getTheJMenuBar();
		window.setTheJMenuBar(mb); //nótese el "the", es para que la tenga como atributo. Si luego se quita para el modo fullscreen se puede volver a poner.
		clientConfigurationMenu.add ( colorConfigurationMenu );
		clientConfigurationMenu.add ( fullScreenOption );
		JRadioButtonMenuItem itemDefaultJuego = new JRadioButtonMenuItem("Por defecto (juego)",true);
		JRadioButtonMenuItem itemDefault = new JRadioButtonMenuItem("Por defecto (AGE)",false);
		JRadioButtonMenuItem itemPergamino = new JRadioButtonMenuItem("Pergamino",false);
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
					if ( vc != null ) setVisualConfiguration ( vc );
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

		elAreaTexto = new JTextPane();
					
		laVentana.updateNow();

		elScrolling = new JScrollPane ( elAreaTexto );
		elScrolling.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		elAreaTexto.setForeground(java.awt.Color.white);
		elAreaTexto.setBackground(java.awt.Color.black);
			
		//elAreaTexto.setContentType("text/html; charset=iso-8859-1");			

		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
		elAreaTexto.setMargin(new Insets(20,20,10,20));			
					
		elAreaTexto.setVisible(true);
		elScrolling.setVisible(true);
				

		laVentana.getMainPanel().add(elScrolling,BorderLayout.CENTER);
					
		elCampoTexto = new JTextField(200);

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
				Clipboard clipboard = elAreaTexto.getToolkit().getSystemClipboard();

				String selection = elAreaTexto.getSelectedText();

				clipboard.setContents(new StringSelection(selection),null);	
				
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

	public void write ( String s )
	{
		
		if ( s == null )
		{
			write("null");
			return;
		}
	
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
					StyleConstants.setForeground(atributos,Color.blue);
				else if ( tok.equalsIgnoreCase("lightgray") )
					StyleConstants.setForeground(atributos,Color.blue);
				else if ( tok.equalsIgnoreCase("magenta") )
					StyleConstants.setForeground(atributos,Color.blue);	
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
					
					doc.insertString(elAreaTexto.getText().length(),tok,atributos);
																													
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
		
		elAreaTexto.moveCaretPosition(elAreaTexto.getText().length());
		elAreaTexto.setVisible(true);
	
	}
	
	public void insertIcon ( String fileName )
	{
	
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
					
		System.out.println("Icon Insert.\n");
		elAreaTexto.insertIcon ( new ImageIcon ( fileName ) ); //I think java auto-preloads.
	
	}
	
	public void insertCenteredIcon ( String fileName )
	{
		
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
	
		JPanel jp = new JPanel();
		jp.setBackground ( elAreaTexto.getBackground() );
		FlowLayout fl = new FlowLayout();
		fl.setAlignment( FlowLayout.CENTER );
		jp.add ( new JLabel ( new ImageIcon ( fileName ) ) );
		elAreaTexto.insertComponent(jp);
	
	}
	
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
		
		System.out.println("Keywait");
		elEscuchador.setPressAnyKeyState ( true );
		System.out.println("Keywait flag set");
		//ponemos un color de fondo algo mas claro
		//Color c1 = elAreaTexto.getBackground();
		//Color c2 = c1.brighter();
		
		//elAreaTexto.setBackground( c2  );
		
		try
		{
			System.out.println("Keywait I said");
			wait();
			System.out.println("Keywait I yelled");
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);	
		}
		//notified
		
		//elAreaTexto.setBackground( c1 );
		
	}
	
	
	//bloqueante.
	public synchronized String getInput(Player pl)
	{
	
		if ( deactivated ) return null;
		
		setTextFieldForeground(Color.black);
	
		try
		{
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
		}
		//notified, y cambiado currentInput
		
		String temp = currentInput ;
		
		currentInput = null;
		
		setTextFieldForeground(Color.red);
		
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

	//no bloqueante.
	public synchronized String getRealTimeInput ( Player pl)  
	{
		if ( deactivated ) return null;
		
		String temp = currentInput;
		setTextFieldForeground(Color.black);
		currentInput = null;
		if ( temp == null ) //log a noop
			loguear("");
		else
			setTextFieldForeground(Color.red);
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
	
	
	//frame management
	private ImagePanel topFrame = null;
	private ImagePanel bottomFrame = null;
	private ImagePanel leftFrame = null;
	private ImagePanel rightFrame = null;
	
	public void addFrame ( int position , int size )
	{
		if ( position == SwingConstants.TOP )
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
		else if ( position == SwingConstants.BOTTOM )
		{
			JPanel newMainPanel = new JPanel();
			bottomFrame = new ImagePanel();
			bottomFrame.setBackground(elAreaTexto.getBackground());
			bottomFrame.setPreferredSize(new Dimension(laVentana.getMainPanel().getWidth(),size));
			bottomFrame.setMaximumSize(new Dimension(10000,size));
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.PAGE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(bottomFrame);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == SwingConstants.LEFT )
		{
			JPanel newMainPanel = new JPanel();
			leftFrame = new ImagePanel();
			leftFrame.setBackground(elAreaTexto.getBackground());
			leftFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			leftFrame.setMaximumSize(new Dimension(size,100000));
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(leftFrame);
			newMainPanel.add(laVentana.getMainPanel());
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == SwingConstants.RIGHT )
		{
			JPanel newMainPanel = new JPanel();
			rightFrame = new ImagePanel();
			rightFrame.setBackground(elAreaTexto.getBackground());
			rightFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			rightFrame.setMaximumSize(new Dimension(size,100000));
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(rightFrame);
			laVentana.setMainPanel(newMainPanel);
		}
		((JComponent)laVentana).revalidate();
		refreshFocus();
	}
	
	private ImagePanel getFrame ( int position )
	{
		switch ( position )
		{
			case SwingConstants.TOP : return topFrame;
			case SwingConstants.BOTTOM : return bottomFrame;
			case SwingConstants.LEFT : return leftFrame;
			case SwingConstants.RIGHT : return rightFrame;
			default : return null;
		}
	}
	
	public void showImageInFrame ( String fileName , int position )
	{
		showImageInFrame ( fileName , position , ImagePanel.NO_SCALING );
	}
	
	public void showImageInFrame ( String fileName , int position , int scalingMode )
	{
		ImagePanel theFrame = getFrame ( position );
		if ( theFrame == null ) addFrame ( position , 200 );
		theFrame = getFrame ( position ); //{not null}
		theFrame.setImage(new ImageIcon(fileName));
		theFrame.setScalingMode(scalingMode);
		theFrame.repaint();
	}


}
