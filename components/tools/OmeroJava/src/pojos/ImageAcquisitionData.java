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


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.RBool;
import omero.RFloat;
import omero.RInt;
import omero.RString;
import omero.model.Correction;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Immersion;
import omero.model.Medium;
import omero.model.Objective;
import omero.model.ObjectiveI;
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
	private ObjectiveSettings	objective;
	
	/** Flag indicating if the position is dirty. */
	private boolean				labelDirty;
	
	/** Flag indicating if the condition is dirty. */
	private boolean				conditionDirty;
	
	/** Flag indicating if the objective settings is dirty. */
	private boolean				objectiveSettingsDirty;
	
	/** Flag indicating if the objective is dirty. */
	private boolean				objectiveDirty;
	
	/** The objective's medium. */
	private Medium				medium;
	
	/** The objective's immersion. */
	private Immersion			immersion;
	
	/** The objective's correction. */
	private Correction			correction;
	
	/**
	 * Returns the objective if any.
	 * 
	 * @return See above.
	 */
	private Objective getObjective()
	{
		if (objective == null) return null;
		return objective.getObjective();
	}
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
        label = image.getPosition();
        environment = image.getCondition();
        objective = image.getObjectiveSettings();
        medium = null;
        immersion = null;
        correction = null;
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
	public Object getPositionX()
	{
		if (label == null) return null;
		RFloat value = label.getPositionX();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the y-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 */
	public Object getPositionY()
	{
		if (label == null) return null;
		RFloat value = label.getPositionY();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the z-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 */
	public Object getPositionZ()
	{
		if (label == null) return null;
		RFloat value = label.getPositionZ();
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
		RFloat value = environment.getTemperature();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the air pressure in bar
	 * 
	 * @return See above.
	 */
	public float getAirPressure()
	{
		if (environment == null) return -1;
		RFloat value = environment.getAirPressure();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the humidity level, this is a value in the interval [0, 1]
	 * 
	 * @return See above.
	 */
	public float getHumidity()
	{
		if (environment == null) return -1;
		RFloat value = environment.getHumidity();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the Co2 level, this is a percent value in the interval [0, 1].
	 * 
	 * @return See above.
	 */
	public float getCo2Percent()
	{
		if (environment == null) return -1;
		RFloat value = environment.getCo2percent();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the correction collar of the objective.
	 * 
	 * @return See above.
	 */
	public float getCorrectionCollar()
	{
		if (objective == null) return -1;
		RFloat value = objective.getCorrectionCollar();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the refractive index of the objective.
	 * 
	 * @return See above.
	 */
	public float getRefractiveIndex()
	{
		if (objective == null) return -1;
		RFloat value = objective.getRefractiveIndex();
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
		if (objective == null) return "";
		Medium value = objective.getMedium();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the objective's calibrated magnification factor.
	 * 
	 * @return See above.
	 */
	public float getCalibratedMagnification()
	{
		Objective obj = getObjective();
		if (obj == null) return -1;
		RFloat value = obj.getCalibratedMagnification();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the objective's nominal magnification factor.
	 * 
	 * @return See above.
	 */
	public int getNominalMagnification()
	{
		Objective obj = getObjective();
		if (obj == null) return -1;
		RInt value = obj.getNominalMagnification();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the objective's LensNA.
	 * 
	 * @return See above.
	 */
	public float getLensNA()
	{
		Objective obj = getObjective();
		if (obj == null) return -1;
		RFloat value = obj.getLensNA();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the immersion value of the objective.
	 * 
	 * @return See above.
	 */
	public String getImmersion()
	{
		if (immersion != null) return immersion.getValue().getValue();
		Objective obj = getObjective();
		if (obj == null) return "";
		Immersion value = obj.getImmersion();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the correction value of the objective.
	 * 
	 * @return See above.
	 */
	public String getCorrection()
	{
		if (correction != null) return correction.getValue().getValue();
		Objective obj = getObjective();
		if (obj == null) return "";
		Correction value = obj.getCorrection();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the working distance.
	 * 
	 * @return See above.
	 */
	public float getWorkingDistance()
	{
		Objective obj = getObjective();
		if (obj == null) return -1;
		RFloat value = obj.getWorkingDistance();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the serial number of the objective.
	 * 
	 * @return See above.
	 */
	public String getSerialNumber()
	{
		if (objective == null) return "";
		Objective obj = objective.getObjective();
		if (obj == null) return "";
		RString value = obj.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model of the objective.
	 * 
	 * @return See above.
	 */
	public String getModel()
	{
		if (objective == null) return "";
		Objective obj = objective.getObjective();
		if (obj == null) return "";
		RString value = obj.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns a boolean flag if the value has been set, <code>null</code> 
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public Object hasIris()
	{
		if (objective == null) return null;
		Objective obj = objective.getObjective();
		if (obj == null) return null;
		RBool value = obj.getIris();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Sets the iris flag.
	 * 
	 * @param iris The value to set.
	 */
	public void setIris(boolean iris)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setIris(omero.rtypes.rbool(iris));
	}
	
	/**
	 * Returns the manufacturer of the objective.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		if (objective == null) return "";
		Objective obj = objective.getObjective();
		if (obj == null) return "";
		RString value = obj.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Sets the serial number.
	 * 
	 * @param number The value to set.
	 */
	public void setSerialNumber(String number)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setSerialNumber(omero.rtypes.rstring(number));
	}
	
	/**
	 * Sets the model.
	 * 
	 * @param model The value to set.
	 */
	public void setModel(String model)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setModel(omero.rtypes.rstring(model));
	}

	/**
	 * Sets the manufacturer.
	 * 
	 * @param manufacturer The value to set.
	 */
	public void setManufacturer(String manufacturer)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setManufacturer(omero.rtypes.rstring(manufacturer));
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
	public void setPositionX(float value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionX(omero.rtypes.rfloat(value));
	}
	
	/**
	 * Sets the y-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionY(float value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionY(omero.rtypes.rfloat(value));
	}
	
	/**
	 * Sets the z-position.
	 * 
	 * @param value The value to set.
	 */
	public void setPositionZ(float value)
	{
		labelDirty = true;
		if (label == null) label = new StageLabelI();
		label.setPositionZ(omero.rtypes.rfloat(value));
	}
	
	/**
	 * Sets the temperature.
	 * 
	 * @param temperature The value to set.
	 */
	public void setTemperature(float temperature)
	{
		conditionDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setTemperature(omero.rtypes.rfloat(temperature));
	}
	
	/**
	 * Sets the air pressure.
	 * 
	 * @param pressure The value to set.
	 */
	public void setAirPressure(float pressure)
	{
		conditionDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setAirPressure(omero.rtypes.rfloat(pressure));
	}
	
	/**
	 * Sets the humidity.
	 * 
	 * @param humidity The value to set.
	 */
	public void setHumidity(float humidity)
	{
		if (humidity < 0 || humidity > 1)
			throw new IllegalArgumentException("Humidity must " +
					"be a value in [0, 1]");
		conditionDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setHumidity(omero.rtypes.rfloat(humidity));
	}
	
	/**
	 * Sets the co2 level.
	 * 
	 * @param co2 The value to set.
	 */
	public void setCo2Percent(float co2)
	{
		if (co2 < 0 || co2 > 1)
			throw new IllegalArgumentException("Co2 must be a value in [0, 1]");
		conditionDirty = true;
		if (environment == null) environment = new ImagingEnvironmentI();
		environment.setCo2percent(omero.rtypes.rfloat(co2));
	}
	
	/**
	 * Sets the correction of the objective.
	 * 
	 * @param correction The value to set.
	 */
	public void setCorrectionCollar(float correction)
	{
		objectiveSettingsDirty = true;
		if (objective == null) objective = new ObjectiveSettingsI();	
		objective.setCorrectionCollar(omero.rtypes.rfloat(correction));
	}
	
	/**
	 * Sets the refractive index of the objective.
	 * 
	 * @param index The value to set.
	 */
	public void setRefractiveIndex(float index)
	{
		objectiveSettingsDirty = true;
		if (objective == null) objective = new ObjectiveSettingsI();	
		objective.setRefractiveIndex(omero.rtypes.rfloat(index));
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
	 * Sets the magnification factor of the objective.
	 * 
	 * @param factor The value to set.
	 */
	public void setCalibratedMagnification(float factor)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setCalibratedMagnification(omero.rtypes.rfloat(factor));
	}
	
	/**
	 * Sets the magnification factor of the objective.
	 * 
	 * @param factor The value to set.
	 */
	public void setNominalMagnification(int factor)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setNominalMagnification(omero.rtypes.rint(factor));
	}
	
	/**
	 * Sets the lens numerical aperture.
	 * 
	 * @param na The value to set.
	 */
	public void setLensNA(float na)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setLensNA(omero.rtypes.rfloat(na));
	}
	
	/**
	 * Sets the working distance.
	 * 
	 * @param distance The value to set.
	 */
	public void setWorkingDistance(float distance)
	{
		objectiveDirty = true;
		Objective ob = objective.getObjective();
		if (ob == null) ob = new ObjectiveI();
		ob.setWorkingDistance(omero.rtypes.rfloat(distance));
	}
	
	/**
	 * Sets the immersion.
	 * 
	 * @param immersion The value to set.
	 */
	public void setImmersion(Immersion immersion)
	{
		objectiveDirty = true;
		this.immersion = immersion;
	}
	
	/**
	 * Sets the correction.
	 * 
	 * @param correction The value to set.
	 */
	public void setCorrection(Correction correction)
	{
		objectiveDirty = true;
		this.correction = correction;
	}
	
	/**
	 * Returns <code>true</code> if the position has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isPositionDirty() { return labelDirty; }
	
	/**
	 * Returns <code>true</code> if the position has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isConditionDirty() { return conditionDirty; }
	
	/**
	 * Returns <code>true</code> if the objective settings has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isObjectiveSettingsDirty() { return objectiveSettingsDirty; }
	
	/**
	 * Returns <code>true</code> if the objective has been updated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isObjectiveDirty() { return objectiveDirty; }
	
	/**
	 * Returns the id of the <code>StageLabel</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getPositionId()
	{
		if (label == null) return -1;
		return label.getId().getValue();
	}
	
	/**
	 * Returns the id of the <code>StageLabel</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getConditionId()
	{
		if (environment == null) return -1;
		return environment.getId().getValue();
	}
	
	/**
	 * Returns the id of the <code>Objective settings</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getObjectiveSettingsId()
	{
		if (objective == null) return -1;
		return objective.getId().getValue();
	}
	
	/**
	 * Returns the id of the <code>Objective</code> or <code>-1</code>
	 * if not already linked to the image.
	 * 
	 * @return See above
	 */
	public long getObjectiveId()
	{
		Objective ob = getObjective();
		if (ob == null) return -1;
		return ob.getId().getValue();
	}

	/**
	 * Returns the medium enumeration value.
	 * 
	 * @return See above.
	 */
	public Medium getMediumAsEnum() { return medium; }
	
	/**
	 * Returns the immersion enumeration value.
	 * 
	 * @return See above.
	 */
	public Immersion getImmersionAsEnum() { return immersion; }
	
	/**
	 * Returns the immersion enumeration value.
	 * 
	 * @return See above.
	 */
	public Correction getCorrectionAsEnum() { return correction; }
	
}
