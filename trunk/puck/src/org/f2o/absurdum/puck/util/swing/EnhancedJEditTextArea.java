/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */

package org.f2o.absurdum.puck.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.jedit.syntax.DefaultInputHandler;
import org.jedit.syntax.InputHandler;
import org.jedit.syntax.JEditTextArea;
import org.jedit.syntax.TextAreaDefaults;

/*
 Created 20/04/2008 20:03:25
 */

public class EnhancedJEditTextArea extends JEditTextArea
{

	public EnhancedJEditTextArea ( )
	{
		super();
		initEnhancements();
	}
	
	Action copyAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			InputHandler.getTextArea(e).copy();
		}	
	};
	
	Action pasteAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			InputHandler.getTextArea(e).paste();
		}	
	};
	
	Action cutAction = new AbstractAction(){

		public void actionPerformed(ActionEvent e) {
			InputHandler.getTextArea(e).cut();
		}	
	};
	
	Action selectAllAction = new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            InputHandler.getTextArea(e).selectAll();
	        }
	};
	
	class MouseWheelSupport implements MouseWheelListener {
	        public void mouseWheelMoved(MouseWheelEvent e) {
	            int wheelRotationCount = e.getWheelRotation();
	            int lineToShow = getFirstLine() + wheelRotationCount;
	            if (wheelRotationCount > 0) {
	                lineToShow += getVisibleLines();
	            }
	            if (lineToShow < 0) {
	                lineToShow = 0;
	            }
	            if (lineToShow >= getLineCount()) {
	                lineToShow = getLineCount() - 1;
	            }
	            scrollTo(lineToShow, getLineStartOffset(lineToShow));
	        }
	    }
	
	public EnhancedJEditTextArea ( TextAreaDefaults tad )
	{
		super(tad);
		initEnhancements();
	}
	
	public void addPopupMenu ( JMenu popup )
	{
		JPopupMenu pop = getRightClickPopup();
		pop.add(popup);
		System.out.println("Add: " + popup);
	}
	
	public void initEnhancements()
	{
		
		addMouseWheelListener(new MouseWheelSupport());
		
		copyAction.putValue(Action.NAME,"copy");
		cutAction.putValue(Action.NAME,"cut");
		pasteAction.putValue(Action.NAME,"paste");
		
		final JPopupMenu popupMenu = new JPopupMenu("Edit");
		
		JMenuItem menuItem = new JMenuItem(cutAction);
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(copyAction);
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        popupMenu.add(menuItem);

        menuItem = new JMenuItem(pasteAction);
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        popupMenu.add(menuItem);
		
        /*
        this.addMouseListener(new MouseAdapter()
        {

            public void mousePressed(MouseEvent e)
            {
                processMouseEvent(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                processMouseEvent(e);
            }

            private void processMouseEvent(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    popupMenu.setInvoker(EnhancedJEditTextArea.this);
                }
            }
        });
        */
        
        setRightClickPopup(popupMenu);
        
        /*
        getInputMap().put(KeyStroke.getKeyStroke("ctrl X"), cutAction.getValue(Action.NAME));
        getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), pasteAction.getValue(Action.NAME));
        getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), copyAction.getValue(Action.NAME));
        
        getActionMap().put(cutAction.getValue(Action.NAME),cutAction);
        getActionMap().put(pasteAction.getValue(Action.NAME),pasteAction);
        getActionMap().put(copyAction.getValue(Action.NAME),copyAction);
        */
        
        getInputHandler().addKeyBinding("C+X", cutAction);
        getInputHandler().addKeyBinding("C+V", pasteAction);
        getInputHandler().addKeyBinding("C+C", copyAction);
        getInputHandler().addKeyBinding("C+A", selectAllAction);

        
	}
	
	
	
}
