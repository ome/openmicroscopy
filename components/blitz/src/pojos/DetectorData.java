/*
 * pojos.DetectorData
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package pojos;

//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.RDouble;
import omero.RString;
import omero.model.Detector;
import omero.model.DetectorType;

/**
 * Hosts a detector.
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
public class DetectorData 
	extends DataObject
{

	/**
	 * Creates a new instance.
	 * 
	 * @param detector The detector to host. Mustn't be <code>null</code>.
	 */
	public DetectorData(Detector detector)
	{
		if (detector == null)
			throw new IllegalArgumentException("Detector cannot null.");
		setValue(detector);
	}
	
	/**
	 * Returns the voltage of the detector.
	 * 
	 * @return See above
	 */
	public Double getVoltage()
	{
		Detector detector = (Detector) asIObject();
		RDouble value = detector.getVoltage();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the amplification gain of the detector.
	 * 
	 * @return See above
	 */
	public Double getAmplificationGain()
	{
		Detector detector = (Detector) asIObject();
		RDouble value = detector.getAmplificationGain();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the gain of the detector.
	 * 
	 * @return See above
	 */
	public Double getGain()
	{
		Detector detector = (Detector) asIObject();
		RDouble value = detector.getGain();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public Double getOffset()
	{
		Detector detector = (Detector) asIObject();
		RDouble value = detector.getOffsetValue();
		if (value == null) return null;
		return value.getValue();
	}

	/**
	 * Returns the offset of the detector.
	 * 
	 * @return See above
	 */
	public Double getZoom()
	{
		Detector detector = (Detector) asIObject();
		RDouble value = detector.getZoom();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the type of the detector.
	 * 
	 * @return See above.
	 */
	public String getType()
	{
		Detector detector = (Detector) asIObject();
		DetectorType type = detector.getType();
		if (type == null) return "";
		return type.getValue().getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		Detector detector = (Detector) asIObject();
		RString value = detector.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer of the detector.
	 * 
	 * @return See above.
	 */
	public String getModel()
	{
		Detector detector = (Detector) asIObject();
		RString value = detector.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the serial number of the detector.
	 * 
	 * @return See above.
	 */
	public String getSerialNumber()
	{
		Detector detector = (Detector) asIObject();
		RString value = detector.getSerialNumber();
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
		Detector detector = (Detector) asIObject();
		RString value = null;//detector.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
}
