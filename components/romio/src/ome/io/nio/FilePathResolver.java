/*
 * ome.io.nio.FilePathResolver
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import ome.model.core.Pixels;

/**
 * Resolver for file paths.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO4.3
 */
public interface FilePathResolver
{
    String getOriginalFilePath(AbstractFileSystemService service,
                               Pixels pixels);
}
