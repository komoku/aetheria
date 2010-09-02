package eu.irreality.age.swing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.app.beans.SVGIcon;

public class IconLoader
{

    public static Icon loadIcon ( java.net.URL url )
    {
		if ( url.toString().toLowerCase().endsWith("svg") )
		{
			//try
			//{
				URI uri = SVGCache.getSVGUniverse().loadSVG(url);
				SVGIcon icon = new SVGIcon();
				icon.setSvgURI(uri);
				return icon;
			//}
			//catch ( Exception e ) //it doesn't throw exceptions
			//{
			//	System.err.println("Warning, SVG not found or could not be read: " + url);
			//	return null;
			//}
		}
		else
		{
			ImageIcon ii = new ImageIcon(url);
			if ( ii == null )
				System.err.println("Warning, image not found or could not be read: " + url);
		    return ii;
		}
    }
    
}
