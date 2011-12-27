/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.net.*;
import java.io.*;
import java.util.*;

import eu.irreality.age.util.xml.DOMUtils;

import eu.irreality.age.debug.Debug;

public class AGEClientProxy implements MultimediaInputOutputClient , ARSPConstants
{



	private Hashtable colorCodesTable = new Hashtable();

	//options by default
	boolean opt_sound = false;
	boolean opt_images = false;
	boolean opt_color = false;
	boolean opt_title = false;

	private World mundo;

	private Thread asyncModeThread;

	private SoundClient sh;

	public String getColorCode ( String colorType )
	{
		if ( colorType == null ) return "";
		String code = (String) colorCodesTable.get(colorType);
		Debug.println("CLPROXY CODEGET" + code);
		if ( code == null ) return "";
		else return code;
	}

	public boolean isColorEnabled()
	{ return opt_color; 
	}
	public boolean isLoggingEnabled()
	{ return true; 
	}
	public boolean isMemoryEnabled()
	{ return false; 
	}
	public boolean isTitleEnabled()
	{ return opt_title; 
	}

	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla()
	{
		clearScreen();
	}

	public void clearScreen()
	{
		pw.println(CLEAR_SCREEN);
		pw.flush();	
	}
	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public synchronized void escribir(String s) //synchronized para evitar conflicto de escrituras del GameEngineThread con escrituras del thread del ClientProxy (enviar ficheros...)
	{
		write(s);
	}

	public synchronized void write(String s) //synchronized para evitar conflicto de escrituras del GameEngineThread con escrituras del thread del ClientProxy (enviar ficheros...)
	{
		pw.println(WRITE+" "+StringMethods.textualSubstitution(s,"\n","\\n"));
		Debug.println("WROTE: " + WRITE+" "+StringMethods.textualSubstitution(s,"\n","\\n"));
		pw.flush();
	}
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo(String s)
	{
		writeTitle(s);
	}

	public void writeTitle(String s)
	{
		pw.println(WRITE_TITLE+" "+s);
		pw.flush();
	}
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo(String s,int pos)
	{
		writeTitle(s, pos);
	}

	public void writeTitle(String s,int pos)
	{
		pw.println(WRITE_TITLE+" "+s+" "+pos);
		pw.flush();
	}
	/**
	 * @deprecated Use {@link #forceInput(String,boolean)} instead
	 */
	public void forzarEntrada(String s,boolean output_enabled)
	{
		forceInput(s, output_enabled);
	}

	public void forceInput(String s,boolean output_enabled)
	{
		pw.println(FORCE_INPUT+" "+output_enabled+s);
		pw.flush();
	}

	boolean clientHasDisconnected=false;
	public boolean isDisconnected()
	{
		return clientHasDisconnected;
	}

	//blocking:
	public String getInput(Player p)
	{
		setSynchronousMode();
		pw.println(GET_INPUT);
		Debug.println("WROTE: " + GET_INPUT);
		pw.flush();
		try
		{
			for (;;)
			{
				//escribir("SynchronousInput> ");
				String str;
				synchronized(this)
				{
					Debug.println("Gonna read.");
					str = br.readLine();
				}
				Debug.println("Line read is " + str);

				if ( str == null )
				{
					clientHasDisconnected=true;
					return null;
				}				

				StringTokenizer st = new StringTokenizer ( str );
				String cmd;
				if ( st.hasMoreTokens() )
					cmd = st.nextToken();
				else
				{
					Debug.println("EMPTY CMD");
					continue;	
				}
				String args;
				if ( cmd.equalsIgnoreCase (  GET_INPUT_RETURN ) )
				{
					if ( st.hasMoreTokens() ) args = st.nextToken("").trim();
					else args = "";
					Debug.println("INPUT GOTTEN: " + args );
					return args;
				}
				else
				{
					parseRequest ( str );
				}
			}
		}
		catch ( IOException ioe )
		{
			Debug.println("Uh oh, an IOException!!\n");
			clientHasDisconnected=true;
			//maybe set an exception flag and ask world to remove player ASAP.
			return null;
		}
	}
	//nonblocking:
	public String getRealTimeInput(Player p)
	{
		setAsynchronousMode();
		pw.println(GET_INPUT);
		Debug.println("WROTE: " + GET_INPUT);
		pw.flush();
		if ( inputQueue.isEmpty() )
			return null;
		String str = (String) inputQueue.removeFirst();	
		return str;
	}
	public void waitKeyPress() 
	{
		setSynchronousMode();
		pw.println(WAIT_KEY_PRESS);
		Debug.println("WROTE: " + WAIT_KEY_PRESS);
		pw.flush();
		boolean terminamos = false;
		try
		{
			while ( !terminamos )
			{
				String linea = br.readLine();
				StringTokenizer st = new StringTokenizer ( linea );
				String t = st.nextToken();
				if ( t.equalsIgnoreCase ( KEY_PRESSED ) )
				{
					terminamos = true;
				}
				else
					parseRequest ( linea );
			}
		}
		catch ( IOException ioe )
		{
			Debug.println(ioe);ioe.printStackTrace();
		}
	}

	//multim
	public SoundClient getSoundClient()
	{
		return sh; //el sound client proxy.
	}


	public void insertCenteredIcon(String fname)
	{
		pw.println(INSERT_ICON + " " + "centered" + " " + fname);
		pw.flush();
	}
	public void insertIcon(String fname)
	{
		pw.println(INSERT_ICON + " " + fname);
		pw.flush();
	}
	public void useImage(String fname,int parm1,int parm2,int parm3)
	{
		pw.println(USE_IMAGE + " " + parm1 + " " + parm2 + " " + parm3 + " " + fname);
		pw.flush();
	}
	public void addFrame ( int parm1 , int parm2 )
	{		
		pw.println(ADD_FRAME + " " + parm1 + " " + parm2 );
		pw.flush();
	}
	public void removeFrames()
	{
		pw.println(REMOVE_FRAMES);
		pw.flush();
	}
	public boolean isGraphicsEnabled()
	{ return opt_images; 
	}
	public boolean isSoundEnabled()
	{ return opt_sound; 
	}

	private Socket sock;
	private InputStream is;
	private OutputStream os;	
	private PrintWriter pw;
	private BufferedReader br;
	private boolean asynchronous;
	private LinkedList inputQueue;

	public AGEClientProxy ( java.net.Socket s )
	{

		try
		{
			sh = new AGESoundClientProxy(s);
		}
		catch ( Throwable e )
		{
			e.printStackTrace();
		}

		this.sock = s;

		try 
		{
			if (s != null) 
			{
				br = new BufferedReader(new InputStreamReader((is=new BufferedInputStream(sock.getInputStream(),100000)),"UTF-8"));         
				pw = new PrintWriter(new OutputStreamWriter((os=new BufferedOutputStream(sock.getOutputStream(),100000)),"UTF-8"))
				{
					public void println(String linea)
					{
						print(linea);
						print("\r\n");
						//System.err.println("ClientProxy says: " + linea);
					}
				}       ;  
			}

			//init
			Debug.println("Calling getClientType()");
			try
			{
				Thread.currentThread().sleep(1000);
			}
			catch ( InterruptedException ie )
			{
				;
			}
			Debug.println("Now really calling it");

			getClientType();
			Debug.println("Returning from proxy constructor.");

		} 
		catch (Exception e) 
		{
			Debug.println("Error: " + e);
			e.printStackTrace();
		}


	}

	public void getClientType() throws IOException
	{
		Debug.println("Sending client type request.\n");
		pw.println( CLIENT_TYPE_REQUEST );
		//pw.println( "" );
		pw.flush();
		String linea = br.readLine();
		StringTokenizer st = new StringTokenizer ( linea );
		String tok1;
		if ( st.hasMoreTokens() ) tok1 = st.nextToken();
		else tok1="";
		if ( tok1.equalsIgnoreCase ( CLIENT_TYPE_REPLY ) )
		{
			StringTokenizer st2 = new StringTokenizer ( st.nextToken("").trim() , ":" );
			String optionString = st2.nextToken();
			String nameString = st2.nextToken("").trim();
			StringTokenizer optionTokenizer = new StringTokenizer ( optionString , " " );
			while ( optionTokenizer.hasMoreTokens() )
			{
				String optionToken = optionTokenizer.nextToken();
				if ( optionToken.equalsIgnoreCase("sound") )
				{
					opt_sound=true;
				}
				else if ( optionToken.equalsIgnoreCase("nosound") )
				{
					opt_sound = false;
				}
				else if ( optionToken.equalsIgnoreCase("images") )
				{
					opt_images = true;
				}
				else if ( optionToken.equalsIgnoreCase("noimages") )
				{
					opt_images = false;
				}
				else if ( optionToken.equalsIgnoreCase("color") )
				{
					opt_color = true;
				}
				else if ( optionToken.equalsIgnoreCase("nocolor") )
				{
					opt_color = false;
				}
				else if ( optionToken.equalsIgnoreCase("title") )
				{
					opt_title = true;
				}
				else if ( optionToken.equalsIgnoreCase("notitle") )
				{
					opt_title = false;
				}
			}
		}

	}

	public void getColorCodesFromClient() throws IOException
	{

		pw.println( COLORCODE_REQUEST );
		pw.flush();
		String tok1="";
		while ( !tok1.equalsIgnoreCase(COLORCODE_INFO_BEGIN) )
		{
			String linea = br.readLine();
			StringTokenizer st = new StringTokenizer ( linea );
			if ( st.hasMoreTokens() ) tok1 = st.nextToken();
			else tok1="";

			if ( tok1.equalsIgnoreCase ( COLORCODE_INFO_BEGIN ) )
			{
				while ( !tok1.equalsIgnoreCase ( COLORCODE_INFO_END ) )
				{
					linea = br.readLine();
					st = new StringTokenizer ( linea );
					if ( st.hasMoreTokens() ) tok1 = st.nextToken();
					else tok1="";
					if ( tok1.equalsIgnoreCase ( COLORCODE_INFO_LINE ) )
					{
						//process COLORCODE_INFO_LINE
						if ( st.hasMoreTokens() )
						{
							String tok2 = st.nextToken();
							if ( st.hasMoreTokens() )
							{
								String tok3 = st.nextToken();
								/*
								if ( tok2.equalsIgnoreCase("action") )
								{
									actionColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("description") )
								{
									descriptionColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("default") )
								{
									defaultColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("information") )
								{
									infoColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("error") )
								{
									gameErrorColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("denial") )
								{
									lifeErrorColorCode=tok3;
								}
								else if ( tok2.equalsIgnoreCase("input") )
								{
									inputColorCode=tok3;
								}			
								 */
								//agregar código de color
								colorCodesTable.put(tok2.toLowerCase(),tok3);
								Debug.println("To hashtable: "+tok2.toLowerCase());
							} //end yet more tokens
							else
							{
								pw.println( UNRECOGNIZED_FORMAT + " " + linea );
								pw.println( SERVER_STATE + " expecting " + COLORCODE_INFO_LINE + ", " + COLORCODE_INFO_END );
								pw.flush();
							}
						} //end has more tokens
						else
						{
							pw.println( UNRECOGNIZED_FORMAT + " " + linea );
							pw.println( SERVER_STATE + " expecting " + COLORCODE_INFO_LINE + ", " + COLORCODE_INFO_END );
							pw.flush();
						}
					} //end is colorcode_info_line
					else if ( !tok1.equalsIgnoreCase(COLORCODE_INFO_END) )
					{
						Debug.println("I'm the ClientProxy and I can't recognize " + linea );
						pw.println( UNRECOGNIZED_MESSAGE + " " + linea );
						pw.println( SERVER_STATE + " expecting " + COLORCODE_INFO_LINE + ", " + COLORCODE_INFO_END );
						pw.flush();
					}
				} //end while not colorcode_info_end
				Debug.println("Colorcode info end marker received.");	
				return;
				//end received

			} //end if is begin
			else if ( tok1.equalsIgnoreCase(UNRECOGNIZED_MESSAGE))
			{
				System.err.println("Protocol error! " + tok1);
			}
			else
			{
				pw.println( UNRECOGNIZED_MESSAGE + " " + linea );
				pw.println( SERVER_STATE + " expecting " + COLORCODE_INFO_BEGIN );
				pw.flush();
			}
		} //end while is not begin
	}

	public void bindToWorld ( World mundo )
	{

		this.mundo = mundo;

		try
		{

			//send world's visual configuration

			Debug.println("Sending visconf.");



			try
			{

				//informar de direct. de mundo
				pw.println ( WORLD_DIR + " " + mundo.getWorldDir() );

				//crear un Document de pacotilla sólo para poder hacer un getXMLRepresentation()
				org.w3c.dom.Document d = null;
				javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
				d = db.newDocument();

				Debug.println("Mundo: " + mundo);
				Debug.println("VisCo: " + mundo.getVisualConfiguration());

				//String elXML = mundo.getVisualConfiguration().getXMLRepresentation(d).toString();
				String elXML = DOMUtils.nodeToString(mundo.getVisualConfiguration().getXMLRepresentation(d));
				StringTokenizer st = new StringTokenizer ( elXML , "\n" );

				pw.println ( VISUALCONF_INIT_BEGIN );
				while ( st.hasMoreTokens() )
				{
					String tok = st.nextToken();  
					pw.println ( VISUALCONF_INIT_LINE + " " + tok );
					Debug.println("Sending line: " + VISUALCONF_INIT_LINE + " " + tok );
				}
				pw.println ( VISUALCONF_INIT_END );
				pw.flush();


				try
				{

					Debug.println("Gonna get color codes...\n");

					getColorCodesFromClient();

					Debug.println("Color codes succ'flly gotten.\n");

				}
				catch ( IOException ioe )
				{
					Debug.println(ioe); ioe.printStackTrace();
				}

			}
			catch ( javax.xml.parsers.ParserConfigurationException pce )
			{
				Debug.println(pce);
			}


			//now, send world's multimedia file list to propose a download.

			List l = mundo.getFileList();
			if ( l != null && l.size() > 0 )
			{

				pw.println(FILE_LIST_BEGIN);
				for ( int k = 0 ; k < l.size() ; k++ )
				{
					pw.println(FILE_LIST_LINE + " " + l.get(k) + " " + (new File((String)l.get(k))).length() );
				}
				pw.println(FILE_LIST_END);
				pw.flush();

			}

			//filegets will be parsed on normal input-request loops




			write("Welcome to " + sock.getLocalAddress().getHostName() + " [port " + sock.getLocalPort() + "] running the Aetheria Game Engine.\n");
			mundo.addNewPlayerASAP ( this );
			Debug.println("Welcoming and adding player.\n");

		}
		catch ( XMLtoWorldException e )
		{
			Debug.println("Couldn't: XMLtoWorldException " + e );
		}

	}

	private synchronized void setSynchronousMode()
	{
		if ( asyncModeThread != null )
		{
			asynchronous = false;
			//asyncModeThread.interrupt();
			while ( asyncModeThread.isAlive() )
			{
				Debug.println("Die, thread! Die!");
				try
				{
					wait(500);
				}
				catch ( InterruptedException ie )
				{
					;
				}
			}
		}
		asynchronous = false;
	}
	private void setAsynchronousMode()
	{
		if ( !asynchronous )
		{
			asynchronous = true;	
			inputQueue = new LinkedList();
			asyncModeThread = new Thread ( )
			{
				//later, maybe do this even on non-async, but hacer caso a RETURNS only async
				public void run ( )
				{
					try
					{
						for(;;)
						{
							if ( !asynchronous ) return;
							String str;
							synchronized(this)
							{
								str = br.readLine();
							}
							if ( str == null )
							{
								clientHasDisconnected=true;
								break;
							}

							StringTokenizer st = new StringTokenizer ( str );
							String cmd = st.nextToken();
							String args;
							if ( st.hasMoreTokens() ) args = st.nextToken("").trim();
							else args = "";
							if ( cmd.equalsIgnoreCase (  GET_INPUT_RETURN ) )
							{
								Debug.println("INPUT GOTTEN: " + args );
								Debug.println("Adding 2 Que: " + args);
								inputQueue.addLast(args);
							}
							else
							{
								parseRequest(str);
							}
						}
					}
					catch ( IOException ioe )
					{
						clientHasDisconnected = true;
						Debug.println("IO Exception.\n");
					}
				}
			};
			asyncModeThread.start();
		}
	}

	//Parsear un comando activo por parte del cliente (algo que no es mera respuesta a
	//mensajes emitidos por nosotros)
	public void parseRequest ( String request )
	{
		StringTokenizer st = new StringTokenizer ( request );
		String tok = st.nextToken();
		if ( tok.equalsIgnoreCase ( "GET_FILE" ) )
		{
			//do
			String fileName = st.nextToken();
			List l = mundo.getFileList();
			if ( l.contains ( fileName ) ) //no enviaremos ficheros que no estén en esa lista
			{
				int k = l.indexOf(fileName);
				File f = (new File((String)l.get(k)));
				try
				{
					FileInputStream fis = new FileInputStream ( f );
					byte[] barr = new byte[(int)f.length()];

					//fis.read(barr);

					Debug.println("Reading file from file input stream.");
					Debug.println("To read " + barr.length + " bytes from input stream.");
					int lei = 0;
					while ( lei < barr.length )
					{
						lei += fis.read ( barr , lei , barr.length-lei );
						Debug.println("Currently read " + lei + " bytes.");
					}

					synchronized(this) //que no se cuelen escrituras entre header y fichero
					{
						pw.println(FILE_HEADER_LINE + " " + l.get(k) + " " + f.length() );
						Debug.println("WROTE: "+ FILE_HEADER_LINE + " " + l.get(k) + " " + f.length());

						/*
						for(;;)
						{

							String linea = br.readLine();
							Debug.println("Expecting file acc/rej. Read: " + linea);
							StringTokenizer lt = new StringTokenizer ( linea );
							String reply = lt.nextToken();
							if ( reply.equalsIgnoreCase( FILE_REJECT ) )
							{
								Debug.println("File rejected...");
								return;
							}
							else if ( reply.equalsIgnoreCase( FILE_ACCEPT ) )
							{
								Debug.println("File accepted...");
								break;
							}
							else
							{
								Debug.println("File ignored...");
								parseRequest(linea);
							}

						}
						 */

						//File accepted. Binary mode set by client.

						pw.flush();
						Debug.println("Writing length " + barr.length + " data to socket stream.");
						Debug.println("Socket buffer sizes are " + sock.getSendBufferSize() + " and " + sock.getReceiveBufferSize() );

						//os.write(barr);
						//pw.print(new String(barr));
						//os.write(barr);
						os.write(barr);
						//for ( int i = 0 ; i < barr.length ; i++ )
						//	pw.print ( (char) barr[i] );

						os.flush();
						Debug.println("WROTE: "+new String(barr));
						//pw.flush();
					}
				}
				catch ( IOException ioe )
				{
					Debug.println(ioe);
					ioe.printStackTrace();
				}
			}
		}
		else if ( GOODBYE.equalsIgnoreCase(tok) )
		{
			clientHasDisconnected = true;
			try {
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}

