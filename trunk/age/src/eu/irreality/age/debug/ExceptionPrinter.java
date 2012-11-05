package eu.irreality.age.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import eu.irreality.age.World;
import eu.irreality.age.scripting.ScriptException;
import eu.irreality.age.scripting.bsh.BSHScriptException;

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
	

	public static String getExceptionReport ( ScriptException se )
	{
		return se.getReport();
	}
	
	public static String getExceptionReport ( ScriptException se , String context )
	{
		return se.getReport(context);
	}
	
	//no longer needed
	/*
	public static String getExceptionReport ( BSHScriptException bse )
	{
		return getExceptionReport (bse.getTargetError());
	}
	*/
	
	//moved to ScriptException
	/*
	public static String getExceptionReport ( bsh.TargetError te )
	{
		StringBuffer report = new StringBuffer();
		report.append("\n**********BeanShell Runtime Error Report**********\n");
		report.append("*Error: " + te.printTargetError(te) );
		report.append("*Location: " + te.getErrorSourceFile() + "\n" );
		report.append("*Line: " + te.getErrorLineNumber() + "\n" );
		report.append("*Offending text: " + te.getErrorText() + "\n" );
		report.append("*Message: " + te.getMessage() + "\n");
		//report.append("Detailed trace: " + getStackTrace(te) + "\n" );
		boolean hasScriptStackTrace = te.getScriptStackTrace().trim().length() > 0;
		if ( hasScriptStackTrace ) report.append("*Script stack trace: " + te.getScriptStackTrace() +"\n");
		//if ( te.getCause() != null ) //seems unuseful
		//	report.append("Cause report: " + getExceptionReport ( te.getCause() ) );
		if ( te.inNativeCode() && te.getTarget() != null && te.getTarget() != te )
		    	report.append("*Exception was generated in native code. Stack trace follows: " + getExceptionReport ( te.getTarget() ) + "\n" );
		report.append("**************************************************\n");
		return report.toString();
	}
	*/
	
	public static String getExceptionReport ( bsh.EvalError te )
	{
		StringBuffer report = new StringBuffer();
		report.append("*Location: " + te.getErrorSourceFile() + "\n" );
		//report.append("*Line: " + te.getErrorLineNumber() + "\n" ); //does not work
		//report.append("*Offending text: " + te.getErrorText() + "\n" ); //does not work
		report.append("*Message: " + te.getMessage());
		//report.append("*Stack trace: " + getStackTrace(te) + "\n" );
		//report.append("*Cause report: " + getExceptionReport ( te.getCause() ) );
		report.append("\n");
		return report.toString();
	}
	
	public static String getExceptionReport ( bsh.EvalError te , String aRoutine , Object theCaller , Object[] theArguments )
	{
		StringBuffer report = new StringBuffer();
		report.append("\n**********BeanShell Syntax Error Report***********\n");
		report.append("*In code for object: " + theCaller + "\n");
		report.append("*Loaded to call method " + (aRoutine==null? "(no method)" : aRoutine) );
		report.append((theArguments == null || theArguments.length == 0) ? " (with no arguments" : " (with arguments:");
		if ( theArguments != null )
		{
			for ( int i = 0 ; i < theArguments.length ; i++ )
				report.append(" " + theArguments[i]);
		}
		report.append(")\n");
		report.append(getExceptionReport(te));
		report.append("**************************************************\n");
		return report.toString();
	}
	
	public static void reportEvalError ( EvalError ee , World w , String aRoutine , Object theCaller , Object[] theArguments )
	{
		w.writeError(getExceptionReport(ee,aRoutine,theCaller,theArguments));
	}
	
	/*
	void reportEvalError ( EvalError pe , String aroutine , Object theCaller , Object[] theArguments )
	{
		theWorld.writeError("Error de sintaxis en el c�digo BeanShell.\n");
		//theWorld.writeError("En concreto: " + pe + "\n"); 
		theWorld.writeError("Error: "+pe.getMessage()+"\n"); 
		//theWorld.writeError("["+pe.getErrorSourceFile()+"]"); 
		theWorld.writeError("En: c�digo del objeto " + theCaller + "\n"); 
		//System.err.println(pe.getMessage());
		//pe.printStackTrace();
		theWorld.writeError("Cargado para llamar la rutina: " + aroutine + "\n");
		//theWorld.writeError("Objeto del c�digo: " + theCaller + "\n");
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
