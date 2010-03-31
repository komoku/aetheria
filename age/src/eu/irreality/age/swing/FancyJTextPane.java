package eu.irreality.age.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JTextPane;

public class FancyJTextPane extends JTextPane
{

		private Image backgroundImage;
	
		public Image getBackgroundImage() { return backgroundImage; }
		public void setBackgroundImage(Image i) 
		{ 
			this.backgroundImage = i; 
			if ( backgroundImage != null ) 
				setOpaque(false);
			else
				setOpaque(true);
		}

		public FancyJTextPane()
		{
			super();
			//setMargin(new Insets(80,80,80,80));
			//setOpaque(false);
			setBackground(new Color(0,0,0,0));
		}
	
		
		
		
		public void paint(Graphics g)
		{
			//super.paint(g);
			//g.setXORMode(Color.white);
			Rectangle rect = null;
			if ( backgroundImage != null )
			{
				rect = getVisibleRect();
				g.drawImage(backgroundImage,rect.x,rect.y,rect.width,rect.height,this);
			}
			//g.drawImage(backgroundImage,0, 0, this);
			//g.setColor(Color.RED);
			//g.drawOval(5, 5, 100, 100);
				super.paint(g);
			//esto si queremos que el margen superior sea "non-scrolling":
			if ( backgroundImage != null )
			{
				g.setClip(rect.x,rect.y,rect.width,getMargin().top);
				g.drawImage(backgroundImage,rect.x,rect.y,rect.width,rect.height,this);
			}
			
		}
		

}
