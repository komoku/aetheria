/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 03/09/2011 23:45:25
 */
package eu.irreality.age.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * @author carlos
 *
 */
public class BshDebugDialog extends JFrame /*JFrame appears on taskbar, JDialog doesn't*/ 
{

	public BshDebugDialog ( String name , final Thread theThread , final Interpreter interpreter )
	{
		super();
		setTitle(name);
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		final JTextField evalTextField = new JTextField();
		JButton evalButton = new JButton("Evaluar");
		JPanel evalPanel = new JPanel();
		evalPanel.setLayout(new BoxLayout(evalPanel,BoxLayout.LINE_AXIS));
		evalPanel.add(Box.createHorizontalStrut(10));
		evalPanel.add(evalTextField,BorderLayout.CENTER);
		evalPanel.add(Box.createHorizontalStrut(10));
		evalPanel.add(evalButton,BorderLayout.EAST);
		evalPanel.add(Box.createHorizontalStrut(10));
		getContentPane().add(evalPanel);
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		JPanel evalResultPanel = new JPanel();
		evalResultPanel.setLayout(new BoxLayout(evalResultPanel,BoxLayout.LINE_AXIS));
		JLabel evalResultLabel = new JLabel("Resultado: ");
		final JTextArea evalResultTextArea = new JTextArea(8,60);
		evalResultTextArea.setEditable(false);
		JScrollPane evalResultScroll = new JScrollPane(evalResultTextArea);
		evalResultPanel.add(Box.createHorizontalStrut(10));
		evalResultPanel.add(evalResultLabel);
		evalResultPanel.add(Box.createHorizontalStrut(10));
		evalResultPanel.add(evalResultScroll);
		evalResultPanel.add(Box.createHorizontalStrut(10));
		getContentPane().add(evalResultPanel);
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		JButton continueButton = new JButton("Continuar ejecución");
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.LINE_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(continueButton);
		bottomPanel.add(Box.createHorizontalStrut(10));
		getContentPane().add(bottomPanel);
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		pack();
		setLocationRelativeTo(null); //center on screen
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
				evalResultTextArea.setText("Expression evaluation: " + evalTextField.getText() + "\n");
				try
				{
					Object returnValue = interpreter.eval(evalTextField.getText());
					evalResultTextArea.append("Result: " + returnValue + "\n");
				}
				catch ( EvalError ee )
				{
					ee.printStackTrace();
					evalResultTextArea.append("Exception:\n");
					evalResultTextArea.append(ee.toString());
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
