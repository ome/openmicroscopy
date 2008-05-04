/*
 * blitzgateway.service.FileServiceImpl 
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
package blitzgateway.service;


//Java imports
import java.util.ArrayList;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import ome.conditions.ApiUsageException;
import omero.model.Format;
import omero.model.OriginalFile;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.service.gateway.IQueryGateway;
import blitzgateway.service.gateway.RawFileStoreGateway;
import blitzgateway.util.ServiceUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class FileServiceImpl
	implements FileService
{	
	
	RawFileStoreGateway rawFileStore;
	IQueryGateway		iQuery;
	
	/**
	 * Create the FileService passing the gateway.
	 * @param gateway
	 */
	FileServiceImpl(RawFileStoreGateway fileStore, IQueryGateway query)
	{
		rawFileStore = fileStore;
		iQuery = query;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#fileExists(java.lang.String)
	 */
	public boolean fileExists(long id) throws DSAccessException,
			DSOutOfServiceException
	{
		rawFileStore.setFileId(id);	
		return rawFileStore.exists();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getOriginalFile(long)
	 */
	public OriginalFile getOriginalFile(long id) throws DSAccessException, 
			DSOutOfServiceException
	{
		return (OriginalFile)iQuery.findByQuery("from OriginalFile as o left outer " +
			"join fetch o.format as f where o.id = "+id);
	}
	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#findFile(java.lang.String, omero.model.Format)
	 */
	public List<Long> findFile(String fileName, Format fmt)
			throws DSAccessException, DSOutOfServiceException
	{
		String queryStr = "from OriginalFile as o left outer join fetch " +
				"o.format as f where f.value = " + fmt.value.val;
		List<OriginalFile> list = ServiceUtilities.
		collectionCast(OriginalFile.class, iQuery.findAllByQuery(queryStr));
		List<Long> lList = new ArrayList<Long>();
		for(OriginalFile file : list)
			lList.add(new Long(file.id.val));
		return lList;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getAllFormats()
	 */
	public List<Format> getAllFormats() throws DSAccessException,
			DSOutOfServiceException
	{
		return (List<Format>)
			ServiceUtilities.collectionCast(
				Format.class, iQuery.findAllByQuery("from Format as format"));
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getFileAsString(long)
	 */
	public String getFileAsString(long id) throws DSAccessException,
			DSOutOfServiceException
	{
		byte[] data = getRawFile(id);
		return new String(data);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getFileFormat(long)
	 */
	public Format getFileFormat(long id) throws DSAccessException,
			DSOutOfServiceException
	{
		OriginalFile file = getOriginalFile(id);
		return file.format;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getFormat(java.lang.String)
	 */
	public Format getFormat(String fmt) throws DSAccessException,
			DSOutOfServiceException
	{
		return (Format)iQuery.findByString("Format", "value", fmt);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#getRawFile(long)
	 */
	public byte[] getRawFile(long id) throws DSAccessException,
			DSOutOfServiceException
	{
		OriginalFile file = getOriginalFile(id);
		rawFileStore.setFileId(id);
		return rawFileStore.read(0, (int)file.size.val);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.FileService#fileExists(java.lang.Long)
	 */
	public boolean fileExists(Long id) throws DSAccessException,
			DSOutOfServiceException
	{
		rawFileStore.setFileId(id);
		return rawFileStore.exists();
	}

	
}


