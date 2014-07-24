/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static org.testng.AssertJUnit.assertFalse;

import omero.api.SearchPrx;
import omero.model.Image;
import omero.model.Project;

import org.testng.annotations.Test;

/**
 * Collection of tests for the <code>Search</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class SearchServiceTest extends AbstractServerTest {

    /**
     * Tests the <code>byFullText</code> method for image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testByFullTextImageType() throws Exception {
        Image p = (Image) factory.getUpdateService().saveAndReturnObject(
                mmFactory.simpleImage());
        String name = p.getName().getValue();
        SearchPrx svc = factory.createSearchService();
        svc.onlyType(Image.class.getName());
        svc.byFullText(name);
        // assertTrue(svc.hasNext());
        svc.close();
    }

    /**
     * Tests the <code>byFullText</code> method for project.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testByFullTextProjectType() throws Exception {
        Image p = (Image) factory.getUpdateService().saveAndReturnObject(
                mmFactory.simpleImage());
        String name = p.getName().getValue();
        SearchPrx svc = factory.createSearchService();
        svc.onlyType(Project.class.getName());
        svc.byFullText(name);
        assertFalse(svc.hasNext());
        svc.close();
    }

}
