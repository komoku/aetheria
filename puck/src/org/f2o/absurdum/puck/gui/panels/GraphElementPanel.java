/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 19:40:27
 * as file GraphElementPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 19:40:27
 */
public class GraphElementPanel extends JPanel 
{

	private String id;
	
	private GraphEditingPanel gep;
	
	static
	{
		new BackgroundInitThread().start();
	}
	
	public GraphElementPanel ( )
	{
		super();
		this.id = PanelIDGenerator.newID();
	}
	
	public void setGraphEditingPanel ( GraphEditingPanel gep )
	{
		this.gep = gep;
	}
	
	//define things to get graph information from gep
	//when overloading this in subclasses.
	//for example, get room names for a combo box.
	public void linkWithGraph()
	{
		;
	}
	
	//called when going to show the panel.
	public void refresh()
	{
		;
	}
	
	public GraphEditingPanel getGraphEditingPanel ()
	{
		return gep;
	}
	
	public String getID ( )
	{
		return id;
	}
	
	//nombre para poner en el grafo.
	public String getNameForElement()
	{
		return "";
	}
		
	
	//cache
	public static boolean CACHE = true;
	
	private boolean initted = false;
	private org.w3c.dom.Node cachedNode = null;
	
	public boolean isCacheEnabled()
	{
		return CACHE && !(this instanceof WorldPanel);
	}
	
	/**
	 * Stores time taken by the last three background initializations of panels
	 * (initializations triggered by the background init thread).
	 * This is used to adjust the rate at which background initializations take place, so that it adapts to the machine's current observed speed so as not to clog the Swing event dispatch thread too much.
	 * We start with very conservative values (supposing an initialization for a panel takes 1 whole second) to ensure that we don't clog slow machines. The values will then automatically update to smaller ones when we have actual data showing that initializations are fasater.
	 */
	private static long lastBackgroundInitTimes[] = new long[] {1000,1000,1000};
	
	/**Index of the next element of lastBackgroundInitTimes that will be modified*/
	private static int nextBackgroundInitTimeIndex = 0; 
	
	synchronized public final void forceRealInitFromXml ( final boolean blocking )
	{
		if ( isCacheEnabled() && !initted && cachedNode != null )
		{
			try
			{
				
				//moved to background init thread:
				//if ( !blocking )
				//	wait(100); //give time to other threads!
				
				Runnable r = new Runnable(){
					public void run()
					{
						long time = 0;
						if ( !blocking) //take starting point for time measurement to track how background initializations are doing and adjust the delay between them
							time = System.currentTimeMillis();
						synchronized(GraphElementPanel.this)
						{
							if ( cachedNode != null ) //might be made null by another thread during the previous wait
							{
								if ( GraphElementPanel.this instanceof ArrowPanel
										&& !((ArrowPanel)GraphElementPanel.this).hasSourceAndDestination()
								) //happens if we remove (for example via del key) the source or destination of the arrow while
									//deferred loads are being executed.
									return;
									
								doInitFromXML(cachedNode);
								initted = true;
								cachedNode = null;
								if ( GraphElementPanel.this instanceof ArrowPanel ) ((ArrowPanel)GraphElementPanel.this).forceRealCustomRelationshipsInitFromXML ( );
							}
						}
						if ( !blocking )
						{
							time = System.currentTimeMillis() - time; //measure time taken by the background initialization
							//System.err.println("Time: " + time + " [" + GraphElementPanel.this + "]");
							lastBackgroundInitTimes[nextBackgroundInitTimeIndex] = time; //update the array of times
							nextBackgroundInitTimeIndex++; //cycle index for next array update
							if ( nextBackgroundInitTimeIndex == lastBackgroundInitTimes.length ) nextBackgroundInitTimeIndex = 0;
						}
					} };
				if ( blocking )
				{
					if ( SwingUtilities.isEventDispatchThread() )
						r.run();
					else
						SwingUtilities.invokeAndWait(r);
				}
				else
					SwingUtilities.invokeLater ( r );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			
		}
	}
	
	public void setVisible( boolean visible )
	{
		if ( visible ) forceRealInitFromXml(true);
		super.setVisible(visible);
	}
	
	
	public static void emptyQueue()
	{
		cachedNotInitted.clear();
	}
	
	private static LinkedBlockingQueue cachedNotInitted = new LinkedBlockingQueue(); 
	
	//rename to queueInitFromXML
	public final void initFromXML ( org.w3c.dom.Node n )
	{
		if ( isCacheEnabled() )
		{
			synchronized(this)
			{
				cachedNode = n;
			}
			//synchronized(cachedNotInitted)
			{
				cachedNotInitted.offer(this);
			}
		}
		else
			doInitFromXML ( n );
	}
	
	static class BackgroundInitThread extends Thread
	{
		public void run()
		{
			this.setPriority(Thread.MIN_PRIORITY);
			for(;;)
			{
				try
				{

					GraphElementPanel g;

					g = (GraphElementPanel) cachedNotInitted.take();
					//System.out.println("Took " + g + ", " + cachedNotInitted.size() + " remaining.");
					synchronized(g)
					{
						if ( !g.initted )
						{
							g.forceRealInitFromXml(false); //this launches, via invokeAndWait(), the GUI initialization of that panel
							
							//calculate delay
							long delayTime = 0;
							for ( int i = 0 ; i < lastBackgroundInitTimes.length ; i++ )
								delayTime += lastBackgroundInitTimes[i];
							delayTime /= lastBackgroundInitTimes.length;
							delayTime += 30; //have to wait the estimated average time taken by initializations, plus extra slack so that the event dispatching thread can dispatch events
							//System.err.println("Will wait " + delayTime + "ms");
							g.wait(delayTime); //give time to other threads
						}
					}
				}
				catch ( InterruptedException ie )
				{
					ie.printStackTrace();
				}
			}
		}
	}
	
	//end cache stuff
	

	
	public final void initMinimal()
	{
		if ( isCacheEnabled() && !initted && cachedNode != null  )
		{
			Element e = (Element) cachedNode;
	
			doInitMinimal(e);
		}
		doInitMinimal();
		
	}
	
	
	public final org.w3c.dom.Node getXML(Document d)
	{
		//if ( isCacheEnabled() && !initted && cachedNode != null )
		//	return d.importNode(cachedNode, true);
		//else
		forceRealInitFromXml(true); //we have to get updated relationship names, etc.
		return doGetXML(d);
	}
	
	
	//override with code to init the minimal stuff to be able to show
	//the graph
	public void doInitMinimal ( org.w3c.dom.Node e )
	{
		;
	}
	
	//override with code to init the minimal stuff to be able to show
	//the graph
	public void doInitMinimal ( )
	{
		;
	}
	
	//override with code to init panel from xml
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
		;
	}

	//override with code to get xml from panel
	public org.w3c.dom.Node doGetXML ( org.w3c.dom.Document d )
	{
		return null;
	}
	
}
