/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests;

import ome.services.icy.client.IceServiceFactory;
import omero.RString;
import omero.api.IQueryPrx;
import omero.api.IQueryPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.constants.QUERYSERVICE;
import omero.constants.UPDATESERVICE;
import omero.model.Image;
import omero.model.ImageI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleWorflowTest extends IceTest {

    IceServiceFactory ice;
    long imageId;
    
    @BeforeMethod
    public void createServiceFactory() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
    }
    
    @AfterMethod
    public void destroyServiceFactory() {
        ice.destroy();
    }
    
    @Test( groups = "first" )
    public void createData() throws Exception {        
        IUpdatePrx prx = ice.getUpdateService(null);
        
        Image i = new ImageI();
        i.name = new RString(false, "simpleworkflowtest");
        i = (Image) prx.saveAndReturnObject(i, null);
        imageId = i.id.val;
    }
    
    @Test( dependsOnGroups = "first" )
    public void checksData() throws Exception {
        IQueryPrx prx = ice.getQueryService(null);
        
        Image i = (Image) prx.get(Image.class.getName(), imageId);
        assertTrue( "simpleworkflowtest".equals(i.name.val));
    }
}
