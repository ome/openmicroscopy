/*
 * ome.formats.testclient.ProjectCreator
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.util;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.system.ServiceFactory;

public class ProjectCreator
{

    private static final String USERNAME = "root";

    private static final String PASSWORD = "ome";

    private static final String SERVER   = "mage";

    private static final String PORT     = "1099";

    public static void main(String[] args)
    {
        System.getProperties().setProperty("omero.user", USERNAME);
        System.getProperties().setProperty("omero.pass", PASSWORD);
        System.getProperties().setProperty("server.host", SERVER);
        System.getProperties().setProperty("server.port", PORT);

        ServiceFactory sf = new ServiceFactory();

        Project p = new Project();
        p.setName("Importer Project");
        p.setDescription("Sets of images that were imported using the"
                + "BioFormats based test importer.");
        Dataset chrisD = new Dataset();
        chrisD.setName("Chris' import dataset");
        Dataset brianD = new Dataset();
        brianD.setName("Brian's import dataset");
        p.linkDataset(chrisD);
        p.linkDataset(brianD);
        sf.getUpdateService().saveObject(p);
    }
}
