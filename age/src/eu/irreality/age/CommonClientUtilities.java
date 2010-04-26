package eu.irreality.age;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

public class CommonClientUtilities
{
    
    public static void guardarLog ( File f , Vector gameLog ) throws java.io.IOException , java.io.FileNotFoundException
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter fwrite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		
		for ( int i = 0 ; i < gameLog.size() ; i++ )
		{
			System.out.println("Saving to " + f + ": " + (String)gameLog.elementAt(i) );
			fwrite.println( (String)gameLog.elementAt(i) );
		}
		
		fwrite.flush();	
	}
	
	public static void guardarEstado ( File f , World mundo ) throws java.io.IOException , java.io.FileNotFoundException 
	{
		FileOutputStream fin = new FileOutputStream ( f );
		PrintWriter frwite = new java.io.PrintWriter ( new java.io.BufferedWriter ( Utility.getBestOutputStreamWriter ( fin ) ) );
		
		org.w3c.dom.Document d = null;
		try
		{
			d = mundo.getXMLRepresentation();
			System.out.println("On saving state, is D=null?" + (d==null) );
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

}
