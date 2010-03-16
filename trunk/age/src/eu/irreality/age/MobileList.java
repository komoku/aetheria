/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import eu.irreality.age.debug.Debug;

public class MobileList extends EntityList
{
	//es como Inventory sólo que con Mobiles y sin peso y volumen.
	//¿Unificar más tarde? (yep, hecho, EntityList)

	/**
	* La función addElement de java.util.Vector.
	*
	*/	
	public void addElement ( Mobile o )
	{
		laLista.addElement ( o );
	}
	
	
	public void addMobile ( Mobile m )
	{
			addElement(m);	
	}
	
	
	/**
	* La función removeElement de java.util.Vector.
	*
	*/	
	public boolean removeElement ( Mobile o )
	{
		return laLista.removeElement ( o );
	}
	
	public int size ( )
	{
		return laLista.size ( );
	}
	
	
	
	public String toString ( )
	{
		return toString ( null );
	}
	
	public String toString ( Entity viewer )
	{
		/*luego hay que hacer el constructname con mas items si hay duplicados.*/
		int i = 0;
		String cadena = "";
		
		boolean [] considerados = new boolean [ size ( ) ];
		boolean vacio = true; //si el inventario esta vacio a efectos de 
		//descripcion (no tiene nada o no tiene nada visible/con descripcion en ese estado)
		int nconsiderados = 0; /*numero de items que hemos considerado ya*/
		for ( int j = 0 ; j < size() ; j++ ) considerados[j] = false;
		
		//primero, descartamos los invisibles en este estado
		while ( i < size() )
		{
			/*para cada item...*/
			int numeroitems = 1;
			/*buscamos sus duplicados...*/
			for ( int k = i+1 ; k < size() ; k++ )
			{
				if ( elementAt(k).isSame( elementAt(i) ) )
				{
					numeroitems++;
				}
			}
			//if ( StringMethods.numToks ( elementAt(i).constructName(1,elementAt(i).getState()) , ' ' ) < 2 )
			if ( elementAt(i).isInvisible( viewer ) )
			{
					//y si no tiene nombre, es decir, es invisible en este estado, lo tachamos
					considerados[i] = true;
					nconsiderados++;
			}
			i++;
		}
		
		
		i = 0;
		int j = 0;

		
		//ahora los visibles, si los hay
		
		while ( i < size() )
		{
			/*si ya esta considerado (era invisible), pasamos*/
			if ( ! considerados[i] )
			{
				considerados[i]=true;				
				nconsiderados++;
								
				int numeroitems = 1;
				/*buscamos sus duplicados*/
				for ( int k = i+1 ; k < size() ; k++ )
				{
				
				/*
					Debug.println("-------");
					Debug.println(elementAt(k).getID() + " " + elementAt(i).getID() );
					Debug.println("Result: " + elementAt(k).isSame( elementAt(i) ) );
					Debug.println("-------");
				*/
					if ( elementAt(k).isSame( elementAt(i) ) )
					{
						considerados[k] = true;
						nconsiderados++;
						numeroitems++;
					}
				}
		
				//if ( StringMethods.numToks ( elementAt(i).constructName(numeroitems,elementAt(i).getState()) , ' ' ) < 2 )
				if ( elementAt(i).isInvisible( viewer ) )
				{
					//si no tienen nombre, es decir, son invisibles en este estado.
					i++; 
					continue;
				}
		
				vacio = false; //agregamos algo a la cadena
		
				if ( j == 0 )
					;
				else if ( nconsiderados < size() )
					cadena += ", ";
				
				else		
					cadena += " y ";	
				cadena += elementAt(i).constructName(numeroitems,viewer);
				
				j++; //j sólo se incrementa con los que se muestran en el string
			}
			i++;
		}
		if ( vacio ) return "nada.";
		else return cadena+".";
	}
	
	
	
	
	
	/*
	
	public String toString ( )
	{
		//luego hay que hacer el constructname con mas items si hay duplicados.
		int i = 0;
		String cadena = "";
		
		boolean [] considerados = new boolean [ size ( ) ];
		int nconsiderados = 0; //numero de items que hemos considerado ya
		for ( int j = 0 ; j < size() ; j++ ) considerados[j] = false;
		
		while ( i < size() )
		{
			//si ya esta considerado, pasamos
			if ( ! considerados[i] )
			{
				considerados[i]=true;
				nconsiderados++;
				int numeroitems = 1;
				//buscamos sus duplicados
				for ( int k = i+1 ; k < size() ; k++ )
				{
					if ( elementAt(k).isSame( elementAt(i) ) )
					{
						considerados[k] = true;
						nconsiderados++;
						numeroitems++;
					}
				}
		
		
				if ( i == 0 )
					;
				else if ( nconsiderados == size() - 1 )
					cadena += ", ";
				
				else		
					cadena += " y ";	
				cadena += elementAt(i).constructName(numeroitems,elementAt(i).getState());
			}
			i++;
		}
		if ( i == 0 ) return "nada.";
		else return cadena+".";
	}
	
	*/
	
	public MobileList (  )
	{
		laLista = new java.util.Vector (  );
	}
	
	public MobileList ( int initSize )
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
	
	public Mobile elementAt ( int i )
	{
		return (Mobile) laLista.elementAt ( i );
	}
	
	public void setElementAt ( Mobile nuevo, int i ) 
	{
		laLista.setElementAt ( nuevo , i );
	}	
		
	public boolean contains ( Mobile m )
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
	
		org.w3c.dom.Element suElemento = doc.createElement( "MobileList" );
		
		for ( int i = 0 ; i < size() ; i++ )
		{
			org.w3c.dom.Element nuevoElemento = doc.createElement ( "MobRef" );
			Mobile nuestroMob = (Mobile) laLista.elementAt(i);
			nuevoElemento.setAttribute ( "id" , String.valueOf ( nuestroMob.getID() ) );
			suElemento.appendChild(nuevoElemento);
		}
		
		return suElemento;
		
	}
	
	
	
	//PRECD: Los mobs referenciados ya han sido creados
	//(es decir, se puede y debe usar como carga diferida)
	public MobileList ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "MobileList node not Element" ) );
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
					//throw ( new XMLtoWorldException ( "MobileList node's child not Element" ) );
					continue;
					//2007-02-08: Parsing made less strict. This was done before already, but the repository was down.
				}
				else
				{
					org.w3c.dom.Element h = (org.w3c.dom.Element)hijo;
					
					Debug.println("Mob List Element: " + hijo);
					Debug.println("Its Mobile: "  + mundo.getMob (  h.getAttribute("id") ) );
					
					//try
					//{
						addMobile ( mundo.getMob (  h.getAttribute("id") ) );
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
	
	
	//SHALLOW copy
	public Object clone()
	{

		MobileList inv = new MobileList( );
		inv.laLista = new java.util.Vector ( laLista.size() );
		for ( int i = 0 ; i < laLista.size() ; i++ )
		{

				inv.addMobile((Mobile)laLista.get(i));


		}
		
		
		return inv;
		
	}
	

}
