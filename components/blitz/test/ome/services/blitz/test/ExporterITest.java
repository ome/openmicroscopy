/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import ome.services.blitz.impl.ExporterI;
import omero.api.AMD_Exporter_addImage;
import omero.model.Image;
import omero.model.ImageI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ExporterITest extends AbstractServantTest {

    ExporterI user_e, root_e;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        user_e = new ExporterI(be);
        user_e.setServiceFactory(user_sf);

        root_e = new ExporterI(be);
        root_e.setServiceFactory(root_sf);
    }

    //
    // XML Generation
    //

    @Test
    public void testSimpleXml() throws Exception {
        ExporterI.Retrieve retrieve = new ExporterI.Retrieve();
        retrieve.addImage(new ImageI());
        String xml = ExporterI.generateXml(retrieve);
        System.out.println(xml);
    }

    //
    // export
    //

    @Test
    public void testForDatabaseUuid() throws Exception {
        fail("NYI");
    }

    @Test
    public void testBasicExport() throws Exception {
        Image i = new ImageI();
        i.setAcquisitionDate(rtime(0));
        i.setName(rstring("basic export"));
        i = assertSaveAndReturn(i);
        assertAddImage(i.getId().getValue());

    }

    // Helpers
    // =========================================================================

    private void assertAddImage(long id) throws Exception {

        final RV rv = new RV();
        user_e.addImage_async(new AMD_Exporter_addImage() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response() {
                rv.rv = null;
            }
        }, id, null);
        rv.assertPassed();
    }

}
