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
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.kitfox.svg.app.beans.SVGIcon;

import eu.irreality.age.ImageConstants;

public class FancyJTextPane extends JTextPane implements ImageConstants
{
	
	private static final boolean VECTOR_TO_RASTER = true; //optimization: vector images will be converted to raster, conversion will hold while panel
														  // size not altered
															//todo: the false variant really makes no sense. This should probably just be assumed.

		private ImageIcon rasterBackgroundImage;
		private SVGIcon vectorBackgroundImage;
		
		/**The vector background image, converted to a raster image for efficiency*/
		//private BufferedImage convertedVectorBackgroundImage;
		
		/**This is used to know when to update the raster->vector conversion (when window has been moved)*/
		private Rectangle lastVisibleRect = null;
		
		//for the top margin (issue 200 avoidance)
		private BufferedImage upperSubImage;
		//for the bottom margin
		private BufferedImage lowerSubImage;
		
		//if true, top-bottom margins act on viewable area instead of on whole document/text area contents
		private boolean marginsOnViewableArea = false; //however, setMargins() in ColoredSwingClient will set it to true by default
		
		
		private void updateVectorToRasterConversion ( )
		{
			if ( vectorBackgroundImage == null ) return;
			Rectangle rect = getVisibleRect();
			if ( rect == null ) return;
			if ( lastVisibleRect == null || ( rect.width != lastVisibleRect.width || rect.height != lastVisibleRect.height || rasterBackgroundImage == null ) ) //window moved/resized, or image changed
			{
				lastVisibleRect = rect;
				BufferedImage conversion = new BufferedImage(rect.width,rect.height,BufferedImage.TYPE_INT_ARGB);
				vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
				vectorBackgroundImage.setScaleToFit(true);
				vectorBackgroundImage.paintIcon(null, conversion.getGraphics(), 0, 0);
				rasterBackgroundImage = new ImageIcon(conversion);
			}
		}
		
		
		public void setMarginsOnViewableArea ( boolean value )
		{
			marginsOnViewableArea = value;
		}
		
		public ImageIcon getRasterBackgroundImage() { return rasterBackgroundImage; }
		
		/*
		 * returns Icon because if we return SVGIcon, checkCoalescing() (internal java method)
		 * creates a dependency with the SVG Salamander library.
		 */
		public Icon getVectorBackgroundImage() { return vectorBackgroundImage; }
		
		//private int scalingMode = FIT_BOTH;

		private void refreshUpperSubImage()
		{
			Rectangle rect = getVisibleRect();
			upperSubImage = new BufferedImage(rect.width,getMargin().top,BufferedImage.TYPE_INT_ARGB);
			Graphics tempG = upperSubImage.createGraphics();
			tempG.drawImage(rasterBackgroundImage.getImage(),0,0,rect.width,rect.height,this);
			tempG.dispose();
		}
		
		private void refreshLowerSubImage()
		{
			Rectangle rect = getVisibleRect();
			lowerSubImage = new BufferedImage(rect.width,getMargin().bottom,BufferedImage.TYPE_INT_ARGB);
			Graphics tempG = lowerSubImage.createGraphics();
			tempG.drawImage(rasterBackgroundImage.getImage(),0,-rect.height+getMargin().bottom,rect.width,rect.height,this);
			tempG.dispose();
		}
		
		public void setRasterBackgroundImage(ImageIcon i) 
		{ 
			this.rasterBackgroundImage = i; 
			if ( rasterBackgroundImage != null || vectorBackgroundImage != null ) 
				setOpaque(false);
			else
				setOpaque(true);
			
			if ( rasterBackgroundImage != null )
			{
				refreshUpperSubImage();
				refreshLowerSubImage();
			}
				
		    //repaint so that change takes place
		    javax.swing.SwingUtilities.invokeLater(new Runnable()
		    {
		        public void run()
		        {
		            repaint();
		        }
		    });
		}
		
		public void setVectorBackgroundImage(Icon i) //this should really be a SVGIcon, but declaring it as Icon removes applet dependency 
		//(there is some reflection method in jcomponent that looks at the component's method's 
		//arguments and complains if classes are not in the classpath, for some
		//reason)
		{ 
			this.vectorBackgroundImage = (SVGIcon) i; 
			if ( vectorBackgroundImage != null || rasterBackgroundImage != null ) 
				setOpaque(false);
			else
				setOpaque(true);
			vectorBackgroundImage.setAntiAlias(true);
			rasterBackgroundImage = null; //new conversion will have to be made
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
			

				LookAndFeel laf = UIManager.getLookAndFeel();
				if ( laf.getID().equals("Nimbus") )
				{
					//fix so that we can change background in Nimbus
					setUI(new javax.swing.plaf.basic.BasicEditorPaneUI());
				}
		
			
			
			
			//setMargin(new Insets(80,80,80,80));
			//setOpaque(false);
			setDocument ( new FancyStyledDocument() );
			setBackground(new Color(0,0,0,0));
		}
	
		//scaling options not yet implemented
		/*
		private Rectangle getDrawingCoordinates ( int baseHeight , int baseWidth , Rectangle viewport )
		{
			 int imageHeight = (int) baseHeight;
			 int imageWidth = (int) baseWidth;
			 int panelHeight = viewport.height;
			 int panelWidth = viewport.width;
				
			 //FIT_BOTH
			int drawX = viewport.x;
			int drawY = viewport.y;
			int drawW = panelWidth;
			int drawH = panelHeight;
			
			//theVectorImage.setScaleToFit(false);
			
			if ( scalingMode == NO_SCALING )
			{
				drawX = panelWidth/2 - imageWidth/2;
				drawY = panelHeight/2 - imageHeight/2;
				drawW = imageWidth;
				drawH = imageHeight;
				//theVectorImage.setScaleToFit(false);
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
				
			//	theVectorImage.setPreferredSize(new Dimension(drawW,drawH));
				
			    //theVectorImage.setScaleToFit(false);
			    //theVectorImage.setPreferredSize(new Dimension(200,200));
				//System.err.println(theVectorImage.getPreferredSize()); //yeah, gets the nominal size
				//theVectorImage.setScaleToFit(true); //we can set another pref. size to scale
			//theVectorImage.paintIcon(this, g, drawX, drawY);
			return new Rectangle(drawX,drawY,drawW,drawH);
		}
		*/
		
		//these variables are kept so that we know when to refresh the top margin subimage
		private int lastWidth=-1;
		private int lastHeight=-1;
		
		//change to paintComponent to avoid exceptions? done (was paint, also in super call)
		public void paintComponent(Graphics g)
		{
			
			if ( VECTOR_TO_RASTER ) updateVectorToRasterConversion(); //this is done only if applicable (checks inside method)
			
			//super.paint(g);
			//g.setXORMode(Color.white);
			Rectangle rect = null;
			
			rect = getVisibleRect();
			if ( rasterBackgroundImage != null )
			{
				//rect = getVisibleRect();
				g.drawImage(rasterBackgroundImage.getImage(),rect.x,rect.y,rect.width,rect.height,this);
			}
			else if ( vectorBackgroundImage != null )
			{
				//rect = getVisibleRect();
				vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
				vectorBackgroundImage.setScaleToFit(true);
				vectorBackgroundImage.paintIcon(this, g, rect.x, rect.y);
			}
			
			if ( rasterBackgroundImage == null && vectorBackgroundImage == null && marginsOnViewableArea && (getMargin().top > 0 || getMargin().bottom > 0) )
			{
				//in this case, we must repaint the background, or java will reuse our margin rectangles when scrolling, creating a mess!
				//(1) -> we need the component to be non-opaque so that java does not resuse our stuff
				setOpaque(false);
				Color oldColor = g.getColor();
				g.setColor(getBackground());
				//g.setColor(Color.ORANGE);
				g.fillRect(rect.x,rect.y,rect.width,rect.height);
				g.setColor(oldColor);
			}
		
			//g.drawImage(backgroundImage,0, 0, this);
			//g.setColor(Color.RED);
			//g.drawOval(5, 5, 100, 100);
				super.paintComponent(g);
				
			//esto si queremos que el margen superior sea "non-scrolling":
			if ( marginsOnViewableArea && (getMargin().top > 0 || getMargin().bottom > 0) )
			{
				if ( rasterBackgroundImage != null )
				{
					//Rectangle oldArea = g.getClipBounds();
					//g.setClip(rect.x,rect.y,rect.width,getMargin().top);
					//System.err.println("Clipping rectangle: " + rect.x + " " + rect.y + " " + rect.width + " " + getMargin().top);
					//g.drawImage(rasterBackgroundImage.getImage(),rect.x,rect.y,rect.width,rect.height,this);
					//g.setClip(oldArea);
					//g.fillRect(rect.x, rect.y, rect.width, getMargin().top);
					if ( lastWidth != rect.width || lastHeight != rect.height ) 
					{
						if ( getMargin().top > 0 ) refreshUpperSubImage();
						if ( getMargin().bottom > 0 ) refreshLowerSubImage();
					}
					lastWidth = rect.width; lastHeight = rect.height;
					if ( getMargin().top > 0 ) g.drawImage(upperSubImage,rect.x,rect.y,rect.width,getMargin().top,this);
					if ( getMargin().bottom > 0 ) g.drawImage(lowerSubImage,rect.x,rect.y+rect.height-getMargin().bottom,rect.width,getMargin().bottom,this);
				}
				else if ( vectorBackgroundImage != null )
				{
					if ( getMargin().top > 0 )
					{
						g.setClip(rect.x,rect.y,rect.width,getMargin().top);
						vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
						vectorBackgroundImage.paintIcon(this, g, rect.x, rect.y);
					}
					if ( getMargin().bottom > 0 )
					{
						g.setClip(rect.x,rect.y+rect.height-getMargin().bottom,rect.width,getMargin().bottom);
						vectorBackgroundImage.setPreferredSize(new Dimension(rect.width,rect.height));
						vectorBackgroundImage.paintIcon(this, g, rect.x, rect.y);
					}
				}
				if ( rasterBackgroundImage == null && vectorBackgroundImage == null )
				{
					//draw rectangles of background colour on margin areas. This needs the component to be non-opaque, see (1)
					Color oldColor = g.getColor();
					g.setColor(getBackground());
					//g.setColor(Color.RED);
					g.fillRect(rect.x,rect.y,rect.width,getMargin().top);
					//g.setColor(Color.RED);
					g.fillRect(rect.x,rect.y+rect.height-getMargin().bottom,rect.width,getMargin().bottom);
					g.setColor(oldColor);
				}
			}
			
		}
		

		public void scaleFonts ( double scaleFactor )
		{
			applyFontSizeTransform ( new FontSizeTransform ( FontSizeTransform.MULTIPLY , scaleFactor ) );
		}
		
		/**
		 * Zooms in/out by applying an offset or factor to font sizes throughout the document.
		 * @param t Transform to apply to all font sizes in the document.
		 */
		private void applyFontSizeTransform ( FontSizeTransform t )
		{
			StyledDocument sd = (StyledDocument) this.getDocument();
			Element [] rootElts = sd.getRootElements();
			List elements = new ArrayList();
			for ( int i = 0 ; i < rootElts.length ; i++ )
			{
				populateElementList ( elements , rootElts[i] );
			}
			for ( int i = 0 ; i < elements.size() ; i++ )
			{
				applyFontSizeTransform( t , (Element) elements.get(i) , sd );
			}
		}
		
		/**
		 * Adds the leaf elements that are descendant of the given element to the given list.
		 * @param elements
		 * @param elt
		 */
		private void populateElementList ( List elements , Element elt )
		{
			
			if ( elt.isLeaf() )
				elements.add(elt);
			
			//apply recursively to children
			for ( int i = 0 ; i < elt.getElementCount() ; i++ )
			{
				populateElementList ( elements , elt.getElement(i) );
			}
			
		}
		
		/**
		 * Changes the font size of a document element.
		 * @param t A transform to apply to font size.
		 * @param elt An element of the document.
		 * @param sd The styled document containing the element elt.
		 */
		private void applyFontSizeTransform ( FontSizeTransform t , Element elt , StyledDocument sd )
		{			
			//System.err.println("Element: " + elt);
			//System.err.println("Attributes: " + elt.getAttributes());
			//System.err.println("Old Font size: " + elt.getAttributes().getAttribute(StyleConstants.FontSize));

			Object attr = elt.getAttributes().getAttribute(StyleConstants.FontSize);
			if ( attr != null )
			{
				int fontSize = StyleConstants.getFontSize(elt.getAttributes());
				int start = elt.getStartOffset();
				int end = elt.getEndOffset();
				SimpleAttributeSet sizeOnly = new SimpleAttributeSet();
				StyleConstants.setFontSize( sizeOnly , t.apply(fontSize) );
				//System.err.println("New Font size: " + (fontSize+offset));
				sd.setCharacterAttributes(start, end-start, sizeOnly, false);
				//StyleConstants.setFontSize( (MutableAttributeSet) elt.getAttributes(), fontSize+offset);
			}

		}
		
}
