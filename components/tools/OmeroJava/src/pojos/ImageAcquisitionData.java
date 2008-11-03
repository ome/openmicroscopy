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
import omero.RDouble;
import omero.RFloat;
import omero.model.Coating;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Immersion;
import omero.model.Medium;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
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
	
	/**
	 * Creates a new instance.
	 * 
	 * @param image
	 */
	public ImageAcquisitionData(Image image)
	{
        if (image == null)
            throw new IllegalArgumentException("Object cannot null.");
        setValue(image);
        label = image.getPosition();
        environment = image.getCondition();
        objective = image.getObjectiveSettings();
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
	public float getPositionX()
	{
		if (label == null) return 0f;
		RFloat value = label.getPositionX();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the y-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 */
	public float getPositionY()
	{
		if (label == null) return 0f;
		RFloat value = label.getPositionY();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the z-coordinate in the frame microscope.
	 * 
	 * @return See above.
	 */
	public float getPositionZ()
	{
		if (label == null) return 0f;
		RFloat value = label.getPositionZ();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the temperature in Celcius.
	 * 
	 * @return See above.
	 */
	public float getTemperature()
	{
		if (environment == null) return 0f;
		RFloat value = environment.getTemperature();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the air pressure in bar
	 * 
	 * @return See above.
	 */
	public float getAirPressure()
	{
		if (environment == null) return 0f;
		RFloat value = environment.getAirPressure();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the humidity level, this is a value in the interval [0, 1]
	 * 
	 * @return See above.
	 */
	public float getHumidity()
	{
		if (environment == null) return 0f;
		RFloat value = environment.getHumidity();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the Co2 level, this is a value in the interval [0, 1]
	 * 
	 * @return See above.
	 */
	public float getCo2Percent()
	{
		if (environment == null) return 0f;
		RFloat value = environment.getCo2percent();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the correction collar of the objective.
	 * 
	 * @return See above.
	 */
	public float getCorrectionCollar()
	{
		if (objective == null) return 0f;
		RFloat value = objective.getCorrectionCollar();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the refractive index of the objective.
	 * 
	 * @return See above.
	 */
	public float getRefractiveIndex()
	{
		if (objective == null) return 0f;
		RFloat value = objective.getRefractiveIndex();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the medium of the objective.
	 * 
	 * @return See above.
	 */
	public String getMedium()
	{
		if (objective == null) return "";
		Medium value = objective.getMedium();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the objective's magnification factor.
	 * 
	 * @return See above.
	 */
	public double getMagnification()
	{
		if (objective == null) return 0.0;
		Objective obj = objective.getObjective();
		if (obj == null) return 0.0;
		RDouble value = obj.getMagnificiation();
		if (value == null) return 0.0;
		return value.getValue();
	}
	
	/**
	 * Returns the objective's magnification factor.
	 * 
	 * @return See above.
	 */
	public float getLensNA()
	{
		if (objective == null) return 0f;
		Objective obj = objective.getObjective();
		if (obj == null) return 0f;
		RFloat value = obj.getLensNA();
		if (value == null) return 0f;
		return value.getValue();
	}
	
	/**
	 * Returns the immersion value of the objective.
	 * 
	 * @return See above.
	 */
	public String getImmersion()
	{
		if (objective == null) return "";
		Objective obj = objective.getObjective();
		if (obj == null) return "";
		Immersion value = obj.getImmersion();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the coating value of the objective.
	 * 
	 * @return See above.
	 */
	public String getCoating()
	{
		if (objective == null) return "";
		Objective obj = objective.getObjective();
		if (obj == null) return "";
		Coating value = obj.getCoating();
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
		if (objective == null) return 0f;
		Objective obj = objective.getObjective();
		if (obj == null) return 0f;
		return 0f;
	}
	
	/**
	 * Sets the name of the stage label.
	 * 
	 * @param name The value to set.
	 */
	public void setLabelName(String name)
	{
		if (label == null) label = new StageLabelI();
		label.setName(omero.rtypes.rstring(name));
	}
	
	/**
	 * Sets the x-position
	 * 
	 * @param value The value to set.
	 */
	public void setPositionX(float value)
	{
		if (label == null) label = new StageLabelI();
		label.setPositionX(omero.rtypes.rfloat(value));
	}
	
	/**
	 * Sets the y-position
	 * 
	 * @param value The value to set.
	 */
	public void setPositionY(float value)
	{
		if (label == null) label = new StageLabelI();
		label.setPositionY(omero.rtypes.rfloat(value));
	}
	
	/**
	 * Sets the y-position
	 * 
	 * @param value The value to set.
	 */
	public void setPositionZ(float value)
	{
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
		if (objective != null) 
			objective.setCorrectionCollar(omero.rtypes.rfloat(correction));
	}
	
	/**
	 * Sets the refractive index of the objective.
	 * 
	 * @param index The value to set.
	 */
	public void setRefractiveIndex(float index)
	{
		if (objective != null) 
			objective.setRefractiveIndex(omero.rtypes.rfloat(index));
	}
	
}
