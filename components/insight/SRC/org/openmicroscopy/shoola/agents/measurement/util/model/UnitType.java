/*
 * org.openmicroscopy.shoola.agents.measurement.util.model.UnitType 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.model;

//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.model.enums.UnitsLength;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public enum UnitType
{
	
	// TODO: This class will be needless once omero model units 
	//    will have symbols attached
	
	DEGREE("°"),
	PIXEL("px"),
	MICRON("µm"),
	METER("m"),
	CENTIMETER("cm"),
	NANOMETER("nm"),
	PICOMETER("pm"),
	MILLIMETER("mm"),
	ANGSTROM("Å");
    
	private String symbol;
	
	private UnitType(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String toString() {
		return symbol;
	}
	
	public static UnitType getUnitType(UnitsLength unit) {
		if(unit.equals(UnitsLength.PIXEL)) {
    		return PIXEL;
    	}
    	if(unit.equals(UnitsLength.M)) {
    		return METER;
    	}
    	if(unit.equals(UnitsLength.CM)) {
    		return CENTIMETER;
    	}
    	if(unit.equals(UnitsLength.MM)) {
    		return MILLIMETER;
    	}
    	if(unit.equals(UnitsLength.MICROM)) {
    		return MICRON;
    	}
    	if(unit.equals(UnitsLength.NM)) {
    		return NANOMETER;
    	}
    	if(unit.equals(UnitsLength.ANGSTROM)) {
    		return ANGSTROM;
    	}
    	if(unit.equals(UnitsLength.PM)) {
    		return PICOMETER;
    	}
    	return null;
	}
}