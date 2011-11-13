/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class EntityPanel extends GraphElementPanel
{

	protected EnhancedJTextField nameTextField = new EnhancedJTextField(20);
	
	public static boolean DYNAMICALLY_CHECK_UNIQUE_NAMES = true;
	
	public EntityPanel()
	{
		super();
		nameTextField.setText("Entity #"+getID());
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(UIMessages.getInstance().getMessage("entity.uniquename")));
		namePanel.add(nameTextField);
		this.add(namePanel);
		
		/*
		nameTextField.addFocusListener ( new FocusListener() 
		{
			WorldPanel wp = (WorldPanel) getGraphEditingPanel().getWorldNode().getAssociatedPanel();
			public void focusGained(FocusEvent e) 
			{	
			}
			public void focusLost(FocusEvent e) 
			{
				System.out.println("Flost");
				if ( nameTextField.getText() != null && wp.nameToNode( nameTextField.getText() ) != getNode() && wp.nameToNode( nameTextField.getText() ) != null )
				{
					System.out.println("Flost");
				}
				if ( nameTextField.getText() != null && nameTextField.getText() != getNode.)
			}
			
		}
		);
		*/
		
		if ( DYNAMICALLY_CHECK_UNIQUE_NAMES )
		nameTextField.addKeyListener( new KeyAdapter() 
		{
			public void keyTyped(KeyEvent e) 
			{
				requestUniqueNameCheck();
			}
			
		}
		);
		

	}
	
	private /*transient*/ long lastNameUpdateTimeStamp; //used by the thread that checks for coincident unique names
	private boolean uniqueNameCheckRequested = false;
	
	public synchronized void requestUniqueNameCheck()
	{
		lastNameUpdateTimeStamp = System.currentTimeMillis();
		Thread thr = new Thread()
		{
			public void run()
			{
				for ( ;; )
				{
					try 
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					synchronized ( EntityPanel.this )
					{
						if ( System.currentTimeMillis() - lastNameUpdateTimeStamp > 1500 )
						{
							checkAndIndexUniqueName();
							uniqueNameCheckRequested = false;
							break;
						}
					}
				}
			}
		};
		if ( !uniqueNameCheckRequested )
		{
			uniqueNameCheckRequested = true;
			thr.start();
		}
	}
	
	public synchronized void checkAndIndexUniqueName()
	{
		String theNewName = nameTextField.getText();
		WorldPanel wp = (WorldPanel) getGraphEditingPanel().getWorldNode().getAssociatedPanel();
		org.f2o.absurdum.puck.gui.graph.Node ourNode = wp.panelToNode(this);
		String oldName = wp.nodeToName(ourNode);
		if ( theNewName != null && !theNewName.equals(oldName) )
		{
			while ( wp.nameToNode(theNewName) != null ) //there exists another node with that name!!
			{
				theNewName = theNewName + " (otro)";
			}
			if ( !theNewName.equals(nameTextField.getText()) ) //we have changed the name
				nameTextField.setText(theNewName);
			wp.updateMaps(ourNode);
		}
	}
	
	/*
	public synchronized void giveNewUniqueName()
	{
		//to call if we know current name is not unique
		String theNewName = nameTextField.getText();
		WorldPanel wp = (WorldPanel) getGraphEditingPanel().getWorldNode().getAssociatedPanel();
		org.f2o.absurdum.puck.gui.graph.Node ourNode = wp.panelToNode(this);
		theNewName = theNewName + " (otro)";
		while ( wp.nameToNode(theNewName) != null ) //there exists another node with that name!!
		{
			theNewName = theNewName + " (otro)";
		}
		if ( !theNewName.equals(nameTextField.getText()) ) //we have changed the name
			nameTextField.setText(theNewName);
		wp.updateMaps(ourNode);
	}
	*/
	
	
	public String toString()
	{
		return nameTextField.getText() + "##" + super.toString();
	}
	
	public String getName()
	{
		String name = nameTextField.getText();
		if ( name != null ) return name;
		else return "Unnamed entity";
	}	
	
	protected org.w3c.dom.Node getCustomRelationshipListXML ( Document d , org.f2o.absurdum.puck.gui.graph.Node entityNode )
	{
		List arrows = entityNode.getArrows();
		Element relationshipsElt = d.createElement("RelationshipList");	
		for ( int i = 0 ; i < arrows.size() ; i++ )
		{
			GraphElementPanel gep = ((Arrow)arrows.get(i)).getAssociatedPanel();
			if ( gep instanceof ArrowPanel ) //this if check will become unnecessary when custom relationship implementation is complete
			{
				ArrowPanel relPanel = (ArrowPanel) gep;
				Node n = relPanel.getCustomRelationshipXML(d);
				if ( n.hasChildNodes() ) //if it doesn't have children there are no custom relationships, would be worthless to append it
					relationshipsElt.appendChild(n);
			}
		}
		return relationshipsElt;
	}
	
	
	
	/**
	 * Convenience method to get an integer value from a text field.
	 * @param tf
	 * @param defaultValue
	 * @return
	 */
	public static int getIntegerFromField ( JTextField tf , int defaultValue )
	{
	    int result;
	    try
	    {
		return Integer.parseInt(tf.getText());
	    }
	    catch ( NumberFormatException nfe )
	    {
		return defaultValue;
	    }
	}
	
	
	
}
