/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.old;

import java.io.File;

import static omero.rtypes.*;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.Image;
import omero.model.ImageI;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleWorflowTest extends IceTest {

    omero.client ice;
    long imageId;

    @BeforeMethod
    public void createServiceFactory() throws Exception {
        File f1 = ResourceUtils.getFile("ice.config");
        File f2 = ResourceUtils.getFile("local.properties");
        ice = new omero.client(f1, f2);
        ice.createSession(null, null);
    }

    @AfterMethod
    public void destroyServiceFactory() {
        ice.closeSession();
    }

    @Test(groups = "first")
    public void createData() throws Exception {
        IUpdatePrx prx = ice.getServiceFactory().getUpdateService();

        Image i = new ImageI();
        i.setName(rstring("simpleworkflowtest"));
        i = (Image) prx.saveAndReturnObject(i, null);
        imageId = i.getId().getValue();
    }

    @Test(dependsOnGroups = "first")
    public void checksData() throws Exception {
        IQueryPrx prx = ice.getServiceFactory().getQueryService();

        Image i = (Image) prx.get(Image.class.getName(), imageId);
        assertTrue("simpleworkflowtest".equals(i.getName().getValue()));
    }
}
