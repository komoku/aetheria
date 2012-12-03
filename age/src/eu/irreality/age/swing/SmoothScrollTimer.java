package eu.irreality.age.swing;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

	private ColoredSwingClient cl;
	
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
				p.y = p.y+2;
				elScrolling.getViewport().setViewPosition(p);
				//elAreaTexto.setVisible(true);
				elAreaTexto.repaint();
				//elAreaTexto.revalidate();
			}
		};
		this.addActionListener(smoothScrollAction);
	}

}
