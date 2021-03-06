/*
 * (c) 2005-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 20-jul-2005 18:51:54
 * as file EntityPanel.java on package org.f2o.absurdum.puck.gui.panels
 */
package org.f2o.absurdum.puck.gui.panels;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.f2o.absurdum.puck.bsh.BeanShellCodeHolder;
import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.Arrow;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.GraphArranger;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.gui.panels.code.BSHCodePanel;
import org.f2o.absurdum.puck.gui.panels.code.BSHCodePanelFactory;
import org.f2o.absurdum.puck.gui.panels.code.JSyntaxBSHCodePanel;
import org.f2o.absurdum.puck.gui.util.SpringUtilities;
import org.f2o.absurdum.puck.i18n.UIMessages;
import org.f2o.absurdum.puck.util.ColorUtils;
import org.f2o.absurdum.puck.util.UniqueNameEnforcer;
import org.f2o.absurdum.puck.util.debug.Debug;
import org.f2o.absurdum.puck.util.swing.EnhancedJTextField;
import org.f2o.absurdum.puck.util.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.irreality.age.windowing.TabUtils;

/**
 * @author carlos
 *
 * Created at regulus, 20-jul-2005 18:51:54
 */
public class WorldPanel extends GraphElementPanel implements BeanShellCodeHolder
{
	

	private GraphEditingPanel gep;
	

	private JTabbedPane jtp;
	
	
	/*
	private JLabel lBackground = new JLabel("Background");
	private JLabel lForeground = new JLabel("Foreground");
	private JLabel lDefault = new JLabel("Default");
	private JLabel lDescription = new JLabel("Description");
	private JLabel lInformation = new JLabel("Information");
	private JLabel lAction = new JLabel("Action");
	private JLabel lDenial = new JLabel("Denial");
	private JLabel lError = new JLabel("Error"); 
	private JLabel lStory = new JLabel("Story");
	private JLabel lInput = new JLabel("Input");
	*/
	
	/*
	private Color cBackground = Color.BLACK;
	private Color cForeground = Color.BLACK;
	private Color cDefault = Color.BLACK;
	private Color cDescription = Color.BLACK;
	private Color cInformation = Color.BLACK;
	private Color cAction = Color.BLACK;
	private Color cDenial = Color.BLACK;
	private Color cError = Color.BLACK;
	private Color cStory = Color.BLACK;
	private Color cInput = Color.BLACK;
	
	
	private JButton bBackground = new JButton("Background");
	private JButton bForeground = new JButton("Foreground");
	private JButton bDefault = new JButton("Default");
	private JButton bDescription = new JButton("Description");
	private JButton bInformation = new JButton("Information");
	private JButton bAction = new JButton("Action");
	private JButton bDenial = new JButton("Denial");
	private JButton bError = new JButton("Error"); 
	private JButton bStory = new JButton("Story");
	private JButton bInput = new JButton("Input");
	*/
	
	private final String [] colorCodes = new String[]{ "Background" , "Foreground" , "Default" , "Description" ,
			"Information" , "Action" , "Denial" , "Error" , "Story" , "Important", "Input" };
	
	private final Color [] defaultColors = new Color[]{ 
			new Color(Integer.parseInt("F7F7F7",16)), //background 
			new Color(Integer.parseInt("000000",16)), //foreground 
			new Color(Integer.parseInt("000000",16)), //default 
			new Color(Integer.parseInt("000099",16)), //description 
			new Color(Integer.parseInt("660066",16)), //information
			new Color(Integer.parseInt("006600",16)), //action 
			new Color(Integer.parseInt("660000",16)), //denial 
			new Color(Integer.parseInt("663300",16)), //error 
			new Color(Integer.parseInt("006666",16)), //story 
			new Color(Integer.parseInt("CC6600",16)), //important 
			new Color(Integer.parseInt("0066CC",16)), //input 
	};
	
	private final String [] supportedWorldLanguages = new String[]{ "es", "en", "eo", "gl", "ca" };
	private final Map languageToIndex; //es->0, etc.
	
	private HashMap colorsMap = new HashMap();
	private HashMap buttonsMap = new HashMap();
	
	public static String DEFAULT_AGE_VERSION = "1.3.6";
	
	public void setDefaultWorldVersion()
	{
		tfAgeVersion.setText(DEFAULT_AGE_VERSION);
	}
	
	public WorldPanel ( final GraphEditingPanel gep )
	{
		
		super();
		this.gep = gep;
		setDefaultWorldVersion();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		entitiesPanel = new EntityListPanel(gep);
		
		//init languages combo box and languageToIndex set in a generic way, from supported world languages
		languageToIndex = new HashMap();
		String[] languageComboBoxLines = new String[supportedWorldLanguages.length];
		for ( int i = 0 ; i < supportedWorldLanguages.length ; i++ )
		{
			languageComboBoxLines[i] = UIMessages.getInstance().getMessage("language." + supportedWorldLanguages[i]);
			languageToIndex.put(supportedWorldLanguages[i], i);
		}
		cbLanguage = new JComboBox( languageComboBoxLines );

	}
	
	private int languageToIndex ( String language )
	{
		Integer index = (Integer) languageToIndex.get(language);
		if ( index != null ) return index.intValue();
		else return 0;
	}
	
	public String toString()
	{
		return "World Panel";
	}
	
	public String getPanelName()
	{
		return "World Panel";
	}
	
	//returns the index of the object in v whose name is s.
	private int indexOf ( Vector v , String s )
	{
		for ( int i = 0 ; i < v.size() ; i++ )
		{
			if ( s.equals(v.get(i).toString()) )
				return i;
		}
		return -1;
	}
	
	private JTextField tfShortName = new EnhancedJTextField();
	private JTextField tfLongName = new EnhancedJTextField();
	
	private JComboBox cbLanguage;
	private JTextField tfAuthor = new EnhancedJTextField();
	private JTextField tfVersion = new EnhancedJTextField();
	private JTextField tfAgeVersion = new EnhancedJTextField();
	private JTextField tfDate = new EnhancedJTextField();
	private JTextField tfType = new EnhancedJTextField();
	
	//private JSyntaxBSHCodePanel bcp = new JSyntaxBSHCodePanel("world");
	private BSHCodePanel bcp = BSHCodePanelFactory.getInstance().createPanel("world");
	
	private JTextField tfFontName = new EnhancedJTextField(20);
	private JTextField tfFontFile = new EnhancedJTextField(20);
	private JTextField tfFontSize = new EnhancedJTextField(5);
	
	private EntityListPanel entitiesPanel;
	
	private void styleAndAddTwo(JPanel p, JLabel l, JComponent editable)
	{
		l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

		p.add(l);
		p.add(editable);
	}

	public void linkWithGraph()
	{   
	
		//no doughnut!
		this.removeAll();
		
		colorsMap = new HashMap();
		buttonsMap = new HashMap();
		
		//we do need a tabbed pane!
		jtp = new JTabbedPane();
		this.add(jtp);
		
		JPanel firstTab = new JPanel();
		
		firstTab.setLayout(new BoxLayout(firstTab, BoxLayout.PAGE_AXIS));
		firstTab.setBorder(BorderFactory.createTitledBorder(
		  UIMessages.getInstance().getMessage("worldpanel.header")));

		JPanel pGeneralData = new JPanel(new SpringLayout());
		
		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.shortname")),
		  tfShortName);

		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.longname")),
		  tfLongName);

		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("language") + ":"),
		  cbLanguage);

		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.author")),
		  tfAuthor);

		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.version")),
		  tfVersion);
		
		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.ageversion")),
		  tfAgeVersion);
		
		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.date")),
		  tfDate);
		
		styleAndAddTwo(pGeneralData,
		  new JLabel(UIMessages.getInstance().getMessage("world.type")),
		  tfType);

		SpringUtilities.makeCompactGrid(
		  pGeneralData,
		  8, 2,        //rows, cols
		  6, 6,        //initX, initY
		  6, 6         //xPad, yPad
		);
		
		
		firstTab.add(pGeneralData);
 		firstTab.add(bcp);
		
		jtp.add("General",firstTab);
		
		JPanel secondTab = new JPanel();
		
		secondTab.setLayout(new BoxLayout(secondTab, BoxLayout.PAGE_AXIS));
		
		JPanel colorsPanel = new JPanel( new GridLayout(0, 2) );
		colorsPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("label.colors")));
		
		final JButton[] _buttons = new JButton[colorCodes.length];
		for ( int i = 0 ; i < colorCodes.length ; i++ )
		{
			final int ind = i;
			Color color;
 			_buttons[i] = new JButton(colorCodes[i]);
			colorsPanel.add(_buttons[i]);

			color = defaultColors[i];
			colorsMap.put(colorCodes[i],color);
			buttonsMap.put(colorCodes[i],_buttons[i]);
			if ( i > 0 ) //one of the foreground colors
				_buttons[i].setForeground(color);
			_buttons[i].addActionListener( new ActionListener() 
					{
						public void actionPerformed ( ActionEvent evt )
						{
							Color c = JColorChooser.showDialog(WorldPanel.this,"Choose Color",(Color)colorsMap.get(colorCodes[ind]));
							if ( c != null )
							{
								_buttons[ind].setForeground(c);
								colorsMap.put(colorCodes[ind],c);
								if ( ind == 0 )
								{
									for ( int i = 1 ; i < colorCodes.length ; i++ )
									{
										_buttons[i].setBackground(c);
									}
								}
							}
						}
					}	
			);
		}
		
		//set background of the visual configuration color buttons to background color
		for ( int k = 1 ; k < colorCodes.length ; k++ )
			_buttons[k].setBackground(defaultColors[0]); //background color
		
		secondTab.add(colorsPanel);
		
		JPanel fontPanel = new JPanel(new SpringLayout());
		fontPanel.setMaximumSize(new Dimension(
  		  (int)fontPanel.getMaximumSize().getWidth(),
  		  (int)(18+6)*3) // FIXME: Take into account the font size.
  		);

		fontPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getInstance().getMessage("label.font")));

		tfFontName.setText("Lucida Sans Typewriter");
		styleAndAddTwo(fontPanel,
		  new JLabel(UIMessages.getInstance().getMessage("label.font.name")),
		  tfFontName
		);
		
		styleAndAddTwo(fontPanel,
		  new JLabel(UIMessages.getInstance().getMessage("label.font.filename")),
		  tfFontFile
		);
		
		tfFontSize.setText("18");
		styleAndAddTwo(fontPanel,
		  new JLabel(UIMessages.getInstance().getMessage("label.font.size")),
		  tfFontSize
		);

		SpringUtilities.makeCompactGrid(
		  fontPanel,
		  3, 2, //rows, cols
		  6, 6,        //initX, initY
		  6, 6         //xPad, yPad
		);

		secondTab.add(fontPanel);
		
		jtp.add(UIMessages.getInstance().getMessage("tab.visualconf"),secondTab);
		
		
		
		JPanel thirdTab = new JPanel();
		
		thirdTab.setLayout(new BoxLayout(thirdTab, BoxLayout.PAGE_AXIS));
		thirdTab.add(entitiesPanel);
		jtp.add(UIMessages.getInstance().getMessage("tab.entities"),thirdTab);
		TabUtils.setDefaultMnemonics(jtp);
		
	}
	
	public void refresh()
	{
		entitiesPanel.refresh();
	}

	/**
	 * Obtain the language code corresponding to the language that is selected in the world language combo box.
	 * @return
	 */
	public String getSelectedLanguageCode()
	{
		int index = cbLanguage.getSelectedIndex();
		return supportedWorldLanguages[index];
	}
	
	public org.w3c.dom.Node doGetXML ( Document d )
	{
		
		//here comes the big stuff.
		
		org.w3c.dom.Element result = d.createElement("World");
		result.setAttribute("worldName",tfShortName.getText());
		result.setAttribute("moduleName",tfLongName.getText());
		
		result.setAttribute("language",getSelectedLanguageCode());
		/*
		String languageString = (String) cbLanguage.getSelectedItem();
		if (languageString.equals(UIMessages.getInstance().getMessage("language.en")))
			result.setAttribute("language","en");
		else if (languageString.equals(UIMessages.getInstance().getMessage("language.eo")))
			result.setAttribute("language","eo");
		else if (languageString.equals(UIMessages.getInstance().getMessage("language.gl")))
			result.setAttribute("language","gl");
		else
			result.setAttribute("language","es");
		*/
		
		result.setAttribute("worldDir",".");
		result.setAttribute("author",tfAuthor.getText());
		result.setAttribute("version",tfVersion.getText());
		result.setAttribute("parserVersion",tfAgeVersion.getText());
		result.setAttribute("date",tfDate.getText());
		result.setAttribute("type",tfType.getText());
		
		
		/*check for duplicate unique names and change them if needed*/
		Set usedNames = new LinkedHashSet();
		for ( int i = 0 ; i < gep.getNodes().size() ; i++ )
		{
			Node n = (Node) gep.getNodes().get(i);
			if ( n.getAssociatedPanel() instanceof EntityPanel ) //entity node
			{
					EntityPanel ep = (EntityPanel) n.getAssociatedPanel();
					ep.checkAndIndexUniqueName();
			}
		}
		
		/*meta-inf*/
		Element metaElt = d.createElement("IdeMetaInf");
		Element viewElt = d.createElement("View");
		viewElt.setAttribute("xoffset",String.valueOf(this.getGraphEditingPanel().getViewXOffset()));
		viewElt.setAttribute("yoffset",String.valueOf(this.getGraphEditingPanel().getViewYOffset()));
		viewElt.setAttribute("zoom",String.valueOf(this.getGraphEditingPanel().getViewZoom()));
		metaElt.appendChild(viewElt);
		
		//We sort the world's nodes by unique name so that nodes are saved to the XML in a consistent order.
		//This is to make it easier to work with diffs, patches, version control, etc. on the world files.
		Vector sortedNodes = new Vector();
		sortedNodes.addAll(gep.getNodes());
		Collections.sort(sortedNodes);
		
		for ( int i = 0 ; i < sortedNodes.size() ; i++ )
		{
			Element nodeElt = d.createElement("Node");
			Node n = (Node) sortedNodes.get(i);
			nodeElt.setAttribute("name",n.getName());
			nodeElt.setAttribute("class",n.getClass().getName());
			nodeElt.setAttribute("x",String.valueOf(n.getBounds().x));
			nodeElt.setAttribute("y",String.valueOf(n.getBounds().y));
			metaElt.appendChild(nodeElt);
		}
		result.appendChild(metaElt);
		
		
		if ( bcp.getXML(d) != null )
			result.appendChild(bcp.getXML(d));
		
		/*visual conf*/
		
		Element visConfElt = d.createElement("VisualConfiguration");
		Element colorsElt = d.createElement("Colors");
		visConfElt.appendChild(colorsElt);
		Iterator codesIter = colorsMap.keySet().iterator();
		while ( codesIter.hasNext() )
		{
			String colorCode = (String) codesIter.next();
			Color color = (Color) colorsMap.get(colorCode);
			if ( color == null ) System.err.println(colorCode + " gives us null!");
			Element colorElt = d.createElement(colorCode);
			
			colorElt.setAttribute("color","#"+ColorUtils.colorToString(color));
			colorsElt.appendChild(colorElt);
		}
		
		Element fontElt = d.createElement("Font");
		fontElt.setAttribute("name","Arial");
		fontElt.setAttribute("size","18.0");
		fontElt.setAttribute("filename","");
		
		fontElt.setAttribute("name", tfFontName.getText());
		fontElt.setAttribute("filename", tfFontFile.getText());
		double size = 18.0;
		try
		{
			size = Double.valueOf(tfFontSize.getText()).doubleValue();
		}
		catch ( NumberFormatException nfe )
		{
			System.err.println("Illegal value in font size field (must be a number): size set to default value 18.0");
		}
		fontElt.setAttribute("size",String.valueOf(size));
		
		visConfElt.appendChild(fontElt);
		
			

		Element roomsElt = d.createElement("Rooms");
		List roomNodes = new Vector();
		roomNodes.addAll(gep.getRoomNodes(false));
		Collections.sort(roomNodes);
		for ( int i = 0 ; i < roomNodes.size() ; i++ )
		{
			if ( roomNodes.get(i) instanceof RoomNode )
			{
				RoomNode rn = (RoomNode)roomNodes.get(i);
				roomsElt.appendChild( rn.getAssociatedPanel().getXML(d) );
			}
		}
		
		Element itemsElt = d.createElement("Items");
		List itemNodes = new Vector();
		itemNodes.addAll(gep.getItemNodes(false));
		Collections.sort(itemNodes);
		for ( int i = 0 ; i < itemNodes.size() ; i++ )
		{
			if ( itemNodes.get(i) instanceof ItemNode )
			{
				ItemNode rn = (ItemNode)itemNodes.get(i);
				itemsElt.appendChild( rn.getAssociatedPanel().getXML(d) );
			}
		}
		
		Element mobsElt = d.createElement("Mobiles");
		Element playersElt = d.createElement("PlayerList");
		List charNodes = new Vector();
		charNodes.addAll(gep.getCharNodes(false));
		Collections.sort(charNodes);
		for ( int i = 0 ; i < charNodes.size() ; i++ )
		{
			if ( charNodes.get(i) instanceof CharacterNode )
			{
				CharacterNode cn = (CharacterNode)charNodes.get(i);
				Element mobNode = (Element) cn.getAssociatedPanel().getXML(d);
				
				//establish "current room" and "last room" info from relationships
				for ( int j = 0 ; j < roomNodes.size() ; j++ )
				{
					if ( roomNodes.get(j) instanceof RoomNode )
					{
						RoomNode rn = (RoomNode)roomNodes.get(j);
						List roomArrows = rn.getArrows();
						for ( int k = 0 ; k < roomArrows.size() ; k++ )
						{
							Arrow a = (Arrow) roomArrows.get(k);
							if ( a.getDestination().equals(cn) )
							{
								Element curRoomElt = d.createElement("CurrentRoom");
								curRoomElt.setAttribute("id",rn.getName());
								Element lastRoomElt = d.createElement("LastRoom");
								lastRoomElt.setAttribute("id",rn.getName());
								if ( mobNode.getElementsByTagName("CurrentRoom").getLength() == 0 )
								{
									mobNode.appendChild(curRoomElt);
									mobNode.appendChild(lastRoomElt);
								}
							}
						}
					}
				}
				
				mobsElt.appendChild( mobNode );
				
				//if the creature is a player, add it to the player list
				CharPanel cp = (CharPanel) cn.getAssociatedPanel();
				if ( cp.isPlayer() )
				{
					Element playerElt = d.createElement("Player");
					playerElt.setAttribute("id",cn.getName());
					playersElt.appendChild(playerElt);
				}
				
			}
		}
		
		Element abstractEntitiesElt = d.createElement("AbstractEntities");
		List abstractEntityNodes = new Vector();
		abstractEntityNodes.addAll(gep.getNodes(AbstractEntityNode.class,false));
		Collections.sort(abstractEntityNodes);
		for ( int i = 0 ; i < abstractEntityNodes.size() ; i++ )
		{
			if ( abstractEntityNodes.get(i) instanceof AbstractEntityNode )
			{
				AbstractEntityNode rn = (AbstractEntityNode)abstractEntityNodes.get(i);
				abstractEntitiesElt.appendChild( rn.getAssociatedPanel().getXML(d) );
			}
		}
		
		Element spellsElt = d.createElement("Spells");
		List spellNodes = new Vector();
		spellNodes.addAll(gep.getNodes(SpellNode.class,false));
		Collections.sort(spellNodes);
		for ( int i = 0 ; i < spellNodes.size() ; i++ )
		{
			if ( spellNodes.get(i) instanceof SpellNode )
			{
				Node rn = (Node)spellNodes.get(i);
				spellsElt.appendChild( rn.getAssociatedPanel().getXML(d) );
			}
		}
		
		result.appendChild(playersElt);
		result.appendChild(visConfElt);
		result.appendChild(roomsElt);
		result.appendChild(itemsElt);
		result.appendChild(mobsElt);
		result.appendChild(spellsElt);
		result.appendChild(abstractEntitiesElt);
	
		
		return result;
		
	}
	
	
	//gets coordinates of an entity from the meta-inf node.
	//if the info cannot be found in the meta-inf node, it returns null.
	private Point findCoords ( org.w3c.dom.Node metaInf , String name )
	{
		if ( metaInf == null ) return null;
		Element e = (Element) metaInf;
		NodeList nl = e.getElementsByTagName("Node");
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Element elt = (Element) nl.item(i);
			String eltName = elt.getAttribute("name");
			if ( name.equals(eltName) )
			{
				int x = Integer.valueOf(elt.getAttribute("x")).intValue();
				int y = Integer.valueOf(elt.getAttribute("y")).intValue();
				return new Point(x,y);
			}
		}
		return null;
	}
	
	
	//gets display size of an entity from the meta-inf node.
	//if the info cannot be found in the meta-inf node, it returns -1.
	private int findDisplaySize ( org.w3c.dom.Node metaInf , String name )
	{
		if ( metaInf == null ) return -1;
		Element e = (Element) metaInf;
		NodeList nl = e.getElementsByTagName("Node");
		for ( int i = 0 ; i < nl.getLength() ; i++ )
		{
			Element elt = (Element) nl.item(i);
			String eltName = elt.getAttribute("name");
			if ( name.equals(eltName) )
			{
				if ( elt.hasAttribute("displaySize") )
					return Integer.valueOf(elt.getAttribute("displaySize")).intValue();
				else
					return -1;
			}
		}
		return -1;
	}
	
	
	/**
	 * Assigns random coordinates to some object when it is read from a file not created by PUCK
	 * (and therefore with unspecified -null- coordinates).
	 * @param coords A point representing the coordinates of an object in the world graph.
	 * @return If coords is not null, it returns it (the same instance), else, a new random point is created
	 * and returned.
	 */
	private Point assignRandomCoordsIfNeeded ( Point coords )
	{
		if ( coords == null )
		{
			Point coords2 = new Point();
			coords2.x = 200 + (int) (Math.random()*100);
			coords2.y = 200 + (int) (Math.random()*100);
			return coords2;
		}
		else return coords;
	}
	
	
	/**
	 * Auxiliary method called by initFromXML (org.w3c.dom.Node).
	 * Reads the RelationshipList element and initialises custom relationships.
	 * @param n The entity node whose relationships will be initialised.
	 * @param namesToNodes A map converting the names and numeric ID's of entities to PUCK nodes.
	 * @param destNodeToArrow A map converting a PUCK node X to the arrow from the node associated to n to X, if such an arrow exists.
	 */
	public void initCustomRelationshipsFromXML ( org.w3c.dom.Node n , Map namesToNodes , Map destNodeToArrow )
	{
		//custom relationship
		Element e = (Element)n;
		NodeList relListNodes = e.getElementsByTagName("RelationshipList");
		String nodeName = e.getAttribute("name");
		Node src = (Node) namesToNodes.get(nodeName);
		if ( relListNodes.getLength() > 0 )
		{
			Element relListNode = (Element) relListNodes.item(0);
			NodeList relationshipNodes = relListNode.getElementsByTagName("Relationship");
			for ( int j = 0 ; j < relationshipNodes.getLength() ; j++ )
			{
				Element relationshipNode = (Element) relationshipNodes.item(j);
				String dest = relationshipNode.getAttribute("id");
				Node dst = (Node) namesToNodes.get(dest);
				if ( dst == null )
				{
					System.out.println("Warning: null destination node (rels).");
					System.out.println("Target name (relationship id): " + dest);
					System.out.println(e);
					System.out.println(e.getAttribute("id"));
					continue;
				}
				Arrow theArrow = (Arrow) destNodeToArrow.get(dst);
				if ( theArrow == null )
				{
					/*
					 * If we enter this if statement, there is no structural relationship
					 * between the nodes, only custom relationships. So we create a new arrow between
					 * the nodes (since none has been created during the initialization of structural
					 * relationships) and set the structural relationship type of its associated
					 * panel to "none".
					 */
					theArrow = new StructuralArrow();
					destNodeToArrow.put(dst,theArrow);
					theArrow.setSource(src); 
					theArrow.setDestination(dst);
					src.addArrow(theArrow);
					if ( theArrow.getAssociatedPanel() instanceof ArrowPanel ) //TODO When custom relationship system is completed, this if check won't be necessary.
						((ArrowPanel)theArrow.getAssociatedPanel()).setRelationshipType(ArrowPanel.NO_STRUCTURAL_RELATIONSHIP);
					gep.getPropertiesPanel().loadWithoutShowing(theArrow);
					theArrow.getAssociatedPanel().initFromXML(relationshipNode); //panel init		
				}
				if ( theArrow.getAssociatedPanel() instanceof ArrowPanel ) //TODO When custom relationship system is completed, this if check won't be necessary.
					((ArrowPanel)theArrow.getAssociatedPanel()).initCustomRelationshipsFromXML(relationshipNode);
			}
		}
	}
	

	/**
	 * 
	 * @param entityNode The valid XML element defining an entity.
	 * @param namesToNodes Names-to-nodes map. Unused parameter.
	 * @param dispSize Display size for the node, -1 for default size (e.g. for pasted nodes).
	 * @param coords (x,y) coordinates to add the node. May be null for random coordinates.
	 */
	public void addEntityFromXML ( Element entityNode , Map unusedParameter , int dispSize , Point coords )
	{
		String entityName = entityNode.getAttribute("name");
		//entityName = UniqueNameEnforcer.makeUnique(entityName,namesToNodes); //didn't work, because then we call initFromXML which ignores this
		String entityId = entityNode.getAttribute("id");
		coords = assignRandomCoordsIfNeeded(coords);
		
		//add the node
		String type = entityNode.getTagName();
		Node rn;
		if ( type.equalsIgnoreCase("Room") )
			rn = new RoomNode(coords.x,coords.y);
		else if ( type.equalsIgnoreCase("Item") )
			rn = new ItemNode(coords.x,coords.y);
		else if ( type.equalsIgnoreCase("Mobile") )
			rn = new CharacterNode(coords.x,coords.y);
		else if ( type.equalsIgnoreCase("Spell") )
			rn = new SpellNode(coords.x,coords.y);
		else if ( type.equalsIgnoreCase("AbstractEntity") )
			rn = new AbstractEntityNode(coords.x,coords.y);
		else
			rn = null;
		
		//this also updates the maps:
		gep.addNode(rn);
		
		//this is currently redundant, done by gep.addNode(...), probably can remove <- no, not the ID! the ID is not redundant.
		if ( namesToNodes != null ) //should not be null if called when building the world. Should be null if called from a paste action, f. ex.
		{
			namesToNodes.put(entityName,rn);
			nodesToNames.put(rn, entityName);
			if ( entityId.length() > 0 )
			{
				namesToNodes.put(entityId,rn);
				//idsToNames.put(roomId,roomName);
			}
		}
		
		if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);

		gep.getPropertiesPanel().loadWithoutShowing(rn);
		rn.getAssociatedPanel().initFromXML(entityNode);
		rn.getAssociatedPanel().initMinimal(); //updated with data from xml
		panelsToNodes.put(rn.getAssociatedPanel(),rn);
				
		
	}
	
	/**
	 * @param entityNode The valid XML element defining an entity.
	 * @param metaInfNode The meta-inf node with info like coordinates of nodes, their size, etc. May be null.
	 * @param namesToNodes Names-to-nodes map. May be null.
	 * @param changeNameIfNotUnique Generate an unique name for the node if it's not already unique?
	 */
	public void addEntityFromXML ( Element entityNode, Element metaInfNode , Map namesToNodes )
	{
		String entityName = entityNode.getAttribute("name");
		//entityName = UniqueNameEnforcer.makeUnique(entityName,namesToNodes); //didn't work, because then we call initFromXML which ignores this
		String entityId = entityNode.getAttribute("id");
		Point coords = findCoords(metaInfNode,entityName);
		int dispSize = findDisplaySize(metaInfNode,entityName);
		addEntityFromXML ( entityNode , namesToNodes , dispSize , coords );
	}
	
	

	/**This map converts both unique names and legacy numeric ID's of entities to their associated PUCK map nodes.
	 * It is used for searches and to check that no two nodes are given the same unique name.*/
	private Map namesToNodes = new TreeMap();
	
	/**This map converts PUCK entity nodes to their unique names that are stored in namesToNodes (but not to numeric ID's). The
	 * only purpose of this map is actually to be able to update namesToNodes when a node changes its name.*/
	private Map nodesToNames = new HashMap();
	
	private Map panelsToNodes = new HashMap();
	
	
	public Node nameToNode ( String name )
	{
		return (Node) namesToNodes.get(name);
	}
	
	public String nodeToName ( Node node )
	{
		return (String) nodesToNames.get(node);
	}
	
	public Node panelToNode ( EntityPanel panel )
	{
		return (Node) panelsToNodes.get(panel);
	}
	
	public void updateMaps ( Node node )
	{
		GraphElementPanel panel = node.getAssociatedPanel();
		String name = node.getName(); //note that getAssociatedPanel() may set the name if this is a newly created node -> order of these two lines is important
		String oldName = (String) nodesToNames.get(node);
		nodesToNames.remove(node);
		nodesToNames.put(node,name);
		if ( oldName != null )
			namesToNodes.remove(oldName);
		namesToNodes.put(name,node);
		panelsToNodes.remove(panel);
		panelsToNodes.put(panel,node);
	}
	
	public void removeFromMaps ( Node node) 
	{
		String name = node.getName();
		GraphElementPanel panel = node.getAssociatedPanel();
		nodesToNames.remove(node);
		namesToNodes.remove(name);
		panelsToNodes.remove(panel);
	}
	
	
//	from World node... the big great initializer, initializing the GEP too!!!
	public void doInitFromXML ( org.w3c.dom.Node n )
	{
				
		Element e = (Element) n;
		
		//gep.getPropertiesPanel().loadWithoutShowing(gep.getWorldNode()); //includes linkWithGraph() -> no longer
		gep.getPropertiesPanel().show(gep.getWorldNode()); //else we won't init it correctly
		
		
		//init tf's
		Debug.println("[init world] Loading general world info...");
		tfShortName.setText(e.getAttribute("worldName"));
		tfLongName.setText(e.getAttribute("moduleName"));
		
		//languages combo box
		/*
		if ( e.hasAttribute("language") && e.getAttribute("language").equals("en") )
			cbLanguage.setSelectedIndex(1);
		else if ( e.hasAttribute("language") && e.getAttribute("language").equals("eo") )
			cbLanguage.setSelectedIndex(2);
		else if ( e.hasAttribute("language") && e.getAttribute("language").equals("gl") )
			cbLanguage.setSelectedIndex(3);
		else if ( e.hasAttribute("language") && e.getAttribute("language").equals("ca") )
			cbLanguage.setSelectedIndex(4);
		else
			cbLanguage.setSelectedIndex(0);
		*/
		cbLanguage.setSelectedIndex(languageToIndex(e.getAttribute("language")));
		
		tfAuthor.setText(e.getAttribute("author"));
		tfVersion.setText(e.getAttribute("version"));
		tfAgeVersion.setText(e.getAttribute("parserVersion"));
		tfDate.setText(e.getAttribute("date"));
		tfType.setText(e.getAttribute("type"));
		
		//bsh code panel
		Debug.println("[init world] Loading world bsh code...");
		NodeList codeNl = DOMUtils.getDirectChildrenElementsByTagName(e,"Code"); // e.getElementsByTagName("Code");
		if ( codeNl.getLength() > 0 )
		{
			Element codeListElt = (Element) codeNl.item(0);
			bcp.initFromXML(codeListElt);
		}
		
		//get the meta-inf
		Debug.println("[init world] Loading meta-inf...");
		NodeList metaInfNodes = e.getElementsByTagName("IdeMetaInf");
		if ( metaInfNodes.getLength() == 0 )
			System.err.println("LACKING META INF NODE");
			//TODO Better error recovery here.
		
		Element metaInfNode = (Element) metaInfNodes.item(0);
		
		if ( metaInfNode != null )
		{
			NodeList viewNodes = metaInfNode.getElementsByTagName("View");
			if ( viewNodes.getLength() > 0 )
			{
				Element viewNode = (Element) viewNodes.item(0);
				try
				{
					gep.setViewXOffset(Double.parseDouble(viewNode.getAttribute("xoffset")));
					gep.setViewYOffset(Double.parseDouble(viewNode.getAttribute("yoffset")));
					gep.setViewZoom(Double.parseDouble(viewNode.getAttribute("zoom")));
				}
				catch ( NumberFormatException nfe )
				{
					System.out.println("Number format error in meta-inf's view node attributes");
				}
			}
		}
		
		//colors panel
		Debug.println("[init world] Loading colours and fonts...");
		NodeList visConfNodes = e.getElementsByTagName("VisualConfiguration");
		if ( visConfNodes.getLength() > 0 )
		{
			Element visConfNode = (Element) visConfNodes.item(0);
			//Iterator codesIter = Arrays.asList(colorCodes).iterator();
			Iterator codesIter = colorsMap.keySet().iterator();
			while ( codesIter.hasNext() )
			{
				String colorCode = (String) codesIter.next();
				NodeList interestingElts = visConfNode.getElementsByTagName(colorCode);
				if ( interestingElts.getLength() > 0 )
				{
					Element interestingElt = (Element) interestingElts.item(0);
					String colorValue = interestingElt.getAttribute("color");
					Color color = ColorUtils.stringToColor(colorValue);
					if ( color != null )
					{
						colorsMap.put(colorCode,color);
						JButton b = (JButton) buttonsMap.get(colorCode);
						if ( "Background".equals(colorCode) )
						{
							/*set backgrounds of all buttons (except for "Background" button) to the background colour code*/
							for ( int i = 0 ; i < colorCodes.length ; i++ )
							{
								JButton bToSet = (JButton) buttonsMap.get(colorCodes[i]);
								if ( bToSet != b )
								{
									bToSet.setBackground(color);
								}
							}
						}
						else
						{
							b.setForeground(color);
							//b.setVisible(false);
							//b.setVisible(true);
							//b.repaint();
							//this.repaint();
						}
					}
				}
			}
			this.repaint();
		}
		
		NodeList fontNodes = e.getElementsByTagName("Font");
		if ( fontNodes.getLength() > 0 )
		{
			Element fontNode = (Element) fontNodes.item(0);
			tfFontName.setText(fontNode.getAttribute("name"));
			tfFontFile.setText(fontNode.getAttribute("filename"));
			tfFontSize.setText(fontNode.getAttribute("size"));
		}
		
		
		//and now... build everything! yay!
		Debug.println("[init world] Fetching entity nodes...");
		NodeList roomNodes = e.getElementsByTagName("Room");
		NodeList itemNodes = e.getElementsByTagName("Item");
		NodeList charNodes = e.getElementsByTagName("Mobile");
		NodeList abstractEntityNodes = e.getElementsByTagName("AbstractEntity");
		NodeList spellNodes = e.getElementsByTagName("Spell");
		
		//converts names of nodes (and legacy numeric ID's, if present) to nodes
		namesToNodes = new HashMap();	
		
		nodesToNames = new HashMap();
		panelsToNodes = new HashMap();
		
		if ( metaInfNode == null )
		{
			JOptionPane.showMessageDialog(this,UIMessages.getInstance().getMessage("warning.nometa.text"),UIMessages.getInstance().getMessage("warning.nometa.title"),JOptionPane.WARNING_MESSAGE);
		}
				
		//add room nodes
		Debug.println("[init world] Processing room nodes...");
		for ( int i = 0 ; i < roomNodes.getLength() ; i++ )
		{
			
			Element roomNode = (Element) roomNodes.item(i);
			/*
			String roomName = roomNode.getAttribute("name");
			String roomId = roomNode.getAttribute("id");
			Point coords = findCoords(metaInfNode,roomName);
			int dispSize = findDisplaySize(metaInfNode,roomName);
			
			coords = assignRandomCoordsIfNeeded(coords);
			
			//add the room node
			RoomNode rn = new RoomNode(coords.x,coords.y);
			gep.addNode(rn);
			namesToNodes.put(roomName,rn);
			if ( roomId.length() > 0 )
			{
				namesToNodes.put(roomId,rn);
				//idsToNames.put(roomId,roomName);
			}
			if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);
				
			//rn.getAssociatedPanel().setGraphEditingPanel(gep);
			gep.getPropertiesPanel().loadWithoutShowing(rn);
			rn.getAssociatedPanel().initFromXML(roomNode); //panel init
			*/
			addEntityFromXML(roomNode,metaInfNode,namesToNodes);
		}
		
		//add char (mob) nodes
		Debug.println("[init world] Processing character nodes...");
		for ( int i = 0 ; i < charNodes.getLength() ; i++ )
		{
			Element charNode = (Element) charNodes.item(i);
			/*
			String charName = charNode.getAttribute("name");
			String charId = charNode.getAttribute("id");
			Point coords = findCoords(metaInfNode,charName);
			int dispSize = findDisplaySize(metaInfNode,charName);
			
			coords = assignRandomCoordsIfNeeded(coords);
			
			//add the mob node
			CharacterNode rn = new CharacterNode(coords.x,coords.y);
			gep.addNode(rn);
			namesToNodes.put(charName,rn);
			if ( charId.length() > 0 ) 
			{
				namesToNodes.put(charId,rn);
				idsToNames.put(charId,charName);
			}
			if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);
				
			//rn.getAssociatedPanel().setGraphEditingPanel(gep);
			gep.getPropertiesPanel().loadWithoutShowing(rn);
			rn.getAssociatedPanel().initFromXML(charNode); //panel init
			*/
			addEntityFromXML(charNode,metaInfNode,namesToNodes);
		}
		
		//add item nodes
		Debug.println("[init world] Processing item nodes...");
		for ( int i = 0 ; i < itemNodes.getLength() ; i++ )
		{
			Element itemNode = (Element) itemNodes.item(i);
			/*
			String itemName = itemNode.getAttribute("name");
			String itemId = itemNode.getAttribute("id");
			Point coords = findCoords(metaInfNode,itemName);
			int dispSize = findDisplaySize(metaInfNode,itemName);
			
			coords = assignRandomCoordsIfNeeded(coords);
			
			//add the item node
			ItemNode rn = new ItemNode(coords.x,coords.y);
			gep.addNode(rn);
			namesToNodes.put(itemName,rn);
			if ( itemId.length() > 0 ) 
			{
				namesToNodes.put(itemId,rn);
				idsToNames.put(itemId,itemName);
			}
			if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);
			
			//rn.getAssociatedPanel().setGraphEditingPanel(gep);
			gep.getPropertiesPanel().loadWithoutShowing(rn);
			rn.getAssociatedPanel().initFromXML(itemNode); //panel init
			*/
			addEntityFromXML(itemNode,metaInfNode,namesToNodes);
		}
		
		//add abstract entity nodes
		Debug.println("[init world] Processing abstract entity nodes...");
		for ( int i = 0 ; i < abstractEntityNodes.getLength() ; i++ )
		{
			Element abstractEntityNode = (Element) abstractEntityNodes.item(i);
			/*
			String abstractEntityName = abstractEntityNode.getAttribute("name");
			String abstractEntityId = abstractEntityNode.getAttribute("id");
			Point coords = findCoords(metaInfNode,abstractEntityName);
			int dispSize = findDisplaySize(metaInfNode,abstractEntityName);
			
			coords = assignRandomCoordsIfNeeded(coords);
			
			//add the item node
			AbstractEntityNode rn = new AbstractEntityNode(coords.x,coords.y);
			gep.addNode(rn);
			namesToNodes.put(abstractEntityName,rn);
			if ( abstractEntityId.length() > 0 ) 
			{
				namesToNodes.put(abstractEntityId,rn);
				idsToNames.put(abstractEntityId,abstractEntityName);
			}
			if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);
			
			gep.getPropertiesPanel().loadWithoutShowing(rn);
			rn.getAssociatedPanel().initFromXML(abstractEntityNode); //panel init
			*/
			addEntityFromXML(abstractEntityNode,metaInfNode,namesToNodes);
		}
		
		//add spell nodes
		Debug.println("[init world] Processing spell nodes...");
		for ( int i = 0 ; i < spellNodes.getLength() ; i++ )
		{
			Element spellNode = (Element) spellNodes.item(i);
			/*
			String spellName = spellNode.getAttribute("name");
			String spellId = spellNode.getAttribute("id");
			Point coords = findCoords(metaInfNode,spellName);
			int dispSize = findDisplaySize(metaInfNode,spellName);
			
			coords = assignRandomCoordsIfNeeded(coords);
			
			//add the item node
			Node rn = new SpellNode(coords.x,coords.y);
			gep.addNode(rn);
			namesToNodes.put(spellName,rn);
			if ( spellId.length() > 0 ) 
			{
				namesToNodes.put(spellId,rn);
				idsToNames.put(spellId,spellName);
			}
			if ( dispSize >= 0 ) rn.setExplicitSize(dispSize);
			
			gep.getPropertiesPanel().loadWithoutShowing(rn);
			rn.getAssociatedPanel().initFromXML(spellNode); //panel init
			*/
			addEntityFromXML(spellNode,metaInfNode,namesToNodes);
		}
		
		//which chars are players?
		Debug.println("[init world] Processing player list...");
		NodeList playerListNodes = e.getElementsByTagName("PlayerList");
		if ( playerListNodes.getLength() > 0 )
		{
			Element playerListNode = (Element) playerListNodes.item(0);
			NodeList playerNodes = playerListNode.getElementsByTagName("Player");
			for ( int i = 0 ; i < playerNodes.getLength() ; i++ )
			{
				Element playerNode = (Element) playerNodes.item(i);
				String playerId = playerNode.getAttribute("id");
				Node plNode = (Node) namesToNodes.get(playerId);
				CharPanel cp = (CharPanel)plNode.getAssociatedPanel();
				cp.setPlayer(true);
			}
		}
		
		//add path arrows, room inventory arrows, and init panels
		Debug.println("[init world] Processing room relationships...");
		for ( int i = 0 ; i < roomNodes.getLength() ; i++ )
		{
			Element roomNode = (Element) roomNodes.item(i);
			String roomName = roomNode.getAttribute("name");
			Node src = (Node) namesToNodes.get(roomName);
			
			//this map converts destinations to arrows in the context of this source node
			//so that custom relationships can be added to the same arrow where structural
			//relationships have been initted, if it exists
			Map destNodeToArrow = new HashMap();
						
			//structural room->room (path) relationships
			NodeList pathNodes = roomNode.getElementsByTagName("Path");
			for ( int j = 0 ; j < pathNodes.getLength() ; j++ )
			{
				Element pathNode = (Element) pathNodes.item(j);
				String pathDest = pathNode.getAttribute("destination");
				
				Node dst = (Node) namesToNodes.get(pathDest);
				Arrow pathArrow = new StructuralArrow();
				destNodeToArrow.put(dst,pathArrow);
				pathArrow.setSource(src);
				pathArrow.setDestination(dst);
				src.addArrow(pathArrow);
				gep.getPropertiesPanel().loadWithoutShowing(pathArrow);
				//pathArrow.getAssociatedPanel().setGraphEditingPanel(gep);
				//door
				NodeList doorNodes = pathNode.getElementsByTagName("AssociatedItem");
				if ( doorNodes.getLength() > 0 )
				{
					Element doorNode = (Element) doorNodes.item(0);
					String doorId = doorNode.getAttribute("id");
					ItemNode doorGraphNode = (ItemNode) namesToNodes.get(doorId);
					((PathPanel)pathArrow.getAssociatedPanel()).setDoor(doorGraphNode);
				}
				pathArrow.getAssociatedPanel().initFromXML(pathNode); //panel init				
			}
			
			//structural room->item (inventory) relationships
			NodeList inventoryNodes = DOMUtils.getDirectChildrenElementsByTagName(roomNode,"Inventory");
			Element inventoryNode = (Element) inventoryNodes.item(0);
			if ( inventoryNode != null )
			{
				
				NodeList itemRefNodes = inventoryNode.getElementsByTagName("ItemRef");
				
				for ( int j = 0 ; j < itemRefNodes.getLength() ; j++ )
				{
					Element itemRefNode = (Element) itemRefNodes.item(j);
					String dest = itemRefNode.getAttribute("id");
					
					Node dst = (Node) namesToNodes.get(dest);
					if ( dst == null )
					{
						System.out.println("Warning: null destination node.");
						System.out.println(itemRefNode);
						System.out.println(itemRefNode.getAttribute("id"));
					}
					Arrow arrow = new StructuralArrow();
					destNodeToArrow.put(dst,arrow);
					arrow.setSource(src);
					arrow.setDestination(dst);
					src.addArrow(arrow);
					//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
					gep.getPropertiesPanel().loadWithoutShowing(arrow);
					arrow.getAssociatedPanel().initFromXML(itemRefNode); //panel init
				}
				
			}
			
			//structural room->mob (mobile list) relationships
			NodeList moblistNodes = roomNode.getElementsByTagName("MobileList");
			if ( moblistNodes != null )
			{
				
				Element moblistNode = (Element) moblistNodes.item(0);
				
				if ( moblistNode != null )
				{

					NodeList mobRefNodes = moblistNode.getElementsByTagName("MobRef");
					
					for ( int j = 0 ; j < mobRefNodes.getLength() ; j++ )
					{
						Element mobRefNode = (Element) mobRefNodes.item(j);
						String dest = mobRefNode.getAttribute("id");
						
						Node dst = (Node) namesToNodes.get(dest);
						if ( dst == null )
						{
							System.out.println("Warning: null destination node.");
							System.out.println(mobRefNode);
							System.out.println(mobRefNode.getAttribute("id"));
						}
						Arrow arrow = new StructuralArrow();
						destNodeToArrow.put(dst,arrow);
						arrow.setSource(src);
						arrow.setDestination(dst);
						src.addArrow(arrow);
						//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
						gep.getPropertiesPanel().loadWithoutShowing(arrow);
						arrow.getAssociatedPanel().initFromXML(mobRefNode); //panel init
					}
				
				}

			
			}
			
			//non-structural (custom) relationships
			initCustomRelationshipsFromXML ( roomNode , namesToNodes , destNodeToArrow );
			

			
			
		}
		
		//add char arrows, init panels
		Debug.println("[init world] Processing character relationships...");
		for ( int i = 0 ; i < charNodes.getLength() ; i++ )
		{
			Element charNode = (Element) charNodes.item(i);
			String charName = charNode.getAttribute("name");
			Node src = (Node) namesToNodes.get(charName);
			Map destNodeToArrow = new HashMap();
			
			NodeList inventoryNodes = 
				DOMUtils.getDirectChildrenElementsByTagName(charNode,"Inventory");
			
			if ( inventoryNodes.getLength() > 0 )
			{
				
				Element inventoryNode = (Element) inventoryNodes.item(0);
				
				initEntityToItemRelationships ( namesToNodes , src , inventoryNode , destNodeToArrow , UIMessages.getInstance().getMessage("structural.char.item.carry") );
				
			}
			
			NodeList partsNodes =
				DOMUtils.getDirectChildrenElementsByTagName(charNode,"Parts");

			if ( partsNodes.getLength() > 0 )
			{
				Element partsNode = (Element) partsNodes.item(0);
				NodeList partsInventoryNodes =
					DOMUtils.getDirectChildrenElementsByTagName(partsNode,"Inventory");
				if ( partsInventoryNodes.getLength() > 0 )
				{
					Element partsInventoryNode = (Element) partsInventoryNodes.item(0);
					initEntityToItemRelationships ( namesToNodes , src , partsInventoryNode , destNodeToArrow , UIMessages.getInstance().getMessage("structural.char.item.haspart") );
				}
			}
			
			/*
			Element inventoryNode = (Element) inventoryNodes.item(0);
			NodeList itemRefNodes = inventoryNode.getElementsByTagName("ItemRef");
			
			
			for ( int j = 0 ; j < itemRefNodes.getLength() ; j++ )
			{
				Element itemRefNode = (Element) itemRefNodes.item(j);
				String dest = itemRefNode.getAttribute("id");
				
				Node dst = (Node) namesToNodes.get(dest);
				Arrow arrow = new StructuralArrow();
				arrow.setSource(src);
				arrow.setDestination(dst);
				src.addArrow(arrow);
				//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
				gep.getPropertiesPanel().loadWithoutShowing(arrow);
				arrow.getAssociatedPanel().initFromXML(itemRefNode); //panel init
			}
			*/
			
			
			//structural char->spell (spell list) relationships
			NodeList spellListNodes = charNode.getElementsByTagName("SpellList");
			if ( spellListNodes != null )
			{
				
				Element spellListNode = (Element) spellListNodes.item(0);
				
				if ( spellListNode != null )
				{

					NodeList spellRefNodes = spellListNode.getElementsByTagName("SpellRef");
					
					for ( int j = 0 ; j < spellRefNodes.getLength() ; j++ )
					{
						Element spellRefNode = (Element) spellRefNodes.item(j);
						String dest = spellRefNode.getAttribute("id");
						
						Node dst = (Node) namesToNodes.get(dest);
						if ( dst == null )
						{
							System.out.println("Warning: null destination node.");
							System.out.println(spellRefNode);
							System.out.println(spellRefNode.getAttribute("id"));
						}
						Arrow arrow = new StructuralArrow();
						destNodeToArrow.put(dst,arrow);
						arrow.setSource(src);
						arrow.setDestination(dst);
						src.addArrow(arrow);
						//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
						gep.getPropertiesPanel().loadWithoutShowing(arrow);
						arrow.getAssociatedPanel().initFromXML(spellRefNode); //panel init
					}
				
				}

			
			}
			
			
			
			
			//non-structural (custom) relationships - TODO <- but need to propagate destNodeToArrow across methods
			initCustomRelationshipsFromXML ( charNode , namesToNodes , destNodeToArrow );
			
		}
		
		//add item arrows, init panels
		Debug.println("[init world] Processing item relationships...");
		for ( int i = 0 ; i < itemNodes.getLength() ; i++ )
		{
			Element itemNode = (Element) itemNodes.item(i);
			String itemName = itemNode.getAttribute("name");
			Node src = (Node) namesToNodes.get(itemName);
			Map destNodeToArrow = new HashMap();
			
			NodeList inventoryNodes =
				DOMUtils.getDirectChildrenElementsByTagName(itemNode,"Inventory");

			if ( inventoryNodes.getLength() > 0 )
			{
			
				Element inventoryNode = (Element) inventoryNodes.item(0);
				
				initEntityToItemRelationships ( namesToNodes , src , inventoryNode , destNodeToArrow , UIMessages.getInstance().getMessage("structural.item.item.contain") );
				
				/*
				NodeList itemRefNodes = inventoryNode.getElementsByTagName("ItemRef");
				Node src = (Node) namesToNodes.get(itemName);
				
				for ( int j = 0 ; j < itemRefNodes.getLength() ; j++ )
				{
					Element itemRefNode = (Element) itemRefNodes.item(j);
					String dest = itemRefNode.getAttribute("id");
					
					Node dst = (Node) namesToNodes.get(dest);
					Arrow arrow = new StructuralArrow();
					arrow.setSource(src);
					arrow.setDestination(dst);
					src.addArrow(arrow);
					//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
					gep.getPropertiesPanel().loadWithoutShowing(arrow);
					
					GraphElementPanel arrowPanel = arrow.getAssociatedPanel();
					
					if ( arrowPanel instanceof ItemHasItemPanel )
					{
						((ItemHasItemPanel)arrowPanel).setRelationshipType(Messages.getInstance().getMessage("structural.item.item.contain"));
					}
					
					arrow.getAssociatedPanel().initFromXML(itemRefNode); //panel init
				}
				*/
			
			}
				
			NodeList partsNodes =
				DOMUtils.getDirectChildrenElementsByTagName(itemNode,"Parts");

			if ( partsNodes.getLength() > 0 )
			{
				Element partsNode = (Element) partsNodes.item(0);
				NodeList partsInventoryNodes =
					DOMUtils.getDirectChildrenElementsByTagName(partsNode,"Inventory");
				if ( partsInventoryNodes.getLength() > 0 )
				{
					Element partsInventoryNode = (Element) partsInventoryNodes.item(0);
					initEntityToItemRelationships ( namesToNodes , src , partsInventoryNode , destNodeToArrow , UIMessages.getInstance().getMessage("structural.item.item.haspart") );
				}
			}
			
			NodeList keysNodes =
				DOMUtils.getDirectChildrenElementsByTagName(itemNode,"KeyList");

			if ( keysNodes.getLength() > 0 )
			{
				Element keysNode = (Element) keysNodes.item(0);
				NodeList keysInventoryNodes =
					DOMUtils.getDirectChildrenElementsByTagName(keysNode,"Inventory");
				if ( keysInventoryNodes.getLength() > 0 )
				{
					Element keysInventoryNode = (Element) keysInventoryNodes.item(0);
					initEntityToItemRelationships ( namesToNodes , src , keysInventoryNode , destNodeToArrow ,  UIMessages.getInstance().getMessage("structural.item.item.haskey") );
				}
			}
			
			
			initCustomRelationshipsFromXML ( itemNode , namesToNodes , destNodeToArrow );
			
		}
		
		//add spell arrows, init panels
		Debug.println("[init world] Processing spell relationships...");
		for ( int i = 0 ; i < spellNodes.getLength() ; i++ )
		{
			Element spellNode = (Element) spellNodes.item(i);
			String spellName = spellNode.getAttribute("name");
			Node src = (Node) namesToNodes.get(spellName);
			Map destNodeToArrow = new HashMap();
			
			NodeList effectListNodes =
				DOMUtils.getDirectChildrenElementsByTagName(spellNode,"EffectList");

			if ( effectListNodes != null && effectListNodes.getLength() > 0 )
			{
			
				Element effectListNode = (Element) effectListNodes.item(0);
			
					if ( effectListNode != null )
					{

						NodeList effectRefNodes = effectListNode.getElementsByTagName("EffectRef");
						
						for ( int j = 0 ; j < effectRefNodes.getLength() ; j++ )
						{
							Element effectRefNode = (Element) effectRefNodes.item(j);
							String dest = effectRefNode.getAttribute("id");
							
							Node dst = (Node) namesToNodes.get(dest);
							if ( dst == null )
							{
								System.out.println("Warning: null destination node.");
								System.out.println(effectRefNode);
								System.out.println(effectRefNode.getAttribute("id"));
							}
							Arrow arrow = new StructuralArrow();
							destNodeToArrow.put(dst,arrow);
							arrow.setSource(src);
							arrow.setDestination(dst);
							src.addArrow(arrow);
							//arrow.getAssociatedPanel().setGraphEditingPanel(gep);
							gep.getPropertiesPanel().loadWithoutShowing(arrow);
							arrow.getAssociatedPanel().initFromXML(effectRefNode); //panel init
						}
					
					}
			
			
			}
		
			initCustomRelationshipsFromXML ( spellNode , namesToNodes , destNodeToArrow );
			
		}
		
		//add abstract entity arrows, init panels
		Debug.println("[init world] Processing abstract entity relationships...");
		for ( int i = 0 ; i < abstractEntityNodes.getLength() ; i++ )
		{
			Element abstractEntityNode = (Element) abstractEntityNodes.item(i);
			String abstractEntityName = abstractEntityNode.getAttribute("name");
			Node src = (Node) namesToNodes.get(abstractEntityName);
			Map destNodeToArrow = new HashMap();
						
			//non-structural (custom) relationships
			initCustomRelationshipsFromXML ( abstractEntityNode , namesToNodes , destNodeToArrow );
		}
		
		Debug.println("[init world] All done.");
		
		
		//show the world node.
		gep.getPropertiesPanel().show(gep.getWorldNode());
		
		
		//if ( metaInfNode == null ) //rearrange nodes a bit
		//	new GraphArranger().arrange(gep, 250);
		
		
	}
	
	public void initEntityToItemRelationships ( Map namesToNodes , Node src , Element inventoryNode , Map destNodeToArrow , String relationshipType )
	{
		NodeList itemRefNodes = inventoryNode.getElementsByTagName("ItemRef");
		
		for ( int j = 0 ; j < itemRefNodes.getLength() ; j++ )
		{
			Element itemRefNode = (Element) itemRefNodes.item(j);
			String dest = itemRefNode.getAttribute("id");
			
			Node dst = (Node) namesToNodes.get(dest);
			if ( dst == null )
			{
				System.out.println("Warning: null destination node.");
				System.out.println(itemRefNode);
				System.out.println(itemRefNode.getAttribute("id"));
				//maybe insert continue here so as to not casck in these cases, but just output the error and don't load the relationship
				continue;
			}
			Arrow arrow = new StructuralArrow();
			destNodeToArrow.put(dst,arrow);
			arrow.setSource(src);
			arrow.setDestination(dst);
			src.addArrow(arrow);
			gep.getPropertiesPanel().loadWithoutShowing(arrow);
			
			GraphElementPanel arrowPanel = arrow.getAssociatedPanel();
			
			if ( arrowPanel instanceof ItemHasItemPanel )
			{
				((ItemHasItemPanel)arrowPanel).setRelationshipType(relationshipType);
			}
			if ( arrowPanel instanceof CharHasItemPanel )
			{
				((CharHasItemPanel)arrowPanel).setRelationshipType(relationshipType);
			}
			
			arrow.getAssociatedPanel().initFromXML(itemRefNode); //panel init
		}
	}
	
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	}
	
	
	public String getBSHCode()
	{
		if ( bcp == null ) forceRealInitFromXml(true); //code panel not yet initted.
		return bcp.getCode();
	}
	
	
}
