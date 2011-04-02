/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 02/04/2011 11:45:54
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.i18n.Messages;

/**
 * @author carlos
 *
 * created 2011-04-02
 */
public class EntityListPanel extends JPanel
{
	
	private GraphEditingPanel gep;
	
	private JList listEntities = new JList();
	private Vector sortedNodes;
	private JButton focusButton = new JButton(Messages.getInstance().getMessage("button.focus"));
	
	public EntityListPanel ( final GraphEditingPanel gep )
	{
		this.gep = gep;
		
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("label.entities")));
		//listEntities.setListData(gep.getNodes());
		JScrollPane listScroll = new JScrollPane(listEntities);
		listScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		listScroll.setPreferredSize(new Dimension(80,45));
		add(listScroll);
		
		listEntities.addListSelectionListener ( new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e) 
			{
				Node theNode = (Node) listEntities.getSelectedValue();
				if ( theNode == null ) return;
				gep.focusOnNode(theNode,false);
				listEntities.requestFocusInWindow();
			}
			
		}
		);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(focusButton);
		add(buttonPanel);
		
		focusButton.addActionListener ( new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				Node theNode = (Node) listEntities.getSelectedValue();
				if ( theNode == null ) return;
				gep.focusOnNode(theNode,true);
			}
		});
		
	}
	
	public void refresh()
	{
		sortedNodes = (Vector) gep.getNodes().clone();
		Collections.sort(sortedNodes);
		listEntities.setListData(sortedNodes);
	}

}
