/*M�todo de an�lisis sint�ctico de la entrada referida a una o dos cosas, que pueden o no estar dentro de otras*/
/*Este m�todo se ejecuta:
  - Cuando el jugador invoca una orden sobre uno o dos objetos, uno de los cuales es �ste (est�n o no estos objetos dentro de un contenedor).
  - Cuando el jugador invoca una orden sobre uno o dos objetos, uno de los cuales est� contenido en �ste.
*/
void parseCommandOnContentsGeneric ( Mobile aCreature , String verb , String args1 , String args2 , Vector path1 , Vector path2 , Entity obj1 , Entity obj2 ,  boolean goesFirst )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "afilar"
	//args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo"
	//args2: parte de la orden que se refiere a un segundo objeto, por ejemplo "con el afilador"
	//path1: camino de contenedores desde el primer objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
		//afilador" y el cuchillo est� en una caja, ser� [cuchillo, caja].
	//path2: camino de contenedores desde el segundo objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
		//afilador" y el afilador no est� dentro de nada, ser� [afilador]. Valdr� null si no hay segundo objeto.
	//obj1: primer objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto cuchillo).
	//obj2: segundo objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto afilador). Valdr� null si no hay segundo objeto.
	//goesFirst: si vale true, es que el primer objeto (obj1) es �ste (self) o est� contenido en �ste.
		//Si vale false, es que el segundo objeto (obj2) es �ste (self) o est� contenido en �ste.
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
