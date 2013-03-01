package eu.irreality.age.swing.newloader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.transform.TransformerException;

import eu.irreality.age.i18n.UIMessages;


/**
 * A panel with a list of games that can be downloaded and/or played.
 * @author carlos
 *
 */
public class NewLoaderGamePanel extends JPanel
{
	
	private JTable gameTable;
	private JTextPane infoPane = new JTextPane();
	private GameTableModel gameTableModel;
	private XTableColumnModel gameTableColumnModel;
	private JScrollPane tableScrollPane;
	private JPanel downloadOrPlayButtonPanel = new JPanel();
	private JButton currentDownloadOrPlayButton; //this can be a play button or a download button
	
	private JButton playButton = new JButton("Play");
	private JButton downloadButton = new JButton("Download");
	
	
	private GameEntry getSelectedGameEntry()
	{
		int index = gameTable.getSelectedRow();
		return (GameEntry) gameTableModel.getGameEntry(index);
	}
	
	public void downloadSelected() throws IOException
	{
		GameEntry toDownload = getSelectedGameEntry();
		toDownload.download();
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
		
		
		add(tableScrollPane);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.PAGE_AXIS));
		
		rightPanel.add(infoPane);
		
		JPanel rightDownPanel = new JPanel();
		rightDownPanel.setLayout(new BoxLayout(rightDownPanel,BoxLayout.LINE_AXIS));
		
		rightPanel.add(rightDownPanel);
		
		rightDownPanel.add(Box.createHorizontalGlue());
		rightDownPanel.add(downloadOrPlayButtonPanel);
		
		add(rightPanel);
		
		downloadButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				try 
				{
					downloadSelected();
				} catch (IOException e1) 
				{
					showError(e1.getLocalizedMessage(),"Whoops!");
					e1.printStackTrace();
				}
			}
		}
				);
		
		
		//System.err.println(gameTable.getRowCount());
		//System.err.println(gameTable.getValueAt(0, 0));
		
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
	}
	
	private void showGameEntry ( GameEntry ge )
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( UIMessages.getInstance().getMessage("gameinfo.name") + ":" + ge.getTitle() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.author") + ":" + ge.getAuthor() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.date") + ":" + ge.getDate() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.version") + ":" + ge.getVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.required") + ":" + ge.getAgeVersion() + "\n" );
		sb.append( UIMessages.getInstance().getMessage("gameinfo.downloaded") + ":" + ge.isDownloaded() + "\n" );
		
		infoPane.setText(sb.toString());
		
		//and show the play button or the download button as needed.
		if ( ge.isDownloaded() )
			activatePlayButton();
		else
			activateDownloadButton();
		
	}
	
}
