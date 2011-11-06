/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 06/11/2011 12:50:58
 */
package eu.irreality.age.swing.mdi.gameloader;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.ServerHandler;

class OptionChoosingPanel extends JPanel
{
	JDesktopPane thePanel;
	
	JCheckBox cb1 = new JCheckBox();
	JCheckBox cb2 = new JCheckBox();
	JCheckBox cb3 = new JCheckBox();
	
	//JTextField tf1 = null;
	//JTextField tf2 = null;
	
	JLabel ageLabel = new JLabel( UIMessages.getInstance().getMessage("gameloader.options.ageserver") );
	JLabel telnetLabel = new JLabel( UIMessages.getInstance().getMessage("gameloader.options.telnetserver") );
	JLabel ircLabel = new JLabel( UIMessages.getInstance().getMessage("gameloader.options.ircserver") );
	
	JLabel infoLabel = new JLabel( UIMessages.getInstance().getMessage("gameloader.options.info") );
	
	public boolean servirAGE()
	{
		return cb1.isSelected();
	}
	public boolean servirTelnet()
	{
		return cb2.isSelected();
	}
	public boolean servirIRC()
	{
		return cb3.isSelected();
	}
	
	public void updateServersAndPorts()
	{
		String agePort = String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge());
		String telnetPort = String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet());
		ageLabel.setText("Servidor de AGE (puerto " + agePort + ")");
		telnetLabel.setText("Servidor de telnet (puerto " + telnetPort + ")");
		//tf1.setText(agePort);
		//tf2.setText(telnetPort);
		boolean age = ServerHandler.getInstance().getServerConfigurationOptions().sirveAge();
		boolean telnet = ServerHandler.getInstance().getServerConfigurationOptions().sirveTelnet();
		boolean irc = ServerHandler.getInstance().getServerConfigurationOptions().sirveIrc();
		cb1.setEnabled(age);
		cb2.setEnabled(telnet);
		cb3.setEnabled(irc);
		ageLabel.setEnabled(age);
		telnetLabel.setEnabled(telnet);
		ircLabel.setEnabled(irc);
		if ( !age ) cb1.setSelected(false);
		if ( !telnet ) cb2.setSelected(false);
		if ( !irc ) cb3.setSelected(false);
		//build info label text
		infoLabel.setText(getInfoLabelText(age,telnet,irc));
	}
	
	/**
	 * Returns a text for the info label, which depends on which servers are enabled on the server configuration options.
	 */
	private String getInfoLabelText ( boolean age , boolean telnet , boolean irc )
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<html><p>");
		if ( !age || !telnet || !irc )
		{
			//informar de servidores deshabilitados
			boolean plur = false;
			if ( (!age && !telnet) || (!age && !irc) || (!telnet && !irc) ) plur = true;
			if ( plur ) 
			{
				sb.append("Los servidores de ");
			}
			else
			{
				sb.append("El servidor de ");
			}
			sb.append(getDisabledServerList(age,telnet,irc));
			if ( plur )
				sb.append("	están deshabilitados.</p><p>Para utilizarlos, ");
			else
				sb.append(" está deshabilitado.</p><p>Para utilizarlo, ");
			sb.append("deben habilitarse primero</p><p>en las opciones de servidor (Servidor/Configuración...).</p>");
			sb.append("<p>En dichas opciones también se pueden</p><p>configurar los correspondientes puertos.</p>");
		}
		else
		{
			sb.append("<p>Se pueden cambiar los puertos telnet y AGE</p><p>en las opciones de servidor (Servidor/Configuración...).</p>");
		}
		sb.append("</html>");
		return sb.toString();
	}
	
	/**
	 * Returns a string with a comma-separated list of disabled servers to use in the info label.
	 */
	private String getDisabledServerList ( boolean age , boolean telnet , boolean irc )
	{
		StringBuffer sb = new StringBuffer();
		if ( !age ) sb.append("AGE");
		if ( !telnet )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append("telnet");
		}
		if ( !irc )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append("IRC");
		}
		return sb.toString();
	}
	
	
	public OptionChoosingPanel ( final JInternalFrame madre )
	{
		JPanel pan0 = new JPanel();
		JPanel pan1 = new JPanel();
		JPanel pan2 = new JPanel();
		JPanel pan3 = new JPanel();
		JPanel pan4 = new JPanel();
		JLabel l0 = new JLabel("<html><p>Las partidas lanzadas desde esta ventana</p><p>serán accesibles remotamente a través de:</p>");
		JLabel l1 = ageLabel;
		JLabel l2 = telnetLabel;
		JLabel l3 = ircLabel;
		JLabel l4 = infoLabel;
		//tf1 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoAge()));
		//tf2 = new JTextField(String.valueOf(ServerHandler.getInstance().getServerConfigurationOptions().getPuertoTelnet()));
		//setLayout ( new GridLayout ( 5 , 1 ) );
		setLayout ( new BoxLayout(this,BoxLayout.PAGE_AXIS) );
		
		setBackground ( GameChoosingInternalFrame.BACKGROUND_COLOR );
		pan0.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan0.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l0.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l0.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan1.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan1.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l1.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l1.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan2.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan2.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l2.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l2.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan3.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan3.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l3.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l3.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		pan4.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		pan4.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		l4.setBackground(GameChoosingInternalFrame.BACKGROUND_COLOR);
		l4.setForeground(GameChoosingInternalFrame.FOREGROUND_COLOR);
		
		//tf1.setEditable(false);
		//tf2.setEditable(false);
		//tf1.setEnabled(false);
		//tf2.setEnabled(false);
		
		pan0.add(l0);
		
		pan1.add ( cb1 );
		pan1.add ( l1 );
		//pan1.add ( tf1 );
		pan2.add ( cb2 );
		pan2.add ( l2 );
		//pan2.add ( tf2 );
		pan3.add ( cb3 );
		pan3.add ( l3 );
		pan4.add ( l4 );
		
		add(pan0);add(pan1);add(pan2);add(pan3);add(pan4);
		
		
	}
	
}