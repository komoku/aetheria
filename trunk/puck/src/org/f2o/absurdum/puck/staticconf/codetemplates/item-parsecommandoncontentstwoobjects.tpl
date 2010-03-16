/*M�todo de an�lisis sint�ctico de la entrada referida a dos cosas, que pueden o no estar dentro de otras*/
/*Este m�todo se ejecuta:
  - Cuando el jugador invoca una orden sobre dos objetos, uno cualquiera de los cuales es �ste (est�n o no estos objetos dentro de un contenedor).
  - Cuando el jugador invoca una orden sobre dos objetos, uno cualquiera de los cuales est� contenido en �ste.
*/
void parseCommandOnContentsTwoObjects ( Mobile aCreature , String verb , String args1 , String args2 , Vector path1 , Vector path2 ,  Entity otherEnt  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "atar"
	//args1: parte de la orden que se refiere a este objeto o a uno contenido en �ste, por ejemplo "la piedra".
	//args2: parte de la orden que se refiere al otro objeto (o a uno contenido en otro), por ejemplo "a la tabla".
	//path1: camino de contenedores desde el objeto referenciado en args1. Por ejemplo, si introdujo "atar la piedra a la tabla"
		//y la piedra est� en una caja, ser� [piedra, caja].
	//path2: camino de contenedores desde el otro objeto al que referencia la orden. Por ejemplo, si introdujo "atar la piedra a la tabla"
		//y la tabla no est� dentro de nada, ser� [tabla].
	//obj2: objeto al que se refiere la acci�n del jugador, aparte de �ste (o del objeto contenido en �ste).
		//si self es la piedra, obj2 ser�a la tabla; si self es la tabla, obj2 ser�a la piedra.
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
