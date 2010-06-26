/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eu.irreality.age.debug.Debug;

public class EntityList
{

	/**Vector que contiene las entidades.*/
	protected java.util.Vector laLista;

	public Object clone()
	{
		EntityList el = new EntityList();
		el.laLista = (java.util.Vector) this.laLista.clone();
		return el;
	}
	
	/**
	* La función addElement de java.util.Vector.
	*
	*/	
	private void addElement ( Entity o )
	{
		laLista.addElement ( o );
	}
	
	public void addEntity ( Entity e )
	{
		addElement(e);
	}
	
	/**
	* La función removeElement de java.util.Vector.
	*
	*/	
	private boolean removeElement ( Entity o )
	{
		return laLista.removeElement ( o );
	}
	
	public boolean remove ( Object o )
	{
		return laLista.remove(o);
	}
	
	public boolean removeAll ( Collection c )
	{
		boolean changed = false;
		for (Iterator iterator = c.iterator(); iterator.hasNext();) 
		{
			Object object = (Object) iterator.next();
			if ( this.remove(object) ) changed = true;
		}
		return changed;
	}

	public boolean removeAll ( EntityList el )
	{
		boolean changed = false;
		for ( int i = 0 ; i < el.size() ; i++ )
		{
			Object object = (Object) el.get(i);
			if ( this.remove(object) ) changed = true;
		}
		return changed;
	}
	
	public int size ( )
	{
		return laLista.size ( );
	}
	
	public Entity entityAt ( int i )
	{
		return (Entity) laLista.elementAt(i);
	}
	
	public Entity get ( int i )
	{
		return (Entity)laLista.get(i);
	}	
	
	public boolean isEmpty ( )
	{
		return laLista.isEmpty();
	}
	
	/*general pattern matching: generates vector of matches*/
	public java.util.Vector patternMatch ( String arguments, boolean singOrPlur )
	{
	
		//Debug.println("patternMatch call: " + this + " - " + arguments + " - " + singOrPlur );
	
	
	
		java.util.Vector resultado = new java.util.Vector ( );
		int [] prioridades = new int [ size() ];
		for ( int i = 0 ; i < size() ; i++ )
		{ 
		
			if ( entityAt(i) == null ) continue; //esto pasa en los wieldedWeapons invs. p.ej.
		
			int currentMatchPriority = entityAt(i).matchesCommand(arguments,singOrPlur);
			
			//Debug.println(">priority " + currentMatchPriority + "[" + entityAt(i) + "]" );
			
			//con el plural, "dejar piedras" devolvera lista de piedras para dejarlas todas. Con el singular, normalmente solo consideraremos una de la lista. Pero esto no es cosa de esta función.
			if ( ( currentMatchPriority != 0 ) )
			{
				//insertar en el vector
				int j = 0;
				while ((j<size()) && ( prioridades[j] != 0 ) && ( prioridades[j] < currentMatchPriority ))
				{
					j++;
				}
				//hemos encontrado la posicion
				Debug.println("Added " + entityAt(i).getID() + " at pos " + j);
				
				if ( j < resultado.size() ) 
					resultado.add ( j , entityAt(i) );
				else
					resultado.add ( entityAt(i) );
				
				for ( int k = size()-2 ; k >= j ; k-- )
				{
					prioridades[k+1] = prioridades[k];
				}
				prioridades[j] = currentMatchPriority;
			}
		}
		if ( resultado.size() == 2 )
		{
			Debug.println("Vector is " + "[" + ((Entity)resultado.get(0)).getID() + "," + ((Entity)resultado.get(1)).getID() + "]" );
		}
		return resultado;
	}
	
	
	
	/*if pearl is inside box inside chest, and we ask for pearl, then
	 * this generates a [pearl,box,chest] vector (full path to pearl)
	 * 
	 * i think no, actually it generates a vector of vectors, where each subvector is a path
	 * [pearl,box,chest], [pearl,bottle] if there are two pearls.
	 */
	public java.util.Vector patternMatchWithRecursion ( String arguments , boolean singOrPlur )
	{
		java.util.Vector resultado = new java.util.Vector ( );
		java.util.Vector path = new java.util.Vector ( );
		return patternMatchWithRecursion ( arguments , singOrPlur , path , resultado );
	}

	protected java.util.Vector patternMatchWithRecursion ( String arguments, boolean singOrPlur , java.util.Vector /*of entities*/ path , java.util.Vector resultado )
	{
	
		//Debug.println("patternMatch call: " + this + " - " + arguments + " - " + singOrPlur );
	
	
		//java.util.Vector resultado = new java.util.Vector ( );
		int [] prioridades = new int [ size() ];
		for ( int i = 0 ; i < size() ; i++ )
		{ 
		
			if ( entityAt(i) == null ) continue; //esto pasa en los wieldedWeapons invs. p.ej.
		
			int currentMatchPriority = entityAt(i).matchesCommand(arguments,singOrPlur);
			
			//Debug.println(">priority " + currentMatchPriority + "[" + entityAt(i) + "]" );
			
			//con el plural, "dejar piedras" devolvera lista de piedras para dejarlas todas. Con el singular, normalmente solo consideraremos una de la lista. Pero esto no es cosa de esta función.
			if ( ( currentMatchPriority != 0 ) )
			{
				//insertar en el vector
				int j = 0;
				while ((j<size()) && ( prioridades[j] != 0 ) && ( prioridades[j] < currentMatchPriority ))
				{
					j++;
				}
				//hemos encontrado la posicion
				Debug.println("Added " + entityAt(i).getID() + " at pos " + j);
				
				java.util.Vector pathToAdd = (java.util.Vector) path.clone();
				pathToAdd.add(0,entityAt(i));
				
				if ( j < resultado.size() ) 
					resultado.add ( j , pathToAdd );
				else
					resultado.add ( pathToAdd );
				
				for ( int k = size()-2 ; k >= j ; k-- )
				{
					prioridades[k+1] = prioridades[k];
				}
				prioridades[j] = currentMatchPriority;
			}
			
			if ( entityAt(i) instanceof Item && ((Item)entityAt(i)).getContents() != null )
			{
				Inventory inv = ((Item)entityAt(i)).getContents();
				java.util.Vector newPath = (java.util.Vector) path.clone();
				newPath.add(0,entityAt(i));
				((Item)entityAt(i)).getContents().patternMatchWithRecursion(arguments,singOrPlur,newPath,resultado);
			}
			
		}
		return resultado;
	}

	
	
	
	/*
	"pattern matching" para cuando se buscan en una frase referencias a dos objetos, no a uno
	(ejemplo: abrir la puerta con la llave)
	*/
	public java.util.Vector [] patternMatchTwoWithRecursion ( String arguments , boolean singOrPlur1 , boolean singOrPlur2 )
	{ 
		//int [] prioridades = new int [ size() ];
		
		java.util.Vector[] resultado = new java.util.Vector [2];
		
		//El procedimiento es dividir el string en todos los pares de strings posibles
		//atendiendo a sus tokens, y hacer patternmatching en cada uno.
		//Ej. "la puerta con la llave"
		// probamos "la", "puerta con la llave" -> falla.
		// "la puerta", "con la llave" -> ¡OK!, encontrados puerta y llave -> return.
		
		//punto division 2? tokens 1-2, 3-5.
		// ergo, punto division va de 1 a 4 (ntoks-1). p.d. 5 sería un solo objeto.
		
		int ntokens = StringMethods.numToks ( arguments , ' ' );
		
		//probar todas las posibles divisiones en tokens.
		//¿Por qué va al revés? Porque son siempre mejores las divisiones "tardías":
		//es mejor poner la cinta roja * en el armario azul que poner la cinta * roja en el armario azul.
			for ( int punto_division = ntokens-1 ; punto_division >= 1 ; punto_division-- )
			{
				String parte1 = StringMethods.getToks ( arguments , 1 , punto_division , ' ' );
				String parte2 = StringMethods.getToks ( arguments , punto_division+1 , ntokens , ' ' );
				java.util.Vector result1 = patternMatchWithRecursion ( parte1 , singOrPlur1 , new java.util.Vector() , new java.util.Vector() );
				java.util.Vector result2 = patternMatchWithRecursion ( parte2 , singOrPlur2 , new java.util.Vector() , new java.util.Vector() );
				if ( result1.size() > 0 && result2.size() > 0 )
				{
					resultado[0] = result1;
					resultado[1] = result2;
					return resultado;
				}
			}
		resultado[0] = new java.util.Vector();
		resultado[1] = new java.util.Vector();
		return resultado; //el vacio (no se han encontrado combos de dos objetos)
	}
	
	

	/*
	"pattern matching" para cuando se buscan en una frase referencias a dos objetos, no a uno
	(ejemplo: abrir la puerta con la llave)
	*/
	public java.util.Vector [] patternMatchTwo ( String arguments , boolean singOrPlur1 , boolean singOrPlur2 )
	{ 
		//int [] prioridades = new int [ size() ];
		
		java.util.Vector[] resultado = new java.util.Vector [2];
		
		//El procedimiento es dividir el string en todos los pares de strings posibles
		//atendiendo a sus tokens, y hacer patternmatching en cada uno.
		//Ej. "la puerta con la llave"
		// probamos "la", "puerta con la llave" -> falla.
		// "la puerta", "con la llave" -> ¡OK!, encontrados puerta y llave -> return.
		
		//punto division 2? tokens 1-2, 3-5.
		// ergo, punto division va de 1 a 4 (ntoks-1). p.d. 5 sería un solo objeto.
		
		int ntokens = StringMethods.numToks ( arguments , ' ' );
		
		//probar todas las posibles divisiones en tokens.
		//¿Por qué va al revés? Porque son siempre mejores las divisiones "tardías":
		//es mejor poner la cinta roja * en el armario azul que poner la cinta * roja en el armario azul.
			for ( int punto_division = ntokens-1 ; punto_division >= 1 ; punto_division-- )
			{
				String parte1 = StringMethods.getToks ( arguments , 1 , punto_division , ' ' );
				String parte2 = StringMethods.getToks ( arguments , punto_division+1 , ntokens , ' ' );
				java.util.Vector result1 = patternMatch ( parte1 , singOrPlur1 );
				java.util.Vector result2 = patternMatch ( parte2 , singOrPlur2 );
				if ( result1.size() > 0 && result2.size() > 0 )
				{
					resultado[0] = result1;
					resultado[1] = result2;
					return resultado;
				}
			}
		resultado[0] = new java.util.Vector();
		resultado[1] = new java.util.Vector();
		return resultado; //el vacio (no se han encontrado combos de dos objetos)
	}
	
	
	/*
	Lo mismo que en el anterior; pero cuando los items han de estar en dos inventarios
	diferentes. Por ejemplo, para "abrir puerta con llave" normalmente se usaría esta
	función (a no ser que llevemos la puerta) por estar la llave en nuestro inventario
	y la puerta en el de la habitación.
	*/
	public java.util.Vector [] patternMatchTwo ( EntityList i , String arguments , boolean singOrPlur1 , boolean singOrPlur2 )
	{ 
		//int [] prioridades = new int [ size() ];
		
		java.util.Vector[] resultado = new java.util.Vector [2];
		
		//El procedimiento es dividir el string en todos los pares de strings posibles
		//atendiendo a sus tokens, y hacer patternmatching en cada uno.
		//Ej. "la puerta con la llave"
		// probamos "la", "puerta con la llave" -> falla.
		// "la puerta", "con la llave" -> ¡OK!, encontrados puerta y llave -> return.
		
		//punto division 2? tokens 1-2, 3-5.
		// ergo, punto division va de 1 a 4 (ntoks-1). p.d. 5 sería un solo objeto.
		
		int ntokens = StringMethods.numToks ( arguments , ' ' );
		
		//probar todas las posibles divisiones en tokens
			for ( int punto_division = ntokens-1 ; punto_division >= 1 ; punto_division-- )
			{
				String parte1 = StringMethods.getToks ( arguments , 1 , punto_division , ' ' );
				String parte2 = StringMethods.getToks ( arguments , punto_division+1 , ntokens , ' ' );
				java.util.Vector result1 = patternMatch ( parte1 , singOrPlur1 );
				java.util.Vector result2 = i.patternMatch ( parte2 , singOrPlur2 );
				if ( result1.size() > 0 && result2.size() > 0 )
				{
					resultado[0] = result1;
					resultado[1] = result2;
					return resultado;
				}
			}
		resultado[0] = new java.util.Vector();
		resultado[1] = new java.util.Vector();
		return resultado; //el vacio (no se han encontrado combos de dos objetos)
	}
	
	public EntityList()
	{
		laLista = new java.util.Vector();
	}

}


