/*M�todo de an�lisis sint�ctico de la entrada referida a dos cosas, que no est�n dentro de otras*/
/*Este m�todo se ejecuta cuando el jugador invoca una orden sobre dos objetos, que no est�n en contenedores, y el segundo de los cuales es �ste.
*/
void parseCommandObj2 ( Mobile aCreature , String verb , String args1 , String args2 , Entity obj1  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "afilar"
	//args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo".
	//args2: parte de la orden que se refiere a un segundo objeto (que es este objeto), por ejemplo "con el afilador"
	//obj2: primer objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto cuchillo).

	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
