package eu.irreality.age.util.networking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
 * To obtain an external local inet address, rather than the loopback address, under linux. 
 * @author carlos
 *
 */
public class LocalIPObtainer {

	
		
	
	
	  public static String longToIpV4(long longIp) {
		    int octet3 = (int) ((longIp >> 24) % 256);
		    int octet2 = (int) ((longIp >> 16) % 256);
		    int octet1 = (int) ((longIp >> 8) % 256);
		    int octet0 = (int) ((longIp) % 256);
		    return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
		  }
		  public static long ipV4ToLong(String ip) {
		    String[] octets = ip.split("\\.");
		    return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) +
		        (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
		  }
	
	public static boolean isIPv4Private(String ip)
	  {
	    long longIp = ipV4ToLong(ip);
	    return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255")) ||
	        (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255")) ||
	        longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
	  }
	
	public static boolean isIPv4Private ( InetAddress ip )
	{
		StringTokenizer st = new StringTokenizer(ip.toString(),"/");
		System.err.println("Analysing " + ip);
		if ( !ip.toString().startsWith("/") && st.hasMoreTokens() ) st.nextToken();
		if ( st.hasMoreTokens() ) 
		{
			try
			{
				String candidate = st.nextToken();
				System.err.println("Cand: " + candidate + ": " + isIPv4Private ( candidate ));
				return isIPv4Private ( candidate );
			}
			catch ( Exception exc )
			{
				exc.printStackTrace();
				return false;
			}
		}
		else return false;
	}
	
	/**
	 * Returns an InetAddress representing the address 
of the localhost.  
	 * Every attempt is made to find an address for this 
host that is not 
	 * the loopback address.  If no other address can 
be found, the 
	 * loopback will be returned.
	 * 
	 * @return InetAddress - the address of localhost
	 * @throws UnknownHostException - if there is a 
problem determing the address
	 */
	public static InetAddress getLocalHost() throws 
	UnknownHostException {
		InetAddress localHost = 
			InetAddress.getLocalHost();
		if(!localHost.isLoopbackAddress() && !localHost.isLinkLocalAddress() &&  !isIPv4Private(localHost)) return 
		localHost;
		InetAddress[] addrs = 
			getAllLocalUsingNetworkInterface();
		for(int i=0; i<addrs.length; i++) {
			System.err.println("Obtained " + addrs[i]);
			if( !addrs[i].isLoopbackAddress() && !addrs[i].isLinkLocalAddress() && !isIPv4Private(addrs[i]) )  //modified so it doesn't return lan addresses (192.168...) either.
				return addrs[i];
		}
		return localHost;	
	}

	/**
	 * This method attempts to find all InetAddresses 
for this machine in a 
	 * conventional way (via InetAddress).  If only one 
address is found 
	 * and it is the loopback, an attempt is made to 
determine the addresses 
	 * for this machine using NetworkInterface.
	 * 
	 * @return InetAddress[] - all addresses assigned to 
the local machine
	 * @throws UnknownHostException - if there is a 
problem determining addresses
	 */
	public static InetAddress[] getAllLocal() throws 
	UnknownHostException {
		InetAddress[] iAddresses = 
			InetAddress.getAllByName("127.0.0.1");
		if(iAddresses.length != 1) return 
		iAddresses;
		if(!iAddresses[0].isLoopbackAddress()) 
			return iAddresses;
		return getAllLocalUsingNetworkInterface();

	}

	/**
	 * Utility method that delegates to the methods of 
NetworkInterface to 
	 * determine addresses for this machine.
	 * 
	 * @return InetAddress[] - all addresses found from 
the NetworkInterfaces
	 * @throws UnknownHostException - if there is a 
problem determining addresses
	 */
	private static InetAddress[] 
	                           getAllLocalUsingNetworkInterface() throws 
	                           UnknownHostException {
		ArrayList addresses = new ArrayList();
		Enumeration e = null;
		try {
			e = 
				NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			throw new UnknownHostException
			("127.0.0.1");
		}
		while(e.hasMoreElements()) {
			NetworkInterface ni = 
				(NetworkInterface)e.nextElement();
			for(Enumeration e2 = 
				ni.getInetAddresses(); e2.hasMoreElements();) {
				addresses.add
				(e2.nextElement());
			}	
		}
		InetAddress[] iAddresses = new 
		InetAddress[addresses.size()];
		for(int i=0; i<iAddresses.length; i++) {
			iAddresses[i] = (InetAddress)
			addresses.get(i);
		}
		return iAddresses;
	}

}
