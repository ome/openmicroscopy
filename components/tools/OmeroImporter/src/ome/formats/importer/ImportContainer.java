/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;

public class ImportContainer
{
        public File file;
        public Long projectID;
        private Long datasetID; 
        public String imageName;
        public boolean archive;
        
        public ImportContainer(File file, Long projectID, Long datasetID, String imageName, boolean archive2)
        {
            this.file = file;
            this.projectID = projectID;
            this.datasetID = datasetID;
            this.imageName = imageName;
            this.archive = archive2;
        }
        
        public Long getDatasetId()
        {
            return datasetID;
        }
}
