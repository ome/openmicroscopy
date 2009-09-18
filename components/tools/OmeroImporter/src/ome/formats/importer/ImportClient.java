/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.support;

import java.io.File;
import java.util.List;
import java.util.Map;

import ome.formats.model.InstanceProvider;
import omero.model.OriginalFile;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public interface ImportClient {

    public void close();

    public Object getFilteredCompanionFiles();

    public void writeFilesToFileStore(List<File> fileNameList,
            Map<String, OriginalFile> originalFileMap);

    public void updatePixels(List<Pixels> pixList);

    public void populateMinMax();

    public void resetDefaultsAndGenerateThumbnails(List<Long> plateIds,
            List<Long> pixelsIds);

    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t);

    public long getExperimenterID();

    public InstanceProvider getInstanceProvider();

}
