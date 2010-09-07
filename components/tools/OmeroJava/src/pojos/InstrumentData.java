/*
 * pojos.InstrumentData
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
import omero.model.FilterSet;
import omero.model.Instrument;
import omero.model.IObject;
import omero.model.LightSource;
import omero.model.Microscope;
import omero.model.MicroscopeType;
import omero.model.Objective;

/**
 * Hosts the instrument used to capture an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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
	
	/** The collection of filter Sets. */
	private List<FilterSetData> filterSets;
	
	/** The collection of dichroics. */
	private List<DichroicData> dichroics;
	
	/** The collection of detectors. */
	private List<DetectorData> detectors;
	
	/** The collection of detectors. */
	private List<OTFData> otfs;
	
	/** Initializes the components. */
	private void initialize()
	{
		objectives = new ArrayList<ObjectiveData>();
		lightSources = new ArrayList<LightSourceData>(); 
		filters = new ArrayList<FilterData>(); 
		dichroics = new ArrayList<DichroicData>(); 
		detectors = new ArrayList<DetectorData>(); 
		filterSets = new ArrayList<FilterSetData>(); 
		otfs = new ArrayList<OTFData>(); 
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
	     initialize();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param components The instrument and its components.
	 */
	public InstrumentData(List<IObject> components)
	{
		if (components == null || components.size() < 1)
			throw new IllegalArgumentException("No components specified.");
		initialize();
		Iterator<IObject> i = components.iterator();
		IObject obj;
		boolean instrument = false;
		while (i.hasNext()) {
			obj = (IObject) i.next();
			if (obj instanceof Instrument) {
				setValue(obj);
				instrument = true;
			} else if (obj instanceof Detector)
				detectors.add(new DetectorData((Detector) obj));
			else if (obj instanceof Objective)
				objectives.add(new ObjectiveData((Objective) obj));
			else if (obj instanceof Filter)
				filters.add(new FilterData((Filter) obj));
			else if (obj instanceof LightSource)
				lightSources.add(new LightSourceData((LightSource) obj));
			else if (obj instanceof Dichroic)
				dichroics.add(new DichroicData((Dichroic) obj));
			else if (obj instanceof FilterSet)
				filterSets.add(new FilterSetData((FilterSet) obj));
		}
		if (!instrument)
			throw new IllegalArgumentException("No instrument specified.");
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
	 * Returns the collection of OTFs.
	 * 
	 * @return See above.
	 */
	public List<OTFData> getOTF() { return otfs; }
	
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
	 * Returns the collection of filter sets.
	 * 
	 * @return See above.
	 */
	public List<FilterSetData> getFilterSets() { return filterSets; }
	
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
