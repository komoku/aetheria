package eu.irreality.age.swing.newloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.util.xml.XMLfromURL;

/**
 * A model for a JTable containing information about AGE worlds.
 * @author carlos
 *
 */
public class GameTableModel extends AbstractTableModel
{
	
	private Vector columnNames;
	private List catalogUrls; //urls to the XML catalog files
	private File catalogWritePath; //file to write the global catalog to
	
	private List gameEntries = new ArrayList(); //game entries (the content of the model)
	
	private void initColumnNames()
	{
		columnNames = new Vector();
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.name") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.author") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.date") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.version") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.required") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.language") );
		columnNames.add( UIMessages.getInstance().getMessage("gameinfo.downloaded") );
		columnNames.add( "Local Path" );
		columnNames.add( "Remote URL" );
	}
	
	public GameTableModel()
	{
		
		initColumnNames();
		
		catalogUrls = new ArrayList();
		catalogUrls.add ( this.getClass().getClassLoader().getResource("catalog.xml") );
		
		
	}
	
	/**
	 * Adds a game entry to the table model.
	 * If the overwrite parameter is false, it adds the game only if it does not already exist in the model.
	 * If it is true, then if the game exists, it deletes the old entry and adds the given game entry.
	 */
	public void addGameEntry( GameEntry ge , boolean overwrite )
	{
		if ( overwrite )
		{
			if ( gameEntries.contains(ge) )
				gameEntries.remove(ge);
			gameEntries.add(ge);
			Collections.sort(gameEntries); //TODO this may not scale
			fireTableDataChanged();
		}
		else
		{
			if ( !gameEntries.contains(ge) ) //equality is by local path and remote url
			{
				gameEntries.add(ge);
				Collections.sort(gameEntries); //TODO this may not scale
				fireTableDataChanged();
			}
		}
	}
	
	public GameEntry getGameEntry ( int index )
	{
		return (GameEntry) gameEntries.get(index);
	}
	
	

	
	/**
	 * Adds the games contained in the given catalog to the table model, and updates the catalog URLs,
	 * adding the given URL as a catalog URL to the model.
	 * @param doc
	 * @param catalogURL
	 * @param overwrite
	 * @throws MalformedGameEntryException
	 */
	public void addGameCatalog ( Document doc , URL catalogURL , boolean overwrite ) throws MalformedGameEntryException
	{
		addGamesFromCatalog((Element)doc.getFirstChild(),overwrite);		
		
		if ( !catalogUrls.contains(catalogURL) )
		{
			if ( overwrite )
				catalogUrls.add(0,catalogURL); //add at beginning so that it takes larger priority when refresh is called
			else
				catalogUrls.add(catalogURL); //add at end
		}
	}
	
	/**
	 * Opens a XML catalog from an URL, adds all the games contained in a catalog to the table model, and updates the catalog URLs
	 * to add the given URL.
	 * This method is blocking and should be called from the event dispatch thread. Note that this means it is not suitable for remote
	 * catalogs where getting the XML information from the URL could take significant time.
	 * @param catalogURL URL where the game catalog in XML can be found.
	 * @param overwrite If true, the entries from the given catalog overwrite those of the old catalog if they have the same local path and remote URL.
	 * @throws MalformedGameEntryException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerConfigurationException 
	 */
	public void loadGameCatalog ( URL catalogURL , boolean overwrite ) throws IOException, TransformerException, MalformedGameEntryException
	{
		Document doc = XMLfromURL.getXMLFromURL(catalogURL);
		addGameCatalog(doc,catalogURL,overwrite);
	}
	
		
	
	/**
	 * Adds all the games described in the game XML elements that are children of the given catalog XML elements.
	 * If overwrite is true, the existing entries with the same local path and remote URL are overwritten by the new entries. If it's false, they aren't.
	 * @param e
	 * @throws MalformedGameEntryException
	 */
	private void addGamesFromCatalog ( Element e , boolean overwrite ) throws MalformedGameEntryException
	{
		NodeList gameList = e.getElementsByTagName("game");
		for ( int i = 0 ; i < gameList.getLength() ; i++ )
		{
			GameEntry ge = new GameEntry();
			ge.initFromXML(gameList.item(i));
			addGameEntry ( ge , overwrite );
		}
	}
	
	/**
	 * Sets the path to which the combined catalog will be written at the end of execution. 
	 * @param path
	 */
	public void setCatalogWritePath ( File path )
	{
		catalogWritePath = path;
	}
	
	/**
	 * Writes the combined, updated catalog to the set path.
	 * @throws ParserConfigurationException 
	 */
	public void writeCatalog() throws TransformerException, ParserConfigurationException, IOException
	{
		if ( catalogWritePath == null ) return;

		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		org.w3c.dom.Element catalogElement = d.createElement("catalog");
		for ( int i = 0 ; i < gameEntries.size() ; i++ )
		{
			catalogElement.appendChild( ((GameEntry)gameEntries.get(i)).getXML(d) );
		}
		
		d.appendChild(catalogElement);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT,"yes");
		Source s = new DOMSource(d);
		Result r = new StreamResult(catalogWritePath);
		t.transform(s,r);		
	}
	
	/**
	 * Empty the table and re-load all the catalogs.
	 * @throws MalformedGameEntryException 
	 * @throws TransformerException 
	 * @throws IOException 
	 */
	/**
	 * TODO: At the moment this operation is unused. If we actually want to use it, maybe some refactoring would be good.
	 * Loading stuff from URLs (especially if they're remote URLs) is not very model-ish. Maybe this should be on the
	 * game panel class, should get the URLs from the model, and load the catalogs from them (calling addCatalog, loadCatalog,
	 * etc. on the model when necessary)
	 */
	public void refreshCatalogs ( ) throws IOException, TransformerException, MalformedGameEntryException
	{
		gameEntries.clear();
		for ( int i = 0 ; i < catalogUrls.size() ; i++ )
		{
			loadGameCatalog ( (URL)catalogUrls.get(i) , false );
		}
		fireTableDataChanged();
	}
	

	public int getRowCount() 
	{
		return gameEntries.size();
	}

	public int getColumnCount() 
	{
		return 9;
	}

	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		Object value;
		if ( rowIndex < 0 || rowIndex >= gameEntries.size() )
			throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex);
		GameEntry gameEntry = (GameEntry) gameEntries.get(rowIndex);
		switch (columnIndex) 
		{
			case 0:
				value = gameEntry.getTitle();
				break;
			case 1:
				value = gameEntry.getAuthor();
				break;
			case 2:
				value = gameEntry.getDate();
				break;
			case 3:
				value = gameEntry.getVersion();
				break;
			case 4:
				value = gameEntry.getAgeVersion();
				break;
			case 5:
				value = gameEntry.getLanguage();
				break;
			case 6:
				value = Boolean.valueOf(gameEntry.isDownloaded());
				break;
			case 7:
				value = gameEntry.getMainResource().getLocalPath();
				break;
			case 8:
				value = gameEntry.getMainResource().getRemoteURL();
				break;
			default:
				throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
		}
		return value;
	}
	
	public Class getColumnClass ( int columnIndex )
	{
		Class cl;
		switch ( columnIndex )
		{
			case 0: case 1: case 2: case 3: case 4: case 5: case 7: cl = String.class; break;
			case 6: cl = Boolean.class; break;
			case 8: cl = URL.class; break;
 			default: throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
		}
		return cl;	
	}
	
	public String getColumnName ( int columnIndex )
	{
		return (String) columnNames.get(columnIndex);
	}
	
}
