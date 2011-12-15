/*Método de análisis sintáctico de la entrada referida a dos cosas, que pueden o no estar dentro de otras*/
/*Este método se ejecuta en el mundo:
  - Cuando el jugador invoca una orden sobre tales objetos (estén o no estos objetos dentro de un contenedor).
  - Cuando el jugador invoca una orden sobre objetos que los contienen (o sobre uno de esos objetos y uno que contiene el otro).
*/
void parseCommandOnContentsTwoObjects ( Mobile aCreature , String verb , String args1 , String args2 , Vector path1 , Vector path2 ,  Entity obj1 , Entity obj2  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "atar"
	//args1: parte de la orden que se refiere a un primer objeto o a lo contenido en un primer objeto, por ejemplo "la piedra".
	//args2: parte de la orden que se refiere al otro objeto (o a uno contenido en otro), por ejemplo "a la tabla".
	//path1: camino de contenedores desde el objeto referenciado en args1. Por ejemplo, si introdujo "atar la piedra a la tabla"
		//y la piedra está en una caja, será [piedra, caja].
	//path2: camino de contenedores desde el otro objeto al que referencia la orden. Por ejemplo, si introdujo "atar la piedra a la tabla"
		//y la tabla no está dentro de nada, será [tabla].
	//obj1: primer objeto al que se refiere la acción del jugador o que lo contiene.
	//obj2: segundo objeto al que se refiere la acción del jugador o que lo contiene.
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
	//comando y ejecución de la acción correspondiente
	
}
