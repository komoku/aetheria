/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;

//analogous to SimpleTelnetClientSelector. AgeIrcBot would be the SimpleTelnetClientHandler.
public class IrcDccChatGameSelector extends IrcDccChatSynchronousHandler
{

	public PartidaEnCurso getPartidaSelection( List partidas )
	{
		//aqu� parafernalia del men�

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
		
			write("=========\n");
			
			write("Nota importante sobre el uso de IRC:\n");
			
			write("El Aetheria Game Engine est� dise�ado para que se pueda jugar a sus juegos a trav�s de distintas plataformas, incluido el IRC.\n");
			write("No obstante, una plataforma como el IRC tiene serias limitaciones y no es el medio �ptimo para jugar.\n");
			write("Adem�s de informaci�n sobrante que suelen introducir los clientes IRC, como por ejemplo el nick del interlocutor, las desventajas de jugar por IRC son las siguientes:\n");
			write("* Como s�lo se pueden mandar l�neas no vac�as, no hay interacci�n posible con el usuario del estilo de \"pulsa una tecla para continuar\". Donde el juego est� programado para tales interacciones, se te pedir� que introduzcas una l�nea no vac�a.\n");
			write("* Como s�lo se pueden enviar l�neas, cada instrucci�n de escritura del juego se hace corresponder con una l�nea, forzando saltos de l�nea donde no deber�a haberlos. Esto puede hacer m�s \"feo\" el formato del juego.\n");
			write("* Falta la capacidad multimedia que se obtiene con el cliente de AGE, adem�s de otras opciones como configuraciones de color, etc.\n");
			write("Busca informaci�n sobre el cliente de AGE en http://www.irreality.org/aetheria/conv.htm\n");
			
			write("==========\n");
		
		//}
		
		if ( partidas.size() < 1 )
		{
			write("�No hay partidas en curso!\n");
			return null;
		}
		if ( partidas.size() == 1 )
		{
			//autoseleccionar la �nica que hay
			System.out.println("Autoselect enabled.");
			PartidaEnCurso pec = (PartidaEnCurso) partidas.get(0);
			if ( pec.getPlayers() >= pec.getMaxPlayers() )
			{
				; //como si hubiera varias partidas, mostrar men�		
			}
			else
			{
				return pec;
			}
		}
		
		//c�digo para m�s de una partida en curso:
		
		boolean done = false;
		
		while ( !done )
		{

			write("PARTIDAS EN CURSO:\n");

			for ( int i = 0 ; i < partidas.size() ; i++ )
			{
				PartidaEnCurso pec = (PartidaEnCurso) partidas.get(i);
				System.out.println(i + "," + pec);
				System.out.println(partidas);
				write ( (i+1) + ". " + pec.getNombre() + " " + "(" + pec.getPlayers() + "/" + pec.getMaxPlayers() + ")" + "\n" ); 
			}

			write("Introduzca el n�mero de la partida a la que desea jugar: ");

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
					write("Esa partida ha alcanzado su n�mero m�ximo de usuarios, no puedes entrar.\n");
			}


		}
		
		return null;
	}


}