/*
 * org.openmicroscopy.shoola.env.data.model.LookupTableData
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * A lookup table object
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 *
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LookupTableData extends OMEDataObject
{

	private List 				entries;
		
	public LookupTableData() {}
	
	public LookupTableData(int id, String name, String description, 
						List entries) 
	{
		super(id,name,description);		
		this.entries = entries;
		
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new LookupTableData(); }
	
	public List getEntries() { return entries; }
	
	public void setEntries(List entries) {
		this.entries = entries;
	}
}
