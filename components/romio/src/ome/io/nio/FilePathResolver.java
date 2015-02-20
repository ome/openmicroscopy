/*
 * ome.io.nio.FilePathResolver
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import ome.model.core.Pixels;

/**
 * Resolver for file paths and related metadata.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO4.3
 */
public interface FilePathResolver
{
    /**
     * Retrieves the original file path for a given set of pixels.
     * @param service File system service which contains methods to resolve
     * the root and directory structure of the path.
     * @param pixels Pixels set to retrieve an original file path for.
     * @return Absolute path to the original file for the set of pixels.
     */
    String getOriginalFilePath(AbstractFileSystemService service,
                               Pixels pixels);
}
