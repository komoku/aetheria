package eu.irreality.age.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import eu.irreality.age.ColoredSwingClient;

/**
 * A timer used to slowly fade in a text from the background color to its foreground color.
 * @author carlos
 *
 */
public class ColorFadeInTimer extends Timer
{

	/**The client where the text effect is going to be shown.*/
	private ColoredSwingClient cl;
	
	/**The color that the text should have at the end of the timer.*/
	private Color targetColor;
	
	/**Starting offset of the text to which the effect should be applied.*/
	private int offset;
	
	/**Length of the text to which the effect should be applied.*/
	private int length;
	
	/**Duration of the effect in milliseconds*/
	private int duration;
	
	/**Whether alpha transparency should be used*/
	private boolean useAlpha = true;
	
	/**
	 * Sets the duration (in milliseconds) of the text color fade-in effect.
	 * @param duration The duration of the text fade-in effect, in ms.
	 */
	public void setDuration ( int duration )
	{
		this.duration = duration;
	}
	
	/**
	 * @return The duration (in milliseconds) of the text color fade-in effect.
	 */
	public int getDuration()
	{
		return duration;
	}
	
	private Color getSourceColor ( ColoredSwingClient cl , Color targetColor )
	{
		if ( useAlpha )
			return new Color ( targetColor.getRed() , targetColor.getGreen() , targetColor.getBlue() , 0 /*transparent*/ );
		else
			return cl.getTextArea().getBackground();
	}
	
	public ColorFadeInTimer ( final int delay , final ColoredSwingClient cl , int offset , int length , final Color targetColor , int effectDuration )
	{
		super ( delay , null );
		this.cl = cl;
		this.offset = offset;
		this.length = length;
		this.targetColor = targetColor;
		this.duration = effectDuration;
		
		Action colorFadeInAction = new AbstractAction()
		{
			Color sourceColor = getSourceColor(cl,targetColor); //initially, the text's color is the text area background
			Color currentColor = sourceColor; 
			int iters = 0;
			
			public void actionPerformed ( ActionEvent evt )
			{
				double progress = (double)(iters * ColorFadeInTimer.this.getDelay()) / (double)duration;
				if ( progress <= 0.0 ) currentColor = sourceColor;
				else if ( progress >= 1.0 ) currentColor = ColorFadeInTimer.this.targetColor;
				else currentColor = getTransitionColor ( sourceColor , ColorFadeInTimer.this.targetColor , progress );
				
				//System.err.println("Iter " + iters + " color " + currentColor);
				
				StyledDocument sd = (StyledDocument) cl.getTextArea().getDocument();
				SimpleAttributeSet colorAttrToApply = new SimpleAttributeSet();
				StyleConstants.setForeground(colorAttrToApply,currentColor);
				sd.setCharacterAttributes(ColorFadeInTimer.this.offset, ColorFadeInTimer.this.length, colorAttrToApply, false);
				
				if ( progress >= 1.0 ) ColorFadeInTimer.this.stop();
				else iters++;
			}
		};
		this.addActionListener(colorFadeInAction);
		
	}
	
	/**
	 * Obtains the color obtained from a weighted average, progress*destination + (1-progress)*source.
	 * @param source
	 * @param destination
	 * @param progress
	 * @return
	 */
	public Color getTransitionColor ( Color source , Color destination , double progress )
	{
		double remaining = 1.0 - progress;
		int red = (int) ( progress * destination.getRed() + remaining * source.getRed() );
		int green = (int) ( progress * destination.getGreen() + remaining * source.getGreen() );
		int blue = (int) ( progress * destination.getBlue() + remaining * source.getBlue() );
		int alpha = (int) ( progress * destination.getAlpha() + remaining * source.getAlpha() );
		return new Color ( red , green , blue , alpha );
	}
	
}
