/*Método de análisis sintáctico de la entrada referida a una o dos cosas, que pueden o no estar dentro de otras*/
/*Este método se ejecuta:
  - Cuando el jugador invoca una orden sobre uno o dos objetos, uno de los cuales es éste (estén o no estos objetos dentro de un contenedor).
  - Cuando el jugador invoca una orden sobre uno o dos objetos, uno de los cuales está contenido en éste.
*/
void parseCommandOnContentsGeneric ( Mobile aCreature , String verb , String args1 , String args2 , Vector path1 , Vector path2 , Entity obj1 , Entity obj2 ,  boolean goesFirst )
{

    //aCreature: criatura que introduce un comando.
    //verb: comando que introduce, por ejemplo "afilar"
    //args1: parte de la orden que se refiere a un primer objeto, por ejemplo "el cuchillo"
    //args2: parte de la orden que se refiere a un segundo objeto, por ejemplo "con el afilador"
    //path1: camino de contenedores desde el primer objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
        //afilador" y el cuchillo está en una caja, será [cuchillo, caja].
    //path2: camino de contenedores desde el segundo objeto al que referencia la orden. Por ejemplo, si introdujo "afilar el cuchillo con el
        //afilador" y el afilador no está dentro de nada, será [afilador]. Valdrá null si no hay segundo objeto.
    //obj1: primer objeto al que se refiere la acción del jugador (en el ejemplo, el objeto cuchillo).
    //obj2: segundo objeto al que se refiere la acción del jugador (en el ejemplo, el objeto afilador). Valdrá null si no hay segundo objeto.
    //goesFirst: si vale true, es que el primer objeto (obj1) es éste (self) o está contenido en éste.
        //Si vale false, es que el segundo objeto (obj2) es éste (self) o está contenido en éste.


    //terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
    //por defecto ante ella
    //terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
    //comando y ejecución de la acción correspondiente

}
