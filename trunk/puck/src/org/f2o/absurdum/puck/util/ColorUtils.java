package org.f2o.absurdum.puck.util;

import java.awt.Color;

public class ColorUtils 
{

	//pasa strings de tipo "000000" o "#000000" a color
	//null si no reconocido
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean;
			if ( colorString.length() > 0 && colorString.charAt(0) == '#' )
				colorClean = colorString.substring(1);
				else colorClean = colorString;
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}

	public static String colorToString ( Color color )
	{
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		String s1 = Integer.toString(red,16);
		if ( s1.length() == 1 ) s1 = "0" + s1;
		String s2 = Integer.toString(green,16);
		if ( s2.length() == 1 ) s2 = "0" + s2;
		String s3 = Integer.toString(blue,16);
		if ( s3.length() == 1 ) s3 = "0" + s3;
		return s1 + s2 + s3;
	}

}
