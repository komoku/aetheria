/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 02/04/2011 12:21:05
 */
package org.f2o.absurdum.puck.gui.dialog;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.f2o.absurdum.puck.i18n.Messages;

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
	
	private static VerbListFrame instance;

	public static VerbListFrame getInstance()
	{
		if ( instance == null ) 
			instance = new VerbListFrame();
		return instance;
	}
	
	private VerbListFrame()
	{
		super(Messages.getInstance().getMessage("verblist.frametitle"));
		setSize(600,600);
		eu.irreality.age.NaturalLanguage lang = eu.irreality.age.NaturalLanguage.getInstance();
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
		columnNames[0] = Messages.getInstance().getMessage("verblist.source");
		columnNames[1] = Messages.getInstance().getMessage("verblist.target");
		
		TableModel tm = new VerbListTableModel(tableData,columnNames);
		
		theTable = new JTable();
		theTable.setAutoCreateRowSorter(true);
		theTable.setModel(tm);
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.PAGE_AXIS));
		
		JScrollPane tableScroll = new JScrollPane(theTable);
		tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout ( new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS) );
		mainPanel.add(new JLabel(Messages.getInstance().getMessage("verblist.explanation")));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add ( tableScroll );
		
		this.getContentPane().add(mainPanel);
		this.setVisible(true);
	}
	
}
