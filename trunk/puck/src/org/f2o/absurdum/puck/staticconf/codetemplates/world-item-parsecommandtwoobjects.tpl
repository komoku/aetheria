/*M�todo de an�lisis sint�ctico de la entrada referida a dos cosas, que no est�n dentro de otras*/
/*Este m�todo se ejecuta en el mundo cuando el jugador invoca una orden sobre dos objetos no contenidos en otros.
*/
void parseCommandTwoObjects ( Mobile aCreature , String verb , String args1 , String args2 ,  Entity obj1 , Entity obj2  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "atar"
	//args1: parte de la orden que se refiere a este objeto, por ejemplo "la piedra". 
	//args2: parte de la orden que se refiere al otro objeto, por ejemplo "a la tabla"
	//obj1: primer objeto al que se refiere la acci�n del jugador (la piedra).
	//obj2: segundo objeto al que se refiere la acci�n del jugador (la tabla).
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
