/*
 * ome.io.nio.OriginalFilesService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.FileNotFoundException;

import ome.model.core.OriginalFile;

/**
 * Raw file service which provides access to <code>FileBuffers</code>.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1.2 $ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
public class OriginalFilesService extends AbstractFileSystemService {

    public OriginalFilesService(String path) {
        super(path);
    }

    public FileBuffer getFileBuffer(OriginalFile file) {
        return new FileBuffer(getFilesPath(file.getId()), file);
    }
}
