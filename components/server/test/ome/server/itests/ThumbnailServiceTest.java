/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.ThumbnailStore;
import ome.model.core.Pixels;
import ome.model.enums.RenderingModel;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;

import org.testng.annotations.Test;

/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 *
 */
public class ThumbnailServiceTest extends AbstractManagedContextTest {

    class Fixture {

        Pixels pixels;
        long pixelsID;
        long userID;
        RenderingEngine re;

        public Fixture() {
            pixels = makePixels();
            pixelsID = pixels.getId();
            userID = iAdmin.getEventContext().getCurrentUserId();

            re = factory.createRenderingEngine();
            re.lookupPixels(pixelsID);
            if (!(re.lookupRenderingDef(pixelsID))) {
               re.resetDefaults();
               re.lookupRenderingDef(pixelsID);
            }
            re.load();
        }
    }

    //
    // XXX: Many other *basic* thumbnail tests are in RenderingSessionTest
    //

    @Test(groups = {"ticket:2075"})
    public void testUserViewsOwnUpdatedThumbnailsByLongestSideSet()
        throws Exception {
        loginNewUser();
        Pixels pix1 = makePixels();
        Pixels pix2 = makePixels();
        Pixels pix3 = makePixels();
        Set<Long> pixelsIds = new HashSet<Long>();
        pixelsIds.add(pix1.getId());
        pixelsIds.add(pix2.getId());
        pixelsIds.add(pix3.getId());
        final ServiceFactory sf = this.factory;
        RenderingEngine re = sf.createRenderingEngine();
        ThumbnailStore tb = sf.createThumbnailService();
        Map<Long, byte[]> before = 
            tb.getThumbnailByLongestSideSet(96, pixelsIds);
        // Retrieve RGB rendering model
        List<RenderingModel> models = re.getAvailableModels();
        RenderingModel rgbModel = getModel(models, "rgb");
        // Make pix1 red
        re.lookupPixels(pix1.getId());
        re.lookupRenderingDef(pix1.getId());
        re.load();
        re.setModel(rgbModel);
        re.setRGBA(0, 255, 0, 0, 255);
        re.saveCurrentSettings();
        // Make pix2 green
        re.lookupPixels(pix2.getId());
        re.lookupRenderingDef(pix2.getId());
        re.load();
        re.setModel(rgbModel);
        re.setRGBA(0, 0, 255, 0, 255);
        re.saveCurrentSettings();
        // Make pix3 blue
        re.lookupPixels(pix3.getId());
        re.lookupRenderingDef(pix3.getId());
        re.load();
        re.setModel(rgbModel);
        re.setRGBA(0, 0, 0, 255, 255);
        re.saveCurrentSettings();
        Map<Long, byte[]> after =
            tb.getThumbnailByLongestSideSet(96, pixelsIds);
        assertEquals(before.size(), after.size());
        assertTrue(before.keySet().equals(after.keySet()));
        Set<Integer> afterThumbnailLengths = new HashSet<Integer>();
        for (Long pixelsId : before.keySet()) {
            byte[] beforeThumbnail = before.get(pixelsId);
            byte[] afterThumbnail = after.get(pixelsId);
            assertTrue(beforeThumbnail.length != afterThumbnail.length);
            assertTrue(afterThumbnailLengths.add(afterThumbnail.length));
        }
    }

    @Test(groups = {"ticket:2075"})
    public void testUserViewsOwnUpdatedThumbnailByLongestSideSet()
        throws Exception {
        loginNewUser();
        Pixels pix = makePixels();
        final ServiceFactory sf = this.factory;
        RenderingEngine re = sf.createRenderingEngine();
        ThumbnailStore tb = sf.createThumbnailService();
        re.lookupPixels(pix.getId());
        assertTrue(re.lookupRenderingDef(pix.getId()));
        re.load();
        List<RenderingModel> models = re.getAvailableModels();
        RenderingModel rgbModel = getModel(models, "rgb");
        RenderingModel greyscaleModel = getModel(models, "greyscale");
        assertEquals(greyscaleModel.getId(), re.getModel().getId());
        Map<Long, byte[]> thumbnails = tb.getThumbnailByLongestSideSet(
                96, Collections.singleton(pix.getId()));
        assertEquals(1, thumbnails.size());
        byte[] before = thumbnails.get(pix.getId());
        assertNotNull(before);
        re.setModel(rgbModel);
        assertEquals(rgbModel.getId(), re.getModel().getId());
        re.saveCurrentSettings();
        thumbnails = tb.getThumbnailByLongestSideSet(
                96, Collections.singleton(pix.getId()));
        assertEquals(1, thumbnails.size());
        byte[] after = thumbnails.get(pix.getId());
        assertNotNull(after);
        assertTrue(before.length != after.length);
    }

    @Test(groups = {"ticket:2075"})
    public void testUserViewsOwnUpdatedThumbnailSet()
        throws Exception {
        loginNewUser();
        Pixels pix = makePixels();
        final ServiceFactory sf = this.factory;
        RenderingEngine re = sf.createRenderingEngine();
        ThumbnailStore tb = sf.createThumbnailService();
        re.lookupPixels(pix.getId());
        assertTrue(re.lookupRenderingDef(pix.getId()));
        re.load();
        List<RenderingModel> models = re.getAvailableModels();
        RenderingModel rgbModel = getModel(models, "rgb");
        RenderingModel greyscaleModel = getModel(models, "greyscale");
        assertEquals(greyscaleModel.getId(), re.getModel().getId());
        Map<Long, byte[]> thumbnails = tb.getThumbnailSet(
                96, 96, Collections.singleton(pix.getId()));
        assertEquals(1, thumbnails.size());
        byte[] before = thumbnails.get(pix.getId());
        assertNotNull(before);
        re.setModel(rgbModel);
        assertEquals(rgbModel.getId(), re.getModel().getId());
        re.saveCurrentSettings();
        thumbnails = tb.getThumbnailSet(
                96, 96, Collections.singleton(pix.getId()));
        assertEquals(1, thumbnails.size());
        byte[] after = thumbnails.get(pix.getId());
        assertNotNull(after);
        assertTrue(before.length != after.length);
    }

    private RenderingModel getModel(List<RenderingModel> models, String value) {
        for (RenderingModel model : models) {
            if (model.getValue().equals(value)) {
                return model;
            }
        }
        throw new RuntimeException("Could not find model: " + value);
    }
}
