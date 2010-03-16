import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.*;

public class MeterFrame extends JInternalFrame
{
	

	
	JProgressBar progressBarMem = new JProgressBar(0,100);
	//JProgressBar progressBar1 = new JProgressBar(0,1);
	//JProgressBar progressBar2 = new JProgressBar(0,1);
	//JLabel labelBar1 = new JLabel("Algorithm progress:");
	//JLabel labelBar2 = new JLabel("Step progress:");
	
	JTextField tf1 = new JTextField("Memoria de objetos asignada: ");
	JTextField tf2 = new JTextField("Memoria de objetos no usada: ");
	JTextField tf3 = new JTextField("Memoria de objetos máxima: ");
	
	JPanel barsPanel;
	
	Thread meterThread;

	Object stopthrSem = new Object();
	boolean stopthr = false;
	
	JInternalFrame self = this;

	public MeterFrame (  )
	{
		
		super("Medidor de recursos",false,true,true,true);
		
		setTitle ( "Medidor de recursos" );
		setSize(400,150);
		setVisible(true);
		
		tf1.setEditable(false);
		tf2.setEditable(false);
		tf3.setEditable(false);
		
		setDefaultCloseOperation ( JInternalFrame.DO_NOTHING_ON_CLOSE ); //se encarga el listener a continuación
		addInternalFrameListener ( new InternalFrameAdapter()
		{ 
			public void internalFrameClosing ( InternalFrameEvent e )
			{
				setVisible(false);
				stopMeterThread();
				//dispose();
			}
		});
		
		
		barsPanel = new JPanel();
		barsPanel.setLayout ( new GridLayout ( 5 , 1 ) );
		barsPanel.add ( new JLabel ( "Uso de memoria:" ) );	
		barsPanel.add ( progressBarMem );
		barsPanel.add(tf1);
		barsPanel.add(tf2);	
		barsPanel.add(tf3);	
		progressBarMem.setStringPainted(true);
//		progressBar1.setStringPainted(true);
//		progressBar2.setStringPainted(true);
		getContentPane().add ( barsPanel );
		updateMemProgressBar();
		
		meterThread = new Thread()
		{
			public void run()
			{
				for (;;)
				{
					try
					{
						sleep(200);
					}
					catch ( InterruptedException ie )
					{
						ie.printStackTrace();
					}
					updateMemProgressBar();
					
					synchronized ( stopthrSem )
					{
						if ( stopthr )
						{
							self.dispose();
							return;
						}
					}
				}
			}
		};
		
		meterThread.start();
		
		
	}
	
	public void stopMeterThread()
	{
		synchronized ( stopthrSem )
		{
			stopthr = true;
		}
	}
	
	public String humanReadableSize ( long size )
	{
		String unit="";
		int divisor = 1;
		if ( size < 1024 )
		{
			divisor = 1;
			unit = "B";
		}
		if ( size < 1024*1024 )
		{
			divisor = 1024;
			unit = "KB";
		}
		if ( size < (1024*1024*1024) )
		{
			divisor = 1024*1024;
			unit = "MB";
		}
		
		double amount = ((double)size)/((double)divisor);
		return (new DecimalFormat("#.###").format(amount)) + " " + unit;
		
	}
	
	public void updateMemProgressBar ( )
	{
		double freeMemory = (double) Runtime.getRuntime().freeMemory();
		double totalMemory = (double) Runtime.getRuntime().totalMemory();
		double maxMemory = (double) Runtime.getRuntime().maxMemory();
		double usedMemory = totalMemory - freeMemory;

	
		tf1.setText( "Memoria de objetos asignada: " + humanReadableSize((long)totalMemory) );
		tf2.setText( "Memoria de objetos usada: " + humanReadableSize((long)usedMemory) );
		tf3.setText( "Memoria de objetos máxima: " + humanReadableSize((long)maxMemory) );
		
		int percent = (int)( (double)(( usedMemory ) / maxMemory) * 100 );
		progressBarMem.setValue(percent);
		progressBarMem.setString(percent + "%");
		if ( percent <= 60 ) progressBarMem.setForeground ( Color.green );
		else if ( percent <= 85 ) progressBarMem.setForeground ( Color.yellow );
		else progressBarMem.setForeground ( Color.red );
	}
	

}