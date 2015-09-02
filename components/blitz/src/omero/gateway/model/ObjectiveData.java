/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import ome.model.units.BigResult;
import omero.RBool;
import omero.RDouble;
import omero.RString;
import omero.model.Correction;
import omero.model.Immersion;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Objective;
import omero.model.enums.UnitsLength;

/**
 * Hosts an objective used to capture an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ObjectiveData 
	extends DataObject
{

	/**
	 * Creates a new instance.
	 * 
	 * @param objective The objective. Mustn't be <code>null</code>.
	 */
	public ObjectiveData(Objective objective)
	{
		if (objective == null)
			throw new IllegalArgumentException("No objective.");
		setValue(objective);
	}
	
	/**
	 * Returns the working distance.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred 
	 */
	public Length getWorkingDistance(UnitsLength unit) throws BigResult
	{
		Objective obj = ((Objective) asIObject());
		Length l = obj.getWorkingDistance();
		if (l==null)
			return null;
		return unit == null ? l : new LengthI(l, unit);
	}
	
	/**
	 * Returns the working distance.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getWorkingDistance(UnitsLength)}
	 */
	@Deprecated
	public double getWorkingDistance()
	{
		Objective obj = ((Objective) asIObject());
		Length value = obj.getWorkingDistance();
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
		Objective obj = ((Objective) asIObject());
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
		Objective obj = ((Objective) asIObject());
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
		Objective obj = ((Objective) asIObject());
		RBool value = obj.getIris();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the correction value of the objective.
	 * 
	 * @return See above.
	 */
	public String getCorrection()
	{
		Objective obj = ((Objective) asIObject());
		Correction value = obj.getCorrection();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the objective's calibrated magnification factor.
	 * 
	 * @return See above.
	 */
	public double getCalibratedMagnification()
	{
		Objective obj = ((Objective) asIObject());
		RDouble value = obj.getCalibratedMagnification();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the objective's nominal magnification factor.
	 * 
	 * @return See above.
	 */
	public double getNominalMagnification()
	{
		Objective obj = ((Objective) asIObject());
		RDouble value = obj.getNominalMagnification();
		if (value == null) return -1;
		return value.getValue();
	}
	
	/**
	 * Returns the objective's LensNA.
	 * 
	 * @return See above.
	 */
	public double getLensNA()
	{
		Objective obj = ((Objective) asIObject());
		RDouble value = obj.getLensNA();
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
		Objective obj = ((Objective) asIObject());
		Immersion value = obj.getImmersion();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		Objective obj = ((Objective) asIObject());
		RString value = obj.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the lot number of the detector.
	 * 
	 * @return See above.
	 */
	public String getLotNumber()
	{
		Objective obj = ((Objective) asIObject());
		RString value = obj.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
}
