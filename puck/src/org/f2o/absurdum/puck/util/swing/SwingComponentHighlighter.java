/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 11/03/2011 19:54:09
 */
package org.f2o.absurdum.puck.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author carlos
 *
 */
public class SwingComponentHighlighter 
{
	
	private static int FADE_ITERATIONS = 10; //number of iterations of the fade
	
	private static Color getDefaultTextFieldBackgroundColor()
	{
		Color c = UIManager.getColor("TextField.background");
		if ( c != null ) return c;
		else return Color.WHITE;
	}

	private static void doColorChange ( final Component c , final Color color )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						c.setBackground(color);
						c.repaint();
					}
				});
	}
	
	public static Color getWeightedAverage ( Color c1 , Color c2 , double w1 , double w2 )
	{
		return new Color (
			(int) ( c1.getRed()*w1 + c2.getRed()*w2 ),
			(int) ( c1.getGreen()*w1 + c2.getGreen()*w2 ),
			(int) ( c1.getBlue()*w1 + c2.getBlue()*w2 )
		);
	}
	
	/**
	 * Sets the background of a text component to a reddish colour and then fades gradually to the default background.
	 * @param c The component for the colour change.
	 * @param milliseconds The time in milliseconds for the fade.
	 */
	public static void temporalRedBackground ( final Component c , final long milliseconds )
	{
		Thread thr = new Thread()
		{
			public void run()
			{
				doTemporalRedBackground ( c , milliseconds );
			}
		};
		thr.start();
	}
	
	/**
	 * Sets the background of a text component to a reddish colour and then fades gradually to the default background.
	 * @param c The component for the colour change.
	 */
	public static void temporalRedBackground ( Component c )
	{
		temporalRedBackground ( c , 400 );
	}
	
	/**
	 * Must be invoked *outside* event dispatching thread.
	 */
	private static void doTemporalRedBackground ( final Component c , long milliseconds )
	{
		final Color defaultColor = getDefaultTextFieldBackgroundColor();
		final Color redColor = new Color(255,150,150);
		doColorChange ( c , redColor );
		//Timer t = new Timer();
		for ( int i = 1 ; i <= FADE_ITERATIONS ; i++ )
		{
			final double defaultWeight = ((double)1 / (double)FADE_ITERATIONS) * i;
			final double redWeight = 1 - defaultWeight;
			//long time = (long)(((double)milliseconds / (double)20) * ((double)i));
			/*
			TimerTask task = new TimerTask()
			{
				public void run()
				{
					doColorChange(c,getWeightedAverage(defaultColor,redColor,defaultWeight,redWeight));
				}
			};
			t.schedule ( task , time );
			*/
			doColorChange(c,getWeightedAverage(defaultColor,redColor,defaultWeight,redWeight));
			try 
			{
				Thread.sleep(milliseconds/FADE_ITERATIONS);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		/*
		t.schedule(new TimerTask()
		{
			public void run()
			{
				doColorChange(c,getDefaultTextFieldBackgroundColor());
			}
		},milliseconds
		);
		*/
	}
	
}
