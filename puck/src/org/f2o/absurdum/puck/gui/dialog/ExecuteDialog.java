package org.f2o.absurdum.puck.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.f2o.absurdum.puck.gui.PuckFrame;
import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.i18n.Messages;

import eu.irreality.age.FiltroFicheroLog;
import eu.irreality.age.ServerHandler;
import eu.irreality.age.Utility;
import eu.irreality.age.filemanagement.Paths;

public class ExecuteDialog extends JDialog 
{

    private JRadioButton mdiButton = new JRadioButton(Messages.getInstance().getMessage("exec.mdi"));
    private JRadioButton sdiButton = new JRadioButton(Messages.getInstance().getMessage("exec.sdi"));
    private JCheckBox logCheckBox = new JCheckBox(Messages.getInstance().getMessage("exec.uselog"));
    private JTextField logTextField = new JTextField(20);
    private JButton logBrowseButton = new JButton(Messages.getInstance().getMessage("exec.browse"));
    private JButton okButton = new JButton(Messages.getInstance().getMessage("exec.ok"));
    private JButton cancelButton = new JButton(Messages.getInstance().getMessage("exec.cancel"));
    
    private PuckFrame frame;
    
    public ExecuteDialog ( PuckFrame pf )
    {
	
	super(pf);
	this.frame = pf;
	this.setModal(true);
	this.setResizable(false);
	setTitle(Messages.getInstance().getMessage("exec.dialogtitle"));
	
	//lay out the components
	
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	
	ButtonGroup bg = new ButtonGroup();
	bg.add(mdiButton);
	bg.add(sdiButton);
	
	if ( PuckConfiguration.getInstance().getProperty("runInSDI").equals("true") )
		sdiButton.setSelected(true);
	else
		mdiButton.setSelected(true);
	
	getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
	getContentPane().add(new JLabel(Messages.getInstance().getMessage("exec.interface")));
	getContentPane().add(mdiButton);
	getContentPane().add(sdiButton);
	
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	
	logCheckBox.setSelected(false);
	JPanel logPanel = new JPanel();
	//logPanel.setLayout(new BoxLayout(logPanel,BoxLayout.LINE_AXIS));
	//logPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	//logPanel.add(logCheckBox);
	//logPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	//JPanel tfPanel = new JPanel(new FlowLayout());
	//tfPanel.add(logTextField);
	//logPanel.add(tfPanel);
	//logPanel.add(logTextField);
	//logPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	//logPanel.add(logBrowseButton);
	//logPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	logPanel.setLayout(new BorderLayout());
	
	JPanel paddedCheckBox = new JPanel();
	paddedCheckBox.setLayout(new BoxLayout(paddedCheckBox,BoxLayout.LINE_AXIS));
	paddedCheckBox.add(Box.createHorizontalStrut(10));
	paddedCheckBox.add(logCheckBox);
	paddedCheckBox.add(Box.createHorizontalStrut(10));
	logPanel.add(paddedCheckBox,BorderLayout.WEST);
	logPanel.add(logTextField,BorderLayout.CENTER);
	JPanel paddedButton = new JPanel();
	paddedButton.setLayout(new BoxLayout(paddedButton,BoxLayout.LINE_AXIS));
	paddedButton.add(Box.createHorizontalStrut(10));
	paddedButton.add(logBrowseButton);
	paddedButton.add(Box.createHorizontalStrut(10));
	logPanel.add(paddedButton,BorderLayout.EAST);
	
	logTextField.setEnabled(logCheckBox.isSelected());
	logBrowseButton.setEnabled(logCheckBox.isSelected());
	getContentPane().add(logPanel);
	
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	getContentPane().add(new JSeparator());
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	
	JLabel lConfirm = new JLabel(Messages.getInstance().getMessage("exec.wanttosave"));
	//lConfirm.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	getContentPane().add(lConfirm);
	getContentPane().add(Box.createHorizontalGlue());
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	
	JPanel buttonsPanel = new JPanel();
	buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.LINE_AXIS));
	buttonsPanel.add(Box.createHorizontalGlue());
	buttonsPanel.add(okButton);
	buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonsPanel.add(cancelButton);
	buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	getContentPane().add(buttonsPanel);
	
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	
	pack();
	this.setLocationRelativeTo(null);
	
	//add the listeners
	
	sdiButton.addItemListener ( new ItemListener() 
	{

		public void itemStateChanged(ItemEvent arg0) 
		{
			String boolString = sdiButton.isSelected() ? "true" : "false";
			PuckConfiguration.getInstance().setProperty("runInSDI",boolString);
		}
	}
	);
	
	cancelButton.addActionListener( new ActionListener() 
	{
	    public void actionPerformed(ActionEvent e)
	    {
		dispose();
	    }
	}
	);
	
	okButton.addActionListener( new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
			boolean saved = frame.saveOrSaveAs();
			if ( !saved ) dispose();
			String logFile = null;
			if ( logCheckBox.isSelected() && logTextField.getText() != null && logTextField.getText().length() > 0 ) logFile = logTextField.getText();
			boolean mdiOption = mdiButton.isSelected();
			frame.runCurrentFileInAge(mdiOption,logFile);
			dispose();
	    }
	}
	);
	
	logBrowseButton.addActionListener( new ActionListener() 
	{
	    public void actionPerformed(ActionEvent arg0)
	    {
		final JFileChooser selector = new JFileChooser( Paths.SAVE_PATH );
		selector.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selector.setDialogTitle("Selecciona el fichero de log");
		selector.setFileFilter ( new FiltroFicheroLog() ); 

		int returnVal = selector.showOpenDialog(ExecuteDialog.this);

		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
		    logTextField.setText(selector.getSelectedFile().getAbsolutePath());
		}
	    }
	}
	);
	
	logCheckBox.addChangeListener( new ChangeListener()
	{
	    public void stateChanged(ChangeEvent arg0)
	    {
		logTextField.setEnabled(logCheckBox.isSelected());
		logBrowseButton.setEnabled(logCheckBox.isSelected());
	    }
	}
	);
	
	this.getRootPane().setDefaultButton(okButton);
	    
    }
	
	public void setVisible ( boolean visible )
	{
		super.setVisible(visible);
		if ( visible == true ) this.getRootPane().setDefaultButton(okButton);
	}
    
    
    
}
