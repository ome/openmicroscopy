/*
 * org.openmicroscopy.shoola.util.ui.colour.GradientUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.colour;


//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies

/** 
 * Collection of methods decomposing the colors composing a gradient.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class GradientUtil
{

	/** From <code>Blue</code> to <code>Red</code>. */
    public final static Color[] GRADIENT_BLUE_TO_RED = 
    	createGradient(Color.BLUE, Color.RED);

	/** From <code>Red</code> to <code>Blue</code>. */
    public final static Color[] GRADIENT_RED_TO_BLUE = 
    	createGradient(Color.RED, Color.BLUE);
    
    /** From <code>Black</code> to <code>White</code>. */
    public final static Color[] GRADIENT_BLACK_TO_WHITE = 
    	createGradient(Color.BLACK, Color.WHITE);

	/** Black, Red, Orange, Yellow, white. */ //added few more
    public final static Color[] GRADIENT_HOT = createGradient(
    		new Color[]{Color.BLACK, Color.RED, 
    				Color.ORANGE, Color.YELLOW, Color.GREEN,
    				Color.BLUE, new Color(138, 43, 226), Color.WHITE}); 
    
	/** The default number of steps. */
	public static final int STEPS = 500;
	
	/**
	 * Decomposes the color composing a gradient.
	 * 
	 * @param start The starting color.
	 * @param end   The ending color.
	 * @return See above.
	 */
	public static Color[] createGradient(Color start, Color end)
	{
		return createGradient(start, end, STEPS);
	}
	
	/**
	 * Decomposes the colors composing a gradient.
	 * 
	 * @param start The starting color.
	 * @param end   The ending color.
	 * @param steps The number of steps.
	 * @return See above.
	 */
	public static Color[] createGradient(Color start, Color end, int steps)
	{
		if (start == null || end == null) return null;
		if (steps <= 1) steps = 2;
		Color[] colors = new Color[steps];
		int r1 = start.getRed();
		int g1 = start.getBlue();
		int b1 = start.getGreen();
		int a1 = start.getAlpha();
		
		int r2 = end.getRed();
		int g2 = end.getGreen();
		int b2 = end.getGreen();
		int a2 = end.getAlpha();
		
		double norm;
        for (int i = 0; i < steps; i++) {
            norm = i/(double) steps; 
            colors[i] = new Color((int) (r1+norm*(r2-r1)), 
            		(int) (g1+norm*(g2-g1)), (int) (b1+norm*(b2-b1)), 
            				(int) (a1+norm*(a2-a1)));
        }
		return colors;
	}
	
	/**
	 * Decomposes the colors composing a gradient.
	 * 
	 * @param colors The initial colors.
	 * @return See above.
	 */
	public static Color[] createGradient(Color[] colors)
	{
		return createGradient(colors, STEPS);
	}
	
    /**
     * Decomposes the colors composing a gradient.
     * 
     * @param colors The initial colors.
	 * @param steps The number of steps.
	 * @return See above.
     */
    public static Color[] createGradient(Color[] colors, int steps)
    {
    	if (colors == null || colors.length < 2) return null;
    	
        int num = colors.length-1;
        int index = 0; 
        Color[] gradient = new Color[steps];
        Color[] temp;
        
        for (int j = 0; j < num; j++) {
            temp = createGradient(colors[j], colors[j+1], steps/num);
            for (int i = 0; i < temp.length; i++)
            	gradient[index++] = temp[i];
        }
        if (index < steps) {
            for (; index < steps; index++)
                gradient[index] = colors[colors.length-1];
        }
        return gradient;
    }
	
}
