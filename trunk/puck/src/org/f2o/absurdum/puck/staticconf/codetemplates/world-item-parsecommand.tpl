/*Método de análisis sintáctico de la entrada referida a una cosa*/
void parseCommand( Mobile aCreature , String verb , String args , Entity target )
{
	
	//aCreature: criatura que introduce un comando.
	//verb: comando que introduce, por ejemplo "comer"
	//args: resto de la orden que introduce, por ejemplo "la seta"
	//target: entidad a la que se refiere la orden
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
	//comando y ejecución de la acción correspondiente
	
}
