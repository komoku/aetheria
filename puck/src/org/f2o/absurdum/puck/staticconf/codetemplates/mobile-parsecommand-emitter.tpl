/*
M�todo de an�lisis sint�ctico de la entrada introducida por este personaje jugador.
Ojo: no confundir con los m�todos para analizar la entrada *referida* a este personaje.
�sos son distintos.
*/
String parseCommand( String verb , String args )
{
	
	//verb: comando que introduce el jugador, por ejemplo "coger"
	//args: resto de la orden que introduce, por ejemplo "el cuchillo grande"
	
	
	//terminar con end(): interceptamos la frase, no se ejecuta lo que se tenga que ejecutar
	//por defecto ante ella
	//terminar normal: despu�s de nuestro procesado, se lleva a cabo el an�lisis normal del
	//comando y ejecuci�n de la acci�n correspondiente. Si se quiere cambiar la cadena de entrada
	//por otra para siguientes pasos del an�lisis, devu�lvase dicha cadena. Si no, devu�lvase null.
	
}