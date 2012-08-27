package eu.irreality.age.language;

/**
 * This object encapsulates state regarding the last mentioned objects and verbs.
 * @author carlos
 *
 */
public class Mentions 
{

	//verb
	private String lastMentionedVerb = "mirar";
	
	//masculine singular
	private String lastMentionedObjectMS="";
	
	//feminine singular
	private String lastMentionedObjectFS="";
	
	//singular
	private String lastMentionedObjectS="";
	
	//masc plural
	private String lastMentionedObjectMP="";
	
	//fem plural
	private String lastMentionedObjectFP="";
	
	//plural
	private String lastMentionedObjectP="";

	
	
	public String toString() 
	{
		return "Mentions [lastMentionedVerb=" + lastMentionedVerb
				+ ", lastMentionedObjectMS=" + lastMentionedObjectMS
				+ ", lastMentionedObjectFS=" + lastMentionedObjectFS
				+ ", lastMentionedObjectS=" + lastMentionedObjectS
				+ ", lastMentionedObjectMP=" + lastMentionedObjectMP
				+ ", lastMentionedObjectFP=" + lastMentionedObjectFP
				+ ", lastMentionedObjectP=" + lastMentionedObjectP + "]";
	}

	/**
	 * @return the lastMentionedVerb
	 */
	public String getLastMentionedVerb() 
	{
		return lastMentionedVerb;
	}

	/**
	 * @param lastMentionedVerb the lastMentionedVerb to set
	 */
	public void setLastMentionedVerb(String lastMentionedVerb) 
	{
		this.lastMentionedVerb = lastMentionedVerb;
	}

	/**
	 * @return the lastMentionedObjectMS
	 */
	public String getLastMentionedObjectMS() 
	{
		return lastMentionedObjectMS;
	}

	/**
	 * @param lastMentionedObjectMS the lastMentionedObjectMS to set
	 */
	public void setLastMentionedObjectMS(String lastMentionedObjectMS) 
	{
		this.lastMentionedObjectMS = lastMentionedObjectMS;
	}

	/**
	 * @return the lastMentionedObjectFS
	 */
	public String getLastMentionedObjectFS() 
	{
		return lastMentionedObjectFS;
	}

	/**
	 * @param lastMentionedObjectFS the lastMentionedObjectFS to set
	 */
	public void setLastMentionedObjectFS(String lastMentionedObjectFS) 
	{
		this.lastMentionedObjectFS = lastMentionedObjectFS;
	}

	/**
	 * @return the lastMentionedObjectS
	 */
	public String getLastMentionedObjectS() 
	{
		return lastMentionedObjectS;
	}

	/**
	 * @param lastMentionedObjectS the lastMentionedObjectS to set
	 */
	public void setLastMentionedObjectS(String lastMentionedObjectS) 
	{
		this.lastMentionedObjectS = lastMentionedObjectS;
	}

	/**
	 * @return the lastMentionedObjectMP
	 */
	public String getLastMentionedObjectMP() 
	{
		return lastMentionedObjectMP;
	}

	/**
	 * @param lastMentionedObjectMP the lastMentionedObjectMP to set
	 */
	public void setLastMentionedObjectMP(String lastMentionedObjectMP) 
	{
		this.lastMentionedObjectMP = lastMentionedObjectMP;
	}

	/**
	 * @return the lastMentionedObjectFP
	 */
	public String getLastMentionedObjectFP() 
	{
		return lastMentionedObjectFP;
	}

	/**
	 * @param lastMentionedObjectFP the lastMentionedObjectFP to set
	 */
	public void setLastMentionedObjectFP(String lastMentionedObjectFP) 
	{
		this.lastMentionedObjectFP = lastMentionedObjectFP;
	}

	/**
	 * @return the lastMentionedObjectP
	 */
	public String getLastMentionedObjectP() 
	{
		return lastMentionedObjectP;
	}

	/**
	 * @param lastMentionedObjectP the lastMentionedObjectP to set
	 */
	public void setLastMentionedObjectP(String lastMentionedObjectP) 
	{
		this.lastMentionedObjectP = lastMentionedObjectP;
	}
	
	
	
}
