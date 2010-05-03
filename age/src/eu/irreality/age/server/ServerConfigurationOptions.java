package eu.irreality.age.server;

import java.util.ArrayList;

public class ServerConfigurationOptions implements java.io.Serializable
{
	private boolean telnet;
	private boolean age;
	private boolean irc;
	private boolean initOnStartup;
	private int tPort;
	private int aPort;
	
	private java.util.List ircServerList; //list of IrcServerEntry
	
	private java.util.List dedicatedGameList; //list of GameEntry

	public ServerConfigurationOptions ( boolean age , boolean telnet , boolean irc ,
			boolean initOnStartup , int tPort , int aPort , java.util.List ircServerList ,
			java.util.List dedicatedGameList )
			{
				this.telnet=telnet;
				this.age=age;
				this.irc=irc;
				this.initOnStartup=initOnStartup;
				this.tPort=tPort;
				this.aPort=aPort;
				//copy lists -> no lateral effects
				this.ircServerList = new ArrayList();
				for ( int i = 0 ; i < ircServerList.size() ; i++ )
					this.ircServerList.add ( ircServerList.get(i) );
				this.dedicatedGameList = new ArrayList();
				for ( int i = 0 ; i < dedicatedGameList.size() ; i++ )
					this.dedicatedGameList.add ( dedicatedGameList.get(i) );	
			}
			
	public boolean sirveTelnet()
	{return telnet;
	}
	public boolean sirveAge()
	{return age;
	}
	public boolean sirveIrc()
	{return irc;
	}
	public boolean initOnStartup()
	{return initOnStartup;
	}	
	public int getPuertoTelnet()
	{return tPort;
	}
	public int getPuertoAge()
	{return aPort;
	}
	public java.util.List getListaServidoresIrc()
	{return ircServerList;
	}
	public java.util.List getListaPartidasDedicadas()
	{return dedicatedGameList;
	}

}
