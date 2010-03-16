/*M�todo de an�lisis sint�ctico de la entrada referida a una o dos cosas, que no est�n dentro de otras*/
/*Este m�todo se ejecuta cuando el jugador invoca una orden sobre uno o dos objetos que no est�n dentro de un contenedor, uno de los cuales es �ste.
*/
void parseCommandGeneric ( Mobile aCreature , String verb , String args1 , String args2 , Entity obj1 , Entity obj2 ,  boolean goesFirst )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "afilar"
	//args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo"
	//args2: parte de la orden que se refiere a un segundo objeto, por ejemplo "con el afilador". La cadena vac�a si no hay segundo objeto.
	//obj1: primer objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto cuchillo).
	//obj2: segundo objeto al que se refiere la acci�n del jugador (en el ejemplo, el objeto afilador). Valdr� null si no hay segundo objeto.
	//goesFirst: si vale true, es que el primer objeto (obj1) es �ste (self) o est� contenido en �ste.
		//Si vale false, es que el segundo objeto (obj2) es �ste (self) o est� contenido en �ste.
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
