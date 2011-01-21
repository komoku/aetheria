/*
 * (c) 2005-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 * 
 * Created at regulus on 10-dic-2008 18:58:17
 * as file ImageManager.java on package org.f2o.absurdum.puck.gui.skin
 */
package org.f2o.absurdum.puck.gui.skin;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.f2o.absurdum.puck.gui.config.PuckConfiguration;
import org.f2o.absurdum.puck.gui.graph.CharacterNode;

/**
 * @author carlos
 *
 * Created at regulus, 10-dic-2008 18:58:17
 */
public class ImageManager 
{


	/**Singleton instance.*/
	private static ImageManager instance;
	
	/**Skin to use.*/
	private Skin skin;
		
	/**Singleton instance getter.*/
	public static ImageManager getInstance()
	{
		if ( instance == null )
			instance = new ImageManager();
		return instance;
	}
	
	public ImageManager()
	{
		System.out.println("ImageManager constructor");
		String skinName = PuckConfiguration.getInstance().getProperty("skin");
		System.out.println("Creating skin: " + skinName);
		 skin = new Skin(skinName);
		System.out.println("Skin " + skin + " created.");
	}
	
	public void setSkin ( String skinName )
	{
		skin = new Skin(skinName); //getImage, loadImage, etc. will go to the new image locations
		codesToImages.clear(); //clears the image cache
	}
	
	private Image loadImageFromFile ( String pathToFile ) throws IOException
	{
		System.out.println("Loading image " + pathToFile);
		try
		{
			return ImageIO.read(this.getClass().getClassLoader().getResource(pathToFile));
		}
		catch ( IllegalArgumentException iae )
		{
			throw new IOException(iae.toString());
		}
	}
	
	private InputStream loadStreamFromFile ( String pathToFile ) throws IOException
	{
		System.out.println("Loading stream " + pathToFile);
		InputStream is = (this.getClass().getClassLoader().getResourceAsStream(pathToFile));
		if ( is == null ) throw new IOException("Could not find image file " + pathToFile + " at skin " + skin);
		return is;
	}
	
	private Image loadImage ( String imageCode )
	{
		System.out.println("Loading image with code " + imageCode);
		String imagePath = skin.getImagePath(imageCode);
		System.out.println("The path is: " + imagePath);
		try
		{
			return loadImageFromFile ( imagePath );
		}
		catch ( IOException ioe )
		{
			System.err.println("Couldn't load image responding to code " + imageCode + " on skin " + skin);
			System.err.println("File " + imagePath + " seems to be missing.");
			return null;
		}
	}
	
	private Hashtable codesToImages = new Hashtable();
	
	public Image getImage ( String imageCode )
	{
		Image img = (Image) codesToImages.get(imageCode);
		if ( img == null )
		{
			img = loadImage(imageCode);
			codesToImages.put(imageCode,img);
		}
		return img;
	}
	
	public InputStream getImageStream ( String imageCode )
	{
		try
		{
			String path = skin.getImagePath(imageCode);
			if ( path == null ) throw new IOException("The skin " + skin + " doesn't give any information about the path to the image with code " + imageCode);
			InputStream	imgStream = loadStreamFromFile(path);
			return imgStream;
		}
		catch ( IOException ioe )
		{
			System.err.println("Couldn't load stream for image responding to code " + imageCode + " on skin " + skin);
			ioe.printStackTrace();
			return null;
		}
	}


}
