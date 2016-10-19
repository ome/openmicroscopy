/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.RGBModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.Color;
import java.util.Collection;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The model of the RGB slider.
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
	
	/** Red component of the model, as float between [0..1]. */
	private float 		red; 
	
	/** Green component of the model, as float between [0..1]. */
	private float 		green; 
	
	/** Blue component of the model, as float between [0..1]. */
	private float 		blue; 
	
	/** Alpha component of the model, as float between [0..1]. */
	private float 		alpha; 
	
	/** All available lookup tables */
	private Collection<String> availableLUTs;
	
	/** The original lookup table */
    private String originalLut;
    
	/** The lookup table */
	private String lut;
	
	/** The reverse intensity flag */
	private boolean revInt;
	
	/** The original reverse intensity flag */
	private boolean originalRevInt;
	
    /**
     * Sets the value of the model to v without firing an event.
     * 
     * @param v
     *            value.
     * @param reset
     *            Pass <code>true</code> to also overwrite the original color
     *            values (see {@link #revert()})
     */
    private void setV(float v, boolean reset)
    {
        float []vals = RGBtoHSV();
        vals[2] = v;
        float[] rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
            originalAlpha = alpha;
        }
    }
    
    /**
     * Sets the hue of the model to h without firing an event.
     * 
     * @param h hue.
     * @param reset
     *            Pass <code>true</code> to also overwrite the original color
     *            values (see {@link #revert()})
     */
    private void setH(float h, boolean reset)
    {
        float []vals = RGBtoHSV();
        vals[0] = h;
        float[] rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
        }
    }
    
    /**
     * Sets the saturation of the model to S without firing an event.
     * 
     * @param s saturation.
     * @param reset
     *            Pass <code>true</code> to also overwrite the original color
     *            values (see {@link #revert()})
     */
    private void setS(float s, boolean reset)
    {
        float[] vals = RGBtoHSV();
        vals[1] = s;
        float[] rgb = HSVtoRGB(vals[0], vals[1], vals[2]);
        red = rgb[0];
        green = rgb[1];
        blue = rgb[2];
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
        }
    }
    
	/**
	 * Sets the models initial conditions to the params (r,g,b,a).
	 * 
	 * @param r  red channel.
	 * @param g  green channel.
	 * @param b  blue channel.
	 * @param a  alpha channel.
	 * @param lut lookup table
	 * @param availableLUTs available lookup tables
	 * @param revInt reverse intensity flag
	 */
	RGBModel(float r, float g, float b, float a, String lut, Collection<String> availableLUTs, boolean revInt)
	{
		originalRed = r;
		originalGreen = g;
		originalBlue = b;
		originalAlpha = a;
		originalLut = lut;
		red = r;
		green = g;
		blue = b;
		alpha = a;
		this.lut = lut;
		this.availableLUTs = availableLUTs;
		this.revInt = revInt;
		this.originalRevInt = revInt;
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
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setValue(float v, boolean reset) { setV(v, reset); }
	
	/**
     * Sets the value of the model (HSV) to v.
     * 
     * @param v  value.
     */
	void setValue(float v) { setValue(v, false); }
	
	/**
	 * Sets the hue of the model to h.
	 * 
	 * @param h hue.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setHue(float h, boolean reset) { setH(h, reset); }
	
	/**
     * Sets the hue of the model to h.
     * 
     * @param h hue.
     */
	void setHue(float h) { setH(h, false); }
	
	/**
	 * Sets the saturation of the model to s.
	 * 
	 * @param s saturation.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setSaturation(float s, boolean reset) { setS(s, reset); }
	
	/**
     * Sets the saturation of the model to s.
     * 
     * @param s saturation.
     */
	void setSaturation(float s) { setSaturation(s, false); }
	
	/**
	 * Sets the red channel of the model to r.
	 * 
	 * @param r red.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setRed(float r, boolean reset) { red = r; if (reset) originalRed = red;}
	
	/**
     * Sets the red channel of the model to r.
     * 
     * @param r red.
     */
	void setRed(float r) {
	    setRed(r, false);
	}
	
	/**
	 * Sets the green channel of the model to g.
	 * 
	 * @param g green.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setGreen(float g, boolean reset) { green = g; if (reset) originalGreen = green;}
	
	/**
     * Sets the green channel of the model to g.
     * 
     * @param g green.
     */
	void setGreen(float g) {
	    setGreen(g, false);
	}
	
	/**
	 * Sets the blue channel of the model to b.
	 * 
	 * @param b blue.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setBlue(float b, boolean reset) { blue = b; if (reset) originalBlue = blue;}
	
	/**
     * Sets the blue channel of the model to b.
     * 
     * @param b blue.
     */
    void setBlue(float b) {
        setBlue(b, false);
    }
	
	/**
	 * Sets the alpha of the model to a.
	 * 
	 * @param a alpha.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setAlpha(float a, boolean reset) { alpha = a; if (reset) originalAlpha = alpha;}
	
	/**
     * Sets the alpha of the model to a.
     * 
     * @param a alpha.
     */
    void setAlpha(float a) {
        setAlpha(a, false);
    }
	
	/**
	 * Sets the colour to the new Color c.
	 * 
	 * @param c new Colour.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setColour(Color c, boolean reset)
	{
		red = (c.getRed()/255.0f);
		green = (c.getGreen()/255.0f);
		blue = (c.getBlue()/255.0f);
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
        }
	}
	
	/**
     * Sets the colour to the new Color c.
     * 
     * @param c new Colour.
     */
	void setColour(Color c) {
	    setColour(c, false);
	}
	
	/** 
	 * Sets the colour of the model based on the rgb values r, b, g and alpha 
     * (a).
	 * 
	 * @param r red.
	 * @param g green.
	 * @param b blue.
	 * @param a alpha.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setRGBColour(float r, float g, float b, float a, boolean reset)
	{
		red = r;
		green = g;
		blue = b;
		alpha = a;
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
            originalAlpha = alpha;
        }
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
	void setRGBColour(float r, float g, float b, float a) {
	    setRGBColour(r, g, b, a, false);
	}
	
	/**
	 * Sets the colour of the model based on the hsv values h, s, v and alpha a.
	 * 
	 * @param h hue.
	 * @param s saturation.
	 * @param v value.
	 * @param a alpha.
	 * @param reset Pass <code>true</code> to also reset the original value
	 */
	void setHSVColour(float h, float s, float v, float a, boolean reset)
	{
		float[] rgb = HSVtoRGB(h,s,v);
		red = rgb[0];
		green = rgb[1];
		blue = rgb[2];
		alpha = a;
        if (reset) {
            originalRed = red;
            originalGreen = green;
            originalBlue = blue;
            originalAlpha = alpha;
        }
	}
	
	/**
     * Sets the colour of the model based on the hsv values h, s, v and alpha a.
     * 
     * @param h hue.
     * @param s saturation.
     * @param v value.
     * @param a alpha.
     */
	void setHSVColour(float h, float s, float v, float a) {
	    setHSVColour(h, s, v, a, false);
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
		
		if (Math.abs(red-max) < UIUtilities.EPSILON) 
			h = (green-blue)/delta;		
		else if (Math.abs(green-max) < UIUtilities.EPSILON)
			h = 2.0f + (blue-red)/delta;	
		else if (Math.abs(blue-max) < UIUtilities.EPSILON)
			h = 4.0f+(red-green)/delta;
		
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
	 * @param h hue, value in [0..1]..
	 * @param s saturation, value in [0..1].
	 * @param v value, value in [0..1].
	 * @return array of floats representing RGB.
	 */
	float [] HSVtoRGB(float h, float s, float v)
	{
    	int i;
    	float f, p, q, t;
    	float rgb [] = new float[3];
    	float r,g,b;
    	
    	if (s == 0) 
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
		lut = originalLut;
		revInt = originalRevInt;
	}
	
	/**
	 * Returns <code>true</code> if the passed color is the orginal color,
	 * <code>false</code>.
	 * 
	 * @param color The color to test.
	 * @return See above.
	 */
	boolean isOriginalColor(Color color)
	{
		if (color == null) return false;
		Color c = getOriginalColor();
		return (c.getRed() == color.getRed() && c.getGreen() == color.getGreen() 
				&& c.getBlue() == color.getBlue() &&
                c.getAlpha() == color.getAlpha());
	}
	
    /**
     * Set the lookup table
     * 
     * @param lut
     *            The lookup table
     *  @param reset 
     *            Pass <code>true</code> to also reset the original value
     */
    void setLUT(String lut, boolean reset) {
        this.lut = lut;
        if (reset)
            originalLut = lut;
    }

    /**
     * Set the lookup table
     * 
     * @param lut
     *            The lookup table
     */
    void setLUT(String lut) {
        setLUT(lut, false);
    }
    
    /**
     * Get the lookup table
     * 
     * @return See above
     */
    String getLUT() {
        return lut;
    }

    /**
     * Get the original lookup table
     * 
     * @return See above
     */
    String getOriginalLUT() {
        return originalLut;
    }

    /**
     * Set the reverse intensity flag
     * 
     * @param revInt
     *            The reverse intensity flag
     * @param reset
     *            Pass <code>true</code> to also reset the original value
     */
    void setReverseIntensity(boolean revInt, boolean reset) {
        this.revInt = revInt;
        if (reset)
            originalRevInt = revInt;
    }
    

    /**
     * Set the reverse intensity flag
     * 
     * @param revInt
     *            The reverse intensity flag
     */
    void setReverseIntensity(boolean revInt) {
        setReverseIntensity(revInt, false);
    }

    /**
     * Get the reverse intensity flag
     * 
     * @return See above
     */
    boolean getReversetIntensity() {
        return revInt;
    }

    /**
     * Get the original reverse intensity flag
     * 
     * @return See above
     */
    boolean getOriginalReversetIntensity() {
        return originalRevInt;
    }
    
    /**
     * Check if the provided lookup table is the same as the original lookup
     * table
     * 
     * @param lut The
     *            lookup table
     * @return <code>true</code> if it is, <code>false</code> if it is not
     */
    boolean isOriginalLut(String lut) {
        // treat null and empty Strings the same
        if (lut == null)
            lut = "";
        if (originalLut == null)
            originalLut = "";
        return lut.equals(originalLut);
    }

    /**
     * Get all available lookup tables
     * @return See above
     */
    Collection<String> getAvailableLookupTables() {
        return availableLUTs;
    }
}



