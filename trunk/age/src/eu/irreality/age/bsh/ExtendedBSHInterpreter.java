package eu.irreality.age.bsh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * This extension of the BeanShell interpreter adds support from sourcing from streams (jar files, etc.)
 * @author carlos
 *
 */
public class ExtendedBSHInterpreter extends Interpreter
{

	public Object source ( URL url ) throws EvalError, IOException 
	{
		return eval( new BufferedReader(new InputStreamReader(url.openStream())), this.getNameSpace() , "URL: "+url.toString() );
	}
	
	public Object source ( InputStream stream ) throws EvalError, IOException 
	{
		return eval( new BufferedReader(new InputStreamReader(stream)), this.getNameSpace() , "Stream: " + stream );
	}

	
}
