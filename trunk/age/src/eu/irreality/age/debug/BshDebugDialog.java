/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 03/09/2011 23:45:25
 */
package eu.irreality.age.debug;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * @author carlos
 *
 */
public class BshDebugDialog extends JDialog 
{

	public BshDebugDialog ( String name , final Thread theThread , final Interpreter interpreter )
	{
		super();
		setTitle(name);
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		final JTextField evalTextField = new JTextField();
		JButton evalButton = new JButton("Evaluar");
		JPanel evalPanel = new JPanel(new BorderLayout());
		evalPanel.add(evalTextField,BorderLayout.CENTER);
		evalPanel.add(evalButton,BorderLayout.EAST);
		getContentPane().add(evalPanel);
		JPanel evalResultPanel = new JPanel(new BorderLayout());
		JLabel evalResultLabel = new JLabel("Resultado: ");
		final JTextArea evalResultTextArea = new JTextArea();
		evalResultPanel.add(evalResultLabel,BorderLayout.WEST);
		evalResultPanel.add(evalResultTextArea,BorderLayout.CENTER);
		getContentPane().add(evalResultPanel);
		JButton continueButton = new JButton("Continuar ejecución");
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(continueButton,BorderLayout.EAST);
		getContentPane().add(bottomPanel);
		pack();
		setVisible(true);
		
		continueButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				synchronized(theThread)
				{
					theThread.notify();
				}
				dispose();
			}
		}
		);
		
		evalButton.addActionListener( new ActionListener()
		{
			public void actionPerformed ( ActionEvent evt )
			{
				evalResultTextArea.setText("");
				try
				{
					Object returnValue = interpreter.eval(evalTextField.getText());
					evalResultTextArea.setText("Return value: " + returnValue);
				}
				catch ( EvalError ee )
				{
					ee.printStackTrace();
					evalResultTextArea.setText(ee.toString());
				}
				evalTextField.setText("");
			}
		}
		);
		
		//block the thread (beware, it's this same thread!)
		try 
		{
			synchronized(theThread)
			{
				theThread.wait();
			}
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		
	}
	
}
