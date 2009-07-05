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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.RString;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.Filter;
import omero.model.Instrument;
import omero.model.LightSource;
import omero.model.Microscope;
import omero.model.MicroscopeType;
import omero.model.Objective;

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
	
	/** The collection of dichroics. */
	private List<DichroicData> dichroics;
	
	/** The collection of detectors. */
	private List<DetectorData> detectors;
	
	/** Initializes the instrument. */
	private void initialize()
	{
		objectives = new ArrayList<ObjectiveData>();
		lightSources = new ArrayList<LightSourceData>(); 
		filters = new ArrayList<FilterData>(); 
		dichroics = new ArrayList<DichroicData>(); 
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
	 * Sets the collection of detectors.
	 * 
	 * @param list The value to set.
	 */
	public void setDetectors(List<Detector> list)
	{
		if (list == null || list.size() == 0) return;
		if (detectors == null) detectors = new ArrayList<DetectorData>();
		Iterator<Detector> i = list.iterator();
		while (i.hasNext()) {
			detectors.add(new DetectorData(i.next()));
		}
	}
	
	/**
	 * Sets the collection of objectives.
	 * 
	 * @param list The value to set.
	 */
	public void setObjectives(List<Objective> list)
	{
		if (list == null || list.size() == 0) return;
		if (objectives == null) objectives = new ArrayList<ObjectiveData>();
		Iterator<Objective> i = list.iterator();
		while (i.hasNext()) {
			objectives.add(new ObjectiveData(i.next()));
		}
	}
	
	/**
	 * Sets the collection of filters.
	 * 
	 * @param list The value to set.
	 */
	public void setFilters(List<Filter> list)
	{
		if (list == null || list.size() == 0) return;
		if (filters == null) filters = new ArrayList<FilterData>();
		Iterator<Filter> i = list.iterator();
		while (i.hasNext()) {
			filters.add(new FilterData(i.next()));
		}
	}
	
	/**
	 * Sets the collection of lights.
	 * 
	 * @param list The value to set.
	 */
	public void setLightSources(List<LightSource> list)
	{
		if (list == null || list.size() == 0) return;
		if (lightSources == null) 
			lightSources = new ArrayList<LightSourceData>();
		Iterator<LightSource> i = list.iterator();
		while (i.hasNext()) {
			lightSources.add(new LightSourceData(i.next()));
		}
	}
	
	/**
	 * Sets the collection of dichroics.
	 * 
	 * @param list The value to set.
	 */
	public void setDichroics(List<Dichroic> list)
	{
		if (list == null || list.size() == 0) return;
		if (dichroics == null) 
			dichroics = new ArrayList<DichroicData>();
		Iterator<Dichroic> i = list.iterator();
		while (i.hasNext()) {
			dichroics.add(new DichroicData(i.next()));
		}
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
	
	/**
	 * Returns the collection of detectors.
	 * 
	 * @return See above.
	 */
	public List<DetectorData> getDetectors() { return detectors; }
	
	/**
	 * Returns the collection of dichroics.
	 * 
	 * @return See above.
	 */
	public List<DichroicData> getDichroics() { return dichroics; }
	
}
