/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;



import javax.swing.*;
import javax.swing.Timer;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;
import java.awt.*;

import javax.swing.text.*;

import net.miginfocom.swing.MigLayout;

import eu.irreality.age.i18n.UIMessages;
import eu.irreality.age.swing.ColorFadeInTimer;
import eu.irreality.age.swing.FancyAttributeSet;
import eu.irreality.age.swing.FancyJTextField;
import eu.irreality.age.swing.FancyJTextPane;
import eu.irreality.age.swing.FontSizeTransform;
import eu.irreality.age.swing.FontUtils;
import eu.irreality.age.swing.IconLoader;
import eu.irreality.age.swing.ImagePanel;
import eu.irreality.age.swing.SmoothScrollTimer;
import eu.irreality.age.swing.config.AGEConfiguration;
import eu.irreality.age.windowing.AGEClientWindow;
import eu.irreality.age.windowing.MenuMnemonicOnTheFly;

import eu.irreality.age.swing.FancyStyledDocument;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;


public class ColoredSwingClient implements MultimediaInputOutputClient, MouseWheelListener
{

	private FancyJTextField elCampoTexto;
	private FancyJTextPane elAreaTexto;
	private JScrollPane elScrolling;
	private boolean scrollPaneAtBottom = true; //this is automatically set to true if scroll pane is at bottom, false otherwise
	private boolean smoothScrolling = true; //whether scrolling should be smooth
	private SmoothScrollTimer smoothScrollTimer; //timer for smooth scrolling
	private SwingEditBoxListener elEscuchador;	
	private Vector gameLog;
	private AGEClientWindow laVentana;
	
	private boolean textFadeIn = false; //whether we apply text fade-in
	private int textFadeInDuration = 500; //duration of text fade-in (if it's applied)
	
	private boolean showTextEffects = AGEConfiguration.getInstance().getBooleanProperty("cscTextFx"); //whether we show text effects - if false, they won't be shown even if textFadeIn/smoothScrolling are set to true

	//for colored output
	private Document doc;
	private MutableAttributeSet atributos = new FancyAttributeSet();
	
	private Color textFieldForeground = Color.black; //colour of the text field when it's responding. the text field will turn to the inactive colour sometimes in real time, then go back to this colour
	private Color textFieldInactiveForeground = Color.red; //colour of the text field when it's not responding, because stuff is loading, or for example in real-time mode it's not our time to enter another command yet.
	private Color keyRequestForeground = Color.black; //colour of the text field in the "waitkeypress" state.
	

	//if deactivated, all input-getting methods will automatically return no input
	//use this only when exiting the client
	private boolean deactivated = false;
	
	
	private String currentInput;
	
	//memoria (doskey)
	private Stack back = new Stack();
	private Stack forward = new Stack();
	
	private VisualConfiguration vc;
	
	private SoundClient sonido;
	
	private Hashtable colorCodesTable = new Hashtable();
	
	//to avoid slowness on log loading for very long logs, we only show the last maxLogCharactersShown characters.
	private int maxLogCharactersShown = 100000;
	
	/**
	 * If set to true, the text area won't autoscroll to the bottom when text is added.
	 * Autoscrolling to the bottom is nice for sighted used, but not so for the blind using screen readers, 
	 * since they have to manually scroll back so that their readers read the new text.
	 */
	private boolean accessibleScrollMode = AGEConfiguration.getInstance().getBooleanProperty("cscBlindAcc");
	
	/**
	 * This font size transform will be applied in all the calls to setCurrentOutputFont(). 
	 */
	private FontSizeTransform fontTransform = null;
	
	public boolean isSoundEnabled()
	{
		return true; //m�s tarde dar opciones, sound on/off, etc.
	}
	
	//en proxys de clientes remotos, p.ej, se devolver�a un SoundClientProxy.
	public SoundClient getSoundClient()
	{
		if ( sonido == null )
			sonido = new AGESoundClient(); //hacerlo Singleton?
		return sonido;	
	}
	
	public FancyJTextPane getTextArea()
	{
		return elAreaTexto;
	}
	
	public FancyJTextField getInputField()
	{
		return elCampoTexto;
	}
	
	public JScrollPane getScrollPane()
	{
		return elScrolling;
	}
	
	/**
	 * Will be disconnected if the window is closing.
	 */
	public boolean isDisconnected()
	{
		return deactivated;
	}
	
	public String getColorCode ( String colorType )
	{
		if ( colorType == null ) return "";
		String code = (String) colorCodesTable.get(colorType);
		if ( code == null ) return "";
		else return code;
	}
	
	/**
	 * Deactivate the client, from now on it will always return null immediately on input requests.
	 * This happens when the window is closed.
	 */
	public synchronized void exit()
	{
		deactivated = true;
		if ( this.getSoundClient() != null )
		{
			this.getSoundClient().stopAllSound();
			if ( this.getSoundClient() instanceof AGESoundClient )
			{
			    ((AGESoundClient)this.getSoundClient()).deactivate();			
			}
		}
		
		notifyAll(); //if we are waiting for input, we just exit
    	
		//stop smooth scroll timer if it's running
		if ( smoothScrollTimer != null && smoothScrollTimer.isRunning() )
    		smoothScrollTimer.stop();
	}
	
	
	private void reformatAllText() //into default foreground color, sadly
	{
		try
		{
			String text = elAreaTexto.getText();
			elAreaTexto.getDocument().remove(0,elAreaTexto.getDocument().getLength());
			write(text);
		}
		catch ( BadLocationException ble )
		{
			System.out.println(ble);
		}
	}
	
	
	public void setDefaultConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","%00CC00%");
		colorCodesTable.put("important","%FFFF00%");
		colorCodesTable.put("information","%FFFF00%");
		colorCodesTable.put("action","%9999FF%");
		colorCodesTable.put("denial","%CC0000%");
		colorCodesTable.put("error","%FF0000%");
		colorCodesTable.put("story","%FFFFFF%");
		colorCodesTable.put("default","%FFFFFF%");
		colorCodesTable.put("input","%AAAAAA%");
		colorCodesTable.put("reset","% %");
		elAreaTexto.setBackground ( Color.black );
		laVentana.getMainPanel().setBackground( elAreaTexto.getBackground() );
		elAreaTexto.setForeground ( Color.white );
		StyleConstants.setForeground(atributos,Color.black);
		StyleConstants.setForeground(atributos,Color.white);
		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
		elCampoTexto.setFont(SwingAetheriaGameLoaderInterface.font.deriveFont((float)24.0));
		
		reformatAllText();
		
		elAreaTexto.repaint();
	}
	



	public void setPergaminoConfiguration ( )
	{
		//color configuration
		colorCodesTable.put("description","%006600%");
		colorCodesTable.put("important","%555500%");
		colorCodesTable.put("information","%555500%");
		colorCodesTable.put("action","%0000FF%");
		colorCodesTable.put("denial","%C00000%");
		colorCodesTable.put("error","%800000%");
		colorCodesTable.put("story","%000001%");
		colorCodesTable.put("default","%000001%");
		colorCodesTable.put("input","%555555%");
		colorCodesTable.put("reset"," ");
		elAreaTexto.setBackground ( new Color ( 255 , 255 , 211 ) );
		laVentana.getMainPanel().setBackground( elAreaTexto.getBackground() );
		elAreaTexto.setForeground ( Color.black );
		StyleConstants.setForeground(atributos,Color.black);
		
		
		//set goudy medieval where available
		
		String fontName = "Lucida Handwriting Cursiva";
		float fontSize = (float) 16.0;
		Font laFuente = SwingAetheriaGameLoaderInterface.font;
		laFuente = new Font("Serif",Font.PLAIN,(int)fontSize);
			Font[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			Font fuenteElegida;
			for ( int f = 0 ; f < fuentes.length ; f++ )
			{
				System.out.println("FONT: " + fuentes[f].getFontName());
				if ( fuentes[f].getFontName().equalsIgnoreCase(fontName) )
				{
					laFuente = fuentes[f].deriveFont((float)fontSize);
					break;
				}
			}
		elAreaTexto.setFont(laFuente);
		elCampoTexto.setFont(laFuente.deriveFont((float)24.0));
		
		reformatAllText();
		
		elAreaTexto.repaint();
	}
	
	public void setVisualConfiguration ( VisualConfiguration c )
	{
		Enumeration keys = c.getColorKeys();
		while ( keys.hasMoreElements() )
		{
			String key = (String) keys.nextElement();
			colorCodesTable.put(key,c.getColorCode(key));
			System.out.println(getClass()+" putting " + " " + key + " " + c.getColorCode(key));
		}
		elAreaTexto.setBackground ( c.getBackgroundColor() );
		elAreaTexto.setForeground ( c.getForegroundColor() );
		laVentana.getMainPanel().setBackground ( c.getBackgroundColor() ); 
		StyleConstants.setForeground(atributos,c.getForegroundColor());
		elAreaTexto.repaint();
		Font laFuente = c.getFont();
		laFuente = FontUtils.applyKerningAndLigaturesIfPossible(laFuente);
		elAreaTexto.setFont(laFuente);
		//System.err.println("Size matters: " + c.getFont().getSize());
		//elAreaTexto.setFont(c.getFont().deriveFont((float)24.0));
		//elAreaTexto.repaint();
		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(laFuente);
		setCurrentOutputFont(laFuente);
		StyleConstants.setFontFamily((MutableAttributeSet)atributos,laFuente.getFamily());
		StyleConstants.setFontSize((MutableAttributeSet)atributos,laFuente.getSize());
		elCampoTexto.setFont(laFuente.deriveFont((float)24.0));
		this.vc = c;
	}
	
	public VisualConfiguration getVisualConfiguration ( )
	{
		if ( vc != null ) return vc;
		else return new VisualConfiguration();
	}
	
	
	public void addToBackStack ( String s )
	{
		//System.out.println("Addin' string " + s + ": " + ( !(back.isEmpty() || s.equalsIgnoreCase((String)back.peek())) && !s.equalsIgnoreCase("") ) );
		if ( ! ( !back.isEmpty() && s.equalsIgnoreCase((String)back.peek()) ) )
			back.push(s);
	}
	
	public void addToForwardStack ( String s )
	{
		if ( !( !forward.isEmpty() && s.equalsIgnoreCase((String)back.peek())) )
			forward.push(s);
	}
	
	public void forwardStackIntoBackStack ( )
	{
		while ( !forward.isEmpty() )
		{
			Object o = forward.pop();
			if ( !o.equals("") )
				back.push(o);
		}
	}
	
	public void goBack (  )
	{
		if ( ! back.isEmpty() )
		{
			//System.out.println("Back is not empty");
			addToForwardStack ( elCampoTexto.getText() );
			elCampoTexto.setText( (String)back.pop() );
		}
	}
	
	public void goForward (  )
	{
		if ( ! forward.isEmpty() )
		{
			addToBackStack ( elCampoTexto.getText() );
			elCampoTexto.setText( (String)forward.pop() );
		}
	}
	
	public void refreshFocus()
	{
		SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	          elCampoTexto.requestFocus();
	    }
		});
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String)} instead
	 */
	public void escribirTitulo ( String s )
	{
		writeTitle(s);
	}

	public void writeTitle ( String s )
	{
		laVentana.setTitle(s);
	}
	
	/**
	 * @deprecated Use {@link #writeTitle(String,int)} instead
	 */
	public void escribirTitulo ( String s , int pos )
	{
		writeTitle(s, pos);
	}

	public void writeTitle ( String s , int pos )
	{
		String titAct = laVentana.getTitle();
		StringTokenizer st = new StringTokenizer ( titAct, "-" );
		int npos = 1;
		String result="";
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			if ( npos == pos )
			{
				result += s;
			}
			else
			{
				result += tok.trim();
			}
			npos++;
			if ( st.hasMoreTokens() ) result += " - ";
		}
		if ( npos == pos )
		{
			result += (" - " + s);
		}
		laVentana.setTitle(result);
	
	}
	
	public boolean isColorEnabled()
	{
		return true;
	}
	
	public boolean isMemoryEnabled()
	{
		return true;
	}
	
	public boolean isLoggingEnabled()
	{
		return true;
	}
	
	public boolean isTitleEnabled()
	{
		return true;
	}
	
	/**
	 * Unused:
	 */
	public ColoredSwingClient ( AGEClientWindow window , FancyJTextField nCampo , JScrollPane scrolling, FancyJTextPane nArea , Vector gameLog )
	{
		laVentana = window;
		elCampoTexto=nCampo;
		elAreaTexto=nArea;	
		elScrolling=scrolling;
		this.gameLog = gameLog;
		elEscuchador = new SwingEditBoxListener ( elCampoTexto ,  gameLog , this );
		elCampoTexto.addActionListener ( elEscuchador );
		elCampoTexto.addKeyListener ( elEscuchador );
		doc = elAreaTexto.getDocument();
		this.setDefaultConfiguration();
		
		initClientMenu(laVentana);

		
		
		
	}
	
	public void uninitClientMenu ( final AGEClientWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doUninitClientMenu(window);
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
								doUninitClientMenu(window);
								window.repaint();
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
	
	public void initClientMenu ( final AGEClientWindow window )
	{
		if ( SwingUtilities.isEventDispatchThread() )
			doInitClientMenu(window);
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
								doInitClientMenu(window);
								window.repaint();
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
	
	public void doUninitClientMenu ( final AGEClientWindow window )
	{
		JMenuBar mb = window.getTheJMenuBar();
		mb.remove(clientConfigurationMenu);
	}
	
	private JMenu clientConfigurationMenu = new JMenu( UIMessages.getInstance().getMessage("csclient.pres") );
	
	private void increaseFontSize()
	{
		setFontZoomFactor(1.2 * getFontZoomFactor());
	}
	
	private void decreaseFontSize()
	{
		setFontZoomFactor(getFontZoomFactor()/1.2);
	}
	
	//llamado por los constructores
	public void doInitClientMenu ( final AGEClientWindow window )
	{
		JMenu colorConfigurationMenu = new JMenu( UIMessages.getInstance().getMessage("csclient.pres.colorthemes") );
		JMenu fontSizeMenu = new JMenu ( UIMessages.getInstance().getMessage("csclient.pres.fontsize") );
		final JCheckBoxMenuItem fullScreenOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.fullscreen") ,window.isFullScreenMode());
		final JCheckBoxMenuItem soundOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.sound") ,true);
		final JCheckBoxMenuItem textEffectsOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.textfx") , showTextEffects ); //true by default, saved btw. executions
		final JCheckBoxMenuItem accessibleScrollOption = new JCheckBoxMenuItem( UIMessages.getInstance().getMessage("csclient.pres.blindacc") , accessibleScrollMode ); //false by default, saved btw. executions
		JMenuBar mb = window.getTheJMenuBar();
		window.setTheJMenuBar(mb); //n�tese el "the", es para que la tenga como atributo. Si luego se quita para el modo fullscreen se puede volver a poner.
		clientConfigurationMenu.add ( colorConfigurationMenu );
		clientConfigurationMenu.add ( fontSizeMenu );
		if ( window.supportsFullScreen() )
			clientConfigurationMenu.add ( fullScreenOption );
		clientConfigurationMenu.add(soundOption);
		clientConfigurationMenu.add(textEffectsOption);
		clientConfigurationMenu.add(accessibleScrollOption);
		
		JRadioButtonMenuItem itemDefaultJuego = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.game") ,true);
		JRadioButtonMenuItem itemDefault = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.age") ,false);
		JRadioButtonMenuItem itemPergamino = new JRadioButtonMenuItem( UIMessages.getInstance().getMessage("csclient.theme.parchment") ,false);
		ButtonGroup bg = new ButtonGroup();
		bg.add ( itemDefaultJuego );
		bg.add ( itemDefault );
		bg.add ( itemPergamino );
		itemDefault.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setDefaultConfiguration();
			}
		} );
		itemDefaultJuego.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( window.getWorld() != null )
				{
					VisualConfiguration vc = ( window.getWorld().getVisualConfiguration() );
					if ( vc != null ) 
					{
						setVisualConfiguration ( vc );
						reformatAllText();
					}
				}
			}
		} );
		itemPergamino.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setPergaminoConfiguration();
			}
		} );
		
		JMenuItem itemLargerFontSize = new JMenuItem( UIMessages.getInstance().getMessage("csclient.pres.fontsize.larger") );
		JMenuItem itemDefaultFontSize = new JMenuItem( UIMessages.getInstance().getMessage("csclient.pres.fontsize.default") );
		JMenuItem itemSmallerFontSize = new JMenuItem( UIMessages.getInstance().getMessage("csclient.pres.fontsize.smaller") );
		itemLargerFontSize.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				increaseFontSize();
			}
		} );
		itemSmallerFontSize.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				decreaseFontSize();
			}
		} );
		itemDefaultFontSize.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				setFontZoomFactor(1.0);
			}
		} );
		
		
		fullScreenOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( fullScreenOption.isSelected() )
				{
					window.setFullScreenMode(true);
				}
				else
				{
					window.setFullScreenMode(false);
				}
			}
		} );
		soundOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( soundOption.isSelected() )
				{
					AGESoundClient asc = (AGESoundClient)getSoundClient();
					asc.activate();
				}
				else
				{
					AGESoundClient asc = (AGESoundClient)getSoundClient();
					asc.deactivate();
				}
			}
		} );
		textEffectsOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( textEffectsOption.isSelected() )
				{
					enableTextEffects();
					AGEConfiguration.getInstance().setProperty("cscTextFx","true");
				}
				else
				{
					disableTextEffects();
					AGEConfiguration.getInstance().setProperty("cscTextFx","false");
				}
			}
		} );
		accessibleScrollOption.addActionListener ( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				if ( accessibleScrollOption.isSelected() )
				{
					accessibleScrollMode = true;
					AGEConfiguration.getInstance().setProperty("cscBlindAcc","true");
				}
				else
				{
					accessibleScrollMode = false;
					AGEConfiguration.getInstance().setProperty("cscBlindAcc","false");
				}
			}
		} );
		
		colorConfigurationMenu.add ( itemDefaultJuego );
		colorConfigurationMenu.add ( itemDefault );
		colorConfigurationMenu.add ( itemPergamino );
		
		fontSizeMenu.add ( itemSmallerFontSize );
		fontSizeMenu.add ( itemDefaultFontSize );
		fontSizeMenu.add ( itemLargerFontSize );
		
		mb.add ( clientConfigurationMenu );
		
		MenuMnemonicOnTheFly.setMnemonics(mb);
		
		window.repaint();
	}
	
	//crea �l los componentes, a�adi�ndolos a la ventana S.A.G.L. dada
	public ColoredSwingClient ( AGEClientWindow window , Vector gameLog )
	{
		laVentana = window;
		this.gameLog = gameLog;
		
		//crear todo lo necesario para el cliente, agregandolo a la ventana
		
		laVentana.getMainPanel().setLayout ( new BorderLayout() );
		
		laVentana.update(laVentana.getGraphics());
		laVentana.repaint();
		laVentana.updateNow();
					
		//Thread.yield();

		elAreaTexto = new FancyJTextPane();
		elAreaTexto.getAccessibleContext().setAccessibleDescription(UIMessages.getInstance().getMessage("accessible.ageoutput"));
					
		laVentana.updateNow();

		elScrolling = new JScrollPane ( elAreaTexto );
		elScrolling.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		//elScrolling.setHorizontalScrollBarPolicy ( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		elScrolling.setBorder(BorderFactory.createEmptyBorder());
		elScrolling.setViewportBorder(BorderFactory.createEmptyBorder());
		elScrolling.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
        {
			
			int lastYPosInView = 0;
			
            public void adjustmentValueChanged(AdjustmentEvent event)
            {
            	if (event.getValueIsAdjusting()) return;
            	
                JScrollBar  vbar = (JScrollBar) event.getSource();
                
                boolean wentUp = false;
                int newYPosInView = elScrolling.getViewport().getViewPosition().y;
                if ( newYPosInView < lastYPosInView ) wentUp = true;
                lastYPosInView = newYPosInView;                                
                
                if ((vbar.getValue() + vbar.getVisibleAmount()) == vbar.getMaximum())
                	scrollPaneAtBottom = true;
                else if ( wentUp ) //this is required because the scrollpane could be temporarily not at the bottom due to more text being added, and due to scrolling being handled
                					//with invokeLater() and taking a moment to actually take place. But in this case, the scroll pane actually counts as being at the bottom
                					//(i.e. we don't have to stop autoscrolling)
                {
                	scrollPaneAtBottom = false;
                	//if we're doing smooth scrolling, we stop it
                	if ( smoothScrollTimer != null && smoothScrollTimer.isRunning() )
                		smoothScrollTimer.stop();
                }
             
            }
        });
		smoothScrollTimer = new SmoothScrollTimer ( 20 , this ); //timer for smooth scrolling
		smoothScrollTimer.setSpeed(300);
		smoothScrollTimer.setMovementMode(SmoothScrollTimer.ADAPTIVE_SPEED_MODE);
		setSmoothScrolling(true); //smooth scrolling by default
		
		elAreaTexto.setForeground(java.awt.Color.white);
		elAreaTexto.setBackground(java.awt.Color.black);
		laVentana.getMainPanel().setBackground(java.awt.Color.black);
			
		//elAreaTexto.setContentType("text/html; charset=iso-8859-1");			

		elAreaTexto.setFont(SwingAetheriaGameLoaderInterface.font);
		elAreaTexto.setMargin(new Insets(20,20,10,20));			
					
		elAreaTexto.setVisible(true);
		elScrolling.setVisible(true);
				

		laVentana.getMainPanel().add(elScrolling,BorderLayout.CENTER);
					
		elCampoTexto = new FancyJTextField(200);
		elCampoTexto.getAccessibleContext().setAccessibleDescription(UIMessages.getInstance().getMessage("accessible.ageinput"));
		
		/*
		JPanel promptPanel = new JPanel();
		JLabel promptLabel = new JLabel("Prompt: ");
		promptPanel.add(promptLabel);
		promptPanel.setOpaque(false);
		elCampoTexto.add(promptPanel);
		promptPanel.setVisible(true);
		promptLabel.setVisible(true);
		System.err.println("Prompt created");
		*/

		elCampoTexto.setFont(SwingAetheriaGameLoaderInterface.getFont().deriveFont((float)24.0));
		
		elCampoTexto.setVisible(true);
		laVentana.getMainPanel().add(elCampoTexto,"South");
					
		elCampoTexto.requestFocus();
					
		elCampoTexto.setCaretColor(Color.red);
					
		laVentana.addFocusListener( new FocusListener( )
		{
		    public void focusGained ( FocusEvent evt )
			{
				elCampoTexto.requestFocus();
			}
			public void focusLost ( FocusEvent evt )
			{
							;
			}
		}
		);
		
		elAreaTexto.setEditable(false);
		
		/*
		elAreaTexto.addFocusListener ( new FocusListener ( ) 
		{
			public void focusGained ( FocusEvent evt )
			{
			    Thread thr = new Thread()
			    {
				public void run()
				{
				    try
				    {
					Thread.sleep(2000);
				    } 
				    catch (InterruptedException e)
				    {
					e.printStackTrace();
				    }
				    SwingUtilities.invokeLater( new Thread() 
				    {
					public void run()
					{
					    elCampoTexto.requestFocus();
					}
				    }
				    );
				}
			    };
			    thr.start();
			}
			public void focusLost ( FocusEvent evt )
			{
							;
			}
		});
		*/
		
		elAreaTexto.addMouseListener( new MouseAdapter()
		{
			public void mouseReleased ( MouseEvent evt )
			{
				
				try
				{
					Clipboard clipboard = elAreaTexto.getToolkit().getSystemClipboard();
					String selection = elAreaTexto.getSelectedText();
					clipboard.setContents(new StringSelection(selection),null);	
				}
				catch ( SecurityException se )
				{
					//Running on an applet, can't access the clipboard. Not a big deal.
					;
				}
				
				elCampoTexto.requestFocus();
			}
		}
		);
		
		
		
				
		//addMenus(laVentana);
					
		laVentana.setVisible(true);			
		laVentana.repaint();
		laVentana.updateNow();

		//Thread.yield();
					
		//gameLog = new Vector(); //init game log
		
		elEscuchador = new SwingEditBoxListener ( elCampoTexto ,  gameLog , this );
		elCampoTexto.addActionListener ( elEscuchador );
		elCampoTexto.addKeyListener ( elEscuchador );
		elAreaTexto.setText("--");
		doc = elAreaTexto.getDocument();
		
		elAreaTexto.addMouseWheelListener(this); //zoom con rueda
		
		
		initClientMenu(laVentana);
		
		laVentana.updateNow();
		
	}
	

	


	//pasa strings de tipo "000000" o "#000000" a color
	//null si no reconocido
	/*
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean;
			if ( colorString.length() > 0 && colorString.charAt(0) == '#' )
				colorClean = colorString.substring(1);
				else colorClean = colorString;
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}
	*/
	
	//gets a Color from a #HHHHHH, HHHHHH, %#HHHHHH% or %HHHHHH% format string
	//null if string unrecognized
	public static java.awt.Color stringToColor ( String colorString )
	{
		try 
		{
			String colorClean = colorString;
			if ( colorClean.charAt(0) == '%')
				colorClean = colorClean.substring(1,colorClean.length()-1);
			if ( colorClean.length() > 0 && colorClean.charAt(0) == '#' )
				colorClean = colorClean.substring(1);
				int ncolor = Integer.parseInt(colorClean,16);
				return new Color ( ncolor );
			}
			catch ( NumberFormatException nfe )
			{
				//unrecognized
				return null;
			}	
	}

	public static String colorToString ( Color color )
	{
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		return (Integer.toString(red,16) + Integer.toString(green,16) + Integer.toString(blue,16));
		
	}


	/**
	 * @deprecated Use {@link #write(String)} instead
	 */
	public void escribir ( String s )
	{
		write(s);
	}
	
	
	public boolean scrollIsAtBottom()
	{
		Rectangle r = elScrolling.getViewport().getViewRect();
		Dimension sz = elAreaTexto.getPreferredSize();
		return ( r.y + r.height >= sz.getHeight() ); //stopping criterion?
	}

	
	//private StringBuffer rawText = new StringBuffer(""); //this attribute stores all the text written to the text area WITH color codes included
	
	public void write ( String s )
	{
	    
	    //System.err.println("Selectionan gaems " + elAreaTexto.getSelectionStart() + " " + elAreaTexto.getSelectionEnd() );
		
		if ( s == null )
		{
			write("null");
			return;
		}
		
		//if scroll pane is at bottom we'll need scrolling if we add more lines of text
		boolean needScroll = scrollPaneAtBottom;
				
		//rawText.append(s);
	
		//parse color codes
		StringTokenizer st = new StringTokenizer ( s , "%" , true );
		String lastDelimiterString = "";
		boolean iscode = (s.length()>0 && s.charAt(0)=='%');
		while ( st.hasMoreTokens() )
		{
			String tok = st.nextToken();
			if ( tok.startsWith("%") )
			{
				//save delimiter string in case this is not a code, and we need to write that string
				lastDelimiterString += tok;
				continue;
			}
			//System.out.println("Token: " + tok );
			if ( iscode )
			{
			//	System.out.println("Code token " + tok );
				if ( tok.equalsIgnoreCase("red") )
					StyleConstants.setForeground(atributos,Color.red);
				else if ( tok.equalsIgnoreCase("green") )
					StyleConstants.setForeground(atributos,Color.green);
				else if ( tok.equalsIgnoreCase("white") )
					StyleConstants.setForeground(atributos,Color.white);
				else if ( tok.equalsIgnoreCase("blue") )
					StyleConstants.setForeground(atributos,Color.blue);	
				else if ( tok.equalsIgnoreCase("yellow") )
					StyleConstants.setForeground(atributos,Color.yellow);
				else if ( tok.equalsIgnoreCase("lightgray") )
					StyleConstants.setForeground(atributos,Color.lightGray);
				else if ( tok.equalsIgnoreCase("magenta") )
					StyleConstants.setForeground(atributos,Color.magenta);	
				else if ( tok.trim().equalsIgnoreCase("") )
				{
					System.out.println(getColorCode("default"));
					System.out.println(stringToColor(getColorCode("default")));
					StyleConstants.setForeground(atributos,stringToColor(getColorCode("default"))); //default
				}
				else
				{
					try 
					{
						String colortok;
						if ( tok.length() > 0 && tok.charAt(0) == '#' )
							colortok = tok.substring(1);
						else colortok = tok;
						int ncolor = Integer.parseInt(colortok,16);
						StyleConstants.setForeground( atributos , new Color ( ncolor ) );
						//System.out.println("Ncolor" + ncolor);
					}
					catch ( NumberFormatException nfe )
					{
						//unrecognized: the string is not a colour code. It's probably a percent symbol that appeared in normal text. So we just write.
						tok = lastDelimiterString + tok;
						iscode = false;
					}
				}
				
		
			}
			if ( !iscode ) //this is not equivalent to an else because iscode can be set to false by an exception above.
			{
				try
				{	
					int savedCaret = 0;
					
					if ( accessibleScrollMode )
					{
						savedCaret = elAreaTexto.getCaretPosition(); //save caret position
						elAreaTexto.setCaretPosition(elAreaTexto.getText().length()); //move caret to end of document (in non-accessible mode it will already be at end w/o doing this, in accessible mode it won't because caret is placed by default before last sentence printed, and who knows where the screen reader will put it)
					}	
						
					//doc.insertString(elAreaTexto.getText().length(),tok,atributos);
					insertTextAtEnd ( tok , atributos );
					
					if ( accessibleScrollMode ) elAreaTexto.setCaretPosition(savedCaret); //restore saved caret position for screen readers																									
				}
				catch ( Exception ble )
				{
					System.out.println(ble);
				}
			}
			
			//reset delim string
			//if ( !tok.startsWith("%") )
			//{
			lastDelimiterString = "";
			//}
			
			//change state
			iscode = !iscode;
			
		}
		
		
		/*
	
		old, noncolored output
	
		elAreaTexto.append(s);
		//elScrolling.getVerticalScrollBar().setValue(elScrolling.getVerticalScrollBar().getMaximum());	
		*/
		
		elAreaTexto.setCaretColor(Color.red);
		
		//System.out.println(elAreaTexto.getText());
		
		//elAreaTexto.moveCaretPosition(elAreaTexto.getText().length());
		
		//the old scrolling - worked until 2011-09-20, but had some problems with margins
		//elAreaTexto.setCaretPosition(elAreaTexto.getText().length()); //this doesn't select
		
		//better scrolling
		//elScrolling.revalidate();
		
		//scroll the JScrollPane to the bottom
		//if ( needsScroll && ! scrollIsAtBottom() )
		
		if ( needScroll && !processingLog )
		{
			scrollToBottom();
		}
		
	}
	
	/**
	 * This method is called by the write method to actually insert a text at the end of the text area.
	 * @param text
	 * @param atributos
	 */
	private void insertTextAtEnd ( final String text , final MutableAttributeSet atributos )
	{
		try
		{
			final int insertPosition = elAreaTexto.getText().length();
			
			//boolean textFadeIn = true;
			
			if ( textFadeIn && showTextEffects && !processingLog )
			{
				//SimpleAttributeSet invisible = atributos.copyAttributes();
				//StyleConstants.setForeground(colorAttrToApply,this.getTextArea().getBackground());
				//sd.setCharacterAttributes(ColorFadeInTimer.this.offset, ColorFadeInTimer.this.length, colorAttrToApply, false);
				
				execInDispatchThread( new Runnable()
				{
					public void run()
					{
						SimpleAttributeSet colorAttrToApply = new SimpleAttributeSet();
						Color targetColor = StyleConstants.getForeground(atributos);
						Color initialColor = new Color ( targetColor.getRed() , targetColor.getGreen() , targetColor.getBlue() , 0 /*transparent*/ );
						StyleConstants.setForeground(colorAttrToApply,initialColor);
						try 
						{
							doc.insertString(insertPosition,text,atributos);
						} 
						catch (BadLocationException e) 
						{
							e.printStackTrace();
						}
						((StyledDocument)doc).setCharacterAttributes(insertPosition,text.length(),colorAttrToApply,false);
						ColorFadeInTimer colorTimer = new ColorFadeInTimer(20,ColoredSwingClient.this,insertPosition,text.length(),targetColor,textFadeInDuration);
						colorTimer.start();
					}
				} );
				
				
			}
			else
			{
				doc.insertString(insertPosition,text,atributos);
			}
			

		}
		catch ( Exception ble )
		{
			System.err.println(ble);
		}
	}
	
	public boolean isSmoothScrolling()
	{
		return smoothScrolling;
	}
	
	public void setSmoothScrolling ( boolean smoothScrolling )
	{
		this.smoothScrolling = smoothScrolling;
	}
	
	/**
	 * @return Whether the text fade-in effect is currently active. If so, text written to the client will be slowly faded in.
	 */
	public boolean isTextFadeIn()
	{
		return textFadeIn;
	}
	
	/**
	 * Activates/deactivates the text fade-in effect. If active, text written to the client will be slowly faded in.
	 * @param textFadeIn The new value (true for activated, false for deactivated) of the text fade-in effect.
	 */
	public void setTextFadeIn ( boolean textFadeIn )
	{
		this.textFadeIn = textFadeIn;
	}
	
	/**
	 * @return The millisecond duration of the text fade-in effect, i.e., the milliseconds the text takes to get its full color.
	 */
	public int getTextFadeInDuration ( )
	{
		return textFadeInDuration;
	}
	
	/**
	 * @param duration The new duration (in milliseconds) of the text fade-in effect.
	 */
	public void setTextFadeInDuration ( int duration )
	{
		textFadeInDuration = duration;
	}
	
	/**
	 * @return The speed of smooth text scrolling (if active) in pixels per second.
	 */
	public int getScrollSpeed ()
	{
		return smoothScrollTimer.getSpeed();
	}
	
	public void setScrollSpeed ( int pixelsPerSecond )
	{
		smoothScrollTimer.setSpeed(pixelsPerSecond);
	}
	
	public void setScrollLinesPerSecond ( double linesPerSecond )
	{
		smoothScrollTimer.setLinesPerSecond(linesPerSecond);
	}
	
	public static final int INSTANT_SCROLLING = -1;
	public static final int FIXED_SPEED_SMOOTH_SCROLLING = SmoothScrollTimer.FIXED_SPEED_MODE;
	public static final int ADAPTIVE_SPEED_SMOOTH_SCROLLING = SmoothScrollTimer.ADAPTIVE_SPEED_MODE;
	
	public int getScrollMode ( )
	{
		if ( !smoothScrolling ) return INSTANT_SCROLLING;
		return smoothScrollTimer.getMovementMode();
	}
	
	public void setScrollMode ( int newMode )
	{
		if ( newMode == INSTANT_SCROLLING ) smoothScrolling = false;
		else
		{
			smoothScrolling = true;
			smoothScrollTimer.setMovementMode( newMode );
		}
	}
	
	private void fastScrollToBottom()
	{
	
		//Nah. Slowpoke. Ugly:
		/*
		execInDispatchThread(new Runnable() 
		{ 
			public void run() 
			{ 
				elAreaTexto.scrollRectToVisible(new Rectangle(0,(int)elAreaTexto.getPreferredSize().getHeight(),10,10));
				//elAreaTexto.setVisible(true);
				elAreaTexto.repaint();
			} 
		});
		*/
		
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{				
				//ensures that the preferred size that follows is up to date
				elAreaTexto.revalidate();
				
				//this is slow in the first execution after loading a very large log that inserts a lot of stuff into the text area:
				elAreaTexto.scrollRectToVisible(new Rectangle(0,(int)elAreaTexto.getPreferredSize().getHeight(),10,10));
				
				//System.err.println("Pref size " + elAreaTexto.getPreferredSize().getHeight());
				//System.err.println("Len " + elAreaTexto.getDocument().getLength());
				//new Throwable().printStackTrace();
				
				//this not really faster - it also calls BoxView.setSize, BoxView.layout, etc. on the inside.
				//if ( !elScrolling.isValid() ) elScrolling.validate();
				//BoundedRangeModel brm = elScrolling.getVerticalScrollBar().getModel();
				//brm.setValue(brm.getMaximum());
				
				elAreaTexto.repaint();
			}
		});
	}
	
	private void smoothScrollToBottom()
	{
		if ( !smoothScrollTimer.isRunning() ) //we could be scrolling already
			smoothScrollTimer.start();
	}
	
	/**
	 * Scrolls the JScrollPane associated to the client to the bottom, i.e., showing the latest text that has been added.
	 */
	private void scrollToBottom()
	{
		
		if ( processingLog ) return; //we are processing the log, we don't need to scroll because component is not visible. We'll scroll when the log is done (then we'll call fastScrollToBottom() once).
		
		if ( !smoothScrolling || !showTextEffects )
		{
			fastScrollToBottom();
		}
		else
		{
			smoothScrollToBottom();
			
		}

	}
	
	public void insertIcon ( Icon icon )
	{
	
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
					
		System.out.println("Icon Insert.\n");
		elAreaTexto.insertIcon ( icon ); //I think java auto-preloads.
	
	}
	
	public void insertIcon ( URL location ) { insertIcon ( IconLoader.loadIcon(location) ); }
	public void insertIcon ( String filename ) { insertIcon ( new ImageIcon(filename) ); }
	
	public void insertCenteredIcon ( Icon icon )
	{
		
		elAreaTexto.setSelectionStart(elAreaTexto.getText().length());
		elAreaTexto.setSelectionEnd(elAreaTexto.getText().length());
	
		JPanel jp = new JPanel();
		jp.setBackground ( elAreaTexto.getBackground() );
		FlowLayout fl = new FlowLayout();
		fl.setAlignment( FlowLayout.CENTER );
		jp.add ( new JLabel ( icon ) );
		elAreaTexto.insertComponent(jp);
	
	}
	
	public void insertCenteredIcon ( URL location ) { if ( location == null ) return; insertCenteredIcon ( IconLoader.loadIcon( location ) ); }
	public void insertCenteredIcon ( String fileName ) { if ( fileName == null ) return; insertCenteredIcon ( new ImageIcon ( fileName ) ); }
	
	public boolean isGraphicsEnabled()
	{
		return true;
	}
	

	public void loguear ( String s )
	{
		gameLog.addElement ( s );
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
		//2010-06-25: We add this if, behind-the-scenes forced commands should not be added to logs.
		//(e.g. if "leer cartel" forces "mirar cartel", we only need to log "leer cartel",
		//not both - "mirar cartel" will be executed behind the scenes anyway):
		if ( output_enabled )
			gameLog.addElement ( s );
		
		elEscuchador.countCommand();
		if ( output_enabled )
		{	
			//the following if makes the client not write the echo line when we are loading a log of a real time game and we find empty inputs caused by player waits.
			//I think the condition Thread.currentThread() instanceof GameEngineThread is unnecessary (always should hold) but I write it just in case, to avoid ClassCastException in the next condition.
			//if ( ! ( processingLog && Thread.currentThread() instanceof GameEngineThread && ((GameEngineThread)Thread.currentThread()).isRealTimeEnabled() && s.length() == 0 ) )
			//the following if makes the client not write empty echo lines in logs in general, not only in real time games. Switch to the line above if we want this in real time only.
			if ( ! ( processingLog && s.length() == 0 ) )
			{
				write("\n");
				write( getColorCode("input") + echoText + s.trim() + getColorCode("reset") + "\n" );
			}
		}
		
	}
	
	
	
	public synchronized void waitKeyPress ()
	{
		if ( deactivated ) return;
		//System.err.println("[DN] waitKeyPress() called");
		
		//System.out.println("Keywait");
		elEscuchador.setPressAnyKeyState ( true );
		//System.out.println("Keywait flag set");
		//ponemos un color de fondo algo mas claro
		//Color c1 = elAreaTexto.getBackground();
		//Color c2 = c1.brighter();
		
		//elAreaTexto.setBackground( c2  );

		//System.err.println("[DN] PAK state set: " + elEscuchador.press_any_key + ", going to wait");
		
		try
		{
			//System.out.println("Keywait I said");
			wait();
			//System.out.println("Keywait I yelled");
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);	
		}
		//notified
		
		//System.err.println("[DN] waitKeyPress() returning");
		//elAreaTexto.setBackground( c1 );
		
	}
	
	/**
	 * Sets the client to way for a key press for a given number of milliseconds.
	 * The client continues executing in a normal way when the player presses a key or the milliseconds are elapsed, whatever happens first.
	 * @param millis Milliseconds to wait.
	 */
	public synchronized void waitKeyPress ( long millis )
	{
		if ( deactivated ) return;
		elEscuchador.setPressAnyKeyState(true);
		try
		{
			wait(millis);
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
		}
		//here either we have been notified (triggered by player keypress), or millis milliseconds have elapsed.
		//in the latter case, we need to return from the "press any key" state (in the former case, we have already done it).
		elEscuchador.returnFromPressAnyKeyStateIfNeeded();
	}
	
	
	//bloqueante.
	public synchronized String getInput(Player pl)
	{
	
		//System.err.println("[DN] getInput called");
		if ( deactivated ) return null;
		showAfterLogLoad();
		
		setActiveColor();
	
		try
		{
			//System.err.println("[DN] getInput going to wait");
			wait();
		}
		catch ( InterruptedException intex )
		{
			System.out.println(intex);
		}
		//notified, y cambiado currentInput
		
		//System.err.println("[DN] getInput no longer waiting");
		
		String temp = currentInput ;
		
		currentInput = null;
		
		setInactiveColor();
		
		//System.err.println("[DN] getInput returning " + temp);
		return temp;
		
		
	}
	
	private void setTextFieldForeground ( final Color color )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elCampoTexto.setForeground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elCampoTexto.setForeground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setActiveColor()
	{
		setTextFieldForeground(textFieldForeground);
	}
	
	public void setInactiveColor()
	{
		setTextFieldForeground(textFieldInactiveForeground);
	}
	
	/**
	 * Returns colour in which the "press any key" text is to be drawn.
	 * @return
	 */
	public Color getKeyRequestForeground ( )
	{
		return keyRequestForeground;
	}
	
	/**
	 * Sets colour in which the "press any key" text is to be drawn.
	 */
	public void setKeyRequestForeground ( Color color )
	{
		keyRequestForeground = color;
	}	

	/**
	 * Returns the colour that the input field text is configured to assume when inactive.
	 */
	public Color getInputFieldInactiveForeground ( )
	{
		return textFieldInactiveForeground;
	}
	
	/**
	 * Sets the colour that the input field text should assume when inactive.
	 * This method never changes the field text's colour immediately, changes will only take place in the UI next time the field becomes inactive
	 * (i.e. when the game engine thread is simulating things). 
	 */
	public void setInputFieldInactiveForeground ( final Color color )
	{
		textFieldInactiveForeground = color;
	}
	
	//no bloqueante.
	public synchronized String getRealTimeInput ( Player pl)  
	{
		if ( deactivated ) return null;
		showAfterLogLoad();
		
		String temp = currentInput;
		setActiveColor();
		currentInput = null;
		if ( temp == null ) //log a noop
			loguear("");
		else
			setInactiveColor();
		return temp;
	}
	
	public synchronized void setInputString ( String s )
	{
		currentInput = s;
		notify();
	}
	
	/**
	 * @deprecated Use {@link #clearScreen()} instead
	 */
	public void borrarPantalla ( )
	{
		clearScreen();
	}

	public void clearScreen ( )
	{
		elAreaTexto.setText("");
	}
	
	
	
	//echo configuration
	private boolean echoEnabled = true;
	//private String echoText = "Tu orden:  ";
	private String echoText = UIMessages.getInstance().getMessage("your.command") + "  ";
	
	public boolean isEchoEnabled()
	{
		return echoEnabled;
	}
	
	public void setEchoEnabled ( boolean echoEnabled )
	{
		this.echoEnabled = echoEnabled;
	}
	
	public String getEchoText()
	{
		return echoText;
	}
	
	public void setEchoText( String echoText )
	{
		this.echoText = echoText;
	}
	
	//key press text
	private String keyRequestText = UIMessages.getInstance().getMessage("csclient.keyrequest");
		//"Pulsa cualquier tecla...";
	
	public String getKeyRequestText()
	{
		return keyRequestText;
	}
	
	public void setKeyRequestText ( String newText )
	{
		keyRequestText = newText;
	}
	
	//frame management
	private ImagePanel topFrame = null;
	private ImagePanel bottomFrame = null;
	private ImagePanel leftFrame = null;
	private ImagePanel rightFrame = null;
	
	//new frame management
	private Map framesById = Collections.synchronizedMap(new HashMap());
	private Map frameIdsByName = Collections.synchronizedMap(new HashMap());
	
	public void removeFrames()
	{
		if ( isDisconnected() ) return;
		try {
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					doRemoveFrames();
				}
			}
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void doRemoveFrames()
	{
		topFrame = null;
		bottomFrame = null;
		leftFrame = null;
		rightFrame = null;
		
		framesById.clear();
		frameIdsByName.clear();
		
		//get innermost panel
		Container current = elAreaTexto.getParent();
		while ( !(current instanceof JPanel) )
			current = current.getParent();
		laVentana.setMainPanel((JPanel)current);
		
		laVentana.getMainPanel().revalidate();
		refreshFocus();
	}
	
	public void addFrame ( final int position , final int size )
	{
		if ( isDisconnected() ) return;
		try {
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					doAddFrame(position,size);
				}
			}
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * New method that adds a frame with a size specification for MigLayout.
	 * This admits percentages, for example.
	 * @param position
	 * @param sizeSpec
	 */
	public void addFrame ( final int position , final String sizeSpec )
	{
		if ( isDisconnected() ) return;
		try {
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					doAddFrame(position,sizeSpec);
				}
			}
			);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * New method that adds a frame with a String for identification and a complete specification for MigLayout, and returns the (new) frame ID, or -1 if the frame hasn't been created.
	 * @param position
	 * @param sizeSpec
	 */
	public int addFrame ( final String frameName , final String sizeSpec )
	{
		if ( isDisconnected() ) return -1;
		try 
		{
			final int[] returnValue = new int[1]; //workaround because we can't mod a variable inside invokeAndWait directly
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					returnValue[0] = doAddFrame(frameName,sizeSpec);
				}
			}
			);
			return returnValue[0];
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			return -1;
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	public int splitFrame ( final String parentFrameName , final String childName1 , final String childSpecs1 , final String childName2 , final String childSpecs2 )
	{
		if ( isDisconnected() ) return -1;
		try 
		{
			final int[] returnValue = new int[1]; //workaround because we can't mod a variable inside invokeAndWait directly
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					returnValue[0] = doSplitFrame(parentFrameName,childName1,childSpecs1,childName2,childSpecs2);
				}
			}
			);
			return returnValue[0];
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			return -1;
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	
	public void doAddFrame ( int position , int size )
	{
		if ( position == ImageConstants.TOP )
		{
			JPanel newMainPanel = new JPanel();
			topFrame = new ImagePanel();
			topFrame.setBackground(elAreaTexto.getBackground());
			topFrame.setPreferredSize(new Dimension(laVentana.getMainPanel().getWidth(),size));
			topFrame.setMaximumSize(new Dimension(10000,size));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.PAGE_AXIS));
			newMainPanel.add(topFrame);
			newMainPanel.add(laVentana.getMainPanel());
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(topFrame,BorderLayout.NORTH);
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			laVentana.setMainPanel(newMainPanel);
			
		}
		else if ( position == ImageConstants.BOTTOM )
		{
			JPanel newMainPanel = new JPanel();
			bottomFrame = new ImagePanel();
			bottomFrame.setBackground(elAreaTexto.getBackground());
			bottomFrame.setPreferredSize(new Dimension(laVentana.getMainPanel().getWidth(),size));
			bottomFrame.setMaximumSize(new Dimension(10000,size));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.PAGE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(bottomFrame);
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			newMainPanel.add(bottomFrame,BorderLayout.SOUTH);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.LEFT )
		{
			JPanel newMainPanel = new JPanel();
			leftFrame = new ImagePanel();
			leftFrame.setBackground(elAreaTexto.getBackground());
			leftFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			leftFrame.setMaximumSize(new Dimension(size,100000));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(leftFrame);
			newMainPanel.add(laVentana.getMainPanel());
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(leftFrame,BorderLayout.WEST);
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.RIGHT )
		{
			JPanel newMainPanel = new JPanel();
			rightFrame = new ImagePanel();
			rightFrame.setBackground(elAreaTexto.getBackground());
			rightFrame.setPreferredSize(new Dimension(size,laVentana.getMainPanel().getHeight()));
			rightFrame.setMaximumSize(new Dimension(size,100000));
			/*
			newMainPanel.setLayout(new BoxLayout(newMainPanel,BoxLayout.LINE_AXIS));
			newMainPanel.add(laVentana.getMainPanel());
			newMainPanel.add(rightFrame);
			*/
			newMainPanel.setLayout(new BorderLayout());
			newMainPanel.add(laVentana.getMainPanel(),BorderLayout.CENTER);
			newMainPanel.add(rightFrame,BorderLayout.EAST);
			laVentana.setMainPanel(newMainPanel);
		}
		laVentana.getMainPanel().revalidate();
		//((JComponent)laVentana).revalidate();
		refreshFocus();
	}
	
	
	private int getUnusedFrameId()
	{
		int k = 10;
		while ( getFrame(k) != null ) k++;
		return k;
	}
	
	/**
	 * Adds a frame according to the given MigLayout specs, returns its frame ID and gives it a name.
	 * @param specs
	 * @return
	 */
	public int doAddFrame ( String frameName , String specs ) /*returns frameId*/
	{
		if ( frameName != null && frameNameToId(frameName) >= 0 ) return -1; //frame name already taken
		int frameId = getUnusedFrameId();
		ImagePanel newFrame = new ImagePanel();
		framesById.put(new Integer(frameId),newFrame);	
		if ( frameName != null ) frameIdsByName.put(frameName, new Integer(frameId));
		JPanel newMainPanel = new JPanel();
		newFrame.setBackground(elAreaTexto.getBackground());
		
		newMainPanel.setLayout(new MigLayout("fill"));
		newMainPanel.add(laVentana.getMainPanel(),"dock center, height 10%:100%:100%, width 10%:100%:100%");
		newMainPanel.add(newFrame, specs);
		
		laVentana.setMainPanel(newMainPanel);
		laVentana.getMainPanel().revalidate();
		refreshFocus();
		return frameId;
	}
	
	/**
	 * Splits a frame into two new frames, following MigLayout specifications.
	 * @param parentFrameName Name of the frame to be split.
	 * @param childName1 Name of the first frame resulting from the split.
	 * @param childSpecs1 Layout specifications for the first frame resulting from the split (e.g. "north, width 50%")
	 * @param childName2 Name of the second frame resulting from the split.
	 * @param childSpecs2 Layout specifications for the second frame resulting from the split (e.g. "south, width 50%")
	 * @return The numeric ID of the first child frame. The ID of the second child will be this value plus 1.
	 */
	public int doSplitFrame ( String parentFrameName , String childName1 , String childSpecs1 , String childName2 , String childSpecs2 )
	{
		if ( childName1 != null && frameNameToId(childName1) >= 0 ) return -1; //child name 1 already taken
		if ( childName2 != null && frameNameToId(childName2) >= 0 ) return -1; //child name 2 already taken
		if ( parentFrameName != null && frameNameToId(parentFrameName) < 0 ) return -1; //parent frame does not exist
		ImagePanel parentFrame = getFrame(frameNameToId(parentFrameName));
		ImagePanel childFrame1 = new ImagePanel();
		ImagePanel childFrame2 = new ImagePanel();
		int childId1 = getUnusedFrameId();
		framesById.put(new Integer(childId1),childFrame1);	
		int childId2 = getUnusedFrameId();
		framesById.put(new Integer(childId2),childFrame2);	
		if ( childName1 != null ) frameIdsByName.put(childName1, new Integer(childId1));
		if ( childName2 != null ) frameIdsByName.put(childName2, new Integer(childId2));
		childFrame1.setBackground(elAreaTexto.getBackground());
		childFrame2.setBackground(elAreaTexto.getBackground());
		
		parentFrame.setLayout(new MigLayout("fill"));
		parentFrame.add(childFrame1,childSpecs1);
		parentFrame.add(childFrame2,childSpecs2);
		parentFrame.revalidate();
		refreshFocus();
		return childId1;
	}
	
	
	public void doAddFrame ( int position , String sizeSpec )
	{
		if ( position == ImageConstants.TOP )
		{
			JPanel newMainPanel = new JPanel();
			topFrame = new ImagePanel();
			topFrame.setBackground(elAreaTexto.getBackground());

			newMainPanel.setLayout(new MigLayout("fill"));
			newMainPanel.add(topFrame, "north, height " + sizeSpec);
			newMainPanel.add(laVentana.getMainPanel(),"dock center, height 10%:100%:100%");
			laVentana.setMainPanel(newMainPanel);
			
		}
		else if ( position == ImageConstants.BOTTOM )
		{
			JPanel newMainPanel = new JPanel();
			bottomFrame = new ImagePanel();
			bottomFrame.setBackground(elAreaTexto.getBackground());

			newMainPanel.setLayout(new MigLayout("fill"));
			newMainPanel.add(laVentana.getMainPanel(),"dock center, height 10%:100%:100%");
			newMainPanel.add(bottomFrame,"south, height " + sizeSpec);
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.LEFT )
		{
			JPanel newMainPanel = new JPanel();
			leftFrame = new ImagePanel();
			leftFrame.setBackground(elAreaTexto.getBackground());

			newMainPanel.setLayout(new MigLayout("fill"));
			newMainPanel.add(leftFrame,"west, width " + sizeSpec);
			newMainPanel.add(laVentana.getMainPanel(),"dock center, width 10%:100%:100%");
			laVentana.setMainPanel(newMainPanel);
		}
		else if ( position == ImageConstants.RIGHT )
		{
			JPanel newMainPanel = new JPanel();
			rightFrame = new ImagePanel();
			rightFrame.setBackground(elAreaTexto.getBackground());

			newMainPanel.setLayout(new MigLayout("fill"));
			newMainPanel.add(laVentana.getMainPanel(),"dock center, width 10%:100%:100%");
			newMainPanel.add(rightFrame,"east, width " + sizeSpec);
			laVentana.setMainPanel(newMainPanel);
		}
		laVentana.getMainPanel().revalidate();
		//((JComponent)laVentana).revalidate();
		refreshFocus();
	}
	
	
	/**
	 * Obtains the image frame at a given position or frame ID.
	 * @param position
	 * @return
	 */
	public ImagePanel getFrame ( int position )
	{
		switch ( position )
		{
			case ImageConstants.TOP : return topFrame;
			case ImageConstants.BOTTOM : return bottomFrame;
			case ImageConstants.LEFT : return leftFrame;
			case ImageConstants.RIGHT : return rightFrame;
			default : return (ImagePanel) framesById.get(new Integer(position));
		}
	}
	
	public ImagePanel getFrame ( String frameName )
	{
		int id = frameNameToId ( frameName );
		if ( id < 0 ) return null;
		return getFrame(id);
	}
	
	private int frameNameToId ( String frameName )
	{
		Integer result = (Integer) frameIdsByName.get(frameName);
		if ( result == null ) return -1;
		else return result.intValue();
	}
	
	public void showImageInFrame ( String fileName , int position )
	{
		showImageInFrame ( fileName , position , ImagePanel.NO_SCALING );
	}
	
	public void showImageInFrame ( URL location , int position )
	{
		showImageInFrame ( location , position , ImagePanel.NO_SCALING );
	}
	
	public void showImageInFrame ( URL location , String frameName )
	{
		showImageInFrame ( location , frameNameToId(frameName) );
	}
	
	public void showImageInFrame ( Icon icon , int position , int scalingMode )
	{
		if ( deactivated ) return;
		ImagePanel theFrame = getFrame ( position );
		if ( theFrame == null ) addFrame ( position , 200 );
		if ( icon == null )
		{
			System.err.println("Called showImageInFrame on a null image. Tried to open nonexistent image file?");
			return;
		}
		theFrame = getFrame ( position ); //{not null}
		theFrame.setImage(icon);
		theFrame.setScalingMode(scalingMode);
		theFrame.repaint();
	}
		
	public void showImageInFrame ( String filename , int position , int scalingMode )
	{
	    //TODO: support svg here (IconLoader.loadIcon).
		showImageInFrame ( new ImageIcon(filename) , position , scalingMode );
	}
	
	public void showImageInFrame ( URL location , int position , int scalingMode )
	{
		showImageInFrame ( IconLoader.loadIcon(location) , position , scalingMode );
	}
	
	public void showImageInFrame ( URL location , String frameName , int scalingMode )
	{
		showImageInFrame ( location , frameNameToId(frameName) , scalingMode );
	}
	
	public void showImageInBackground ( String fileName )
	{
		if ( fileName == null ) 
			elAreaTexto.setBackgroundImage(null);
		else
			//TODO: g.setClip(rect.x,rect.y,rect.width,getMargin().top);
			elAreaTexto.setBackgroundImage(new ImageIcon(fileName));
	}
	
	public void showImageInBackground ( URL location )
	{
		if ( deactivated ) return;
		if ( location == null ) 
			elAreaTexto.setBackgroundImage(null);
		else
			elAreaTexto.setBackgroundImage(IconLoader.loadIcon(location));
	}
	
	
	
	public void useImage ( URL url , int mode , int location , int scaling )
	{
		if ( mode == ImageConstants.INLINE )
		{
			if ( location == ImageConstants.LEFT )
				insertIcon(url);
			else 
				insertCenteredIcon(url);
		}
		else if ( mode == ImageConstants.BACKGROUND )
			showImageInBackground(url);
		else if ( mode == ImageConstants.FRAME )
			showImageInFrame(url,location,scaling);
	}
	
	public void useImage ( URL url , int mode , String frameName , int scaling )
	{
		useImage ( url , mode , frameNameToId(frameName) , scaling );
	}
	
	public void useImage ( String fileName , int mode , int location , int scaling )
	{
		useImage ( fileToURL(fileName) , mode , location , scaling );
	}
	
	public void useImage ( String fileName , int mode , int location ) 
	{ 
		useImage(fileName,mode,location,ImageConstants.NO_SCALING);
	}
	
	public void useImage ( URL url , int mode , int location ) 
	{ 
		useImage(url,mode,location,ImageConstants.NO_SCALING);
	}
	
	public void useImage ( URL url , int mode , String frameName ) 
	{ 
		useImage(url,mode,frameName,ImageConstants.NO_SCALING);
	}
	
	public void useImage ( String fileName , int mode )
	{
		useImage(fileToURL(fileName),mode);
	}
	
	public void useImage ( URL url , int mode )
	{
		if ( mode == ImageConstants.FRAME )
			useImage(url,mode,ImageConstants.TOP);
		else
			useImage(url,mode,ImageConstants.CENTER);
	}
	
	public static URL fileToURL ( String filename )
	{
		try 
		{
			return new File(filename).toURI().toURL();
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void setPrompts ( final String left , final String right )
	{
		if ( isDisconnected() ) return;
		try 
		{
			SwingUtilities.invokeAndWait( new Runnable() 
			{
				public void run()
				{
					elCampoTexto.setPrompts(left,right);
				}
			}
			);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static Color parseColor ( String colorString )
	{
		String colorHex;
		if ( colorString.length() > 0 && colorString.charAt(0) == '#' )
			colorHex = colorString.substring(1);
		else colorHex = colorString;
		int ncolor = Integer.parseInt(colorHex,16);
		return new Color ( ncolor );
	}	
	
	/**
	 * High-level method that sets the default colour for the input field.
	 * Do not confuse with setTextFieldForeground which momentarily changes the colour (ignoring the default colour if necessary).
	 * @param color
	 */
	public void setInputFieldForeground ( final Color color )
	{
		textFieldForeground = color;
		setTextFieldForeground(color);
	}
	
	public void setInputFieldBackground ( final Color color )
	{
		if ( isDisconnected() ) return;
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elCampoTexto.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elCampoTexto.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setOutputAreaBackground ( final Color color )
	{
		if ( isDisconnected() ) return;
		if ( SwingUtilities.isEventDispatchThread() )
		{
			elAreaTexto.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { elAreaTexto.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setScrollBarBackground ( final Color color )
	{
		if ( isDisconnected() ) return;
		final JScrollBar jsb = elScrolling.getVerticalScrollBar();
		if ( SwingUtilities.isEventDispatchThread() )
		{
			jsb.setBackground(color);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { jsb.setBackground(color); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Changes the font to use for the output area.
	 * @param f New font to use for the output area.
	 */
	public void setOutputAreaFont ( final Font f )
	{
		execInDispatchThread ( new Runnable() { public void run() { elAreaTexto.setFont(f); } } );
	}
	
	
	/**
	 * Changes the factor by which all fonts are scaled.
	 * @param factor
	 */
	public void setFontZoomFactor ( final double factor )
	{
		
		execInDispatchThread ( new Runnable() { public void run() { 
			
			//calculate ratio that we are applying to the old zoom factor
			double oldFactor = getFontZoomFactor();
			double ratio = factor / oldFactor;
			
			//if ratio > 1, the font size transformation may make us "lose" the bottom, so we will need scrolling if we're at the bottom.
			boolean needScrolling = false;
			if ( scrollPaneAtBottom && ratio > 1.0 ) needScrolling = true;
			
			//scale existing fonts (the past)
			elAreaTexto.scaleFonts(ratio);
			
			//add the transform so that it affects future font changes (the future)
			fontTransform = new FontSizeTransform ( FontSizeTransform.MULTIPLY , factor );
			
			//change the current output font (the present)
			Font currentFont = ((FancyAttributeSet)atributos).getFont();
			//setCurrentOutputFont(currentFont); //it may seem that we're setting the same, but no, because the transform we just set will be applied
			//nah, this won't work with cumulative upgrades - it doesn't take ratio into account
			int newSize = (int)(currentFont.getSize() * ratio);
			Font f = currentFont.deriveFont((float)newSize);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
			((FancyAttributeSet)atributos).setFont(f);
			StyleConstants.setFontFamily((MutableAttributeSet)atributos,f.getFamily());
			StyleConstants.setFontSize((MutableAttributeSet)atributos,f.getSize());
		
			if ( needScrolling ) fastScrollToBottom(); //if ratio > 1, the font size transformation may make us "lose" the bottom.
			
		} } );
		
	}
	
	/**
	 * Obtains the factor by which all fonts are scaled.
	 */
	public double getFontZoomFactor ( )
	{
		if ( fontTransform == null || fontTransform.getType() != FontSizeTransform.MULTIPLY ) return 1.0;
		else return fontTransform.getAmount();
	}
	
	
	/**
	 * Changes the font to use for new text outputted to the output area.
	 *  @param f New font to use for new text outputted to the output area.
	 */
	public void setCurrentOutputFont ( final Font font )
	{
		//System.err.println("sCOF called w/f. size " + font.getSize());
		//write("Setting current output font: " + f + "\n");
		//new Throwable().printStackTrace();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
		execInDispatchThread ( new Runnable() { public void run() { 
			Font f = font;
			if ( fontTransform != null ) //apply size transform
			{
				//System.err.println("Changing current output font from " + f.getSize() + " to the fryoleer of " + fontTransform.apply(f.getSize()));
				f = f.deriveFont((float)fontTransform.apply(f.getSize()));
			}
			elAreaTexto.setFont(f); //for some unknown reason, this makes kerning work (if it has been applied to the font e.g. via FontUtils).
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
			((FancyAttributeSet)atributos).setFont(f);
			StyleConstants.setFontFamily((MutableAttributeSet)atributos,f.getFamily());
			StyleConstants.setFontSize((MutableAttributeSet)atributos,f.getSize());
			//write("Done the setting to " + f);
			} } );
	}
	
	/**
	 * Sets the output area's font to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setOutputAreaFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			setOutputAreaFont ( fuente.deriveFont((float)fontSize) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the font for new output to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the new output to the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setCurrentOutputFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			Font derived = fuente.deriveFont((float)fontSize);
			setCurrentOutputFont ( derived );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the color for new output.
	 * @param c
	 */
	public void setCurrentOutputColor ( Color c )
	{
		StyleConstants.setForeground(atributos,c);
	}
	
	/**
	 * Sets the bold attribute for new output.
	 * @param c
	 */
	public void setCurrentOutputBold ( boolean bold )
	{
		StyleConstants.setBold(atributos, bold);
	}
	
	/**
	 * Sets the italic attribute for new output.
	 * @param c
	 */
	public void setCurrentOutputItalic ( boolean italic )
	{
		StyleConstants.setBold(atributos, italic);
	}
	
	/**
	 * Changes the font to use for the text input field.
	 * @param f New font to use for the text input field.
	 */
	public void setInputFieldFont ( final Font f )
	{
		execInDispatchThread ( new Runnable() { public void run() { elCampoTexto.setFont(f); } } );
	}
	
	/**
	 * Sets the input field's font to the font read from the specified input stream, with the specified size.
	 * @param is Stream in which we read the font to use for the input field.
	 * @param fontSize Size of the font to use.
	 */
	public void setInputFieldFont ( InputStream is , int fontSize )
	{
		try
		{
			Font fuente = Font.createFont ( Font.TRUETYPE_FONT , is );
			setInputFieldFont ( fuente.deriveFont((float)fontSize) );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the input field's font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the input field.
	 * @param fontSize Size of the font to use.
	 */
	public void setInputFieldFont ( URL u , int fontSize )
	{
		try
		{
			setInputFieldFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Sets the output area's font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the output area.
	 * @param fontSize Size of the font to use.
	 */
	public void setOutputAreaFont ( URL u , int fontSize )
	{
		try
		{
			setOutputAreaFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Sets the output area's new output font to the font read from the specified URL, with the specified size.
	 * @param u URL in which we read the font to use for the text that we output to the output area from now.
	 * @param fontSize Size of the font to use.
	 */
	public void setCurrentOutputFont ( URL u , int fontSize )
	{
		try
		{
			setCurrentOutputFont ( u.openStream() , fontSize );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
	
	public static void execInDispatchThread ( Runnable r )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			r.run();
		}
		else
		{
			try
			{
				//changed from invokeLater, 2011-06-09.
				SwingUtilities.invokeAndWait(r);
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	
	public void setMargins ( final int top , final int left , final int bottom , final int right , final boolean marginsOnView )
	{
		execInDispatchThread(new Runnable() 
		{ 
			public void run() 
			{ 
				elAreaTexto.setMargin(new Insets(top,left,bottom,right)); /*elCampoTexto.setMargin(new Insets(0,left,0,right));*/ 
				elAreaTexto.setMarginsOnViewableArea(marginsOnView);
			} 
		});
	}
	
	public void setMargins ( final int top , final int left , final int bottom , final int right )
	{
		setMargins ( top , left , bottom , right , true );
	}
	
	/**
	 * Sets margins on the input text box.
	 * @param left
	 * @param bottom
	 * @param right
	 */
	public void setInputMargins (  final int left , final int right )
	{
		execInDispatchThread(new Runnable() 
		{ 
			public void run() 
			{ 
				elCampoTexto.setMargin(new Insets(0,left,0,right)); /*elCampoTexto.setMargin(new Insets(0,left,0,right));*/ 
			} 
		});
	}
	
	
	/*
	public void setOutputAreaForeground ( final Color c )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			StyleConstants.setForeground(atributos,c);
		}
		else
		{
			try
			{
				SwingUtilities.invokeLater(new Runnable() { public void run() { StyleConstants.setForeground(atributos,c); } });
			}
			catch ( Exception ie )
			{
				ie.printStackTrace();
			}
		}
	}
	*/
	
	public void setInputFieldBackground ( String s ) { setInputFieldBackground(parseColor(s)); }
	public void setInputFieldForeground ( String s ) { setInputFieldForeground(parseColor(s)); }
	public void setOutputAreaBackground ( String s ) { setOutputAreaBackground(parseColor(s)); }
	//public void setOutputAreaForeground ( String s ) { setOutputAreaForeground(parseColor(s)); }

	public void setScrollBarBackground ( String s ) { setScrollBarBackground(parseColor(s)); }
	
	public void setKeyRequestForeground ( String s ) { setKeyRequestForeground(parseColor(s)); }
	
	
	private boolean processingLog = false;
	
	
	

	
	
	private void trimHistoryIfLong()
	{
		//trim the text history if it got really long
		if ( elAreaTexto.getDocument().getLength() > maxLogCharactersShown )
		{
			try
			{
				elAreaTexto.getDocument().remove(0, elAreaTexto.getDocument().getLength() - maxLogCharactersShown );
				elAreaTexto.getDocument().insertString(0, "(...)\n\n",atributos);
			}
			catch ( BadLocationException ble )
			{
				System.err.println(ble);
			}
		}
	}
	
	/**
	 * We store the main panel here while it is hidden for log load.
	 * Need not match the current main panel.
	 * This is because, during log load, the frame's main panel may change (due to adding frames).
	 * We want to unhide whatever panel *was* the main panel at the time of starting to load the log.
	 */
	private JPanel hiddenMainPanel;
	
	public void hideForLogLoad()
	{
		processingLog = true;
		//if ( laVentana instanceof JFrame )
		//{
			//JPanel glass = (JPanel)((JFrame)laVentana).getGlassPane();
			JPanel glass = new JPanel();
			glass.setBackground(Color.WHITE);
			glass.setOpaque(true);
			//((JFrame)laVentana).setGlassPane(glass);			
			laVentana.setGlassPane(glass);
			glass.removeAll();
			glass.setLayout(new GridLayout(1,1));
			glass.setBackground(Color.WHITE);
			JLabel loadingLabel = new JLabel( UIMessages.getInstance().getMessage("csclient.loadinglog") ,JLabel.CENTER);
			loadingLabel.setBorder(BorderFactory.createLineBorder(Color.black,4));
			loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			loadingLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
			loadingLabel.setHorizontalTextPosition(JLabel.CENTER);
			loadingLabel.setVerticalTextPosition(JLabel.CENTER);
			//loadingLabel.setForeground(this.getVisualConfiguration().getForegroundColor());
			//loadingLabel.setBackground(this.getVisualConfiguration().getBackgroundColor());
			loadingLabel.setFont(loadingLabel.getFont().deriveFont(25.0f));
			glass.add(loadingLabel);
			glass.setVisible(true);
			laVentana.getMainPanel().setVisible(false);
			hiddenMainPanel = laVentana.getMainPanel();
			//laVentana.getMainPanel().setVisible(false);
		//}
	}
	
	public void showAfterLogLoad()
	{
		if ( processingLog )
		{

			
			execInDispatchThread( new Runnable() 
			{
				public void run()
				{
					processingLog = false;
					
					trimHistoryIfLong();
					
					//if ( laVentana instanceof JFrame )
					//{
						//laVentana.getMainPanel().setVisible(true);
						JPanel glass = (JPanel)(/*(JFrame)*/laVentana).getGlassPane();
						hiddenMainPanel.setVisible(true);
						hiddenMainPanel = null;
						elCampoTexto.requestFocusInWindow();
						fastScrollToBottom();
						glass.setVisible(false);
						
						//fastScrollToBottom();
					//}
				}
			}
			);
			

		}
	}
	
	/**
	 * Returns the dimensions of the screen where the window holding the client is displayed, if available.
	 * If unavailable (due to, for example, being in an applet and not having the adequate parameters
	 * or having javascript turned off) then null is returned.
	 * @return
	 */
	public Dimension getScreenSize()
	{
		return laVentana.getScreenSize();
	}
	
	public MutableAttributeSet getTextAttributes()
	{
		return atributos;
	}
	
	/**
	 * This controls the text effects setting in the client menu
	 */
	private void enableTextEffects()
	{
		showTextEffects = true;
	}
	
	/**
	 * This controls the text effects setting in the client menu
	 */
	private void disableTextEffects()
	{
		showTextEffects = false;
	}
	
	
	/**
	 * Zoom with the mouse wheel
	 * @param e
	 */
	public void mouseWheelMoved ( MouseWheelEvent e )
	{
		if ( e.isControlDown() ) //zoom with Ctrl+mouse wheel
		{
			int rotation = e.getWheelRotation(); //negative: up (zoom in), positive: down (zoom out)
			if ( rotation < 0 )
			{
				increaseFontSize();
			}
			else
			{
				decreaseFontSize();
			}
			elAreaTexto.repaint();
			e.consume();
		}
		else
		{
			//keep processing the event. This will reach the scroll pane containing the text area, and trigger the default handling of the mouse wheel event
			//which is scrolling.
			e.getComponent().getParent().dispatchEvent(e);
		}
	}
	
	
	private Dimension storedScrollBarSize = null;
	
	/**
	 * Makes the vertical scroll bar visible or invisible, as requested.
	 * Mouse wheel scrolling will still work with the invisible scroll bar.
	 * @param visible
	 */
	public void setVerticalScrollBarVisible ( final boolean visible )
	{
		execInDispatchThread(new Runnable() 
		{ 
			public void run() 
			{ 
				if ( elScrolling.getVerticalScrollBar() == null ) elScrolling.createVerticalScrollBar();
				if ( !visible )
				{
					storedScrollBarSize = elScrolling.getVerticalScrollBar().getPreferredSize();
					elScrolling.getVerticalScrollBar().setPreferredSize(new Dimension(0,0));
				}
				else
				{
					if ( storedScrollBarSize != null )
						elScrolling.getVerticalScrollBar().setPreferredSize(storedScrollBarSize);
				}
			} 
		});
		
	}
	
}
