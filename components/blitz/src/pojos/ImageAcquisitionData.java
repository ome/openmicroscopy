/*
 * pojos.ImageAcquisitionData 
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

import static ome.xml.model.StageLabel.getXUnitXsdDefault;
import static ome.xml.model.StageLabel.getYUnitXsdDefault;
import static ome.xml.model.StageLabel.getZUnitXsdDefault;
import static ome.formats.model.UnitsFactory.makeLength;

import omero.RDouble;
import omero.RLong;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Length;
import omero.model.Medium;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.StageLabel;
import omero.model.StageLabelI;

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
	 * @return See above.
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
	 * @return See above.
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
	 * @return See above.
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
	 * @return See above.
	 */
	public Object getTemperature()
	{
		if (environment == null) return null;
		RDouble value = environment.getTemperature();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the air pressure in bar
	 * 
	 * @return See above.
	 */
	public double getAirPressure()
	{
		if (environment == null) return -1;
		RDouble value = environment.getAirPressure();
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
	@Deprecated
	public void setPositionX(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionX(makeLength(value, getXUnitXsdDefault()));
	}
	
	/**
	 * Sets the y-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionY(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionY(makeLength(value, getYUnitXsdDefault()));
	}
	
	/**
	 * Sets the z-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionZ(double value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionZ(makeLength(value, getZUnitXsdDefault()));
	}
	
	/**
	 * Sets the temperature.
	 * 
	 * @param temperature The value to set.
	 */
	public void setTemperature(double temperature)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setTemperature(omero.rtypes.rdouble(temperature));
	}
	
	/**
	 * Sets the air pressure.
	 * 
	 * @param pressure The value to set.
	 */
	public void setAirPressure(double pressure)
	{
		imagingEnvironmentDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setAirPressure(omero.rtypes.rdouble(pressure));
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
