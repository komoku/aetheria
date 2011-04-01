/*
 * (c) 2005-2009 Carlos Gómez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:31:22
 * as file GraphEditingPanel.java on package org.f2o.absurdum.puck.gui.graph
 */
package org.f2o.absurdum.puck.gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.f2o.absurdum.puck.gui.AddNodeTool;
import org.f2o.absurdum.puck.gui.CopyNodeAction;
import org.f2o.absurdum.puck.gui.CutNodeAction;
import org.f2o.absurdum.puck.gui.DefaultMouseMotionListener;
import org.f2o.absurdum.puck.gui.DeleteArrowAction;
import org.f2o.absurdum.puck.gui.DeleteNodeAction;
import org.f2o.absurdum.puck.gui.PasteNodeAction;
import org.f2o.absurdum.puck.gui.PropertiesPanel;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.RoomPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.Messages;
import org.w3c.dom.Document;


//class 

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:31:22
 */
public class GraphEditingPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener
{
	
	private MouseListener toolListener = null;
	private MouseMotionListener toolMotionListener = null;
	
	private Vector nodes = new Vector(); //of Node
	private Node specialNode;
	private Arrow specialArrow;
	
	private PropertiesPanel propP;
	
	private boolean enableGrid = true;
	private boolean snapToGrid = true;
	
	private WorldNode worldN = new WorldNode(new WorldPanel(this));
	
	/**Experimental**/
	/**For Combo Boxes**/
	private Vector roomNodes = new Vector();
	private Vector itemNodes = new Vector();
	private Vector charNodes = new Vector();
	
	
	private Map nodeListsByClass = new HashMap(); //of Vectors (with room nodes, item nodes, etc.)
	private Map nodeListsByClassN = new HashMap(); //of Vectors (with room nodes, item nodes, etc.) including null
	
	
	public boolean isSnapToGridEnabled()
	{
		return snapToGrid;
	}
	
	public boolean isGridEnabled()
	{
		return enableGrid;
	}
	
	public void setSnapToGrid ( boolean value )
	{
		this.snapToGrid = value;
	}
	
	public void setGrid ( boolean value )
	{
		this.enableGrid = value;
	}
	
	public void clear()
	{
		nodes = new Vector();
		specialNode = null;
		specialArrow = null;
		worldN = new WorldNode(new WorldPanel(this));
		roomNodes = new Vector();
		itemNodes = new Vector();
		charNodes = new Vector();
		resetNodeLists();
		/*
		roomNodes.add(Messages.getInstance().getMessage("none"));
		itemNodes.add(Messages.getInstance().getMessage("none"));
		charNodes.add(Messages.getInstance().getMessage("none"));
		*/
	}
	
	public void resetNodeLists()
	{
		//lists including null
		nodeListsByClassN = new HashMap();
		Vector roomVecN = new Vector();
		roomVecN.add(Messages.getInstance().getMessage("none"));
		nodeListsByClassN.put(RoomNode.class,roomVecN);
		Vector itemVecN = new Vector();
		itemVecN.add(Messages.getInstance().getMessage("none"));
		nodeListsByClassN.put(ItemNode.class,itemVecN);
		Vector charVecN = new Vector();
		charVecN.add(Messages.getInstance().getMessage("none"));
		nodeListsByClassN.put(CharacterNode.class,charVecN);
		Vector spellVecN = new Vector();
		spellVecN.add(Messages.getInstance().getMessage("none"));
		nodeListsByClassN.put(SpellNode.class,spellVecN);
		Vector abstractVecN = new Vector();
		abstractVecN.add(Messages.getInstance().getMessage("none"));
		nodeListsByClassN.put(AbstractEntityNode.class,abstractVecN);
		
		//lists not including null
		nodeListsByClass = new HashMap();
		Vector roomVec = new Vector();
		nodeListsByClass.put(RoomNode.class,roomVec);
		Vector itemVec = new Vector();
		nodeListsByClass.put(ItemNode.class,itemVec);
		Vector charVec = new Vector();
		nodeListsByClass.put(CharacterNode.class,charVec);
		Vector spellVec = new Vector();
		nodeListsByClass.put(SpellNode.class,spellVec);
		Vector abstractVec = new Vector();
		nodeListsByClass.put(AbstractEntityNode.class,abstractVec);
		
	}
	
	public Vector getNodes()
	{
		return nodes;
	}
	
	public Vector getNodes ( Class cl , boolean includeNull )
	{
		if ( includeNull )
			return (Vector) nodeListsByClassN.get(cl);
		else
			return (Vector) nodeListsByClass.get(cl);
	}
	
	public Vector getRoomNodes(boolean includeNull)
	{
		return getNodes(RoomNode.class,includeNull);
		//return (Vector) nodeListsByClass.get(RoomNode.class);
	}
	public Vector getItemNodes(boolean includeNull)
	{
		return getNodes(ItemNode.class,includeNull);
		//return (Vector) nodeListsByClass.get(ItemNode.class);
	}
	public Vector getCharNodes(boolean includeNull)
	{
		return getNodes(CharacterNode.class,includeNull);
		//return (Vector) nodeListsByClass.get(CharacterNode.class);
	}

	public WorldNode getWorldNode()
	{
		return worldN;
	}
	
	public void setWorldNode(WorldNode wn)
	{
		worldN = wn;
	}
	
	
	/**The following method is totally experimental**/
	//unused as of 2009-04-11
	
	public JComboBox buildRoomNamesBox()
	{
		MutableComboBoxModel mcbm = new MutableComboBoxModel()
		{
			Object sel;
			public void removeElementAt(int arg0) {
			}
			public void addElement(Object arg0) {
			}
			public void removeElement(Object arg0) {
			}
			public void insertElementAt(Object arg0, int arg1) {	
			}
			public Object getSelectedItem() {
				return sel;
			}
			public void setSelectedItem(Object arg0) {
				sel = arg0;
			}
			public int getSize() {
				return getRoomNames().size();
			}
			public Object getElementAt(int arg0) {
				return getRoomNames().get(arg0);
			}

			public void addListDataListener(ListDataListener arg0) {
				// TODO Hmm.
			}

			public void removeListDataListener(ListDataListener arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		return new JComboBox ( mcbm );
	}
	
	public PropertiesPanel getPropertiesPanel()
	{
		return propP;
	}
	
	public List getRoomNames()
	{
		ArrayList names = new ArrayList();
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node n = (Node) nodes.get(i);
			if ( n instanceof RoomNode )
			{
				RoomNode rn = (RoomNode) n;
				names.add ( rn.getName() );
			}
		}
		return names;
	}
	
	public void setSpecialNode ( Node n )
	{
		if ( specialNode != null ) specialNode.setHighlighted(false);
		specialNode = n;
		if ( n != null ) n.setHighlighted(true);
	}
	
	public Node getSpecialNode ()
	{
		return specialNode;
	}
	
	public void setSpecialArrow ( Arrow a )
	{
		if ( specialArrow != null ) specialArrow.setHighlighted(false);
		specialArrow = a;
		if ( a != null ) a.setHighlighted(true);
	}
	
	public Arrow getSpecialArrow ()
	{
		return specialArrow;
	}
	
	public void addNode ( Node n )
	{
		nodes.add(n);
		
		Vector nodeVector = (Vector) nodeListsByClass.get(n.getClass());
		Vector nodeVectorN = (Vector) nodeListsByClassN.get(n.getClass());
		if ( nodeVector == null )
		{
			nodeVector = new Vector();
			nodeVector.add( Messages.getInstance().getMessage("none") );
			nodeListsByClass.put(n.getClass(),nodeVector);
		}
		nodeVector.add(n);
		if ( nodeVectorN == null )
		{
			nodeVectorN = new Vector();
			nodeVectorN.add( Messages.getInstance().getMessage("none") );
			nodeListsByClassN.put(n.getClass(),nodeVectorN);
		}
		nodeVectorN.add(n);
		
		/*
		if ( n instanceof RoomNode )
			roomNodes.add(n);
		if ( n instanceof ItemNode )
			itemNodes.add(n);
		if ( n instanceof CharacterNode )
			charNodes.add(n);
		*/
		
		WorldPanel wp = (WorldPanel) this.getWorldNode().getAssociatedPanel();
		wp.updateMaps(n);
		
	}
	
	public void removeNode ( Node n )
	{
		nodes.remove(n);
		
		Vector nodeVector = (Vector) nodeListsByClass.get(n.getClass());
		if ( nodeVector != null )
		{
			nodeVector.remove(n);
		}
		Vector nodeVectorN = (Vector) nodeListsByClassN.get(n.getClass());
		if ( nodeVectorN != null )
		{
			nodeVectorN.remove(n);
		}
			
		WorldPanel wp = (WorldPanel) this.getWorldNode().getAssociatedPanel();
		wp.removeFromMaps(n);
		
	}
	
	public void totallyRemoveNode ( Node n )
	{

		nodes.remove(n);
		
		Vector nodeVector = (Vector) nodeListsByClass.get(n.getClass());
		if ( nodeVector != null )
		{
			nodeVector.remove(n);
		}
		Vector nodeVectorN = (Vector) nodeListsByClassN.get(n.getClass());
		if ( nodeVectorN != null )
		{
			nodeVectorN.remove(n);
		}
		
		//remove arrows pointing to this node
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node no = (Node) nodes.get(i);
			List arrows = no.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				if ( a.getDestination() == n )
					no.removeArrow(a);
			}
		}
		
		WorldPanel wp = (WorldPanel) this.getWorldNode().getAssociatedPanel();
		wp.removeFromMaps(n);
		
	}
	
	private Image buffer;
	
	
	public double mapToPanelX ( double xcoord )
	{
		return (xcoord-viewXOffset)*viewZoom;
	}
	
	public double mapToPanelY ( double ycoord )
	{
		return (ycoord-viewYOffset)*viewZoom;
	}
	
	public int mapToPanelX ( int xcoord )
	{
		return (int) ((xcoord-viewXOffset)*viewZoom);
	}
	
	public int mapToPanelY ( int ycoord )
	{
		return (int)((ycoord-viewYOffset)*viewZoom);
	}
	
	public double panelToMapX ( double xcoord )
	{
		return viewXOffset + xcoord/viewZoom;
	}
	
	public double panelToMapY ( double ycoord )
	{
		return viewYOffset + ycoord/viewZoom;
	}
	
	public int panelToMapX ( int xcoord )
	{
		return (int) ( viewXOffset + xcoord/viewZoom );
	}
	
	public int panelToMapY ( int ycoord )
	{
		return (int) ( viewYOffset + ycoord/viewZoom );
	}
	
	
	private double viewZoom=2.0;
	private double viewXOffset=0;
	private double viewYOffset=0;
	
	private int bufferW = 5000;
	private int bufferH = 5000;
	
	
	/*
	System.out.println("*"+panelToMapX(arg0.getX()));
	System.out.println("#"+getWidth());
	System.out.println(viewXOffset+","+viewYOffset);
	viewXOffset = -this.getWidth()/2/viewZoom + panelToMapX(arg0.getX());
	viewYOffset = -this.getHeight()/2/viewZoom + panelToMapY(arg0.getY());
	if ( viewXOffset < 0 ) viewXOffset = 0;
	if ( viewYOffset < 0 ) viewYOffset = 0;
	System.out.println("*"+panelToMapX(arg0.getX()));
	System.out.println(viewXOffset+","+viewYOffset);
	repaint();
	*/
	
	//public void centerOn ( int map)
	
	
	
	public void multiplyZoom ( double factor )
	{
		double oldZoom = viewZoom;
		viewZoom*=factor;
		
		{
			viewXOffset = viewXOffset + ((1.0/2.0)*this.getWidth()-(1.0/(2.0*factor))*this.getWidth())/oldZoom;
			viewYOffset = viewYOffset + ((1.0/2.0)*this.getWidth()-(1.0/(2.0*factor))*this.getWidth())/oldZoom;			
		}
			
	}
	
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
		
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		if ( buffer == null || buffer.getWidth(this) != this.getWidth() || buffer.getHeight(this) != this.getHeight() )
			buffer = this.createImage ( this.getWidth() , this.getHeight() );
	
		Graphics g2 = buffer.getGraphics();
		
		doPaintComponentInBuffer ( g2 );
		
		g.drawImage(buffer,0,0,Color.RED,this);
		
	}
	
	
	public void paintComponentOld ( Graphics g )
	{
		super.paintComponent(g);
	
		
		//Si el buffer no est� correctamente inicializado, lo (re)creamos
        if ( buffer == null || 
         buffer.getWidth(this) != bufferW || 
         buffer.getHeight(this) != bufferH )
            buffer = this.createImage ( bufferW , bufferH );
		
        Graphics g2 = buffer.getGraphics();
        
		doPaintComponent ( g2 );
	
		g.drawImage(buffer,0,0,this.size().width,this.size().height,(int)viewXOffset,(int)viewYOffset,(int)viewXOffset+(int)(this.size().width/viewZoom),(int)viewYOffset+(int)(this.size().height/viewZoom),Color.BLACK,this);
	
	}
	
	public void doPaintComponentInBuffer ( Graphics g )
	{
		super.paintComponent(g);
		//paint grid
		if ( enableGrid )
		{
			paintGrid( g );
		}
		//paint nodes and arrows
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node n = (Node) nodes.get(i);
			n.paintToView(g,viewZoom,(int)viewXOffset,(int)viewYOffset);
			if ( n.isHighlighted() && !n.isSelected() ) drawNodeHighlight(g,n,new Color(0.8F,0.8F,0.0F,0.2F));
			if ( n.isSelected() ) drawNodeHighlight(g,n,new Color(1.0F,0.6F,0.0F,0.3F));
			List arrows = n.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				a.paintToView(g,viewZoom,(int)viewXOffset,(int)viewYOffset);
				if ( a.isHighlighted() && !a.isSelected() ) drawArrowHighlight(g,a,new Color(0.8F,0.8F,0.0F,0.2F));
				if ( a.isSelected() ) drawArrowHighlight(g,a,new Color(1.0F,0.6F,0.0F,0.3F));
			}
		}
		//paint special node
		if ( specialNode != null )
		{
			specialNode.paintToView(g,viewZoom,(int)viewXOffset,(int)viewYOffset);
			if ( !(specialNode instanceof InvisibleNode) )
			{
    				if ( specialNode.isHighlighted() && !specialNode.isSelected() ) drawNodeHighlight(g,specialNode,new Color(0.8F,0.8F,0.0F,0.2F));
    				if ( specialNode.isSelected() ) drawNodeHighlight(g,specialNode,new Color(1.0F,0.6F,0.0F,0.3F));
			}
			List arrows = specialNode.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				a.paintToView(g,viewZoom,(int)viewXOffset,(int)viewYOffset);
				if ( a.isHighlighted() && !a.isSelected() ) drawArrowHighlight(g,a,new Color(0.8F,0.8F,0.0F,0.2F));
				if ( a.isSelected() ) drawArrowHighlight(g,a,new Color(1.0F,0.6F,0.0F,0.3F));
			}
		}
		//paint special arrow
		if ( specialArrow != null )
		{
			specialArrow.paintToView(g,viewZoom,(int)viewXOffset,(int)viewYOffset);
			if ( specialArrow.isHighlighted() && !specialArrow.isSelected() ) drawArrowHighlight(g,specialArrow,new Color(0.8F,0.8F,0.0F,0.2F));
			if ( specialArrow.isSelected() ) drawArrowHighlight(g,specialArrow,new Color(1.0F,0.6F,0.0F,0.3F));
		}
	}
	
	private void drawNodeHighlight ( Graphics g , Node theNode , Color color )
	{
		int viewXCoord = (int)(( (int) theNode.getBounds().getX() - viewXOffset ) * viewZoom);
		int viewYCoord = (int)(( (int) theNode.getBounds().getY() - viewYOffset ) * viewZoom);
		int viewHeight = (int)(( (int) theNode.getBounds().getHeight() ) * viewZoom);
		int viewWidth = (int)(( (int) theNode.getBounds().getWidth() ) * viewZoom);
		Color old = g.getColor();
		g.setColor(color);
		g.fillOval(viewXCoord-20,viewYCoord-20,(int)(viewHeight+40),(int)(viewWidth+40));
		g.setColor(old);
	}
	
	private void drawArrowHighlight ( Graphics g , Arrow theArrow , Color color )
	{
	    int [] coords = theArrow.getPaintingCoords();
		
	    //transform coords
	    //source x,y
	    coords[0] = (int)(( (int) coords[0] - viewXOffset ) * viewZoom);
	    coords[1] = (int)(( (int) coords[1] - viewYOffset ) * viewZoom);
	    //destination x,y
	    coords[2] = (int)(( (int) coords[2] - viewXOffset ) * viewZoom);
	    coords[3] = (int)(( (int) coords[3] - viewYOffset ) * viewZoom);
	    
	    Graphics2D g2d = (Graphics2D)g;
	    Stroke oldStroke = g2d.getStroke();
	    Color oldColor = g2d.getColor();
	    g2d.setStroke(new BasicStroke(15.0F));
	    g2d.setColor(color);
	    g2d.drawLine(coords[0],coords[1],coords[2],coords[3]);
	    g2d.setStroke(oldStroke);
	    g2d.setColor(oldColor);
	}
	
	public void paintGrid( Graphics g )
	{
		//draw vertical lines
		int leftMapX = this.panelToMapX(0);
		int leftMapXRounded = (leftMapX/20)*20;
		boolean finished = false;
		int currentX = leftMapXRounded;
		while ( !finished )
		{
			Color prevColor = g.getColor();
			g.setColor(new Color((float)0.95,(float)0.95,(float)0.95));
			//g.setColor(Color.LIGHT_GRAY);
			g.drawLine(mapToPanelX(currentX),0,mapToPanelX(currentX),this.getHeight());
			g.setColor(prevColor);
			if ( mapToPanelX(currentX) > this.getWidth() )
				finished = true;
			currentX += 20;
		}
		
		//draw horizontal lines
		int leftMapY = this.panelToMapY(0);
		int leftMapYRounded = (leftMapY/20)*20;
	    finished = false;
		int currentY = leftMapYRounded;
		while ( !finished )
		{
			Color prevColor = g.getColor();
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(0,mapToPanelY(currentY),this.getWidth(),mapToPanelY(currentY));
			g.setColor(prevColor);
			if ( mapToPanelY(currentY) > this.getHeight() )
				finished = true;
			currentY += 20;
		}
		
	}
	
	public void doPaintComponent ( Graphics g )
	{
		super.paintComponent(g);
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node n = (Node) nodes.get(i);
			n.paint(g);
			List arrows = n.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				a.paint(g);
			}
		}
		if ( specialNode != null )
		{
			specialNode.paint(g);
			List arrows = specialNode.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				a.paint(g);
			}
		}
		if ( specialArrow != null )
		{
			specialArrow.paint(g);
		}
	}
	
	public Node nodeAt ( int x , int y )
	{
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node n = (Node) nodes.get(i);
			Rectangle r = n.getBounds();
			if ( r.contains(x,y) )
				return n;
		}
		return null;
	}
	
	private double ARROWAT_TOLERANCE = 5;
	
	public Arrow arrowAt ( int x , int y )
	{
		Arrow best = null;
		double bestDist = 999.99;
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			List arrows = ((Node)nodes.get(i)).getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow a = (Arrow) arrows.get(j);
				int[] coords = a.getPaintingCoords();
				Line2D l = new Line2D.Float((float)coords[0],(float)coords[1],(float)coords[2],(float)coords[3]);
				double dist = l.ptLineDist((double)x,(double)y);
				if ( dist < ARROWAT_TOLERANCE && dist < bestDist
					&& x <= Math.max((float)coords[0],(float)coords[2])+ARROWAT_TOLERANCE
					&& x >= Math.min((float)coords[0],(float)coords[2])-ARROWAT_TOLERANCE
					&& y <= Math.max((float)coords[1],(float)coords[3])+ARROWAT_TOLERANCE
					&& y >= Math.min((float)coords[1],(float)coords[3])-ARROWAT_TOLERANCE
				)
				{
					best = a;
					bestDist = dist;
				}
			}
		}
		return best; 
	}
	
	
	private Node selectedNode = null;
	private Arrow selectedArrow = null;
	
	
	public Node getSelectedNode()
	{
		return selectedNode;
	}
	
	public void resetSelections()
	{
		if ( selectedNode != null ) selectedNode.setSelected(false);
		if ( selectedArrow != null ) selectedArrow.setSelected(false);
		selectedNode = null;
		selectedArrow = null;
	}
	
	public void selectNode ( Node n )
	{
		selectedNode = n;
		n.setSelected(true);
		requestFocus();
	}
	
	public void selectArrow ( Arrow a )
	{
		selectedArrow = a;
		a.setSelected(true);
		requestFocus();
	}
	
	public void mouseClicked(MouseEvent arg0) 
	{
		if ( toolListener != null )
			toolListener.mouseClicked(arg0);
		else
		{
			Node n = nodeAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
			if ( n != null )
			{
				propP.show(n);
				
				resetSelections();
				selectNode(n);					
				
				showNodeMenuIfApplicable(arg0,n);
				
			}
			else 
			{
				Arrow a = arrowAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
				if ( a != null )
				{
					
					propP.show(a);

					resetSelections();
					selectArrow(a);
					
					showArrowMenuIfApplicable(arg0,a);
					
				}
				else
				{
					resetSelections();
					propP.show(worldN);			
					
					showEmptySpaceMenuIfApplicable(arg0);
				}
			}
		}
		repaint();
		/*
		else
		{
			if ( specialNode == null )
			{
				Node n = nodeAt(arg0.getX(),arg0.getY());
				if ( n != null )
				{
					removeNode(n);
					setSpecialNode(n);
				}
			}
			else
			{
				System.out.println("Dropped: " +specialNode + specialNode.getBounds());
				specialNode.setLocation(arg0.getX(),arg0.getY());
				addNode(specialNode);
				setSpecialNode(null);
				repaint();
			}
		}
		*/
	}
	
	public void mouseEntered(MouseEvent arg0) 
	{
		if ( toolListener != null )
			toolListener.mouseEntered(arg0);
		else
			setBorder(BorderFactory.createLineBorder(Color.red));
	}
	
	public void mouseExited(MouseEvent arg0) 
	{
		if ( toolListener != null )
			toolListener.mouseExited(arg0);
		else
			setBorder(null);
	}
	
	double lastPressX = 0.0;
	double lastPressY = 0.0;
	
	public void showEmptySpaceMenuIfApplicable ( MouseEvent arg0 )
	{
		if ( arg0.isPopupTrigger() )
		{
			JPopupMenu jpm = new JPopupMenu();
			JMenuItem pasteItem = new JMenuItem(new PasteNodeAction(this));
			jpm.add(pasteItem);
			jpm.show(this,arg0.getX(),arg0.getY());
		}
	}
	
	public void showArrowMenuIfApplicable( MouseEvent arg0 , Arrow a )
	{
		if ( arg0.isPopupTrigger() )
		{
			JPopupMenu jpm = new JPopupMenu();
			JMenuItem delItem = new JMenuItem(new DeleteArrowAction(a,this));
			jpm.add(delItem);
			jpm.show(this,arg0.getX(),arg0.getY());
		}
	}
	
	public void showNodeMenuIfApplicable ( MouseEvent arg0 , Node n )
	{
		if ( arg0.isPopupTrigger() )
		{
			JPopupMenu jpm = new JPopupMenu();
			JMenuItem cutItem = new JMenuItem(new CutNodeAction(n,this));
			jpm.add(cutItem);
			JMenuItem copyItem = new JMenuItem(new CopyNodeAction(n,this));
			jpm.add(copyItem);
			jpm.add(new JSeparator());
			JMenuItem delItem = new JMenuItem(new DeleteNodeAction(n,this));
			jpm.add(delItem);
			jpm.show(this,arg0.getX(),arg0.getY());
		}
	}
	
	public void mousePressed(MouseEvent arg0) 
	{
		lastPressX = arg0.getX();
		lastPressY = arg0.getY();
		if ( toolListener != null )
			toolListener.mousePressed(arg0);
		else
		{
			Node n = nodeAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
			if ( n != null )
			{
				propP.show(n);
				resetSelections();
				selectNode(n);		
				
				showNodeMenuIfApplicable(arg0,n);
			}
			else 
			{
				Arrow a = arrowAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
				if ( a != null )
				{
					propP.show(a);
					resetSelections();
					selectArrow(a);	
					//if ( arg0.getButton() == MouseEvent.BUTTON3 )
					showArrowMenuIfApplicable(arg0,a);
				}
				else
				{
					propP.show(worldN);	
					resetSelections();
					
					showEmptySpaceMenuIfApplicable(arg0);
				}
			}
		}
		repaint();
	}

	public void mouseReleased(MouseEvent arg0) 
	{
		if ( toolListener != null )
			toolListener.mouseReleased(arg0);
		else if ( specialNode != null )
		{
			int newLocationX = panelToMapX(arg0.getX());
			int newLocationY = panelToMapY(arg0.getY());
			
			if ( isSnapToGridEnabled( ))
			{
				newLocationX = (newLocationX/20)*20;
				newLocationY = (newLocationY/20)*20;
			}
			
			specialNode.setLocation(newLocationX,newLocationY);
			addNode(specialNode);
			setSpecialNode(null);
			repaint();
		}
		else
		{
		    Node n = nodeAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
		    if ( n!=null ) showNodeMenuIfApplicable(arg0,n);
		    else
		    {
			Arrow a = arrowAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
			if ( a != null ) showArrowMenuIfApplicable(arg0,a);
			else showEmptySpaceMenuIfApplicable(arg0);
		    }
		}
	}
	


	public void mouseDragged(MouseEvent arg0) 
	{
		if ( toolMotionListener != null )
			toolMotionListener.mouseDragged(arg0);
		else
		{
			if ( specialNode == null )
			{
				Node n = nodeAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
				if ( n != null && n.isSelected() )
				{
					removeNode(n);
					setSpecialNode(n);
				}
				else
				{
					//view translation function (center view on cursor)
					//TODO
					double xDrag = arg0.getX() - lastPressX;
					double yDrag = arg0.getY() - lastPressY;
									
					if ( "push".equals(PuckConfiguration.getInstance().getProperty("translateMode")) )
					{
						viewXOffset += Math.ceil(xDrag/viewZoom);
						viewYOffset += Math.ceil(yDrag/viewZoom);
					}
					else //"hold"
					{
						viewXOffset -= Math.ceil(xDrag/viewZoom);
						viewYOffset -= Math.ceil(yDrag/viewZoom);
					}
					
					/*
					if ( viewXOffset < 0 ) viewXOffset = 0;
					if ( viewYOffset < 0 ) viewYOffset = 0;
					
					if ( viewXOffset + this.getWidth()/viewZoom > bufferW-10 )
						viewXOffset = bufferW-10-this.getWidth()/viewZoom;
					if ( viewYOffset + this.getWidth()/viewZoom > bufferH-10 )
						viewYOffset = bufferH-10-this.getWidth()/viewZoom;
					*/
					
					lastPressX = arg0.getX();
					lastPressY = arg0.getY();
					
					repaint();
					/*
					System.out.println("*"+panelToMapX(arg0.getX()));
					System.out.println("#"+getWidth());
					System.out.println(viewXOffset+","+viewYOffset);
					viewXOffset = -this.getWidth()/2/viewZoom + panelToMapX(arg0.getX());
					viewYOffset = -this.getHeight()/2/viewZoom + panelToMapY(arg0.getY());
					if ( viewXOffset < 0 ) viewXOffset = 0;
					if ( viewYOffset < 0 ) viewYOffset = 0;
					System.out.println("*"+panelToMapX(arg0.getX()));
					System.out.println(viewXOffset+","+viewYOffset);
					repaint();
					*/
				}
			}
			else
			{
				//if ( snapToGrid )
				//	specialNode.setLocation((panelToMapX(arg0.getX())/20)*20,(panelToMapY(arg0.getY())/20)*20);
				//else
				int newLocationX = panelToMapX(arg0.getX());
				int newLocationY = panelToMapY(arg0.getY());
				
				if ( isSnapToGridEnabled( ))
				{
					newLocationX = (newLocationX/20)*20;
					newLocationY = (newLocationY/20)*20;
				}
				
				
				specialNode.setLocation(newLocationX,newLocationY);
				repaint();
			}
		}
		
		
		/*
		else if ( specialNode != null )
		{
			System.out.println("Dropped: " +specialNode + specialNode.getBounds());
			specialNode.setLocation(arg0.getX(),arg0.getY());
			addNode(specialNode);
			setSpecialNode(null);
			repaint();
		}
		*/
		/*
		else
		{
			if ( specialNode != null )
			{
				specialNode.setLocation(arg0.getX(),arg0.getY());
				repaint();
			}
		}
		*/
	}

	
	private Node currentHighlightedNode = null;
	private Arrow currentHighlightedArrow = null;
	
	public void mouseMoved(MouseEvent arg0) 
	{
		if ( toolMotionListener != null )
			toolMotionListener.mouseMoved(arg0);
		else
		{
			
			
			Node n = nodeAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
			
			if ( currentHighlightedNode != null && currentHighlightedNode != n )
			{
				currentHighlightedNode.setHighlighted ( false );
				currentHighlightedNode = null;
				repaint();
			}
			
			if ( n != null && currentHighlightedNode != n )
			{
				n.setHighlighted ( true );
				currentHighlightedNode = n;
				repaint();
			}
			
			Arrow a = arrowAt(panelToMapX(arg0.getX()),panelToMapY(arg0.getY()));
			
			if ( currentHighlightedArrow != null && currentHighlightedArrow != a )
			{
				currentHighlightedArrow.setHighlighted ( false );
				currentHighlightedArrow = null;
				repaint();
			}
			
			if ( a != null && currentHighlightedArrow != a )
			{
				a.setHighlighted ( true );
				currentHighlightedArrow = a;
				repaint();
			}
			
			
		}
		/*
		else
		{
			if ( specialNode != null )
			{
				specialNode.setLocation(arg0.getX(),arg0.getY());
				repaint();
			}
		}
		*/
	}
	
	public void keyPressed ( KeyEvent evt ) {}
	public void keyReleased ( KeyEvent evt ) 
	{
		//System.err.println("Released " + evt.getKeyCode() + " vs " + KeyEvent.VK_DELETE );
		if ( evt.getKeyCode() == KeyEvent.VK_DELETE )
		{
			if ( selectedNode != null )
			{
				totallyRemoveNode(selectedNode);
			}
			else if ( selectedArrow != null )
			{
				selectedArrow.getSource().removeArrow(selectedArrow);
			}
			repaint();
		}	
	}
	public void keyTyped ( KeyEvent evt ) 
	{
		/*
		System.err.println("Typed " + evt.getKeyCode() + " vs " + KeyEvent.VK_DELETE );
		if ( evt.getKeyCode() == KeyEvent.VK_DELETE )
		{
			if ( selectedNode != null )
			{
				totallyRemoveNode(selectedNode);
			}
			else if ( selectedArrow != null )
			{
				selectedArrow.getSource().removeArrow(selectedArrow);
			}
		}
		*/
	}
	
	public GraphEditingPanel( PropertiesPanel propP )
	{
		setBackground(Color.WHITE);
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true); //will generate key evts.
		addKeyListener(this);
		setVisible(true);
		this.propP = propP;
		roomNodes.add(Messages.getInstance().getMessage("none"));
		itemNodes.add(Messages.getInstance().getMessage("none"));
		charNodes.add(Messages.getInstance().getMessage("none"));
		resetNodeLists();
	}
	
	public void setToolListener ( MouseListener ml )
	{
		toolListener = ml;
	}
	
	public void setToolMotionListener ( MouseMotionListener ml )
	{
		toolMotionListener = ml;
	}
	
	public void resetToolListeners ( )
	{
		toolListener = null;
		//toolMotionListener = new DefaultMouseMotionListener(this);
		toolMotionListener = null;
	}

	
	public void centerViewOn(Node n)
	{
		double nodeX = mapToPanelX ( n.getBounds().getCenterX() );
		double nodeY = mapToPanelY ( n.getBounds().getCenterY() );
		double viewCenterX = this.getWidth() / 2;
		double viewCenterY = this.getHeight() / 2;
		viewXOffset += ((nodeX-viewCenterX)/viewZoom);
		viewYOffset += ((nodeY-viewCenterY)/viewZoom);
	}


	/**
	 * @return Returns the viewXOffset.
	 */
	public double getViewXOffset() {
		return viewXOffset;
	}
	/**
	 * @param viewXOffset The viewXOffset to set.
	 */
	public void setViewXOffset(double viewXOffset) {
		this.viewXOffset = viewXOffset;
	}
	/**
	 * @return Returns the viewYOffset.
	 */
	public double getViewYOffset() {
		return viewYOffset;
	}
	/**
	 * @param viewYOffset The viewYOffset to set.
	 */
	public void setViewYOffset(double viewYOffset) {
		this.viewYOffset = viewYOffset;
	}
	/**
	 * @return Returns the viewZoom.
	 */
	public double getViewZoom() {
		return viewZoom;
	}
	/**
	 * @param viewZoom The viewZoom to set.
	 */
	public void setViewZoom(double viewZoom) {
		this.viewZoom = viewZoom;
	}
}
