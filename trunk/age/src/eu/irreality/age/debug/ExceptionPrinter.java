package eu.irreality.age.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import eu.irreality.age.World;

import bsh.EvalError;

public class ExceptionPrinter 
{

	public static String getStackTrace(Throwable aThrowable) 
	{
		if ( aThrowable == null ) return "[no exception]";
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	}
	

	
	public static String getExceptionReport ( bsh.TargetError te )
	{
		StringBuffer report = new StringBuffer();
		report.append("**\n");
		report.append("Error: " + te.printTargetError(te) );
		report.append("Location: " + te.getErrorSourceFile() + "\n" );
		report.append("Line: " + te.getErrorLineNumber() + "\n" );
		report.append("Offending text: " + te.getErrorText() + "\n" );
		report.append("Message: " + te.getMessage() + "\n");
		report.append("Detailed trace: " + getStackTrace(te) + "\n" );
		if ( te.getCause() != null )
			report.append("Cause report: " + getExceptionReport ( te.getCause() ) );
		if ( te.getTarget() != null && te.getTarget() != te )
		    	report.append("Target report: " + getExceptionReport ( te.getTarget() ) );
		report.append("**\n");
		return report.toString();
	}
	
	public static String getExceptionReport ( bsh.EvalError te )
	{
		StringBuffer report = new StringBuffer();
		report.append("File: " + te.getErrorSourceFile() + "\n" );
		//report.append("Line: " + te.getErrorLineNumber() + "\n" );
		//report.append("Error: " + te.getErrorText() + "\n" );
		report.append("Stack trace: " + getStackTrace(te) + "\n" );
		report.append("Cause report: " + getExceptionReport ( te.getCause() ) );
		report.append("\n");
		return report.toString();
	}
	
	public static String getExceptionReport ( bsh.EvalError te , String aRoutine , Object theCaller , Object[] theArguments )
	{
		StringBuffer report = new StringBuffer();
		report.append("Syntax error in BeanShell code in object: " + theCaller + "\n");
		report.append("Loaded to call method " + (aRoutine==null? "(no method)" : aRoutine) + "\n");
		report.append((theArguments == null || theArguments.length == 0) ? "(with no arguments)" : "With arguments: ");
		if ( theArguments != null )
		{
			for ( int i = 0 ; i < theArguments.length ; i++ )
				report.append(theArguments[i] + " ");
		}
		report.append("\n");
		report.append(getExceptionReport(te));
		return report.toString();
	}
	
	public static void reportEvalError ( EvalError ee , World w , String aRoutine , Object theCaller , Object[] theArguments )
	{
		w.writeError(getExceptionReport(ee,aRoutine,theCaller,theArguments));
	}
	
	/*
	void reportEvalError ( EvalError pe , String aroutine , Object theCaller , Object[] theArguments )
	{
		theWorld.writeError("Error de sintaxis en el código BeanShell.\n");
		//theWorld.writeError("En concreto: " + pe + "\n"); 
		theWorld.writeError("Error: "+pe.getMessage()+"\n"); 
		//theWorld.writeError("["+pe.getErrorSourceFile()+"]"); 
		theWorld.writeError("En: código del objeto " + theCaller + "\n"); 
		//System.err.println(pe.getMessage());
		//pe.printStackTrace();
		theWorld.writeError("Cargado para llamar la rutina: " + aroutine + "\n");
		//theWorld.writeError("Objeto del código: " + theCaller + "\n");
		theWorld.writeError("Con argumentos: ");
		for ( int i = 0 ; i < theArguments.length ; i++ )
			theWorld.writeError(theArguments[i] + " "); 
		theWorld.writeError("\n");
	}
	*/
	
	public static String getExceptionReport ( Throwable e )
	{
		return getStackTrace(e);
	}
	
	
	
}
