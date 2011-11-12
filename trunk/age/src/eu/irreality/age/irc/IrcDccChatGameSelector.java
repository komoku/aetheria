/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.irc;
import java.util.*;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.server.PartidaEnCurso;

//analogous to SimpleTelnetClientSelector. AgeIrcBot would be the SimpleTelnetClientHandler.
public class IrcDccChatGameSelector extends IrcDccChatSynchronousHandler
{

	public PartidaEnCurso getPartidaSelection( List partidas )
	{
		//aquí parafernalia del menú

		System.out.println("getPartidaSelection() called");
		
		System.out.println("Socket: " + s);
		System.out.println("Sizeof partidas: " + partidas.size() );
		
		try
		{
			s.waitUntilConnected();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
			return null;
		}
		
		//synchronized(s)
		//{
		
			System.out.println("The Socket is: " + s);
		
			
			write(UIMessages.getInstance().getMessage("irc.note.1")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.2")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.3")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.4")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.5")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.6")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.7")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.8")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.9")+"\n");
			write(UIMessages.getInstance().getMessage("irc.note.1")+"\n");
			
			/*
			write("=========\n");
			
			write("Nota importante sobre el uso de IRC:\n");
			
			write("El Aetheria Game Engine está diseñado para que se pueda jugar a sus juegos a través de distintas plataformas, incluido el IRC.\n");
			write("No obstante, una plataforma como el IRC tiene serias limitaciones y no es el medio óptimo para jugar.\n");
			write("Además de información sobrante que suelen introducir los clientes IRC, como por ejemplo el nick del interlocutor, las desventajas de jugar por IRC son las siguientes:\n");
			write("* Como sólo se pueden mandar líneas no vacías, no hay interacción posible con el usuario del estilo de \"pulsa una tecla para continuar\". Donde el juego esté programado para tales interacciones, se te pedirá que introduzcas una línea no vacía.\n");
			write("* Como sólo se pueden enviar líneas, cada instrucción de escritura del juego se hace corresponder con una línea, forzando saltos de línea donde no debería haberlos. Esto puede hacer más \"feo\" el formato del juego.\n");
			write("* Falta la capacidad multimedia que se obtiene con el cliente de AGE, además de otras opciones como configuraciones de color, etc.\n");
			write("Busca información sobre el cliente de AGE en http://www.irreality.eu/age/index.html?jtexto.htm o descárgatelo en http://code.google.com/p/aetheria/\n");
			
			write("==========\n");
			*/
		
		//}
		
		if ( partidas.size() < 1 )
		{
			write(UIMessages.getInstance().getMessage("server.no.games")+"\n");
			return null;
		}
		if ( partidas.size() == 1 )
		{
			//autoseleccionar la única que hay
			System.out.println("Autoselect enabled.");
			PartidaEnCurso pec = (PartidaEnCurso) partidas.get(0);
			if ( pec.getPlayers() >= pec.getMaxPlayers() )
			{
				; //como si hubiera varias partidas, mostrar menú		
			}
			else
			{
				return pec;
			}
		}
		
		//código para más de una partida en curso:
		
		boolean done = false;
		
		while ( !done )
		{

			write(UIMessages.getInstance().getMessage("server.active.games")+"\n");

			for ( int i = 0 ; i < partidas.size() ; i++ )
			{
				PartidaEnCurso pec = (PartidaEnCurso) partidas.get(i);
				System.out.println(i + "," + pec);
				System.out.println(partidas);
				write ( (i+1) + ". " + pec.getNombre() + " " + "(" + pec.getPlayers() + "/" + pec.getMaxPlayers() + ")" + "\n" ); 
			}

			write(UIMessages.getInstance().getMessage("server.enter.game.number")+" ");

			String linea = getInput();
			int part = -1;
			try
			{
				part = Integer.valueOf ( linea ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				;
			}
			if ( part > 0 && part <= partidas.size() )
			{
				PartidaEnCurso p = (PartidaEnCurso) partidas.get(part-1);
				if ( p.getPlayers() < p.getMaxPlayers() )
				{
					System.out.println("Returnin' one of them.");
					return p;
				}
				else
					write(UIMessages.getInstance().getMessage("server.player.limit.hit")+"\n");
			}


		}
		
		return null;
	}


}