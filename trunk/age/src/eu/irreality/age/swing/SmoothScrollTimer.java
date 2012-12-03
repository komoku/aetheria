package eu.irreality.age.swing;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;

import eu.irreality.age.ColoredSwingClient;

/**
 * Timer for smooth scrolling a JScrollPane to the bottom.
 * @author carlos
 *
 */
public class SmoothScrollTimer extends Timer
{

	/** Client containing the text pane that we are going to scroll.*/
	private ColoredSwingClient cl;
		
	public static final int FIXED_SPEED_MODE = 0;
	public static final int ADAPTIVE_SPEED_MODE = 1;
	
	/** Whether the speed is constant or adaptive.*/
	private int movementMode = FIXED_SPEED_MODE;
	
	/** Number of pixels that we scroll per frame of the timer, if in fixed speed mode. In adaptive speed mode, this can be multiplied by a factor.*/
	private int pixelsPerFrame = 2;
	
	public int getMovementMode()
	{
		return movementMode;
	}
	
	/**
	 * Sets the movement mode: FIXED_SPEED_MODE for fixed speed, ADAPTIVE_SPEED_MODE for speed that varies according to
	 * distance to the scrolling goal.
	 * @param movementMode
	 */
	public void setMovementMode ( int movementMode )
	{
		this.movementMode = movementMode;
	}
	
	/**
	 * Obtains the movement mode: FIXED_SPEED_MODE for fixed speed, ADAPTIVE_SPEED_MODE for speed that varies according to
	 * distance to the scrolling goal.
	 * @return
	 */
	public int getPixelsPerFrame()
	{
		return pixelsPerFrame;
	}
	
	public void setPixelsPerFrame ( int pixelsPerFrame )
	{
		this.pixelsPerFrame = pixelsPerFrame;
	}
	
	/**
	 * Sets the speed of scrolling in pixels per second, leaving the frame unchanged but changing pixelsPerFrame.
	 * @param pixelsPerSecond
	 */
	public void setSpeed ( int pixelsPerSecond )
	{
		int fps = 1000/this.getDelay();
		pixelsPerFrame = pixelsPerSecond / fps;
	}
	
	/**
	 * Gets the speed of scrolling in pixels per second.
	 */
	public int getSpeed ( )
	{
		int fps = 1000/this.getDelay();
		return pixelsPerFrame * fps;
	}
	
	public int calculateSpeed ( ActionEvent evt , ColoredSwingClient cl )
	{
		JScrollPane elScrolling = cl.getScrollPane();
		JScrollBar  vbar = (JScrollBar) elScrolling.getVerticalScrollBar();
		int distance = vbar.getMaximum() - (vbar.getValue() + vbar.getVisibleAmount());
		double speedFactor = ((double)distance)/40;
		int theSpeed = (int) Math.round(pixelsPerFrame * speedFactor);
		if ( theSpeed < 1 ) return 1;
		else return theSpeed;
	}
	
	public SmoothScrollTimer( final int millis , final ColoredSwingClient cl ) 
	{
		super( millis , null );
		this.cl = cl;
		final JScrollPane elScrolling = cl.getScrollPane();
		final JTextPane elAreaTexto = cl.getTextArea();
		Action smoothScrollAction = new AbstractAction()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				//runs on the EDT
				if ( cl.scrollIsAtBottom() )
				{
					//stop scrolling: we're already at bottom
					SmoothScrollTimer.this.stop();
					return;
				}
				Point p = elScrolling.getViewport().getViewPosition();
				
				int offset = 1;
				if ( movementMode == FIXED_SPEED_MODE )
					offset = pixelsPerFrame;
				else if ( movementMode == ADAPTIVE_SPEED_MODE )
					offset = calculateSpeed ( evt , cl );
				
				p.y = p.y+offset;
					
				elScrolling.getViewport().setViewPosition(p);
				//elAreaTexto.setVisible(true);
				elAreaTexto.repaint();
				//elAreaTexto.revalidate();
			}
		};
		this.addActionListener(smoothScrollAction);
	}

}
