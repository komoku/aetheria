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

import eu.irreality.age.swing.newloader.download.ProgressKeepingDelegate;
import eu.irreality.age.swing.newloader.download.ProgressKeepingReadableByteChannel;

/**
 * An object of this class represents an entry for a game in the game loader.
 * Each entry contains metadata about the game (title, author, etc.) and information about the resources (files) it requires
 * and whether they have been downloaded or not.
 * @author carlos
 *
 */
public class GameEntry implements Comparable
{

	private String title;
	private String author;
	private String date;
	private String version;
	private String ageVersion;
	private String type;
	private String language;
	//private String worldDir; //only for states (it's the way in which they recover multimedia).
	
	private GameResource mainResource;
	private List extraResources = new ArrayList();
	private boolean downloaded;
	private boolean downloadInProgress;
	
	
	/**
	 * Gets the information about a game entry from an XML node.
	 * @param n
	 * @throws MalformedGameEntryException
	 */
	public void initFromXML ( Node n ) throws MalformedGameEntryException
	{
		Element e = (Element) n;
		if ( !e.hasAttribute("worldName") && !e.hasAttribute("title") && !e.hasAttribute("moduleName") ) throw new MalformedGameEntryException("Game entry missing world name (attribute worldName, title or moduleName)");
		else 
		{
			if ( e.hasAttribute("worldName") ) title = e.getAttribute("worldName");
			if ( e.hasAttribute("moduleName") ) title = e.getAttribute("moduleName");
			if ( e.hasAttribute("title") ) title = e.getAttribute("title");
		}
		if ( e.hasAttribute("author") ) author = e.getAttribute("author");
		if ( e.hasAttribute("date") ) date = e.getAttribute("date");
		if ( e.hasAttribute("version") ) version = e.getAttribute("version");
		if ( e.hasAttribute("ageVersion") ) ageVersion = e.getAttribute("ageVersion");
		if ( e.hasAttribute("parserVersion") ) ageVersion = e.getAttribute("parserVersion");
		if ( e.hasAttribute("type") ) type = e.getAttribute("type");
		if ( e.hasAttribute("language") ) language = e.getAttribute("language");
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
		result.setAttribute("version", version);
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
	 * @return Whether this game is currently being downloaded. 
	 */
	public boolean getDownloadInProgress()
	{
		return downloadInProgress;
	}
	
	/**
	 * Returns whether the download of this game is in progress.
	 */
	public boolean isDownloadInProgress()
	{
		return downloadInProgress;
	}
	
	/**
	 * Set this game as currently being downloaded or not.
	 * If the value is false, it is propagated to all the game's resources.
	 * @param value
	 */
	public void setDownloadInProgress ( boolean value )
	{
		downloadInProgress = value;
		if ( value == false )
		{
			mainResource.setDownloadInProgress(false);
			for ( int i = 0 ; i < extraResources.size() ; i++ )
			{
				((GameResource)extraResources.get(i)).setDownloadInProgress(false);
			}
		}
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
	public void download ( final ProgressKeepingDelegate toNotify ) throws IOException
	{
		if ( downloaded && checkLocalFilesExist() ) return; //no need to download, file is already there.
		else
		{
			try
			{
				setDownloadInProgress(true);
				
				final int numResources = 1 + extraResources.size(); //this is used to keep progress
				mainResource.download(new ProgressKeepingDelegate()
				{
					public void progressUpdate(double progress , String progressString) 
					{
						toNotify.progressUpdate(progress / ((double)numResources) , progressString);
					}	
				}
						);
				for ( int i = 0 ; i < extraResources.size() ; i++ )
				{
					final int alreadyDownloaded = i+1;
					((GameResource)extraResources.get(i)).download(new ProgressKeepingDelegate()
					{
						public void progressUpdate(double progress , String progressString) 
						{
							toNotify.progressUpdate( (alreadyDownloaded+1+progress) / ((double)numResources),progressString);
						}	
					}
							);
				}
				setDownloaded(true);
				setDownloadInProgress(false);
			}
			catch (IOException e)
			{
				setDownloadInProgress(false);
				throw e;
			}
		}
	}

	/**
	 * @return the title
	 */
	public String getTitle() 
	{
		return title;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() 
	{
		return author;
	}

	/**
	 * @return the date
	 */
	public String getDate() 
	{
		return date;
	}

	/**
	 * @return the ageVersion
	 */
	public String getAgeVersion() 
	{
		return ageVersion;
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() 
	{
		return version;
	}


	/**
	 * @return the type
	 */
	public String getType() 
	{
		return type;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() 
	{
		return language;
	}

	/**
	 * @return the mainResource
	 */
	public GameResource getMainResource() 
	{
		return mainResource;
	}

	/**
	 * @return the downloaded
	 */
	public boolean isDownloaded() 
	{
		return downloaded;
	}
	
	/**
	 * Marks this game as downloaded or not.
	 */
	public void setDownloaded ( boolean value )
	{
		downloaded = value;
	}
	
	/**
	 * We will consider two game entries to be the same if both the remote URL and the local relative path associated to their main resource is the same.
	 * @param ge
	 * @return
	 */
	public boolean equals ( Object obj )
	{
		if ( !(obj instanceof GameEntry) ) return false;
		GameEntry ge = (GameEntry) obj;
		if ( mainResource.getLocalRelativePath() == null )
		{
			if ( ge.getMainResource().getLocalRelativePath() != null ) 
				return false;
		}
		else
		{
			if ( !mainResource.getLocalRelativePath().equals(ge.getMainResource().getLocalRelativePath()) )
				return false;
		}
		if ( mainResource.getRemoteURL() == null )
		{
			if ( ge.getMainResource().getRemoteURL() != null ) 
				return false;
		}
		else
		{
			if ( !mainResource.getRemoteURL().equals(ge.getMainResource().getRemoteURL()) )
				return false;
		}
		return true;	
	}
	
	public int hashCode()
	{
		int hash = 1;
		if ( mainResource.getLocalRelativePath() != null ) hash = hash*31 + mainResource.getLocalRelativePath().hashCode();
		if ( mainResource.getRemoteURL() != null ) hash = hash*31 + mainResource.getRemoteURL().hashCode();
		return hash;
	}

	public int compareTo(Object o) 
	{
		return getTitle().compareTo(((GameEntry)o).getTitle()); 
	}
	
	
}
