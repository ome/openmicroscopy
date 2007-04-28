/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.util.ArrayList;

import junit.framework.TestCase;
import omero.model.ProjectDatasetLink;
import omero.model.Project;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;

import org.testng.annotations.Test;

public class AccessorsTest extends TestCase {

    @Test
    public void test_simple() throws Exception {
        ProjectI p_remote = new ProjectI();
        ProjectDatasetLinkI pdl_remote = new ProjectDatasetLinkI();
        p_remote.datasetLinks = new ArrayList<ProjectDatasetLink>();
        p_remote.datasetLinks.add(pdl_remote);
        assertTrue(p_remote.iterateDatasetLinks().next() == pdl_remote);
    }

}
