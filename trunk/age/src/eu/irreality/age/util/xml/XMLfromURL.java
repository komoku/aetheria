package eu.irreality.age.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class XMLfromURL 
{

	/**
	 * Obtains an XML document (will be used for catalogs) from a URL.
	 * @param catalogURL
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static Document getXMLFromURL ( URL catalogURL ) throws IOException, TransformerException
	{
		if ( catalogURL == null ) throw new IOException("Null catalog URL passed");
		
		InputStream is = catalogURL.openStream();
		StreamSource s = new StreamSource(is,catalogURL.toString());
		Transformer t = TransformerFactory.newInstance().newTransformer();
		DOMResult r = new DOMResult();
		t.transform(s,r);
		return ((Document) r.getNode());
	}
	
}
