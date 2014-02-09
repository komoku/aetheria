/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:48:02
 * as file PuckToolBar.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.gui.skin.ImageManager;
import org.f2o.absurdum.puck.i18n.UIMessages;

/**
 * @author carlos
 *
 * Created at regulus, 19-jul-2005 19:48:02
 */
public class PuckToolBar extends JToolBar 
{

	private GraphEditingPanel associatedPanel;
	
	private PuckFrame associatedFrame;
	
	private PropertiesPanel associatedPropertiesPanel;
	
	private static final String SELECTED_ICON = "selectedIcon"; //Action key to associate a selected icon to an action (an icon that will appear in the toolbar button when the action is selected) 
	
	private ButtonGroup toggleButtons = new ButtonGroup();
	

	
    private JToggleButton addToggleButton(ToolAction a) {
        JToggleButton b = createActionToggleButton(a);
        b.setAction(a);
        add(b);
        toggleButtons.add(b);
        return b;
    }
        
    /**
     * Based on createActionComponent for the superclass.
     * @param a
     * @return
     */
    private JToggleButton createActionToggleButton ( final ToolAction a )
    {
    	/*
        JToggleButton b = new JToggleButton() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                PropertyChangeListener pcl = super.createActionPropertyChangeListener(a);
                return pcl;
            }
        };
        */
        
        JToggleButton b = new JToggleButton();
        
        b.addActionListener( new ActionListener() 
        {
        	public void actionPerformed ( ActionEvent evt )
        	{
        		if ( !a.isToolSelectionPersistent() ) //deselect the action/button if tool selection is not persistent
        			a.putValue(Action.SELECTED_KEY, Boolean.FALSE);
        	}
        }
       	);      
        
        b.setAction(a);
        
        if (a != null && (a.getValue(Action.SMALL_ICON) != null || a.getValue(Action.LARGE_ICON_KEY) != null)) 
        {
        	b.setHideActionText(true);
        }
        b.setSelectedIcon((Icon)a.getValue(SELECTED_ICON));
        b.setHorizontalTextPosition(JToggleButton.CENTER);
        b.setVerticalTextPosition(JToggleButton.BOTTOM);
        return b;
    }
	
	
	public PuckToolBar ( GraphEditingPanel gep , PropertiesPanel right , PuckFrame frame )
	{
		super("Tools",JToolBar.VERTICAL);
		associatedPanel = gep;
		associatedPropertiesPanel = right;
		associatedFrame = frame;
		ToolAction a = new AddNodeTool(new RoomNode(0,0),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.addroom"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ROOM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addRoom")));
		a.putValue(SELECTED_ICON, new ImageIcon(ImageManager.getInstance().getImage("addRoomPushed")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.addroom"));
		addToggleButton(a);
		
		a = new AddNodeTool(new ItemNode(0,0),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.additem"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ITEM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addItem")));
		a.putValue(SELECTED_ICON, new ImageIcon(ImageManager.getInstance().getImage("addItemPushed")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.additem"));
		addToggleButton(a);
		
		a = new AddNodeTool(new CharacterNode(0,0),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.addchar"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_CHAR_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addChar")));
		a.putValue(SELECTED_ICON, new ImageIcon(ImageManager.getInstance().getImage("addCharPushed")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.addchar"));
		addToggleButton(a);
		
		a = new AddNodeTool(new SpellNode(0,0),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.addspell"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_SPELL_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addSpell")));
		a.putValue(SELECTED_ICON, new ImageIcon(ImageManager.getInstance().getImage("addSpellPushed")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.addspell"));
		a.putValue(Action.SELECTED_KEY, Boolean.FALSE);
		addToggleButton(a);
		//watch the add of "action" incl. createActionComponent and do something similar here
		// - hmm, but rollover won't work.
		
		a = new AddNodeTool(new AbstractEntityNode(0,0),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.addabstract"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ABSTRACT_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addAbstract")));
		a.putValue(SELECTED_ICON, new ImageIcon(ImageManager.getInstance().getImage("addAbstractPushed")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.addabstract"));
		addToggleButton(a);
		//add(a);
		
		a = new AddArrowTool(new StructuralArrow(),associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.addstarrow"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_STRUCT_ARROW_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addStruct")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.addstarrow"));
		addToggleButton(a);
		//add(a);
		
		a = new ZoomTool(associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.zoom"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ZOOM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("zoom")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.zoom"));
		addToggleButton(a);
		//add(a);
		
		a = new TranslateTool(associatedPanel);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.move"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(MOVE_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("move")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.move"));
		addToggleButton(a);
		
		a = new ExecAgeTool(associatedFrame);
		a.putValue(Action.NAME,UIMessages.getInstance().getMessage("tool.go"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(GO_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("goButton")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
		a.putValue(Action.SHORT_DESCRIPTION,UIMessages.getInstance().getMessage("tool.go"));
		addToggleButton(a);
		
	}

}
