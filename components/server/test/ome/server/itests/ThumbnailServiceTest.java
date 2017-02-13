/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IPixels;
import ome.api.ThumbnailStore;
import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.RenderingModel;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
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
               re.resetDefaultSettings(true);
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

    @Test(groups = {"ticket:3161"})
    public void testTicket3161ThreeUserView() throws Exception {
        Experimenter e1 = loginNewUser();
        final ServiceFactory sf = this.factory;// new InternalServiceFactory();
        Pixels pix = makePixels();
        loginRoot();
        makeDefaultGroupReadWrite(e1);
        // Experimenter1's interactions
        loginUser(e1.getOmeName());
        ThumbnailStore ts = sf.createThumbnailService();
        IPixels ps = sf.getPixelsService();
        ts.setPixelsId(pix.getId());
        byte[] thumbExp1 = ts.getThumbnail(5, 5);
        assertNotNull(thumbExp1);
        List<IObject> settingsList = 
            ps.retrieveAllRndSettings(pix.getId(), -1);
        RenderingDef settings0 = (RenderingDef) settingsList.get(0);
        assertEquals(1, settingsList.size());
        assertEquals(1, settings0.getVersion().intValue());
        // Experimenter2's interactions
        Experimenter e2 = loginNewUserInOtherUsersGroup(e1);
        RenderingEngine re = sf.createRenderingEngine();
        ts = sf.createThumbnailService();
        re.lookupPixels(pix.getId());
        if (!re.lookupRenderingDef(pix.getId())) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pix.getId());
        }
        re.load();
        ts.setPixelsId(pix.getId());
        List<RenderingModel> models = re.getAvailableModels();
        re.setModel(getModel(models, "rgb"));
        re.saveCurrentSettings();
        byte[] thumbExp2 = ts.getThumbnail(5, 5);
        assertNotNull(thumbExp2);
        settingsList = ps.retrieveAllRndSettings(pix.getId(), -1);
        assertEquals(2, settingsList.size());
        settings0 = (RenderingDef) settingsList.get(0);
        RenderingDef settings1 = (RenderingDef) settingsList.get(1);
        assertEquals(1, settings0.getVersion().intValue());
        assertEquals(2, settings1.getVersion().intValue());
        byte[][] thumbnails = retrieveAllThumbnailsBySettings(
                settingsList, ts);
        assertEquals(2, thumbnails.length);
        // Experimenter3's interactions
        Experimenter e3 = loginNewUserInOtherUsersGroup(e1);
        ts = sf.createThumbnailService();
        ts.setPixelsId(pix.getId());
        ts.resetDefaults();
        ts.setPixelsId(pix.getId());
        byte[] thumbExp3 = ts.getThumbnail(5, 5);
        assertNotNull(thumbExp3);
        settingsList = 
            ps.retrieveAllRndSettings(pix.getId(), -1);
        settings0 = (RenderingDef) settingsList.get(0);
        settings1 = (RenderingDef) settingsList.get(1);
        RenderingDef settings2 = (RenderingDef) settingsList.get(2);
        assertEquals(3, settingsList.size());
        assertEquals(1, settings0.getVersion().intValue());
        assertEquals(2, settings1.getVersion().intValue());
        assertEquals(1, settings2.getVersion().intValue());
        thumbnails = retrieveAllThumbnailsBySettings(
                settingsList, ts);
        assertEquals(3, thumbnails.length);
    }

    @Test(groups = {"ticket:3161"})
    public void testTicket3161SecurityViolation() throws Exception {
        Experimenter e1 = loginNewUser();
        final ServiceFactory sf = this.factory;// new InternalServiceFactory();
        Pixels pix = makePixels();
        loginRoot();
        makeDefaultGroupReadWrite(e1);
        // Experimenter1's services
        loginUser(e1.getOmeName());
        RenderingEngine re = sf.createRenderingEngine();
        ThumbnailStore ts = sf.createThumbnailService();
        IPixels psExp1 = sf.getPixelsService();
        re.lookupPixels(pix.getId());
        if (!re.lookupRenderingDef(pix.getId())) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pix.getId());
        }
        re.load();
        List<RenderingModel> models = re.getAvailableModels();
        RenderingModel modelExp1 = re.getModel();
        ts.setPixelsId(pix.getId());
        byte[] thumbExp1 = ts.getThumbnail(5, 5);
        assertNotNull(thumbExp1);
        // Experimenter2's services
        Experimenter e2 = loginNewUserInOtherUsersGroup(e1);
        re = sf.createRenderingEngine();
        ts = sf.createThumbnailService();
        re.lookupPixels(pix.getId());
        if (!re.lookupRenderingDef(pix.getId())) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pix.getId());
        }
        re.load();
        RenderingModel modelExp2 = re.getModel();
        ts.setPixelsId(pix.getId());
        byte[] thumbExp2A = ts.getThumbnail(5, 5);
        assertNotNull(thumbExp2A);

        // Check rendering models are the same
        assertEquals(modelExp1.getId(), modelExp2.getId());
        // Check that the rendering settings count is two
        List<IObject> settingsList = 
            psExp1.retrieveAllRndSettings(pix.getId(), -1);
        assertEquals(2, settingsList.size());
        byte[][] thumbnails = retrieveAllThumbnailsBySettings(
                settingsList, ts);
        assertEquals(2, thumbnails.length);
        // Switch rendering model to check the settings
        re.setModel(getModel(models, "rgb"));
        re.saveCurrentSettings();
        settingsList = 
            psExp1.retrieveAllRndSettings(pix.getId(), -1);
        loginUser(e1.getOmeName());
        thumbnails = retrieveAllThumbnailsBySettings(
                settingsList, ts);
        assertEquals(2, thumbnails.length);
    }

    private byte[][] retrieveAllThumbnailsBySettings(
            List<IObject> settingsList, ThumbnailStore ts) {
        byte[][] toReturn = new byte[settingsList.size()][];
        int i = 0;
        for (IObject o : settingsList) {
            ts.setRenderingDefId(o.getId());
            toReturn[i] = ts.getThumbnail(5, 5);
            i++;
        }
        return toReturn;
    }

    private RenderingModel getModel(List<RenderingModel> models, String value) {
        for (RenderingModel model : models) {
            if (model.getValue().equals(value)) {
                return model;
            }
        }
        throw new RuntimeException("Could not find model: " + value);
    }

    private void makeDefaultGroupReadWrite(Experimenter experimenter)
    {
        ExperimenterGroup group = iAdmin.getDefaultGroup(experimenter.getId());
        group.getDetails().setPermissions(Permissions.GROUP_WRITEABLE);
        iAdmin.updateGroup(group);
    }

    private void makeDefaultGroupReadOnly(Experimenter experimenter)
    {
        ExperimenterGroup group = iAdmin.getDefaultGroup(experimenter.getId());
        group.getDetails().setPermissions(Permissions.GROUP_READABLE);
        iAdmin.updateGroup(group);
    }
}
