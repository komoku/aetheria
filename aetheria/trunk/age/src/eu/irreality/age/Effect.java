/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import eu.irreality.age.debug.Debug;


public class Effect extends AbstractEntity
{



	public Effect ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		constructAbstractEntity ( mundo , n , true , "effect" );
	}


	public void readEffectSpecifics ( World mundo , org.w3c.dom.Node n ) //throws XMLtoWorldException
	{
		;
	}
	
	
	//se supone que se castea bien (ya se cobró el maná, etc.)
	public void enable ( Entity caster , Entity target , int intensity , int duration )
	{
		if ( duration == 0 )
		{
			Debug.println("Zero duration.");
			cast ( caster , target , intensity );
		}
		else
		{
			Debug.println("Duration is " + duration);
			cast ( caster , target , intensity );

		
			//try to set target to caster's room if no target is set
			//this is done for the "cast" relationship to work with some Entity,
			//even when there is no target
			
			if ( target == null )
				target = ((Mobile)caster).getRoom();
		


			if ( target != null )
			{
				target.setRelationshipProperty ( this , "cast" , true );
				this.setRelationshipProperty ( target , "cast" , true );
				target.setRelationshipPropertyTimeLeft ( this , "cast" , duration );
				this.setRelationshipPropertyTimeLeft ( target , "cast" , duration );
			}
		}	
	}
	public void disable ( Entity target )
	{
	
	
				
		//try to set target to caster's room if no target is set
		//this is done for the "cast" relationship to work with some Entity,
		//even when there is no target
		
		//nah, above doesn't make sense: target won't be null, guaranteed in enable
			
		//if ( target == null )
		//	target = ((Mobile)caster).getRoom();
	
		if ( target != null )
		{
			target.setRelationshipProperty ( this , "cast" , false );
			this.setRelationshipProperty ( target , "cast" , false );
		}
		fade (  target );
	}
	
	public boolean cast ( Entity caster , Entity target , int intensity )
	{
		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "cast" , new Object[] { caster , target , new Integer(intensity) } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at cast routine" );
			;
		}
		return ejecutado;
	}
	
	public boolean fade ( Entity target  )
	{
		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "fade" , new Object[] { target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fade routine" );
			;
		}
		return ejecutado;
	}
	
	public boolean fail ( Entity caster , Entity target , int intensity  )
	{
		boolean ejecutado = false;
		try
		{
			ejecutado = execCode( "fail" , new Object[] { caster , target  } );
		}
		catch (bsh.TargetError bshte)
		{
			//escribir("bsh.TargetError found at fail routine" );
			;
		}
		return ejecutado;
	}
	
	
	
	
	
	public /*abstract*/ boolean updateRelationship ( Entity e , PropertyEntry pe , World mundo )
	{
	
		if ( pe.getName().equalsIgnoreCase("cast") && pe.getValueAsBoolean() )
		{
			//spell/effect fade
			disable(e);
		}
	
		return true;
	}
	
	
	
	

}
