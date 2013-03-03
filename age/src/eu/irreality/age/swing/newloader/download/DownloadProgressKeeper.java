package eu.irreality.age.swing.newloader.download;

import java.awt.Dimension;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import eu.irreality.age.swing.newloader.GameEntry;

public class DownloadProgressKeeper implements ProgressKeepingDelegate 
{

	private boolean downloaded;
	private boolean downloadInProgress;
	
	private double progress;
	String progressString;
	
	private GameEntry theGameEntry;
	
	private JProgressBar jpb = new JProgressBar(0,100);
	
	public DownloadProgressKeeper ( GameEntry theGameEntry )
	{
		this.theGameEntry = theGameEntry;
		downloaded = theGameEntry.isDownloaded();
		jpb.setStringPainted(true);
		int preferredHeight = jpb.getPreferredSize() != null ? jpb.getPreferredSize().height : 50;
		jpb.setPreferredSize( new Dimension(600,preferredHeight) );
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				if ( downloaded )
				{
					jpb.setValue(100);
					jpb.setString("Game is available");
				}
				else
				{
					jpb.setValue(0);
					jpb.setString("Game not downloaded");
				}
			}
		}
		);
	}
	
	public JProgressBar getBar()
	{
		return jpb;
	}
	
	public void progressUpdate( double progress , String progressString ) 
	{
		this.progress = progress;
		this.progressString = progressString;
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				jpb.setValue( (int)Math.round(DownloadProgressKeeper.this.progress*100) );
				jpb.setString(DownloadProgressKeeper.this.progressString + " (" + ((int)Math.round(DownloadProgressKeeper.this.progress*100)) + "%)");
			}
		});
	}

}
