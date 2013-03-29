package org.f2o.absurdum.puck.gui.graph;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.f2o.absurdum.puck.gui.panels.ArrowPanel;
import org.f2o.absurdum.puck.gui.panels.GraphElementPanel;
import org.f2o.absurdum.puck.gui.panels.PathPanel;
import org.f2o.absurdum.puck.i18n.UIMessages;

public class GraphArranger 
{

	private static double GRAVITATIONAL_CONSTANT = 0.005;
	private static int MAX_BUMP = 10;
	private static int IDEAL_PATH_LENGTH = 80;
	private static Random random = new Random();
	private static double REPULSION_DISTANCE = 80;
	private static double MAX_ACCEL = 5.0;
	
	
	
	
	private void applyForce ( Node firstNode , Node secondNode , Point2D.Double tl1 , Point2D.Double tl2 , Point2D.Double center1 , Point2D.Double center2 , boolean repulse )
	{
		
		double x1 = tl1.x;
		double y1 = tl1.y;
		
		double x2 = tl2.x;
		double y2 = tl2.y;
		
		double xCenter1 = center1.x;
		double yCenter1 = center1.y;
		
		double xCenter2 = center2.x;
		double yCenter2 = center2.y;
		
		//we assume constant density, mass proportional to volume
		int mass1 = firstNode.getBounds().height * firstNode.getBounds().width;
		int mass2 = secondNode.getBounds().height * secondNode.getBounds().width;
		
		if ( xCenter1 == yCenter1 && xCenter2 == yCenter2 ) //nodes in exactly the same position: we bump one of them (the second one) randomly
		{
			x2 += random.nextInt(MAX_BUMP*2+1)-MAX_BUMP;
			y2 += random.nextInt(MAX_BUMP*2+1)-MAX_BUMP;	
		}
		else
		{
			System.err.println("TL: (" + x1 +"," + y1+") (" + x2 + "," + y2 + ")");
			System.err.println("CN: (" + xCenter1 +"," + yCenter1+") (" + xCenter2 + "," + yCenter2 + ")");
			//repulsion on x axis. It's not really acceleration, we don't keep speed (we assume huge drag)
			double xDistance = (double) Math.abs(xCenter1-xCenter2);
			if ( xDistance != 0.0 )
			{
				double xRepulsionForce = (((double)mass1)*((double)mass2)*GRAVITATIONAL_CONSTANT)/(xDistance*xDistance);
				double xAccel1 = Math.max( xRepulsionForce / mass1 , MAX_ACCEL );
				double xAccel2 = Math.max( xRepulsionForce / mass2 , MAX_ACCEL );
				System.err.println("x-dist " + xDistance + ", rep. force " + xRepulsionForce);
				if (!repulse) { xAccel1 = -xAccel1; xAccel2 = -xAccel2; } //attract
				if ( xCenter1 > xCenter2 ) { x1 += xAccel1 ; x2 -= xAccel2; }
				else { x1 -= xAccel1 ; x2 += xAccel2; }
			}
			
			//repulsion on y axis. It's not really acceleration, we don't keep speed (we assume huge drag)
			double yDistance = (double) Math.abs(yCenter1-yCenter2);
			if ( yDistance != 0.0 )
			{
				double yRepulsionForce = (((double)mass1)*((double)mass2)*GRAVITATIONAL_CONSTANT)/(yDistance*yDistance);
				double yAccel1 = Math.max( yRepulsionForce / mass1 , MAX_ACCEL ); 
				double yAccel2 = Math.max( yRepulsionForce / mass2 , MAX_ACCEL );
				System.err.println("y-dist " + yDistance + ", rep. force " + yRepulsionForce);
				if (!repulse) { yAccel1 = -yAccel1; yAccel2 = -yAccel2; } //attract
				if ( yCenter1 > yCenter2 ) { y1 += yAccel1 ; y2 -= yAccel2; }
				else { y1 -= yAccel1 ; y2 += yAccel2; }
			}
		}
		
		System.err.println("N1 Old: " + tl1.x + ", " + tl1.y + " -> New: " + x1 + ", " + y1 );
		System.err.println("N2 Old: " + tl2.x + ", " + tl2.y + " -> New: " + x2 + ", " + y2 );
		
		//update node position
		tl1.x = x1;
		tl2.x = x2;
		tl1.y = y1;
		tl2.y = y2;
		
		//update the center information
		center1.x = tl1.x + ((double)firstNode.getBounds().width/2);
		center2.x = tl2.x + ((double)secondNode.getBounds().width/2);
		center1.y = tl1.y + ((double)firstNode.getBounds().height/2);
		center2.y = tl2.y + ((double)secondNode.getBounds().height/2);
	}
	
	public void arrangeIter ( GraphEditingPanel gep , List nodes , List topLeftCoords , List centerCoords )
	{

		
		//apply repulsion forces
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			for ( int j = i+1 ; j < nodes.size() ; j++ )
			{
				//a pair of different nodes. There is a repulsion.
				Node firstNode = (Node) nodes.get(i);
				Node secondNode = (Node) nodes.get(j);
				
				Point2D.Double tl1 = (Point2D.Double) topLeftCoords.get(i);
				Point2D.Double tl2 = (Point2D.Double) topLeftCoords.get(j);
				
				Point2D.Double center1 = (Point2D.Double) centerCoords.get(i);
				Point2D.Double center2 = (Point2D.Double) centerCoords.get(j);
				
				double nodeDist = center1.distance(center2);
				
				if ( nodeDist < REPULSION_DISTANCE )
					applyForce(firstNode,secondNode,tl1,tl2,center1,center2,true);
				else if ( nodeDist > 4 * REPULSION_DISTANCE ) //attract at large distances
					applyForce(firstNode,secondNode,tl1,tl2,center1,center2,false);
			}
		}
		
		//apply path direction forces
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node theNode = (Node) nodes.get(i);
			List arrows = theNode.getArrows();
			for ( int j = 0 ; j < arrows.size() ; j++ )
			{
				Arrow theArrow = (Arrow) arrows.get(j);
				GraphElementPanel panel = theArrow.getAssociatedPanel();
				Node destinationNode = theArrow.getDestination();
				if ( destinationNode != null )
				{
					if ( panel instanceof PathPanel )
					{
	
						PathPanel thePanel = (PathPanel) panel;
						String direction = thePanel.getDirectionString();
						if ( direction != null )
						{
							int destinationIndex = nodes.indexOf(destinationNode);
							if ( destinationIndex >= 0 )
							{
								Point2D.Double destTl = (Point2D.Double) topLeftCoords.get(destinationIndex);
								Point2D.Double destCenter = (Point2D.Double) centerCoords.get(destinationIndex);
								Point2D.Double srcTl = (Point2D.Double) topLeftCoords.get(i);
								Point2D.Double srcCenter = (Point2D.Double) centerCoords.get(i);
								applyPathDirectionForces ( theNode , destinationNode , srcTl , destTl , srcCenter , destCenter , direction );
							}
						}
					}
					else
					{
						int destinationIndex = nodes.indexOf(destinationNode);
						if ( destinationIndex >= 0 )
						{
							Point2D.Double destTl = (Point2D.Double) topLeftCoords.get(destinationIndex);
							Point2D.Double destCenter = (Point2D.Double) centerCoords.get(destinationIndex);
							Point2D.Double srcTl = (Point2D.Double) topLeftCoords.get(i);
							Point2D.Double srcCenter = (Point2D.Double) centerCoords.get(i);
						    double nodeDist = srcTl.distance(destTl);
						    if ( nodeDist > 2*REPULSION_DISTANCE ) //attract connected nodes somewhat //nah, not good result
						       ;// 	applyForce(theNode,destinationNode,srcTl,destTl,srcCenter,destCenter,false);
						} //if destinationIndex >=0
					} //i not instance of path panel
				} //if destination node not null
			} //for all arrows
		} //for all nodes
		
		//actually move nodes to the new coordinates
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node node = (Node) nodes.get(i);
			Point2D.Double topLeft = (Point2D.Double) topLeftCoords.get(i);
			node.setLocation( (int)Math.round(topLeft.x) , (int)Math.round(topLeft.y) );
		}
		
	}
	
	private void applyPathDirectionForces ( Node source , Node destination , Point2D.Double sourceTopLeft , Point2D.Double destinationTopLeft , Point2D.Double sourceCenter , Point2D.Double destinationCenter , String directionString )
	{
		//destination node coordinates
		double x = destinationTopLeft.x;
		double y = destinationTopLeft.y;
		
		//destination node center
		double xCenter = destinationCenter.x;
		double yCenter = destinationCenter.y;
		
		double mass = destination.getBounds().height * destination.getBounds().width;
		
		//coordinates where the destination node will be attracted
		double xIdealDestination;
		double yIdealDestination;
		
		if ( directionString.equals(UIMessages.getInstance().getMessage("dir.n")) )
		{
			xIdealDestination = sourceCenter.x;
			yIdealDestination = sourceCenter.y - IDEAL_PATH_LENGTH;
		}
		else if ( directionString.equals(UIMessages.getInstance().getMessage("dir.s")) )
		{
			xIdealDestination = sourceCenter.x;
			yIdealDestination = sourceCenter.y + IDEAL_PATH_LENGTH;
		}
		else if ( directionString.equals(UIMessages.getInstance().getMessage("dir.w")) )
		{
			xIdealDestination = sourceCenter.x - IDEAL_PATH_LENGTH;
			yIdealDestination = sourceCenter.y;
		}
		else if ( directionString.equals(UIMessages.getInstance().getMessage("dir.e")) )
		{
			xIdealDestination = sourceCenter.x + IDEAL_PATH_LENGTH;
			yIdealDestination = sourceCenter.y;
		}
		else return;
		
		//attraction on x axis. It's not really acceleration, we don't keep speed (we assume huge draft)
		double xDistance = (double) Math.abs(xCenter-xIdealDestination);
		if ( xDistance != 0.0 )
		{
			double xAttractionForce = (((double)mass)*((double)mass)*GRAVITATIONAL_CONSTANT)/(xDistance*xDistance);
			double xAccel = Math.max( xAttractionForce / mass , MAX_ACCEL );
			if ( xCenter < xIdealDestination ) { x += xAccel; }
			else { x -= xAccel; }
		}
		
		//attraction on y axis. It's not really acceleration, we don't keep speed (we assume huge draft)
		double yDistance = (double) Math.abs(yCenter-yIdealDestination);
		if ( yDistance != 0.0 )
		{
			double yAttractionForce = (((double)mass)*((double)mass)*GRAVITATIONAL_CONSTANT)/(yDistance*yDistance);
			double yAccel = Math.max( yAttractionForce / mass , MAX_ACCEL );
			if ( yCenter < yIdealDestination ) { y += yAccel; }
			else { y -= yAccel; }
		}
		
		//update node position
		destinationTopLeft.x = x;
		destinationTopLeft.y = y;
		
		//update the center information
		destinationCenter.x = destinationTopLeft.x + ((double)destination.getBounds().width/2);
		destinationCenter.y = destinationTopLeft.y + ((double)destination.getBounds().height/2);
		
	}
	
	public void arrange ( final GraphEditingPanel gep , int iters )
	{		
		
		List nodes = gep.getNodes();
		List topLeftCoords = new ArrayList(); //node coordinates as double
		List centerCoords = new ArrayList(); //node coordinates as double
		
		//fill coordinate list
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			Node node = (Node) nodes.get(i);
			Point2D.Double topLeft = new Point2D.Double( node.getBounds().x , node.getBounds().y );
			Point2D.Double center = new Point2D.Double( node.getBounds().getCenterX() , node.getBounds().getCenterY() );
			topLeftCoords.add(topLeft);
			centerCoords.add(center);
		}
		
		for ( int i = 0 ; i < iters ; i++ )
		{
			arrangeIter(gep , nodes , topLeftCoords , centerCoords);
			/*
			try {
				SwingUtilities.invokeAndWait(new Runnable()
				{
					public void run()
					{
					*/
						gep.repaint();
						//if ( i % 50 == 0 ) JOptionPane.showConfirmDialog(gep, "UH UH MOOVAN");
						/*
					}
				}
				);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
	}
	
	
}
