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
import omero.RDouble;
import omero.RInt;
import omero.RString;
import omero.model.Binning;
import omero.model.Detector;
import omero.model.DetectorI;
import omero.model.DetectorSettings;
import omero.model.DetectorSettingsI;
import omero.model.DetectorType;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.LightSettings;
import omero.model.LightSettingsI;
import omero.model.LightSource;
import omero.model.LogicalChannel;

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
	private FilterData			secondaryEmFilter;
	
	/** The filter used for the excitation wavelength. */
	private FilterData			secondaryExFilter;
	
	/** The light source. */
	private LightSourceData		ligthSource;
	
	/** Flag indicating if the detector is dirty. */
	private boolean				detectorDirty;
	
	/** Flag indicating if the detector settings is dirty. */
	private boolean				detectorSettingsDirty;
	
	/** Flag indicating if the detector settings is dirty. */
	private boolean				ligthSourceSettingsDirty;

	/** The detector settings binning. */
	private Binning 			binning;
	
	/** The detector's type. */
	private DetectorType 		detectorType;

	/**
	 * Returns the detector.
	 * 
	 * @return See above.
	 */
	private Detector getDetector()
	{
		if (detectorSettings == null) return null;
		return detectorSettings.getDetector();
	}
	
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
        Filter f = channel.getSecondaryEmissionFilter();
        if (f != null) secondaryEmFilter = new FilterData(f);
        f = channel.getSecondaryExcitationFilter();
        if (f != null) secondaryExFilter = new FilterData(f);
	}
	
	/**
	 * Returns the offset set on the detector.
	 * 
	 * @return See above.
	 */
	public double getDetectorSettingsOffset()
	{
		if (detectorSettings == null) return -1;
		RDouble value = detectorSettings.getOffsetValue();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the gain set on the detector.
	 * 
	 * @return See above.
	 */
	public double getDetectorSettingsGain()
	{
		if (detectorSettings == null) return -1;
		RDouble value = detectorSettings.getGain();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the voltage set on the detector.
	 * 
	 * @return See above.
	 */
	public double getDetectorSettingsVoltage()
	{
		if (detectorSettings == null) return -1;
		RDouble value = detectorSettings.getVoltage();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the Read out rate set on the detector.
	 * 
	 * @return See above.
	 */
	public double getDetectorSettingsReadOutRate()
	{
		if (detectorSettings == null) return -1;
		RDouble value = detectorSettings.getReadOutRate();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the binning.
	 * 
	 * @return See above.
	 */
	public String getDetectorSettingsBinning()
	{
		if (binning != null) return binning.getValue().getValue();
		if (detectorSettings == null) return "";
		Binning value = detectorSettings.getBinning();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the voltage of the detector.
	 * 
	 * @return See above
	 */
	public double getDetectorVoltage()
	{
		Detector detector = getDetector();
		if (detector == null) return -1;
		RDouble value = detector.getVoltage();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the amplification gain of the detector.
	 * 
	 * @return See above
	 */
	public double getDetectorAmplificationGain()
	{
		Detector detector = getDetector();
		if (detector == null) return -1;
		RDouble value = detector.getAmplificationGain();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the gain of the detector.
	 * 
	 * @return See above
	 */
	public double getDetectorGain()
	{
		Detector detector = getDetector();
		if (detector == null) return -1;
		RDouble value = detector.getGain();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public double getDetectorOffset()
	{
		Detector detector = getDetector();
		if (detector == null) return -1;
		RDouble value = detector.getOffsetValue();
		if (value == null) return -1;
		return value.getValue();
	}

	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public double getDetectorZoom()
	{
		Detector detector = getDetector();
		if (detector == null) return -1;
		RDouble value = detector.getZoom();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the type of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorType()
	{
		if (detectorType != null) return detectorType.getValue().getValue();
		Detector detector = getDetector();
		if (detector == null) return "";
		DetectorType type = detector.getType();
		if (type == null) return "";
		return type.getValue().getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorManufacturer()
	{
		Detector detector = getDetector();
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
		Detector detector = getDetector();
		if (detector == null) return "";
		RString value = detector.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the serial number of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorSerialNumber()
	{
		Detector detector = getDetector();
		if (detector == null) return "";
		RString value = detector.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the lot number of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorLotNumber()
	{
		Detector detector = getDetector();
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
	public double getLigthSettingsAttenuation()
	{
		if (lightSettings == null) return -1;
		RDouble value = lightSettings.getAttenuation();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the wavelength of the ligth source.
	 * 
	 * @return See above.
	 */
	public int getLigthSettingsWavelength()
	{
		if (lightSettings == null) return -1;
		RInt value = lightSettings.getWavelength();
		if (value == null) return -1;
		return value.getValue();
	}

	/**
	 * Returns the secondary emission filter.
	 * 
	 * @return See above.
	 */
	public FilterData getSecondaryEmissionFilter() { return secondaryEmFilter; }
	
	/**
	 * Returns the secondary excitation filter.
	 * 
	 * @return See above.
	 */
	public FilterData getSecondaryExcitationFilter()
	{
		return secondaryExFilter;
	}
	
	
	
	
	/**
	 * Returns <code>true</code> if there is an filter for that channel, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFilter()
	{
		return filterSet != null;
	}
	
	/**
	 * Returns <code>true</code> if there is a detector for that channel,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDectector() { return getDetector() != null; }
	
	/**
	 * Sets the attenuation of the light settings.
	 * 
	 * @param value The value to set.
	 */
	public void setLigthSettingsAttenuation(double value)
	{
		ligthSourceSettingsDirty = true;
		if (lightSettings == null) lightSettings = new LightSettingsI();
		lightSettings.setAttenuation(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Returns the wavelength of the ligth source.
	 * 
	 * @param value The value to set.
	 */
	public void setLigthSettingsWavelength(int value)
	{
		ligthSourceSettingsDirty = true;
		if (lightSettings == null) lightSettings = new LightSettingsI();
		lightSettings.setWavelength(omero.rtypes.rint(value));
	}
	
	/**
	 * Sets the serial number of the detector.
	 * 
	 * @param number The value to set.
	 */
	public void setDetectorSerialNumber(String number)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setSerialNumber(omero.rtypes.rstring(number));
	}
	
	/**
	 * Sets the model of the detector.
	 * 
	 * @param model The value to set.
	 */
	public void setDetectorModel(String model)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setModel(omero.rtypes.rstring(model));
	}

	/**
	 * Sets the manufacturer of the detector.
	 * 
	 * @param manufacturer The value to set.
	 */
	public void setDetectorManufacturer(String manufacturer)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setManufacturer(omero.rtypes.rstring(manufacturer));
	}

	/**
	 * Sets the detector's gain.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorAmplificationGain(double value)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setAmplificationGain(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's gain.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorGain(double value)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setGain(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's offset.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorOffset(double value)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setOffsetValue(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's voltage.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorVoltage(double value)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setVoltage(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's zoom.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorZoom(double value)
	{
		detectorDirty = true;
		Detector d = getDetector();
		if (d == null) d = new DetectorI();
		d.setZoom(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's type.
	 * 
	 * @param detectorType The value to set.
	 */
	public void setDetectorType(DetectorType detectorType)
	{
		detectorDirty = true;
		this.detectorType = detectorType;
	}

	/**
	 * Sets the detector's setting offset.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorSettingOffset(double value)
	{
		detectorSettingsDirty = true;
		if (detectorSettings == null) 
			detectorSettings = new DetectorSettingsI();
		detectorSettings.setOffsetValue(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector setting's gain.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorSettingsGain(double value)
	{
		detectorSettingsDirty = true;
		if (detectorSettings == null) 
			detectorSettings = new DetectorSettingsI();
		detectorSettings.setGain(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector setting's read out rate.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorSettingsReadOutRate(double value)
	{
		detectorSettingsDirty = true;
		if (detectorSettings == null) 
			detectorSettings = new DetectorSettingsI();
		detectorSettings.setReadOutRate(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector setting's valtage.
	 * 
	 * @param value The value to set.
	 */
	public void setDetectorSettingsVoltage(double value)
	{
		detectorSettingsDirty = true;
		if (detectorSettings == null) 
			detectorSettings = new DetectorSettingsI();
		detectorSettings.setVoltage(omero.rtypes.rdouble(value));
	}
	
	/**
	 * Sets the detector's binning.
	 * 
	 * @param binning The value to set.
	 */
	public void setDetectorSettingBinning(Binning binning)
	{
		this.binning = binning;
	}
	
	/**
	 * Returns the binning enumeration value.
	 * 
	 * @return See above.
	 */
	public Binning getDetectorBinningAsEnum() { return binning; }
	
	/**
	 * Returns the binning enumeration value.
	 * 
	 * @return See above.
	 */
	public DetectorType getDetectorTypeAsEnum() { return detectorType; }
	
	/**
	 * Returns <code>true</code> if the detector has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDetectorDirty() { return detectorDirty; }
	
	/**
	 * Returns <code>true</code> if the detector settings has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDetectorSettingsDirty() { return detectorSettingsDirty; }
	
	/**
	 * Returns <code>true</code> if the light source settings has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isLightSourceSettingsDirty()
	{ 
		return ligthSourceSettingsDirty; 
	}
	
	/**
	 * Returns the source of light.
	 * 
	 * @return See above.
	 */
	public LightSourceData getLightSource()
	{
		if (lightSettings == null) return null;
		if (ligthSource != null) return ligthSource;
		LightSource src = lightSettings.getLightSource();
		if (src != null) ligthSource = new LightSourceData(src);
		return ligthSource;
	}
	
}
