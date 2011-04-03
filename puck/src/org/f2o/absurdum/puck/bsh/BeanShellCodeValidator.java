/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 02/04/2011 16:34:34
 */
package org.f2o.absurdum.puck.bsh;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.f2o.absurdum.puck.gui.graph.GraphEditingPanel;
import org.f2o.absurdum.puck.gui.graph.Node;
import org.f2o.absurdum.puck.gui.panels.EntityPanel;
import org.f2o.absurdum.puck.gui.panels.WorldPanel;
import org.f2o.absurdum.puck.i18n.Messages;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;

/**
 * @author carlos
 * Created 2011-04-02
 *
 */
public class BeanShellCodeValidator 
{
	
	private GraphEditingPanel gep;
	private Interpreter interp;
	private StringBuffer errorText = new StringBuffer();

	public BeanShellCodeValidator ( GraphEditingPanel gep )
	{
		this.gep = gep;
		interp = new Interpreter();
	}
	
	/**
	 * 
	 * @param code The code to validate.
	 * @param source The place where the code was taken from.
	 * @return
	 */
	public boolean validateCode ( String code , Object source )
	{
		try
		{
			interp.eval(code);
		}
		catch ( ParseException pe )
		{
			errorText.append(Messages.getInstance().getMessage("bsh.parse.error.in") + source.toString() + "\n");
			errorText.append(pe.getMessage()+"\n");
			return false;
		}
		catch ( EvalError ee )
		{
			; //other kinds of errors (non-parse errors)
		}
		return true;
	}
	
	public boolean validate ()
	{
		boolean ok = true;
		errorText = new StringBuffer();
		Collection nodes = gep.getNodes();
		for ( Iterator it = nodes.iterator() ; it.hasNext() ;  )
		{
			Node n = (Node) it.next();
			EntityPanel ep = (EntityPanel) n.getAssociatedPanel();
			if ( ep instanceof BeanShellCodeHolder )
			{
				BeanShellCodeHolder bsch = (BeanShellCodeHolder)ep;
				boolean thisOneOk = validateCode(bsch.getBSHCode(),n);
				ok = ok && thisOneOk;
			}
		}
		BeanShellCodeHolder worldPanel = (WorldPanel)gep.getWorldNode().getAssociatedPanel();
		boolean thisOneOk = validateCode(worldPanel.getBSHCode(),gep.getWorldNode());
		ok = ok && thisOneOk;
		return ok;
	}
	
	public String getErrorText()
	{
		return errorText.toString();
	}
	
}
