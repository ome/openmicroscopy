/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import integration.AbstractTest;
import integration.DeleteServiceTest;
import omero.api.delete.DeleteCommand;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>Delete</code> service. This carries on
 * from {@link DeleteServiceTest}
 *
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class RelatedToTest extends AbstractTest {

    @Test(groups = { "ticket:1228", "ticket:2776" })
    public void testDeleteWithProjectionRemovesRelatedTo() throws Exception {

        newUserAndGroup("rw----");
        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        Pixels p1 = i1.getPixels(0);
        Image i2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        Pixels p2 = i2.getPixels(0);

        p2.setRelatedTo(p1);
        p2 = (Pixels) iUpdate.saveAndReturnObject(p2);
        assertEquals(p1.getId(), p2.getRelatedTo().getId());

        delete(client, new DeleteCommand(DeleteServiceTest.REF_IMAGE, i1
                .getId().getValue(), null));

        assertDoesNotExist(i1);
        assertDoesNotExist(p1);
        assertExists(i2);
        assertExists(p2);

    }

    /**
     * Test to control if the related pixels set is to <code>null</code> when
     * deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2776")
    public void testPixelsRelatedTo() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels1 = img1.getPrimaryPixels();
        Pixels pixels2 = img2.getPrimaryPixels();
        pixels1.setRelatedTo(pixels2);
        pixels1 = (Pixels) iUpdate.saveAndReturnObject(pixels1);
        Pixels pixels = pixels1.getRelatedTo();
        assertNotNull(pixels);
        assertTrue(pixels.getId().getValue() == pixels2.getId().getValue());
        delete(client, new DeleteCommand(DeleteServiceTest.REF_IMAGE, img2
                .getId().getValue(), null));

        String sql = "select i from Image i where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(img2.getId().getValue());
        assertNull(iQuery.findByQuery(sql, param));
        sql = "select i from Pixels i where i.id = :id";
        param = new ParametersI();
        param.addId(pixels2.getId().getValue());
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select i from Pixels i where i.id = :id";
        param = new ParametersI();
        param.addId(pixels1.getId().getValue());
        pixels1 = (Pixels) iQuery.findByQuery(sql, param);
        assertNull(pixels1.getRelatedTo());
    }

    /**
     * Test to control if the related pixels set is to <code>null</code> when
     * deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2776")
    public void testPixelsRelatedToUsingDeleteImage() throws Exception {
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels1 = img1.getPrimaryPixels();
        Pixels pixels2 = img2.getPrimaryPixels();
        pixels1.setRelatedTo(pixels2);
        pixels1 = (Pixels) iUpdate.saveAndReturnObject(pixels1);
        Pixels pixels = pixels1.getRelatedTo();
        assertNotNull(pixels);
        assertTrue(pixels.getId().getValue() == pixels2.getId().getValue());
        iDelete.deleteImage(img2.getId().getValue(), true);

        String sql = "select i from Image i where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(img2.getId().getValue());
        assertNull(iQuery.findByQuery(sql, param));
        sql = "select i from Pixels i where i.id = :id";
        param = new ParametersI();
        param.addId(pixels2.getId().getValue());
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select i from Pixels i where i.id = :id";
        param = new ParametersI();
        param.addId(pixels1.getId().getValue());
        pixels1 = (Pixels) iQuery.findByQuery(sql, param);
        assertNull(pixels1.getRelatedTo());
    }

}
