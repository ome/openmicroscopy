/*
 * pojos.ImageAcquisitionData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package omero.gateway.model;

import ome.formats.model.UnitsFactory;
import ome.model.units.BigResult;
import omero.RDouble;
import omero.RLong;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Medium;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.Pressure;
import omero.model.PressureI;
import omero.model.StageLabel;
import omero.model.StageLabelI;
import omero.model.Temperature;
import omero.model.TemperatureI;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsPressure;
import omero.model.enums.UnitsTemperature;

/** 
 * Object hosting the acquisition metadata.
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
public class ImageAcquisitionData 
	extends DataObject
{

	/** The stage Label object linked to the image if any. */
	private StageLabel 			label;
	
	/** The environment information. */
	private ImagingEnvironment	environment;
	
	/** The objective used to acquire the image. */
	private ObjectiveSettings	objectiveSettings;
	
	/** Flag indicating if the StageLabel is dirty. */
	private boolean				labelDirty;
	
	/** Flag indicating if the imagingEnvironment is dirty. */
	private boolean				imagingEnvironmentDirty;
	
	/** Flag indicating if the objective settings is dirty. */
	private boolean				objectiveSettingsDirty;
	
	/** The objective's medium. */
	private Medium				medium;
	
	/** The objective used to capture the image. */
	private ObjectiveData		objective;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param image The image the acquisition data is related to. 
	 * 				Mustn't be <code>null</code>.
	 */
	public ImageAcquisitionData(Image image)
	{
        if (image == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(image);
        label = image.getStageLabel();
        environment = image.getImagingEnvironment();
        objectiveSettings = image.getObjectiveSettings();
        medium = null;
	}
	
	/**
	 * Returns the objective used to capture the image.
	 * 
	 * @return See above.
	 */
	public ObjectiveData getObjective()
	{
		if (objectiveSettings == null) return null;
		if (objective == null) 
			objective = new ObjectiveData(objectiveSettings.getObjective());
		return objective;
	}
	
	/**
	 * Returns the name of the stage label.
	 * 
	 * @return See above.
	 */
	public String getLabelName()
	{
		if (label == null) return "";
		return label.getName().getValue();
	}
	
	/**
	 * Returns the x-coordinate in the frame microscope.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred 
	 */
	public Length getPositionX(UnitsLength unit) throws BigResult
	{
		if (label == null) 
			return null;
		Length l = label.getPositionX();
		if (l==null)
			return null;
		return unit == null ? l : new LengthI(l, unit);
	}
	
	/**
	 * Returns the x-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getPositionX(UnitsLength)}
	 */
	@Deprecated
	public Object getPositionX()
	{
		if (label == null) return null;
		Length value = label.getPositionX();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the y-coordinate in the frame microscope.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred 
	 */
	public Length getPositionY(UnitsLength unit) throws BigResult
	{
		if (label == null) 
			return null;
		Length l = label.getPositionY();
		if (l==null)
			return null;
		return unit == null ? l : new LengthI(l, unit);
	}
	
	/**
	 * Returns the y-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getPositionY(UnitsLength)}
	 */
	@Deprecated
	public Object getPositionY()
	{
		if (label == null) return null;
		Length value = label.getPositionY();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the z-coordinate in the frame microscope.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Length getPositionZ(UnitsLength unit) throws BigResult
	{
		if (label == null) 
			return null;
		Length l = label.getPositionZ();
		if (l==null)
			return null;
		return unit == null ? l : new LengthI(l, unit);
	}
	
	/**
	 * Returns the z-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getPositionZ(UnitsLength)}
	 */
	@Deprecated
	public Object getPositionZ()
	{
		if (label == null) return null;
		Length value = label.getPositionZ();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the temperature in Celcius.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Temperature getTemperature(UnitsTemperature unit) throws BigResult
	{
		if (environment == null) 
			return null;
		Temperature t = environment.getTemperature();
		if (t==null)
			return null;
		return unit == null ? t : new TemperatureI(t, unit);
	}
	
	/**
	 * Returns the temperature in Celcius.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getTemperature(UnitsTemperature)}
	 */
	@Deprecated
	public Object getTemperature()
	{
		if (environment == null) return null;
		Temperature value = environment.getTemperature();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the air pressure in bar
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Pressure getAirPressure(UnitsPressure unit) throws BigResult
	{
		if (environment == null) 
			return null;
		Pressure p = environment.getAirPressure();
		if (p == null)
			return null;
		return unit == null ? p : new PressureI(p, unit);
	}
	
	/**
	 * Returns the air pressure in bar
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getAirPressure(UnitsPressure)}
	 */
	@Deprecated
	public double getAirPressure()
	{
		if (environment == null) return -1;
		Pressure value = environment.getAirPressure();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the humidity level, this is a value in the interval [0, 1]
	 * 
	 * @return See above.
	 */
	public double getHumidity()
	{
		if (environment == null) return -1;
		RDouble value = environment.getHumidity();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the Co2 level, this is a percent value in the interval [0, 1].
	 * 
	 * @return See above.
	 */
	public double getCo2Percent()
	{
		if (environment == null) return -1;
		RDouble value = environment.getCo2percent();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the correction collar of the objective.
	 * 
	 * @return See above.
	 */
	public double getCorrectionCollar()
	{
		if (objectiveSettings == null) return -1;
		RDouble value = objectiveSettings.getCorrectionCollar();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the refractive index of the objective.
	 * 
	 * @return See above.
	 */
	public double getRefractiveIndex()
	{
		if (objectiveSettings == null) return -1;
		RDouble value = objectiveSettings.getRefractiveIndex();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the medium of the objective.
	 * 
	 * @return See above.
	 */
	public String getMedium()
	{
		if (medium != null) return medium.getValue().getValue();
		if (objectiveSettings == null) return "";
		Medium value = objectiveSettings.getMedium();
		if (value == null) return "";
		return value.getValue().getValue();
	}

	/**
	 * Sets the name of the stage label.
	 * 
	 * @param name The value to set.
	 */
	public void setLabelName(String name)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setName(omero.rtypes.rstring(name));
	}
	
	/**
	 * Sets the x-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionX(Length value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionX(value);
	}
	
	/**
	 * Sets the x-position.
	 * 
	 * @param value The value to set.
	 * @deprecated Replaced by {@link #setPositionX(Length)}
	 */
	@Deprecated
	public void setPositionX(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionX(new LengthI(value, UnitsFactory.StageLabel_X));
	}
	
	/**
	 * Sets the y-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionY(Length value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionY(value);
	}
	
	/**
	 * Sets the y-position.
	 * 
	 * @param value The value to set.
	 * @deprecated Replaced by {@link #setPositionY(Length)}
	 */
	@Deprecated
	public void setPositionY(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionY(new LengthI(value, UnitsFactory.StageLabel_Y));
	}
	
	/**
	 * Sets the z-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionZ(Length value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionZ(value);
	}
	
	/**
	 * Sets the z-position.
	 * 
	 * @param value The value to set.
	 * @deprecated Replaced by {@link #setPositionZ(Length)}
	 */
	@Deprecated
	public void setPositionZ(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionZ(new LengthI(value, UnitsFactory.StageLabel_Z));
	}
	
	/**
	 * Sets the temperature.
	 * 
	 * @param temperature The value to set.
	 */
	public void setTemperature(Temperature temperature)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setTemperature(temperature);
	}
	
	/**
	 * Sets the temperature.
	 * 
	 * @param temperature The value to set.
	 * @deprecated Replaced by {@link #setTemperature(Temperature)}
	 */
	@Deprecated
	public void setTemperature(double temperature)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setTemperature(new TemperatureI(temperature,
		        UnitsFactory.ImagingEnvironment_Temperature));
	}
	
	/**
	 * Sets the air pressure.
	 * 
	 * @param pressure The value to set.
	 */
	public void setAirPressure(Pressure pressure)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setAirPressure(pressure);
	}
	
	/**
	 * Sets the air pressure.
	 * 
	 * @param pressure The value to set.
	 * @deprecated Replaced by {@link #setAirPressure(Pressure)}
	 */
	@Deprecated
	public void setAirPressure(double pressure)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setAirPressure(new PressureI(pressure,
		        UnitsFactory.ImagingEnvironment_AirPressure));
	}
	
	/**
	 * Sets the humidity.
	 * 
	 * @param humidity The value to set.
	 */
	public void setHumidity(double humidity)
	{
		if (humidity < 0 || humidity > 1)
			throw new IllegalArgumentException("Humidity must " +
					"be a value in [0, 1]");
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setHumidity(omero.rtypes.rdouble(humidity));
	}
	
	/**
	 * Sets the co2 level.
	 * 
	 * @param co2 The value to set.
	 */
	public void setCo2Percent(double co2)
	{
		if (co2 < 0 || co2 > 1)
			throw new IllegalArgumentException("Co2 must be a value in [0, 1]");
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setCo2percent(omero.rtypes.rdouble(co2));
	}
	
	/**
	 * Sets the correction of the objective.
	 * 
	 * @param correction The value to set.
	 */
	public void setCorrectionCollar(double correction)
	{
		objectiveSettingsDirty = true;
		if (objectiveSettings == null) 
			objectiveSettings = new ObjectiveSettingsI();	
		objectiveSettings.setCorrectionCollar(omero.rtypes.rdouble(correction));
	}
	
	/**
	 * Sets the refractive index of the objective.
	 * 
	 * @param index The value to set.
	 */
	public void setRefractiveIndex(double index)
	{
		objectiveSettingsDirty = true;
		if (objectiveSettings == null) 
			objectiveSettings = new ObjectiveSettingsI();	
		objectiveSettings.setRefractiveIndex(omero.rtypes.rdouble(index));
	}

	/**
	 * Sets the medium of the objective.
	 * 
	 * @param medium The value to set.
	 */
	public void setMedium(Medium medium)
	{
		this.medium = medium;
		objectiveSettingsDirty = true;
	}
	
	/**
	 * Returns <code>true</code> if the position has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isStageLabelDirty() { return labelDirty; }
	
	/**
	 * Returns <code>true</code> if the StageLabel has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isImagingEnvironmentDirty()
	{ 
		return imagingEnvironmentDirty;
	}
	
	/**
	 * Returns <code>true</code> if the objective settings has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isObjectiveSettingsDirty() { return objectiveSettingsDirty; }
	
	/**
	 * Returns the id of the <code>StageLabel</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getStageLabelId()
	{
		if (label == null) return -1;
		RLong id = label.getId();
		if (id == null) return -1;
		return id.getValue();
	}
	
	/**
	 * Returns the id of the <code>StageLabel</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getImagingEnvironmentId()
	{
		if (environment == null) return -1;
		RLong id = environment.getId();
		if (id == null) return -1;
		return id.getValue();
	}
	
	/**
	 * Returns the id of the <code>Objective settings</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getObjectiveSettingsId()
	{
		if (objectiveSettings == null) return -1;
		RLong id = objectiveSettings.getId();
		if (id == null) return -1;
		return id.getValue();
	}

	/**
	 * Returns the medium enumeration value.
	 * 
	 * @return See above.
	 */
	public Medium getMediumAsEnum() { return medium; }
	
}
