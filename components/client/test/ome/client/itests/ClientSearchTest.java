/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import junit.framework.TestCase;
import ome.api.Search;
import ome.model.core.Image;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "manual" })
public class ClientSearchTest extends TestCase {

    ServiceFactory sf = new ServiceFactory();
    Search search;

    @Test
    public void testBasics() throws Exception {
        search = sf.createSearchService();
        search.onlyType(Image.class);
        search.byFullText("root");
        assertTrue(search.hasNext());
        search.close();
    }

}
