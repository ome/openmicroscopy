/*
 * pojos.ChannelAcquisitionData 
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
package pojos;



//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.RFloat;
import omero.RInt;
import omero.RString;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.OTF;

/** 
 * Object hosting the acquisition related to a logical channel.
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
public class ChannelAcquisitionData 
	extends DataObject
{

	/** The settings of the detector. */
	private DetectorSettings 	detectorSettings;
	
	/** The settings of the light source. */
	private LightSettings 		lightSettings;
	
	/** The filter used. */
	private FilterSet			filterSet;
	
	/** The filter used for the emission wavelength. */
	private Filter				secondaryEmFilter;
	
	/** The filter used for the excitation wavelength. */
	private Filter				secondaryExFilter;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channel The image the acquisition data is related to. 
	 * 				Mustn't be <code>null</code>.
	 */
	public ChannelAcquisitionData(LogicalChannel channel)
	{
        if (channel == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(channel);
        detectorSettings = channel.getDetectorSettings();
        lightSettings = channel.getLightSourceSettings();
        filterSet = channel.getFilterSet();
        secondaryEmFilter = channel.getSecondaryEmissionFilter();
        secondaryExFilter = channel.getSecondaryExcitationFilter();
	}
	
	/**
	 * Returns the offset set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsOffset()
	{
		if (detectorSettings == null) return 0f;
		RFloat value = detectorSettings.getOffsetValue();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the gain set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsGain()
	{
		if (detectorSettings == null) return 0f;
		RFloat value = detectorSettings.getGain();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the voltage set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsVoltage()
	{
		if (detectorSettings == null) return 0f;
		RFloat value = detectorSettings.getVoltage();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the Read out rate set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsReadOutRate()
	{
		if (detectorSettings == null) return 0f;
		RFloat value = detectorSettings.getReadOutRate();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the voltage of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorVoltage()
	{
		if (detectorSettings == null) return 0f;
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return 0f;
		RFloat value = detector.getVoltage();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the amplification gain of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorAmplificationGain()
	{
		if (detectorSettings == null) return 0f;
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return 0f;
		RFloat value = detector.getAmplificationGain();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the gain of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorGain()
	{
		if (detectorSettings == null) return 0f;
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return 0f;
		RFloat value = detector.getGain();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorOffset()
	{
		if (detectorSettings == null) return 0f;
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return 0f;
		RFloat value = detector.getOffsetValue();
		if (value == null) return 0f;
		return value.getValue();
	}

	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorZoom()
	{
		if (detectorSettings == null) return 0f;
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return 0f;
		RFloat value = detector.getZoom();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorManufacturer()
	{
		if (detectorSettings == null) return "";
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return "";
		RString value = detector.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorModel()
	{
		if (detectorSettings == null) return "";
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return "";
		RString value = detector.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorSerialNumber()
	{
		if (detectorSettings == null) return "";
		Detector detector = detectorSettings.getDetector();
		if (detector == null) return "";
		RString value = detector.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the attenuation of the ligth source, percent value 
	 * between 0 and 1.
	 * 
	 * @return See above.
	 */
	public float getLigthSettingsAttenuation()
	{
		if (lightSettings == null) return 0f;
		RFloat value = lightSettings.getAttenuation();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the wavelength of the ligth source.
	 * 
	 * @return See above.
	 */
	public int getLigthSettingsWavelength()
	{
		if (lightSettings == null) return 0;
		RInt value = lightSettings.getWavelength();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the secondary excitation filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryExFilterManufacturer()
	{
		if (secondaryExFilter == null) return "";
		RString value = secondaryExFilter.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the secondary excitation filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryExFilterModel()
	{
		if (secondaryExFilter == null) return "";
		RString value = secondaryExFilter.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the secondary excitation filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryExFilterLotNumber()
	{
		if (secondaryExFilter == null) return "";
		RString value = secondaryEmFilter.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the type of the secondary excitation filter. One of a predefined
	 * list.
	 * 
	 * @return See above.
	 */
	public String getSecondaryExFilterType()
	{
		if (secondaryExFilter == null) return "";
		RString value = secondaryExFilter.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the type of the secondary excitation filter. One of a predefined
	 * list.
	 * 
	 * @return See above.
	 */
	public String getSecondaryExFilterFilterWheel()
	{
		if (secondaryExFilter == null) return "";
		RString value = secondaryExFilter.getFilterWheel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the secondary emission filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryEmFilterManufacturer()
	{
		if (secondaryEmFilter == null) return "";
		RString value = secondaryEmFilter.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the secondary emission filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryEmFilterModel()
	{
		if (secondaryEmFilter == null) return "";
		RString value = secondaryEmFilter.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the secondary emission filter.
	 * 
	 * @return See above.
	 */
	public String getSecondaryEmFilterLotNumber()
	{
		if (secondaryEmFilter == null) return "";
		RString value = secondaryEmFilter.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the type of the secondary emission filter. One of a predefined
	 * list.
	 * 
	 * @return See above.
	 */
	public String getSecondaryEmFilterType()
	{
		if (secondaryEmFilter == null) return "";
		RString value = secondaryEmFilter.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the type of the secondary emission filter. One of a predefined
	 * list.
	 * 
	 * @return See above.
	 */
	public String getSecondaryEmFilterFilterWheel()
	{
		if (secondaryEmFilter == null) return "";
		RString value = secondaryEmFilter.getFilterWheel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the light source.
	 * 
	 * @return See above.
	 */
	public String getLightSourceManufacturer()
	{
		if (lightSettings == null) return "";
		LightSource light = lightSettings.getLightSource();
		if (light == null) return "";
		RString value = light.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the light source.
	 * 
	 * @return See above.
	 */
	public String getLightSourceModel()
	{
		if (lightSettings == null) return "";
		LightSource light = lightSettings.getLightSource();
		if (light == null) return "";
		RString value = light.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the light source.
	 * 
	 * @return See above.
	 */
	public String getLightSourceSerialNumber()
	{
		if (lightSettings == null) return "";
		LightSource light = lightSettings.getLightSource();
		if (light == null) return "";
		RString value = light.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the power of the light source.
	 * 
	 * @return See above.
	 */
	public float getLightSourcePower()
	{
		if (lightSettings == null) return 0f;
		LightSource light = lightSettings.getLightSource();
		if (light == null) return 0f;
		RFloat value = light.getPower();
		if (value == null) return 0f;
		return value.getValue();
	}

}
