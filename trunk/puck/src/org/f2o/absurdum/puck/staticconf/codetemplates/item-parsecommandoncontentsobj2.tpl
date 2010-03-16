/*M�todo de an�lisis sint�ctico de la entrada referida a dos cosas, que pueden o no estar dentro de otras*/
/*Este m�todo se ejecuta:
  - Cuando el jugador invoca una orden sobre dos objetos, el segundo de los cuales es �ste (est�n o no estos objetos dentro de un contenedor).
  - Cuando el jugador invoca una orden sobre dos objetos, el segundo de los cuales est� contenido en �ste.
*/
void parseCommandOnContentsObj2 ( Mobile aCreature , String verb , String args1 , String args2 , Vector path1 , Vector path2 ,  Entity obj1  )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "afilar"
	//args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo". 	
	//args2: parte de la orden que se refiere a un segundo objeto, por ejemplo "con el afilador". Ese segundo objeto
		//es �ste o est� contenido en �ste.
	//path1: camino de contenedores desde el primer objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
		//afilador" y el cuchillo est� en una caja, ser� [cuchillo, caja].
	//path2: camino de contenedores desde el segundo objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
		//afilador" y el afilador no est� dentro de nada, ser� [afilador].
	//obj1: primer objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto cuchillo).
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
