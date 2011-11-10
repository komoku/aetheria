package eu.irreality.age.irc;

import eu.irreality.age.InputOutputClient;
import eu.irreality.age.Player;
import eu.irreality.age.World;
import eu.irreality.age.XMLtoWorldException;
import eu.irreality.age.i18n.UIMessages;

//(entradas síncronas y asíncronas, en este caso, extiende Synchronous por conveniencia,
//no quiere decir que siga siendo Synchronous)
class IrcDccChatClientProxy extends IrcDccChatSynchronousHandler implements InputOutputClient
{


	private java.util.Vector gameLog = new java.util.Vector();
	
	
	public boolean isDisconnected()
	{
		return clientHasDisconnected; //protected
	}
	
	public IrcDccChatClientProxy ( IrcDccChatSocket s , World mundo )
	{
		super(s);
		
		System.out.println("Creatin' IRCDccChatClientProxy");

		try
		{
			System.out.println("Calling addNewPlayerASAP()");
			mundo.addNewPlayerASAP ( this );
		}
		catch ( XMLtoWorldException x2we )
		{
			write("Couldn't: " + x2we);
		}
	}
	
	public String getColorCode ( String colorKey )
	{
		if ( colorKey == null ) return "";
		String lowerKey = colorKey.toLowerCase();
		if ( lowerKey.equals("action") )
			return "12";
		else if ( lowerKey.equals("default") )
			return "01";
		else if ( lowerKey.equals("description") )
			return "03";
		else if ( lowerKey.equals("error"))
			return "04";
		else if ( lowerKey.equals("important"))
			return "14";
		else if ( lowerKey.equals("information"))
			return "15";
		else if ( lowerKey.equals("input"))
			return "11"; //unused, I think
		else if ( lowerKey.equals("denial"))
			return "05";
		else if ( lowerKey.equals("reset"))
			return "";
		else if ( lowerKey.equals("story"))
			return "06";
		else
			return "";
	}
	


	
	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla()
	{
		clearScreen();
	}

	public void clearScreen()
	{
		;
	}
	
	
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo(String s)
	{
		writeTitle(s);
	}

	public void writeTitle(String s)
	{
		;
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo(String s,int i)
	{
		writeTitle(s, i);
	}

	public void writeTitle(String s,int i)
	{
		;
	}
	
	/**
	 * @deprecated Use {@link #forceInput(String,boolean)} instead
	 */
	public void forzarEntrada ( String s , boolean output_enabled )
	{
		forceInput(s, output_enabled);
	}

	public void forceInput ( String s , boolean output_enabled )
	{
		gameLog.addElement(s);
		write("\n");
		write("<" + curNick + "> " + s.trim() );
	}
	
	
	//getInput() y getRealTimeInput() funcionan igual que las de ColoredSwingClient.
	//(al fin y al cabo, el método de notificación es igual)
	public String getInput ( Player p )
	{
		write(UIMessages.getInstance().getMessage("irc.yourturn"));
		return super.getInput();
	}
	
	//no bloqueante.
	public synchronized String getRealTimeInput ( Player pl )  
	{
		String temp = curText;
		curText = null;
		return temp;
	}
	
	public boolean isColorEnabled()
	{
		return true;
	}
	
	public boolean isLoggingEnabled()
	{
		return true;
	}
	
	public void loguear ( String s )
	{
		gameLog.add ( s );
	}
	
	public boolean isMemoryEnabled()
	{
		return false;
	}
	
	public boolean isTitleEnabled()
	{
		return false;
	}
	

	

	
	
	public void waitKeyPress()
	{
		write( UIMessages.getInstance().getMessage("irc.keyrequest") + "\n" );
		String str = getInput();
		System.out.println("INPUT GOTTEN (line)\n");

	}
	
}