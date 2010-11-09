/*
 * (c) 2005-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 22-jul-2005 23:55:11
 * as file DescriptionListPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

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
 * Created at regulus, 22-jul-2005 23:55:11
 */
public class DescriptionListPanel extends JPanel
{

	//TODO: Completar soporte de nombre propio.
	//Para ello, hacer que el checkbox correspondiente meta un texto "Proper:" o lo que sea en la descripci�n,
	//que �ste sirva para que al clickearla se ponga el checkbox como debe,
	//pero luego que a XML se escriba m�s bien:
	//<Description properName="true"> si es propio, o sin el atributo si no lo es.
	
	
	private JList theList;
	
	private JTextField condTextField = new EnhancedJTextField(20);
	
	//private JTextField descTextField = new EnhancedJTextField(20);
	private JTextComponent descTextField;
		
	
	private JButton delButton = new JButton(Messages.getInstance().getMessage("button.del"));
	private JButton addButton = new JButton(Messages.getInstance().getMessage("button.add"));
	private JButton modButton = new JButton(Messages.getInstance().getMessage("button.mod"));
	private JButton topButton = new JButton(Messages.getInstance().getMessage("button.top"));
	
	private JLabel descLabel;
	private JLabel condLabel;
	
	//privatize it
	public DefaultListModel listContent = new DefaultListModel();
	
	//support for SUCCESS: and FAIL: descriptions on openables, closeables, etc.
	//added 2007-04-07
	private boolean succFail = false;
	private JCheckBox succCheckBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.success"));

	
	
	//support for proper name
	//beginning to add, as of 2009-01-31
	private boolean propNamesEnabled = false;
	private JCheckBox propCheckBox = new JCheckBox(Messages.getInstance().getMessage("checkbox.propername"));
	private Vector /*of Boolean*/ propList = new Vector();
	
	
	//debugging
	private DefaultListModel devVector = new DefaultListModel();
	
	//se le pasa el texto del borde y el que dice "Description:" o similar.
	public DescriptionListPanel ( String borderTitle , String descLabelText , int rows )
	{
		this(borderTitle,descLabelText,false,rows);
	}
	
	//�dem que DescriptionListPanel ( String borderTitle , String descLabelText ), pero se le pasa
	//un tercer par�metro booleano indicando si las descripciones soportan SUCCESS: y FAIL:
	public DescriptionListPanel ( String borderTitle , String descLabelText , boolean succFail , int rows )
	{
		this(succFail,false,rows);
		this.setBorder(BorderFactory.createTitledBorder(borderTitle));
		descLabel.setText(descLabelText);
	}
	
	public DescriptionListPanel ( String borderTitle , String descLabelText , String condLabelText , boolean succFail , int rows )
	{
		this( borderTitle , descLabelText , condLabelText ,  succFail , false , rows );
	}
	
	
	public DescriptionListPanel ( String borderTitle , String descLabelText , String condLabelText , boolean succFail , boolean propNamesEnabled , int rows )
	{
		this(succFail,propNamesEnabled,rows);
		this.setBorder(BorderFactory.createTitledBorder(borderTitle));
		descLabel.setText(descLabelText);
		condLabel.setText(condLabelText);
	}
	
	public DescriptionListPanel ( String borderTitle , String descLabelText , boolean succFail , boolean propNamesEnabled , int rows )
	{
		this(succFail,propNamesEnabled,rows);
		this.setBorder(BorderFactory.createTitledBorder(borderTitle));
		descLabel.setText(descLabelText);
	}
	
	public DescriptionListPanel ( )
	{
		this(1);
	}
	
	public DescriptionListPanel ( int rows )
	{
		this(false,false,rows);
	}
	
	public DescriptionListPanel ( final boolean succFail , final boolean propNamesEnabled , int rows )
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
	    
	    JScrollPane scroller = new JScrollPane(descTextField);
	    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    
		
		this.succFail = succFail;
		this.propNamesEnabled = propNamesEnabled;
		
		//listContent.setSize(100);
		theList = new JList( listContent );
		//listContent.setSize(2);
		theList.setCellRenderer ( new TwoStringCellRenderer()
		{
			 public Component getListCellRendererComponent(
				      JList list,
				      Object value,            // value to display
				      int index,               // cell index
				      boolean isSelected,      // is the cell selected
				      boolean cellHasFocus)    // the list and the cell have the focus
				    {
				 		super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
				 		if ( propNamesEnabled && propList.size() > index && ((Boolean)propList.get(index)).booleanValue() )
				 		{
				 			this.setText(this.getText() + " " + Messages.getInstance().getMessage("text.propname"));
				 		}
				 		return this;
				    }
		}
		
		);
		
		
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//this.add(new JScrollPane(theList));
		theList.setVisibleRowCount(4);
		final JScrollPane jsp = new JScrollPane(theList);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setPreferredSize(new Dimension(80,45));
		final JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(jsp,BorderLayout.CENTER);
		this.add(jp);
		//this.add(theList);
		
		this.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getMessage("label.des")));
		
		JPanel p1 = new JPanel();
		p1.add ( condLabel = new JLabel(Messages.getInstance().getMessage("label.condition")) );
		p1.add ( condTextField );
		add(p1);
		
		JPanel p2 = new JPanel();
		p2.add ( descLabel = new JLabel(Messages.getInstance().getMessage("label.description")) );
		//p2.add ( descTextField );
		p2.add(scroller);
		add(p2);
		
		if ( succFail )
		{
			JPanel p3 = new JPanel();
			p3.add ( succCheckBox );
			succCheckBox.setSelected(true);
			add(p3);
		}
		
		if ( propNamesEnabled )
		{
			JPanel p4 = new JPanel();
			p4.add ( propCheckBox );
			propCheckBox.setSelected(false);
			add(p4);
		}
		
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
					    
					        if ( descTextField.getText() == null  || descTextField.getText().length() == 0 ) return;
					    
						String[] nu;
						if ( !succFail )
							nu = new String[]
												 {
					//			"re","doxon"
								condTextField.getText(), descTextField.getText()
												 };
						else
							nu = new String[]
											{
								condTextField.getText(),
								succCheckBox.isSelected() ? "SUCCESS:"+descTextField.getText() : "FAIL:"+descTextField.getText()
											};
						
						
						listContent.addElement(nu);
						if ( propNamesEnabled )
							propList.add(Boolean.valueOf(propCheckBox.isSelected()));
						jsp.repaint();
						

						
						condTextField.setText("");
						descTextField.setText("");
						
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
							if ( propNamesEnabled )
								propList.remove(indices[i]);
						}
						
						//repaint();

						//DescriptionListPanel.this.setVisible(false);
						//DescriptionListPanel.this.setVisible(true);
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
						if ( !succFail )
							nu = new String[]
												 {
					//			"re","doxon"
								condTextField.getText(), descTextField.getText()
												 };
						else
							nu = new String[]
											{
								condTextField.getText(),
								succCheckBox.isSelected() ? "SUCCESS:"+descTextField.getText() : "FAIL:"+descTextField.getText()
											};
						
						
						if ( ind < 0 ) return; //nada que cambiar.
						
						listContent.set(ind,nu);
						
						//repaint();
						//theList.repaint();
						//DescriptionListPanel.this.setVisible(false);
						//DescriptionListPanel.this.setVisible(true);
						jsp.repaint();
						
						
						int selListIndex = theList.getSelectedIndex();
						if ( selListIndex >= 0 && propNamesEnabled )
							propList.set(selListIndex, Boolean.valueOf(propCheckBox.isSelected()) );
						
					}
			
				}
				);
		
		topButton.addActionListener ( new ActionListener() 
				{

					public void actionPerformed(ActionEvent evt) 
					{
						
						String[] it = (String[]) theList.getSelectedValue();
						int ind = theList.getSelectedIndex();
						boolean propVal = false;
						if ( propNamesEnabled ) propVal = ((Boolean)propList.get(ind)).booleanValue();
						if ( ind > 0 )
						{
							String[] prev = (String[]) theList.getModel().getElementAt(ind-1);

							listContent.set(ind-1,it);
							listContent.set(ind,prev);
							theList.setSelectedIndex(ind-1);
							
							if ( propNamesEnabled )
							{
								boolean prevProp = ((Boolean)propList.get(ind-1)).booleanValue();
								propList.set(ind-1 , Boolean.valueOf(propVal) );
								propList.set(ind , Boolean.valueOf(prevProp) );
							}
							
						//repaint();
						//theList.repaint();
						//DescriptionListPanel.this.setVisible(false);
						//DescriptionListPanel.this.setVisible(true);
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
						if ( succFail )
						{
							if ( ar[1].startsWith("SUCCESS:") )
							{
								succCheckBox.setSelected(true);
								desc = ar[1].substring("SUCCESS:".length());
							}
							else if ( ar[1].startsWith("FAIL:") )
							{
								succCheckBox.setSelected(false);
								desc = ar[1].substring("FAIL:".length());
							}
						}
						if ( propNamesEnabled )
						{
							propCheckBox.setSelected( ((Boolean)propList.get(theList.getSelectedIndex())).booleanValue() );
						}
						condTextField.setText(ar[0]);
						descTextField.setText(desc);
					}
					
				}
				);
		
		/*
		propCheckBox.addChangeListener( //nope, only on press modify
		
			new ChangeListener()
			{

				public void stateChanged(ChangeEvent arg0) 
				{
					int selListIndex = theList.getSelectedIndex();
					if ( selListIndex >= 0 )
						propList.set(selListIndex, propCheckBox.isSelected() );
				}
				
			}
		);
		*/

		
	}
	
	public Node getXML ( Document d )
	{
		return getXML ( d , "DescriptionList" );
	}
	
	public Node getXML ( Document d , String name )
	{
		
		Element result = d.createElement(name);
		
		for ( int i = 0 ; i < listContent.size() ; i++ )
		{
			String[] desc = (String[]) listContent.get(i);
			Element descNode = d.createElement("Description");
			
			if ( desc[0] != null && desc[0].length() > 0 )
			{
				Element condNode = d.createElement("Condition");
				condNode.setAttribute("language","BeanShell");
				condNode.appendChild(d.createTextNode(desc[0]));
				descNode.appendChild(condNode);
			}
			String theText = desc[1];
			
			if ( propNamesEnabled )
			{
				boolean isProper = ((Boolean)propList.get(i)).booleanValue();
				if ( isProper )
				{
					descNode.setAttribute("properName", "true");
				}
			}
			
			Text textNode = d.createTextNode(theText);
			descNode.appendChild(textNode);
			
			result.appendChild(descNode);
		}
		
		return result;
		
	}
	
	//from DescriptionList node
	public void initFromXML ( org.w3c.dom.Node n )
	{
		Element e = (Element) n;
		//have to init ListContent as a Model of String[] { cond , desc }
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
			if ( propNamesEnabled )
			{
				if ( descElt.hasAttribute("properName") && descElt.getAttribute("properName").equalsIgnoreCase("true") )
				{
					propList.add(Boolean.TRUE);
				}
				else
					propList.add(Boolean.FALSE);
			}
			listContent.addElement(new String[]{conditionText.trim(),descriptionText.trim()});
		}
	}
	
}