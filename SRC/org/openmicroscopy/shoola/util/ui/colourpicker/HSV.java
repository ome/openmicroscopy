/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.HSV.java
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies

/** 
 * HSV class for converting between RGB and HSV values. Stores all values as 
 * HSV float (h,s,v,a). 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
class HSV
{
    
	/** Hue component of the colour (0..1). */
	float 	h;

	/** Saturation component of the colour (0..1). */
	float 	s;
	
	/** Value component of the colour (0..1). */
	float 	v;

	/** Alpha component of the colour (0..1).*/
	float 	a;
	
	/**  Constructor of the HSV component (sets all values to 0). */
	HSV()
	{
		h = 0;
		s = 0;
		v = 0;
		a = 0;
	}
	
	/**
	 * Constructor of the HSV component (sets all values to the parameter c)
	 *
	 * @param c HSV colour to copy.
	 */
	HSV(HSV c)
	{
		h = c.h;
		s = c.s;
		v = c.v;
		a = c.a;
	}

	/**
	 * Constructor of the HSV component (sets all values to the parameter c)
	 *
	 * @param c RGB colour to copy (uses the {@link #fromColor(Color)} method.
	 */
	HSV(Color c)
	{
		this.fromColor(c);
	}

	/**
	 * Constructor of the HSV component. Initialises the values with the 
	 * parameters provided, has an alpha channel.
	 *
	 * @param hs hue. 
	 * @param ss saturation. 
	 * @param vs value. 
	 * @param as alpha.
	 */
	HSV(float hs, float ss, float vs, float as)
	{
		h = hs;
		s = ss;
		v = vs;
		a = as;
	}
	
	/**
	 * Constructor of the HSV component. Initialises the values with the 
	 * parameters provided, has no alpha channel.
	 *
	 * @param hs hue. 
	 * @param ss saturation. 
	 * @param vs value. 
	 */
	HSV(float hs, float ss, float vs)
	{
		h = hs;
		s = ss;
		v = vs;
		a = 1;
	}
	
	/**
	 * Sets the current HSV values from the RGB parameter c.
	 * 
	 * @param c RGB Colour to set the HSV values from.
	 */
	void fromColor(Color c)
	{
		float min, max, delta;

		min = findMin(c.getRed(), c.getGreen(), c.getBlue());
		max = findMax(c.getRed(), c.getGreen(), c.getBlue());
		
		v = max;				
		delta = max-min;

		if (max != 0.0f) s = delta/max;		
		else 
		{
			v = 0.0f;
			s = 0.0f;
			h = 0.0f;
		}

		if (c.getRed()==max)
			h = (c.getGreen()-c.getBlue())/delta;		
		else if (c.getGreen() == max)
			h = 2.0f + (c.getBlue()-c.getRed())/delta;	
		else
			h = 4.0f+(c.getRed()-c.getGreen())/delta;	

		h = h * 60.0f;			
		if (h < 0) h += 360.0f;
			
		h = h/360.0f;
		v = v/255.0f;
		s = s/255.0f;
		a = c.getAlpha()/255.0f;
	}
		
	/** 
	 * Finds the maximum value of a, b or c. Used to calculate Value and 
	 * Saturation.
	 * 
	 * @param a One of the values to analyze.
	 * @param b One of the values to analyze.   
	 * @param c One of the values to analyze.
	 * @return greatest of(a,b,c)
	 */
	float findMin(float a, float b, float c)
	{
		if (a < b && a < c) return a;
		if (b < a && b < c) return b;
		return c;
	}
	 
	/** 
	 * Finds the minimum value of a, b or c. Used to calculate Value and 
	 * Saturation.
	 * 
     * @param a One of the values to analyze.
     * @param b One of the values to analyze.   
     * @param c One of the values to analyze.
	 * @return smallest of(a,b,c)
	 */
	float findMax(float a, float b, float c)
	{
		if (a > b && a > c) return a;
		if (b > a && b > c) return b;
		return c;
	}
	
	/**
	 * Converts HSV values to RGB Colour components less alpha parameter.
	 * 
	 * @return See above.
	 */
	Color toColor()
	{	
		int i;
		float f, p, q, t;
		float r,g,b;
		
		float hquad;
		
		if (s == 0) {
			r = g = b = v;
			return new Color(r,g,b,a);
		}

		hquad = h*6;			
		i = (int) Math.floor(hquad);
		f = hquad-i;			
		p = v*(1-s);
		q = v*(1-s*f);
		t = v*(1-s*(1-f));

		switch (i) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			default:		
				r = v;
				g = p;
				b = q;
				break;
		}
		return new Color(r,g,b,1);
	}
	
	/**
	 * Converts HSV values to RGB Colour components with alpha channel.
	 * 
	 * @return See above.
	 */
	Color toColorA()
	{
		int i;
		float f, p, q, t;
		float r,g,b;
		
		float hquad;
		
		if (s == 0) {
			r = g = b = v;
			return new Color(r,g,b,a);
		}

		hquad = h*6;			
		i = (int) Math.floor(hquad);
		f = hquad-i;			
		p = v*(1-s);
		q = v*(1-s*f);
		t = v*(1-s*(1-f));

		switch (i) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			default:		
				r = v;
				g = p;
				b = q;
				break;
		}
		return new Color(r,g,b,a);
	}

}
