/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


import omero.model.IObject;
import omero.gateway.model.ExperimenterData;

/** 
 * 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class AnnotationLinkData 
{
    
    /** The child of the link.*/
    private omero.gateway.model.DataObject child;
    
    /** The parent of the link.*/
    private omero.gateway.model.DataObject parent;
    
    /** The original link.*/
    private IObject link;
    
    private ExperimenterData owner;
	/**
	 * Creates a new instance.
	 * 
	 * @param link
	 */
	public AnnotationLinkData(IObject link, 
	        omero.gateway.model.DataObject child, omero.gateway.model.DataObject parent)
	{
		this.link = link;
		this.child = child;
		this.parent = parent;
	}
	
	public ExperimenterData getOwner()
	{ 
		if (owner == null)
			owner = new ExperimenterData(link.getDetails().getOwner());
		return owner;
	}
	
	/**
	 * Returns the parent of the link.
	 * 
	 * @return See above.
	 */
	public omero.gateway.model.DataObject getParent()
	{
		return parent;
	}
	
	/**
	 * Returns the child of the link.
	 * 
	 * @return See above.
	 */
	public omero.gateway.model.DataObject getChild()
	{
		return child;
	}

	/**
	 * Returns the link.
	 * 
	 * @return See above.
	 */
	public IObject getLink() { return link; }
	
	/**
	 * Returns <code>true</code> if the link can be deleted, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean canDelete()
	{
		return link.getDetails().getPermissions().canDelete();
	}

}
