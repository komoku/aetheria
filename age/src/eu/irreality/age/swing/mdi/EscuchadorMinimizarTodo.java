package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public class EscuchadorMinimizarTodo implements ActionListener
{

	JDesktopPane thePanel;

	public EscuchadorMinimizarTodo ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt ) 
	{
		JInternalFrame[] lasVentanas = thePanel.getAllFrames();
		try
		{
			for ( int i = 0 ; i < lasVentanas.length ; i++ )
				lasVentanas[i].setIcon(true);
		}
		catch ( java.beans.PropertyVetoException excest )
		{
		}
	}

}