/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.RGBControl.java
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

//Java imports
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.util.CommonsLangUtils;


//Third-party libraries

//Application-internal dependencies

/** 
 * Control the RGB component.
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

class RGBControl 
	extends JComponent
{
    
    /** Some predefined colors */
    public static Map<String, Color> PREDEFINED_COLORS;
    
    static {
        PREDEFINED_COLORS = new LinkedHashMap<String, Color>();
        PREDEFINED_COLORS.put("Red", Color.red);
        PREDEFINED_COLORS.put("Green", Color.green);
        PREDEFINED_COLORS.put("Blue", Color.blue);
        PREDEFINED_COLORS.put("White", Color.white);
        PREDEFINED_COLORS.put("Black", Color.black);
        PREDEFINED_COLORS.put("Gray", Color.gray);
        PREDEFINED_COLORS.put("Orange", Color.orange);
        PREDEFINED_COLORS.put("Yellow", Color.yellow);
        PREDEFINED_COLORS.put("Indigo", new Color(75, 0, 130));
        PREDEFINED_COLORS.put("Violet", new Color(238, 130, 238));
        PREDEFINED_COLORS.put("Cyan", Color.cyan);
        PREDEFINED_COLORS.put("Magenta", Color.magenta);
    }
    
	/**
	 * RGB Model, this holds the current value of the R, G, B and alpha 
	 * channels.
	 */
	private RGBModel				model;
	
	/** List of objects listening for changes to the model. */
	private List<ChangeListener>	listeners;
	
	/**
	 * Creates a reference to the model m, and instantiates 
	 * the listener array.
	 * 
	 * @param m Refereence to the model. Mustn't be <code>null</code>.
	 */
	RGBControl(RGBModel m)
	{
        if (m == null) throw new NullPointerException("No model.");
		model = m;
		listeners = new ArrayList<ChangeListener>();
	}
	
    /**
     * @return <code>true</code> if this control neither represents a Lookup
     *         Table nor one of the {@link RGBControl#PREDEFINED_COLORS}
     */
    boolean isCustomColor() {
        if (CommonsLangUtils.isNotEmpty(getLUT()))
            return false;
        Color c = getColour();
        
        for (Color c2 : PREDEFINED_COLORS.values()) {
            if (c2.getRed() == c.getRed()
                    && c2.getBlue() == c.getBlue()
                    && c2.getGreen() == c.getGreen())
                return false;
        }
        return true;
    }
	
	/**
	 * Sets the Red channel to the parameter [0..1] and fire change event to 
	 * notify listeners. 
	 * 
	 * @param r The value to set.
	 */
	void setRed(float r)
	{
		model.setRed(r);
		fireChangeEvent();
	}
	
	/**
	 * Sets the Blue channel to the parameter [0..1] and fire change event to 
	 * notify listeners. 
	 * 
	 * @param b The value to set.
	 */
	void setBlue(float b)
	{
		model.setBlue(b);
		fireChangeEvent();
	}
	
	/**
	 * Sets the Green channel to the parameter [0..1] and fire change event to 
	 * notify listeners. 
	 * 
	 * @param g The value to set.
	 */
	void setGreen(float g)
	{
		model.setGreen(g);
		fireChangeEvent();
	}
	
	/**
	 * Sets the Alpha channel to the parameter [0..1] and fire change event to 
	 * notify listeners. 
     * 
	 * @param a The value to set.
	 */
	void setAlpha(float a)
	{
		model.setAlpha(a);
		fireChangeEvent();
	}
	
	/**
	 * Sets the Hue component to the parameter [0..1] and fire change event to 
	 * notify listeners. 
	 * 
	 * @param h The value to set.
	 */
	void setHue(float h)
	{
		model.setHue(h);
		fireChangeEvent();
	}

	/**
	 * Sets the Saturation component to the parameter [0..1] and fire change 
	 * event to  notify listeners. 
	 * 
	 * @param s The value to set.
	 */
	void setSaturation(float s)
	{
		model.setSaturation(s);
		fireChangeEvent();
	}
	
	/**
	 * Sets the value component to the parameter [0..1] and fire change 
	 * event to  notify listeners. 
	 * 
	 * @param v The value to set.
	 */
	void setValue(float v)
	{
		model.setValue(v);
		fireChangeEvent();
	}
	
	/**
	 * Sets the model colour to c fire change event to notify listeners. 
	 * 
	 * @param c The value to set.
	 */
	void setColour(Color c)
	{
		model.setColour(c);
		fireChangeEvent();
	}
	
	/**
	 * Sets the model colour to h, s, v, a and fires change event to 
	 * notify listeners. 
	 * 
	 * @param h hue. 
	 * @param s saturation. 
	 * @param v value. 
	 * @param a alpha. 
	 */	
	void setHSVColour(float h, float s, float v, float a)
	{
		model.setHSVColour(h, s, v, a);
		fireChangeEvent();
	}

	/**
	 * Sets the model colour to r, g, b and a floats (0..1)
	 * 
	 * @param r red.
	 * @param g green.
	 * @param b blue.
	 * @param a alpha.
	 */
	void setRGBColour(float r, float g, float b, float a)
	{
		model.setRGBColour(r,g,b,a);
		fireChangeEvent();
	}
	
	/**  Allows the user to revert to their original colour selection. */
	void revert()
	{
		model.revert();
		fireChangeEvent();
	}
	
	/**
	 * Gets Colour from model. 
	 * 
	 * @return RGB Colour.
	 */
	Color getColour() { return model.getColour(); }
	
	/**
	 * Returns <code>true</code> if the color set is the original color,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOriginalColour() { return model.isOriginalColor(getColour()); }
	
	/**
	 * Gets red component from Model.
	 * 
	 * @return red component as float.
	 */
	float getRed() { return model.getRed(); }
	
	/**
	 * Gets green component from Model.
	 * 
	 * @return green component as float.
	 */
	float getGreen() { return model.getGreen(); }
	
	/**
	 * Gets Blue component from Model.
	 * 
	 * @return Blue component as float.
	 */
	float getBlue() { return model.getBlue(); }
	
	/**
	 * Gets alpha component from Model.
	 * 
	 * @return alpha component as float.
	 */
	float getAlpha() { return model.getAlpha(); }
	
	/**
	 * Gets hue component represented by RGB Colour model, this requires a 
	 * conversion of the RGB components to HSV. 
	 * 
	 * @return See above.
	 */
	float getHue() { return model.getHue();}
	
	/**
	 * Gets saturation component represented by RGB Colour model, this requires
     * a conversion of the RGB components to HSV. 
	 * 
	 * @return See above.
	 */
	float getSaturation() { return model.getSaturation(); }
	
	/**
	 * Gets value component represented by RGB Colour model, this requires a 
	 * conversion of the RGB components to HSV. 
	 * 
	 * @return See above.
	 */
	float getValue() { return model.getValue(); }
	
    /**
     * Set the lookup table
     * 
     * @param lut
     *            The lookup table
     */
    void setLUT(String lut) {
        model.setLUT(lut);
        fireChangeEvent(PaintPotUI.LUT_PROPERTY);
    }

    /**
     * Get the lookup table
     * 
     * @return See above
     */
    String getLUT() {
        return model.getLUT();
    }

    /**
     * Set the reverse intensity
     * 
     * @param revInt
     *            The reverse intensity
     */
    void setReverseIntensity(boolean revInt) {
        model.setReverseIntensity(revInt);
        fireChangeEvent(null);
    }

    /**
     * Get the reverse intensity
     * 
     * @return See above
     */
    boolean getReverseIntensity() {
        return model.getReverseIntensity();
    }

    
    /**
     * Check if the lookup table has been changed
     * 
     * @return <code>true</code> if it has not, <code>false</code> if it has
     */
    boolean isOriginalLut() {
        return model.isOriginalLut(getLUT());
    }
    
    /**
     * Check if the reverse intensity has been changed
     * 
     * @return <code>true</code> if it has not, <code>false</code> if it has
     */
    boolean isOriginalRevInt() {
        return model.getOriginalReverseIntensity() == getReverseIntensity();
    }

    /**
     * Get all available lookup tables
     * 
     * @return See above
     */
    Collection<String> getAvailableLookupTables() {
        return model.getAvailableLookupTables();
    }
	
	/**
	 * Adds listener e to the list of listeners.
	 * 
	 * @param e The listener to add.
	 */
	void addListener(ChangeListener e) { listeners.add(e); }
	
    /** Fires Changed event to all listeners stating the model has changed. */
    void fireChangeEvent() {
        fireChangeEvent(PaintPotUI.COLOUR_CHANGED_PROPERTY);
    }

    /**
     * Fires Changed event to all listeners stating the model has changed.
     * 
     * @param property
     *            An optional property to fire (see {@link
     *            PaintPotUI#LUT_PROPERTY} ,
     *            {@link PaintPotUI#COLOUR_CHANGED_PROPERTY}
     */
    void fireChangeEvent(String property) {
        ChangeListener e;
        for (int i = 0; i < listeners.size(); i++) {
            e = listeners.get(i);
            e.stateChanged(new ColourChangedEvent(this));
        }

        if (PaintPotUI.LUT_PROPERTY.equals(property))
            firePropertyChange(PaintPotUI.LUT_PROPERTY, null, model.getLUT());

        else if (PaintPotUI.COLOUR_CHANGED_PROPERTY.equals(property))
            firePropertyChange(PaintPotUI.COLOUR_CHANGED_PROPERTY, null,
                    model.getColour());
    }
    
}
