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
import omero.RBool;
import omero.RFloat;
import omero.RInt;
import omero.RString;
import omero.model.Arc;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.DetectorType;
import omero.model.Filament;
import omero.model.FilamentType;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.Laser;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LightEmittingDiode;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.Pulse;

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

	/** Indicates that the light source is a <code>laser</code>. */
	public static final String LASER = Laser.class.getName();
	
	/** Indicates that the light source is a <code>filament</code>. */
	public static final String FILAMENT = Filament.class.getName();
	
	/** Indicates that the light source is a <code>arc</code>. */
	public static final String ARC = Arc.class.getName();
	
	/** 
	 * Indicates that the light source is a 
	 * <code>light emitting diode</code>. 
	 */
	public static final String LIGHT_EMITTING_DIODE = 
		LightEmittingDiode.class.getName();
	
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
	
	/** The light source. */
	private LightSource			ligthSource;
	
	/**
	 * Returns the source of light.
	 * 
	 * @return See above.
	 */
	private LightSource getLightSource()
	{
		if (lightSettings == null) return null;
		if (ligthSource != null) return ligthSource;
		return lightSettings.getLightSource();
	}
	
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
		if (detectorSettings == null) return 0;
		RFloat value = detectorSettings.getOffsetValue();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the gain set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsGain()
	{
		if (detectorSettings == null) return 0;
		RFloat value = detectorSettings.getGain();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the voltage set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsVoltage()
	{
		if (detectorSettings == null) return 0;
		RFloat value = detectorSettings.getVoltage();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the Read out rate set on the detector.
	 * 
	 * @return See above.
	 */
	public float getDetectorSettingsReadOutRate()
	{
		if (detectorSettings == null) return 0;
		RFloat value = detectorSettings.getReadOutRate();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the binning.
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
	 * Returns the voltage of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorVoltage()
	{
		Detector detector = getDetector();
		if (detector == null) return 0;
		RFloat value = detector.getVoltage();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the amplification gain of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorAmplificationGain()
	{
		Detector detector = getDetector();
		if (detector == null) return 0;
		RFloat value = detector.getAmplificationGain();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the gain of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorGain()
	{
		Detector detector = getDetector();
		if (detector == null) return 0;
		RFloat value = detector.getGain();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorOffset()
	{
		Detector detector = getDetector();
		if (detector == null) return 0;
		RFloat value = detector.getOffsetValue();
		if (value == null) return 0;
		return value.getValue();
	}

	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public float getDetectorZoom()
	{
		Detector detector = getDetector();
		if (detector == null) return 0;
		RFloat value = detector.getZoom();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the type of the detector.
	 * 
	 * @return See above.
	 */
	public String getDetectorType()
	{
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
	 * Returns the manufacturer of the detector.
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
	 * Returns the attenuation of the ligth source, percent value 
	 * between 0 and 1.
	 * 
	 * @return See above.
	 */
	public float getLigthSettingsAttenuation()
	{
		if (lightSettings == null) return 0;
		RFloat value = lightSettings.getAttenuation();
		if (value == null) return 0;
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
		LightSource light = getLightSource();
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
		LightSource light = getLightSource();
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
		LightSource light = getLightSource();
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
		LightSource light = getLightSource();
		if (light == null) return 0;
		RFloat value = light.getPower();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the type of light.
	 * 
	 * @return See above.
	 */
	public String getLightType()
	{
		LightSource light = getLightSource();
		if (light == null) return "";
		RString value = null;
		if (light instanceof Laser) {
			LaserType t = ((Laser) light).getType();
			value = t.getValue();
		} else if (light instanceof Filament) {
			FilamentType t = ((Filament) light).getType();
			value = t.getValue();
		} else if (light instanceof Arc) {
			ArcType t = ((Arc) light).getType();
			value = t.getValue();
		}
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the laser's medium.
	 * 
	 * @return See above.
	 */
	public String getLaserMedium()
	{
		LightSource light = getLightSource();
		if (light == null || !(light instanceof Laser)) return "";
		Laser laser = (Laser) light;
		LaserMedium medium = laser.getLaserMedium();
		return medium.getValue().getValue();
	}
	
	/**
	 * Returns the laser's wavelength.
	 * 
	 * @return See above.
	 */
	public int getLaserWavelength()
	{
		if (!LASER.equals(getLightSourceKind())) return 0;
		Laser laser = (Laser) getLightSource();
		RInt value = laser.getWavelength();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the value of the <code>tuneable</code> field or <code>null</code>
	 * if no value set.
	 * 
	 * @return See above.
	 */
	public Object getLaserTuneable()
	{
		LightSource light = getLightSource();
		if (light == null || !(light instanceof Laser)) return null;
		Laser laser = (Laser) light;
		RBool value = laser.getTunable();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the kind of light source.
	 * 
	 * @return See above.
	 */
	public String getLightSourceKind()
	{
		LightSource light = getLightSource();
		if (light == null) return "";
		if (light instanceof Laser) return LASER;
		if (light instanceof Filament) return FILAMENT;
		if (light instanceof Arc) return ARC;
		if (light instanceof LightEmittingDiode) return LIGHT_EMITTING_DIODE;
		return "";
	}

	/**
	 * Returns <code>true</code> if there is a detector for that channel,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDectector() { return getDetector() != null; }
	
	/**
	 * Returns <code>true</code> if there is a light source for that channel,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasLightSource() { return getLightSource() != null; }
	
	/**
	 * Returns <code>true</code> if the light source is a laser with a pump,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	public boolean hasPump()
	{
		if (!LASER.equals(getLightSourceKind())) return false;
		Laser laser = (Laser) getLightSource();
		return laser.getPump() != null;
	}
	
	/**
	 * Returns the frequency multiplication of the laser.
	 * 
	 * @return See above
	 */
	public int getLaserFrequencyMultiplication()
	{
		if (!LASER.equals(getLightSourceKind())) return 0;
		Laser laser = (Laser) getLightSource();
		RInt value = laser.getFrequencyMultiplication();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the pulse of the laser.
	 * 
	 * @return See above
	 */
	public String getLaserPulse()
	{
		if (!LASER.equals(getLightSourceKind())) return null;
		Laser laser = (Laser) getLightSource();
		Pulse value = laser.getPulse();
		if (value == null) return null;
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the pockel cell flag of the laser.
	 * 
	 * @return See above
	 */
	public Object getLaserPockelCell()
	{
		if (!LASER.equals(getLightSourceKind())) return null;
		Laser laser = (Laser) getLightSource();
		RBool value = laser.getPockelCell();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the repetition rate (Hz) if the laser is repetitive.
	 * 
	 * @return See above.
	 */
	public float getLaserRepetitionRate()
	{
		if (!LASER.equals(getLightSourceKind())) return 0;
		Laser laser = (Laser) getLightSource();
		RFloat value = laser.getRepetitionRate();
		if (value  == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Returns the id of the light source.
	 * 
	 * @return See above.
	 */
	public long getLightSourceId()
	{
		LightSource source = getLightSource();
		if (source == null) return -1;
		return source.getId().getValue();
	}
	
	/**
	 * Sets the light source.
	 * 
	 * @param lightSource The value to set.
	 */
	public void setLightSource(LightSource lightSource)
	{
		this.ligthSource = lightSource;
	}
	
}
