/*
 * ome.services.RawFileStore
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

package ome.api;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.security.RolesAllowed;

import ome.annotations.NotNull;
import ome.conditions.ResourceError;

/** 
 * Raw file gateway which provides access to the OMERO file repository.  
 *
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/08 15:21:59 $)
 * </small>
 * @since OMERO3.0
 */
public interface RawFileStore extends StatefulServiceInterface
{
    /**
     * This method manages the state of the service.
     * @param fileId an {@link ome.model.core.OriginalFile} id.
     */
    public void setFileId(long fileId);
    
    /**
	 * Delegates to {@link ome.io.nio.FileBuffer}
	 * 
	 * @see ome.io.nio.FileBuffer#read(java.nio.ByteBuffer, long)
	 */
    public byte[] read(long position, int length);

	/**
     * Delegates to {@link ome.io.nio.FileBuffer}
     * 
     * @see ome.io.nio.FileBuffer#write(java.nio.ByteBuffer, long)
     */
	public void write(byte[] buf, long position, int length);
}
