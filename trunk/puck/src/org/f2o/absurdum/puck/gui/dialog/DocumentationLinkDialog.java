package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.i18n.Messages;

public class DocumentationLinkDialog extends JDialog
{
	
	private JButton bClose = new JButton(Messages.getInstance().getMessage("button.clo"));

	
    private void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                        desktop.browse(uri);
                } catch (IOException e) {
                	JOptionPane.showMessageDialog(this, "No puedo encontrar el navegador por defecto de tu sistema. Por favor, navega hasta http://www.caad.es/aetheria/doc a mano.");
                }
        } else {
        	JOptionPane.showMessageDialog(this, "No puedo encontrar el navegador por defecto de tu sistema. Por favor, navega hasta http://www.caad.es/aetheria/doc a mano.");
        }
    }
	
	public DocumentationLinkDialog ( final PuckFrame parent , boolean modal )
	{
		
		super(parent,modal);
		this.setTitle(Messages.getInstance().getMessage("menu.help.toc"));
		
		this.getContentPane().setLayout( new BorderLayout() );
		JPanel mainPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		
		/*
		JLabel label = new JLabel(
				"<html><p>Puedes encontrar ayuda detallada sobre c�mo crear aventuras en PUCK en su documentaci�n online:</p>" +
				"<a href=\"http://www.caad.es/aetheria/doc\">http://www.caad.es/aetheria/doc</a></html>"
				);
		*/
		
		//mainPanel.add(label);
		
		try
		{
			final URI uri = new URI("http://www.caad.es/aetheria/doc/");
			JButton button = new JButton();
	        button
	                        .setText(
	                        "<html>" +
	                        "<p>Puedes consultar informaci�n detallada sobre c�mo construir mundos en PUCK en la documentaci�n online: " +
	                        "<FONT color=\"#000099\"><U>http://www.caad.es/aetheria/doc/</U></FONT>.</p></html>");
	        button.setHorizontalAlignment(SwingConstants.LEFT);
	        button.setBorderPainted(false);
	        button.setOpaque(false);
	        button.setBackground(Color.WHITE);
	        button.setToolTipText(uri.toString());
	        button.addActionListener(new ActionListener() {
	                @Override
	                public void actionPerformed(ActionEvent e) {
	                        open(uri);
	                }
	        });
	        mainPanel.add(button);
		}
		catch ( URISyntaxException use )
		{
			use.printStackTrace();
		}
        
		bClose.addActionListener ( new ActionListener() 
		{		
			public void actionPerformed(ActionEvent e) 
			{

				DocumentationLinkDialog.this.dispose();
			}
		}
		);
		
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(bClose,BorderLayout.EAST);
		
		this.getContentPane().add(mainPanel,BorderLayout.CENTER);
		this.getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
		
		pack();
		
	}
	
}
