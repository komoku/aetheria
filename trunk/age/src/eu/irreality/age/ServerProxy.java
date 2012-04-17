/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.net.*;
import java.util.*;
import java.io.*;

import javax.swing.*;

import eu.irreality.age.debug.Debug;

public class ServerProxy extends Thread implements ARSPConstants
{

	Socket sock;
	MultimediaInputOutputClient cliente;
	
	VersatileBufferedInputStream br;
	PrintWriter pw;
	InputStream is;
	OutputStream os;

	ArrayList requestedFiles = new ArrayList();
	
	
	private String worldDir = null;
	
	boolean input_thread_flag = false;
	

	public ServerProxy ( Socket s , MultimediaInputOutputClient ioc )
	{
		this.sock = s;
		this.cliente = ioc;
		
		try 
		{
      		if (s != null) 
			{
				br = new VersatileBufferedInputStream ( sock.getInputStream() );
        		//br = new BufferedReader(new InputStreamReader((is=new BufferedInputStream(sock.getInputStream(),100000))));         
        		pw = new PrintWriter(new OutputStreamWriter((os=new BufferedOutputStream(sock.getOutputStream(),100000)),"UTF-8"))
			    {
				public void println(String linea)
				{
				    print(linea);
				    print("\r\n");
				    //System.err.println("ServerProxy says: " + linea);
				}

			    };         
			}
    	} 
		catch (Exception e) 
		{
      		System.err.println("Error: " + e);
    	}
		
	}
	
	public void run()
	{
		try
		{
			String linea="Inicial";
			for ( ;; )
			{
				if ( cliente.isDisconnected() )
				{
					//client disconnected (for example, closed the window): we stop executing this proxy.
					pw.println(GOODBYE);
					sock.close();
					return;
				}
				
				Debug.println("Last linea: " + linea);
				br.mark(500000);
				linea = br.readLine();
				if ( linea == null )
				{
					System.err.println("Read null line. Disconnected, I guess.\n");
					cliente.write("Null line: the server seems to have disconnected.\n");
					return;
				}
				//is.mark(500000);
				Debug.println("Current linea: " + linea);
				parseMessage ( linea );
			}
		}
		catch ( IOException ioe )
		{
			cliente.write("Exception: the server seems to have disconnected.\n");
			System.err.println(ioe);ioe.printStackTrace();
		}
	}


	public void parseMessage ( String mesg )
	{
	
		
		Debug.println("Command to parse by client-side server proxy: " + mesg);
		
		StringTokenizer st = new StringTokenizer ( mesg , " " );
		
		String command;
		if ( st.hasMoreTokens() )
			command = st.nextToken().trim();
		else command = "";
		
		String arguments;
		if ( st.hasMoreTokens() ) arguments = st.nextToken("").trim();
		else arguments = "";
		
		if ( command.equalsIgnoreCase( WRITE ) )
			cliente.write( StringMethods.textualSubstitution ( arguments , "\\n" , "\n" ) );
		else if ( command.equalsIgnoreCase( WRITE_TITLE ) )
			cliente.writeTitle(arguments); 
		else if ( command.equalsIgnoreCase( CLEAR_SCREEN ) )
			cliente.clearScreen();
			
		/*sound handler cmd's*/	
		else if ( command.equalsIgnoreCase( STOP_ALL_SOUND ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() );
					cliente.getSoundClient().stopAllSound();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( MIDI_INIT ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() );
					cliente.getSoundClient().midiInit();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( MIDI_PRELOAD ) )
		{
			try
			{
			if ( cliente.isSoundEnabled() );
				cliente.getSoundClient().midiPreload(arguments);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( MIDI_START ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() );
				{
					if ( arguments != null && !arguments.equals("") )
						cliente.getSoundClient().midiStart(arguments);
					else
						cliente.getSoundClient().midiStart();
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( MIDI_OPEN ) )
		{
			try
			{
			if ( cliente.isSoundEnabled() );
				cliente.getSoundClient().midiOpen(arguments);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( MIDI_STOP ) )
		{
			if ( cliente.isSoundEnabled() );
				cliente.getSoundClient().midiStop();
		}
		else if ( command.equalsIgnoreCase( MIDI_CLOSE ) )
		{
			if ( cliente.isSoundEnabled() );
				cliente.getSoundClient().midiClose();
		}
		else if ( command.equalsIgnoreCase( AUDIO_PRELOAD ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() );
					cliente.getSoundClient().audioPreload(arguments);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( AUDIO_UNLOAD ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() );
					cliente.getSoundClient().audioUnload(arguments);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( AUDIO_STOP ) )
		{
				if ( cliente.isSoundEnabled() );
				{
					StringTokenizer st2 = new StringTokenizer(arguments);
					String file = st2.nextToken();
					if ( st2.hasMoreTokens() )
					{
						double fadeTime = Double.parseDouble(st2.nextToken());
						cliente.getSoundClient().audioFadeOut(file,fadeTime);
					}
					else
						cliente.getSoundClient().audioStop(file);
				}
		}
		else if ( command.equalsIgnoreCase( AUDIO_START ) )
		{
			try
			{
				if ( cliente.isSoundEnabled() )
				{
					StringTokenizer st2 = new StringTokenizer(arguments);
					try
					{
						int loopTimes;
						if ( (loopTimes=Integer.valueOf(st2.nextToken()).intValue()) > 0 )
						{
							if ( st2.hasMoreTokens() )
							{
								double seconds = Double.parseDouble(st2.nextToken());
								double delay = Double.parseDouble(st2.nextToken());
								cliente.getSoundClient().audioFadeIn(st2.nextToken("").trim(),loopTimes,seconds,delay);
							}
							else
								cliente.getSoundClient().audioStart(st2.nextToken("").trim(),loopTimes);
						}
					}
					catch ( NumberFormatException nfe )
					{
						cliente.getSoundClient().audioStart(arguments);
					}
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if ( command.equalsIgnoreCase( AUDIO_SET_GAIN ) )
		{
			if ( cliente.isSoundEnabled() )
			{
				StringTokenizer st2 = new StringTokenizer(arguments);
				try
				{
					double gain = 0.0;
					if ( st2.hasMoreTokens() ) gain = Double.valueOf(st2.nextToken()).doubleValue();
					if ( st2.hasMoreTokens() ) 
						cliente.getSoundClient().audioSetGain(st2.nextToken(""),gain);
				}
				catch ( NumberFormatException nfe )
				{
					nfe.printStackTrace();
				}
			}
		}
				
		else if ( command.equalsIgnoreCase( FORCE_INPUT ) )
		{
			StringTokenizer argTok = new StringTokenizer ( arguments , " " );
			String outputEnabled = argTok.nextToken();
			String inputText = argTok.nextToken("").trim();
			cliente.forceInput(inputText,Boolean.valueOf(outputEnabled).booleanValue());
		}
		else if ( command.equalsIgnoreCase( GET_INPUT ) ) //aquí todo asíncrono, ya se encargará el servidor de interpretarlo como síncrono si procede 
														//<- i think this comment should be in reverse. this is treated in a synchronous way. But if the server doesn't
														// receive input on time and is on asynchronous mode, it will go on simulating the world.
		{
		    Debug.println("Input get command");
			
		    //for(;;)
			//{
		    	if ( !input_thread_flag )
		    	{
		    		Debug.println("Spawning input-returning thread");
		    		input_thread_flag = true;
		    		Thread th = new Thread ()
					{
		    			public void run()
		    			{
		    				Debug.println("getInput called");
		    				String s = cliente.getInput(null); //synchronous (blocking) call
		    				if ( s == null )
		    					pw.println(GET_INPUT_RETURN_NULL);
		    				else
		    					pw.println(GET_INPUT_RETURN + " " +s);	
		    				pw.flush();
		    				input_thread_flag = false;
		    			}
					};
					th.start();
					//break;
		    	}
			//}
		}
		/*
		else if ( command.equalsIgnoreCase( GET_INPUT_ASYNCHRONOUS ) )
		{
			Debug.println("getRealTimeInput called");
			String s = cliente.getRealTimeInput(null);
			pw.println(GET_INPUT_RETURN + " " +s);
			pw.flush();
		}
		*/
		else if ( command.equalsIgnoreCase( INSERT_ICON ) )
		{
			if ( cliente.isGraphicsEnabled() )
			{
				StringTokenizer argTok = new StringTokenizer ( arguments , " " );
				String head = argTok.nextToken();
				String tail = argTok.nextToken("").trim();
				if ( head.equalsIgnoreCase("centered") )
				{
					cliente.insertCenteredIcon(tail);
				}
				else
				{
					cliente.insertIcon(arguments);	
				}	
			}
		}
		else if ( command.equalsIgnoreCase( USE_IMAGE ) )
		{
			if ( cliente.isGraphicsEnabled() )
			{
				StringTokenizer argTok = new StringTokenizer ( arguments , " " );
				String one = argTok.nextToken().trim();
				String two = argTok.nextToken().trim();
				String three = argTok.nextToken().trim();
				String tail = argTok.nextToken("").trim();
				cliente.useImage(tail,Integer.valueOf(one).intValue(),Integer.valueOf(two).intValue(),Integer.valueOf(three).intValue());
			}
		}
		else if ( command.equalsIgnoreCase( ADD_FRAME ) )
		{
			if ( cliente.isGraphicsEnabled() )
			{
				StringTokenizer argTok = new StringTokenizer ( arguments , " " );
				String one = argTok.nextToken().trim();
				String two = argTok.nextToken().trim();
				cliente.addFrame(Integer.valueOf(one).intValue(),Integer.valueOf(two).intValue());
			}
		}
		else if ( command.equalsIgnoreCase( REMOVE_FRAMES ) )
		{
			if ( cliente.isGraphicsEnabled() )
			{
				cliente.removeFrames();
			}
		}
		/* inexistent
		else if ( command == SET_INPUT_STRING )
		{
			cliente.setInputString ( arguments );
		}
		*/
		else if ( command.equalsIgnoreCase( WAIT_KEY_PRESS ) )
		{
			cliente.waitKeyPress();
			pw.println(KEY_PRESSED);
			pw.flush();
		}
		else if ( command.equalsIgnoreCase( CLIENT_TYPE_REQUEST ) )
		{
			pw.println(CLIENT_TYPE_REPLY + " sound" + " images" + " color" + " title" + " :ServerProxy for generic MultimediaInputOutputClient" );
			Debug.println("Client type reply sent.\n");
			pw.flush();
		}
		else if ( command.equalsIgnoreCase( COLORCODE_REQUEST ) )
		{
			Debug.println("COLORCODE REQUEST COMMAND FOUND!" + cliente.getColorCode("description"));
			pw.println(COLORCODE_INFO_BEGIN);
			pw.println(COLORCODE_INFO_LINE + " " + "description" + " " + cliente.getColorCode("description") );
			pw.println(COLORCODE_INFO_LINE + " " + "input" + " " + cliente.getColorCode("input") );
			pw.println(COLORCODE_INFO_LINE + " " + "error" + " " + cliente.getColorCode("error") );
			pw.println(COLORCODE_INFO_LINE + " " + "information" + " " + cliente.getColorCode("information") );
			pw.println(COLORCODE_INFO_LINE + " " + "denial" + " " + cliente.getColorCode("denial") );
			pw.println(COLORCODE_INFO_LINE + " " + "action" + " " + cliente.getColorCode("action") );
			pw.println(COLORCODE_INFO_LINE + " " + "default" + " " + cliente.getColorCode("default") );
			pw.println(COLORCODE_INFO_LINE + " " + "important" + " " + cliente.getColorCode("important") );
			pw.println(COLORCODE_INFO_LINE + " " + "story" + " " + cliente.getColorCode("story") );
			pw.println(COLORCODE_INFO_LINE + " " + "reset" + " " + cliente.getColorCode("reset") );
			pw.println(COLORCODE_INFO_END);
			pw.flush();
		}	
		
		else if ( command.equalsIgnoreCase( WORLD_DIR ) )
		{
			worldDir = arguments;
		}
				
		else if ( command.equalsIgnoreCase( VISUALCONF_INIT_BEGIN ) )
		{
			String xmlText = "";
			boolean terminamos = false;
			while ( !terminamos )
			{
				String linea;
				try
				{
					Debug.println("Tryin' to read newline:");
					linea = br.readLine();	
					Debug.println("Line read, " + linea);
				}
				catch ( IOException ioe )
				{
					System.err.println("Exception (0) " + ioe);
					break;
				}
				StringTokenizer sto = new StringTokenizer ( linea );
				//System.err.println("String: " + linea + " (len " + linea.length() + ")");
				if ( linea.length() == 0 ) continue;
				String token = sto.nextToken();
				if ( token.equalsIgnoreCase ( VISUALCONF_INIT_LINE ) )
				{
					xmlText += ( sto.nextToken("").trim() );
					xmlText += "\n";
				}
				else if ( token.equalsIgnoreCase ( VISUALCONF_INIT_END ) )
				{
					terminamos = true;	
				}
				else
				{
					Debug.println("Callin' emergency parseMessage recursion.");
					parseMessage ( linea );
				}
			}		
			//crear la visconf que nos mandan
			Debug.println("Gonna create 'doc.\n");
			

			
			org.w3c.dom.Document d = null;
			BufferedReader br2;
			try
			{
				br2 = new BufferedReader ( new StringReader ( xmlText  ) );
				org.xml.sax.InputSource is = new org.xml.sax.InputSource(br2);
				javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
				d = db.parse(is);
			}
			catch ( javax.xml.parsers.ParserConfigurationException pce )
			{
				System.err.println(pce);
			}
			catch ( org.xml.sax.SAXException se ) //parse()
			{
				System.err.println(se);
			}
			catch ( IOException ioe ) //parse()
			{
				Debug.println("ServerProxy throws: ");
				Debug.println(ioe);ioe.printStackTrace();
				Debug.println("When text was " + xmlText);
			}
			
			Debug.println("Doc " + d + " created.");
			
			if ( d!=null)
			{
				try
				{
					VisualConfiguration vc = new VisualConfiguration ( d.getDocumentElement() , worldDir ); 
					if ( cliente instanceof ColoredSwingClient )
						((ColoredSwingClient)cliente).setVisualConfiguration(vc);
					Debug.println("Visconf set.");	
				}
				catch ( XMLtoWorldException xml2we )
				{
					;
				}

			}
			
			
			Debug.println("Returnin' from parseMessage()\n");
			
		}
		
		else if ( command.equalsIgnoreCase( FILE_LIST_BEGIN ) )
		{
			boolean terminamos = false;
			Vector ficheros = new Vector();
			Debug.println("File list begin.");
			while ( !terminamos )
			{
				String linea;
				try
				{
					linea = br.readLine();
				}
				catch ( IOException ioe )
				{
					System.err.println(ioe);ioe.printStackTrace();break;
				}
				StringTokenizer sto = new StringTokenizer ( linea );
				String token = sto.nextToken();
				if ( token.equalsIgnoreCase ( FILE_LIST_LINE ) )
				{
					ficheros.add ( sto.nextToken("").trim() );
				}
				else if ( token.equalsIgnoreCase ( FILE_LIST_END ) )
				{
					terminamos = true;
					
					//do all the ask-for-file-get, etc. stuff.
					
					requestUserSelectedFiles ( ficheros );
									
					
					
					
				}
				else
				{
					System.out.println("Callin' emergency parseMessage recursion.");
					parseMessage ( linea );
				}
			}
		}
		
		else if ( command.equalsIgnoreCase ( FILE_HEADER_LINE ) ) //nos envían un fichero
		{
				
				System.out.println("File header line.");
				
				StringTokenizer argTok = new StringTokenizer ( arguments , " " );
				String remoteName = argTok.nextToken();
				int byteSize = Integer.valueOf(argTok.nextToken()).intValue();
				
				boolean ficheroDeseado = false;
				for ( int i = 0 ; i < requestedFiles.size() ; i++ )
				{
					StringTokenizer st2 = new StringTokenizer( (String) requestedFiles.get(i) );
					String filename = st2.nextToken();
					if ( filename.equals(remoteName) )
					{
						ficheroDeseado = true;
						break;
					}
				}
				
				/*
				if ( !ficheroDeseado )
				{
					pw.println(FILE_REJECT);
					pw.flush();
					System.out.println("Rejected.");
					return;
				}
				*/
				
				//{fichero deseado}
				
				/*	
				pw.println(FILE_ACCEPT);	
				pw.flush();
				System.out.println("Accepted.");
				*/
							
				char[] charArray = new char[byteSize];
				byte[] byteArray = new byte[byteSize];	
			
				//read the file from socket
				try
				{
				
					//br.reset();
				
					int lei = 0;
					
					/*
					while ( lei < byteArray.length )
					{
					
						System.out.println("To request this time: " + (byteArray.length-lei));
						lei += is.read ( byteArray , lei , byteArray.length-lei );
						System.out.println("Finally read " + lei + " bytes.");
						
						System.out.println("Data read till mom't eez:\n\n");
						System.out.write(byteArray,0,lei);
						System.out.println("\n\n");
						
						//create test file
						FileOutputStream juasesblases = new FileOutputStream(new File("prueba.txt"));
						juasesblases.write(byteArray,0,lei);
						juasesblases.flush();
						
					}
					*/
					
				
					
					System.out.println("LASTREAD: " + mesg);
					System.out.println("Reading file from socket.");
					System.out.println("To read " + byteArray.length + " bytes from input stream.");

					
					/*ASCII mode works
					while ( lei < byteArray.length )
					{
						System.out.println("To request this time: " + (byteArray.length-lei));
						lei += br.read ( byteArray , lei , byteArray.length-lei );
						System.out.println("Finally read " + lei + " bytes.");
						
						System.out.println("Data read till mom't eez:\n\n");
						System.out.print(byteArray);
						System.out.println("\n\n");		
						
						//create test file
						FileWriter osw = new FileWriter ( "Prueba.txt" );
						BufferedWriter bw2 = new BufferedWriter ( osw , lei );
						System.out.println("lei is: " + lei);
						
						int escribi = 0;

						bw2.write(byteArray,0,lei);
						bw2.flush();
					
					}
					*/
					
					while ( lei < byteArray.length )
					{
						
						/*
						int cur = br.read();
						char curAsChar = (char)cur;
					
						byteArray[lei] = (byte)cur;
						charArray[lei] = curAsChar;
						lei++;
					
						//16 by 16
						
						System.out.print( Integer.toString(cur,16) + " " );
						if ( lei % 16 == 0 )
							System.out.println();
						*/
						
						lei += br.read ( byteArray , lei , byteSize-lei );
						System.out.println(lei + " bytes read.");
						
					}
					
					/*
					String s = new String ( charArray );
					byteArray = s.getBytes();
					*/

					/*
					//ponemos la stream antes de leer la FILE_HEADER_LINE
					br.reset();
					//skipeamos dicha linea
					
					int jal=0;
					char c;
					while ( (c = (char)is.read()) != '\n' ) 
					{jal++; System.out.println("Byte " + jal + ": " + (int)(c) );
					}
					is.mark(5);
					if ( (c = (char)is.read()) != '\r' )
					{
						System.out.println("Stream reset. Read: " + (int)(c) );
						is.reset();
					}
					//is.skip(mesg.length()+1);
					
					
					while ( lei < byteArray.length )
					{
					
						System.out.println("To request this time: " + (byteArray.length-lei));
						lei += is.read ( byteArray , lei , byteArray.length-lei );
						System.out.println("Finally read " + lei + " bytes.");
						
						System.out.println("Data read till mom't eez:\n\n");
						System.out.write(byteArray,0,lei);
						System.out.println("\n\n");
						
						
						//create test file
						FileOutputStream juasesblases = new FileOutputStream(new File("prueba.txt"));
						juasesblases.write(byteArray,0,lei);
						
					}
					//System.out.println("Skipped (br): " + br.skip(byteArray.length));
					*/
				
				}
				catch ( IOException ioe )
				{
					ioe.printStackTrace();
					return;
				}
				
				/*
				System.out.println("REMOTE NAME: " + remoteName);
				System.out.println("REQUESTED FILES:");
				for ( int i = 0 ; i < requestedFiles.size() ; i++ )
				{
					String s = (String) requestedFiles.get(i);
					System.out.println(s);
				}
				*/

				
				//if ( ficheroDeseado ) //que un cliente falso no nos mande otro fichero
				{
					System.out.println("Writin' actual file.");
					try
					{
						FileOutputStream fos = new FileOutputStream ( remoteName );
						OutputStreamWriter osw = new OutputStreamWriter ( fos );
						fos.write( /*new String(byteArray).getBytes()*/byteArray );
						fos.flush();
						fos.close();
						
						cliente.write(cliente.getColorCode("information") + "Descargado con éxito " + remoteName + " (" + byteSize + " bytes)\n" + cliente.getColorCode("reset"));
						
					}
					catch ( IOException ioe )
					{
						ioe.printStackTrace();
					}
				}
			
		}
		else if ( command.equalsIgnoreCase(UNRECOGNIZED_MESSAGE))
		{
			System.out.println("Protocol error!");
		}
		
		else
		{
			System.out.println("I'm the ServerProxy and I can't recognize " + command );
			pw.println(UNRECOGNIZED_MESSAGE + " " + command);
			pw.flush();
		}
	
		
	
	
	}
	
	public void requestServerFiles ( List /*of String*/ fnames )
	{
		for ( int i = 0 ; i < fnames.size() ; i++ )
		{
			
			String fName = (String)fnames.get(i);
			pw.println(GET_FILE+" "+fName);
			pw.flush();
		}
		requestedFiles.addAll(fnames);
	}
	//public void requestServerFiles ( List /*of String*/ fnames )
	/*{
		for ( int i = 0 ; i < fnames.size() ; i++ )
		{
			
			String fName = (String)fnames.get(i);
			pw.println(GET_FILE+" "+fName);
			pw.flush();
			
			String linea;
			try
			{
				System.out.println("Expecting file data:");
					linea = br.readLine();	
			}
			catch ( IOException ioe )
			{
				System.out.println(ioe);ioe.printStackTrace();
				return;
			}
			StringTokenizer sto = new StringTokenizer ( linea );
			String token = sto.nextToken();
			String remoteName;
			int byteSize;
			if ( token.equalsIgnoreCase ( FILE_HEADER_LINE ) )
			{
				remoteName = sto.nextToken();
				byteSize = Integer.valueOf(sto.nextToken()).intValue();
				
				byte[] byteArray = new byte[byteSize];
			
				//read the file from socket
				try
				{
					System.out.println("Reading file from socket.");
					is.read ( byteArray , 0 , byteArray.length );
				}
				catch ( IOException ioe )
				{
					ioe.printStackTrace();
					return;
				}
				
				if ( remoteName == fName ) //que un cliente falso no nos mande otro fichero
				{
					try
					{
						FileOutputStream fos = new FileOutputStream ( fName );
						fos.write(byteArray);
						fos.close();
					}
					catch ( IOException ioe )
					{
						ioe.printStackTrace();
						continue;
					}
				}
			
			} //end if token is file header line
			
			else
			{
				System.out.println("Parsing non-file data.");
				parseMessage(linea);
				i--; //didn't do this file
			}
			
		}
	}
	*/
	
	public void requestUserSelectedFiles ( final Vector /*of String*/ ficheros )
	{
	
	
		//show dialog: no, esto debería ser swing-independent.
		//se podría hacer un swingserverproxy extends serverproxy que
		//detallara esto, en forma de método genérico getChosenFilesFromClient()
					
		final JDialog jd = new JDialog();
		jd.setModal(true);	
		jd.setTitle("Ficheros");
		jd.setSize(500,500);
		jd.getContentPane().setLayout ( new java.awt.BorderLayout ( ) );
					
		final JList jl = new JList(ficheros);
					
		for ( int i = 0 ; i < ficheros.size() ; i++ )
		{
			StringTokenizer st1 = new StringTokenizer ( (String)ficheros.get(i) );
			String fileName = st1.nextToken();
			File theFile = new File ( fileName );
			if ( !theFile.exists() ) jl.setSelectedValue(ficheros.get(i),false);
		}					
					
		jd.getContentPane().add ( new JLabel("El servidor ofrece los siguientes ficheros:") , java.awt.BorderLayout.NORTH );			
					
		jd.getContentPane().add ( new JScrollPane(jl) , java.awt.BorderLayout.CENTER );
					
		JPanel jp = new JPanel();
		JButton b1 = new JButton("Descargar todos");
		JButton b2 = new JButton("Descargar seleccionados");
		JButton b3 = new JButton("No descargar ninguno");
		
		b1.addActionListener ( 
			new java.awt.event.ActionListener()
			{
				public void actionPerformed ( java.awt.event.ActionEvent evt )
				{
					requestServerFiles ( ficheros );
					jd.dispose();
				}
			}
		);
		b2.addActionListener (
			new java.awt.event.ActionListener()
			{
				public void actionPerformed ( java.awt.event.ActionEvent evt )
				{
					Vector seleccionados = new Vector();
					for ( int i = 0 ; i < ficheros.size() ; i++ )
					{
						if ( jl.isSelectedIndex(i) )
							seleccionados.add ( ficheros.get(i) );
					}
					requestServerFiles ( seleccionados );
					jd.dispose();
				}
			}
		);
		b3.addActionListener (
			new java.awt.event.ActionListener()
			{
				public void actionPerformed ( java.awt.event.ActionEvent evt )
				{
					jd.dispose();
				}
			}
		);
		
		jp.add(b1);
		jp.add(b2);
		jp.add(b3);
		jd.getContentPane().add(jp , java.awt.BorderLayout.SOUTH );
					
		System.out.println("Size of list: " + ficheros.size() );
					
		jd.pack();
		jd.setVisible(true);
	

	
	
	}



}
				
