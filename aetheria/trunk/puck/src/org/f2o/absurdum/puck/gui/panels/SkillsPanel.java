/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 23-jul-2005 22:03:53
 * as file ExtraDescriptionsPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * @author carlos
 * Created: 2008-02-03
 *
 */
public class SkillsPanel extends JPanel
{

	private JList theList;
	
	private JTextField nameTextField = new EnhancedJTextField(8);
	private JTextField valueTextField = new EnhancedJTextField(5);
	
	private JButton delButton = new JButton(Messages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(Messages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(Messages.getInstance().getMessage("button.mod"));
	
	private DefaultListModel listContent = new DefaultListModel();
	
	
	private String valueAttrName = "value"; //sometimes it will be "relevance" instead
	
	
	public SkillsPanel ( )
	{
		this ( "value" );
	}
	
	public SkillsPanel ( String valueAttrName )
	{
		this.valueAttrName = valueAttrName;
		theList = new JList( listContent );
		
		//theList.setCellRenderer ( new PropertiesCellRenderer() );
		theList.setCellRenderer ( new TwoStringCellRenderer(Messages.getInstance().getMessage("onlist.traitname"),Messages.getInstance().getMessage("onlist.traitval")) );
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		final JScrollPane jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,50));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(jp);
		
		this.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("label.skills")));
		
		JPanel p0 = new JPanel();
		p0.add ( new JLabel(Messages.getInstance().getMessage("label.traitname")) );
		p0.add ( nameTextField );
		add(p0);
		
		p0.add ( new JLabel(Messages.getInstance().getMessage("label.traitval")) );
		p0.add ( valueTextField );
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(addButton);
		buttonsPanel.add(modButton);
		buttonsPanel.add(delButton);
		//buttonsPanel.add(topButton);
		add(buttonsPanel);
		
		addButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						
						if ( nameTextField.getText() == null || valueTextField.getText() == null 
								 || nameTextField.getText().length() == 0 || valueTextField.getText().length() == 0		
								) return;
						
						String[] nu = new String[]
												 {
								nameTextField.getText(),valueTextField.getText()
												 };
						listContent.addElement(nu);
						jsp.repaint();
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
						
						if ( nameTextField.getText() == null || valueTextField.getText() == null 
								 || nameTextField.getText().length() == 0 || valueTextField.getText().length() == 0		
								) return;
													
						String[] nu = new String[]
												 {
								nameTextField.getText(),valueTextField.getText()
												 };
						listContent.set(ind,nu);
						jsp.repaint();
					}
			
				}
				);
		
		
		theList.addListSelectionListener(
				new ListSelectionListener()
				{

					public void valueChanged(ListSelectionEvent arg0) 
					{
						String[] ar = (String[]) theList.getSelectedValue();
						if ( ar != null ) //could be null if we have just deleted the selected value
						{
							nameTextField.setText(ar[0]);
							valueTextField.setText(ar[1]);					
						}
					}
					
				}
				);

		
	}

	
	public Node getListXML ( Document d , String listName , String eltName )
	{
		Element result = d.createElement(listName);
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String[] desc = (String[]) listContent.get(i);
			Element eltNode = d.createElement(eltName);
			
			eltNode.setAttribute("name",desc[0]);
			eltNode.setAttribute(valueAttrName,desc[1]);
	
			result.appendChild(eltNode);
		}
		return result;
	}
	
	public Node getXML ( Document d )
	{
		return getListXML ( d, "SkillList" , "Skill" );		
	}
	
	public void initFromXML ( org.w3c.dom.Node n , String eltName )
	{
		Element e = (Element) n;
		//have to init ListContent as a Model of String[] { name , val }
		NodeList nl = e.getElementsByTagName(eltName);
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Element anElt = (Element) nl.item(i);
			String name = anElt.getAttribute("name");
			String value = anElt.getAttribute(valueAttrName);
			listContent.addElement(new String[]{name,value});
		}
	}
	
//	from SkillList node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		if ( n != null )
			initFromXML ( n , "Skill" );
	}
	
}
