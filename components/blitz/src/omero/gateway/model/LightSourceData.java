/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package omero.gateway.model;

import ome.model.units.BigResult;
import omero.RBool;
import omero.RInt;
import omero.RString;
import omero.model.Arc;
import omero.model.ArcType;
import omero.model.Filament;
import omero.model.FilamentType;
import omero.model.Frequency;
import omero.model.FrequencyI;
import omero.model.Laser;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.LightEmittingDiode;
import omero.model.LightSource;
import omero.model.Power;
import omero.model.PowerI;
import omero.model.Pulse;
import omero.model.enums.UnitsFrequency;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsPower;

/** 
 * Object hosting a light source: filament, arc, laser or light emitting diode
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class LightSourceData 
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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The light source. Mustn't be <code>null</code>.
	 */
	public LightSourceData(LightSource source)
	{
        if (source == null)
            throw new IllegalArgumentException("No light source.");
        setValue(source);
	}
	
	/**
	 * Returns the serial number of the light source.
	 * 
	 * @return See above.
	 */
	public String getSerialNumber()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return "";
		RString value = light.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the lot of number the light source.
	 * 
	 * @return See above.
	 */
	public String getLotNumber()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return "";
		RString value = light.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the light source.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return "";
		RString value = light.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the light source.
	 * 
	 * @return See above.
	 */
	public String getLightSourceModel()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return "";
		RString value = light.getModel();
		if (value == null) return "";
		return value.getValue();
	}

	/**
	 * Returns the power of the light source.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred 
	 */
	public Power getPower(UnitsPower unit) throws BigResult
	{
		LightSource light = (LightSource) asIObject();
		if (light == null)
			return null;
		Power p = light.getPower();
		return unit == null ? p : new PowerI(p, unit);
	}
	
	/**
	 * Returns the power of the light source.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getPower(UnitsPower)}
	 */
	@Deprecated
	public double getPower()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return -1;
		Power value = light.getPower();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the type of light.
	 * 
	 * @return See above.
	 */
	public String getType()
	{
		LightSource light = (LightSource) asIObject();
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
		LightSource light = (LightSource) asIObject();
		if (light == null || !(light instanceof Laser)) return "";
		Laser laser = (Laser) light;
		LaserMedium medium = laser.getLaserMedium();
		return medium.getValue().getValue();
	}
	
	/**
	 * Returns the laser's wavelength.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Length getLaserWavelength(UnitsLength unit) throws BigResult
	{
		if (!LASER.equals(getKind())) 
			return null;
		Laser laser = (Laser) asIObject();
		Length l = laser.getWavelength();
		if (l==null)
			return null;
		return unit == null ? l : new LengthI(l, unit);
	}
	
	/**
	 * Returns the laser's wavelength.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getLaserWavelength(UnitsLength)}
	 */
	@Deprecated
	public double getLaserWavelength()
	{
		if (!LASER.equals(getKind())) return -1;
		Laser laser = (Laser) asIObject();
		Length value = laser.getWavelength();
		if (value == null) return -1;
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
		LightSource light = (LightSource) asIObject();
		if (light == null || !(light instanceof Laser)) return null;
		Laser laser = (Laser) light;
		RBool value = laser.getTuneable();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the kind of light source.
	 * 
	 * @return See above.
	 */
	public String getKind()
	{
		LightSource light = (LightSource) asIObject();
		if (light == null) return "";
		if (light instanceof Laser) return LASER;
		if (light instanceof Filament) return FILAMENT;
		if (light instanceof Arc) return ARC;
		if (light instanceof LightEmittingDiode) return LIGHT_EMITTING_DIODE;
		return "";
	}
	
	/**
	 * Returns <code>true</code> if the light source is a laser with a pump,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	public boolean hasPump()
	{
		if (!LASER.equals(getKind())) return false;
		Laser laser = (Laser) asIObject();
		return laser.getPump() != null;
	}
	
	/**
	 * Returns the frequency multiplication of the laser.
	 * 
	 * @return See above
	 */
	public int getLaserFrequencyMultiplication()
	{
		if (!LASER.equals(getKind())) return -1;
		Laser laser = (Laser) asIObject();
		RInt value = laser.getFrequencyMultiplication();
		if (value == null) return -1;
		return value.getValue();
	}

	/**
	 * Returns the pulse of the laser.
	 * 
	 * @return See above
	 */
	public String getLaserPulse()
	{
		if (!LASER.equals(getKind())) return null;
		Laser laser = (Laser) asIObject();
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
		if (!LASER.equals(getKind())) return null;
		Laser laser = (Laser) asIObject();
		RBool value = laser.getPockelCell();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the repetition rate (Hz) if the laser is repetitive.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Frequency getLaserRepetitionRate(UnitsFrequency unit) throws BigResult
	{
		if (!LASER.equals(getKind())) return null;
		Laser laser = (Laser) asIObject();
		Frequency f = laser.getRepetitionRate();
		if (f==null)
			return null;
		return unit == null ? f : new FrequencyI(f, unit);
	}
	
	/**
	 * Returns the repetition rate (Hz) if the laser is repetitive.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getLaserRepetitionRate(UnitsFrequency)}
	 */
	@Deprecated
	public double getLaserRepetitionRate()
	{
		if (!LASER.equals(getKind())) return -1;
		Laser laser = (Laser) asIObject();
		Frequency value = laser.getRepetitionRate();
		if (value  == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the pump.
	 * 
	 * @return See above.
	 */
	public LightSourceData getLaserPump()
	{
		if (!LASER.equals(getKind())) return null;
		Laser laser = (Laser) asIObject();
		LightSource pump = laser.getPump();
		if (pump == null) return null;
		return new LightSourceData(pump);
	}
	
}
