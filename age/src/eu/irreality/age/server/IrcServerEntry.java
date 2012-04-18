package eu.irreality.age.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class IrcServerEntry implements java.io.Serializable
{
	
	private String servidor;
	private int port;
	private String nick;
	
	private boolean privado;
	private boolean canal;
	private boolean dcc;
	
	private List canales;
	
	public String toString()
	{
		return nick + "@" + servidor + ":" + port;
	}
	
	public IrcServerEntry ( String server , int port , String nick , boolean privado , boolean canal , boolean dcc , List canales )
	{
		this.servidor = server;
		this.port = port;
		this.nick = nick;
		this.privado = privado;
		this.canal = canal;
		this.dcc = dcc;
		this.canales = new ArrayList();
		this.canales.addAll(canales);
	}
	
	public IrcServerEntry ( String server , String port , String nick , boolean privado , boolean canal , boolean dcc , List canales ) throws NumberFormatException
	{
		this.servidor = server;
		this.port = Integer.valueOf ( port ).intValue();
		this.nick = nick;
		this.privado = privado;
		this.canal = canal;
		this.dcc = dcc;
		this.canales = new ArrayList();
		this.canales.addAll(canales);
	}
	
	public IrcServerEntry ()
	{
		//constructor called by serialization
	}
	
	public String getServer()
	{return servidor;
	}
	public String getNick()
	{return nick;
	}
	public int getPort()
	{return port;
	}
	public boolean respondeAPrivados()
	{return privado;
	}
	public boolean respondeACanales()
	{return canal;
	}
	public boolean respondeADCC()
	{return dcc;
	}
	public List getChannels()
	{return canales;
	}
	
}