/*
 * org.openmicroscopy.shoola.util.roi.model.attachment.AttachmentMap 
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

package org.openmicroscopy.shoola.util.roi.model.attachment;

import java.util.HashMap;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class AttachmentMap 
{
	private static final int MAPSIZE = 11;
	private HashMap<AttachmentKey, Attachment> attachmentMap;
	
	public AttachmentMap()
	{
		attachmentMap = new HashMap<AttachmentKey, Attachment>(MAPSIZE);
	}
	
	public void addAttachment(AttachmentKey key, Attachment attachment)
	{
		attachmentMap.put(key, attachment);
	}
	
	public Attachment getAttachment(AttachmentKey key)
	{
		return attachmentMap.get(key);
	}
	
}


