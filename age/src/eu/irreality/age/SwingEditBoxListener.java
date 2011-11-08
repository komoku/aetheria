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
		if ( inTransitoryState ) return; //we are changing between normal mode and "press any key" mode, discard all events until change is completed
		
		if ( !press_any_key )
		{
			//System.err.println("[DN] editbox action performed, not in PAK state");
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
			
			//System.err.println("[DN] Input string set to " + elCampoJTexto.getText());
			
			//add new command to game log
			gameLog.addElement(elCampoJTexto.getText());
			elCampoJTexto.setText("");
			//esperando.resumeExecution();
		}
		else
		{
			//System.err.println("[DN] editbox action performed, in PAK state");
			setPressAnyKeyState(false);
			//System.err.println("[DN] PAK state unset: " + press_any_key + " - setting input string to null");
			
			//notificar
			cl.setInputString(null);
			//cl.notify();
			//esperando.resumeExecution();
		}
	}
	
	private static boolean isPageUpDownEvent ( KeyEvent e )
	{
		return ( e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN );
	}
	
	private void redirectToTextArea ( KeyEvent e )
	{
		cl.getTextArea().dispatchEvent(e);
	}
	
	private boolean consumeKeyEvents = false;
	
	public void keyTyped(KeyEvent e)
	{
		if ( isPageUpDownEvent(e) )
			redirectToTextArea(e);
		if ( press_any_key || consumeKeyEvents ) //don't show the character that they have typed in this case:
			e.consume();
	}
	public void keyReleased(KeyEvent e)
	{
		if ( isPageUpDownEvent(e) )
			redirectToTextArea(e);
		if ( press_any_key || consumeKeyEvents ) //don't show the character that they have typed in this case:
		{
			e.consume();
			consumeKeyEvents = false; //keyReleased is last event to be consumed for this key
		}
	}
	
	public boolean isModifierKey ( KeyEvent e )
	{
		return ( e.getKeyCode() == KeyEvent.VK_ALT || e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == KeyEvent.VK_META || e.getKeyCode() == KeyEvent.VK_ALT_GRAPH 
		 || e.getKeyCode() == KeyEvent.VK_PAUSE || e.getKeyCode() == KeyEvent.VK_PRINTSCREEN );		
	}
	
	public void keyPressed(KeyEvent e)
	{
		
		if ( isPageUpDownEvent(e) )
		{
			redirectToTextArea(e);
			return;
		}
		
		if ( isModifierKey(e) ) return; 
		
		if ( press_any_key )
		{
			if ( e.getKeyCode() != KeyEvent.VK_ENTER )
			{
				//ya pulsaron la tecla, continúa la ejecución normal.
				setPressAnyKeyState(false);
				cl.setInputString(null);
				e.consume();
				consumeKeyEvents = true; //consume also the typed and released events
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
	
	private boolean inTransitoryState = false;
	
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
				/*
				 * The inTransitoryState flag marks that a waitKeyPress() has been called but we have not yet established the
				 * wait key press state in the edit box and edit box listener.
				 * If this flag is not used, we are at risk of the following when holding the ENTER key for a while:
				 * 1. waitKeyPress() is called, but
				 * 2. a pending ActionEvent (ENTER keypress) is processed before the change of "press any key" state is realised
				 * 3. this ActionEvent notifies the waiting waitKeyPress() method, so it returns and the game engine thread continues its run
				 * 4. then, the change of "press any key" state happens, so we are at an inconsistent state: the edit box thinks we are
				 * waiting for a key, but the game engine has moved on and issues a getInput() call
				 * 5. the edit box's next ENTER key press notifies the getInput() call and it returns null
				 */
				inTransitoryState = true;				
				SwingUtilities.invokeLater //to process all prev. events
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
			//System.out.println("Setting PAK");
			//esperamos por una tecla
			elCampoJTexto.setForeground(Color.black);
			//System.out.println("Setting PAK 1");
			elCampoJTexto.setPromptsEnabled(false);
			elCampoJTexto.setText(cl.getKeyRequestText());
			//System.out.println("Setting PAK 2");
			elCampoJTexto.setEditable(false);
			//workaround for java bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6223733 :
			elCampoJTexto.getCaret().setVisible(false);
			//System.out.println("Setting PAK 3");
			elCampoJTexto.grabFocus();
			//System.out.println("Set PAK");
		}		
		else
		{
			//ejecución normal
			//System.out.println("Setting UNPAK 1");
			elCampoJTexto.setPromptsEnabled(true);
			elCampoJTexto.setText("");
			//System.out.println("Setting UNPAK 2");
			elCampoJTexto.setEditable(true);
			//workaround for java bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6223733 :
			elCampoJTexto.getCaret().setVisible(true);
			//System.out.println("Setting UNPAK 3");
			elCampoJTexto.setForeground(Color.red); //until getInput() is called.
		}
		inTransitoryState = false;
	}
	
}

