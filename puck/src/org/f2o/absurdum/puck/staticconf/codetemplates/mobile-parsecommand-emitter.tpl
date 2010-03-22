/*
Método de análisis sintáctico de la entrada introducida por este personaje jugador.
Ojo: no confundir con los métodos para analizar la entrada *referida* a este personaje.
Ésos son distintos.
*/
String parseCommand( String verb , String args )
{
	
	//verb: comando que introduce el jugador, por ejemplo "coger"
	//args: resto de la orden que introduce, por ejemplo "el cuchillo grande"
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: después de nuestro procesado, se lleva a cabo el análisis normal del
	//comando y ejecución de la acción correspondiente. Si se quiere cambiar la cadena de entrada
	//por otra para siguientes pasos del análisis, devuélvase dicha cadena. Si no, devuélvase null.
	
}