/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import java.util.*;
import java.awt.*;

import eu.irreality.age.debug.Debug;
import eu.irreality.age.filemanagement.URLUtils;

public class VisualConfiguration
{


	//text colors in color-code format
	private Hashtable colorCodesTable = new Hashtable();
	/*
	private String descriptionColor;
	private String importantColor;
	private String infoColor;
	private String actionColor;
	private String lifeErrorColor;
	private String gameErrorColor;
	private String storyColor;
	private String defaultColor;
	private String inputColor;
	*/
	
	//foreground/background colors in AWT Color format
	private Color background;
	private Color foreground;
	
	//font-loading data
	private String fontName;
	private float fontSize;
	private String fontFileName;
	
	//font
	private Font laFuente;

	public VisualConfiguration()
	{
		//crea la configuracion por defecto
		colorCodesTable.put("description","%00CC00%");
		colorCodesTable.put("important","%FFFF00%");
		colorCodesTable.put("information","%FFFF00%");
		colorCodesTable.put("action","%9999FF%");
		colorCodesTable.put("denial","%CC0000%");
		colorCodesTable.put("error","%FF0000%");
		colorCodesTable.put("story","%FFFFFF%");
		colorCodesTable.put("default","%FFFFFF%");
		colorCodesTable.put("input","%AAAAAA%");
		colorCodesTable.put("reset","% %");
		background = Color.black;
		foreground = Color.white;
		laFuente = SwingAetheriaGameLoaderInterface.font;
	}
	
	public String getColorCode ( String colorType )
	{
		String code = (String) colorCodesTable.get(colorType.toLowerCase());
		if ( code == null ) return "";
		else return code;
	}
	
	
	public Enumeration getColorKeys()
	{
		return colorCodesTable.keys();	
	}


	public org.w3c.dom.Element getXMLRepresentation( org.w3c.dom.Document doc )
	{
		
		org.w3c.dom.Element suElemento = doc.createElement( "VisualConfiguration" );
		
		org.w3c.dom.Element colorsElt = doc.createElement("Colors");
		
		Enumeration en = colorCodesTable.keys();
		while ( en.hasMoreElements() )
		{
			String key = (String) en.nextElement();
			org.w3c.dom.Element eltCol = doc.createElement(Character.toUpperCase(key.charAt(0)) + key.substring(1));
			eltCol.setAttribute("color",getColorCode(key));
			colorsElt.appendChild(eltCol);
		}
		
		/*
		org.w3c.dom.Element desCol = doc.createElement("Description");
		org.w3c.dom.Element impCol = doc.createElement("Important");
		org.w3c.dom.Element actCol = doc.createElement("Action");
		org.w3c.dom.Element infCol = doc.createElement("Information");
		org.w3c.dom.Element denCol = doc.createElement("Denial");
		org.w3c.dom.Element errCol = doc.createElement("Error");
		org.w3c.dom.Element stoCol = doc.createElement("Story");
		org.w3c.dom.Element defCol = doc.createElement("Default");
		org.w3c.dom.Element inpCol = doc.createElement("Input");
		*/
		org.w3c.dom.Element forCol = doc.createElement("Foreground");
		org.w3c.dom.Element bacCol = doc.createElement("Background");
		/*
		desCol.setAttribute("color",descriptionColor);
		impCol.setAttribute("color",importantColor);
		actCol.setAttribute("color",actionColor);
		infCol.setAttribute("color",infoColor);
		denCol.setAttribute("color",lifeErrorColor);
		errCol.setAttribute("color",gameErrorColor);
		stoCol.setAttribute("color",storyColor);
		defCol.setAttribute("color",defaultColor);
		inpCol.setAttribute("color",inputColor);
		*/
		forCol.setAttribute("color",ColoredSwingClient.colorToString(foreground));
		bacCol.setAttribute("color",ColoredSwingClient.colorToString(background));

		/*
		colorsElt.appendChild(desCol);
		colorsElt.appendChild(impCol);
		colorsElt.appendChild(actCol);
		colorsElt.appendChild(infCol);
		colorsElt.appendChild(denCol);
		colorsElt.appendChild(errCol);
		colorsElt.appendChild(stoCol);
		colorsElt.appendChild(defCol);
		colorsElt.appendChild(inpCol);
		*/
		colorsElt.appendChild(forCol);
		colorsElt.appendChild(bacCol);

		org.w3c.dom.Element fontElt = doc.createElement("Font");
		
		if ( fontName != null )
		{
	
			fontElt.setAttribute("name",fontName);
			fontElt.setAttribute("size",String.valueOf(fontSize));
			if ( fontFileName != null )
				fontElt.setAttribute("filename",fontFileName);
		
		}
		
		suElemento.appendChild ( colorsElt );
		
		if ( fontName != null )
			suElemento.appendChild ( fontElt );
		
		return suElemento;

	}


	/**
	* Toma la información de configuración del documento XML.
	* Fontdir puede ser null (busca en el directorio actual)
	*
	*/
	public VisualConfiguration ( org.w3c.dom.Node n , String fontDir ) throws XMLtoWorldException
	{
	
		this(); //Set all to defaults. Will remain that way if Elements empty, not present.
		
		if ( ! ( n instanceof org.w3c.dom.Element ) )
		{
			throw ( new XMLtoWorldException ( "VisualConfiguration node not Element" ) );
		}
		
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;

		if ( ! e.getTagName().equalsIgnoreCase("VisualConfiguration") )
		{
			throw ( new XMLtoWorldException ( "Element not named VisualConfiguration as expected" ) );
		}
		
		org.w3c.dom.NodeList nl = e.getElementsByTagName ( "Colors" );
		
		if ( nl.getLength() > 0 )
		{
			
			org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
			
				org.w3c.dom.Element elt1;
				org.w3c.dom.NodeList nl1 = el.getElementsByTagName("Description");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
						{
							if (elt1.getAttribute("color").charAt(0)=='%')
								colorCodesTable.put("description",elt1.getAttribute("color"));
							else
								colorCodesTable.put("description","%"+elt1.getAttribute("color")+"%");
						}
				}
				
				nl1 = el.getElementsByTagName("Important");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("important",elt1.getAttribute("color"));
						else
							colorCodesTable.put("important","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Action");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("action",elt1.getAttribute("color"));
						else
							colorCodesTable.put("action","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Information");
				if ( nl1.getLength() > 0 )
				{
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("information",elt1.getAttribute("color"));
						else
							colorCodesTable.put("information","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Denial");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("denial",elt1.getAttribute("color"));
						else
							colorCodesTable.put("denial","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Error");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("error",elt1.getAttribute("color"));
						else
							colorCodesTable.put("error","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Story");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("story",elt1.getAttribute("color"));
						else
							colorCodesTable.put("story","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Default");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						if (elt1.getAttribute("color").charAt(0)=='%')
							colorCodesTable.put("default",elt1.getAttribute("color"));
						else
							colorCodesTable.put("default","%"+elt1.getAttribute("color")+"%");
					}
				}
				
				nl1 = el.getElementsByTagName("Input");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						Debug.println("Element:"+elt1);
						if ( elt1.hasAttribute("color") ) 
						{
							if (elt1.getAttribute("color").charAt(0)=='%')
								colorCodesTable.put("input",elt1.getAttribute("color"));
							else
								colorCodesTable.put("input","%"+elt1.getAttribute("color")+"%");
						}
						Debug.println("Visconf init with input color" + colorCodesTable.get("input"));
					}
				}
				
				nl1 = el.getElementsByTagName("Foreground");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{		
						Color c = stringToColor ( elt1.getAttribute("color" ) );
						if ( c!=null ) foreground = c;
					}
				}
				
				nl1 = el.getElementsByTagName("Background");
				if ( nl1.getLength() > 0 ) 
				{ 
					elt1 = (org.w3c.dom.Element) nl1.item(0);
					if ( elt1.hasAttribute("color") ) 
					{
						Color c = stringToColor ( elt1.getAttribute("color" ) );
						if ( c!=null ) background = c;
					}
				}
			
		}
		
		nl = e.getElementsByTagName ( "Font" );
		
		/*Funcionamiento de la carga de fuentes:
			Si hay un atributo filename=".." y se encuentra el fichero, la fuente cargada es
			ésa con tamaño dado en el atributo size=".." (o el tamaño por defecto)
			De lo contrario, se escoge la fuente instalada en el sistema de nombre dada por
			el atributo name=".."
			Y si no, pues nada. Por defecto habemus.
		*/
		
		boolean usingDefaultFont = true; //if we haven't set a specific font but revert to default
		
		if ( nl.getLength() > 0 )
		{
			
			/*String*/ fontName = "Courier New";
			/*float*/ fontSize = (float) 12.0;
		
			org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
			
			
			if ( el.hasAttribute("name") )
				fontName = el.getAttribute("name");
			
			if ( el.hasAttribute("size") )
				fontSize = Float.valueOf( el.getAttribute("size") ).floatValue();	
			
			Font[] fuentes = null;
			try
			{
				fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			}
			catch ( AWTError err )
			{
				//may have no AWT
				System.err.println("Warning: couldn't get fonts from local graphics environment");
				return;
			}
			
			Font fuenteElegida;
			for ( int f = 0 ; f < fuentes.length ; f++ )
			{
				//Debug.println(fuentes[f].getFontName());
				if ( fuentes[f].getFontName().equalsIgnoreCase(fontName) )
				{
					laFuente = fuentes[f].deriveFont((float)fontSize);
					usingDefaultFont = false;
					break;
				}
			}
			
			if ( el.hasAttribute("filename") )
			{
				try
				{
					fontFileName = el.getAttribute("filename");
					Debug.println("Font filename: " + fontFileName);
					Debug.println("Font directory: " + fontDir);
					//java.io.File f;
					String f;
					if ( fontDir != null ) 
					{
						f = fontDir + fontFileName;
						//f = new java.io.File ( fontDir + fontFileName );
					}
					else
					{
						f = fontFileName;
						//f = new java.io.File ( fontFileName );
					}
					java.io.InputStream is = //new java.io.FileInputStream ( f );
						URLUtils.openFileOrURL(f);
					Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
					laFuente = fuente.deriveFont((float)fontSize);
					usingDefaultFont = false;
				}
				catch ( Exception ex )
				{
					Debug.println(ex);
				}
			}
			
			if ( usingDefaultFont && el.hasAttribute("size") && laFuente != null ) //at least set the size
				laFuente = laFuente.deriveFont((float)fontSize);
			

		}
		
	}	
		




	//gets a Color from a #HHHHHH, HHHHHH, %#HHHHHH% or %HHHHHH% format string
	//null if string unrecognized
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean = colorString;
			if ( colorClean.charAt(0) == '%')
				colorClean = colorClean.substring(1,colorClean.length()-1);
			if ( colorClean.length() > 0 && colorClean.charAt(0) == '#' )
				colorClean = colorClean.substring(1);
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}


	
	public Color getBackgroundColor()
	{
		return background;
	}
	
	public Color getForegroundColor()
	{
		return this.foreground;
	}
	
	public Font getFont()
	{
		return laFuente;
	}
	
	


}
