/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.AttributeUnits 
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
package org.openmicroscopy.shoola.agents.measurement.util.ui;

import java.util.HashMap;
import java.util.Map;

import omero.model.enums.UnitsLength;

import org.openmicroscopy.shoola.agents.measurement.util.model.UnitType;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.UnitsObject;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class AttributeUnits
{	
	private static HashMap<String, Map<UnitType, String>>	units;
	
	static
	{
		units = new HashMap<String, Map<UnitType, String>>();
		Map <UnitType, String> unitMap;
		unitMap = new HashMap<UnitType, String>();
		unitMap.put(UnitType.MICRONS, UnitsObject.MICRONS);
		unitMap.put(UnitType.PIXELS, UIUtilities.PIXELS_SYMBOL);
		unitMap.put(UnitType.ANGSTROMS, UnitsObject.ANGSTROM);
		unitMap.put(UnitType.NANOMETERS, UnitsObject.NANOMETER);
		unitMap.put(UnitType.PICOMETERS, UnitsObject.PICOMETER);
		unitMap.put(UnitType.MILLIMETERS, UnitsObject.MILLIMETER);
		unitMap.put(UnitType.CENTIMETERS, UnitsObject.CENTIMETER);
		unitMap.put(UnitType.METERS, UnitsObject.METER);
		
		units.put(AnnotationKeys.HEIGHT.getKey(), unitMap);
		units.put(AnnotationKeys.WIDTH.getKey(), unitMap);
		units.put(AnnotationKeys.LENGTH.getKey(), unitMap);
		units.put(AnnotationKeys.CENTREX.getKey(), unitMap);
		units.put(AnnotationKeys.CENTREY.getKey(), unitMap);
		units.put(AnnotationKeys.ENDPOINTX.getKey(), unitMap);
		units.put(AnnotationKeys.ENDPOINTY.getKey(), unitMap);
		units.put(AnnotationKeys.STARTPOINTX.getKey(), unitMap);
		units.put(AnnotationKeys.STARTPOINTY.getKey(), unitMap);
		units.put(AnnotationKeys.PERIMETER.getKey(), unitMap);
		units.put(AnnotationKeys.POINTARRAYX.getKey(), unitMap);
		units.put(AnnotationKeys.POINTARRAYY.getKey(), unitMap);
		
		unitMap = new HashMap<UnitType, String>();
		unitMap.put(UnitType.PIXELS, UnitsObject.DEGREES);
		unitMap.put(UnitType.MICRONS, UnitsObject.DEGREES);
		units.put(AnnotationKeys.ANGLE.getKey(), unitMap);
		unitMap = new HashMap<UnitType, String>();
		unitMap.put(UnitType.PIXELS, 
				UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.MICRONS, 
				UnitsObject.MICRONS+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.ANGSTROMS, 
				UnitsObject.ANGSTROM+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.PICOMETERS, 
				UnitsObject.PICOMETER+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.NANOMETERS, 
				UnitsObject.NANOMETER+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.MILLIMETERS, 
				UnitsObject.MILLIMETER+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.CENTIMETERS, 
				UnitsObject.CENTIMETER+UIUtilities.SQUARED_SYMBOL);
		unitMap.put(UnitType.METERS, 
				UnitsObject.METER+UIUtilities.SQUARED_SYMBOL);
		
		units.put(AnnotationKeys.AREA.getKey(), unitMap);
	}
	
	public static String getUnits(String key, MeasurementUnits type)
	{
		UnitType unitType;
		if (!units.containsKey(key)) return "";
		Map<UnitType, String> unitMap = units.get(key);
		if (!type.getUnit().equals(UnitsLength.PIXEL)) {
			UnitsObject uo = UIUtilities.transformSize(type.getPixelSizeX().getValue());
			String v = uo.getUnits();
			unitType = UnitType.MICRONS;
			if (UnitsObject.ANGSTROM.equals(v))
				unitType = UnitType.ANGSTROMS;
			else if (UnitsObject.MILLIMETER.equals(v))
				unitType = UnitType.MILLIMETERS;
			else if (UnitsObject.CENTIMETER.equals(v))
				unitType = UnitType.CENTIMETERS;
			else if (UnitsObject.METER.equals(v))
				unitType = UnitType.METERS;
			else if (UnitsObject.NANOMETER.equals(v))
				unitType = UnitType.NANOMETERS;
			else if (UnitsObject.PICOMETER.equals(v))
				unitType = UnitType.PICOMETERS;
		} else unitType = UnitType.PIXELS;
		if (!unitMap.containsKey(unitType))
			return "";
		return unitMap.get(unitType);
	}
}

