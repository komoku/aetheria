/*M�todo de an�lisis sint�ctico de la entrada referida a una cosa, que puede estar contenida dentro de otra*/
/*Este m�todo se ejecuta:
  - Cuando el jugador invoca una orden sobre este objeto (est� o no dentro de un contenedor).
  - Cuando este objeto es un contenedor, y el jugador invoca una orden sobre un objeto contenido en �l.
      [en este caso, el objeto sobre el que invoc� la orden se obtiene como ((Item)path.get(0))].
*/
void parseCommandOnContents ( Mobile aCreature , String verb , String args , Vector path )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "comer"
	//args: resto de la orden que introduce, por ejemplo "la seta"
	//path: camino de contenedores desde el objeto que introdujo el jugador. Por ejemplo, si introdujo "comer la seta" y la seta
	//   est� en una caja, este vector ser� [seta, caja]. Si la caja est� a su vez dentro de un ba�l, [seta, caja, ba�l]
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente
	
}
