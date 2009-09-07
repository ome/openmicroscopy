/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import ome.services.blitz.impl.ExporterI;
import ome.services.blitz.impl.OmeroMetadata;
import ome.services.db.DatabaseIdentity;
import omero.api.AMD_Exporter_addImage;
import omero.api.AMD_Exporter_getBytes;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.PixelsI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ExporterITest extends AbstractServantTest {

    DatabaseIdentity db = new DatabaseIdentity("test", "test");
    ExporterI user_e, root_e;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        user_e = new ExporterI(be, db);
        user_e.setServiceFactory(user_sf);

        root_e = new ExporterI(be, db);
        root_e.setServiceFactory(root_sf);
    }

    //
    // XML Generation
    //

    @Test
    public void testSimpleXml() throws Exception {
        OmeroMetadata retrieve = new OmeroMetadata(db);
        retrieve.addImage(new ImageI());
        String xml = ExporterI.generateXml(retrieve);
        assertNotNull(xml);
    }

    //
    // export
    //

    @Test
    public void testForDatabaseUuid() throws Exception {
        fail("NYI");
    }

    @Test
    public void testForExistingExternalInfo() throws Exception {
        fail("NYI ~ all queries will need to load them.");
    }

    @Test
    public void testBasicExport() throws Exception {
        Image i = assertNewImage();
        assertAddImage(i.getId().getValue());
        byte[] buf = assertGetBytes(1024 * 1024);
        assertNotNull(buf);
        assertTrue(buf.length > 1);

        // Now let's compare the XML
        String xml1 = new String(buf);
        OmeroMetadata retrieve = new OmeroMetadata(db);
        retrieve.addImage(i);
        String xml2 = ExporterI.generateXml(retrieve);
        System.out.println(xml1);
        assertEquals(xml1, xml2);

        // After reading, nothing should be returned
        buf = assertGetBytes(1024 * 1024);
        assertEquals(0, buf.length);
    }

    // Helpers
    // =========================================================================

    private Image assertNewImage() throws Exception {
        long id = this.makePixels();
        Image i = (Image) assertFindByQuery(
                "select i from Image i join fetch i.pixels p where p.id = "
                        + id, null).get(0);
        // Image i = new ImageI();
        // i.setAcquisitionDate(rtime(0));
        // i.setName(rstring("basic export"));
        // i.addPixels(new PixelsI(id, false));
        // i = assertSaveAndReturn(i);
        return i;
    }

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

    private byte[] assertGetBytes(int size) throws Exception {

        final RV rv = new RV();
        user_e.getBytes_async(new AMD_Exporter_getBytes() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(byte[] buf) {
                rv.rv = buf;
            }
        }, size, null);
        rv.assertPassed();
        return (byte[]) rv.rv;
    }

}
