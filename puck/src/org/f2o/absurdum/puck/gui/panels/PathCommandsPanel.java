/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 22-jul-2005 23:55:11
 * as file DescriptionListPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.swing.EnhancedJList;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.f2o.absurdum.puck.util.swing.SwingComponentHighlighter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;




/**
 * @author carlos
 *
 * Created at regulus, 22-jul-2005 23:55:11
 */
public class PathCommandsPanel extends JPanel
{

	private JList theList;
	
	private JTextField commandTextField = new EnhancedJTextField(20);
	
	
	private JButton delButton = new JButton(UIMessages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(UIMessages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(UIMessages.getInstance().getMessage("button.mod"));
	private JButton topButton = new JButton(UIMessages.getInstance().getMessage("button.top"));
	
	private JLabel cmdLabel;
	private JPanel buttonsPanel;
	private JScrollPane jsp;
	
	//privatize it
	public DefaultListModel listContent = new DefaultListModel();
	
	
	public DefaultListModel getListModel()
	{
		return listContent;
	}
	
	public PathCommandsPanel ( String borderTitle , String labelMsg , boolean priorizeButton )
	{
		this();
		this.setBorder(BorderFactory.createTitledBorder(borderTitle));
		cmdLabel.setText(labelMsg);
		if ( priorizeButton )
		{
			buttonsPanel.add(topButton);
			topButton.addActionListener ( new ActionListener() 
					{

						public void actionPerformed(ActionEvent evt) 
						{
							
							String it = (String) theList.getSelectedValue();
							int ind = theList.getSelectedIndex();
							if ( ind > 0 )
							{
								String prev = (String) theList.getModel().getElementAt(ind-1);

								listContent.set(ind-1,it);
								listContent.set(ind,prev);
								theList.setSelectedIndex(ind-1);
								jsp.repaint();
							}
						}
				
					}
					);
			
		}
	}
	
	public PathCommandsPanel ( )
	{
		//listContent.setSize(100);
		theList = new EnhancedJList( listContent );
		//listContent.setSize(2);
		theList.setCellRenderer ( new SingleStringCellRenderer() );
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		this.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("path.commands")));
		
		//this.add(new JScrollPane(theList));
		jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,45));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(jp);
		//this.add(theList);
		
		JPanel p1 = new JPanel();
		p1.add ( cmdLabel = new JLabel(UIMessages.getInstance().getMessage("label.command")) );
		p1.add ( commandTextField );
		add(p1);
		

		buttonsPanel = new JPanel();
		buttonsPanel.add(addButton);
		buttonsPanel.add(modButton);
		buttonsPanel.add(delButton);
		//buttonsPanel.add(topButton);
		add(buttonsPanel);
		
		addButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						String nu = new String ( commandTextField.getText() );
						if ( nu != null && nu.length() > 0 )
						{
							listContent.addElement(nu);
							jsp.repaint();
							commandTextField.setText("");
						}
						else
						{
							SwingComponentHighlighter.temporalRedBackground(commandTextField);
						}
					}
			
				}
				);
		
		delButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						
						int[] indices = theList.getSelectedIndices();
						for ( int i = indices.length-1 ; i >= 0 ; i-- )
						{
							listContent.remove(indices[i]);
						}
						jsp.repaint();
					}
			
				}
				);
		
		modButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						
						int ind = theList.getSelectedIndex();
						
						String nu = new String ( commandTextField.getText() );
						if ( nu != null && nu.length() > 0 )
						{
							listContent.set(ind,nu);
							jsp.repaint();
						}
					}
			
				}
				);

		
		theList.addListSelectionListener(
				new ListSelectionListener()
				{

					public void valueChanged(ListSelectionEvent arg0) 
					{
						String val = (String) theList.getSelectedValue();
						commandTextField.setText(val);
					}
					
				}
				);

		updateButtonEnabledness();
		theList.getModel().addListDataListener(new ListDataListener()
		{
			public void intervalAdded(ListDataEvent e) 
			{
				updateButtonEnabledness();
			}

			public void intervalRemoved(ListDataEvent e) 
			{
				updateButtonEnabledness();
			}

			public void contentsChanged(ListDataEvent e) 
			{	
			}
			
		}
		);
		
	}
	
	/**
	 * Disables all buttons but "add" if the list is empty, and enables them otherwise
	 */
	public void updateButtonEnabledness()
	{
		if ( theList.getModel().getSize() > 0 )
		{
			delButton.setEnabled(true);
			modButton.setEnabled(true);
			topButton.setEnabled(true);
		}
		else
		{
			delButton.setEnabled(false);
			modButton.setEnabled(false);
			topButton.setEnabled(false);
		}
	}
	
	public Node getXML ( Document d )
	{
		
		Element result = d.createElement("CommandList");
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String cmd = (String) listContent.get(i);
			Element cmdNode = d.createElement("Command");
			cmdNode.setAttribute("name",cmd);	
			result.appendChild(cmdNode);
		}
		
		return result;
		
	}
	
	/**
	 * For example set the names to InvolvedSkills, Skill, name to get: <InvolvedSkills><Skill name="foo"/></InvolvedSkills>
	 * @param d An XML document to create the element in.
	 * @param listName Name of the XML tag containing the list expressed by this panel.
	 * @param listElementName Name of the XML tag for each element of the list.
	 * @param listElementAttributeName Name of the XML attribute containing the value of each element in the list.
	 * @return
	 */
	public Node getXML ( Document d , String listName , String listElementName , String listElementAttributeName )
	{
		
		Element result = d.createElement(listName);
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String cmd = (String) listContent.get(i);
			Element cmdNode = d.createElement(listElementName);
			cmdNode.setAttribute(listElementAttributeName,cmd);	
			result.appendChild(cmdNode);
		}
		
		return result;
		
	}
	
	public void initFromXML ( org.w3c.dom.Node n , String elementName , String attributeName  )
	{
		
		Element e = (Element) n;

			//inverse operation to getXML(Document,[listName],elementName,attributeName)
			NodeList nl = e.getElementsByTagName(elementName);
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				Element cmdElt = (Element) nl.item(i);
				listContent.addElement(cmdElt.getAttribute(attributeName));
			}

	}
	
	//from CommandList or SingularNames, PluralNames, etc. node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		
		Element e = (Element) n;

		if ( e.getNodeName().equals("Path") )
		{
			//inverse operation to getXML(Document)
			NodeList nl = e.getElementsByTagName("Command");
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				Element cmdElt = (Element) nl.item(i);
				listContent.addElement(cmdElt.getAttribute("name"));
			}
		}
		else
		{
			//inverse operation to getXMLFoNames(Document)
			NodeList nl = e.getElementsByTagName("Name");
			for ( int i = 0 ; i < nl.getLength() ; i++ )
			{
				Element namElt = (Element) nl.item(i);
				NodeList nl2 = namElt.getChildNodes();
				for ( int j = 0 ; j < nl2.getLength() ; j++ )
				{
					if ( nl2.item(j) instanceof Text )
						listContent.addElement ( nl2.item(j).getNodeValue() );
				}
				
			}
		}

	}
	
	
	
	
	public Node getXMLForNames ( Document d , String eltName )
	{
		Element result = d.createElement(eltName);
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String cmd = (String) listContent.get(i);
			Element cmdNode = d.createElement("Name");
			cmdNode.appendChild(d.createTextNode(cmd));
			result.appendChild(cmdNode);
		}
		
		return result;
	}

	
}
