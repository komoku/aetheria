package eu.irreality.age.swing.newloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An object of this class represents an entry for a game in the game loader.
 * Each entry contains metadata about the game (title, author, etc.) and information about the resources (files) it requires
 * and whether they have been downloaded or not.
 * @author carlos
 *
 */
public class GameEntry 
{

	private String title;
	private String author;
	private String date;
	private String ageVersion;
	private String type;
	private String language;
	//private String worldDir; //only for states (it's the way in which they recover multimedia).
	
	private GameResource mainResource;
	private List extraResources = new ArrayList();
	private boolean downloaded;
	
	
	
	/**
	 * Gets the information about a game entry from an XML node.
	 * @param n
	 * @throws MalformedGameEntryException
	 */
	public void initFromXML ( Node n ) throws MalformedGameEntryException
	{
		Element e = (Element) n;
		if ( !e.hasAttribute("worldName") ) throw new MalformedGameEntryException("Game entry missing world name (attribute worldName)");
		else title = e.getAttribute("worldName");
		if ( e.hasAttribute("author") ) author = e.getAttribute("author");
		if ( e.hasAttribute("date") ) author = e.getAttribute("date");
		if ( e.hasAttribute("ageVersion") ) author = e.getAttribute("ageVersion");
		if ( e.hasAttribute("type") ) author = e.getAttribute("type");
		if ( e.hasAttribute("language") ) author = e.getAttribute("language");
		if ( e.hasAttribute("downloaded") ) downloaded = Boolean.valueOf(e.getAttribute("downloaded")).booleanValue();
			
		//main resource
		NodeList mainResList = e.getElementsByTagName("main-resource");
		if ( mainResList.getLength() < 1 ) throw new MalformedGameEntryException("Game entry for " + title + " missing main resource entry (element main-resource)");
		Element mainResElt = (Element) mainResList.item(0);
		if ( mainResElt != null )
		{
			mainResource = new GameResource();
			mainResource.initFromXML(mainResElt);
		}
			
		//extra resources
		NodeList extraResList = e.getElementsByTagName("resource");
		for ( int i = 0 ; i < extraResList.getLength() ; i++ )
		{
			Element extraResElt = (Element) extraResList.item(i);
			if ( extraResElt != null )
			{
				GameResource newResource = new GameResource();
				newResource.initFromXML(mainResElt);
				extraResources.add(newResource);
			}
		}

	}
	
	/**
	 * Obtain an XML representation for this game entry entry.
	 * @param doc The document in which to create the XML element associated with this game entry.
	 * @return
	 */
	public Node getXML( Document doc )
	{
		Element result;
		result = doc.createElement("game");
		result.setAttribute("title", title);
		result.setAttribute("author", author);
		result.setAttribute("date", date);
		result.setAttribute("ageVersion", ageVersion);
		result.setAttribute("type", type);
		result.setAttribute("language", language);
		result.setAttribute("downloaded", String.valueOf(downloaded));
		
		//main resource
		result.appendChild(mainResource.getXML(doc, true));
		
		//extra resources
		for ( int i = 0 ; i < extraResources.size() ; i++ )
		{
			result.appendChild( ((GameResource)extraResources.get(i)).getXML(doc,false) );
		}
			
		return result;
	}
	
	
	/**
	 * Check whether the local filesystem has files for all the referenced resources (regardless of the value of the
	 * downloaded flag)
	 * @return
	 */
	public boolean checkLocalFilesExist()
	{
		if ( !mainResource.checkLocalFileExists() ) return false;
		for ( int i = 0 ; i < extraResources.size() ; i++ )
			if ( ! ((GameResource)extraResources.get(i)).checkLocalFileExists() ) return false;
		return true;
	}
	
	/**
	 * Downloads this game's resources from their remote URLs (if they haven't already been downloaded).
	 * This method shouldn't be called when the resource has already been downloaded, as it will check the filesystem for the
	 * resources, causing inefficiency. 
	 * @throws IOException
	 */
	public void download () throws IOException
	{
		if ( downloaded && checkLocalFilesExist() ) return; //no need to download, file is already there.
		else
		{
			mainResource.download();
			for ( int i = 0 ; i < extraResources.size() ; i++ )
				((GameResource)extraResources.get(i)).download();
		}
	}
	
	
}
