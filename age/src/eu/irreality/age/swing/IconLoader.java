package eu.irreality.age.swing;

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
	    URI uri = SVGCache.getSVGUniverse().loadSVG(url);
	    SVGIcon icon = new SVGIcon();
	    icon.setSvgURI(uri);
	    return icon;
	}
	else
	{
	    return new ImageIcon(url);
	}
    }
    
}
