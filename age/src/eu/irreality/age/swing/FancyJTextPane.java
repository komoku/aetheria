/*
 * (c) 2000-2010 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;

import com.kitfox.svg.app.beans.SVGIcon;

public class FancyJTextPane extends JTextPane
{

		private ImageIcon rasterBackgroundImage;
		private SVGIcon vectorBackgroundImage;
	
		public ImageIcon getRasterBackgroundImage() { return rasterBackgroundImage; }
		public SVGIcon getVectorBackgroundImage() { return vectorBackgroundImage; }
		
		public void setRasterBackgroundImage(ImageIcon i) 
		{ 
			this.rasterBackgroundImage = i; 
			if ( rasterBackgroundImage != null || vectorBackgroundImage != null ) 
				setOpaque(false);
			else
				setOpaque(true);
		    //repaint so that change takes place
		    javax.swing.SwingUtilities.invokeLater(new Runnable()
		    {
		        public void run()
		        {
		            repaint();
		        }
		    });
		}
		
		public void setVectorBackgroundImage(SVGIcon i) 
		{ 
			this.vectorBackgroundImage = i; 
			if ( vectorBackgroundImage != null || rasterBackgroundImage != null ) 
				setOpaque(false);
			else
				setOpaque(true);
		    //repaint so that change takes place
		    javax.swing.SwingUtilities.invokeLater(new Runnable()
		    {
		        public void run()
		        {
		            repaint();
		        }
		    });
		}
		
		
		public void setBackgroundImage(Icon ic) 
		{ 
			if ( ic == null )
			{
				if ( vectorBackgroundImage != null ) setVectorBackgroundImage(null);
				if ( rasterBackgroundImage != null ) setRasterBackgroundImage(null);
				return;
			}
		    if ( !(ic instanceof ImageIcon) && !(ic instanceof SVGIcon) )
			throw new UnsupportedOperationException("setBackgroundImage only supports ImageIcon or SVGIcon");
		    else if ( ic instanceof ImageIcon )
			{
		    	vectorBackgroundImage = null;
		    	setRasterBackgroundImage((ImageIcon)ic);
		    }
		    else if ( ic instanceof SVGIcon )
		    {
		    	rasterBackgroundImage = null;
		    	setVectorBackgroundImage((SVGIcon)ic);
		    }
		}

		public FancyJTextPane()
		{
			super();
			//setMargin(new Insets(80,80,80,80));
			//setOpaque(false);
			setDocument ( new FancyStyledDocument() );
			setBackground(new Color(0,0,0,0));
		}
	
		
		
		//change to paintComponent to avoid exceptions? done (was paint, also in super call)
		public void paintComponent(Graphics g)
		{
			//super.paint(g);
			//g.setXORMode(Color.white);
			Rectangle rect = null;
			if ( rasterBackgroundImage != null )
			{
				rect = getVisibleRect();
				g.drawImage(rasterBackgroundImage.getImage(),rect.x,rect.y,rect.width,rect.height,this);
			}
			if ( vectorBackgroundImage != null )
			{
				rect = getVisibleRect();
				vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
				vectorBackgroundImage.setScaleToFit(true);
				vectorBackgroundImage.paintIcon(this, g, rect.x, rect.y);
			}
			
			
			//g.drawImage(backgroundImage,0, 0, this);
			//g.setColor(Color.RED);
			//g.drawOval(5, 5, 100, 100);
				super.paintComponent(g);
			//esto si queremos que el margen superior sea "non-scrolling":
			if ( rasterBackgroundImage != null )
			{
				g.setClip(rect.x,rect.y,rect.width,getMargin().top);
				//System.err.println("Clipping rectangle: " + rect.x + " " + rect.y + " " + rect.width + " " + getMargin().top);
				g.drawImage(rasterBackgroundImage.getImage(),rect.x,rect.y,rect.width,rect.height,this);
			}
			if ( vectorBackgroundImage != null )
			{
				g.setClip(rect.x,rect.y,rect.width,getMargin().top);
				vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
				vectorBackgroundImage.paintIcon(this, g, rect.x, rect.y);
			}
			
		}
		

}
