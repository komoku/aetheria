package eu.irreality.age.server;

import eu.irreality.age.World;

//se crea a partir de una PartidaEntry
public class PartidaEnCurso
{
	
	private World mundo;
	private int maxPlayers;
	private String nombrePartida;
	private String passwordPartida;
	
	public PartidaEnCurso ( World mundo , int maxPlayers , String nombrePartida , String passwordPartida )
	{
		this.mundo = mundo;
		this.maxPlayers = maxPlayers;
		this.nombrePartida = nombrePartida;
		this.passwordPartida = passwordPartida;
	}
	
	public int getMaxPlayers()
	{
		return maxPlayers;
	}
	public int getPlayers()
	{
			return mundo.getNumberOfConnectedPlayers();
	}
	public World getMundo()
	{
		return mundo;
	}
	public String getNombre()
	{
		return nombrePartida;
	}
	public String getPass()
	{
		return passwordPartida;
	}
	
}

