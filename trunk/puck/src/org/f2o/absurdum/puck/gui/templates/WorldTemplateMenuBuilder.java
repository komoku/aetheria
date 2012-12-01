package org.f2o.absurdum.puck.gui.templates;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.codeassist.CodeAssistMenuHandler;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WorldTemplateMenuBuilder 
{

	private Document worldTemplateDoc;
	private Element worldTemplateRoot;
	
	private PuckFrame pf;
	
	private boolean useDescriptions = true; //use xml descriptions instead of names in menu items
	
	public WorldTemplateMenuBuilder(PuckFrame pf)
	{
		this.pf = pf;
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/worldTemplates.xml");
			if ( is == null ) throw new IOException("getResourceAsStream returned null stream for worldTemplates.xml");
		
			Transformer t = TransformerFactory.newInstance().newTransformer();
			Source s = new StreamSource(is);
			DOMResult r = new DOMResult();
			t.transform(s,r);
			worldTemplateDoc = (Document) r.getNode();	
			worldTemplateRoot = (Element) worldTemplateDoc.getFirstChild();
		
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
	
	/**
	 * Returns a menu with the templates for world creation, in the current UI language of, if there are no templates for that language, then in English.
	 * @return
	 */
	public JMenu getMenu()
	{
		JMenu inOurLanguage = getMenu ( UIMessages.getInstance().getPreferredLanguage() );
		if ( inOurLanguage != null ) return inOurLanguage;
		else return getMenu("en");
	}
	
	public JMenu getMenu( String preferredLanguage )
	{
		if ( worldTemplateRoot == null ) return null;
		NodeList nl = worldTemplateRoot.getElementsByTagName("templates");
		for ( int i = 0 ; i < nl.getLength(); i++ )
		{
			Element elt = (Element) nl.item(i);
			if ( preferredLanguage.equals(elt.getAttribute("language")) )
			{
				//this element contains the menus for our language (i.e. the ones we want)
				return (JMenu) getMenu(elt);
			}
		}
		//no templates found for our language
		return null;
	}
	
	
	public JMenuItem getMenu( Element elt )
	{
		if ( "templates".equals(elt.getTagName()) || "menu".equals(elt.getTagName()) ) //top-level or non-top-level menu
		{
			//recursively traverse nodes and create a menu with their corresponding submenus
			JMenu result;
			if ( useDescriptions )
				result = new JMenu ( elt.getAttribute("description") );
			else
				result = new JMenu ( elt.getAttribute("name") );
			NodeList nl = elt.getChildNodes();
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				if ( nl.item(i) instanceof Element )
				{
					Element child = (Element) nl.item(i);
					JMenuItem childMenu = getMenu((Element)child);
					if ( childMenu != null )
						result.add(childMenu);
				}
			}
			return result;
		}
		if ( "item".equals(elt.getTagName() ) )
		{
			//obtain a leaf menu item
			JMenuItem result;
			String label;
			if ( useDescriptions )
				label = ( elt.getAttribute("description") );
			else
				label = ( elt.getAttribute("name") );
			result = new JMenuItem(label);
			Action a = new WorldFromTemplateActionBuilder(pf).getWorldFromTemplateAction(elt.getAttribute("template-ref"));
			a.putValue(Action.NAME, label);
			result.setAction(a);
			return result;
		}
		return null;
	}
	
}
