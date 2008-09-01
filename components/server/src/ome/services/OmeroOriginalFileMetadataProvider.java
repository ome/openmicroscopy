/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services;

import java.util.List;

import ome.api.IQuery;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.parameters.Parameters;

/**
 * OMERO implementation of an original file metadata provider, uses {@link
 * ome.api.IQuery}.
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO-Beta3.1
 */
public class OmeroOriginalFileMetadataProvider
	implements OriginalFileMetadataProvider
{
	/** Query service. */
	private IQuery iQuery;
	
	/**
	 * Constructor.
	 * @param iQuery OMERO query service instance.
	 */
	public OmeroOriginalFileMetadataProvider(IQuery iQuery)
	{
		this.iQuery = iQuery;
	}

	/* (non-Javadoc)
	 * @see ome.io.nio.OriginalFileMetadataProvider#getOriginalFileWhereFormatStartsWith(ome.model.core.Pixels, java.lang.String)
	 */
	public OriginalFile getOriginalFileWhereFormatStartsWith(Pixels pixels,
			                                                 String s)
	{
		String hql = String.format(
			"select f from OriginalFile f " +
			"left outer join f.pixelsFileMaps as map " +
			"where f.format.value like '%s%%' and map.child.id = :id", s);
		Parameters p = new Parameters();
		p.addId(pixels.getId());
		List<OriginalFile> originalFiles = iQuery.findAllByQuery(hql, p);
		return originalFiles.size() > 0? originalFiles.get(0) : null;
	}

}
