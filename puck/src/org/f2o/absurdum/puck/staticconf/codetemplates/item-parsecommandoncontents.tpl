/*Método de análisis sintáctico de la entrada referida a una cosa, que puede estar contenida dentro de otra*/
/*Este método se ejecuta:
  - Cuando el jugador invoca una orden sobre este objeto (esté o no dentro de un contenedor).
  - Cuando este objeto es un contenedor, y el jugador invoca una orden sobre un objeto contenido en él.
      [en este caso, el objeto sobre el que invocó la orden se obtiene como ((Item)path.get(0))].
*/
void parseCommandOnContents ( Mobile aCreature , String verb , String args , Vector path )
{

    //aCreature: criatura que introduce un comando.
    //verb: comando que introduce, por ejemplo "comer"
    //args: resto de la orden que introduce, por ejemplo "la seta"
    //path: camino de contenedores desde el objeto que introdujo el jugador. Por ejemplo, si introdujo "comer la seta" y la seta
    //   está en una caja, este vector será [seta, caja]. Si la caja está a su vez dentro de un baúl, [seta, caja, baúl]


    //terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
    //por defecto ante ella
    //terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
    //comando y ejecución de la acción correspondiente

}
