package eu.irreality.age.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

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
		report.append("**\n");
		return report.toString();
	}
	
	public static String getExceptionReport ( bsh.EvalError te )
	{
		StringBuffer report = new StringBuffer();
		report.append("File: " + te.getErrorSourceFile() + "\n" );
		report.append("Line: " + te.getErrorLineNumber() + "\n" );
		report.append("Error: " + te.getErrorText() + "\n" );
		report.append("Stack trace: " + getStackTrace(te) + "\n" );
		report.append("Cause report: " + getExceptionReport ( te.getCause() ) );
		return report.toString();
	}
	
	public static String getExceptionReport ( Throwable e )
	{
		return getStackTrace(e);
	}
	
	
	
}
