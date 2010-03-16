import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.*;
import java.util.*;

public class FourMillionIntegerTest extends JInternalFrame
{
	

	
	//java.util.List enteros = new LinkedList();

	int[] enteros = new int[10000000];

	public FourMillionIntegerTest (  )
	{
		
		super("Test enteros",false,true,true,true);
		
		setTitle ( "Test enteros" );
		setSize(400,150);
		setVisible(true);
	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
	/*
	for ( int i = 0 ; i < 10000000 ; i++ )
	{
		enteros[i] = i;
	}
	*/
	
	/*	
		for ( int i = 0 ; i < 1000000 ; i++ )
		{
			if ( i % 100000 == 0 )
				System.out.println("Jarl " + i);
			enteros.add ( new Integer(4) );
		}
	*/
		
		
	}
	


	

}