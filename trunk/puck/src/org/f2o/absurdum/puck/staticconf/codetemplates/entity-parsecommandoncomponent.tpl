/* Método para reaccionar a entradas referidas a un componente de esta entidad */
void parseCommandOnComponent( Mobile aCreature , String verb , String args )
{
    //aCreature: criatura que introduce un comando.
    //verb: comando que introduce, por ejemplo "afilar"
    //args: resto de la orden que introduce, por ejemplo "la hoja"

    if ( !equals(verb,"mirar") )
    {
        //...
    }

    //terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
    //por defecto ante ella
    //terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal de la orden

}