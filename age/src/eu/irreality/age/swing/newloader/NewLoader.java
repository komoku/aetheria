package eu.irreality.age.swing.newloader;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class NewLoader extends JFrame 
{

	public NewLoader()
	{
		super("New Loader");
		setSize(400,400);
		getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(new NewLoaderGamePanel());
		pack();
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
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
