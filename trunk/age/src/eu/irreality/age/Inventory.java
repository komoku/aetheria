/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import eu.irreality.age.debug.Debug;

class WeightLimitExceededException extends Exception
{

}
class VolumeLimitExceededException extends Exception
{

}

public class Inventory extends EntityList
{
	
	/**Peso total.*/
	private int weight;
	
	/**Volumen total.*/
	private int volume;
	
	/**Límite de peso de este inventario (en personaje, baúl, etc.)*/
	private int weightLimit;
	/**Límite de volumen.*/
	private int volumeLimit;


	/**
	* La función addElement de java.util.Vector.
	*
	*/	
	private void addElement ( Item o )
	{
		laLista.addElement ( o );
	}
	
	/**
	* La función removeElement de java.util.Vector.
	*
	*/	
	private boolean removeElement ( Item o )
	{
		return laLista.removeElement ( o );
	}
	
	public void addItem ( Item o ) throws WeightLimitExceededException, VolumeLimitExceededException
	{
		if ( weight+o.getTotalWeight() > weightLimit )
			throw new WeightLimitExceededException ( );
		else if ( volume+o.getVolume() > volumeLimit )
			throw new VolumeLimitExceededException ( );	
		else
		{
			addElement(o);
			weight += o.getTotalWeight();
			volume += o.getVolume();
		}		
	}
	
	public boolean removeItem ( Item o )
	{
		boolean valor = removeElement(o);
		if ( valor == true )
		{
			weight -= o.getTotalWeight();
			volume -= o.getVolume();
		}
		return valor;
	}
	
	public void empty()
	{
		for ( int i = 0 ; i < size() ; i++ )
		{
			removeItem ( elementAt(i) );
		}
	}
	
	public void clear()
	{
		empty();
	}
	
	public int size ( )
	{
		return laLista.size ( );
	}
	
	public String toString ( )
	{
		return toString ( null , null );
	}
	
	public String toString ( World w )
	{
		return toString ( null , w );
	}
	
	public String toString ( /*nullable*/ Entity viewer , World w )
	{
		/*luego hay que hacer el constructname con mas items si hay duplicados.*/
		int i = 0;
		String cadena = "";
		
		String conjunction;
		if ( w == null ) conjunction="y";
		else conjunction = w.getMessages().getMessage("coord.conj");
		
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
			/*
			if ( viewer == null )
			{
				if ( StringMethods.numToks ( elementAt(i).constructName(1,elementAt(i).getState()) , ' ' ) < 2 )
				{
						//y si no tiene nombre, es decir, es invisible en este estado, lo tachamos
						considerados[i] = true;
						nconsiderados++;
						//Debug.println("Invisible item detected");
				}
			}
			else
			*/
			{
				if ( elementAt(i).isInvisible(viewer) )
				//if ( StringMethods.numToks ( elementAt(i).constructName(1,viewer) , ' ' ) < 2 )
				{
						//y si no tiene nombre, es decir, es invisible en este estado, lo tachamos
						considerados[i] = true;
						nconsiderados++;
						//Debug.println("Invisible item detected");
				}
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
					if ( elementAt(k).isIndistinguishableFrom( elementAt(i) , viewer ) )
					{
						considerados[k] = true;
						nconsiderados++;
						numeroitems++;
					}
				}
		
				/*
				if ( viewer == null )
				{
					if ( StringMethods.numToks ( elementAt(i).constructName(numeroitems,elementAt(i).getState()) , ' ' ) < 2 )
					{
						//si no tienen nombre, es decir, son invisibles en este estado.
						i++; 
						continue;
					}
				}
				else
				*/
				{
					if ( elementAt(i).isInvisible(viewer) )
					//if ( StringMethods.numToks ( elementAt(i).constructName(numeroitems,viewer) , ' ' ) < 2 )
					{
						//si no tienen nombre, es decir, son invisibles en este estado.
						i++; 
						continue;
					}
				}
		
				vacio = false; //agregamos algo a la cadena
		
				if ( j == 0 )
					;
				else if ( nconsiderados < size() )
					cadena += ", ";
				
				else		
					cadena += ( " " + conjunction + " " );	
				//if ( viewer == null )	
				//	cadena += elementAt(i).constructName(numeroitems,elementAt(i).getState());
				//else
					cadena += elementAt(i).constructName(numeroitems,viewer);
				
				j++; //j sólo se incrementa con los que se muestran en el string
			}
			i++;
		}
		if ( vacio ) return "nada.";
		else return cadena+".";
	}
	
	public Inventory ( int weightLimit , int volumeLimit )
	{
		laLista = new java.util.Vector (  );
		weight = 0; volume = 0;
		this.weightLimit = weightLimit; 
		this.volumeLimit = volumeLimit;
	}
	
	public Inventory ( int weightLimit , int volumeLimit , int initSize )
	{
		laLista = new java.util.Vector ( initSize );
		weight = 0; volume = 0;
		this.weightLimit = weightLimit; 
		this.volumeLimit = volumeLimit;
	}
	
	public void incrementSize ( int increment )
	{
		if ( increment > 0 )
			laLista.setSize ( laLista.size() + increment );
	}
	
	public int getWeightLimit ( )
	{
		return weightLimit;
	}
	
	public int getVolumeLimit ( )
	{
		return volumeLimit;
	}
	
	public void setWeightLimit ( int nuevo ) throws WeightLimitExceededException
	{
		weightLimit = nuevo;
		if ( weight > weightLimit )
			throw new WeightLimitExceededException ( );
	}
	
	public void setVolumeLimit ( int nuevo ) throws VolumeLimitExceededException
	{
		volumeLimit = nuevo;
		if ( volume > volumeLimit )
			throw new VolumeLimitExceededException ( );
	}
	
	public boolean isEmpty ( )
	{
		return laLista.isEmpty();
	}
	
	public Item elementAt ( int i )
	{
		return (Item) laLista.elementAt ( i );
	}
	
	public void setElementAt ( Item nuevo, int i ) throws WeightLimitExceededException, VolumeLimitExceededException
	{
		if ( i < size() && elementAt(i) != null )
		{
			volume -= elementAt(i).getVolume();
			weight -= elementAt(i).getTotalWeight();
		}
		volume += nuevo.getVolume();
		weight += nuevo.getTotalWeight();
		laLista.setElementAt ( nuevo , i );
		if ( volume > volumeLimit )
			throw new VolumeLimitExceededException ( );
		if ( weight > weightLimit )
			throw new WeightLimitExceededException ( );	
	}	
	
	public boolean contains ( Item it )
	{
		for ( int i = 0 ; i < size() ; i++ )
		{
			Debug.println(laLista);
			Debug.println("i=" + i);
			if ( elementAt(i) != null && elementAt(i).equals( it ) ) return true;
		}
		return false;
	}
	
	public int getWeight()
	{
		return weight;
	}
	
	public int getVolume()
	{
		return volume;
	}
	
	
	public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
	{
	
		org.w3c.dom.Element suElemento = doc.createElement( "Inventory" );
		
		suElemento.setAttribute ( "weight" , String.valueOf(weight) );
		suElemento.setAttribute ( "volume" , String.valueOf(volume) );
		suElemento.setAttribute ( "weightLimit" , String.valueOf(weightLimit) );
		suElemento.setAttribute ( "volumeLimit" , String.valueOf(volumeLimit) );
		
		for ( int i = 0 ; i < size() ; i++ )
		{
			org.w3c.dom.Element nuevoElemento = doc.createElement ( "ItemRef" );
			Item nuestroItem = (Item) laLista.elementAt(i);
			if ( nuestroItem != null ) //null puede salir en inventories como el de wielded weapons
				nuevoElemento.setAttribute ( "id" , String.valueOf ( nuestroItem.getID() ) );
			suElemento.appendChild(nuevoElemento);
		}
		
		return suElemento;
		
	}
	
	//PRECD: Los items referenciados ya han sido creados
	//(es decir, se puede y debe usar como carga diferida)
	public Inventory ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
	{
		
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "Inventory node not Element" ) );
		}
		else
		{
			int weight,volume,weightLimit,volumeLimit;
			try
			{
				org.w3c.dom.Element e = (org.w3c.dom.Element) n;
				if ( !e.hasAttribute("weight") )
					weight=0;
				else	
					weight = Integer.valueOf ( e.getAttribute("weight") ) . intValue();
				if ( !e.hasAttribute("volume") )
					volume=0;
				else	
					volume = Integer.valueOf ( e.getAttribute("volume") ) . intValue();
				if ( !e.hasAttribute("weightLimit") )
					weightLimit=50000;
				else	
					weightLimit = Integer.valueOf ( e.getAttribute("weightLimit") ) . intValue();
				if ( !e.hasAttribute("volumeLimit") )
					volumeLimit=50000;
				else	
					volumeLimit = Integer.valueOf ( e.getAttribute("volumeLimit") ) . intValue();
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException ( "Inventory attribute value not found or invalid" ) );
			}
			
			//Inventory inv = new Inventory ( weightLimit , volumeLimit , 0 );
			
			this.laLista = new java.util.Vector();
			this.weightLimit = weightLimit;
			this.volumeLimit = volumeLimit;
			this.weight = 0;
			this.volume = 0; //on adding items, will increase
			
			org.w3c.dom.NodeList nl = n.getChildNodes();
			
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				org.w3c.dom.Node hijo = nl.item(i);
				//si todo va bien, es de la forma <ItemRef id="una id"/>
				
				if ( !( hijo instanceof org.w3c.dom.Element ) )
				{
					//throw ( new XMLtoWorldException ( "Inventory node's child " + i + " not Element:" + nl.item(i) + "(" + nl.item(i).getClass() + ")" + ", next one being " + nl.item(i+1) ) );
					continue;
				}
				else
				{
					org.w3c.dom.Element h = (org.w3c.dom.Element)hijo;
					
					try
					{
						if ( h.hasAttribute("id") )
							addItem ( mundo.getItem ( h.getAttribute("id") ) );
						else
							//(add null) <- useful for wielded weapons, etc.
							incrementSize(1);
					}
					catch ( WeightLimitExceededException wle )
					{
						throw ( new XMLtoWorldException ( "Inventory overloaded in weight" ) );
					}
					catch ( VolumeLimitExceededException wle )
					{
						throw ( new XMLtoWorldException ( "Inventory overloaded in volume" ) );
					}
				}
				
			} //end for each son of inventory node
			
			if ( this.volume != volume || this.weight != weight )
				//throw ( new XMLtoWorldException ( "Inventory volume or weight do not match items: " + toString() ) );
				Debug.println("Warning: Inventory volume or weight do not match items: " + toString() );
			
		} //end if the node is an element
				
	} //end method
	


	//SHALLOW copy
	public Object clone()
	{

		Inventory inv = new Inventory( weightLimit , volumeLimit );
		inv.laLista = new java.util.Vector ( laLista.size() );
		for ( int i = 0 ; i < laLista.size() ; i++ )
		{
			try
			{
				inv.addItem((Item)laLista.get(i));
			}
			catch ( WeightLimitExceededException wlee )
			{
				;
			}
			catch ( VolumeLimitExceededException wlee )
			{
				;
			}
		}
		
		
		return inv;
		
	}
	
	/*
	Nótese que esta función añade los nuevos items al mundo.
	cloneContents: si se clona a su vez el inventario del item.
	cloneParts: ídem con las partes.
	*/
	public Inventory cloneCopyingItems ( World w , boolean cloneContents , boolean cloneParts )
	{
		//do it!
		Inventory inv = new Inventory( weightLimit , volumeLimit );
		inv.laLista = new java.util.Vector ( laLista.size() );
		for ( int i = 0 ; i < laLista.size() ; i++ )
		{
			try
			{
				Item original = (Item)laLista.get(i);
				Item copia = original.createNewInstance( w , cloneContents , cloneParts );
				w.addItemAssigningID ( copia );
				inv.addItem(copia);
			}
			catch ( WeightLimitExceededException wlee )
			{
				;
			}
			catch ( VolumeLimitExceededException wlee )
			{
				;
			}
		}
		
		return inv;
	}
	
	
	

	
	
		
	
	
	
	


} //end class
