package eu.irreality.age.swing;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FontUtils 
{

	
	/**
	 * Applies kerning to the given font if we're in java 1.6 or later (i.e. that functionality is available)
	 * and if it is possible.
	 * @param base
	 * @return
	 */
	public static Font applyKerningAndLigaturesIfPossible ( Font base )
	{
		try
		{
			Class cl = java.awt.font.TextAttribute.class;
			
			TextAttribute kerningAttr = (TextAttribute) cl.getField("KERNING").get(null); //this will throw NoSuchField exception in java < 1.6
			Integer kerningOnVal = (Integer) cl.getField("KERNING_ON").get(null); //as above
			
			TextAttribute ligaturesAttr = (TextAttribute) cl.getField("LIGATURES").get(null); //this will throw NoSuchField exception in java < 1.6
			Integer ligaturesOnVal = (Integer) cl.getField("LIGATURES_ON").get(null); //as above
			
			Map attributes = new HashMap();
			attributes.put(kerningAttr, kerningOnVal);
			attributes.put(ligaturesAttr, ligaturesOnVal);
			//attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			return base.deriveFont(attributes);
		}
		catch ( Exception e )
		{
			/*
			 * could be because we are in java < 1.6 or because we have insufficient privileges to check
			 * (maybe also because the font doesn't support kerning, I guess... in any case, if something goes wrong
			 * we just return the original font).
			 */
			System.err.println(e);
			return base;
		}	
	}
	
	
}
