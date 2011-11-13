/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.gui.codeassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.f2o.absurdum.puck.i18n.UIMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class CodeAssistMenuHandler 
{

	private Document codeAssistDoc;
	private Element codeAssistRoot;
	
	private static CodeAssistMenuHandler instance;
	
	private boolean useDescriptions = true; //use descriptions as menu names
	
	private CodeAssistMenuHandler()
	{	
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/codeAssist.xml");
			if ( is == null ) throw new IOException("getResourceAsStream returned null stream for codeAssist.xml");
		
			Transformer t = TransformerFactory.newInstance().newTransformer();
			Source s = new StreamSource(is);
			DOMResult r = new DOMResult();
			t.transform(s,r);
			codeAssistDoc = (Document) r.getNode();	
			codeAssistRoot = (Element) codeAssistDoc.getFirstChild();
		
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		} 
		catch (TransformerConfigurationException e) 
		{
			e.printStackTrace();
		} 
		catch (TransformerFactoryConfigurationError e) 
		{
			e.printStackTrace();
		}
		catch (TransformerException te)
		{
			te.printStackTrace();
		}
	}
	
	public static CodeAssistMenuHandler getInstance()
	{
		if ( instance == null )
			instance = new CodeAssistMenuHandler();
		return instance;
	}
	
	
	private HashMap codeTemplateCache = new HashMap();
	
	private String getCodeTemplateContent ( String templateLocation )
	{
		String cached = (String) codeTemplateCache.get(templateLocation);
		if ( cached != null ) return cached;
		try
		{
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/codetemplates/"+templateLocation);
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader ( new InputStreamReader ( stream ) );
			String linea = "";
			while ( linea != null )
			{
				linea = br.readLine();
				if ( linea != null ) sb.append(linea+"\n");
			}
			codeTemplateCache.put(templateLocation,sb.toString());
			return sb.toString();
		}
		catch ( Exception e )
		{
			System.err.println("Can't read template at " + templateLocation + ".");
			e.printStackTrace();
			return "//template unrecoverable due to error, at " + templateLocation;
		}
	}
	
	private JMenuItem getMenuItem ( Element elt , CodeInsertActionBuilder builder )
	{
		JMenuItem result;
		String label;
		if ( useDescriptions )
			label = ( elt.getAttribute("description") );
		else
			label = ( elt.getAttribute("name") );
		result = new JMenuItem(label);
		//System.out.println("*Item: " + result.getText());
		String location = elt.getAttribute("template-ref");
		
		/*
		try
		{
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/codetemplates/"+location);
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader ( new InputStreamReader ( stream ) );
			String linea = "";
			while ( linea != null )
			{
				linea = br.readLine();
				if ( linea != null ) sb.append(linea+"\n");
			}
			*/
			String templateContent = getCodeTemplateContent(location);
			Action a = builder.getInsertAction(templateContent);
			a.putValue(Action.NAME, label);
			result.setAction(a);
		/*
		}
		catch ( Exception e )
		{
			System.err.println("Can't read template at " + location);
			e.printStackTrace();
		}
		*/
		
		return result;
	}
	
	//private HashMap menusCache = new HashMap();
	
	public JMenu getMenuForContext ( String context , CodeInsertActionBuilder builder )
	{
		//JMenu cached = (JMenu) menusCache.get(context);
		//if ( cached != null ) 
		//	return cached;
		//else
		//{
			JMenu result = getMenuForContext ( context , codeAssistRoot , builder );
		//	menusCache.put(context,result);
			return result;
		//}
	}

	
	private JMenu getMenuForContext ( String context , Element elt , CodeInsertActionBuilder builder )
	{
		
		JMenu result;
		if ( "menu".equals(elt.getTagName()) )
		{
			if ( useDescriptions )
				result = new JMenu ( elt.getAttribute("description") );
			else
				result = new JMenu ( elt.getAttribute("name") );
		}
		else
			result = new JMenu ( UIMessages.getInstance().getMessage("menu.codeassist") );
		
		//System.out.println("result: " + result);
		
		NodeList eltList = elt.getChildNodes();
		for ( int i = 0 ; i < eltList.getLength() ; i++ )
		{
			Node child = eltList.item(i);
			//System.out.println("ch: " + child);
			if ( child instanceof Element )
			{
				Element childElt = (Element) child;
				//System.out.println("Tag:" + childElt.getTagName());
				//System.out.println("Context:" + childElt.getAttribute("context"));
				//System.out.println("OContext:" + context);
				if ( "menu".equals(childElt.getTagName()) )
				{
					if ( "".equals(childElt.getAttribute("context"))
							|| (childElt.getAttribute("context") != null && childElt.getAttribute("context").contains(context) )  )
					{
						//context.equals(childElt.getAttribute("context"))
						//System.out.println("Submenu?");
						//ok, this menu is valid for this context
						JMenu subMenu = getMenuForContext(context,childElt,builder);
						result.add(subMenu);
						//System.out.println("*Submenu: " + subMenu.getLabel());
					}
				}
				else if ( "item".equals(childElt.getTagName()) )
				{
					JMenuItem theItem = getMenuItem(childElt,builder);
					result.add(theItem);
				}
			}
		}
		
		return result;
		
	}
	
	
	
}
