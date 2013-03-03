package eu.irreality.age.swing.newloader;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import javax.xml.transform.TransformerException;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.newloader.download.DownloadProgressKeeper;
import eu.irreality.age.swing.newloader.download.ProgressKeepingDelegate;
import eu.irreality.age.swing.newloader.download.ProgressKeepingReadableByteChannel;


/**
 * A panel with a list of games that can be downloaded and/or played.
 * @author carlos
 *
 */
public class NewLoaderGamePanel extends JPanel implements ProgressKeepingDelegate
{
	
	private JTable gameTable;
	private JTextPane infoPane = new JTextPane();
	private GameTableModel gameTableModel;
	private XTableColumnModel gameTableColumnModel;
	private JScrollPane tableScrollPane;
	private JPanel downloadOrPlayButtonPanel = new JPanel();
	private JPanel progressBarPanel = new JPanel();
	private JButton currentDownloadOrPlayButton; //this can be a play button or a download button
	
	private JButton playButton = new JButton("Play");
	private JButton downloadButton = new JButton("Download");
	private JButton downloadingButton = new JButton("Downloading...");
	
	/**This maps game entries to the objects keeping their download progress and holding their progress bars*/
	private Map downloadProgressKeepers = Collections.synchronizedMap(new HashMap());
	
	
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
	
	private GameEntry getSelectedGameEntry()
	{
		int index = gameTable.getSelectedRow();
		return (GameEntry) gameTableModel.getGameEntry(index);
	}
	

	
	private void showError(String message, String title)
	{
		JOptionPane.showMessageDialog(this,"<html><p>"+message+"</p>",title,JOptionPane.ERROR_MESSAGE);
	}
	
	public NewLoaderGamePanel()
	{
		
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		
		gameTableModel = new GameTableModel();
		try 
		{
			gameTableModel.addGameCatalog(this.getClass().getClassLoader().getResource("catalog.xml"));
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
		infoPane.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,18));
		Font tableFont = new Font(Font.SANS_SERIF,Font.PLAIN,18);
		gameTable.setFont(tableFont);
		FontMetrics fm = gameTable.getFontMetrics(tableFont);
		gameTable.setRowHeight(fm.getHeight());
		
		
		add(tableScrollPane);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.PAGE_AXIS));
		
		rightPanel.add(infoPane);
		
		JPanel rightDownPanel = new JPanel();
		rightDownPanel.setLayout(new BoxLayout(rightDownPanel,BoxLayout.LINE_AXIS));
		
		rightPanel.add(rightDownPanel);
		
		rightDownPanel.add(progressBarPanel);
		rightDownPanel.add(Box.createHorizontalGlue());
		rightDownPanel.add(downloadOrPlayButtonPanel);
		
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
		

		
		//System.err.println(gameTable.getRowCount());
		//System.err.println(gameTable.getValueAt(0, 0));
		
	}
	
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
							gameTableModel.fireTableDataChanged(); //game has changed to downloaded //TODO: This unselects the game. Store selection and then re-select after this!
							gameTable.setRowSelectionInterval(selIndex, selIndex);
							progressKeeper.progressUpdate(1.0, "Game is available");
						}
					} );	
					//gameTableModel.fireTableDataChanged(); //game has changed to downloaded
				} 
				catch (final IOException e1) 
				{
					SwingUtilities.invokeLater( new Runnable() {
						public void run()
						{
							progressKeeper.progressUpdate(0.0,"Problem occurred");
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
	
	private void showGameEntry ( GameEntry ge )
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( UIMessages.getInstance().getMessage("gameinfo.name") + " " + ge.getTitle() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.author") + " " + ge.getAuthor() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.date") + " " + ge.getDate() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.version") + " " + ge.getVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.required") + " " + ge.getAgeVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.downloaded") + " " + yesNo(ge.isDownloaded()) + "\n" );
		
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
			activateDownloadButton();
		
	}

	//temporary
	public void progressUpdate(double progress , String progressString) 
	{
		System.err.println("Progress " + progress);
		infoPane.setText(infoPane.getText()+ progressString + ": " + progress + "\n");
	}
	
}
