package org.f2o.absurdum.puck.gui.templates;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.xml.transform.TransformerException;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.jedit.syntax.JEditTextArea;

public class WorldFromTemplateActionBuilder 
{

	private PuckFrame pf;
	
	public WorldFromTemplateActionBuilder ( PuckFrame pf )
	{
		this.pf = pf;
	}
	
	public Action getWorldFromTemplateAction ( final String path )
	{
		return new AbstractAction() {
			public void actionPerformed ( ActionEvent evt )
			{
				InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/f2o/absurdum/puck/staticconf/worldtemplates/"+path);
				if ( is == null )
				{
					JOptionPane.showMessageDialog(pf,"Internal error loading template: " + "org/f2o/absurdum/puck/staticconf/worldtemplates/"+path,"Whoops!",JOptionPane.ERROR_MESSAGE);
					return;
				}
				boolean success = pf.openStreamOrShowError(is);
				if ( success )
				{
					((WorldPanel)pf.getGraphEditingPanel().getWorldNode().getAssociatedPanel()).setDefaultWorldVersion();
					pf.resetCurrentlyEditingFile();
					pf.refreshTitle();
				}
			}
		};
	}
	
}
