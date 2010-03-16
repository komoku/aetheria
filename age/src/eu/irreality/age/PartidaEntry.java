package eu.irreality.age;

public class PartidaEntry implements java.io.Serializable
{

	private GameInfo juego;

	private String nombrePartida;
	private int maxPlayers;
	private String passwordPartida; //null = no hay password
	
	private boolean servirAge;
	private boolean servirTelnet;
	private boolean servirIrc;	

	public String toString()
	{
		return nombrePartida + " [" + maxPlayers + "]";
	}
	
	public PartidaEntry ( GameInfo juego , String nombrePartida , int maxPlayers , String passwordPartida , boolean servirAge , boolean servirTelnet , boolean servirIrc )
	{
		this.juego = juego;
		this.nombrePartida = nombrePartida;
		this.maxPlayers = maxPlayers;
		this.passwordPartida = passwordPartida;
		this.servirAge = servirAge;
		this.servirTelnet = servirTelnet;
		this.servirIrc = servirIrc;
	}
	
	public PartidaEntry ( GameInfo juego , String nombrePartida , String maxPlayers , String passwordPartida , boolean servirAge , boolean servirTelnet , boolean servirIrc ) throws NumberFormatException
	{
		this.juego = juego;
		this.nombrePartida = nombrePartida;
		this.maxPlayers = Integer.valueOf ( maxPlayers ).intValue();
		this.passwordPartida = passwordPartida;
		this.servirAge = servirAge;
		this.servirTelnet = servirTelnet;
		this.servirIrc = servirIrc;
	}
	
	public PartidaEntry ( )
	{
		//constructor called by serialization
	}
	
	public String getName()
	{return nombrePartida;
	}
	public int getMaxPlayers()
	{return maxPlayers;
	}
	public String getPassword()
	{return passwordPartida;
	}
	public boolean sirveAge()
	{return servirAge;
	}
	public boolean sirveTelnet()
	{return servirTelnet;
	}
	public boolean sirveIrc()
	{return servirIrc;
	}	
	public GameInfo getGameInfo()
	{return juego;
	}
	

}