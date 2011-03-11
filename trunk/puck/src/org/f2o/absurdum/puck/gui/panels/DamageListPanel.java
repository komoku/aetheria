/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 22-abr-2008 23:55:11
 * as file DamageListPanel.java on package org.f2o.absurdum.puck.gui.panels
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJList;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
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
public class DamageListPanel extends JPanel
{

	private JList theList;
	
	private JTextField typeTextField = new EnhancedJTextField(20);
	private JTextField formulaTextField = new EnhancedJTextField(20);
	
	
	private JButton delButton = new JButton(Messages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(Messages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(Messages.getInstance().getMessage("button.mod"));
	private JButton topButton = new JButton(Messages.getInstance().getMessage("button.top"));
	
	private JLabel formulaLabel;
	private JLabel typeLabel;
	
	//privatize it
	public DefaultListModel listContent = new DefaultListModel();
	
	
	private static String typeLabelText = Messages.getInstance().getMessage("label.damage.type");
	private static String formulaLabelText = Messages.getInstance().getMessage("label.damage.formula");
	
	
	public DamageListPanel ( String borderText )
	{
		
		theList = new EnhancedJList( listContent );
		theList.setCellRenderer ( new TwoStringCellRenderer() );
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		final JScrollPane jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,45));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(jp);
		
		this.setBorder(BorderFactory.createTitledBorder(borderText));
		
		JPanel p1 = new JPanel();
		p1.add ( typeLabel = new JLabel(typeLabelText) );
		p1.add ( typeTextField );
		add(p1);
		
		JPanel p2 = new JPanel();
		p2.add ( formulaLabel = new JLabel(formulaLabelText) );
		p2.add ( formulaTextField );
		add(p2);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(addButton);
		buttonsPanel.add(modButton);
		buttonsPanel.add(delButton);
		buttonsPanel.add(topButton);
		add(buttonsPanel);
		
		addButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						String[] nu;
						
							nu = new String[]
												 {
								typeTextField.getText(), formulaTextField.getText()
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
						
						String[] nu;
					
							nu = new String[]
												 {

								typeTextField.getText(), formulaTextField.getText()
												 };
				
						listContent.set(ind,nu);
						jsp.repaint();
					}
			
				}
				);
		
		topButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						
						String[] it = (String[]) theList.getSelectedValue();
						int ind = theList.getSelectedIndex();
						if ( ind > 0 )
						{
							String[] prev = (String[]) theList.getModel().getElementAt(ind-1);

							listContent.set(ind-1,it);
							listContent.set(ind,prev);
							theList.setSelectedIndex(ind-1);
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
						String[] ar = (String[]) theList.getSelectedValue();
						String desc;
						if ( ar == null ) return;
						if ( ar[1] == null )
							desc = "";
						else
							desc = ar[1];
						typeTextField.setText(ar[0]);
						formulaTextField.setText(desc);
					}
					
				}
				);

		
	}
	
	public Node getXML ( Document d )
	{
		return getXML ( d , "DamageList" );
	}
	
	public Node getXML ( Document d , String name )
	{
		
		Element result = d.createElement(name);
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String[] desc = (String[]) listContent.get(i);
			Element descNode = d.createElement("Damage");
			if ( desc[0] != null && desc[0].length() > 0 && desc[1] != null && desc[1].length() > 0 )
			{
				descNode.setAttribute("type", desc[0]);
				descNode.setAttribute("formula", desc[1]);
				result.appendChild(descNode);
			}
		}
		return result;
		
	}
	
	//from DescriptionList node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		Element e = (Element) n;
		//have to init ListContent as a Model of String[] { type , formula }
		NodeList nl = e.getElementsByTagName("Damage");
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Element damElt = (Element) nl.item(i);	
			String typeText = damElt.getAttribute("type");
			String formulaText = damElt.getAttribute("formula");

			listContent.addElement(new String[]{typeText.trim(),formulaText.trim()});
		}
	}
	
}
