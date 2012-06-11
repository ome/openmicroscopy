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
import omero.model.AcquisitionMode;
import omero.model.Binning;
import omero.model.ContrastMethod;
import omero.model.DetectorSettings;
import omero.model.DetectorSettingsI;
import omero.model.FilterSet;
import omero.model.Illumination;
import omero.model.LightPath;
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
	
	/** The filterSet used. */
	private FilterSetData		filterSet;
	
	/** The light path described. */
	private LightPathData		lightPath;
	
	/** The light source. */
	private LightSourceData		ligthSource;
	
	/** Flag indicating if the detector settings is dirty. */
	private boolean				detectorSettingsDirty;
	
	/** Flag indicating if the detector settings is dirty. */
	private boolean				ligthSourceSettingsDirty;

	/** The detector used. */
	private DetectorData		detector;
	
	/** The otf used. */
	private OTFData				otf;
	
	/** The binning factor. */
	private Binning 			binning;
	
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
        FilterSet set = channel.getFilterSet();
        if (set != null) filterSet = new FilterSetData(set);
        LightPath path = channel.getLightPath();
        if (path != null) lightPath = new LightPathData(path);
        if (channel.getOtf() != null) 
			otf = new OTFData(channel.getOtf());
	}
	
	/**
	 * Returns the detector used for that channel.
	 * 
	 * @return See above.
	 */
	public DetectorData getDetector()
	{
		if (detectorSettings == null) return null;
		if (detector == null) 
			detector = new DetectorData(detectorSettings.getDetector());
		return detector;
	}
	
	/**
	 * Returns the OTF used for that channel.
	 * 
	 * @return See above.
	 */
	public OTFData getOTF() { return otf; }
	
	/**
	 * Returns the offset set on the detector.
	 * 
	 * @return See above.
	 */
	public Double getDetectorSettingsOffset()
	{
		if (detectorSettings == null) return null;
		RDouble value = detectorSettings.getOffsetValue();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the gain set on the detector.
	 * 
	 * @return See above.
	 */
	public Double getDetectorSettingsGain()
	{
		if (detectorSettings == null) return null;
		RDouble value = detectorSettings.getGain();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the voltage set on the detector.
	 * 
	 * @return See above.
	 */
	public Double getDetectorSettingsVoltage()
	{
		if (detectorSettings == null) return null;
		RDouble value = detectorSettings.getVoltage();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the Read out rate set on the detector.
	 * 
	 * @return See above.
	 */
	public Double getDetectorSettingsReadOutRate()
	{
		if (detectorSettings == null) return null;
		RDouble value = detectorSettings.getReadOutRate();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the Binning factor.
	 * 
	 * @return See above.
	 */
	public String getDetectorSettingsBinning()
	{
		if (detectorSettings == null) return "";
		Binning value = detectorSettings.getBinning();
		if (value == null) return "";
		return value.getValue().getValue();
	}

	/**
	 * Returns the attenuation of the light source, percent value 
	 * between 0 and 1.
	 * 
	 * @return See above.
	 */
	public Double getLigthSettingsAttenuation()
	{
		if (lightSettings == null) return null;
		RDouble value = lightSettings.getAttenuation();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the wavelength of the light source.
	 * 
	 * @return See above.
	 */
	public Integer getLigthSettingsWavelength()
	{
		if (lightSettings == null) return null;
		RInt value = lightSettings.getWavelength();
		if (value == null) return null;
		return value.getValue();
	}

	/**
	 * Returns <code>true</code> if there is a filter set linked to the channel
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFilter() { return filterSet != null; }
	
	/**
	 * Returns <code>true</code> if there is a light path described
	 * for that channel, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasLightPath() { return lightPath != null; }
	
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
	 * Returns the wavelength of the light source.
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
	 * Sets the detector setting's voltage.
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
	
	/**
	 * Sets the light source associated to the settings.
	 * 
	 * @param ligthSource The value to set.
	 */
	public void setLightSource(LightSourceData ligthSource)
	{
		this.ligthSource = ligthSource;
	}
	
	/**
	 * Returns the light path or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public LightPathData getLightPath() { return lightPath; }
	
	/**
	 * Returns the filter set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public FilterSetData getFilterSet() { return filterSet; }
	
    /**
     * Returns the illumination.
     * 
     * @return See above.
     */
    public String getIllumination()
    { 
    	LogicalChannel lc = (LogicalChannel) asIObject();
    	if (lc == null) return null;
    	Illumination value =  lc.getIllumination();
    	if (value != null) return value.getValue().getValue();
    	return null; 
    }

    /**
     * Returns the contrast method.
     * 
     * @return See above.
     */
    public String getContrastMethod()
    { 
    	LogicalChannel lc = (LogicalChannel) asIObject();
    	if (lc == null) return null;
    	ContrastMethod value =  lc.getContrastMethod();
    	if (value != null) return value.getValue().getValue();
    	return null; 
    }
    
    /**
     * Returns the mode.
     * 
     * @return See above.
     */
    public String getMode()
    { 
    	LogicalChannel lc = (LogicalChannel) asIObject();
    	if (lc == null) return null;
    	AcquisitionMode value =  lc.getMode();
    	if (value != null) return value.getValue().getValue();
    	return null; 
    }
    
}
