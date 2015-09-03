/*
 * pojos.DichroicData
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

import omero.RString;
import omero.model.Dichroic;

/**
 * Hosts the dichroic.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class DichroicData 
	extends DataObject
{

	/**
	 * Creates a new instance.
	 * 
	 * @param dichroic The dichroic to host. Mustn't be <code>null</code>.
	 */
	public DichroicData(Dichroic dichroic)
	{
        if (dichroic == null)
            throw new IllegalArgumentException("Dichroic cannot null.");
        setValue(dichroic);
	}
	
	/**
	 * Returns the manufacturer.
	 * 
	 * @return See above.
	 */
	public String getManufacturer()
	{
		Dichroic f = (Dichroic) asIObject();
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
		Dichroic f = (Dichroic) asIObject();
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
		Dichroic f = (Dichroic) asIObject();
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
		Dichroic f = (Dichroic) asIObject();
		RString value = f.getSerialNumber();
		if (value == null) return "";
		return value.getValue();
	}
	
}
