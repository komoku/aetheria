/**
 * (c) 2000-2011 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 03/09/2011 23:45:25
 */
package eu.irreality.age.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.UtilEvalError;

/**
 * @author carlos
 *
 */
public class BshDebugDialog extends JFrame /*JFrame appears on taskbar, JDialog doesn't*/ 
{
	
	private JList listOfVariables = new JList();
	
	void refreshVariableList( Interpreter interpreter , NameSpace namespace )
	{
		String[] varNames = namespace.getVariableNames();
		for ( int i = 0 ; i < varNames.length ; i++ )
		{
			String realVarName = varNames[i];
			varNames[i] += " = ";
				try {
					varNames[i] += interpreter.eval(realVarName,namespace);
				} catch (EvalError e) {
					e.printStackTrace();
				}
		}
		listOfVariables.setListData(varNames);
		pack();
		repaint();
	}

	public BshDebugDialog ( String name , final Thread theThread , final Interpreter interpreter , final NameSpace namespace )
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
		listOfVariables = new JList();
		JPanel variablesPanel = new JPanel();
		JLabel variablesLabel = new JLabel("Variables locales: ");
		variablesPanel.setLayout(new BoxLayout(variablesPanel,BoxLayout.LINE_AXIS));
		variablesPanel.add(Box.createHorizontalStrut(10));
		variablesPanel.add(variablesLabel);
		variablesPanel.add(Box.createHorizontalStrut(10));
		JScrollPane variablesScrollPane = new JScrollPane(listOfVariables);
		variablesPanel.add(variablesScrollPane);
		variablesPanel.add(Box.createHorizontalStrut(10));
		getContentPane().add(variablesPanel);
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		JButton continueButton = new JButton("Continuar ejecuci�n");
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.LINE_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(continueButton);
		bottomPanel.add(Box.createHorizontalStrut(10));
		getContentPane().add(bottomPanel);
		getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
		refreshVariableList(interpreter,namespace);
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
		
		addWindowListener ( new WindowAdapter() //closing window is same as pressing continue
		{
			public void windowClosing ( WindowEvent evt )
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
					Object returnValue = interpreter.eval(evalTextField.getText(),namespace);
					refreshVariableList(interpreter,namespace);
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
