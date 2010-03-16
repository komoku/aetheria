package eu.irreality.age;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class ClassicalColoredSwingClient implements InputOutputClient
{
	private JTextPane elAreaTexto;
	private JScrollPane elScrolling;
	
	private Vector gameLog;
	private SwingAetheriaGameLoader laVentana;

	//for colored output
	private Document doc;
	private MutableAttributeSet atributos = new SimpleAttributeSet();
	
	
	
	private Hashtable colorCodesTable = new Hashtable();
	

	
	private String currentInput;
	
	//memoria (doskey)
	private Stack back = new Stack();
	private Stack forward = new Stack();

	private SwingTextAreaListener elEscuchador;

	public String getColorCode ( String colorType )
	{
		String code = (String) colorCodesTable.get(colorType.toLowerCase());
		if ( code == null ) return "";
		else return "%"+code+"%";
	}

	public void setDefaultConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","00CC00");
		colorCodesTable.put("important","FFFF00");
		colorCodesTable.put("information","FFFF00");
		colorCodesTable.put("action","9999FF");
		colorCodesTable.put("denial","CC0000");
		colorCodesTable.put("error","FF0000");
		colorCodesTable.put("story","FFFFFF");
		colorCodesTable.put("default","FFFFFF");
		colorCodesTable.put("input","AAAAAA");
		colorCodesTable.put("reset"," ");
		elAreaTexto.setBackground ( Color.black );
		elAreaTexto.setForeground ( Color.white );
		StyleConstants.setForeground(atributos,Color.black);
		elAreaTexto.repaint();
	}

	public boolean isDisconnected()
	{
		return false;
	}

	public void setPergaminoConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","00CC00");
		colorCodesTable.put("important","FFFF00");
		colorCodesTable.put("information","FFFF00");
		colorCodesTable.put("action","9999FF");
		colorCodesTable.put("denial","CC0000");
		colorCodesTable.put("error","FF0000");
		colorCodesTable.put("story","FFFFFF");
		colorCodesTable.put("default","FFFFFF");
		colorCodesTable.put("input","AAAAAA");
		elAreaTexto.setBackground ( Color.yellow );
		elAreaTexto.setForeground ( Color.black );
		elAreaTexto.repaint();
	}




	public String getCommandText()
	{

		String s = elAreaTexto.getText();
		StringTokenizer st = new StringTokenizer(s,"\n",true);
		String tok="";
		String tokant = "";
		while ( st.hasMoreTokens() )
		{
			tokant = tok;
			tok = st.nextToken();
		}
		//if ( tok.length() > 0 )
		//	return tok;
		//else
		//	return tokant;	
		
		if ( tok.indexOf("\n") >= 0 )
		{
			System.out.println("Barraene found, toklen " + tok.length() + " antlen " + tokant.length() );
			return tokant;
		}	
		else
			return tok;	
		
	}
	
	public void setCommandText ( String s )
	{
		//borrar el actual
		//doc.remove(off,len)
		try
		{
			doc.remove(elAreaTexto.getText().length()-getCommandText().length(),getCommandText().length());
			doc.insertString(elAreaTexto.getText().length(),s,atributos);
		}
		catch ( BadLocationException ble )
		{
			System.out.println(ble);
		}
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
	
	
			addToForwardStack ( getCommandText() );
	
	
			setCommandText( (String)back.pop() );
		}
	}
	
	public void goForward (  )
	{
		if ( ! forward.isEmpty() )
		{
			
			
			addToBackStack ( getCommandText() );
			
				
			setCommandText( (String)forward.pop() );
		
		}
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
	
	public ClassicalColoredSwingClient ( SwingAetheriaGameLoader window , JScrollPane scrolling, JTextPane nArea , Vector gameLog )
	{
		laVentana = window;
		elAreaTexto=nArea;	
		elScrolling=scrolling;
		this.gameLog = gameLog;
		elEscuchador = new SwingTextAreaListener ( elAreaTexto ,  gameLog , this );
		//elCampoTexto.addActionListener ( elEscuchador );
		elAreaTexto.addKeyListener ( elEscuchador );
		doc = elAreaTexto.getDocument();
		setDefaultConfiguration();
	}
	
	public ClassicalColoredSwingClient ( SwingAetheriaGameLoader window , Vector gameLog )
	{

		laVentana = window;
		this.gameLog = gameLog;
		
		//crear todo lo necesario para el cliente, agregandolo a la ventana
		
		window.getMainPanel().setLayout ( new BorderLayout() );
		
		window.update(window.getGraphics());
		window.repaint();
		window.updateNow();
					
		Thread.yield();

		elAreaTexto = new JTextPane();
					
		window.updateNow();

		elScrolling = new JScrollPane ( elAreaTexto );
		elScrolling.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		elAreaTexto.setForeground(java.awt.Color.white);
		elAreaTexto.setBackground(java.awt.Color.black);
					
		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
					
		elAreaTexto.setVisible(true);
		elScrolling.setVisible(true);

		window.getMainPanel().add(elScrolling,BorderLayout.CENTER);
							
		window.setJMenuBar( new SwingMenuAetheria(window) );
					
		window.setVisible(true);			
		window.repaint();
		window.updateNow();

		Thread.yield();
					
		//gameLog = new Vector(); //init game log
		
		elEscuchador = new SwingTextAreaListener ( elAreaTexto ,  gameLog , this );
		elAreaTexto.addKeyListener ( elEscuchador );
		elAreaTexto.setText("--");
		doc = elAreaTexto.getDocument();
		
		setDefaultConfiguration();

	}
	
	//pasa strings de tipo "000000" o "#000000" a color
	//null si no reconocido
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

	public void write ( final String s )
	{
		if ( s == null ) 
		{
			write("null");
			return;
		}
		if ( !SwingUtilities.isEventDispatchThread(  ) )
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doWrite(s);
							}
						}
					);
			}
			catch (Exception e )
			{
				e.printStackTrace();
			}
		}
		else
			doWrite(s);
	}
	
	public void doWrite ( String s )
	{
	
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
					StyleConstants.setForeground(atributos,stringToColor(getColorCode("default"))); //default
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
				catch ( BadLocationException ble )
				{
					System.out.println(ble);
				}
			}
			iscode = !iscode;
		}
		
		elAreaTexto.setCaretColor(Color.red);
		elAreaTexto.moveCaretPosition(elAreaTexto.getText().length());
		elAreaTexto.setVisible(true);
		
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
		
		/*
		try
		{
			doc.insertString(elAreaTexto.getText().length(),"\n\n > ",atributos);
		}
		catch ( BadLocationException ble )
		{
			System.out.println(ble);
		}
		*/
		

	
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
			write( getColorCode("input") + " > " + s.trim() + getColorCode("reset") );
		}
	}
	
	public synchronized void waitKeyPress ()
	{
		System.out.println("Keywait");
		elEscuchador.setPressAnyKeyState ( true );
		
		//ponemos un color de fondo algo mas claro
		//Color c1 = elAreaTexto.getBackground();
		//Color c2 = c1.brighter();
		
		//elAreaTexto.setBackground( c2  );
		
		try
		{
			System.out.println("Keywait I said");
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);	
		}
		//notified
		
		//elAreaTexto.setBackground( c1 );
		
	}
	
	/*
	//bloqueante.
	public synchronized String getInput(Player pl)
	{
		try
		{
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
		}
		//notified, y cambiado currentInput
		System.out.println("INPUT GOTTEN: " + currentInput);
		return currentInput;	
	}
	*/
	
	//bloqueante.
	public String getInput(Player pl)
	{
		synchronized(this)
		{
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
			return temp;	
		}
	}

	//no bloqueante.
	public synchronized String getRealTimeInput ( Player pl)  
	{
		String temp = currentInput;
		currentInput = null;
		if ( temp == null ) //log a noop
			loguear("");
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

}
