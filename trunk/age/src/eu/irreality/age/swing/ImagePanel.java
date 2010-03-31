package eu.irreality.age.swing;

import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	
	private ImageIcon theImage;
	
	public static final int NO_SCALING = 0;
	public static final int FIT_HEIGHT = 1;
	public static final int FIT_WIDTH = 2;
	public static final int FIT_BOTH = 3;
	
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
	
	public ImageIcon getImage ()
	{
		return theImage;
	}
	
	public void setImage ( ImageIcon ii )
	{
		theImage = ii;
	}
	
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
		if ( theImage == null ) return;
		
		int imageHeight = theImage.getIconHeight();
		int imageWidth = theImage.getIconWidth();
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
		g.drawImage(theImage.getImage(),drawX,drawY,drawW,drawH,null);
	}
	

}
