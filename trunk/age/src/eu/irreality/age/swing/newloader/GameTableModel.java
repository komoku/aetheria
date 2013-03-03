package eu.irreality.age.swing.newloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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
	 * Adds a game entry to the table model, if it does not exist yet in the model.
	 */
	public void addGameEntry( GameEntry ge )
	{
		if ( !gameEntries.contains(ge) ) //equality is by local path and remote url
		{
			gameEntries.add(ge);
			fireTableDataChanged();
		}
	}
	
	public GameEntry getGameEntry ( int index )
	{
		return (GameEntry) gameEntries.get(index);
	}
	
	/**
	 * Adds all the games contained in a catalog to the table model.
	 * @param catalogURL URL where the game catalog in XML can be found.
	 * @throws MalformedGameEntryException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerConfigurationException 
	 */
	public void addGameCatalog ( URL catalogURL ) throws IOException, TransformerException, MalformedGameEntryException
	{
		if ( catalogURL == null ) throw new IOException("Null catalog URL passed");
				
		InputStream is = catalogURL.openStream();
		StreamSource s = new StreamSource(is,catalogURL.toString());
		Transformer t = TransformerFactory.newInstance().newTransformer();
		DOMResult r = new DOMResult();
		t.transform(s,r);
		addGameCatalog((Element)((Document)r.getNode()).getFirstChild());		
		
		if ( !catalogUrls.contains(catalogURL) )
			catalogUrls.add(catalogURL);
	}
	
	/**
	 * Tries to add all the games contained in a catalog to the table model, but it this fails for some reason, this method does not throw exceptions but return false instead.
	 * @param catalogURL
	 */
	public boolean addGameCatalogIfPossible ( URL catalogURL )
	{
		try
		{
			addGameCatalog ( catalogURL );
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}
	
	
	/**
	 * Adds all the games described in the game XML elements that are children of the given catalog XML elements.
	 * @param e
	 * @throws MalformedGameEntryException
	 */
	public void addGameCatalog ( Element e ) throws MalformedGameEntryException
	{
		NodeList gameList = e.getElementsByTagName("game");
		for ( int i = 0 ; i < gameList.getLength() ; i++ )
		{
			GameEntry ge = new GameEntry();
			ge.initFromXML(gameList.item(i));
			addGameEntry ( ge );
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
	public void refreshCatalogs ( ) throws IOException, TransformerException, MalformedGameEntryException
	{
		gameEntries.clear();
		for ( int i = 0 ; i < catalogUrls.size() ; i++ )
		{
			addGameCatalog ( (URL)catalogUrls.get(i) );
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
				value = gameEntry.getMainResource().getLocalRelativePath();
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
