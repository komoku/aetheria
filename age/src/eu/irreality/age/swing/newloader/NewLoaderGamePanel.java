package eu.irreality.age.swing.newloader;

import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.transform.TransformerException;


/**
 * A panel with a list of games that can be downloaded and/or played.
 * @author carlos
 *
 */
public class NewLoaderGamePanel extends JPanel
{
	
	private JTable gameTable;
	private JTextPane infoPane;
	private GameTableModel gameTableModel;
	private XTableColumnModel gameTableColumnModel;
	private JScrollPane tableScrollPane;
	
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
			JOptionPane.showMessageDialog(this,"<html><p>"+e.getLocalizedMessage()+"</p>","Whoops!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		gameTableColumnModel = new XTableColumnModel();
		
		gameTable = new JTable ( gameTableModel /*, gameTableColumnModel*/ );
		
		tableScrollPane = new JScrollPane(gameTable);
		gameTable.setFillsViewportHeight(true);
		gameTable.setShowHorizontalLines(false);
		
		add(tableScrollPane);
		add(new JLabel("test"));
		
		//System.err.println(gameTable.getRowCount());
		//System.err.println(gameTable.getValueAt(0, 0));
		
	}
	
}
