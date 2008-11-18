/*
 * org.openmicroscopy.shoola.env.data.model.EnumerationObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.AcquisitionMode;
import omero.model.Binning;
import omero.model.Coating;
import omero.model.ContrastMethod;
import omero.model.DetectorType;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Immersion;
import omero.model.Medium;
import omero.model.PhotometricInterpretation;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EnumerationObject
{

	/** The objet hosted by this component. */
	private IObject object;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param object The 
	 */
	public EnumerationObject(IObject object)
	{
		if (object == null)
			throw new IllegalArgumentException();
		this.object = object;
	}
	
	/**
	 * Returns the object hosted by this component.
	 * 
	 * @return See above.
	 */
	public IObject getObject() { return object; }
	
	/**
	 * Returns the value of the enumeration. 
	 * 
	 * @return See above.
	 */
	public String getValue()
	{
		RString value = null;
		if (object instanceof Immersion)
			value = ((Immersion) object).getValue();
		else if (object instanceof Coating)
			value = ((Coating) object).getValue();
		else if (object instanceof Medium)
			value = ((Medium) object).getValue();
		else if (object instanceof DetectorType)
			value = ((DetectorType) object).getValue();
		else if (object instanceof Binning)
			value = ((Binning) object).getValue();
		else if (object instanceof ContrastMethod)
			value = ((ContrastMethod) object).getValue();
		else if (object instanceof Illumination)
			value = ((Illumination) object).getValue();
		else if (object instanceof PhotometricInterpretation)
			value = ((PhotometricInterpretation) object).getValue();
		else if (object instanceof AcquisitionMode)
			value = ((AcquisitionMode) object).getValue();
		if (value != null) return value.getValue();
		return "";
	}
	
	/**
	 * Overridden to return the value of the enumeration object.
	 * @see Object#toString()
	 */
	public String toString() { 
		
		return getValue(); }
	
}
