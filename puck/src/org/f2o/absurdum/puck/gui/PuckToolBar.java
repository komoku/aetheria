/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 19-jul-2005 19:48:02
 * as file PuckToolBar.java on package org.f2o.absurdum.puck.gui
 */
package org.f2o.absurdum.puck.gui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.f2o.absurdum.puck.gui.graph.AbstractEntityNode;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;
import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.ItemNode;
import org.f2o.absurdum.puck.gui.graph.RoomNode;
import org.f2o.absurdum.puck.gui.graph.SpellNode;
import org.f2o.absurdum.puck.gui.graph.StructuralArrow;
import org.f2o.absurdum.puck.gui.skin.ImageManager;
import org.f2o.absurdum.puck.i18n.Messages;

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
	

	
	public PuckToolBar ( GraphEditingPanel gep , PropertiesPanel right , PuckFrame frame )
	{
		super("Tools",JToolBar.HORIZONTAL);
		associatedPanel = gep;
		associatedPropertiesPanel = right;
		associatedFrame = frame;
		Action a = new AddNodeTool(new RoomNode(0,0),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.addroom"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ROOM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addRoom")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.addroom"));
		add(a);
		
		a = new AddNodeTool(new ItemNode(0,0),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.additem"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ITEM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addItem")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.additem"));
		add(a);
		
		a = new AddNodeTool(new CharacterNode(0,0),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.addchar"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_CHAR_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addChar")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.addchar"));
		add(a);
		
		a = new AddNodeTool(new SpellNode(0,0),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.addspell"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_SPELL_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addSpell")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.addspell"));
		add(a);
		
		a = new AddNodeTool(new AbstractEntityNode(0,0),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.addabstract"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_ABSTRACT_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addAbstract")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.addabstract"));
		add(a);
		
		a = new AddArrowTool(new StructuralArrow(),associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.addstarrow"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ADD_STRUCT_ARROW_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("addStruct")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.addstarrow"));
		add(a);
		
		a = new ZoomTool(associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.zoom"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(ZOOM_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("zoom")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.zoom"));
		add(a);
		
		a = new TranslateTool(associatedPanel);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.move"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(MOVE_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("move")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.move"));
		add(a);
		
		a = new ExecAgeTool(associatedFrame);
		a.putValue(Action.NAME,Messages.getInstance().getMessage("tool.go"));
		//a.putValue(Action.SMALL_ICON,new ImageIcon(getClass().getClassLoader().getResource(GO_IMAGE)));
		a.putValue(Action.SMALL_ICON,new ImageIcon(ImageManager.getInstance().getImage("goButton")));
		a.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
		a.putValue(Action.SHORT_DESCRIPTION,Messages.getInstance().getMessage("tool.go"));
		add(a);
		
	}

}
