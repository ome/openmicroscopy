/*
 * pojos.InstrumentData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Instrument;
import omero.model.Microscope;
import omero.model.MicroscopeType;

/**
 * Hosts the instrument used to capture an image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class InstrumentData 
	extends DataObject
{

	/** The collection of objectives. */
	private List<ObjectiveData> objectives;
	
	/** The collection of light sources. */
	private List<LightSourceData> lightSources;
	
	/** The collection of filters. */
	private List<FilterData> filters;
	
	/** Initializes the instrument. */
	private void initialize()
	{
		objectives = new ArrayList<ObjectiveData>();
		lightSources = new ArrayList<LightSourceData>(); 
		filters = new ArrayList<FilterData>(); 
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param instrument The instrument used to capture an image.
	 */
	public InstrumentData(Instrument instrument)
	{
		 if (instrument == null)
	            throw new IllegalArgumentException("Object cannot null.");
	     setValue(instrument);
	}

	/**
	 * Returns the type of microscope.
	 * 
	 * @return See above.
	 */
	public String getMicroscopeType()
	{
		Microscope m = ((Instrument) asIObject()).getMicroscope();
		if (m == null) return null;
		MicroscopeType v = m.getType();
		if (v == null) return "";
		return v.getValue().getValue();
	}
	
	/**
	 * Returns the model of the microscope.
	 * 
	 * @return See above.
	 */
	public String getMicroscopeModel()
	{
		Microscope m = ((Instrument) asIObject()).getMicroscope();
		if (m == null) return null;
		RString v = m.getModel();
		if (v == null) return "";
		return v.getValue();
	}
	
	/**
	 * Returns the serial number of the microscope.
	 * 
	 * @return See above.
	 */
	public String getMicroscopeSerialNumber()
	{
		Microscope m = ((Instrument) asIObject()).getMicroscope();
		if (m == null) return null;
		RString v = m.getSerialNumber();
		if (v == null) return "";
		return v.getValue();
	}
	
	/**
	 * Returns the lot number of the microscope.
	 * 
	 * @return See above.
	 */
	public String getMicroscopeLotNumber()
	{
		Microscope m = ((Instrument) asIObject()).getMicroscope();
		if (m == null) return null;
		RString v = m.getSerialNumber();
		if (v == null) return "";
		return v.getValue();
	}
	
	/**
	 * Returns the model of the microscope.
	 * 
	 * @return See above.
	 */
	public String getMicroscopeManufacturer()
	{
		Microscope m = ((Instrument) asIObject()).getMicroscope();
		if (m == null) return null;
		RString v = m.getSerialNumber();
		if (v == null) return "";
		return v.getValue();
	}
	
	/**
	 * Returns the collection of objectives.
	 * 
	 * @return See above.
	 */
	public List<ObjectiveData> getObjectives() { return objectives; }
	
	/**
	 * Returns the collection of filters.
	 * 
	 * @return See above.
	 */
	public List<FilterData> getFilters() { return filters; }
	
	/**
	 * Returns the collection of light sources.
	 * 
	 * @return See above.
	 */
	public List<LightSourceData> getLightSources() { return lightSources; }
	
}
