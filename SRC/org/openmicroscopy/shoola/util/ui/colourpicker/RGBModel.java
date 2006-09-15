/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.RGBModel
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
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RGBModel 
{
    
	/** Original red Component of the model. */
	private float		originalRed;

    /** Original green Component of the model. */
	private float		originalGreen;
	
    /** Original blue Component of the model. */
	private float		originalBlue;
	
    /** Original alpha Component of the model. */
	private float		originalAlpha;
	
	/** Red component of the model, as float between 0..1 */
	private float 		red; 
	
	/** Green component of the model, as float between 0..1  */
	private float 		green; 
	
	/** Blue component of the model, as float between 0..1  */
	private float 		blue; 
	
	/** Alpha component of the model, as float between 0..1 */
	private float 		alpha; 
	
    /**
     * Sets the value of the model to v without firing an event.
     * 
     * @param v value.
     */
    private void setV(float v)
    {
        float []vals = RGBtoHSV();
        vals[2] = v;
        float rgb[] = new float[3];
        rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
    }
    
    /**
     * Sets the hue of the model to h without firing an event.
     * 
     * @param h hue.
     */
    private void setH(float h)
    {
        float []vals = RGBtoHSV();
        vals[0] = h;
        float rgb[] = new float[3];
        rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
    }
    
    /**
     * Sets the saturation of the model to S without firing an event.
     * 
     * @param s saturation.
     */
    private void setS(float s)
    {
        float []vals = RGBtoHSV();
        vals[1] = s;
        float rgb[] = new float[3];
        rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
    }
    
	/**
	 * Sets the models initial conditions to the params (r,g,b,a).
	 * 
	 * @param r  red channel.
	 * @param g  green channel.
	 * @param b  blue channel.
	 * @param a  alpha channel.
	 */
	RGBModel(float r, float g, float b, float a)
	{
		originalRed = r;
		originalGreen = g;
		originalBlue = b;
		originalAlpha = a;
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	
	/**
	 * Gets the red channel of the model.
	 * 
	 * @return See above.
	 */
	float getRed() { return red; }
	
	/**
	 * Gets the green channel of the model.
	 * 
	 * @return See above.
	 */
	float getGreen() { return green; }
	
	/**
	 * Gets the blue channel of the model.
     * 
	 * @return See above.
	 */
	float getBlue() { return blue; }
	
	/**
	 * Gets the alpha channel of the model.
	 * 
	 * @return See above.
	 */
	float getAlpha() { return alpha; }

	/**
	 * Gets the hue of the model.
	 * 
	 * @return See above.
	 */
	float getHue()
	{
		float[] vals = RGBtoHSV();
		return vals[0];
	}
	
	/**
	 * Gets the saturation of the model.
	 * 
	 * @return See above.
	 */
	float getSaturation()
	{
		float[] vals = RGBtoHSV();
		return vals[1];
	}
	
	/**
	 * Gets the vbalue of the model (HSV).
	 * 
	 * @return See above.
	 */
	float getValue()
	{
		float[] vals = RGBtoHSV();
		return vals[2];
	}
	
	/**
	 * Sets the value of the model (HSV) to v.
	 * 
	 * @param v  value.
	 */
	void setValue(float v) { setV(v); }
	
	/**
	 * Sets the hue of the model to h.
	 * 
	 * @param h hue.
	 */
	void setHue(float h) { setH(h); }
	
	/**
	 * Sets the saturation of the model to s.
	 * 
	 * @param s saturation.
	 */
	void setSaturation(float s) { setS(s); }
	
	/**
	 * Sets the red channel of the model to r.
	 * 
	 * @param r red.
	 */
	void setRed(float r) { red = r; }
	
	/**
	 * Sets the green channel of the model to g.
	 * 
	 * @param g green.
	 */
	void setGreen(float g) { green = g; }
	
	/**
	 * Sets the blue channel of the model to b.
	 * 
	 * @param b blue.
	 */
	void setBlue(float b) { blue = b; }
	
	/**
	 * Sets the alpha of the model to a.
	 * 
	 * @param a alpha.
	 */
	void setAlpha(float a) { alpha = a; }
	
	/**
	 * Sets the colour to the new Color c.
	 * 
	 * @param c new Colour.
	 */
	void setColour(Color c)
	{
		red = (c.getRed()/255.0f);
		green = (c.getGreen()/255.0f);
		blue = (c.getBlue()/255.0f);
	}
	
	/** 
	 * Sets the colour of the model based on the rgb values r, b, g and alpha 
     * (a).
	 * 
	 * @param r red.
	 * @param g green.
	 * @param b blue.
	 * @param a alpha.
	 */
	void setRGBColour(float r, float g, float b, float a)
	{
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}
	
	/**
	 * Sets the colour of the model based on the hsv values h, s, v and alpha 
     * (a).
	 * 
	 * @param h hue.
	 * @param s saturation.
	 * @param v value.
	 * @param a alpha.
	 */
	void setHSVColour(float h, float s, float v, float a)
	{
		float []rgb = new float[3];
		rgb = HSVtoRGB(h,s,v);
		red = rgb[0];
		green = rgb[1];
		blue = rgb[2];
		alpha = a;
	}

	/**
	 * Returns the Colour representing by the model.
	 * 
	 * @return Color from model.
	 */
	Color getColour() { return new Color(red, green, blue, alpha); }

	/**
	 * Converts current RGB model components to HSV and returns it as an array.
	 * 
	 * @return hsv conversion of the red, green and blue components to hsv.
	 */
	float [] RGBtoHSV()
	{
		float hsv[] = new float[3];
		
		float min, max, delta;

		float h, s, v;
		
		min = findMin(red, green, blue);
		max = findMax(red, green, blue);
		
		v = max;				
		delta = max-min;

		if (max != 0.0f) s = delta/max;		
		else 
		{
			v = 0.0f;
			s = 0.0f;
			h = 0.0f;
		}
		
		h = 0;
		
		if (red==max) h = (green-blue)/delta;		
		else if (green == max) h = 2.0f + (blue-red)/delta;	
		else if (blue == max) h = 4.0f+(red-green)/delta;
		
		h = h * 60.0f;			
		if (h < 0) h += 360.0f;	
		h = h/360.0f;
		
		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;
		return hsv;
	}

	/** 
	 * Finds the minimum value of a, b or c. Used to calculate Value and 
	 * Saturation.
	 * 
	 * @param a One of the values to analyze.
	 * @param b One of the values to analyze.
	 * @param c One of the values to analyze.
	 * @return greatest of(a,b,c)
	 */
	float findMin(float a, float b, float c)
	{
		if (a <= b && a <= c) return a;
		else if (b <= a && b <= c) return b;
		else if (c <= a && c <= b) return c;
		return c;

	}
	
	/** 
	 * Finds the maximum value of a,b or c. Used to calculate Value and 
	 * Saturation.
	 * 
     * @param a One of the values to analyze.
     * @param b One of the values to analyze.
     * @param c One of the values to analyze.
	 * @return smallest of(a,b,c)
	 */
	float findMax(float a, float b, float c)
	{
		if (a >= b && a >= c) return a;
		if (b > a && b > c) return b;
		return c;
	}
	
	/**
	 * Converts 3 float parameters (h, s, v) to 3 float 
	 * RGB values which it returns.
	 * 
	 * @param h hue (0..1).
	 * @param s saturation (0..1).
	 * @param v value (0..1).
	 * @return array of floats representing RGB.
	 */
	float [] HSVtoRGB(float h, float s, float v)
	{
    	int i;
    	float f, p, q, t;
    	float rgb [] = new float[3];
    	float r,g,b;
    	
    	if( s == 0 ) 
    	{
    		r = g = b = v;
    		rgb[0] = r;
    		rgb[1] = g;
    		rgb[2] = b;
    		return rgb;
    	}
    
    	h *= 6;			
    	i = (int) Math.floor(h);
    	f = h-i;			
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
    	rgb[0] = r;
    	rgb[1] = g;
    	rgb[2] = b;
    	
    	return rgb;	
	}

    /**
     * Returns the original color.
     * 
     * @return See above.
     */
    Color getOriginalColor()
    {
        return new Color(originalRed, originalGreen, originalBlue, 
                originalAlpha);
    }
    
	/** Allows the user to revert to their original colour selection. */
	void revert()
	{
		red = originalRed;
		green = originalGreen;
		blue = originalBlue;
		alpha = originalAlpha;
	}
	
}



