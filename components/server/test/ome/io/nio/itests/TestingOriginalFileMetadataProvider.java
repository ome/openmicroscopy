/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import ome.io.nio.OriginalFileMetadataProvider;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;

public class TestingOriginalFileMetadataProvider
	implements OriginalFileMetadataProvider
{
	public OriginalFile getOriginalFileWhereFormatStartsWith(Pixels pixels,
			                                                 String s)
	{
		return null;
	}
}
