/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import eu.irreality.age.debug.Debug;
	public class PropertyEntry
	{
		private String propertyName;
		private String propertyValue;
		private Object propertyValueObject = null; //una property también puede tener como valor un objeto. [problema: guardarlo]
		private long timeUnitsLeft;
	
	
		public Object clone()
		{
			PropertyEntry pe = new PropertyEntry(propertyName,propertyValue,timeUnitsLeft);
			pe.propertyValueObject = propertyValueObject;
			
			Debug.println("HAVE CLONED PROP: " + propertyName + "=" + propertyValue);
			
			return pe;
		}
	
		public String getName()
		{
			return propertyName;
		}
		public String getValue()
		{
			return propertyValue;
		}
		public long getTimeLeft()
		{
			return timeUnitsLeft;
		}
		public boolean getValueAsBoolean()
		{
			if ( propertyValue == null | propertyValue.equals("") ) return false;
			if ( propertyValue.equalsIgnoreCase("true") || propertyValue.equals("1") || propertyValue.equals("yes") )
				return true;
			else
				return false;	
		}
		public int getValueAsInteger()
		{
			try
			{
				return Integer.valueOf ( propertyValue ).intValue();
			}
			catch ( NumberFormatException nfe )
			{
				return 0;
			}
		}
		public double getValueAsDouble()
		{
			try
			{
				return Double.valueOf ( propertyValue ).doubleValue();
			}
			catch ( NumberFormatException nfe )
			{
				return 0;
			}
		}
		public float getValueAsFloat()
		{
			try
			{
				return Float.valueOf ( propertyValue ).floatValue();
			}
			catch ( NumberFormatException nfe )
			{
				return 0;
			}
		}
		
		public Object getValueAsWrapper()
		{
			if ( "true".equals(propertyValue) ) return new Boolean(true);
			if ( "false".equals(propertyValue) ) return new Boolean(false);
			try
			{
				return Integer.valueOf ( propertyValue );
			}
			catch ( NumberFormatException nfe )
			{
				;
			}
			try
			{
				return Double.valueOf ( propertyValue );
			}
			catch ( NumberFormatException nfe )
			{
				;
			}
			return propertyValue;
		}
		
		
		public Object getValueAsObject()
		{
			return propertyValueObject;
		}
		public PropertyEntry ( String name , String value , long timeUnitsLeft )
		{
			propertyName = name;
			propertyValue = value;
			this.timeUnitsLeft = timeUnitsLeft;
		}
		/*
		public void setValue ( String s )
		{
			propertyValue = s;
		}
		*/
		public void setObjectValue ( Object o )
		{
			propertyValueObject = o;
		}
		public void setTime ( long l )
		{
			timeUnitsLeft = l;
		}	
		public void setValueAndTime ( String s , long l )
		{
			propertyValue = s;
			timeUnitsLeft = l;
		}
		
		/*
		public void setValue ( String s , long l )
		{
			setValueAndTime(s,l);
		}
		*/
		
		public void setValue ( boolean b , long l )
		{
			setValue(String.valueOf(b),l);
		}
		
		public void setValue ( boolean b  )
		{
			setValue(String.valueOf(b));
		}
		
		public void setValue ( int i , long l )
		{
			setValue(String.valueOf(i),l);
		}
		
		public void setValue ( int i )
		{
			setValue(String.valueOf(i));
		}

		public void setValue ( Object o )
		{
			propertyValue = (o != null ? o.toString() : "null");
			propertyValueObject = o;
		}
		
		public void setValue ( Object o , long l )
		{
			propertyValue = o.toString();
			propertyValueObject = o;
			this.setTime ( l );
		}
		
		public void decreaseTime ( )
		{
			timeUnitsLeft--;
		}
		public boolean needsUpdate ( )
		{
			return ( timeUnitsLeft <= 0 );
		}
		
		public String toString()
		{
			return ( propertyName + ":" + propertyValue + " (" + propertyValueObject + ") !" + timeUnitsLeft ); 
		}
		
		
		public org.w3c.dom.Node getXMLRepresentation ( org.w3c.dom.Document doc )
		{
			org.w3c.dom.Element suElemento = doc.createElement("PropertyEntry");
			suElemento.setAttribute("name",propertyName);
			suElemento.setAttribute("value",propertyValue);
			suElemento.setAttribute("timeUnitsLeft",String.valueOf(timeUnitsLeft));
			return suElemento;
		}
		
		public PropertyEntry ( World mundo , org.w3c.dom.Node n ) throws XMLtoWorldException
		{
		
			if ( ! ( n instanceof org.w3c.dom.Element ) )
			{
				throw ( new XMLtoWorldException ( "PropertyEntry node not Element" ) );
			}
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			
			if ( !e.hasAttribute("name") )
				throw ( new XMLtoWorldException ( "name attribute missing at PropertyEntry" ) );
			this.propertyName = e.getAttribute("name");
			
			if ( !e.hasAttribute("value") )
				throw ( new XMLtoWorldException ( "value attribute missing at PropertyEntry" ) );
			this.propertyValue = e.getAttribute("value");
			
			if ( !e.hasAttribute("timeUnitsLeft") )
				throw ( new XMLtoWorldException ( "timeUnitsLeft attribute missing at PropertyEntry" ) );
			try
			{
				this.timeUnitsLeft = Long.valueOf ( e.getAttribute("timeUnitsLeft") ) . longValue() ;
			}
			catch ( NumberFormatException nfe )
			{
				throw ( new XMLtoWorldException ( "Wrong number format at timeUnitsLeft attribute, at PropertyEntry"  ) );
			}
		
		}
		
		
	}