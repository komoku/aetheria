/**
 * (c) 2000-2011 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license.txt / License in license.txt
 * File created: 26/07/2012 12:15:30
 */
package eu.irreality.age.util.compression;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author carlos
 *
 * This class codes/decodes strings into/from a base 64 compressed form.
 */
public class StringCompressor 
{

	public static String compress ( String orig , String encoding ) throws UnsupportedEncodingException
	{
		byte[] input = orig.getBytes("UTF-8");
		
		// Compress the bytes
		byte[] output = new byte[input.length];
		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();
		int compressedDataLength = compresser.deflate(output);
		
		char[] base64encoded = Base64Coder.encode(output,compressedDataLength);
		return new String(base64encoded);
	}
	
	public static String decompress ( String compressed , String encoding , int bufsize ) throws UnsupportedEncodingException, DataFormatException
	{
		byte[] base64decoded = Base64Coder.decode(compressed);
		
		// Decompress the bytes
		Inflater decompresser = new Inflater();
		decompresser.setInput(base64decoded);
		byte[] result = new byte[bufsize];
		int resultLength = decompresser.inflate(result);
		decompresser.end();

		// Decode the bytes into a String
		String decodedString = new String(result, 0, resultLength, encoding);
		return decodedString;
	}
	
	public static String decompress ( String compressed , String encoding ) throws UnsupportedEncodingException, DataFormatException
	{
		return decompress ( compressed, encoding, 10000 );
	}
	
}
