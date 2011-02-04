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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.f2o.absurdum.puck.gui.SpacingPanel;
import org.f2o.absurdum.puck.i18n.Messages;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextArea;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
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
public class ExtraDescriptionsPanel extends JPanel
{

	private JList theList;
	
	private JTextField condTextField = new EnhancedJTextField(20);
	private JTextComponent descTextField; 
	//JTextField descTextField = new EnhancedJTextField(20);
	private JTextField nameTextField = new EnhancedJTextField(20);
	
	private JButton delButton = new JButton(Messages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(Messages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(Messages.getInstance().getMessage("button.mod"));
	private JButton topButton = new JButton(Messages.getInstance().getMessage("button.top"));
	
	//privatize it
	private DefaultListModel listContent = new DefaultListModel();
	
	private static int MED_SKIP = 10;
	private static int SMALL_SKIP = 5;
	
	public ExtraDescriptionsPanel ( )
	{
	    this (1);
	}
	
	public ExtraDescriptionsPanel ( int rows )
	{
	    
	    	if ( rows <= 1 )
	    	    descTextField = new EnhancedJTextField(20);
	    	else
	    	{
	    	    descTextField = new EnhancedJTextArea(rows,20);
	    	    // Enable line-wrapping
	    	    ((EnhancedJTextArea)descTextField).setLineWrap(true);
	    	    ((EnhancedJTextArea)descTextField).setWrapStyleWord(true);
	    	}
	    	
	    	JScrollPane scroller = null;
	    	if ( rows > 1 )
	    	{
	    	    scroller = new JScrollPane(descTextField);
	    	    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    	}
		
		theList = new JList( listContent );
		theList.setCellRenderer ( new ThreeStringCellRenderer() );
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//this.add(new JScrollPane(theList));
		final JScrollPane jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,45));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(jp);
		//this.add(theList);
		
		this.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("label.extrades")));
		
		this.add(Box.createVerticalStrut(MED_SKIP));
		
		JPanel p01and2 = new JPanel();
		p01and2.setLayout(new BorderLayout());
		
		JPanel p0and1 = new JPanel();
		p0and1.setLayout(new BoxLayout(p0and1,BoxLayout.PAGE_AXIS));
		
		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0,BoxLayout.LINE_AXIS));
		JLabel refNamesLabel = new JLabel(Messages.getInstance().getMessage("label.refnames"));
		
		//fix html setting maximum size to maxint
		refNamesLabel.setMaximumSize(refNamesLabel.getPreferredSize());
		
		p0.add ( refNamesLabel );
		p0.add(Box.createHorizontalStrut(MED_SKIP));
		p0.add ( nameTextField );
		p0and1.add(new SpacingPanel(p0,true,true,true,true));
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1,BoxLayout.LINE_AXIS));
		p1.add ( new JLabel(Messages.getInstance().getMessage("label.condition")) );
		p1.add(Box.createHorizontalStrut(MED_SKIP));
		p1.add ( condTextField );
		p0and1.add(new SpacingPanel(p1,true,true,true,true));
		
		p01and2.add(p0and1,BorderLayout.NORTH);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2,BoxLayout.LINE_AXIS));
		p2.add ( new JLabel(Messages.getInstance().getMessage("label.description")) );
		
		p2.add(Box.createHorizontalStrut(MED_SKIP));
		
		if ( rows > 1 )
		    p2.add(scroller);
		else
		    p2.add ( descTextField );
		    
		
		p01and2.add(new SpacingPanel(p2),BorderLayout.CENTER);
		add(p01and2);
		
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
						
						if ( nameTextField.getText() == null || descTextField.getText() == null 
								 || nameTextField.getText().length() == 0 || descTextField.getText().length() == 0		
								) return;
						
						String[] nu = new String[]
												 {
								nameTextField.getText(),condTextField.getText(), descTextField.getText()
												 };
						listContent.addElement(nu);
						jsp.repaint();
						condTextField.setText("");
						descTextField.setText("");
						nameTextField.setText("");
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
						
						if ( nameTextField.getText() == null || descTextField.getText() == null 
						 || nameTextField.getText().length() == 0 || descTextField.getText().length() == 0		
						) return;
							
						
						String[] nu = new String[]
												 {
								nameTextField.getText(),condTextField.getText(), descTextField.getText()
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
						if ( ar != null ) //could be null if we have just deleted the selected value
						{
							nameTextField.setText(ar[0]);
							condTextField.setText(ar[1]);
							descTextField.setText(ar[2]);
						}
						
					}
					
				}
				);

		
	}
	
	public Node getXML ( Document d )
	{
		
		Element result = d.createElement("ExtraDescriptionList");
		
		HashMap namesToDescriptionListNodes = new HashMap(); //to group several descriptions referring to same names
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String[] desc = (String[]) listContent.get(i);
						
			Element subDescListNode;
			
			if ( namesToDescriptionListNodes.get(desc[0]) == null )
			{
				Element descNode = d.createElement("ExtraDescription");
				
				StringTokenizer st = new StringTokenizer(desc[0],","); //desc[0] = names string
				while ( st.hasMoreTokens() )
				{
					String nextName = st.nextToken().trim();
					Element nameElt = d.createElement("Name");
					nameElt.appendChild(d.createTextNode(nextName));
					descNode.appendChild(nameElt);
				}
				subDescListNode = d.createElement("DescriptionList");
				descNode.appendChild(subDescListNode);
				namesToDescriptionListNodes.put(desc[0],subDescListNode);
				
				result.appendChild(descNode);
			}
			else
			{
				subDescListNode = (Element) namesToDescriptionListNodes.get(desc[0]);
			}
				
			Element subDescNode = d.createElement("Description");
			subDescListNode.appendChild(subDescNode);
			
			if ( desc[1] != null && desc[1].length() > 0 )
			{
				Element condNode = d.createElement("Condition");
				condNode.setAttribute("language","BeanShell");
				condNode.appendChild(d.createTextNode(desc[1]));
				subDescNode.appendChild(condNode);
			}
			Text textNode = d.createTextNode(desc[2]);
			subDescNode.appendChild(textNode);
			
			
		}
		
		return result;
		
	}
	
	
//from ExtraDescriptionList node

//source: a structure of the type
// <ExtraDescriptionList>
//	<ExtraDescription>
//   <Name>tal</Name>
//   <Name>cual</Name>
//	 <DescriptionList>
//	  <Description>
//     <Condition language="BeanShell">...</Condition>
//	  Description Text
//	  </Description>
//    ...
//   </DescriptionList>
//	</ExtraDescription>
//  ...
// </ExtraDescriptionList>
	
//alternate source: the abbreviated notation,
// <ExtraDescriptionList>
//  <ExtraDescription>
//   <Name>tal</Name>
//   <Name>cual</Name>
//	 bla bla bla (text here)
//	</ExtraDescription>
//	...
// </ExtraDescriptionList>
// <--- not yet supported	
	
//target: list model must contain a list of String[]:
// name , condition , description
	
	
	public void initFromXML ( org.w3c.dom.Node n )
	{
		

		NodeList nlist = ((Element)n).getElementsByTagName("ExtraDescription");
		for ( int k = 0 ; k < nlist.getLength() ; k++ )
		{
			Element elt = (Element) nlist.item(k); //an ExtraDescription elt.
			List names = new ArrayList();
			//fetch the names!
			NodeList nameNodes = elt.getElementsByTagName("Name");
			for ( int w = 0 ; w < nameNodes.getLength() ; w++ )
			{
				Element nameNode = (Element)nameNodes.item(w);
				NodeList nameChildren = nameNode.getChildNodes();
				for ( int x = 0 ; x < nameChildren.getLength() ; x++ )
				{
					Node nameChild = (Node) nameChildren.item(x);
					if ( nameChild instanceof Text )
					{
						if ( nameChild.getNodeValue().trim().length() > 0 )
							names.add(nameChild.getNodeValue().trim());
					}
				}
			}
			
			NodeList descriptionListNodes = elt.getElementsByTagName("DescriptionList");
			Element e = (Element) descriptionListNodes.item(0);
			
			if ( e != null )
			{
				
				//read in verbose (default) notation
			
				//cp'd from DescriptionListPanel
				NodeList nl = e.getElementsByTagName("Description");
				for ( int i = 0 ; i < nl.getLength() ; i++ )
				{
					Element descElt = (Element) nl.item(i);
					NodeList condNl = descElt.getElementsByTagName("Condition");
					String conditionText = "";
					String descriptionText = "";
					if ( condNl.getLength() > 0 )
					{
						Element condElt = (Element)condNl.item(0);
						NodeList condChildren = condElt.getChildNodes();
						for ( int j = 0 ; j < condChildren.getLength() ; j++ )
						{
							Node condChild = condChildren.item(j);
							if ( condChild instanceof Text )
								conditionText = conditionText + condChild.getNodeValue();
						}
					}
					NodeList descChildren = descElt.getChildNodes();
					for ( int j = 0 ; j < descChildren.getLength() ; j++ )
					{
						Node descChild = descChildren.item(j);
						if ( descChild instanceof Text )
							descriptionText = descriptionText + descChild.getNodeValue();
					}
					//for ( int j = 0 ; j < names.size() ; j++ )
					//	listContent.addElement(new String[]{(String)names.get(j),conditionText,descriptionText});
					String commaSeparatedList = "";
					for ( int j = 0 ; j < names.size() ; j++ )
					{
						commaSeparatedList += names.get(j);
						if ( j < names.size() - 1 )
							commaSeparatedList += ",";
					}
					listContent.addElement(new String[]{commaSeparatedList,conditionText,descriptionText});
					
					
				} //foreach Description node in ExtraDescription's DescriptionList
			
			}
			
			else
			{
				
				//read in abbreviated notation
				
				//untested:
				
				NodeList nl = elt.getChildNodes();
				String descriptionText="";
				for ( int i = 0 ; i < nl.getLength(); i++ )
				{
					Node potentialTextNode = nl.item(i);
					if ( potentialTextNode instanceof Text )
					{
						Text textNode = (Text) potentialTextNode;
						descriptionText = descriptionText += textNode.getNodeValue();
					}
				}
				
				//for ( int j = 0 ; j < names.size() ; j++ )
				//	listContent.addElement(new String[]{(String)names.get(j),"",descriptionText});
				String commaSeparatedList = "";
				for ( int j = 0 ; j < names.size() ; j++ )
				{
					commaSeparatedList += names.get(j);
					if ( j < names.size() - 1 )
						commaSeparatedList += ",";
				}
				listContent.addElement(new String[]{commaSeparatedList,"",descriptionText});
				
			}
			
			
		} //foreach ExtraDescription node
				
		
		
	} //method
	
	

}
