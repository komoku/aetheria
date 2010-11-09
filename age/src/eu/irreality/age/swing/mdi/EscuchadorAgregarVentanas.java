package eu.irreality.age.swing.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;

import eu.irreality.age.SwingAetheriaGameLoader;

class EscuchadorAgregarVentanas implements ActionListener 
{

	JDesktopPane thePanel;

	public EscuchadorAgregarVentanas ( JDesktopPane p )
	{
		thePanel = p;		
	}

	public void actionPerformed ( ActionEvent evt )
	{
		new SwingAetheriaGameLoader ( "" , thePanel , false , null , null , false );
		thePanel.setVisible(true);
	}
}
