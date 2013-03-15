package eu.irreality.age.swing.newloader;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import eu.irreality.age.SwingAetheriaGameLoaderInterface;
import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.sdi.SwingSDIInterface;

public class NewLoader extends JFrame 
{
	
	private NewLoaderGamePanel gamePanel;
	
	private JButton loadFromDiskButton;

	public NewLoader()
	{
		super("New Loader");
		setSize(400,400);
		getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		
		//the panel with the game catalog
		getContentPane().add(gamePanel = new NewLoaderGamePanel());
		
		JPanel fromDiskPanel = new JPanel();
		fromDiskPanel.setLayout(new BoxLayout(fromDiskPanel,BoxLayout.LINE_AXIS));
		fromDiskPanel.add ( Box.createHorizontalGlue() );
		fromDiskPanel.add( new JLabel( UIMessages.getInstance().getMessage("gameloader.fromdisk") ) );
		loadFromDiskButton = new JButton( UIMessages.getInstance().getMessage("gameloader.browse") );
		loadFromDiskButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent e )
			{
				SwingSDIInterface.main( new String[0] );
			}
		});
		//TODO: Option to add the game loaded from disk to the catalog: a checkbox + a method to read a world XML and create a catalog entry (even w/o remote url)
		fromDiskPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		fromDiskPanel.add(Box.createRigidArea(new Dimension(5,5)));
		fromDiskPanel.add(loadFromDiskButton);
		getContentPane().add(new JSeparator());
		getContentPane().add(fromDiskPanel);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener ( new WindowAdapter()
		{
			public void windowClosing ( WindowEvent e )
			{
				gamePanel.writeData();
				NewLoader.this.dispose();
			}
		}
		);
		JOptionPane.showMessageDialog(this, UIMessages.getInstance().getMessage("gameloader.beta.message") , UIMessages.getInstance().getMessage("gameloader.beta.title") , JOptionPane.INFORMATION_MESSAGE );
	}
	
	public static void main ( String[] args )
	{
		SwingUtilities.invokeLater ( new Runnable()
		{
			public void run() 
			{
				new NewLoader();	
			}	
		});
	}
	
}
