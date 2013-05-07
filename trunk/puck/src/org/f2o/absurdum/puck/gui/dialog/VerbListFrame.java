/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 02/04/2011 12:21:05
 */
package org.f2o.absurdum.puck.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.f2o.absurdum.puck.i18n.UIMessages;

import eu.irreality.age.windowing.DialogUtils;

/**
 * @author carlos
 *
 */
public class VerbListFrame extends JFrame 
{
	
	private Set sourceForms = new TreeSet();
	
	private Object[][] tableData;
	private Object[] columnNames;
	
	private JTable theTable;
	
	private static Map instances;

	public static VerbListFrame getInstance( String languageCode )
	{
		if ( instances == null ) 
			instances = Collections.synchronizedMap ( new HashMap() );
		VerbListFrame candidateInstance = (VerbListFrame) instances.get(languageCode);
		if ( candidateInstance == null )
		{
			candidateInstance = new VerbListFrame(languageCode);
			instances.put(languageCode,candidateInstance);
		}
		return candidateInstance;
	}
	
	private VerbListFrame( String languageCode )
	{
		super(UIMessages.getInstance().getMessage("verblist.frametitle"));
		setSize(600,600);
		eu.irreality.age.NaturalLanguage lang = eu.irreality.age.NaturalLanguage.getInstance( languageCode );
		sourceForms.addAll ( lang.getVerbForms() ); //to sort them we add them to TreeSet.
		
		tableData = new Object[sourceForms.size()][2];
		int i = 0;
		for ( Iterator it = sourceForms.iterator() ; it.hasNext() ; )
		{
			String sourceForm = (String) it.next();
			tableData[i][0] = sourceForm;
			tableData[i][1] = lang.toInfinitive(sourceForm);
			i++;
		}
		columnNames = new Object[2];
		columnNames[0] = UIMessages.getInstance().getMessage("verblist.source");
		columnNames[1] = UIMessages.getInstance().getMessage("verblist.target");
		
		TableModel tm = new VerbListTableModel(tableData,columnNames);
		
		theTable = new JTable();
		theTable.setAutoCreateRowSorter(true);
		theTable.setModel(tm);
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		
		JScrollPane tableScroll = new JScrollPane(theTable);
		tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout ( new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS) );
		mainPanel.add(new JLabel(UIMessages.getInstance().getMessage("verblist.explanation")));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add ( tableScroll );
		
		this.getContentPane().add(mainPanel);
		
		DialogUtils.registerEscapeAction(this);
		DialogUtils.registerCloseAction(this,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		
		//pack();
		setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
}
