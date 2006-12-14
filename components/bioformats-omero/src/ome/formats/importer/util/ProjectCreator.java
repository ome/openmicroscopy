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
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
