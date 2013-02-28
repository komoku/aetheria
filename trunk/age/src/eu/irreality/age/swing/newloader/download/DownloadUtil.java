package eu.irreality.age.swing.newloader.download;

import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil 
{
	
    /**
     * Obtains the size of the remote file pointed to by an URL.
     * @param url
     * @return
     */
    public static int contentLength( URL url ) 
    {
        HttpURLConnection connection;
        int contentLength = -1;

        try 
        {
            HttpURLConnection.setFollowRedirects( false );
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod( "HEAD" );
            contentLength = connection.getContentLength();
        } 
        catch ( Exception e ) { }
        return contentLength;
    }

}
