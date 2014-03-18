package eu.irreality.age;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.filemanagement.URLUtils;
import eu.irreality.age.filemanagement.WorldLoader;
import eu.irreality.age.i18n.UIMessages;

public class GameInfo implements Serializable
{
	private String[] theInfo;
	private String f = null;
	
	static Vector allInstances = new Vector();
	
	public boolean equals ( Object obj )
	{
		if ( ! ( obj instanceof GameInfo ) ) return false;
		else
		{
			return ( ( f==null?((GameInfo)obj).f==null:f.equals(((GameInfo)obj).f)) && ( (theInfo==null)?(((GameInfo)obj).theInfo==null):Arrays.equals ( theInfo , ((GameInfo)obj).theInfo )  ) );
		}
	}
	
	
	//info for that file if loaded, else load if exists or return null if doesn't.
	public static GameInfo getGameInfoFromFile ( String f )
	{
		for ( int i = 0 ; i < allInstances.size() ; i++ )
		{
			if ( ( (GameInfo) allInstances.elementAt(i) ).getFile() != null && ( (GameInfo) allInstances.elementAt(i) ).getFile().equals(f) )
			{
				return ( (GameInfo) allInstances.elementAt(i) );
			}
		}
		try
		{
			return GameInfo.getGameInfo(f);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public GameInfo ( String[] info , String f )
	{
		theInfo = info;
		this.f = f;
		allInstances.add(this);
	}
	
	public GameInfo()
	{
		theInfo = new String[5];
		for ( int i = 0 ; i < 5 ; i++ )
			theInfo[i] ="";
		allInstances.add(this);	
	}
	
	public String getFile()
	{
		return f;
	}
	public boolean isValid()
	{
		return (f!=null);
	}
	
	public String getName() 
	{
		return theInfo[0];
	}
	public String getAuthor() 
	{
		return theInfo[1];
	}
	public String getDate() 
	{
		return theInfo[3];
	}
	public String getVersion() 
	{
		return theInfo[2];
	}
	public String getAGEVersion() 
	{
		return theInfo[4];
	}
	
	public String toString()
	{
		return theInfo[0] + " " + theInfo[2];
	}
	
	public String toLongString()
	{
		return UIMessages.getInstance().getMessage("gameinfo.name")
		+ " " + getName() + "\n" 
		+ UIMessages.getInstance().getMessage("gameinfo.author")
		+ " " + getAuthor() + "\n"  
		+ UIMessages.getInstance().getMessage("gameinfo.date")
		+ " " + getDate() + "\n" 
		+ UIMessages.getInstance().getMessage("gameinfo.version") 
		+ " " + getVersion() + "\n" 
		+ UIMessages.getInstance().getMessage("gameinfo.required")
		+ " " + getAGEVersion() + "\n" 
		+ UIMessages.getInstance().getMessage("gameinfo.file")
		+ " " + getFile();
	}
	
	
	private static org.w3c.dom.Document documentFromFile ( String moduleFile ) throws TransformerException
	{
		//InputStream str = new FileInputStream (  modulefile  ) /*before: iso reader*/;
		
		//InputStream str = URLUtils.openFileOrURL( modulefile );
		
		//InputSource is = new InputSource(str);
		
		StreamSource ss; 
		ss = new StreamSource ( moduleFile );
		
		Transformer t = TransformerFactory.newInstance().newTransformer();
		DOMResult r = new DOMResult();
		t.transform(ss,r);
		
		//DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		//io.escribir(io.getColorCode("information") + "Obteniendo árbol DOM de los datos XML...\n" + io.getColorCode("reset") );
		//d = db.parse(ss , new File(modulefile).toURI().toString() /*systemId*/ );
		return (org.w3c.dom.Document) r.getNode();
	}
	
	public static GameInfo getGameInfo ( String modulefile ) throws FileNotFoundException, IOException
	{
	
                System.out.println("getGameInfo called on " + modulefile);
            
		//si readLine lee null, se acabó el fichero.
		String linea="";
		String token="";
		String[] moduleInfo = 
		{
			"?","?","?","?","?"
		};

		boolean useAlternativeFile = false;


		//if the gamefile type is XML, then
			//search for a .dat equivalent of the XML file, where info summary is contained
			//if (exists)
				//just process that one
			//if ( doesn't)
				//parse XML using DOM parser
				//get world info
				//use it
				//create .dat equivalent for use the next time

		if ( ( modulefile.toLowerCase().endsWith ( ".xml" ) || modulefile.toLowerCase().endsWith ( ".agw" ) || modulefile.toLowerCase().endsWith ( ".asf" /*state*/ ) ) )
		{
		
			//create .dat equivalent of .xml file [only if file is a real file]
		
			File resFile = null;
			if ( new File(modulefile).exists() )
				resFile = new File ( new File(modulefile).getAbsolutePath().substring(0,new File(modulefile).getAbsolutePath().length()-4) + ".res" );
			if ( resFile!= null && resFile.exists() )
			{
                            
                                System.out.println("RES file exists.\n");
                            
				//continuamos con la datFile
				//System.out.println("RES file exists " + resFile.getAbsolutePath());
				useAlternativeFile = true;
			}
			else
			{
			
				//read the XML (slow!)
                            
                                System.out.println("RES file doesn't exist.\n");
                            
				org.w3c.dom.Document d = null;
				try
				{
					d = documentFromFile(modulefile);
				}
				catch ( TransformerException te )
				{
					if ( modulefile.endsWith(".xml") )
						modulefile = modulefile.substring(0,modulefile.toString().length()-3) + "agw";
					try
					{
						d = documentFromFile(modulefile);
					}
					catch ( TransformerException te2 )
					{
						//we report the first exception
						System.err.println(te);
						System.err.println("Trying with .agw extension wasn't successful either");
						return null;
					}
				}
				//obtain the DOM tree root
				org.w3c.dom.Element n = d.getDocumentElement();
				//obtain the information
				if ( n.hasAttribute("moduleName") )
					moduleInfo[0] = n.getAttribute("moduleName");
				if ( n.hasAttribute("author") )
					moduleInfo[1] = n.getAttribute("author");
				if ( n.hasAttribute("version") )
					moduleInfo[2] = n.getAttribute("version");
				if ( n.hasAttribute("date") )
					moduleInfo[3] = n.getAttribute("date");
				if ( n.hasAttribute("parserVersion") )
					moduleInfo[4] = n.getAttribute("parserVersion");	
				//create the file for easy access to this information on other executions
				if ( resFile != null )
				{
					PrintWriter pw = new PrintWriter ( new OutputStreamWriter ( new FileOutputStream ( resFile ) , "UTF-8" ) );
					pw.println("comment Fichero resumen de información de mundo generado por Aetheria Game Engine el " + java.text.DateFormat.getDateTimeInstance().format ( new Date() ) + " a partir de " + modulefile );
					pw.println("modulename " + moduleInfo[0]);
					pw.println("author " + moduleInfo[1]);
					pw.println("version " + moduleInfo[2]);
					pw.println("date " + moduleInfo[3]);
					pw.println("parserversion " + moduleInfo[4]);
					pw.flush();
					pw.close();
					System.out.println("Print Writer closed");
				}
				return new GameInfo(moduleInfo , modulefile);
				
			}
			
		
		}


		FileInputStream fp = null;
		java.io.BufferedReader filein = null;

		//if the gamefile type is XML but we had a .res file
		if ( useAlternativeFile )
		{
			File resFile = new File ( new File(modulefile).getAbsolutePath().substring(0,new File(modulefile).getAbsolutePath().length()-4) + ".res" );
			fp = new FileInputStream(resFile);
			filein = new java.io.BufferedReader ( new java.io.InputStreamReader ( fp , "UTF-8" ) );
			
		}

		//if the gamefile type is not XML
		else
		{
			fp = new FileInputStream(modulefile);
			filein = new java.io.BufferedReader ( new java.io.InputStreamReader ( fp , "UTF-8" ) );
		}
		
		while( true )
		{
			linea=filein.readLine();
			if ( linea == null ) break;
			token = StringMethods.getTok( linea,1,' ' );	
			//if ( token.equalsIgnoreCase("module") ) worldname=StringMethods.getTok( linea,2,' ' );
			//else if ( token.equalsIgnoreCase("maxroom") ) maxroom = (Integer.valueOf( StringMethods.getTok( linea,2,' ' )).intValue() );
			//else if ( token.equalsIgnoreCase("maxitem") ) maxitem = (Integer.valueOf( StringMethods.getTok( linea,2,' ' )).intValue() );
			//else if ( token.equalsIgnoreCase("maxmob") ) maxmob = (Integer.valueOf( StringMethods.getTok( linea,2,' ' )).intValue() );	
			//else if ( token.equalsIgnoreCase("printthis") ) escribir(StringMethods.getToks(linea,2,StringMethods.numToks(linea,' '),' ')+"\n");
			if ( token.equalsIgnoreCase("modulename" ) ) moduleInfo[0]=StringMethods.getToks ( linea , 2 , StringMethods.numToks ( linea , ' ' ) , ' ' );
			
			else if ( token.equalsIgnoreCase("author") ) moduleInfo[1] = StringMethods.getToks(linea,2,StringMethods.numToks(linea,' '),' ');
			else if ( token.equalsIgnoreCase("version") ) moduleInfo[2] = StringMethods.getToks(linea,2,StringMethods.numToks(linea,' '),' ');
			else if ( token.equalsIgnoreCase("date") ) moduleInfo[3] = StringMethods.getToks(linea,2,StringMethods.numToks(linea,' '),' ');
			else if ( token.equalsIgnoreCase("parserversion") ) moduleInfo[4] = StringMethods.getToks(linea,2,StringMethods.numToks(linea,' '),' ');

			else if ( token.equalsIgnoreCase("begin_eva_code") ) //pasar de todo
			{
				boolean terminamos = false;
				while ( !terminamos )
				{
					linea = filein.readLine();
					String id_linea = StringMethods.getTok(linea,1,' ');
					if ( id_linea.equalsIgnoreCase("end_eva_code") ) terminamos=true; //EVA code termination line
					else
					{
						;
					}
				}	
			}
			else if ( token.equalsIgnoreCase("begin_bsh_code") )
			{
				boolean terminamos = false;
				while ( !terminamos )
				{
					linea = filein.readLine();
					String id_linea = StringMethods.getTok(linea,1,' ');
					if ( id_linea.equalsIgnoreCase("end_bsh_code") ) terminamos=true; //EVA code termination line
					else
					{
							;
					}
				}
			}	
		} //end while true
		
		//System.out.println("Moduleinfo of 0 is " + moduleInfo[0]);
		
		return new GameInfo(moduleInfo , modulefile);
		
	} //end method getGameInfo
		
		
	public static GameInfo[] getListOfGames()
	{

                System.out.println("getListOfGames() called\n");
            
        
        File cwd = new File ( Paths.getWorkingDirectory() );
        
		File worldsDirectory = new File ( cwd.getAbsolutePath() + File.separatorChar + Paths.WORLD_PATH );
		
		if ( !worldsDirectory.exists() )
		{
			if ( worldsDirectory.mkdir() )
			{
				System.out.println( "Worlds directory didn't exist, created at " + Paths.WORLD_PATH );
			}
			else
			{
				System.err.println("Could not create worlds directory at " + Paths.WORLD_PATH );
			}
		}
		
		File[] worldsSubdirectories = worldsDirectory.listFiles();
		
		Vector result = new Vector();
		
		for ( int i = 0 ; i < worldsSubdirectories.length ; i++ )
		{
			if ( worldsSubdirectories[i].isDirectory() )
			{
				File[] fl2 = worldsSubdirectories[i].listFiles();
				for ( int j = 0 ; j < fl2.length ; j++ )
				{
                                    
                                        //System.out.println("File: " + fl2[j].getName());
                                    
					if ( fl2[j].getName().equalsIgnoreCase("world.dat") || fl2[j].getName().equalsIgnoreCase("world.xml") )
					{
						try
						{
							result.addElement ( getGameInfo( fl2[j].getAbsolutePath() ) );
						}
						catch ( IOException ioe )
						{
							System.out.println(ioe);ioe.printStackTrace();
						}
					}
					else if ( fl2[j].getName().endsWith(".agz") )
					{
						addInfoFromCompressedFile(fl2[j],result);
					}
					
					
				}
			}
			else if ( worldsSubdirectories[i].isFile() )
			{
				if ( worldsSubdirectories[i].getName().endsWith(".agz") )
				{
					addInfoFromCompressedFile(worldsSubdirectories[i],result);
				}
			}
			
		}
		
		Object[] objetos = result.toArray();
		GameInfo[] ficheros = new GameInfo[objetos.length];
		for ( int i = 0 ; i < objetos.length ; i++ )
			ficheros[i] = (GameInfo)objetos[i];
		
		return ( ficheros );
		
	}
	
	public static void addInfoFromCompressedFile ( File f , List result )
	{
		try
		{
			result.add(getGameInfo(WorldLoader.goIntoFileIfCompressed(f.getAbsolutePath())));
		}
		catch ( IOException ioe )
		{
			System.out.println(ioe);ioe.printStackTrace();
		}
	}
	
}


