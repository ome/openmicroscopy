/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.fake_login;

import static omero.rtypes.*;

import java.util.Collections;
import java.util.List;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.gui.GuiImporter;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Project;
import omero.model.ProjectI;

/**
 * Wrapper around the various importer applications.
 * 
 * @since Beta4.1
 */
public class Test {

    public static void main(String[] args) {
        ImportConfig config = new ImportConfig() {
            @Override
            public OMEROMetadataStoreClient createStore() {
                return new OMEROMetadataStoreClient(){
                    @Override
                    public List<Project> getProjects() {
                        return Collections.singletonList(addProject("a", "b"));
                    }
                    
                    @Override
                    public List<Dataset> getDatasets(Project p) {
                        return Collections.singletonList(addDataset("a", "b", p));
                    }
                    
                    @Override
                    public long getRepositorySpace() {
                        return 100000;
                    }
                    
                    @Override
                    public Project addProject(String projectName,
                            String projectDescription) {
                        Project p = new ProjectI();
                        p.setName(rstring(projectName));
                        p.setId(rlong(1));
                        return p;
                    }
                    
                    @Override
                    public Dataset addDataset(String datasetName,
                            String datasetDescription, Project project) {
                        Dataset d = new DatasetI();
                        d.setName(rstring(datasetName));
                        d.setId(rlong(1));
                        return d;
                    }
                    
                };
            }
        };
        new GuiImporter(config);
    }

}
