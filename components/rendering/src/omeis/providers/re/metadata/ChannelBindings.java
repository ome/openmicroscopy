/*
 * omeis.providers.re.metadata.ChannelBindings
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeis.providers.re.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** TODO: review javadoc.
 * Tells which pixel intensity interval of a specified wavelength is affected
 * by quantization. Also associates the wavelength to a color and tells
 * whether or not it has to be mapped.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.5 $ $Date: 2005/06/13 21:09:39 $)
 * </small>
 * @since OME2.2
 */
public class ChannelBindings
{
    
    /** Identifier of the red component. */
    public static final int     RED = 0;
    
    /** Identifier of the green component. */
    public static final int     GREEN = 1;
    
    /** Identifier of the blue component. */
    public static final int     BLUE = 2;
    
    /** Identifier of the alpha component. */
    public static final int     ALPHA = 3;
    
    /** Default value for the alpha component. */
    public static final int     DEFAULT_ALPHA = 255;
    
	/** The bounds of the color range. */
	private static final int   COLOR_MIN = 0, COLOR_MAX = 255;

	/** The OME index of the wavelength. */
	private int 				index;
	
	/** The lower bound of the pixel intensity interval. */
	private double 				inputStart;
	
	/** The upper bound of the pixel intensity interval. */
	private double 				inputEnd;
	
	/** Color associated to the wavelength. */
	private int[] 				rgba;
	
	/** 
	 *<code>true</code> if the wavelength has to mapped, 
	 *<code>false</code> otherwise.
	 */
	private boolean				active;
	
    /** 
     * Identifies a family of maps. 
     * One of the constants defined by 
     * {@link omeis.providers.re.quantum.QuantumFactory QuantumFactory}.
     */
    private int                 family;
    
    /** Selects a curve in the family. */
    private double              curveCoefficient;
    
    private double[]            stats;
    
    /**
     * Apply or not the algorithm to reduce the noise.
     * If <code>true</code>, the values close to the min or max are map to 
     * a constant.
     */
    private boolean             noiseReduction;
    
    
    public ChannelBindings(int index, double inputStart, double inputEnd,
                            int[] color, boolean active, int family, 
                            double curveCoefficient)
	{
		this.index = index;
		setInputWindow(inputStart, inputEnd);
		rgba = new int[4];
		setRGBA(color[RED], color[GREEN], color[BLUE], color[ALPHA]);
		this.active = active;
        this.family = family;
        this.curveCoefficient = curveCoefficient;
	}

	/** Private empty constructor. */
	private ChannelBindings() {}
	
	/** TEST. can be null, value between 0 and 1*/
    public void setStats(double[] stats) { this.stats = stats; }
    
    public double[] getStats() { return stats; }
    
    public void setNoiseReduction(boolean nr) { noiseReduction = nr; }
    
    public boolean getNoiseReduction() { return noiseReduction; }
    
    public boolean isActive() { return active; }
    
    public int getIndex() { return index; }

    public double getInputEnd() { return inputEnd; }

    public double getInputStart() { return inputStart; }

    public int[] getRGBA() 
    {
        int[] colors = new int[rgba.length];
        for (int i = 0; i < rgba.length; i++)
            colors[i] = rgba[i];
        return colors;
    }

    public int getFamily() { return family; }
    
    public double getCurveCoefficient() { return curveCoefficient; }
    
    public void setQuantizationMap(int family, double curveCoefficient, 
                                boolean noiseReduction)
    {
        this.family = family;
        this.curveCoefficient = curveCoefficient;
        this.noiseReduction = noiseReduction;
    }

    public void setActive(boolean active) { this.active = active; }

    //TODO: checks done in QuantumStrategy, where do they belong to?
    public void setInputWindow(double start, double end)
    {
        inputStart = start;
        inputEnd = end;
    }

    public void setRGBA(int red, int green, int blue, int alpha)
    {
        verifyColorComponent(red);
        verifyColorComponent(green);
        verifyColorComponent(blue);
        verifyColorComponent(alpha);
        rgba[RED] = red;
        rgba[GREEN] = green;
        rgba[BLUE] = blue;
        rgba[ALPHA] = alpha;
    }
    
    public void setRGBA(int[] rgba)
    {
        if (rgba == null || rgba.length != 4)
            throw new IllegalArgumentException("Invalid rgba array.");
        verifyColorComponent(rgba[RED]);
        verifyColorComponent(rgba[GREEN]);
        verifyColorComponent(rgba[BLUE]);
        verifyColorComponent(rgba[ALPHA]);
        this.rgba[RED] = rgba[RED];
        this.rgba[GREEN] = rgba[GREEN];
        this.rgba[BLUE] = rgba[BLUE];
        this.rgba[ALPHA] = rgba[ALPHA];
    }
    
    /** Make a copy of the object. */
    ChannelBindings copy()
    {
        ChannelBindings cb = new ChannelBindings();
        cb.index = this.index;
        cb.active = this.active;
        cb.family = this.family;
        cb.curveCoefficient = this.curveCoefficient;
        //Will work b/c the objects are read-only: Integer, Float, etc.
        cb.inputStart = this.inputStart;
        cb.inputEnd = this.inputEnd;
        cb.rgba = getRGBA();
        cb.stats = getStats();
        cb.noiseReduction  = getNoiseReduction();
        return cb;
    }
    
    /** Verify the color components. */
    private void verifyColorComponent(int c)
    {
        if (c < COLOR_MIN || COLOR_MAX < c)
            throw new IllegalArgumentException("Value must be in [0,255].");
    }
    
}
