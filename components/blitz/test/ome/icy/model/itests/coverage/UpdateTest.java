/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import ome.icy.model.itests.IceTest;
import ome.services.icy.client.IceServiceFactory;
import omero.JString;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.constants.CONFIGSERVICE;
import omero.constants.UPDATESERVICE;

import org.testng.annotations.Test;

public class UpdateTest extends IceTest {

    @Test
    public void testSaveAndReturnObject() throws Exception {
        IUpdatePrx prx = ice.getUpdateService(null);
        assertNotNull(prx);

        omero.model.ImageI obj = new omero.model.ImageI();
        obj.name = new JString("foo");
        
        omero.model.CategoryImageLinkI link = new omero.model.CategoryImageLinkI();
        omero.model.CategoryI cat = new omero.model.CategoryI();
        cat.name = new JString("bar");
        link.parent = cat;
        link.child = obj;
        obj.categoryLinks.add(link);

        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);
        link = (omero.model.CategoryImageLinkI) obj.categoryLinks.get(0);
        cat = (omero.model.CategoryI) link.parent;
        
        assertTrue("foo".equals(obj.name));
        assertTrue(cat != null);
        assertTrue("bar".equals(cat.name));
    }

    @Test
    public void testDeleteObject() throws Exception {
        IUpdatePrx prx = ice.getUpdateService(null);
        assertNotNull(prx);

        omero.model.ImageI obj = new omero.model.ImageI();
        obj.name = new JString("foo");
        obj = (omero.model.ImageI) prx.saveAndReturnObject(obj);
        
        prx.deleteObject(obj);
        fail("need to query here");
    }
}
