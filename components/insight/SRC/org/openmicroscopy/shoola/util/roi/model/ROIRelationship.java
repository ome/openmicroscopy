/*
 *  org.openmicroscopy.shoola.util.roi.model.ROIRelationship 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.roi.model;

import org.openmicroscopy.shoola.util.roi.model.attachment.AttachmentMap;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class ROIRelationship 
{
	long id;

	private String 			description;
	private AttachmentMap	attachments;
	
	private ROI				parent;
	private	ROI  			child;
	
	ROIRelationship(long id, ROI parent, ROI child)
	{
		this.id = id;
		attachments = new AttachmentMap();
		this.parent = parent;
		this.child = child;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setChild(ROI child)
	{
		this.child = child;
	}
	
	public ROI getParent()
	{
		return parent;
	}
	
	public ROI getChild()
	{
		return child;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String desc)
	{
		description = desc;
	}
	
	public AttachmentMap getAttachmentMap()
	{
		return attachments;
	}
}


