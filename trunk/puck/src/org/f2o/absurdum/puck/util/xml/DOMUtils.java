/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

/*
 * Created at regulus on 08-abr-2007 11:44:59
 * as file DOMUtils.java on package org.f2o.absurdum.puck.util.xml
 */
package org.f2o.absurdum.puck.util.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author carlos
 *
 * Created at regulus, 08-abr-2007 11:44:59
 */
public class DOMUtils 
{
	
	
	public static NodeList getDirectChildrenElementsByTagName ( Node n , String name )
	{
	
		ArrayNodeList theList = new ArrayNodeList();
		if ( !n.hasChildNodes() ) return theList;
		Node current = n.getFirstChild();
		do
		{
			if ( current instanceof Element )
			{
				Element curElt = (Element) current;
				if ( curElt.getTagName().equals(name) ) theList.add(curElt);
			}
			current = current.getNextSibling();
		} while ( current != null );
		
		return theList;
		
	}
	
	
	private static Document doc = null;
	

	public static Document getXMLClipboard ( )
	{
		if ( doc == null )
		{
			try 
			{
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			} 
			catch (ParserConfigurationException e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		return doc;
	}
	
	
	public static String nodeToString ( Node n )
	{
		DOMSource ds = new DOMSource(n);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult ( sw );
		try
		{
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT,"yes");
			t.transform(ds,sr);
		}
		catch ( TransformerException te )
		{
			te.printStackTrace();
		}
		return sw.getBuffer().toString();
	}
	
	public static Node stringToNode ( String s )
	{
		StreamSource ss = new StreamSource(new StringReader(s));
		DOMResult dr = new DOMResult();
		try
		{
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.transform(ss,dr);
		}
		catch ( TransformerException te )
		{
			te.printStackTrace();
			return null;
		}
		return dr.getNode().getFirstChild();
	}
	
}
