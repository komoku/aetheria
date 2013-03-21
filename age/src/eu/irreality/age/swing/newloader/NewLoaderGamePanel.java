package eu.irreality.age.swing.newloader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.newloader.download.DownloadProgressKeeper;
import eu.irreality.age.swing.newloader.download.ProgressKeepingDelegate;
import eu.irreality.age.swing.newloader.download.ProgressKeepingReadableByteChannel;
import eu.irreality.age.swing.sdi.SwingSDIInterface;
import eu.irreality.age.util.xml.XMLfromURL;


/**
 * A panel with a list of games that can be downloaded and/or played.
 * @author carlos
 *
 */
public class NewLoaderGamePanel extends JPanel implements ProgressKeepingDelegate
{
	
	private final Frame parentFrame;
	private JTable gameTable;
	private JTextPane infoPane = new JTextPane();
	private GameTableModel gameTableModel;
	private XTableColumnModel gameTableColumnModel;
	private JScrollPane tableScrollPane;
	private JPanel downloadOrPlayButtonPanel = new JPanel();
	private JPanel progressBarPanel = new JPanel();
	private JButton currentDownloadOrPlayButton; //this can be a play button or a download button
	
	private JButton playButton = new JButton( UIMessages.getInstance().getMessage("gameloader.play") );
	private JButton downloadButton = new JButton( UIMessages.getInstance().getMessage("gameloader.download") );
	private JButton downloadingButton = new JButton( UIMessages.getInstance().getMessage("gameloader.downloading") );
	
	private JButton syncButton = new JButton( UIMessages.getInstance().getMessage("gameloader.sync") );
	
	/**This maps game entries to the objects keeping their download progress and holding their progress bars*/
	private Map downloadProgressKeepers = Collections.synchronizedMap(new HashMap());
	
	
	public void writeData()
	{
		try 
		{
			gameTableModel.writeCatalog();
		} 
		catch (Exception e)
		{
			this.showError(e.getLocalizedMessage(), UIMessages.getInstance().getMessage("gameloader.error.saving.catalog") );
		}
	}
	
	/**
	 * Obtains the DownloadProgressKeeper for the given game entry, or initializes it if there is none. 
	 * @param ge
	 * @return
	 */
	private DownloadProgressKeeper getProgressKeeper ( GameEntry ge )
	{
		DownloadProgressKeeper result = (DownloadProgressKeeper) downloadProgressKeepers.get(ge);
		if ( result != null ) return result;
		result = new DownloadProgressKeeper(ge);
		downloadProgressKeepers.put(ge, result);
		return result;
	}
	
	/**
	 * Associates a new DownloadProgressKeeper with the given game entry, based on its current download progress data, and 
	 * discarding the already stored progress keeper if present.
	 * This is used if we thought a game was downloaded, but it was actually not present in the hard disk, so we have to reset
	 * its progress.
	 * @param ge
	 * @return
	 */
	private void resetProgressKeeper ( GameEntry ge )
	{
		DownloadProgressKeeper newKeeper = new DownloadProgressKeeper(ge);
		downloadProgressKeepers.put( ge, newKeeper );
	}
	
	private GameEntry getSelectedGameEntry()
	{
		int index = gameTable.getSelectedRow();
		return (GameEntry) gameTableModel.getGameEntry(index);
	}
	

	
	private void showError(String message, String title)
	{
		JOptionPane.showMessageDialog(this,"<html><p>"+message+"</p>",title,JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows an error message as soon as possible in the event dispatching thread (i.e. via invokeLater()).
	 * Can be called from any thread.
	 * @param message
	 * @param title
	 */
	private void showErrorWhenPossible(final String message, final String title)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JOptionPane.showMessageDialog(NewLoaderGamePanel.this,"<html><p>"+message+"</p>",title,JOptionPane.ERROR_MESSAGE);
			}
		});
		
	}
	
	/**
	 * Tries to add all the games contained in a catalog to the table model, but it this fails for some reason, this method does not throw exceptions but return false instead.
	 * If the overwrite parameter is true, then the added entries overwrite existing entries with the same local path / remote URL.
	 * @param catalogURL
	 */
	public boolean loadGameCatalogIfPossible ( URL catalogURL , boolean overwrite )
	{
		try
		{
			gameTableModel.loadGameCatalog ( catalogURL , overwrite );
			return true;
		}
		catch ( Exception e )
		{
			return false;
		}
	}
	

	
	/**
	 * Loads the games contained in a catalog in a URL when possible. Shows dialogs showing the number of games updated,
	 * or the errors found, if any.
	 * This method is not blocking. It will open a new thread to download the catalog, and add the games to the table
	 * in the event dispatch thread when it is ready. The dialogs will also be enqueued on the event dispatch thread.
	 * While it is possible to call this method for a local URL as well, it wouldn't make much sense to go through all
	 * the threading complexity for a local catalog - just call loadCatalog on the GameTableModel for that.
	 * @param catalogURL
	 * @param overwrite
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void syncWithRemoteCatalog ( final URL catalogURL , final boolean overwrite ) throws IOException, TransformerException
	{
		//If we ask for java 1.6, this could be done better with SwingWorker. doInBackground(), throw exception, and in done catch in get() ExecutedException, InterruptedException
	
		Thread thr = new Thread()
		{
			public void run()
			{
				
				final Document doc;
				try 
				{
					doc = XMLfromURL.getXMLFromURL(catalogURL);
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
					showErrorWhenPossible(e1.getLocalizedMessage(),"Whoops!");
					return;
				} 
				catch (TransformerException e1) 
				{
					e1.printStackTrace();
					showErrorWhenPossible(e1.getLocalizedMessage(),"Whoops!");
					return;
				}
		
				SwingUtilities.invokeLater( new Runnable() 
				{
					public void run()
					{
						try 
						{
							int nGamesUpdated = gameTableModel.addGameCatalog(doc,catalogURL,overwrite);
							if ( nGamesUpdated == 0 )
								JOptionPane.showMessageDialog(NewLoaderGamePanel.this,"<html><p>"+UIMessages.getInstance().getMessage("gameloader.no.games.updated")+"</p>",UIMessages.getInstance().getMessage("gameloader.sync.result"),JOptionPane.INFORMATION_MESSAGE);
							else
								JOptionPane.showMessageDialog(NewLoaderGamePanel.this,"<html><p>"+nGamesUpdated + " " + UIMessages.getInstance().getMessage("gameloader.games.updated")+"</p>",UIMessages.getInstance().getMessage("gameloader.sync.result"),JOptionPane.INFORMATION_MESSAGE);
						} 
						catch (MalformedGameEntryException e) 
						{
							e.printStackTrace();
							showError(e.getLocalizedMessage(),"Whoops!");
							return;
						}		
					}
				}
				);
		
			}
		};
		
		thr.start();
		
	}
	
	public NewLoaderGamePanel( final Frame parentFrame )
	{
		
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		
		this.parentFrame = parentFrame;
		
		gameTableModel = new GameTableModel();
		try 
		{
			loadGameCatalogIfPossible(new File("maincatalog.xml").toURI().toURL(),false); //this will exist only if the application has been ran in the past
			gameTableModel.loadGameCatalog(this.getClass().getClassLoader().getResource("catalog.xml"),false); //this will exist always, distributed with AGE
			gameTableModel.setCatalogWritePath(new File("maincatalog.xml"));
		} 
		catch (Exception e)
		{
			showError(e.getLocalizedMessage(),"Whoops!");
			e.printStackTrace();
		}
		gameTableColumnModel = new XTableColumnModel();
		
		gameTable = new JTable ( gameTableModel , gameTableColumnModel );
		gameTable.createDefaultColumnsFromModel();
		gameTableColumnModel.setAllColumnsVisible();
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(2),false);
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(3),false);
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(4),false);
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(5),false);
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(7),false);
		gameTableColumnModel.setColumnVisible(gameTableColumnModel.getColumnByModelIndex(8),false);
		
		tableScrollPane = new JScrollPane(gameTable);
		gameTable.setFillsViewportHeight(true);
		gameTable.setShowHorizontalLines(false);
		
		ListSelectionModel selModel = gameTable.getSelectionModel();
		selModel.addListSelectionListener ( new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e) 
			{
				//Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) 
                {
                    //no rows are selected - do nothing.
                } 
                else 
                {
                    int selectedRow = lsm.getMinSelectionIndex();
                    showGameEntry ( (GameEntry) gameTableModel.getGameEntry(selectedRow) );
                }	
			}	
		}
				);
		gameTable.setRowSelectionInterval(0,0);
		
		
		downloadingButton.setEnabled(false);
		infoPane.setEditable(false);
		infoPane.setPreferredSize(new Dimension(300,400));
		infoPane.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,18));
		infoPane.setMargin(new Insets(10,10,10,10));
		Font tableFont = new Font(Font.SANS_SERIF,Font.PLAIN,18);
		gameTable.setFont(tableFont);
		FontMetrics fm = gameTable.getFontMetrics(tableFont);
		gameTable.setRowHeight(fm.getHeight());
		gameTable.getColumn(UIMessages.getInstance().getMessage("gameinfo.downloaded")).setPreferredWidth(80);
		gameTable.getColumn(UIMessages.getInstance().getMessage("gameinfo.downloaded")).setMaxWidth(120);
				
		tableScrollPane.setPreferredSize(new Dimension(800,400));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.PAGE_AXIS));
		
		leftPanel.add(tableScrollPane);
		
		JPanel leftDownPanel = new JPanel();
		leftDownPanel.setLayout(new BoxLayout(leftDownPanel,BoxLayout.LINE_AXIS));
		
		leftPanel.add(leftDownPanel);
		
		leftDownPanel.add(syncButton);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.PAGE_AXIS));
		
		rightPanel.add(infoPane);
		
		JPanel rightDownPanel = new JPanel();
		rightDownPanel.setLayout(new BoxLayout(rightDownPanel,BoxLayout.LINE_AXIS));
		
		rightPanel.add(rightDownPanel);
		
		progressBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		rightDownPanel.add(progressBarPanel);
		rightDownPanel.add(Box.createHorizontalGlue());
		downloadOrPlayButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		rightDownPanel.add(downloadOrPlayButtonPanel);
		
		add(leftPanel);
		add(rightPanel);
		
		downloadButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				launchDownload();
				/*
				GameEntry toDownload = getSelectedGameEntry();
				try 
				{
					activateDownloadingButton();
					toDownload.download(NewLoaderGamePanel.this);
					activatePlayButton();
					//gameTableModel.fireTableDataChanged(); //game has changed to downloaded
				} 
				catch (IOException e1) 
				{
					showError(e1.getLocalizedMessage(),"Whoops!");
					e1.printStackTrace();
					activateDownloadButton();
				}
				*/
			}
		});
		
		playButton.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent e )
			{
				launchGame();
			}
		}
		);
		
		syncButton.addActionListener ( new ActionListener() 
		{
			public void actionPerformed ( ActionEvent e )
			{
				new SyncWithServerDialog(parentFrame,true);
			}
		}
		);
		
		//double click: play or download!
		gameTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2) 
				{
					GameEntry game = getSelectedGameEntry();
					if ( game.isDownloaded() ) launchGame();
					else if ( game.isDownloadable() ) launchDownload();
					else JOptionPane.showMessageDialog(NewLoaderGamePanel.this, UIMessages.getInstance().getMessage("gameloader.game.missing.undownloadable"), "Oops!", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		

		
		//System.err.println(gameTable.getRowCount());
		//System.err.println(gameTable.getValueAt(0, 0));
		
	}
	
	
	/**
	 * Launches the game that is currently selected in the table.
	 * Must be invoked from the Swing event dispatching thread.
	 */
	private void launchGame()
	{
		final GameEntry toPlay = getSelectedGameEntry();
		if ( !toPlay.getMainResource().checkLocalFileExists() ) //game is marked as downloaded, but the game file doesn't exist (removed, moved, not actually downloaded, etc.)
		{
			toPlay.setDownloaded(false);
			resetProgressKeeper(toPlay);
			showGameEntry(toPlay);
			if ( toPlay.isDownloadable() ) //we ask the user if she wants to download the game
			{
				int opt = JOptionPane.showConfirmDialog(this, UIMessages.getInstance().getMessage("gameloader.game.missing.downloadable"), "Oops!", JOptionPane.YES_NO_OPTION);
				if ( opt == JOptionPane.YES_OPTION )
				{
					showGameEntry(toPlay); //refresh display (downloading button, etc.)
					refreshTable();
					launchDownload();
					return;
				}
			}
			else //if the game is not downloadable (we don't have its remote URL) we show a dialog telling the user that life is hard
			{
				JOptionPane.showMessageDialog(NewLoaderGamePanel.this, UIMessages.getInstance().getMessage("gameloader.game.missing.undownloadable"), "Oops!", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		Thread thr = new Thread()
		{
			public void run()
			{
				SwingUtilities.invokeLater( new Runnable() {
					public void run()
					{
						new SwingSDIInterface(toPlay.getMainResource().getLocalPath().getAbsolutePath(),false,null,null);
					}
				} );
			}
		};
		thr.start();
	}
	
	private void refreshTable()
	{
		int selIndex = gameTable.getSelectedRow();
		gameTableModel.fireTableDataChanged();
		gameTable.setRowSelectionInterval(selIndex, selIndex);
	}
	
	/**
	 * Launches the download of the game currently selected in the table.
	 */
	private void launchDownload()
	{
		final GameEntry toDownload = getSelectedGameEntry();
		final DownloadProgressKeeper progressKeeper = getProgressKeeper(toDownload); 
		progressBarPanel.removeAll();
		progressBarPanel.add(progressKeeper.getBar());
		activateDownloadingButton();
		Thread thr = new Thread()
		{
			public void run()
			{
				try 
				{
					toDownload.download(progressKeeper);
					SwingUtilities.invokeLater( new Runnable() {
						public void run()
						{
							activatePlayButton();
							int selIndex = gameTable.getSelectedRow();
							refreshTable(); //this fires a data changed event
							gameTable.setRowSelectionInterval(selIndex, selIndex);
							progressKeeper.progressUpdate(1.0, UIMessages.getInstance().getMessage("gameloader.game.available") );
						}
					} );	
					//gameTableModel.fireTableDataChanged(); //game has changed to downloaded
				} 
				catch (final IOException e1) 
				{
					SwingUtilities.invokeLater( new Runnable() {
						public void run()
						{
							progressKeeper.progressUpdate(0.0, UIMessages.getInstance().getMessage("gameloader.download.problem") );
							showError(e1.getLocalizedMessage(),"Whoops!");
							e1.printStackTrace();
							activateDownloadButton();
						}
					} );
				}
			}
		};
		thr.start();
	}
	
	private void activatePlayButton()
	{
		if ( currentDownloadOrPlayButton != null && currentDownloadOrPlayButton != playButton )
		{
			downloadOrPlayButtonPanel.remove(currentDownloadOrPlayButton);
			currentDownloadOrPlayButton = null;
		}
		if ( currentDownloadOrPlayButton == null )
		{
			downloadOrPlayButtonPanel.add(playButton);
			currentDownloadOrPlayButton = playButton;
		}
		downloadOrPlayButtonPanel.revalidate();
	}
	
	private void activateDownloadButton()
	{
		if ( currentDownloadOrPlayButton != null && currentDownloadOrPlayButton != downloadButton )
		{
			downloadOrPlayButtonPanel.remove(currentDownloadOrPlayButton);
			currentDownloadOrPlayButton = null;
		}
		if ( currentDownloadOrPlayButton == null )
		{
			downloadOrPlayButtonPanel.add(downloadButton);
			currentDownloadOrPlayButton = downloadButton;
		}
		downloadOrPlayButtonPanel.revalidate();
	}
	
	private void activateDownloadingButton()
	{
		if ( currentDownloadOrPlayButton != null && currentDownloadOrPlayButton != downloadingButton )
		{
			downloadOrPlayButtonPanel.remove(currentDownloadOrPlayButton);
			currentDownloadOrPlayButton = null;
		}
		if ( currentDownloadOrPlayButton == null )
		{
			downloadOrPlayButtonPanel.add(downloadingButton);
			currentDownloadOrPlayButton = downloadingButton;
		}
		downloadOrPlayButtonPanel.revalidate();
		
	}
	
	//TODO: Color table entries green/red depending on downloaded or not?
	//TODO: Handle zipped games
	
	
	private String yesNo ( boolean b )
	{
		if ( b ) return UIMessages.getInstance().getMessage("boolean.yes");
		else return UIMessages.getInstance().getMessage("boolean.no"); 
	}
	
	/**
	 * Shows the information associated with a game entry (title, author, etc.) as well as its progress bar and the relevant button (play, download, etc.)
	 * @param ge
	 */
	private void showGameEntry ( GameEntry ge )
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( UIMessages.getInstance().getMessage("gameinfo.name") + " " + ge.getTitle() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.author") + " " + ge.getAuthor() + "\n" );
		if ( ge.getLanguage() != null )
			sb.append( UIMessages.getInstance().getMessage("gameinfo.language") + " " + new Locale(ge.getLanguage()).getDisplayLanguage() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.date") + " " + ge.getDate() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.version") + " " + ge.getVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.required") + " " + ge.getAgeVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.downloaded") + " " + yesNo(ge.isDownloaded()) + "\n" );
		
		if ( !ge.isDownloaded() )
		{
			sb.append( UIMessages.getInstance().getMessage("gameinfo.downloadable") + " " + yesNo(ge.isDownloadable()) + "\n" );
		}
		
		
		infoPane.setText(sb.toString());
		infoPane.revalidate();
		
		//show the progress bar if applicable
		progressBarPanel.removeAll();
		progressBarPanel.add(getProgressKeeper(ge).getBar());
		progressBarPanel.revalidate();
		
		//and show the play button or the download button as needed.
		if ( ge.isDownloaded() )
			activatePlayButton();
		else if ( ge.isDownloadInProgress() )
			activateDownloadingButton();
		else
		{
			activateDownloadButton();
			downloadButton.setEnabled(ge.isDownloadable());
		}
		
	}

	//temporary
	public void progressUpdate(double progress , String progressString) 
	{
		System.err.println("Progress " + progress);
		infoPane.setText(infoPane.getText()+ progressString + ": " + progress + "\n");
	}
	
	public void addGameEntry ( GameEntry ge , boolean overwrite )
	{
		gameTableModel.addGameEntry(ge,false);
	}
	
}
