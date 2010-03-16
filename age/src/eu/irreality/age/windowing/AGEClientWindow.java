package eu.irreality.age.windowing;

import java.awt.Graphics;
import java.awt.event.FocusListener;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

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
    
    public void guardarLog();
    public void guardarEstado();
    
    public void exitNow();
 
    
    public void addFocusListener ( FocusListener fl );
    
}
