/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import ome.services.blitz.impl.ExporterI;
import ome.services.blitz.impl.OmeroMetadata;
import ome.services.db.DatabaseIdentity;
import omero.api.AMD_Exporter_addImage;
import omero.api.AMD_Exporter_generateTiff;
import omero.api.AMD_Exporter_generateXml;
import omero.api.AMD_Exporter_read;
import omero.api.AMD_IConfig_getDatabaseUuid;
import omero.model.Image;
import omero.model.ImageI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class ExporterITest extends AbstractServantTest {

    DatabaseIdentity db = new DatabaseIdentity("test", "test");
    ExporterI user_e, root_e;

    @Override
    @BeforeMethod
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
        ExporterI exporter = new ExporterI(null, null);
        String xml = exporter.generateXml(retrieve);
        assertNotNull(xml);
    }

    //
    // export
    //

    @Test
    public void testForDatabaseUuid() throws Exception {
        final RV rv = new RV();
        user_config.getDatabaseUuid_async(new AMD_IConfig_getDatabaseUuid(){

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(String __ret) {
                rv.rv = __ret;
            }}, current("getDatabaseUuid"));
        rv.assertPassed();
        assertNotNull(rv.rv);
    }

    @Test
    public void testForExistingExternalInfo() throws Exception {
        fail("NYI ~ all queries will need to load them.");
    }

    @Test
    public void testBasicExport() throws Exception {
        Image i = assertNewImage();
        assertAddImage(i.getId().getValue());
        assertGenerateXml();
        byte[] buf = assertRead(0, 1024 * 1024);
        assertNotNull(buf);
        assertTrue(buf.length > 1);

        // Now let's compare the XML
        String xml1 = new String(buf);
        OmeroMetadata retrieve = new OmeroMetadata(db);
        retrieve.addImage(i);
        ExporterI exporter = new ExporterI(null, null);
        String xml2 = exporter.generateXml(retrieve);
        System.out.println(xml1);
        assertEquals(xml1, xml2);

        // After reading, nothing should be returned
        buf = assertRead(0, 1024 * 1024);
        assertEquals(0, buf.length);
    }

//    Used for manually testing
//    @Test(enabled = false)
//    public void testBasicExportDicom() throws Exception {
//        assertAddImage(root_e, 701);
//        assertGenerateTiff(root_e);
//    }

    @Test
    public void testTiffGeneration() throws Exception {
        Image i = assertNewImage();
        assertAddImage(i.getId().getValue());
        long size = assertGenerateTiff();
        byte[] buf = assertRead(0, (int)size);
        assertTrue(size > 0);
        assertEquals(size, buf.length);
        
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
    
    private long assertGenerateXml() throws Exception {

        final RV rv = new RV();
        user_e.generateXml_async(new AMD_Exporter_generateXml() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(long val) {
                rv.rv = val;
            }
        }, null);
        rv.assertPassed();
        return ((Long)rv.rv).longValue();
    }

    private long assertGenerateTiff() throws Exception {
        return assertGenerateTiff(user_e);
    }        
    
    private long assertGenerateTiff(ExporterI e) throws Exception {

        final RV rv = new RV();
        e.generateTiff_async(new AMD_Exporter_generateTiff() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(long val) {
                rv.rv = val;
            }
        }, null);
        rv.assertPassed();
        return ((Long)rv.rv).longValue();
    }

    private void assertAddImage(long id) throws Exception {
        assertAddImage(user_e, id);
    }
    
    private void assertAddImage(ExporterI e, long id) throws Exception {

        final RV rv = new RV();
        e.addImage_async(new AMD_Exporter_addImage() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response() {
                rv.rv = null;
            }
        }, id, null);
        rv.assertPassed();
    }

    private byte[] assertRead(long pos, int size) throws Exception {

        final RV rv = new RV();
        user_e.read_async(new AMD_Exporter_read() {

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(byte[] buf) {
                rv.rv = buf;
            }
        }, pos, size, null);
        rv.assertPassed();
        return (byte[]) rv.rv;
    }

}
