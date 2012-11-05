/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 04/11/2012 13:16:42
 */
package eu.irreality.age.scripting.bsh;

import java.io.PrintStream;
import java.io.PrintWriter;

import eu.irreality.age.debug.ExceptionPrinter;
import eu.irreality.age.scripting.ScriptException;

import bsh.EvalError;
import bsh.TargetError;

/**
 * @author carlos
 *
 * An exception that can be thrown by a BSH script.
 * Adapter class for TargetError.
 */
public class BSHScriptException extends ScriptException
{

	private TargetError te;
	
	public TargetError getTargetError()
	{
		return te;
	}
	
	public BSHScriptException( TargetError te )
	{
		this.te = te;
	}
	
	
	/**
	 * Return a detailed report of the exception
	 */
	public String getReport ()
	{
		return getReport(null);
	}
	
	/**
	 * Return a detailed report of the exception for the given context
	 */
	public String getReport ( String context )
	{
		StringBuffer report = new StringBuffer();
		report.append("\n**********BeanShell Runtime Error Report**********\n");
		if ( context != null ) report.append("*Context: " + context + "\n");
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
		    	report.append("*Exception was generated in native code. Stack trace follows: " + ExceptionPrinter.getExceptionReport ( te.getTarget() ) + "\n" );
		report.append("**************************************************\n");
		return report.toString();
	}
	
	

	/**
	 * @return
	 * @see bsh.TargetError#getTarget()
	 */
	public Throwable getTarget() {
		return te.getTarget();
	}

	/**
	 * @param msg
	 * @throws EvalError
	 * @see bsh.EvalError#reThrow(java.lang.String)
	 */
	public void reThrow(String msg) throws EvalError {
		te.reThrow(msg);
	}

	/**
	 * @return
	 * @see bsh.TargetError#toString()
	 */
	public String toString() {
		return te.toString();
	}

	/**
	 * 
	 * @see bsh.TargetError#printStackTrace()
	 */
	public void printStackTrace() {
		te.printStackTrace();
	}

	/**
	 * @param out
	 * @see bsh.TargetError#printStackTrace(java.io.PrintStream)
	 */
	public void printStackTrace(PrintStream out) {
		te.printStackTrace(out);
	}

	/**
	 * @return
	 * @see bsh.EvalError#getErrorText()
	 */
	public String getErrorText() {
		return te.getErrorText();
	}

	/**
	 * @param debug
	 * @param out
	 * @see bsh.TargetError#printStackTrace(boolean, java.io.PrintStream)
	 */
	public void printStackTrace(boolean debug, PrintStream out) {
		te.printStackTrace(debug, out);
	}

	/**
	 * @return
	 * @see bsh.EvalError#getErrorLineNumber()
	 */
	public int getErrorLineNumber() {
		return te.getErrorLineNumber();
	}

	/**
	 * @return
	 * @see bsh.EvalError#getErrorSourceFile()
	 */
	public String getErrorSourceFile() {
		return te.getErrorSourceFile();
	}

	/**
	 * @param t
	 * @return
	 * @see bsh.TargetError#printTargetError(java.lang.Throwable)
	 */
	public String printTargetError(Throwable t) {
		return te.printTargetError(t);
	}

	/**
	 * @return
	 * @see bsh.EvalError#getScriptStackTrace()
	 */
	public String getScriptStackTrace() {
		return te.getScriptStackTrace();
	}

	/**
	 * @return
	 * @see bsh.EvalError#getMessage()
	 */
	public String getMessage() {
		return te.getMessage();
	}

	/**
	 * @param s
	 * @see bsh.EvalError#setMessage(java.lang.String)
	 */
	public void setMessage(String s) {
		te.setMessage(s);
	}

	/**
	 * @return
	 * @see bsh.TargetError#inNativeCode()
	 */
	public boolean inNativeCode() {
		return te.inNativeCode();
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	public String getLocalizedMessage() {
		return te.getLocalizedMessage();
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getCause()
	 */
	public Throwable getCause() {
		return te.getCause();
	}

	/**
	 * @param s
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
	 */
	public void printStackTrace(PrintWriter s) {
		te.printStackTrace(s);
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getStackTrace()
	 */
	public StackTraceElement[] getStackTrace() {
		return te.getStackTrace();
	}

}
