/*
 * (c) 2000-2009 Carlos G�mez Rodr�guez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

public interface MultimediaInputOutputClient extends InputOutputClient
{

	//sound
	public boolean isSoundEnabled();
	public SoundClient getSoundClient();
	
	//graphics
	public boolean isGraphicsEnabled();
	public void insertIcon ( String fileName );
	
	public void insertCenteredIcon ( String fileName );
	
	public void useImage ( String fileName , int mode , int location , int scaling );
	public void addFrame ( int position , int size );
	public void removeFrames();
		
}