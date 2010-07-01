package eu.irreality.age.swing;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.kitfox.svg.app.beans.SVGIcon;

import eu.irreality.age.ImageConstants;

public class ImagePanel extends JPanel implements ImageConstants
{
	
	private ImageIcon theRasterImage;
	private SVGIcon theVectorImage;
	
	private int scalingMode = NO_SCALING;
		
	public ImagePanel()
	{
		super();
		setBorder(BorderFactory.createEmptyBorder());
	}
	
	public int getScalingMode()
	{
		return scalingMode;
	}
	
	public void setScalingMode ( int scalingMode )
	{
		this.scalingMode = scalingMode;
	}
	
	public ImageIcon getRasterImage ()
	{
		return theRasterImage;
	}
	
	public SVGIcon getVectorImage ()
	{
		return theVectorImage;
	}
	
	public Icon getImage()
	{
	    if ( theRasterImage != null ) return theRasterImage;
	    else return theVectorImage;
	}
	
	public void setRasterImage ( ImageIcon ii )
	{
	    theRasterImage = ii;
	}
	
	public void setVectorImage ( SVGIcon si )
	{
	    theVectorImage = si;
	}
	
	public void setImage ( Icon ic ) throws UnsupportedOperationException
	{
	    if ( !(ic instanceof ImageIcon) && !(ic instanceof SVGIcon) )
		throw new UnsupportedOperationException("setImage only supports ImageIcon or SVGIcon");
	    else if ( ic instanceof ImageIcon )
		setRasterImage((ImageIcon)ic);
	    else if ( ic instanceof SVGIcon )
		setVectorImage((SVGIcon)ic);
	}
	
	
	private void paintVectorImage ( Graphics g )
	{	    
	    theVectorImage.setScaleToFit(true);
	    //theVectorImage.setPreferredSize(new Dimension(200,200));
	    theVectorImage.paintIcon(this, g, 0, 0);    
	}
	
	private void paintRasterImage( Graphics g )
	{
	    	
	    	int imageHeight = theRasterImage.getIconHeight();
	    	int imageWidth = theRasterImage.getIconWidth();
		int panelHeight = this.getHeight();
		int panelWidth = this.getWidth();
		
		int drawX = 0;
		int drawY = 0;
		int drawW = panelWidth;
		int drawH = panelHeight;
		
		if ( scalingMode == NO_SCALING )
		{
			drawX = panelWidth/2 - imageWidth/2;
			drawY = panelHeight/2 - imageHeight/2;
			drawW = imageWidth;
			drawH = imageHeight;
		}
		if ( scalingMode == FIT_WIDTH )
		{
			drawX = 0;
			drawW = panelWidth;
			drawH = (int) ( panelWidth * ( (double) imageHeight / (double) imageWidth ) );
			drawY = panelHeight/2 - drawH/2;
		}
		if ( scalingMode == FIT_HEIGHT )
		{
			drawY = 0;
			drawH = panelHeight;
			drawW = (int) ( panelHeight * ( (double) imageWidth / (double) imageHeight ) );
			drawX = panelWidth/2 - drawW/2;
		}
		//if scalingMode is FIT_BOTH, default.
		g.drawImage(theRasterImage.getImage(),drawX,drawY,drawW,drawH,this);
	}
	
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
		if ( theRasterImage != null ) paintRasterImage(g);
		if ( theVectorImage != null ) paintVectorImage(g);
		
	}
	

}
