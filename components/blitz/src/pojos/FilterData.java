/*
 * pojos.FilterData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import omero.model.Filter;
import omero.model.FilterType;
import omero.model.Length;
import omero.model.TransmittanceRange;

/** 
 * Object hosting a filter.
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
public class FilterData 
	extends DataObject
{

	/**
	 * Creates a new instance.
	 * 
	 * @param filter The filter to host. Mustn't be <code>null</code>.
	 */
	public FilterData(Filter filter)
	{
        if (filter == null)
            throw new IllegalArgumentException("Filter cannot null.");
        setValue(filter);
	}
	
	/**
	 * Returns the cut in value or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Length getCutInAsLength()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null)
			return null;
		return range.getCutIn();
	}
	
	/**
	 * Returns the cut in value or <code>null</code>.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getCutInAsLength()}
	 */
	@Deprecated
	public Integer getCutIn()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) return null;
		Length value = range.getCutIn();
		if (value == null) return null;
		return (int) value.getValue();
	}
	
	/**
	 * Returns the cut in tolerance value or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Length getCutInToleranceAsLength()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) 
			return null;
		return range.getCutInTolerance();
	}
	
	/**
	 * Returns the cut in tolerance value or <code>null</code>.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getCutInToleranceAsLength()}
	 */
	@Deprecated
	public Integer getCutInTolerance()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) return null;
		Length value = range.getCutInTolerance();
		if (value == null) return null;
		return (int) value.getValue();
	}
	
	/**
	 * Returns the cut out value or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Length getCutOutAsLength()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null)
			return null;
		return range.getCutOut();
	}
	
	/**
	 * Returns the cut out value or <code>null</code>.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getCutOutAsLength()}
	 */
	@Deprecated
	public Integer getCutOut()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) return null;
		Length value = range.getCutOut();
		if (value == null) return null;
		return (int) value.getValue();
	}
	
	/**
	 * Returns the cut out tolerance value or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Length getCutOutToleranceAsLength()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) 
			return null;
		return range.getCutOutTolerance();
	}
	
	/**
	 * Returns the cut out tolerance value or <code>null</code>.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getCutOutToleranceAsLength()}
	 */
	@Deprecated
	public Integer getCutOutTolerance()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) return null;
		Length value = range.getCutOutTolerance();
		if (value == null) return null;
		return (int) value.getValue();
	}
	
	/**
	 * Returns the cut out tolerance value or <code>-1</code>.
	 * 
	 * @return See above.
	 */
	public Double getTransmittance()
	{
		Filter f = (Filter) asIObject();
		TransmittanceRange range = f.getTransmittanceRange();
		if (range == null) return null;
		RDouble value = range.getTransmittance();
		if (value == null) return null;
		return value.getValue();
	}
	
	/**
	 * Returns the manufacturer.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		Filter f = (Filter) asIObject();
		RString value = f.getManufacturer();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the model.
	 * 
	 * @return See above.
	 */
	public String getModel()
	{
		Filter f = (Filter) asIObject();
		RString value = f.getModel();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the lot number.
	 * 
	 * @return See above.
	 */
	public String getLotNumber()
	{
		Filter f = (Filter) asIObject();
		RString value = f.getLotNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the serial number.
	 * 
	 * @return See above.
	 */
	public String getSerialNumber()
	{
		Filter f = (Filter) asIObject();
		RString value = f.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the type. One out of a predefined list.
	 * 
	 * @return See above.
	 */
	public String getType()
	{
		Filter f = (Filter) asIObject();
		FilterType type = f.getType();
		if (type == null) return "";
		return type.getValue().getValue();
	}
	
	/**
	 * Returns the wheel.
	 * 
	 * @return See above.
	 */
	public String getFilterWheel()
	{
		Filter f = (Filter) asIObject();
		RString value = f.getFilterWheel();
		if (value == null) return "";
		return value.getValue();
	}
	
}
