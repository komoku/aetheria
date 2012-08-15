package eu.irreality.age.windowing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.FocusListener;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.World;

/**
 * Interface for any window that is able to serve as an AGE client.
 * For example, SwingAetheriaGameLoader is one.
 *
 */
public interface AGEClientWindow extends AGELoggingWindow
{

    public void setTitle ( String s );
    public String getTitle();
    
    public void repaint();
    
    public JMenuBar getTheJMenuBar();
    public void setTheJMenuBar( JMenuBar jmb );
    
    public World getMundo();
    
    public void setFullScreenMode(boolean b);
    public boolean isFullScreenMode();
    
    public Graphics getGraphics();
    
    public void reinit();
    
    public void update ( Graphics g );
    
    public void setVisible ( boolean b );
    
    public void updateNow();
    
    public JPanel getMainPanel();
    public void setMainPanel(JPanel p); //changes the panel that the window is showing
    
    public void guardarLog();
    public void guardarEstado();
    
    public void exitNow();
 
    public boolean supportsFullScreen();
    
    public void addFocusListener ( FocusListener fl );
    
    /**
     * Writes a String to the client window.
     */
    public void write ( String s );
    
    public InputOutputClient getIO();
    
    /**
     * Returns the version of the client window.
     */
    public String getVersion();
    
    
	/**
	 * Obtains the size of the screen in which the window is being displayed. This can be useful to make layout decisions.
	 * @return The size of the screen, or null if it cannot be recovered for some reason (e.g. running in an applet with javascript disabled or applet not passing height and width parameters).
	 */
	public Dimension getScreenSize();

	
    
}
