/*Método de análisis sintáctico de la entrada referida a dos cosas, que no están dentro de otras*/
/*Este método se ejecuta cuando el jugador invoca una orden sobre dos objetos, que no están en contenedores, y el segundo de los cuales es éste.
*/
void parseCommandObj2 ( Mobile aCreature , String verb , String args1 , String args2 , Entity obj1  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "afilar"
	//args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo".
	//args2: parte de la orden que se refiere a un segundo objeto (que es este objeto), por ejemplo "con el afilador"
	//obj2: primer objeto al que se refiere la acción del jugador (en el ejemplo, el objeto cuchillo).

	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
	//comando y ejecución de la acción correspondiente
	
}
