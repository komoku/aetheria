package eu.irreality.age;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JTextPane;

public class SwingTextAreaListener implements KeyListener
{

	JTextPane elAreaTexto;
	ClassicalColoredSwingClient cl;
	Vector gameLog;
	
	boolean press_any_key = false;
	
	int ncommands=0;

	public void countCommand()
	{
		ncommands++;
	}

	public SwingTextAreaListener ( JTextPane nAreaJTexto , Vector gameLog , ClassicalColoredSwingClient cl )
	{
		elAreaTexto = nAreaJTexto;
		//elAreaJTexto = nAreaJTexto;
		this.gameLog = gameLog;
		this.cl = cl;
	}
	
	
	public void keyTyped(KeyEvent e)
	{
		;
	}
	public void keyReleased(KeyEvent e)
	{
		;
	}
	public void keyPressed(KeyEvent e)
	{
		if ( press_any_key )
		{
			if ( e.getKeyCode() != KeyEvent.VK_ENTER )
			{
				//ya pulsaron la tecla, continúa la ejecución normal.
				setPressAnyKeyState(false);
				cl.setInputString(null);
				//cl.notify();
			}
			else
			{
				//ya pulsaron; pero es además un action event. Será éste el que se
				//encargue de cambiar el estado. 
			}
		}
		
		else if ( e.getKeyCode() == KeyEvent.VK_ENTER )
		{
		
				if ( cl.isMemoryEnabled() )
					cl.addToBackStack ( cl.getCommandText().trim() );
				
				cl.write("\n");
				countCommand();
				
				cl.setInputString( cl.getCommandText() ); //manda el comando al cliente y lo notifica de ello.
				
				cl.write( cl.getColorCode("input") + " > " + cl.getCommandText().trim() + cl.getColorCode("reset") );
				
				cl.writeTitle ( ncommands + " comando" + ((ncommands==1)?"":"s") , 2 );
				
				
				//cl.notify();
				
				//add new command to game log
					gameLog.addElement( cl.getCommandText() );
				//elAreaTexto.setText("");
				//esperando.resumeExecution();
		}

				
		else if ( cl.isMemoryEnabled() )
		{
			//doskey (memoria)
			if ( e.getKeyCode() == KeyEvent.VK_UP )
			{
				//System.out.println("GoBack");
				cl.goBack();
			}
			else if ( e.getKeyCode() == KeyEvent.VK_DOWN )
			{
				cl.goForward();
			}
			
		}
		
	}
	
	public void setPressAnyKeyState ( boolean value )
	{
		press_any_key = value;
		if ( value )
		{
			//esperamos por una tecla
			
			//elCampoJTexto.setText("Pulsa cualquier tecla...");
			//elCampoJTexto.setEditable(false);
			//elCampoJTexto.grabFocus();
			cl.write("Pulsa cualquier tecla...");
			elAreaTexto.grabFocus();
			elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
			elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
			elAreaTexto.setEditable(false);
		
		}		
		else
		{
			//ejecución normal
			
			//elCampoJTexto.setText("");
			elAreaTexto.setEditable(true);
		}
	}
	
}

