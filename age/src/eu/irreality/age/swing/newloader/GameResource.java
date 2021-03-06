package eu.irreality.age.swing.newloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.newloader.download.DownloadUtil;
import eu.irreality.age.swing.newloader.download.ProgressKeepingDelegate;
import eu.irreality.age.swing.newloader.download.ProgressKeepingReadableByteChannel;
import eu.irreality.age.swing.newloader.download.Unzipper;

public class GameResource 
{

	/**
	 * The local path of the game resource, relative to the AGE worlds directory. Either this or localAbsolutePath must be non-null.
	 */
	private String localRelativePath;
	
	/**
	 * The absolute path of the game resource. Either this or localRelativePath must be non-null.
	 */
	private String localAbsolutePath;
	
	/**
	 * If this is not null, then it's the path to store the zipfile downloaded from the remoteURL
	 * and the local relative path stores the path of the main resource inside that zipfile (which may
	 * be in some subdirectory extracted from the zipfile).
	 */
	private String zipfileRelativePath;
	
	private URL localURL;
	
	private URL remoteURL;
	
	private boolean downloaded;
	
	private boolean downloadInProgress;
	
	/**Path to the local directory containing world files and world resources.*/
	private static String pathToWorlds;
	
	/**
	 * Obtains the absolute path to the local directory containing world files and world resources.
	 * @return
	 */
	private static String getPathToWorlds()
	{
		if ( pathToWorlds == null )
		{
			File cwd = new File ( Paths.getWorkingDirectory() );
			pathToWorlds = cwd.getAbsolutePath() + File.separatorChar + Paths.WORLD_PATH;
		}
		return pathToWorlds;
	}
	
	/**
	 * @return the local absolute path of the game resource.
	 */
	public File getLocalPath()
	{
		if ( localRelativePath != null )
			return new File(getPathToWorlds(),localRelativePath);
		else
			return new File(localAbsolutePath);
	}

	/**
	 * @return the local absolute URL of the game resource.
	 */
	public URL getLocalURL() 
	{
		return localURL;
	}
		
	/**
	 * @return The string indicating the local relative path to the resource.
	 */
	public String getLocalRelativePath()
	{
		return localRelativePath;
	}
	
	/**
	 * @return The string indicating the full relative path to store the zipfile downloaded from the server, that we will
	 * need to unzip in order to get the game resource.
	 */
	public String getZipfileRelativePath()
	{
		if ( zipfileRelativePath != null ) return zipfileRelativePath;
		else if ( localRelativePath == null ) return null; //this shouldn't happen, we shouldn't be asking for the zipfile path of a file in this situation
		else return localRelativePath + ".zip"; //example: vampiro/world.xml.zip
	}
	
	/**
	 * @return the local absolute path to store the zipfile downloaded from the server, that we will need to unzip in order to
	 * get the game resource.
	 */
	public File getZipfilePath()
	{
		return new File(getPathToWorlds(),getZipfileRelativePath());
	}

	/**
	 * @return the remote URL of the game resource.
	 */
	public URL getRemoteURL()
	{
		return remoteURL;
	}

	/**
	 * @return whether the remote resource has been downloaded or not.
	 */
	public boolean isDownloaded() 
	{
		return downloaded;
	}
	
	/**
	 * Whether the download of the remote resource is currently in progress.
	 */
	public boolean isDownloadInProgress()
	{
		return downloadInProgress;
	}
	
	/**
	 * Sets or unsets the flag saying that the resource's download is in progress.
	 */
	public void setDownloadInProgress ( boolean newValue )
	{
		downloadInProgress = newValue;
	}

	/**
	 * @param sets the remote resource as downloaded (or not).
	 */
	public void setDownloaded(boolean downloaded) 
	{
		this.downloaded = downloaded;
	}
	
	/**
	 * Sets the local absolute path to the resource.
	 * This is used to build a game resource from a local disk file.
	 * @param thePath
	 */
	public void setLocalAbsolutePath ( String thePath )
	{
		localAbsolutePath = thePath;
	}
	
	/**
	 * Gets the information about a game resource from an XML node.
	 * @param n
	 * @throws MalformedGameEntryException
	 */
	public void initFromXML ( Node n ) throws MalformedGameEntryException
	{
		try
		{
			Element e = (Element) n;
			if ( !e.hasAttribute("local") && !e.hasAttribute("localAbsolute") ) throw new MalformedGameEntryException("Game resource entry missing local path (attribute local or localAbsolute)");
			else if ( e.hasAttribute("local") )
			{
				URL localWorldsURL = new File(getPathToWorlds()).toURI().toURL();
				localURL = new URL(localWorldsURL,e.getAttribute("local"));
				localRelativePath = e.getAttribute("local");
			}
			else //has attribute localAbsolute
			{
				localAbsolutePath = e.getAttribute("localAbsolute");
				localURL = new File(localAbsolutePath).toURI().toURL();
				if ( e.hasAttribute("zip") || e.hasAttribute("remote") ) throw new MalformedGameEntryException("Malformed game resource: a resource without local relative path (attribute local) cannot have attributes zip or remote");
			}
			if ( e.hasAttribute("zip") ) zipfileRelativePath = e.getAttribute("zip");
			if ( e.hasAttribute("remote") ) remoteURL = new URL(e.getAttribute("remote"));
			if ( e.hasAttribute("downloaded") ) downloaded = Boolean.valueOf(e.getAttribute("downloaded")).booleanValue();
		}
		catch ( MalformedURLException mue )
		{
			throw new MalformedGameEntryException(mue);
		}
	}
	
	/**
	 * Obtain an XML representation for this resource entry.
	 * @param doc The document in which to create the XML element associated with this game resource.
	 * @param isMainResource Whether the resource is the main resource of a game or not. Will be used to set the XML element name.
	 * @return
	 */
	public Node getXML ( Document doc , boolean isMainResource )
	{
		Element result;
		if ( isMainResource )
			result = doc.createElement("main-resource");
		else
			result = doc.createElement("resource");
		if ( localRelativePath != null ) result.setAttribute("local",localRelativePath);
		if ( localAbsolutePath != null ) result.setAttribute("localAbsolute",localAbsolutePath);
		if ( remoteURL != null ) result.setAttribute("remote",remoteURL.toString());
		if ( zipfileRelativePath != null ) result.setAttribute("zip", zipfileRelativePath);
		result.setAttribute("downloaded",String.valueOf(downloaded));
		return result;
	}
	
	/**
	 * Obtains only the filename (not complete path) from an URL.
	 * @param u
	 */
	private String getFileNameFromURL ( URL u )
	{
		String path = u.getFile();
		int index = path.lastIndexOf('/');
		if ( index < 0 ) return "";
		else return path.substring(index+1);
	}
	
	private void downloadFileFromURL ( URL fromURL , File toFile , ProgressKeepingDelegate toNotify ) throws IOException
	{
		//TODO Could also try the default API class ProgressMonitorInputStream
		toNotify.progressUpdate(0.001 , UIMessages.getInstance().getMessage("gameloader.pre.download") + ": " + getFileNameFromURL(fromURL) );
		URLConnection connection = fromURL.openConnection();
		connection.setReadTimeout(5000);
		InputStream inStream = connection.getInputStream();
		ReadableByteChannel rbc = Channels.newChannel(inStream);
		toNotify.progressUpdate(0.002 , UIMessages.getInstance().getMessage("gameloader.pre.download.connection") + ": " + getFileNameFromURL(fromURL) );
		int contentLength = DownloadUtil.contentLength(fromURL);
		toNotify.progressUpdate(0.003 , UIMessages.getInstance().getMessage("gameloader.pre.download.length") + ": " + getFileNameFromURL(fromURL) );
		ProgressKeepingReadableByteChannel prbc = new ProgressKeepingReadableByteChannel(rbc,contentLength,toNotify, UIMessages.getInstance().getMessage("gameloader.game.downloading") + ": " + getFileNameFromURL(fromURL) );
		FileOutputStream fos = new FileOutputStream(toFile);
		fos.getChannel().transferFrom(prbc, 0, Long.MAX_VALUE);
		inStream.close();
		fos.close();
	}
	
	/**
	 * Checks if the local file referenced in the resource exists (regardless of the contents of the "downloaded" flag)
	 * @return
	 */
	public boolean checkLocalFileExists ()
	{
		return getLocalPath().exists();
	}
	
	
	/**
	 * Downloads this resource from the remote URL (if it hasn't already been downloaded).
	 * This method shouldn't be called when the resource has already been downloaded, as it will check the filesystem for the
	 * resource, causing inefficiency. 
	 * @throws IOException
	 */
	public void download ( ProgressKeepingDelegate toNotify ) throws IOException
	{
		if ( downloaded && checkLocalFileExists() ) return; //no need to download, file is already there.
		else
		{
			try
			{
				boolean isZipped = remoteURL.toString().endsWith(".zip"); //if the download is zipped, we'll need to download the zip file and then decompress it
				setDownloadInProgress(true);
				File outputPath = getLocalPath();
				if ( isZipped ) outputPath = getZipfilePath();
				if ( !outputPath.getParentFile().exists() ) outputPath.getParentFile().mkdirs(); //create directory if it doesn't exist
				downloadFileFromURL ( remoteURL , outputPath , toNotify );
				if ( isZipped )
				{
					toNotify.progressUpdate(1.0, "Unzipping " + outputPath.getName());
					Unzipper.unzip(outputPath.getAbsolutePath(), outputPath.getParentFile().getAbsolutePath()); //unzip
					outputPath.delete(); //delete the zipfile since we have extracted the contents
				}
				setDownloaded(true);
				setDownloadInProgress(false);
			}
			catch ( IOException e )
			{
				setDownloadInProgress(false);
				throw e;
			}
		}
	}
	
	
}
