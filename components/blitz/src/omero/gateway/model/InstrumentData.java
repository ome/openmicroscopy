/*
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
package omero.gateway.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.RString;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.Instrument;
import omero.model.LightSource;
import omero.model.Microscope;
import omero.model.MicroscopeType;
import omero.model.OTF;
import omero.model.Objective;

/**
 * Hosts the instrument used to capture an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
		RString v = m.getLotNumber();
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
		RString v = m.getManufacturer();
		if (v == null) return "";
		return v.getValue();
	}
	
	/**
	 * Returns the collection of OTFs.
	 * 
	 * @return See above.
	 */
	public List<OTFData> getOTF()
	{ 
		if (otfs != null) return otfs;
		otfs = new ArrayList<OTFData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfOtf() > 0) {
			List<OTF> list = instrument.copyOtf();
			Iterator<OTF> i = list.iterator();
			while (i.hasNext()) {
				otfs.add(new OTFData(i.next()));
			}
		}
		return otfs; 
	}
	
	/**
	 * Returns the collection of objectives.
	 * 
	 * @return See above.
	 */
	public List<ObjectiveData> getObjectives()
	{ 
		if (objectives != null) return objectives;
		objectives = new ArrayList<ObjectiveData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfObjective() > 0) {
			List<Objective> list = instrument.copyObjective();
			Iterator<Objective> i = list.iterator();
			while (i.hasNext()) {
				objectives.add(new ObjectiveData(i.next()));
			}
		}
		return objectives; 
	}
	
	/**
	 * Returns the collection of filters.
	 * 
	 * @return See above.
	 */
	public List<FilterData> getFilters()
	{ 
		if (filters != null) return filters;
		filters = new ArrayList<FilterData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfFilter() > 0) {
			List<Filter> list = instrument.copyFilter();
			Iterator<Filter> i = list.iterator();
			while (i.hasNext()) {
				filters.add(new FilterData(i.next()));
			}
		}
		return filters; 
	}
	
	/**
	 * Returns the collection of filter sets.
	 * 
	 * @return See above.
	 */
	public List<FilterSetData> getFilterSets()
	{ 
		if (filterSets != null) return filterSets;
		filterSets = new ArrayList<FilterSetData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfFilterSet() > 0) {
			List<FilterSet> list = instrument.copyFilterSet();
			Iterator<FilterSet> i = list.iterator();
			while (i.hasNext()) {
				filterSets.add(new FilterSetData(i.next()));
			}
		}
		return filterSets; 
	}
	
	/**
	 * Returns the collection of light sources.
	 * 
	 * @return See above.
	 */
	public List<LightSourceData> getLightSources()
	{ 
		if (lightSources != null) return lightSources;
		lightSources = new ArrayList<LightSourceData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfLightSource() > 0) {
			List<LightSource> list = instrument.copyLightSource();
			Iterator<LightSource> i = list.iterator();
			while (i.hasNext()) {
				lightSources.add(new LightSourceData(i.next()));
			}
		}
		return lightSources; 
	}
	
	/**
	 * Returns the collection of detectors.
	 * 
	 * @return See above.
	 */
	public List<DetectorData> getDetectors()
	{ 
		if (detectors != null) return detectors;
		detectors = new ArrayList<DetectorData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfDetector() > 0) {
			List<Detector> list = instrument.copyDetector();
			Iterator<Detector> i = list.iterator();
			while (i.hasNext()) {
				detectors.add(new DetectorData(i.next()));
			}
		}
		return detectors; 
	}
	
	/**
	 * Returns the collection of dichroics.
	 * 
	 * @return See above.
	 */
	public List<DichroicData> getDichroics()
	{ 
		if (dichroics != null) return dichroics;
		dichroics = new ArrayList<DichroicData>(); 
		Instrument instrument = (Instrument) asIObject();
		if (instrument.sizeOfDichroic() > 0) {
			List<Dichroic> list = instrument.copyDichroic();
			Iterator<Dichroic> i = list.iterator();
			while (i.hasNext()) {
				dichroics.add(new DichroicData(i.next()));
			}
		}
		return dichroics; 
	}
	
}
