/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
public class EffectList extends EntityList
{
	//es como Inventory sólo que con Mobiles y sin peso y volumen.
	//¿Unificar más tarde? (yep, hecho, EntityList)

	/**
	* La función addElement de java.util.Vector.
	*
	*/	
	public void addElement ( Effect o )
	{
		laLista.addElement ( o );
	}
	
	
	public void addEffect ( Effect m )
	{
			addElement(m);	
	}
	
	
	/**
	* La función removeElement de java.util.Vector.
	*
	*/	
	public boolean removeElement ( Effect o )
	{
		return laLista.removeElement ( o );
	}
	
	public int size ( )
	{
		return laLista.size ( );
	}
	
	
	
	
	
	public EffectList (  )
	{
		laLista = new java.util.Vector (  );
	}
	
	public EffectList ( int initSize )
	{
		laLista = new java.util.Vector ( initSize );
	}
	
	public void incrementSize ( int increment )
	{
		if ( increment > 0 )
			laLista.setSize ( laLista.size() + increment );
	}
	
	public boolean isEmpty ( )
	{
		return laLista.isEmpty();
	}
	
	public Effect elementAt ( int i )
	{
		return (Effect) laLista.elementAt ( i );
	}
	
	public void setElementAt ( Effect nuevo, int i ) 
	{
		laLista.setElementAt ( nuevo , i );
	}	
		
	public boolean contains ( Effect m )
	{
		for ( int i = 0 ; i < size() ; i++ )
		{
			//Debug.println(laLista);
			//Debug.println("i=" + i);
			if ( elementAt(i) != null && elementAt(i).equals( m ) ) return true;
		}
		return false;
	}
	
	
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "EffectList" );
		
		for ( int i = 0 ; i < size() ; i++ )
		{
			org.w3c.dom.Element nuevoElemento = doc.createElement ( "EffectRef" );
			Effect nuestroMob = (Effect) laLista.elementAt(i);
			nuevoElemento.setAttribute ( "id" , String.valueOf ( nuestroMob.getID() ) );
			suElemento.appendChild(nuevoElemento);
		}
		
		return suElemento;
		
	}
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc , java.util.List intensities )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "EffectList" );
		
		for ( int i = 0 ; i < size() ; i++ )
		{
			org.w3c.dom.Element nuevoElemento = doc.createElement ( "EffectRef" );
			Effect nuestroMob = (Effect) laLista.elementAt(i);
			nuevoElemento.setAttribute ( "id" , String.valueOf ( nuestroMob.getID() ) );
			nuevoElemento.setAttribute ( "intensity" , String.valueOf( ((Integer)( intensities.get(i) )).intValue() ) );
			suElemento.appendChild(nuevoElemento);
		}
		
		return suElemento;
		
	}
	
	
	
	
	//PRECD: Los mobs referenciados ya han sido creados
	//(es decir, se puede y debe usar como carga diferida)
	public EffectList ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "EffectList node not Element" ) );
		}
		else
		{
			//int weight,volume,weightLimit,volumeLimit;
			//try
			//{
				org.w3c.dom.Element e = (org.w3c.dom.Element) n;
				//weight = Integer.valueOf ( e.getAttribute("weight") ) . intValue();
				//volume = Integer.valueOf ( e.getAttribute("volume") ) . intValue();
				//weightLimit = Integer.valueOf ( e.getAttribute("weightLimit") ) . intValue();
				//volumeLimit = Integer.valueOf ( e.getAttribute("volumeLimit") ) . intValue();
			//}
			//catch ( NumberFormatException nfe )
			//{
			//	throw ( new XMLtoWorldException ( "Inventory attribute value not found or invalid" ) );
			//}
			
			//Inventory inv = new Inventory ( weightLimit , volumeLimit , 0 );
			
			this.laLista = new java.util.Vector();
			//this.weightLimit = weightLimit;
			//this.volumeLimit = volumeLimit;
			//this.weight = 0;
			//this.volume = 0; //on adding items, will increase
			
			org.w3c.dom.NodeList nl = n.getChildNodes();
			
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				org.w3c.dom.Node hijo = nl.item(i);
				
				//si todo va bien, es de la forma <ItemRef id="una id"/>
				
				if ( !( hijo instanceof org.w3c.dom.Element ) )
				{
					; //throw ( new XMLtoWorldException ( "EffectList node's child not Element " + "s" + hijo + "p" + n ) );
				}
				else
				{
					org.w3c.dom.Element h = (org.w3c.dom.Element)hijo;
					
					//Debug.println("Mob List Element: " + hijo);
					//Debug.println("Its Mobile: "  + mundo.getMob (  h.getAttribute("id") ) );
					
					//try
					//{
						addEffect ( (Effect) mundo.getAbstractEntity (  h.getAttribute("id") ) );
					//}
					//catch ( WeightLimitExceededException wle )
					//{
					//	throw ( new XMLtoWorldException ( "Inventory overloaded in weight" ) );
					//}
					//catch ( VolumeLimitExceededException wle )
					//{
					//	throw ( new XMLtoWorldException ( "Inventory overloaded in volume" ) );
					//}
				}
				
			} //end for each son of inventory node
			
			//if ( this.volume != volume || this.weight != weight )
			//	throw ( new XMLtoWorldException ( "Inventory volume or weight do not match items." ) );
			
		} //end if the node is an element
				
	} //end method
	
	

}
