package eu.irreality.age;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class CommandLineClient implements InputOutputClient 
{

	private Vector gameLog;
	
	private BufferedReader reader = null;
	private PrintStream writer = null;
	//echo configuration
	private boolean echoEnabled = true;
	private String echoText = "Tu orden:  ";
	private String encoding = "UTF-8";
	
	private boolean rebotFriendly = false;
	private boolean unstrict = false;
	
	private static String getDefaultEncoding()
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if ( osName.contains("win") )
		{
			System.out.println("Windows system detected, setting default console encoding to CP850");
			return "CP850";
		}
		else
		{
			String enc = (new OutputStreamWriter(new ByteArrayOutputStream())).getEncoding();
			System.out.println("Unix system detected, setting default console encoding to " + enc);
			return enc;
		}
	}
	
	public CommandLineClient ( Vector gameLog , boolean rebotFriendly , boolean unstrict )
	{
		this ( gameLog , getDefaultEncoding() , rebotFriendly , unstrict );
	}
	
	public CommandLineClient ( Vector gameLog , String encoding , boolean rebotFriendly , boolean unstrict )
	{
		this.encoding = encoding;
		this.gameLog = gameLog;
		this.rebotFriendly = rebotFriendly;
		this.unstrict = unstrict;
		if ( rebotFriendly ) echoEnabled = false;
		try
		{
			reader = new BufferedReader ( new InputStreamReader ( System.in , encoding ) );
			writer = new PrintStream ( System.out, true , encoding );
			
			writer.println();
			writer.println();
			writer.println("******************************************************************");
			writer.println("*** CheapAGE - Command Line Interface for Aetheria Game Engine ***");
			writer.println("Special CheapAGE commands:");
			if ( unstrict )
				writer.println("save <filename.alf>: save the game");
			else
				writer.println("/save <filename.alf>: save the game");
			if ( unstrict )
				writer.println("quit, exit, fin: quit the game");
			else
				writer.println("/quit, /exit, /fin: quit the game");
			writer.println("******************************************************************");
			writer.println();
			writer.println();
			waitKeyPress();
			
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	
	public void borrarPantalla() 
	{
		clearScreen();
	}

	public void clearScreen() 
	{
		//cannot clear screen in a portable way, so just write lines
		for ( int i = 0 ; i < 25 ; i++ ) writer.println();
	}

	public void escribir(String s) 
	{
		write(s);
	}

	public void escribirTitulo(String s) 
	{
	}

	public void escribirTitulo(String s, int pos) 
	{
	}

	public void forceInput(String s, boolean outputEnabled) 
	{
		gameLog.addElement ( s );
		if ( outputEnabled )
		{	
			write("\n");
			write( getColorCode("input") + echoText + s.trim() + getColorCode("reset") + "\n" );
		}
	}

	public void forzarEntrada(String s, boolean outputEnabled) 
	{
		forceInput(s,outputEnabled);
	}

	public String getColorCode(String name) 
	{
		return "";
	}

	public String getInput(Player pl) 
	{
		try
		{
			if ( echoEnabled )
				writer.print(echoText);
			String line = reader.readLine();
			String lowerLine = line.toLowerCase().trim();
			
			if ( lowerLine.equalsIgnoreCase("/quit")
					|| lowerLine.equalsIgnoreCase("/fin")
					|| lowerLine.equalsIgnoreCase("/exit")
					|| (
							unstrict && (
									lowerLine.equalsIgnoreCase("quit")
									|| lowerLine.equalsIgnoreCase("fin")
									|| lowerLine.equalsIgnoreCase("exit")
							)
					)
				)
			{
				writer.println("Quitting CheapAGE. Bye!");
				System.exit(0);
			}
			else if ( lowerLine.startsWith("/save") || lowerLine.startsWith("salvar") 
					|| ( unstrict && (lowerLine.equalsIgnoreCase("save") || lowerLine.startsWith("salvar")) ) )
			{
				writer.println("Saving the game...");
				processSaveLogCommand(line);
				return getInput(pl);
			}
			gameLog.addElement(line);
			return line;
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
			return null;
		}
	}

	public String getRealTimeInput(Player pl) 
	{
		writer.println("[WARNING] Real-time mode unsupported at the moment in the command-line client.\n");
		writer.println("Please use the windowed client.");
		return getInput(pl);
	}

	public boolean isColorEnabled() 
	{
		return false;
	}

	public boolean isDisconnected() 
	{
		return false;
	}

	public boolean isLoggingEnabled() 
	{
		return true;
	}

	public boolean isMemoryEnabled() 
	{
		return false;
	}

	public boolean isTitleEnabled() 
	{
		return false;
	}

	public void waitKeyPress() 
	{
		if ( rebotFriendly )
			writer.println("Escribe cualquier cosa para seguir...\n");
		else
			writer.println("Pulsa [ENTER] para seguir...\n");
		try
		{
			reader.readLine();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	public void write(String s) 
	{
		writer.print(s);
	}

	public void writeTitle(String s) 
	{
		;
	}

	public void writeTitle(String s, int pos) 
	{
		;
	}

	
	//copypasted from SwingAetheriaGameLoader::guardarLog
	public void saveLog ( File f ) throws java.io.IOException , java.io.FileNotFoundException
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter fwrite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		for ( int i = 0 ; i < gameLog.size() ; i++ )
		{
			fwrite.println( (String)gameLog.elementAt(i) );
		}
		fwrite.flush();	
	}
	
	private void processSaveLogCommand ( String command )
	{
		String path = command.substring(command.indexOf(" ")+1);
		File f = new File(path);
		try
		{
			saveLog(f);
			System.out.println("Log saved to " + f);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/*
	//copypasted from SwingAetheriaGameLoader::guardarEstado
	public void saveState ( File f ) throws java.io.IOException , java.io.FileNotFoundException 
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter frwite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		
		org.w3c.dom.Document d = null;
		try
		{
			d = mundo.getXMLRepresentation();
			System.out.println("D=null?" + (d==null) );
		}
		catch ( javax.xml.parsers.ParserConfigurationException exc )
		{
			System.out.println(exc);
		}
		
		javax.xml.transform.stream.StreamResult sr = null;
					
		sr = new javax.xml.transform.stream.StreamResult ( new FileOutputStream ( f ) );
			
		//hace la transformacion identidad (copia), eso si, escribiendo en ISO.
		try
		{
			javax.xml.transform.Transformer tr = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty ( javax.xml.transform.OutputKeys.ENCODING , "UTF-8" );
			javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource ( d );
			System.out.println("Nodo:" + ((javax.xml.transform.dom.DOMSource)s).getNode());
			tr.transform(s,sr);		
		}
		catch ( javax.xml.transform.TransformerConfigurationException tfe ) //newTransformer()
		{
			System.out.println(tfe);
		}
		catch ( javax.xml.transform.TransformerException te ) //transform()
		{
				System.out.println(te);
		}		
	}
	*/
	
}
