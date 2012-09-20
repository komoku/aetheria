/*Método de análisis sintáctico de la entrada referida a dos cosas, que no están dentro de otras*/
/*Este método se ejecuta en el mundo cuando el jugador invoca una orden sobre dos objetos no contenidos en otros.
*/
void parseCommandTwoObjects ( Mobile aCreature , String verb , String args1 , String args2 ,  Entity obj1 , Entity obj2  )
{

    //aCreature: criatura que introduce un comando.
    //verb: comando que introduce, por ejemplo "atar"
    //args1: parte de la orden que se refiere a este objeto, por ejemplo "la piedra".
    //args2: parte de la orden que se refiere al otro objeto, por ejemplo "a la tabla"
    //obj1: primer objeto al que se refiere la acción del jugador (la piedra).
    //obj2: segundo objeto al que se refiere la acción del jugador (la tabla).


    //terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
    //por defecto ante ella
    //terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
    //comando y ejecución de la acción correspondiente

}
