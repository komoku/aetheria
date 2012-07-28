/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
//package AetheriaAWT;
/**
* Funciones útiles en el juego.
*
* @author Carlos Gómez
*/
import java.io.*;
import java.util.*;

import eu.irreality.age.i18n.UIMessages;
public class Utility
{

	/**Constantes*/
	
	public static final String room_preffix = "10";
	public static final String mobile_preffix = "20";
	public static final String item_preffix = "30";
	public static final String absent_preffix = "40";
	public static final String spell_preffix = "50";
	
	public static final int room_summand = 10000000;
	public static final int mobile_summand = 20000000;
	public static final int item_summand = 30000000;
	public static final int absent_summand = 40000000;
	public static final int spell_summand = 50000000;




	//ej. si el número es el 1000 y el stdevfactor es 0.1, nos desviará el número típicamente 100.
	public static double applyGaussianVariation ( double number , java.util.Random r , double stdevFactor )
	{
		return number + ( r.nextGaussian() * number * stdevFactor );
	}
	

	/**
	* Nos devuelve un InputStreamReader que a ser posible lea UTF-8, para ficheros.
	*/
	public static InputStreamReader getBestInputStreamReader ( InputStream is )
	{
		InputStreamReader isr;
		try
		{
			isr = new InputStreamReader ( is , "UTF-8" );
		}
		catch ( UnsupportedEncodingException uee )
		{
			System.out.println(UIMessages.getInstance().getMessage("iso.encoding.warning") + "\n");
			isr = new InputStreamReader ( is );
		}
		return isr;
	}
	
	/**
	* Nos devuelve un OutputStreamWriter que a ser posible escriba UTF-8, para ficheros.
	*/
	public static OutputStreamWriter getBestOutputStreamWriter ( OutputStream os )
	{
		OutputStreamWriter osw;
		try
		{
			osw = new OutputStreamWriter ( os , "UTF-8" );
		}
		catch ( UnsupportedEncodingException uee )
		{
			System.out.println(UIMessages.getInstance().getMessage("iso.encoding.warning") + "\n");
			osw = new OutputStreamWriter ( os );
		}
		return osw;
	}



	/**
	* Completa una ID incompleta a roomID
	*
	* @param id la ID incompleta (ya completa también sirve)
	* @return La ID en formato roomID, con el prefijo correspondiente
	*/
	public static int completeRoomID ( int ID )
	{
		return ( ID > room_summand ? ID : ID + room_summand );	
	}
	
	/**
	* Completa una ID incompleta a itemID
	*
	* @param id la ID incompleta (ya completa también sirve)
	* @return La ID en formato itemID, con el prefijo correspondiente
	*/
	public static int completeItemID ( int ID )
	{
		return ( ID > item_summand ? ID : ID + item_summand );	
	}
	
	/**
	* Completa una ID incompleta a mobileID
	*
	* @param id la ID incompleta (ya completa también sirve)
	* @return La ID en formato mobileID, con el prefijo correspondiente
	*/
	public static int completeMobileID ( int ID )
	{
		return ( ID > mobile_summand ? ID : ID + mobile_summand );	
	}

	/**
	* Completa un número con ceros hasta alcanzar nzeroes cifras.
	*
	* @param n el número a completar.
	* @param nzeroes número de cifras a alcanzar.
	* @return Un string con el número de nzeroes cifras resultante.
	*/
	public static String completeWithZeroes ( int n , int nzeroes )
	{
		String s = String.valueOf(n);
		while (s.length()<nzeroes)
			s = "0" + s;
		return s;	
	}
	/**
	* Nos da el nombre y ruta de un fichero de habitación.
	*
	* @param n el número de habitación.
	* @param mundo el mundo.
	* @return Un string con el nombre del fichero requerido.
	*/
	public static String roomFile ( World mundo, int n )
	{
		String s = completeWithZeroes( n,6 );
		return ( mundo.getWorldPath() + "objects" + File.separatorChar + room_preffix + s + ".ae" );	
	}
	
	/**
	* Nos da el nombre y ruta de un fichero de objeto.
	*
	* @param n el número de objeto (no id completa).
	* @param mundo el mundo.
	* @return Un string con el nombre del fichero requerido.
	*/
	public static String itemFile ( World mundo , int n )
	{
		String s = completeWithZeroes( n,6 );
		return ( mundo.getWorldPath() + "objects" + File.separatorChar + item_preffix + s + ".ae" );	
	}
	
	
	public static String playerFile ( World mundo )
	{
		return ( mundo.getWorldPath() + "objects" + File.separatorChar + mobile_preffix + "000000" + ".ae" );	
	}
	
	/**
	* Nos da el nombre y ruta de un fichero de bicho.
	*
	* @param n el número de bicho (no id completa).
	* @param mundo el mundo.
	* @return Un string con el nombre del fichero requerido.
	*/
	public static String mobFile ( World mundo , int n )
	{
		String s = completeWithZeroes( n,6 );
		return ( mundo.getWorldPath() + "objects" + File.separatorChar + mobile_preffix + s + ".ae" );	
	}
	
	/**
	* Carga una lista (array dinámico) de descripciones con comparando y máscara en memoria. 
	*
	* @param linea El string del fichero.
	* @return theList El array.
	*/
	public static Description[] loadDescriptionListFromString ( String linea )
	{
		if ( linea == null ) return new Description[0];
		if ( linea.indexOf('&') < 0 )
		{
			//si no hay &'s, se supone que es una sola descripción para todos los estados:
			//equivale a 0&0&...
			Description[] theList = new Description[1];
			theList[0] = new Description ( linea,0,0 );
			return theList;
		}
		else
		{
			Description[] theList = new Description[ StringMethods.numToks(linea,'&')/3 ];
			for ( int i = 0 ; i+3 <= StringMethods.numToks(linea,'&') ; i+=3 )
			{
				String text = StringMethods.getTok( linea,i+3,'&' );
				long comp = Long.valueOf(StringMethods.getTok( linea,i+1,'&' )).longValue();
				long mask = Long.valueOf(StringMethods.getTok( linea,i+2,'&' )).longValue();
				theList[i/3] = new Description ( StringMethods.textualSubstitution( text, "\\n" , "\n" ) , comp  , mask ) ;
				//comparando:token i+1
				//mascara:token i+2
				//texto:token i+3	
			}	
			return theList;
		}
	}
	
	//devuelve un List cuyo primer elemento es un List of String[] y el segundo un List of Description[] (con nombres correspondientes)
	public static List loadExtraDescriptionsFromXML ( World mundo , org.w3c.dom.Element e , String tagName , boolean nullifyIfNotFound ) throws XMLtoWorldException
	{
		org.w3c.dom.NodeList interestingNodes = e.getElementsByTagName(tagName);
		if ( interestingNodes.getLength() > 0 )
		{
			//nos interesa solo el primero
			org.w3c.dom.Element interestingNode = (org.w3c.dom.Element)interestingNodes.item(0);
			try
			{
				return Utility.loadExtraDescriptionsFromXML ( mundo , interestingNode );
			}
			catch ( XMLtoWorldException ex )
			{
				throw ( new XMLtoWorldException ( "When parsing " + tagName + ": " + ex.getMessage() ) );
			}
		}
		else
		{
			return new ArrayList();
		}
	
	}
	
	//continuacion del anterior
	public static List loadExtraDescriptionsFromXML ( World mundo , org.w3c.dom.Element exDesListNode  ) throws XMLtoWorldException
	{
	
		List laLista = new ArrayList();
		List listaArraysNombres = new ArrayList();
		List listaArraysDescripciones = new ArrayList();
		laLista.add ( listaArraysNombres );
		laLista.add ( listaArraysDescripciones );
	
		org.w3c.dom.NodeList exDesNodes = exDesListNode.getElementsByTagName("ExtraDescription");
		if ( exDesNodes.getLength() > 0 )
		{
			for ( int i = 0 ; i < exDesNodes.getLength() ; i++ )
			{
				
				org.w3c.dom.Element currentExDes = (org.w3c.dom.Element)exDesNodes.item(i);
			
				//load names
			
				org.w3c.dom.NodeList nameNodes = currentExDes.getElementsByTagName("Name");
				String[] nameArray = new String[nameNodes.getLength()];
				for ( int j = 0 ; j < nameNodes.getLength() ; j++ )
				{
					org.w3c.dom.Element hijo = (org.w3c.dom.Element)nameNodes.item(j);
					org.w3c.dom.Node nieto = hijo.getFirstChild();
					//buscar primer Text (deberia haber solo un hijo y Text)
					while ( !(nieto instanceof org.w3c.dom.Text ) )
					{
						nieto = nieto.getNextSibling();
					}
					nameArray[j]=(nieto.getNodeValue());
				}
			
				//names loaded
				//load descriptions represented as DescriptionList elements
				Description[] desArray = Utility.loadDescriptionListFromXML ( mundo , currentExDes , "DescriptionList" , false );			
				
				if ( nameArray.length > 0 && desArray.length > 0 )
				{
					listaArraysNombres.add ( nameArray );
					listaArraysDescripciones.add ( desArray );
				}
				else
				{
					//no "serious" descriptions, watch for text nodes
					//search first text node
					org.w3c.dom.Node hijo = currentExDes.getFirstChild();
					while ( !(hijo instanceof org.w3c.dom.Text ) || hijo.getNodeValue().trim().length()==0 )
					{
						hijo = hijo.getNextSibling();
					}
					if ( hijo != null && nameArray.length > 0 )
					{
						listaArraysNombres.add ( nameArray );
						listaArraysDescripciones.add ( new Description[] 
						{
							new Description(hijo.getNodeValue().trim(),0,0)
						} );
					}
				}

			
			} //foreach <ExtraDescription> node [tries to add an element to both lists]
		} //if there are <ExtraDescription> nodes				 
	
		return laLista;
	
	}
	
	//toma el Node correspondiente a la entidad completa, busca el de descripciones de ese tipo si lo hay,
	//y llama a su funcion hermana en Utility para construir las descripciones 
	//(es decir, busca un nodo etiquetado con tagName dado que dentro a su vez tenga un nodo <Description>)
	public static Description[] loadDescriptionListFromXML ( World mundo , org.w3c.dom.Element e , String tagName , boolean nullifyIfNotFound ) throws XMLtoWorldException
	{
		org.w3c.dom.NodeList interestingNodes = e.getElementsByTagName(tagName);
		if ( interestingNodes.getLength() > 0 )
		{
			//nos interesa solo el primero
			org.w3c.dom.Element interestingNode = (org.w3c.dom.Element)interestingNodes.item(0);
			try
			{
				return Utility.loadDescriptionListFromXML ( mundo , interestingNode );
			}
			catch ( XMLtoWorldException ex )
			{
				throw ( new XMLtoWorldException ( "When parsing " + tagName + ": " + ex.getMessage() ) );
			}
		}
		else
		{
			if ( nullifyIfNotFound ) return null;
			else return new Description[0];
		}
	}
	
	//carga una Description[] de los nodos etiquetado <Description> que haya dentro del nodo que le pasamos.
	public static Description[] loadDescriptionListFromXML ( World mundo , org.w3c.dom.Element descrListNode ) throws XMLtoWorldException 
	{

			org.w3c.dom.NodeList descrNodes = descrListNode.getElementsByTagName ( "Description" );
			Description[] ourArray = new Description[descrNodes.getLength()];
			for ( int i = 0 ; i < descrNodes.getLength() ; i++ )
			{
				org.w3c.dom.Element descrNode = (org.w3c.dom.Element) descrNodes.item(i);
				try
				{
					ourArray[i] = new Description(mundo,descrNode);
				}
				catch ( XMLtoWorldException xe )
				{
					throw ( new XMLtoWorldException ( "Error at description: " + xe.getMessage()  ) );
				}
			}
			return ourArray;
			
	}
	
	private static String loadNameListFromXML ( World mundo , org.w3c.dom.Element namesNode ) throws XMLtoWorldException
	{

		//an example of this is singular reference names (respondToSing), XML format is:
		//<SingularReferenceNames><Name>nombre1</Name><Name>nombre2</Name>...</SingularReferenceNames>
		//while internal format is
		//nombre1$nombre2
		
		
		/*Do this to get the node in which to call this function:
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName("SingularReferenceNames" );
		if ( singRefNamesNodes.getLength() > 0 )
		{
			org.w3c.dom.Element singRefNamesNode = (org.w3c.dom.Element)singRefNamesNodes.item(0);
		}
		*/	
			org.w3c.dom.NodeList nameNodes = namesNode.getElementsByTagName("Name");
			
			//init ourString
			String ourString = "";
			
			for ( int i = 0 ; i < nameNodes.getLength() ; i++ )
			{
				//get this name node
				org.w3c.dom.Element nameNode = (org.w3c.dom.Element) nameNodes.item(i);
				
				//get first text node in this name node -- WE ASSUME THERE IS ONE!!
				org.w3c.dom.Node hijo = nameNode.getFirstChild();
				while ( !( hijo instanceof org.w3c.dom.Text ) )
					hijo = hijo.getNextSibling();
				
				//{hijo is an org.w3c.dom.Text}
				
				ourString += ( hijo.getNodeValue() );			
					
				if ( i < nameNodes.getLength()-1 ) //i.e. not last
					ourString += "$";	
			}
		
		return ourString;
	
	}
	
	//e: entity Element
	public static String loadNameListFromXML ( World mundo , org.w3c.dom.Element e , String tagName , boolean nullifyIfNotFound ) throws XMLtoWorldException
	{
	
		org.w3c.dom.NodeList singRefNamesNodes = e.getElementsByTagName ( tagName );
		if ( singRefNamesNodes.getLength() > 0 )
		{
			org.w3c.dom.Element singRefNamesNode = (org.w3c.dom.Element)singRefNamesNodes.item(0);
			
			return loadNameListFromXML ( mundo , singRefNamesNode );
		}
		else
		{
			if ( nullifyIfNotFound ) 
				return null;
			else
				return "";	
		}
	
	}
	
	//obsolete!
	public static String loadExtraDescriptionsFromXML (  org.w3c.dom.Element e , String tagName , boolean nullifyIfNotFound ) throws XMLtoWorldException
	{
		org.w3c.dom.NodeList extraDesListNodes = e.getElementsByTagName( tagName );
		if ( extraDesListNodes.getLength() > 0 )
		{
			org.w3c.dom.Element theExtraDescriptionListNode = (org.w3c.dom.Element)extraDesListNodes.item(0);
			
			//build extra descriptions from the node
		
			return loadExtraDescriptionsFromXML ( theExtraDescriptionListNode );
		
		}
		else
		{
			if ( nullifyIfNotFound ) 
				return null;
			else
				return "";	
		}
	}
	
	//obsolete!
	private static String loadExtraDescriptionsFromXML ( org.w3c.dom.Element extraDesElement ) throws XMLtoWorldException
	{
		String extraDescriptions="";
		org.w3c.dom.NodeList extraDesNodes = extraDesElement.getElementsByTagName("ExtraDescription");
		for ( int i = 0 ; i < extraDesNodes.getLength() ; i++ )
		{
			org.w3c.dom.Element extraDesNode = (org.w3c.dom.Element) extraDesNodes.item(i);
			org.w3c.dom.Node hijo = extraDesNode.getFirstChild();
			while ( !( hijo instanceof org.w3c.dom.Text ) )
			{
				//nodos Element con un nombre (Name), si no cambia el formato.
				if ( ((org.w3c.dom.Element)hijo).getTagName().equalsIgnoreCase("Name") )
				{
					org.w3c.dom.Node nieto = hijo.getFirstChild();
					//buscar primer Text (deberia haber solo un hijo y Text)
					while ( !(nieto instanceof org.w3c.dom.Text ) )
					{
						nieto = nieto.getNextSibling();
					}
					extraDescriptions+=nieto.getNodeValue();
					extraDescriptions+="$";
				}
				hijo = hijo.getNextSibling();
			}
			//hijo is now a Text: the extra des text.
			extraDescriptions+=hijo.getNodeValue();
			if ( i < extraDesNodes.getLength() - 1 ) //i.e. not last
				extraDescriptions+="@";
		}
		return extraDescriptions;
	}

}