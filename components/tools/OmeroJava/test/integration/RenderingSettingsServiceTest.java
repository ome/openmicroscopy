/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.api.IRenderingSettingsPrx;
import omero.model.Channel;
import omero.model.ChannelBinding;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.RenderingDef;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>RenderingSettingsService</code> service.
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class RenderingSettingsServiceTest extends AbstractServerTest {

    /**
     * Create an entire plate, uploading binary data for all the images.
     *
     * After recent changes on the server to check for existing binary data for
     * pixels, many resetDefaults methods tested below began returning null
     * since {@link omero.LockTimeout} exceptions were being thrown server-side.
     * By using omero.client.forEachTile, we can set the necessary data easily.
     *
     * @see ticket:5755
     */
    public Plate createBinaryPlate(int rows, int cols, int fields,
            int acquisitions) throws Exception {
        Plate plate = mmFactory.createPlate(rows, cols, fields, acquisitions,
                true);
        plate = (Plate) iUpdate.saveAndReturnObject(plate);

        for (Well well : plate.copyWells()) {
            for (WellSample ws : well.copyWellSamples()) {
                Image image = createBinaryImage(ws.getImage());
                ws.setImage(image);
            }
        }

        return plate;

    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPixels() throws Exception {
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        // Pixels first
        List<Long> ids = new ArrayList<Long>();
        ids.add(pixels.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Pixels.class.getName(), ids);
        Assert.assertNotNull(v);
        // check if we have settings now.
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);

        // Image
        ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        v = prx.resetDefaultsInSet(Image.class.getName(), ids);
        Assert.assertNotNull(v);
        values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForImage() throws Exception {
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        // Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(image.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Image.class.getName(), ids);
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to set the default rendering settings for a set.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForDataset() throws Exception {
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();

        // create a dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        DatasetImageLink l = new DatasetImageLinkI();
        l.setChild(image);
        l.setParent(d);
        iUpdate.saveAndReturnObject(l);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));

        // Dataset
        List<Long> v = prx.resetDefaultsInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to set the default rendering settings for a project.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForProject() throws Exception {
        // create a project.
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        // create a dataset

        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.link(p, d);
        link = (ProjectDatasetLink) iUpdate.saveAndReturnObject(link);

        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), image);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Project.class.getName(),
                Arrays.asList(p.getId().getValue()));

        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Project.class.getName(), ids);
        ParametersI param;
        String sql;
        param = new ParametersI();
        ids.clear();
        ids.add(p.getId().getValue());
        param.addIds(ids);
        sql = "select pix from Pixels as pix " + "join fetch pix.image as i "
                + "join fetch pix.pixelsType "
                + "join fetch pix.channels as c "
                + "join fetch c.logicalChannel "
                + "join i.datasetLinks as dil " + "join dil.parent as d "
                + "left outer join d.projectLinks as pdl "
                + "left outer join pdl.parent as p " + "where p.id in (:ids)";
        Assert.assertEquals(iQuery.findAllByQuery(sql, param).size(), 1);

        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to set the default rendering settings for a screen.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForScreen() throws Exception {
        Screen screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);

        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild(p);
        link.setParent(screen);
        iUpdate.saveAndReturnObject(link);

        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Screen.class.getName(),
                Arrays.asList(screen.getId().getValue()));
        // Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(screen.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Screen.class.getName(), ids);
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to set the default rendering settings for a empty dataset. Tests
     * the <code>resetDefaultsInSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForEmptyDataset() throws Exception {
        // create a dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Dataset
        prx.setOriginalSettingsInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));
        List<Long> ids = new ArrayList<Long>();
        ids.add(d.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Dataset.class.getName(), ids);
        Assert.assertNotNull(v);
        Assert.assertTrue(v.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForImage() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Create a second image.
        Image image2 = createBinaryImage();
        Map<Boolean, List<Long>> m = prx
                .applySettingsToSet(id, Image.class.getName(),
                        Arrays.asList(image2.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to a collection of images within a
     * dataset. Tests the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForDataset() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Create a second image.
        Image image2 = createBinaryImage();
        // Create a dataset

        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        DatasetImageLink l = new DatasetImageLinkI();
        l.setChild(image2);
        l.setParent(d);
        iUpdate.saveAndReturnObject(l);

        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                Dataset.class.getName(), Arrays.asList(d.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to an empty dataset. Tests the
     * <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForEmptyDataset() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // create a dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        // Dataset
        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                Dataset.class.getName(), Arrays.asList(d.getId().getValue()));
        Assert.assertNotNull(m);
    }

    /**
     * Tests to apply the rendering settings to a collection of images contained
     * in a project. Tests the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForProject() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Create a second image.
        Image image2 = createBinaryImage();
        // Create a dataset
        // Link image and dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        Project project = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());

        ProjectDatasetLink pLink = new ProjectDatasetLinkI();
        pLink.link(project, d);
        iUpdate.saveAndReturnObject(pLink);

        DatasetImageLink link = new DatasetImageLinkI();
        link.link(new DatasetI(d.getId().getValue(), false), image2);
        iUpdate.saveAndReturnObject(link);

        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                Project.class.getName(),
                Arrays.asList(project.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to a plate. Tests the
     * <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForPlate() throws Exception {
        Plate p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Plate.class.getName(),
                Arrays.asList(p.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

        // Create a second plate
        p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        results = loadWells(p.getId().getValue(), true);
        well = (Well) results.get(0);
        Image image2 = well.getWellSample(0).getImage();
        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                Plate.class.getName(), Arrays.asList(p.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to a plate acquisition. Tests the
     * <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForPlateAcquisition() throws Exception {
        Plate p = createBinaryPlate(1, 1, 1, 1);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        WellSample ws = well.getWellSample(0);
        Image image = ws.getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(PlateAcquisition.class.getName(),
                Arrays.asList(ws.getPlateAcquisition().getId().getValue()));
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

        // Create a second plate
        p = createBinaryPlate(1, 1, 1, 1);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        results = loadWells(p.getId().getValue(), true);
        well = (Well) results.get(0);
        ws = well.getWellSample(0);
        Image image2 = ws.getImage();
        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                PlateAcquisition.class.getName(),
                Arrays.asList(ws.getPlateAcquisition().getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to a screen. Tests the
     * <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testApplySettingsToSetForScreen() throws Exception {
        Screen screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);

        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild(p);
        link.setParent(screen);
        link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);
        screen = link.getParent();
        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image

        prx.setOriginalSettingsInSet(Plate.class.getName(),
                Arrays.asList(p.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

        // Create a second plate
        p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);

        link = new ScreenPlateLinkI();
        link.setChild(p);
        link.setParent(screen);
        link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);

        results = loadWells(p.getId().getValue(), true);
        well = results.get(0);
        Image image2 = well.getWellSample(0).getImage();
        Map<Boolean, List<Long>> m = prx.applySettingsToSet(id,
                Screen.class.getName(),
                Arrays.asList(screen.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);

    }

    /**
     * Tests to reset the default rendering settings to a plate. Tests the
     * <code>ResetDefaultInSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPlate() throws Exception {
        Plate p = createBinaryPlate(1, 1, 1, 0);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Plate.class.getName(),
                Arrays.asList(p.getId().getValue()));
        // Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(p.getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(Plate.class.getName(), ids);
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to reset the default rendering settings to a plate acquisition.
     * Tests the <code>ResetDefaultInSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetForPlateAcquisition() throws Exception {
        Plate p = createBinaryPlate(1, 1, 1, 1);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        // load the well
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        WellSample ws = well.getWellSample(0);
        Image image = ws.getImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(PlateAcquisition.class.getName(),
                Arrays.asList(ws.getPlateAcquisition().getId().getValue()));
        // Image
        List<Long> ids = new ArrayList<Long>();
        ids.add(ws.getPlateAcquisition().getId().getValue());
        List<Long> v = prx.resetDefaultsInSet(PlateAcquisition.class.getName(),
                ids);
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForImage() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);
        List<Long> m = prx.resetMinMaxInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForDataset() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);

        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);
        // Link image and dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image);
        link.setParent(d);
        iUpdate.saveAndReturnObject(link);
        List<Long> m = prx.resetMinMaxInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForEmptyDataset() throws Exception {
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));
        List<Long> m = prx.resetMinMaxInSet(Dataset.class.getName(),
                Arrays.asList(d.getId().getValue()));
        Assert.assertTrue(m.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:5755")
    public void testResetMinMaxForSetForProject() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertNotNull(def);

        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);
        // Link image and dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        Project project = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());

        ProjectDatasetLink pLink = new ProjectDatasetLinkI();
        pLink.link(project, d);
        iUpdate.saveAndReturnObject(pLink);

        DatasetImageLink link = new DatasetImageLinkI();
        link.link(new DatasetI(d.getId().getValue(), false), image);
        iUpdate.saveAndReturnObject(link);

        List<Long> m = prx.resetMinMaxInSet(Project.class.getName(),
                Arrays.asList(project.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }

    }

    /**
     * Tests to apply the rendering settings to a plate. Tests the
     * <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForPlate() throws Exception {
        Plate plate = createBinaryPlate(1, 1, 1, 0);
        plate = (Plate) iUpdate.saveAndReturnObject(plate);
        // load the well
        List<Well> results = loadWells(plate.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        prx.setOriginalSettingsInSet(Plate.class.getName(),
                Arrays.asList(plate.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);

        List<Long> m = prx.resetMinMaxInSet(Plate.class.getName(),
                Arrays.asList(plate.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }
    }

    /**
     * Tests to apply the rendering settings to a plate. Tests the
     * <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForPlateAcquisition() throws Exception {
        Plate plate = createBinaryPlate(1, 1, 1, 1);
        plate = (Plate) iUpdate.saveAndReturnObject(plate);
        // load the well
        List<Well> results = loadWells(plate.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        WellSample ws = well.getWellSample(0);
        Image image = ws.getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(PlateAcquisition.class.getName(),
                Arrays.asList(ws.getPlateAcquisition().getId().getValue()));
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);

        List<Long> m = prx.resetMinMaxInSet(PlateAcquisition.class.getName(),
                Arrays.asList(ws.getPlateAcquisition().getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }
    }

    /**
     * Tests to apply reset the min/max values for a screen.s Tests the
     * <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:5755")
    public void testResetMinMaxForSetForScreen() throws Exception {
        Screen screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        Plate plate = createBinaryPlate(1, 1, 1, 0);
        plate = (Plate) iUpdate.saveAndReturnObject(plate);

        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild(plate);
        link.setParent(screen);
        link = (ScreenPlateLink) iUpdate.saveAndReturnObject(link);
        screen = link.getParent();
        // load the well
        List<Well> results = loadWells(plate.getId().getValue(), true);
        Well well = results.get(0);

        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = well.getWellSample(0).getImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();

        prx.setOriginalSettingsInSet(Plate.class.getName(),
                Arrays.asList(plate.getId().getValue()));
        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Modified the settings.
        ChannelBinding channel;
        List<Point> list = new ArrayList<Point>();

        Point p;
        List<IObject> toUpdate = new ArrayList<IObject>();
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(0);
            p = new Point();
            p.setLocation(channel.getInputStart().getValue(), channel
                    .getInputEnd().getValue());
            list.add(p);
            channel.setInputStart(omero.rtypes.rdouble(1));
            channel.setInputEnd(omero.rtypes.rdouble(2));
            toUpdate.add(channel);
        }
        iUpdate.saveAndReturnArray(toUpdate);

        List<Long> m = prx.resetMinMaxInSet(Plate.class.getName(),
                Arrays.asList(plate.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
        def = factory.getPixelsService().retrieveRndSettings(id);
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = def.getChannelBinding(i);
            p = list.get(i);
            Assert.assertEquals(channel.getInputStart().getValue(), p.getX());
            Assert.assertEquals(channel.getInputEnd().getValue(), p.getY());
        }
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSetOriginalSettingsInSet() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        // Image
        // method already tested
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // method already tested
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        // Create a second image.
        Image image2 = createBinaryImage();
        Map<Boolean, List<Long>> m = prx
                .applySettingsToSet(id, Image.class.getName(),
                        Arrays.asList(image2.getId().getValue()));
        Assert.assertNotNull(m);
        List<Long> success = (List<Long>) m.get(Boolean.valueOf(true));
        List<Long> failure = (List<Long>) m.get(Boolean.valueOf(false));
        Assert.assertNotNull(success);
        Assert.assertNotNull(failure);
        Assert.assertEquals(success.size(), 1);
        Assert.assertTrue(failure.isEmpty());
        id = success.get(0); // image id.
        Assert.assertEquals(id, image2.getId().getValue());
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(
                image2.getPrimaryPixels().getId().getValue());
        compareRenderingDef(def, def2);
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwner() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        // Image
        List<Long> v = prx.resetDefaultsByOwnerInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        param.addLong("oid", ctx2.userId);
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid and rdef.details.owner.id = :oid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerNoRndSettingsTarget() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));

        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        prx = factory.getRenderingSettingsService();

        // in that case create rendering settings for the target.
        // Image
        List<Long> v = prx.resetDefaultsByOwnerInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertEquals(v.size(), 1);
        ParametersI param = new ParametersI();
        param.addLong("pid", pixels.getId().getValue());
        param.addLong("oid", ctx2.userId);
        String sql = "select rdef from RenderingDef as rdef "
                + "where rdef.pixels.id = :pid and rdef.details.owner.id = :oid";
        List<IObject> values = iQuery.findAllByQuery(sql, param);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerNoRndSettingsSource() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();

        disconnect();
        EventContext ctx2 = newUserInGroup(ctx);
        prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        // Image
        List<Long> v = prx.resetDefaultsByOwnerInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertTrue(v.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForImageNoSettings() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        // Image
        // method already tested
        List<Long> m = prx.resetMinMaxInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForNonValidImage() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = mmFactory.simpleImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        // Image
        // method already tested
        List<Long> m = prx.resetMinMaxInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertTrue(m.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultByOwnerNonValidImage() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        // Image
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        List<Long> v = prx.resetDefaultsByOwnerInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertTrue(v.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSetOriginalSettingsInSetNonValidImage() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        // Image
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        List<Long> v = prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertTrue(v.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to a collection of images. Tests
     * the <code>ApplySettingsToSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultInSetNonValidImage() throws Exception {
        EventContext ctx = newUserAndGroup("rwra--");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        // Image
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        List<Long> v = prx.resetDefaultsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(v);
        Assert.assertTrue(v.isEmpty());
    }

    /**
     * Tests to apply the rendering settings to an image w/o stats info Tests
     * the <code>ResetMinMaxForSet</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetMinMaxForSetForImageNoStatsInfo() throws Exception {
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        // Delete the stats info object.
        Pixels pixels = image.getPixels(0);
        // load the channel.
        Channel channel;
        String sql = "select c from Channel as c where c.pixels.id = :iid";
        ParametersI param = new ParametersI();
        param.addLong("iid", pixels.getId().getValue());
        List<IObject> channels = iQuery.findAllByQuery(sql, param);
        Iterator<IObject> i = channels.iterator();
        while (i.hasNext()) {
            channel = (Channel) i.next();
            channel.setStatsInfo(null);
            iUpdate.saveAndReturnObject(channel);
        }

        // Make sure the channels not have stats info.
        channels = iQuery.findAllByQuery(sql, param);
        i = channels.iterator();
        while (i.hasNext()) {
            channel = (Channel) i.next();
            Assert.assertNull(channel.getStatsInfo());
        }
        // Image
        // method already tested
        List<Long> m = prx.resetMinMaxInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        Assert.assertNotNull(m);
        Assert.assertEquals(m.size(), 1);
    }
}
