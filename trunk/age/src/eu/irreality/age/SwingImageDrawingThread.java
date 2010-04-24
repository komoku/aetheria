/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

class SplashWindow extends Window {
    Image splashIm;

    SplashWindow(Frame parent, Image splashIm) {
        super(parent);
        this.splashIm = splashIm;
        setSize(splashIm.getWidth(null),splashIm.getHeight(null));

        /* Center the window */
        Dimension screenDim = 
             Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = getBounds();
        setLocation((screenDim.width - winDim.width) / 2,
		(screenDim.height - winDim.height) / 2);
        setVisible(true);
    }

    public void paint(Graphics g) {
       if (splashIm != null) {
           g.drawImage(splashIm,0,0,this);
       }
    }
}

public class SwingImageDrawingThread extends Thread
{

	String imfile;
	Image img;
	long delay;
	//Component glassPane;
	JFrame madre;

	public SwingImageDrawingThread ( String imfile , long delay , JFrame madre )
	{
		this.imfile = imfile;
		this.delay = delay;
	//	this.glassPane = madre.getGlassPane();
		this.madre = madre;

	}
	
	public synchronized void ejecutar ( )
	{

		/*//para ganar el control del monitor
		
		img = madre.getToolkit().createImage( imfile );
		
		JLabel etiq = new JLabel ( new ImageIcon ( img ) );
		
		((Container)glassPane).add(etiq);
		//((JComponent)glassPane).setAlignmentX ( Component.CENTER_ALIGNMENT );
		//((JComponent)glassPane).setAlignmentY ( Component.CENTER_ALIGNMENT );
		etiq.setAlignmentX ( Component.CENTER_ALIGNMENT );
		etiq.setAlignmentY ( Component.CENTER_ALIGNMENT );
		glassPane.repaint();
		glassPane.setVisible(true);
		
		Thread.yield();
		
		//setPriority ( Thread.MIN_PRIORITY );
		
		try
		{
			wait(delay);
		}
		catch ( InterruptedException intex )
		{
			;
		}
		((Container)glassPane).remove(etiq);
		glassPane.setVisible(false);*/
		
		
		
		
		//BEGIN SPLASH WINDOW RELATED CODE
		
		MediaTracker mt = new MediaTracker(madre);
       	//Image splashIm = madre.getToolkit().createImage(imfile);
       	Image splashIm = madre.getToolkit().getImage(this.getClass().getClassLoader().getResource(imfile));
       	
       	
       	mt.addImage(splashIm,0);
       	try 
		{
         	mt.waitForID(0);
       	} 
		catch(InterruptedException ie){}
		
		//splash window (test)
		
		SplashWindow w = new SplashWindow( madre , splashIm );
        w.setVisible(true);
	
		
		//give time to GUI thread
        try
        {
        	Thread.sleep(200);
            Thread.currentThread().yield(); // Give a chance to other threads.
        }  catch(InterruptedException e)  { }
        w.repaint();
        try
        {
        	Thread.sleep(200);
        	Thread.currentThread().yield(); // Give a chance to other threads.
        }  catch(InterruptedException e)  { }
		
        try {
	  		Thread.sleep(delay);
       	} catch(InterruptedException ie){}
       	w.dispose();
			

		//END SPLASH WINDOW RELATED CODE
		
		
		

	}
	
	public void run ()
	{
	//	setPriority ( Thread.MAX_PRIORITY );
		ejecutar();

	}

}
