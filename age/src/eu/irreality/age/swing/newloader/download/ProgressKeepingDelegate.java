package eu.irreality.age.swing.newloader.download;

/**
 * An object that is interested in progress updates from a ProgressKeepingReadableByteChannel.
 * @author carlos
 *
 */
public interface ProgressKeepingDelegate 
{

	/**
	 * Callback method that will be called by a ProgressKeepingReadableByteChannel to notify its progress.
	 * @param progress
	 */
	public void progressUpdate ( /*ProgressKeepingReadableByteChannel rbc,*/ double progress , String progressString ); 
	
}
