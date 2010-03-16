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
	
	synchronized public final void forceRealInitFromXml ( boolean blocking )
	{
		if ( isCacheEnabled() && !initted && cachedNode != null )
		{
			try
			{
				if ( !blocking )
					wait(100); //give time to other threads!
				Runnable r = new Runnable(){
					public void run()
					{
						synchronized(GraphElementPanel.this)
						{
							if ( cachedNode != null ) //might be made null by another thread during the previous wait
							{
								doInitFromXML(cachedNode);
								initted = true;
								cachedNode = null;
								if ( GraphElementPanel.this instanceof ArrowPanel ) ((ArrowPanel)GraphElementPanel.this).forceRealCustomRelationshipsInitFromXML ( );
							}
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
						//System.out.println("Forcing " + g);
							g.forceRealInitFromXml(false);
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
