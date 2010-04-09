package eu.irreality.age;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import eu.irreality.age.swing.FancyJTextField;

class SwingEditBoxListener implements ActionListener , KeyListener
{

	FancyJTextField elCampoJTexto;
	//JTextComponent elAreaJTexto;
	ColoredSwingClient cl;
	//Player esperando;
	Vector gameLog;
	
	boolean press_any_key = false;
	
	int ncommands=0;

	public void countCommand()
	{
		ncommands++;
	}

	public SwingEditBoxListener ( FancyJTextField nCampoJTexto , Vector gameLog , ColoredSwingClient cl )
	{
		elCampoJTexto = nCampoJTexto;
		//elAreaJTexto = nAreaJTexto;
		this.gameLog = gameLog;
		this.cl = cl;
	}
	
	//public void setWaitingPlayer ( Player p )
	//{
	//	esperando = p;	
	//}
	
	//public void setWaitingClient ( Client cl )

	

	

	
	public void actionPerformed( ActionEvent e )
	{
		if ( !press_any_key )
		{
			if ( cl.isMemoryEnabled() )
			{
				
				cl.addToBackStack ( elCampoJTexto.getText().trim() );
			
				//reintegrate forward stack into back stack
				
				cl.forwardStackIntoBackStack();
				
				//if there was something in forward stack, add this
				//(this function doesn't add repeated)
				
				cl.addToBackStack ( elCampoJTexto.getText().trim() );
			
			}
			
			cl.write("\n");
			countCommand();
			
			if ( cl.isEchoEnabled() )
				cl.write( cl.getColorCode("input") + cl.getEchoText() + elCampoJTexto.getText().trim() + cl.getColorCode("reset") + "\n" );
			
			cl.writeTitle ( ncommands + " comando" + ((ncommands==1)?"":"s") , 2 );
			
			//elAreaJTexto.append("\n");
			//elAreaJTexto.append("\n" + "[COMANDO]  " + elCampoJTexto.getText());
			
			//elAreaJTexto.getDocument().insertString("\n");
			//elAreaJTexto.getDocument().insertString("\n" + "[COMANDO]  " + elCampoJTexto.getText());
			
			//esperando.setCommandString(elCampoJTexto.getText());
			//notificar (setInputString ya notifica)
			cl.setInputString(elCampoJTexto.getText());
			//cl.notify();
			
			//add new command to game log
			gameLog.addElement(elCampoJTexto.getText());
			elCampoJTexto.setText("");
			//esperando.resumeExecution();
		}
		else
		{
			setPressAnyKeyState(false);
			
			//notificar
			cl.setInputString(null);
			//cl.notify();
			//esperando.resumeExecution();
		}
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
	
	public void setPressAnyKeyState ( final boolean value )
	{
		//System.out.println("In PAK, Thread is " + Thread.currentThread());
		//System.out.println("And it's " + SwingUtilities.isEventDispatchThread());
		
		if ( SwingUtilities.isEventDispatchThread() )
			doSetPressAnyKeyState(value);
		else
		{
			try
			{
				SwingUtilities.invokeAndWait 
				( 
						new Runnable()
						{
							public void run()
							{
								doSetPressAnyKeyState(value);
							}
						}
				);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
				
	}
	
	public void doSetPressAnyKeyState ( final boolean value )
	{
		press_any_key = value;
		if ( value )
		{
			System.out.println("Setting PAK");
			//esperamos por una tecla
			elCampoJTexto.setForeground(Color.black);
			System.out.println("Setting PAK 1");
			elCampoJTexto.setPromptsEnabled(false);
			elCampoJTexto.setText("Pulsa cualquier tecla...");
			System.out.println("Setting PAK 2");
			elCampoJTexto.setEditable(false);
			System.out.println("Setting PAK 3");
			elCampoJTexto.grabFocus();
			System.out.println("Set PAK");
		}		
		else
		{
			//ejecución normal
			System.out.println("Setting UNPAK 1");
			elCampoJTexto.setPromptsEnabled(true);
			elCampoJTexto.setText("");
			System.out.println("Setting UNPAK 2");
			elCampoJTexto.setEditable(true);
			System.out.println("Setting UNPAK 3");
		}
	}
	
}

