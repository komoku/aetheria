package eu.irreality.age;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;

public class SaveInfo
{

	File fichSalv;
	Date fecha;
	String name;
	GameInfo infoJuego;

	public SaveInfo () 
	{
	;
	}

	public SaveInfo ( File fichSalv , Date fecha , String fichJuego )
	{
		this.fichSalv = fichSalv;
		this.fecha = fecha;
		this.name = fichSalv.getName();
		this.infoJuego = GameInfo.getGameInfoFromFile( fichJuego );
	}
	
	public File getFile()
	{
		return fichSalv;
	}
	
	public String getGameFile()
	{
		if ( infoJuego == null ) return null;
		else return infoJuego.getFile();
	}
	
	public GameInfo getGameInfo ( )
	{
		return infoJuego;
	}
	
	public String getName()
	{
		return name;
	}
	public Date getDate()
	{
		return fecha;
	}
	
	public String toString ( )
	{
		return getName();
	}
	
	public String toLongString (  )
	{
		String nameMsg = UIMessages.getInstance().getMessage("save.name");
		String dateMsg = UIMessages.getInstance().getMessage("save.date");
		String gameMsg = UIMessages.getInstance().getMessage("save.game");
		if ( infoJuego == null )
			return nameMsg + " " + getName() + "\n" + dateMsg + " " + getDate(); //
		else
			return nameMsg + " " + getName() + "\n" + dateMsg + " " + getDate() + "\n" + gameMsg + " " + infoJuego.toString();
	}
	
	public static SaveInfo getSaveInfo ( File savefile ) throws FileNotFoundException, IOException
	{
		FileInputStream fp = new FileInputStream(savefile);
		java.io.BufferedReader filein =
			new java.io.BufferedReader 	(
			new java.io.InputStreamReader 	(fp));
			
		String fichJuego = filein.readLine();	
		Date fecha = new Date(savefile.lastModified());
		
		System.out.println(savefile);
		System.out.println(fichJuego);
		try
		{
			return new SaveInfo ( savefile , fecha , fichJuego );	
		}
		catch ( Exception fnfe )
		{
			return null;
		}
	}
	
	public static SaveInfo[] getListOfSaves()
	{
	
		File f = new File ( Paths.getWorkingDirectory()  + File.separatorChar + Paths.SAVE_PATH  );
		
		if ( !f.exists() )
		{
			if ( f.mkdir() )
			{
				System.out.println( "Saves directory didn't exist, created at " + Paths.SAVE_PATH );
			}
			else
			{
				System.err.println("Could not create saves directory at " + Paths.SAVE_PATH );
			}
		}
		
		File[] fl = f.listFiles();
		
		Vector result = new Vector();
		
		for ( int i = 0 ; i < fl.length ; i++ )
		{
			if ( fl[i].isDirectory() )
			{
				File[] fl2 = fl[i].listFiles();
				for ( int j = 0 ; j < fl2.length ; j++ )
				{
					if ( fl2[j].getName().toLowerCase().endsWith(".alf") )
					{
						try
						{
							result.addElement ( getSaveInfo( fl2[j] ) );
						}
						catch ( IOException ioe )
						{
							System.out.println(ioe);ioe.printStackTrace();
						}
					}
				}
			}
			else if ( fl[i].getName().toLowerCase().endsWith(".alf") )
			{
					try
					{
						result.addElement ( getSaveInfo( fl[i] ) );
					}
					catch ( IOException ioe )
					{
						System.out.println(ioe);ioe.printStackTrace();
					}
			}
		}
		
		Object[] objetos = result.toArray();
		SaveInfo[] ficheros = new SaveInfo[objetos.length];
		for ( int i = 0 ; i < objetos.length ; i++ )
			ficheros[i] = (SaveInfo)objetos[i];
		
		return ( ficheros );
		
	}
	
}
