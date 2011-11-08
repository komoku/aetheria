/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 23-jul-2005 22:03:53
 * as file ExtraDescriptionsPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.gui.SpacingPanel;
import org.f2o.absurdum.puck.i18n.Messages;
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
 * Created at regulus, 23-jul-2005 22:03:53
 */
public class PropertiesPanel extends JPanel
{

	private JList theList;
	
	private JTextField nameTextField = new EnhancedJTextField(8);
	private JTextField valTextField = new EnhancedJTextField(5);
	private JTextField tuTextField = new EnhancedJTextField(2);
	
	private JButton delButton = new JButton(Messages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(Messages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(Messages.getInstance().getMessage("button.mod"));
	//private JButton topButton = new JButton(Messages.getInstance().getMessage("button.top"));
	
	
	//a summarized report of what the panel contains, to show e.g. in a relationship arrow name.
	protected String getReport()
	{
		if ( listContent != null && listContent.size() > 0 )
		{
			String[] firstOne = (String[]) listContent.get(0);
			if ( firstOne.length > 1 )
			{
				StringBuffer result = new StringBuffer("");
				result.append(firstOne[0]);
				result.append("=");
				result.append(firstOne[1]);
				if ( listContent.size() > 1 )
					result.append(", ...");
				return result.toString();
			}
			else return "";
		}
		else return "";
	}
	
	
	String[] nu = new String[]
							 {
			nameTextField.getText(),valTextField.getText(),tuTextField.getText()
							 };
	
	
	
	//privatize it
	private DefaultListModel listContent = new DefaultListModel();
	
	public PropertiesPanel ( )
	{
		this ( Messages.getInstance().getMessage("label.properties") );
	}
	
	public PropertiesPanel ( String title )
	{
		theList = new EnhancedJList( listContent );
		theList.setCellRenderer ( new PropertiesCellRenderer() );
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//this.add(new JScrollPane(theList));
		final JScrollPane jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,50));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(new SpacingPanel(jp));
		//this.add(theList);
		
		this.setBorder(BorderFactory.createTitledBorder(title));
		
		JPanel p0 = new JPanel();
		p0.add ( new JLabel(Messages.getInstance().getMessage("label.propname")) );
		p0.add ( nameTextField );
		add(p0);
		
		//JPanel p1 = new JPanel();
		p0.add ( new JLabel(Messages.getInstance().getMessage("label.propval")) );
		p0.add ( valTextField );
		//add(p1);
		
		//JPanel p2 = new JPanel();
		p0.add ( new JLabel(Messages.getInstance().getMessage("label.proptu")) );
		p0.add ( tuTextField );
		//add(p2);
		
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
						
						if ( nameTextField.getText() == null || valTextField.getText() == null 
								 || nameTextField.getText().length() == 0 || valTextField.getText().length() == 0		
								) 
						{
							if ( nameTextField.getText() == null || nameTextField.getText().length() == 0 )
								SwingComponentHighlighter.temporalRedBackground(nameTextField);
							if ( valTextField.getText() == null || valTextField.getText().length() == 0 )
								SwingComponentHighlighter.temporalRedBackground(valTextField);
							return;
						}
							
						try{Integer.parseInt(tuTextField.getText());}
						catch(NumberFormatException nfe)
						{
							if ( tuTextField.getText() == null || tuTextField.getText().length() == 0 )
							{
								//timer defaults to -1 (representing infinity) if no value is explicitly set when adding a property
								tuTextField.setText("-1");
							}
							else
							{
								SwingComponentHighlighter.temporalRedBackground(tuTextField);
								return;
							}
						}
						
						//check if property is already present
						int foundIndex = -1;
						for ( int i = 0 ; i < listContent.size() ; i++ )
						{
							String[] s = (String[])listContent.get(i);
							if ( nameTextField.getText().equals(s[0]) )
							{
								//property present at this position
								foundIndex = i;
								break;
							}
						}
						
						String[] nu = new String[]
												 {
								nameTextField.getText(),valTextField.getText(),tuTextField.getText()
												 };
						
						if ( foundIndex < 0 )
							listContent.addElement(nu);
						else
						{
							listContent.set(foundIndex,nu);
							
							//unfortunately this does not work:
							//it seems that the JList does not have these components, it just uses their paint method to draw to
							//coordinates of the JList.
							//Component c = theList.getCellRenderer().getListCellRendererComponent(theList, nu, foundIndex, theList.isSelectedIndex(foundIndex), theList.hasFocus() );
							//SwingComponentHighlighter.temporalBlueBackground(c);
							
						}
						
						
						
						jsp.repaint();
						nameTextField.setText("");
						valTextField.setText("");
						tuTextField.setText("");
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
						
						if ( nameTextField.getText() == null || valTextField.getText() == null 
								 || nameTextField.getText().length() == 0 || valTextField.getText().length() == 0		
								) return;
						
						try{Integer.parseInt(tuTextField.getText());}
						catch(NumberFormatException nfe){return;}
							
						
						String[] nu = new String[]
												 {
								nameTextField.getText(),valTextField.getText(), tuTextField.getText()
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
							valTextField.setText(ar[1]);
							tuTextField.setText(ar[2]);
						}
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
		}
		else
		{
			delButton.setEnabled(false);
			modButton.setEnabled(false);
		}
	}

	
	public Node getXML ( Document d )
	{
		
		Element result = d.createElement("PropertyList");
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String[] desc = (String[]) listContent.get(i);
			Element propNode = d.createElement("PropertyEntry");
			
			propNode.setAttribute("name",desc[0]);
			propNode.setAttribute("value",desc[1]);
			propNode.setAttribute("timeUnitsLeft",desc[2]);
	
			result.appendChild(propNode);
		}
		
		return result;
		
	}
	
//	from PropertyList node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		Element e = (Element) n;
		if ( e == null ) return;
		//have to init ListContent as a Model of String[] { name , val , tu }
		NodeList nl = e.getElementsByTagName("PropertyEntry");
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Element peElt = (Element) nl.item(i);
			String name = peElt.getAttribute("name");
			String value = peElt.getAttribute("value");
			String tu = peElt.getAttribute("timeUnitsLeft");
			listContent.addElement(new String[]{name,value,tu});
		}
	}
	
}
