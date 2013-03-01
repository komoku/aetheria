package eu.irreality.age.swing.newloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.irreality.age.filemanagement.Paths;
import eu.irreality.age.swing.newloader.download.DownloadUtil;
import eu.irreality.age.swing.newloader.download.ProgressKeepingDelegate;
import eu.irreality.age.swing.newloader.download.ProgressKeepingReadableByteChannel;

public class GameResource 
{

	private String localRelativePath;
	private URL localURL;
	private URL remoteURL;
	private boolean downloaded;
	
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
	 * @return the local absolute URL of the game resource.
	 */
	public URL getLocalURL() 
	{
		return localURL;
	}
	
	/**
	 * @return the local path of the game resource relative to the worlds path.
	 */
	public URL getLocalRelativePathURL() 
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
	 * @param sets the remote resource as downloaded (or not).
	 */
	public void setDownloaded(boolean downloaded) 
	{
		this.downloaded = downloaded;
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
			if ( !e.hasAttribute("local") ) throw new MalformedGameEntryException("Game resource entry missing local path (attribute local)");
			else
			{
				URL localWorldsURL = new File(getPathToWorlds()).toURI().toURL();
				localURL = new URL(localWorldsURL,e.getAttribute("local"));
				localRelativePath = e.getAttribute("local");
			}
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
		if ( remoteURL != null ) result.setAttribute("remote",remoteURL.toString());
		result.setAttribute("downloaded",String.valueOf(downloaded));
		return result;
	}
	
	private void downloadFileFromURL ( URL fromURL , File toFile , ProgressKeepingDelegate toNotify ) throws IOException
	{
		//TODO Could also try the default API class ProgressMonitorInputStream
		toNotify.progressUpdate(null,0.05);
		ReadableByteChannel rbc = Channels.newChannel(fromURL.openStream());
		toNotify.progressUpdate(null,0.10);
		int contentLength = DownloadUtil.contentLength(fromURL);
		toNotify.progressUpdate(null,0.15);
		ProgressKeepingReadableByteChannel prbc = new ProgressKeepingReadableByteChannel(rbc,contentLength,toNotify);
		FileOutputStream fos = new FileOutputStream(toFile);
		fos.getChannel().transferFrom(prbc, 0, 1 << 24);
	}
	
	/**
	 * Checks if the local file referenced in the resource exists (regardless of the contents of the "downloaded" flag)
	 * @return
	 */
	protected boolean checkLocalFileExists ()
	{
		return new File(getPathToWorlds(),localRelativePath).exists();
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
			downloadFileFromURL ( remoteURL , new File(getPathToWorlds(),localRelativePath) , toNotify );
			downloaded = true;
		}
	}
	
	
}
