/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import java.util.Collections;
import java.util.List;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import omero.cmd.Delete2;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import static org.testng.AssertJUnit.*;

/**
 * Collections of tests for the <code>Delete</code> service. This carries on
 * from {@link DeleteServiceTest}
 *
 * @since 4.2.1
 */
@Test(groups = "ticket:2615")
public class RelatedToTest extends AbstractServerTest {

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

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(i1.getId().getValue()));
        callback(true, client, dc);

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
        assertEquals(pixels.getId().getValue(), pixels2.getId().getValue());

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        callback(true, client, dc);

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
        assertEquals(pixels.getId().getValue(), pixels2.getId().getValue());

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img2.getId().getValue()));
        callback(true, client, dc);

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
