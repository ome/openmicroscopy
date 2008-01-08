/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;

import ome.model.containers.Dataset;
import ome.model.containers.Project;


public class ImportContainer
{
        public File file;
        public Long projectID;
        public Dataset dataset; 
        public String imageName;
        public boolean archive;
        
        public ImportContainer(File file, Long projectID, Dataset dataset, String imageName, boolean archive2)
        {
            this.file = file;
            this.projectID = projectID;
            this.dataset = dataset;
            this.imageName = imageName;
            this.archive = archive2;
        }
}
