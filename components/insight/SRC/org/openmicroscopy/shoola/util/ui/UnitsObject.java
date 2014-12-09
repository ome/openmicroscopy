/*
 * org.openmicroscopy.shoola.util.UnitsObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui;

import omero.model.enums.UnitsLength;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class used to display units.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class UnitsObject 
{
	
	// TODO: Remove this class once unit symbols are 
	//       available from the API

	/** Unicode for the degrees symbol. */
	public final static String DEGREES = "°";
	
	/** String to represent the picometer symbol. */
    public static final String PICOMETER = "pm";
    
	/** String to represent the angstrom symbol. */
    public static final String ANGSTROM = "Å";
    
	/** String to represent the nanometer symbol. */
    public static final String NANOMETER = "nm";
    
	/** String to represent the micron symbol. */
    public static final String MICRONS = "µm";
    
    /** String to represent the millimeter symbol. */
    public static final String MILLIMETER = "mm";
    
    /** String to represent the centimeter symbol. */
    public static final String CENTIMETER = "cm";
    
    /** String to represent the meter symbol. */
    public static final String METER = "m";
    
    /** String to represent the meter symbol. */
    public static final String PIXEL = "px";
    
    /** The unit for the corresponding value.*/
    private String units;
    
    /** The value to display.*/
    private double value;
    
    public static String getSymbol(UnitsLength l) {
    	if(l.equals(UnitsLength.PIXEL)) {
    		return UnitsObject.PIXEL;
    	}
    	if(l.equals(UnitsLength.M)) {
    		return UnitsObject.METER;
    	}
    	if(l.equals(UnitsLength.CM)) {
    		return UnitsObject.CENTIMETER;
    	}
    	if(l.equals(UnitsLength.MM)) {
    		return UnitsObject.MILLIMETER;
    	}
    	if(l.equals(UnitsLength.MICROM)) {
    		return UnitsObject.MICRONS;
    	}
    	if(l.equals(UnitsLength.NM)) {
    		return UnitsObject.NANOMETER;
    	}
    	if(l.equals(UnitsLength.ANGSTROM)) {
    		return UnitsObject.ANGSTROM;
    	}
    	if(l.equals(UnitsLength.PM)) {
    		return UnitsObject.PICOMETER;
    	}
    	return "";
    }
    
    /**
     * Stores the units and the value.
     * 
     * @param units The unit to display
     * @param value The value to set.
     */
    public UnitsObject(String units, double value)
    {
    	this.units = units;
    	this.value = value;
    }
    
    /**
     * Returns the unit.
     * 
     * @return See above.
     */
    public String getUnits() { return units; }
    
    /**
     * Returns the value.
     * 
     * @return See above.
     */
    public double getValue() { return value; }
    
}
