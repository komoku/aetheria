/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.i18n.Messages;
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
	
	public EntityPanel()
	{
		super();
		nameTextField.setText("Entity #"+getID());
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(Messages.getInstance().getMessage("entity.uniquename")));
		namePanel.add(nameTextField);
		this.add(namePanel);
		
		
		//why do this? no!
		//setVisible(true);
	}
	
	public String toString()
	{
		return nameTextField.getText() + "##" + super.toString();
	}
	
	public String getName()
	{
		return nameTextField.getText();
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
	
	
}
