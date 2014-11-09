package eu.irreality.age.swing;

/**
 * Defines a transformation on font size. Can be adding an offset, or multiplying by a number (i.e. zooming)
 * @author carlos
 *
 */
public class FontSizeTransform 
{

	public static int ADD = 0;
	public static int MULTIPLY = 1;
	
	private int type = ADD;
	private double param = 4.0;
	
	/**
	 * Creates a new FontSizeTransform. If type is FontSizeTransform.ADD, then the transform adds param as a
	 * (positive or negative) offset to font size. If type is FontSizeTransform.MULTIPLY, then the transform
	 * scales font size by the factor given in param.
	 * @param type
	 * @param param
	 */
	public FontSizeTransform ( int type , double param )
	{
		this.type = type;
		this.param = param;
	}
	
	public int apply ( int size )
	{
		if ( type == ADD )
		{
			return size + (int)param;
		}
		else //if ( type == MULTIPLY )
		{
			return (int) Math.round ( size * param );
		}
	}
	
	public int getType()
	{
		return type;
	}
	
	public double getAmount()
	{
		return param;
	}
	
}
