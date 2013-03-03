package eu.irreality.age.swing.newloader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import eu.irreality.age.SwingAetheriaGameLoaderInterface;

public class NewLoader extends JFrame 
{
	
	private NewLoaderGamePanel gamePanel;

	public NewLoader()
	{
		super("New Loader");
		setSize(400,400);
		getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(gamePanel = new NewLoaderGamePanel());
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
