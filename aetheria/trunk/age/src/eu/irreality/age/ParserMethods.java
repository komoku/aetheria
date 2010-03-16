/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;

public class ParserMethods
{
	
	/*
	Las siguientes tres funciones est�n hechas para situaciones particulares, tipo puzzle,
	donde s�lo sirva un objeto concreto. Son para ser llamadas por c�digo EVA o similar,
	no utilizadas por las funciones del juego, ya que se refieren a objetos particulares.
	Para cosas generales, mejor es EntityList::patternMatch() y similares.
	�stas est�n para puzzles, p. ej. si en un sitio no hay m�s huevos que lanzar una piedra
	a una l�mpara, buscar la piedra y la l�mpara en el comando lanzar (en vez de tener un
	comando general)
	Al buscar s�lo objetos concretos, a estas funciones tampoco les importan las prioridades
	de referencia de los objetos.
	*/
	
	
	/*
	Atenci�n al detalle, en las funciones que no tienen "in" se puede dejar alguna entidad
	a null para que no la mire. Ejemplo: refersToEntities(s,e1,null,false,false) devolver� true
	ante "lanzar la piedra a la l�mpara" si e1 es la piedra.
	*/
	
	
	public static boolean refersToEntity ( String s , Entity e , boolean plur_or_sing )
	{
		if ( e == null ) return true;
		else return ( e.matchesCommand ( s , plur_or_sing ) > 0 );
	}

	public static Vector refersToEntityIn ( String s , EntityList el , boolean plur_or_sing )
	{
		return ( el.patternMatch( s , plur_or_sing ) );
	}
	
	public static Vector refersToEntityInRecursive ( String s , EntityList el , boolean plur_or_sing )
	{
		return ( el.patternMatchWithRecursion( s , plur_or_sing ) );
	}
	
	public static Vector[] refersToTwoEntitiesInRecursive ( String s , EntityList el , boolean plur_or_sing , boolean plur_or_sing_2 )
	{
		return ( el.patternMatchTwoWithRecursion( s , plur_or_sing , plur_or_sing_2 ) );
	}

	//order matters as far as this one is concerned
	public static boolean refersToEntities ( String s , Entity e1 , Entity e2 , boolean plur1 , boolean plur2 )
	{		
			//El procedimiento es dividir el string en todos los pares de strings posibles
			//atendiendo a sus tokens, y hacer patternmatching en cada uno, del mismo modo
			//que en Inventory::patternMatchTwo (explicado ah�) s�lo que no queremos buscar
			//los objetos con los que la frase hace patternmatching, sino saber si lo hace 
			//con unos concretos.

			int ntokens = StringMethods.numToks ( s , ' ' );
			
			//probar todas las posibles divisiones en tokens
				for ( int punto_division = 1 ; punto_division < ntokens ; punto_division++ )
				{
					String parte1 = StringMethods.getToks ( s , 1 , punto_division , ' ' );
					String parte2 = StringMethods.getToks ( s , punto_division+1 , ntokens , ' ' );
					
					boolean primeraentidad = (e1==null)?true:refersToEntity(parte1,e1,plur1);
					boolean segundaentidad = (e2==null)?true:refersToEntity(parte2,e2,plur2);
					
					if ( primeraentidad && segundaentidad ) return true;
					
				}
			
			//no encontrado pattern-matching con ambos objetos en orden
				return false;
				
	}
	
	//order matters as far as this one is concerned
	//devuelve array de dos vectores de entidades matcheadas, o vectores vac�os
	public static Vector[] refersToEntitiesIn ( String s , EntityList el1 , EntityList el2 , boolean plur1 , boolean plur2 )
	{		
			//�dem que el anterior; pero con listas de entidades

			Vector[] resultado = new Vector[2];

			int ntokens = StringMethods.numToks ( s , ' ' );
			
			//probar todas las posibles divisiones en tokens
				for ( int punto_division = 1 ; punto_division < ntokens ; punto_division++ )
				{
					String parte1 = StringMethods.getToks ( s , 1 , punto_division , ' ' );
					String parte2 = StringMethods.getToks ( s , punto_division+1 , ntokens , ' ' );
					
					Vector primeraentidad = refersToEntityIn(parte1,el1,plur1);
					Vector segundaentidad = refersToEntityIn(parte2,el2,plur2);
					
					if ( primeraentidad.size() > 0 && segundaentidad.size() > 0 ) 
					{
						resultado[0] = primeraentidad;
						resultado[1] = segundaentidad;
						return resultado;
					}
					
				}
			
			//no encontrado pattern-matching con ambos objetos en orden
				resultado[0] = new Vector();
				resultado[1] = new Vector();
				return resultado;
				
	}
	
	
	
	
	
	
	public static List /*of SentenceInfo*/ parseReferencesToEntitiesIn ( String s , EntityList el1 , EntityList el2 , boolean plur1 , boolean plur2 )
	{		
			//devolviendo linguistic info

			List resultado = new ArrayList(); //of SentenceInfo

			int ntokens = StringMethods.numToks ( s , ' ' );
			
			//probar todas las posibles divisiones en tokens
				for ( int punto_division = 1 ; punto_division < ntokens ; punto_division++ )
				{
					String parte1 = StringMethods.getToks ( s , 1 , punto_division , ' ' );
					String parte2 = StringMethods.getToks ( s , punto_division+1 , ntokens , ' ' );
					
					Vector primeraentidad = refersToEntityIn(parte1,el1,plur1);
					Vector segundaentidad = refersToEntityIn(parte2,el2,plur2);
					
					if ( primeraentidad.size() > 0 && segundaentidad.size() > 0 ) 
					{
						for ( int i = 0 ; i < primeraentidad.size() ; i++ )
						{
							for ( int j = 0 ; j < segundaentidad.size() ; j++ )
							{
								Entity firstEntity = (Entity) primeraentidad.get(i);
								Entity secondEntity = (Entity) segundaentidad.get(j);
								SentenceInfo si = new SentenceInfo();
								si.setArgs1(parte1);
								si.setArgs2(parte2);
								si.setObj1(firstEntity);
								si.setObj2(secondEntity);
								resultado.add(si);
							}
						}
					}
					
				}
			
			return resultado;
				
	}
	
	
	//this should now be working.
	//the "recursive" bit refers to the fact that it parses references to entities in the contents of an entity,
	//in the contents of the contents, etc.
	public static List /*of SentenceInfo*/ parseReferencesToEntitiesInRecursive ( String s , EntityList el1 , EntityList el2 , boolean plur1 , boolean plur2 )
	{		
			//devolviendo linguistic info

			List resultado = new ArrayList(); //of SentenceInfo

			int ntokens = StringMethods.numToks ( s , ' ' );
			
			//probar todas las posibles divisiones en tokens
				for ( int punto_division = 1 ; punto_division < ntokens ; punto_division++ )
				{
					String parte1 = StringMethods.getToks ( s , 1 , punto_division , ' ' );
					String parte2 = StringMethods.getToks ( s , punto_division+1 , ntokens , ' ' );
					
					Vector primeraentidad = refersToEntityInRecursive(parte1,el1,plur1);
					Vector segundaentidad = refersToEntityInRecursive(parte2,el2,plur2);
					
					if ( primeraentidad.size() > 0 && segundaentidad.size() > 0 ) 
					{
						for ( int i = 0 ; i < primeraentidad.size() ; i++ )
						{
							for ( int j = 0 ; j < segundaentidad.size() ; j++ )
							{
							    Vector firstPath = (Vector) primeraentidad.get(i);
							    Vector secondPath = (Vector) segundaentidad.get(j);
							    Entity firstEntity = (Entity) firstPath.get(0);
							    Entity secondEntity = (Entity) secondPath.get(0);
							    SentenceInfo si = new SentenceInfo();
							    si.setArgs1(parte1);
							    si.setArgs2(parte2);
							    si.setObj1(firstEntity);
							    si.setObj2(secondEntity);
							    si.setPath1(firstPath);
							    si.setPath2(secondPath);
							    resultado.add(si);
							}
						}
					}
					
				}
			
			return resultado;
				
	}
	
	



	



}