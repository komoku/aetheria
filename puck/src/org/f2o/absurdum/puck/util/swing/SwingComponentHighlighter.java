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

	private static void doColorChange ( final Component c , final Color color , final boolean background )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						if ( background )
							c.setBackground(color);
						else
							c.setForeground(color);
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
	 * Sets the background or foreground of a text component to a given colour and then fades gradually to the default background.
	 * @param c The component for the colour change.
	 * @param milliseconds The time in milliseconds for the fade.
	 * @param color The colour to change to.
	 * @param background Change the background color (true) or the foreground color (false)?
	 */
	public static void gradualBackgroundChange ( final Component c , final long milliseconds , final Color color , final boolean background )
	{
		Thread thr = new Thread()
		{
			public void run()
			{
				doGradualColorChange ( c , milliseconds , color , background );
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
		gradualBackgroundChange ( c , 400 , new Color(255,150,150) , true );
	}
	
	public static void temporalBlueForeground ( Component c )
	{
		gradualBackgroundChange ( c , 400 , new Color(150,150,255) , false );
	}
	
	private static void doGradualColorChange ( final Component c , long milliseconds , Color color , boolean background )
	{
		final Color defaultColor = getDefaultTextFieldBackgroundColor();
		final Color redColor = color;
		doColorChange ( c , redColor , background );
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
			doColorChange(c,getWeightedAverage(defaultColor,redColor,defaultWeight,redWeight),background);
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
