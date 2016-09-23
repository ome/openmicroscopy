/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import omero.api.IPixelsPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IScriptPrx;
import omero.api.RenderingEnginePrx;
import omero.cmd.Chgrp2;
import omero.cmd.Delete2;
import omero.gateway.util.Requests;
import omero.model.Channel;
import omero.model.ChannelBinding;
import omero.model.CodomainMapContext;
import omero.model.ExperimenterGroup;
import omero.model.Family;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.ProjectionAxisI;
import omero.model.ProjectionDef;
import omero.model.ProjectionDefI;
import omero.model.ProjectionTypeI;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.model.RenderingModel;
import omero.model.ReverseIntensityContext;
import omero.model.ReverseIntensityContextI;
import omero.romio.PlaneDef;
import omero.romio.RGBBuffer;
import omero.romio.RegionDef;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;
import org.testng.Assert;

import sun.awt.image.IntegerInterleavedRaster;

/**
 * Collection of tests for the <code>RenderingEngine</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class RenderingEngineTest extends AbstractServerTest {

    /** The red mask. */
    private static final int RED_MASK = 0x00ff0000;

    /** The green mask. */
    private static final int GREEN_MASK = 0x0000ff00;

    /** The blue mask. */
    private static final int BLUE_MASK = 0x000000ff;

    /** The RGB masks. */
    private static final int[] RGB = { RED_MASK, GREEN_MASK, BLUE_MASK };

    /**
     * Saves the rendering settings when the image is viewed by another member
     * of the group.
     *
     * @param permissions
     *            The permissions.
     * @param role
     * @throws Exception
     */
    private void saveRenderingSettings(String permissions, int role)
            throws Exception {
        saveRenderingSettings(permissions, role, true);
        saveRenderingSettings(permissions, role, false);
    }

    /**
     * Inner method which allows to optionally call the rendering method as the
     * owner. If this is not called, then there will be no rendering def at all
     * when the secondary uses attempts access.
     */
    private void saveRenderingSettings(String permissions, int role,
            boolean preload) throws Exception {
        EventContext ctx = newUserAndGroup(permissions);
        // Import the image
        File f = File.createTempFile("saveRenderingSettings", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        long userId = ctx.userId;
        long originalOwnerId = -1;
        boolean saveAs = false;
        if (preload) {
            originalOwnerId = ctx.userId;
            saveAs = true;
            assertRendering(pixels, userId, originalOwnerId, false); // View as owner
        }

        disconnect();
        // login as another user.
        EventContext ctx2 = newUserInGroup(ctx);
        userId = ctx2.userId;
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                userId = iAdmin.getEventContext().userId;
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        assertRendering(pixels, userId, originalOwnerId, saveAs);
    }

    private void assertRendering(List<Pixels> pixels, long userId, long originalOwnerId, boolean saveAs) throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> luts = svc.getScriptsByMimetype(
                ScriptServiceTest.LUT_MIMETYPE);
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);

        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        if (saveAs) {
            //check that we are doing a "saveAs" is done
            Assert.assertNotEquals(userId, originalOwnerId);
            Assert.assertEquals(def.getDetails().getOwner().getId().getValue(), originalOwnerId);
        }
        int t = re.getDefaultT();
        int v = t + 1;
        re.setDefaultT(v);
        //
        int sizeC = re.getPixels().getSizeC().getValue();
        String lutName = luts.get(0).getName().getValue();
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext c = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(c, k);
            re.setChannelLookupTable(k, lutName);
        }
        //Save the settings for the user currently logged in.
        re.saveCurrentSettings();
        Assert.assertEquals(re.getDefaultT(), v);
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        if (saveAs) {
            //original data and other user not the same
            Assert.assertEquals(def.getDetails().getOwner().getId().getValue(), userId);
        }
        Assert.assertEquals(def.getDefaultT().getValue(), v);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Iterator<ChannelBinding> j = channels.iterator();
        ChannelBinding b;
        while (j.hasNext()) {
            b = j.next();
            Assert.assertNotNull(b.getLookupTable().getValue());
            Assert.assertEquals(b.getLookupTable().getValue(), lutName);
            List<CodomainMapContext> cl = b.copySpatialDomainEnhancement();
            Assert.assertEquals(cl.size(), 1);
            CodomainMapContext ctx;
            if (saveAs) {
                Iterator<CodomainMapContext> k = cl.iterator();
                while (k.hasNext()) {
                    ctx = k.next();
                    Assert.assertNotNull(ctx);
                    //make sure that the codomain contexts are not shared.
                    Assert.assertEquals(ctx.getDetails().getOwner().getId().getValue(), userId);
                }
            }
        }
    }

    /**
     * Creates a buffer image from the specified <code>array</code> of integers.
     *
     * @param buf
     *            The array to handle.
     * @param bits
     *            The number of bits in the pixel values.
     * @param sizeX
     *            The width (in pixels) of the region of image data described.
     * @param sizeY
     *            The height (in pixels) of the region of image data described.
     * @return See above.
     */
    private BufferedImage createImage(int[] buf, int bits, int sizeX, int sizeY) {
        if (buf == null)
            return null;
        DataBuffer j2DBuf = new DataBufferInt(buf, sizeX * sizeY);
        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, RGB);
        WritableRaster raster = new IntegerInterleavedRaster(sampleModel,
                j2DBuf, new Point(0, 0));

        ColorModel colorModel = new DirectColorModel(bits, RGB[0], RGB[1],
                RGB[2]);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);
        image.setAccelerationPriority(1f);
        return image;
    }

    /**
     * Creates an image from the passed values.
     *
     * @param values
     *            The values to handle.
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private BufferedImage createImage(byte[] values) throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(values);
        return ImageIO.read(stream);
    }

    /**
     * Checks the value of the bytes are the same.
     *
     * @param region
     *            The region.
     * @param plane
     *            The plane.
     * @param w
     *            The width of a step.
     * @param rWidth
     *            The width of the region.
     */
    private void checkBuffer(byte[] region, byte[] plane, int w, int rWidth) {
        int j;
        int k = 0;
        for (int i = 0; i < region.length; i++) {
            j = w * k + i;
            Assert.assertEquals(region[i], plane[j]);
            if (i % rWidth == rWidth - 1)
                k++;
        }
    }

    /**
     * Checks the value of the bytes are the same.
     *
     * @param region
     *            The region.
     * @param plane
     *            The plane.
     * @param w
     *            The width of a step.
     * @param rWidth
     *            The width of the region.
     */
    private void checkIntBuffer(int[] region, int[] plane, int w, int rWidth) {
        int j;
        int k = 0;
        for (int i = 0; i < region.length; i++) {
            j = w * k + i;
            Assert.assertEquals(region[i], plane[j]);
            if (i % rWidth == rWidth - 1)
                k++;
        }
    }

    /**
     * Tests the creation of the rendering engine for a given pixels set w/o
     * looking up for rendering settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateRenderingEngineNoSettings() throws Exception {
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        RenderingEnginePrx svc = factory.createRenderingEngine();
        try {
            svc.lookupPixels(pixels.getId().getValue());
            svc.load();
            Assert.fail("We should not have been able to load it.");
        } catch (Exception e) {

        }
    }

    /**
     * Tests the creation of the rendering engine for a given pixels set when
     * looking up for rendering settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateRenderingEngine() throws Exception {
        File f = File.createTempFile("testCreateRenderingEngine", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        re.close();
    }

    /**
     * Tests the retrieval of the rendering settings data using the rendering
     * engine.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineGetters() throws Exception {
        File f = File.createTempFile("testRenderingEngineGetters", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        // retrieve the rendering def
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertEquals(def.getDefaultZ().getValue(), re.getDefaultZ());
        Assert.assertEquals(def.getDefaultT().getValue(), re.getDefaultT());
        Assert.assertEquals(def.getModel().getValue().getValue(), re.getModel()
                .getValue().getValue());
        QuantumDef q1 = def.getQuantization();
        QuantumDef q2 = re.getQuantumDef();
        Assert.assertNotNull(q1);
        Assert.assertNotNull(q2);
        Assert.assertEquals(q1.getBitResolution().getValue(), q2.getBitResolution()
                .getValue());
        Assert.assertEquals(q1.getCdStart().getValue(), q2.getCdStart().getValue());
        Assert.assertEquals(q1.getCdEnd().getValue(), q2.getCdEnd().getValue());
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        int[] rgba;
        while (i.hasNext()) {
            c1 = i.next();
            rgba = re.getRGBA(index);
            Assert.assertEquals(c1.getRed().getValue(), rgba[0]);
            Assert.assertEquals(c1.getGreen().getValue(), rgba[1]);
            Assert.assertEquals(c1.getBlue().getValue(), rgba[2]);
            Assert.assertEquals(c1.getAlpha().getValue(), rgba[3]);
            Assert.assertEquals(c1.getCoefficient().getValue(),
                    re.getChannelCurveCoefficient(index));
            Assert.assertEquals(c1.getFamily().getValue().getValue(), re
                    .getChannelFamily(index).getValue().getValue());
            Assert.assertEquals(c1.getInputStart().getValue(),
                    re.getChannelWindowStart(index));
            Assert.assertEquals(c1.getInputEnd().getValue(),
                    re.getChannelWindowEnd(index));
            Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
            Boolean b2 = Boolean.valueOf(re.isActive(index));
            Assert.assertEquals(b1.booleanValue(), b2.booleanValue());
            b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
            b2 = Boolean.valueOf(re.getChannelNoiseReduction(index));
            Assert.assertEquals(b1.booleanValue(), b2.booleanValue());
            index++;
        }
        re.close();
    }

    /**
     * Tests to modify the rendering settings using the rendering engine.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineSetters() throws Exception {
        File file = File.createTempFile("testRenderingEngineSetters", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(file, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(file, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        int v = def.getDefaultT().getValue() + 1;
        re.setDefaultT(v);
        Assert.assertEquals(re.getDefaultT(), v);
        v = def.getDefaultZ().getValue() + 1;
        re.setDefaultZ(v);
        Assert.assertEquals(re.getDefaultZ(), v);

        // tested in PixelsService
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> families = svc.getAllEnumerations(Family.class.getName());
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel model = def.getModel();
        Iterator<IObject> i;
        RenderingModel m;
        i = models.iterator();
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue()) {
                model = m;
                break;
            }
        }
        re.setModel(model);
        Assert.assertEquals(re.getModel().getId().getValue(), model.getId().getValue());
        QuantumDef qdef = def.getQuantization();
        int start = qdef.getCdStart().getValue() + 10;
        int end = qdef.getCdEnd().getValue() - 10;
        re.setCodomainInterval(start, end);
        Assert.assertEquals(re.getQuantumDef().getCdStart().getValue(), start);
        Assert.assertEquals(re.getQuantumDef().getCdEnd().getValue(), end);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Iterator<ChannelBinding> j = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        boolean b;
        double s, e;
        int[] RGBA = { 255, 0, 10, 200 };
        int[] rgba;
        double coefficient = 0.5;
        Family f = (Family) families.get(families.size() - 1);
        while (j.hasNext()) {
            c1 = j.next();
            b = !c1.getActive().getValue();
            re.setActive(index, b);
            Assert.assertEquals(b, re.isActive(index));
            s = c1.getInputStart().getValue() + 1;
            e = c1.getInputEnd().getValue() + 1;
            re.setChannelWindow(index, s, e);
            Assert.assertEquals(re.getChannelWindowStart(index), s);
            Assert.assertEquals(re.getChannelWindowEnd(index), e);
            b = !c1.getNoiseReduction().getValue();
            re.setRGBA(index, RGBA[0], RGBA[1], RGBA[2], RGBA[3]);
            rgba = re.getRGBA(index);
            Assert.assertTrue(Arrays.equals(rgba, RGBA));
            b = !c1.getNoiseReduction().getValue();
            re.setQuantizationMap(index, f, coefficient, b);
            Assert.assertEquals(b, re.getChannelNoiseReduction(index));
            Assert.assertEquals(re.getChannelCurveCoefficient(index), coefficient);
            Assert.assertEquals(re.getChannelFamily(index).getId().getValue(), f
                    .getId().getValue());
        }
        re.close();
    }

    /**
     * Tests to reset the default settings but do not save them back to the
     * database using the <code>resetDefaultsNoSave</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultsNoSave() throws Exception {
        File f = File.createTempFile("testResetDefaultsNoSave", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();

        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        int t = def.getDefaultT().getValue();
        int v = t + 1;
        def.setDefaultT(omero.rtypes.rint(v));
        // update
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        Assert.assertEquals(re.getDefaultT(), def.getDefaultT().getValue());
        re.resetDefaultSettings(false);
        Assert.assertEquals(re.getDefaultT(), t);
        // reload from db
        def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertEquals(def.getDefaultT().getValue(), v);
    }

    /**
     * Tests to reset the default settings and save them back to the database
     * using the <code>resetDefaults</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaults() throws Exception {
        File f = File.createTempFile("testResetDefaults", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();

        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        int t = def.getDefaultT().getValue();
        int v = t + 1;
        def.setDefaultT(omero.rtypes.rint(v));
        // update
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);

        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!re.lookupRenderingDef(id)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        Assert.assertEquals(re.getDefaultT(), def.getDefaultT().getValue());
        re.resetDefaultSettings(true);
        Assert.assertEquals(re.getDefaultT(), t);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettings() throws Exception {
        File f = File.createTempFile("testSaveCurrentSettings", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        //change t
        int new_t = re.getDefaultT() + 1;
        re.setDefaultT(new_t);
        Assert.assertEquals(re.getDefaultT(), new_t);
        //change z
        int new_z = re.getDefaultZ() + 1;
        re.setDefaultZ(new_z);
        Assert.assertEquals(re.getDefaultZ(), new_z);
        //change compression
        float new_compression = 0.5f;
        re.setCompressionLevel(new_compression);
        Assert.assertEquals(re.getCompressionLevel(), new_compression);
        //codomain change
        QuantumDef qdef = re.getQuantumDef();
        int new_bit = 127;
        int new_cd_start = qdef.getCdStart().getValue()+1;
        int new_cd_end = qdef.getCdEnd().getValue()-1;
        re.setQuantumStrategy(new_bit);
        re.setCodomainInterval(new_cd_start, new_cd_end);
        //mode change
        RenderingModel model = re.getModel();
        List<IObject> models = factory.getPixelsService().getAllEnumerations(
                RenderingModel.class
                .getName());
        Iterator<IObject> j = models.iterator();
        RenderingModel m, new_model = null;
        // Change the color model so it is not grey scale.
        while (j.hasNext()) {
            m = (RenderingModel) j.next();
            if (m.getId().getValue() != model.getId().getValue()) {
                re.setModel(m);
                new_model = m;
            }
        }
        int sizeC = p.getSizeC().getValue();
        List<IObject> families = re.getAvailableFamilies();
        List<Boolean> active = new ArrayList<Boolean>(sizeC);
        List<String> lut = new ArrayList<String>(sizeC);
        List<Double> coeff = new ArrayList<Double>(sizeC);
        List<Point2D.Double> interval = new ArrayList<Point2D.Double>(sizeC);
        List<Family> new_families = new ArrayList<Family>(sizeC);
        List<Boolean> noise = new ArrayList<Boolean>(sizeC);
        List<int[]> color = new ArrayList<int[]>(sizeC);
        for (int i = 0; i < sizeC; i++) {
            //active flag
            active.add(!re.isActive(i));
            re.setActive(i, active.get(i));
            //lut
            lut.add("new_"+i+".lut");
            re.setChannelLookupTable(i, lut.get(i));
            //input interval
            Point2D.Double point = new Point2D.Double(
                    re.getChannelWindowStart(i)+0.1,
                    re.getChannelWindowEnd(i)-0.1);
            interval.add(point);
            re.setChannelWindow(i, point.getX(), point.getY());
            Family family = re.getChannelFamily(i);
            Iterator<IObject> ff = families.iterator();
            while (ff.hasNext()) {
                IObject o = ff.next();
                if (o.getId().getValue() != family.getId().getValue()) {
                    new_families.add((Family) o);
                    break;
                }
            }
            coeff.add(re.getChannelCurveCoefficient(i)+0.1);
            noise.add(!re.getChannelNoiseReduction(i));
            re.setQuantizationMap(i, new_families.get(i), coeff.get(i),
                    noise.get(i));
            //color
            int[] rgba = new int[4];
            for (int k = 0; k < rgba.length; k++) {
                rgba[k] = i+k;
            }
            color.add(rgba);
            re.setRGBA(i, rgba[0], rgba[1], rgba[2], rgba[3]);
            //reverse intensity
            omero.romio.ReverseIntensityMapContext c = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(c, i);
        }
        //save settings
        re.saveCurrentSettings();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertEquals(def.getDefaultT().getValue(), new_t);
        Assert.assertEquals(def.getDefaultZ().getValue(), new_z);
        if (def.getCompression() != null) {
            Assert.assertEquals(def.getCompression().getValue(), new_compression);
        }
        Assert.assertEquals(def.getQuantization().getBitResolution().getValue(),
                new_bit);
        Assert.assertEquals(def.getQuantization().getCdStart().getValue(),
                new_cd_start);
        Assert.assertEquals(def.getQuantization().getCdEnd().getValue(),
                new_cd_end);
        Assert.assertNotNull(new_model);
        Assert.assertEquals(re.getModel().getValue().getValue(),
                new_model.getValue().getValue());
        List<IObject> contextList;
        for (int i = 0; i < sizeC; i++) {
            Assert.assertEquals(re.isActive(i), active.get(i).booleanValue());
            Assert.assertEquals(re.getChannelLookupTable(i), lut.get(i));
            Point2D.Double point = interval.get(i);
            Assert.assertEquals(re.getChannelWindowStart(i), point.getX());
            Assert.assertEquals(re.getChannelWindowEnd(i), point.getY());
            Assert.assertEquals(re.getChannelCurveCoefficient(i), coeff.get(i));
            Assert.assertEquals(re.getChannelNoiseReduction(i),
                    noise.get(i).booleanValue());
            Assert.assertEquals(re.getChannelFamily(i).getId().getValue(),
                    new_families.get(i).getId().getValue());
            Assert.assertTrue(Arrays.equals(color.get(i), re.getRGBA(i)));
            contextList = re.getCodomainMapContext(i);
            Assert.assertNotNull(contextList);
            Assert.assertEquals(contextList.size(), 1);
            IObject ho = contextList.get(0);
            Assert.assertTrue(ho instanceof ReverseIntensityContext);
        }
        re.close();
    }

    /**
     * Tests to render a plane using the <code>render</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderPlane() throws Exception {
        File f = File.createTempFile("testRenderPlane", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        // delete the file.
        RGBBuffer buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        Assert.assertEquals(p.getSizeX().getValue(), buffer.sizeX1);
        Assert.assertEquals(p.getSizeY().getValue(), buffer.sizeX2);
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel model = re.getModel();
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the <code>render</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderRegion() throws Exception {
        File f = File.createTempFile("testRenderRegion", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int[] values = { 1, 2, 3, 4, 5 };
        int v;
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        RGBBuffer bufferRegion, bufferPlane;
        byte[] region, plane;
        for (int i = 0; i < values.length; i++) {
            v = values[i];
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            RegionDef r = new RegionDef();
            r.x = 0;
            r.y = 0;
            r.width = sizeX / v;
            r.height = sizeY / v;
            pDef.region = r;
            bufferRegion = re.render(pDef);
            Assert.assertNotNull(bufferRegion);
            Assert.assertEquals(r.width, bufferRegion.sizeX1);
            Assert.assertEquals(r.height, bufferRegion.sizeX2);
            // now render a plane and compare the renderer value.
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            bufferPlane = re.render(pDef);
            Assert.assertNotNull(bufferPlane);
            // red band
            region = bufferRegion.bands[0];
            plane = bufferPlane.bands[0];
            Assert.assertNotNull(region);
            Assert.assertNotNull(plane);
            checkBuffer(region, plane, sizeX - r.width, r.width);
            // green band
            region = bufferRegion.bands[1];
            plane = bufferPlane.bands[1];
            Assert.assertNotNull(region);
            Assert.assertNotNull(plane);
            checkBuffer(region, plane, sizeX - r.width, r.width);
            // blue band
            region = bufferRegion.bands[2];
            plane = bufferPlane.bands[2];
            Assert.assertNotNull(region);
            Assert.assertNotNull(plane);
            checkBuffer(region, plane, sizeX - r.width, r.width);
        }
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the <code>render</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderRegionChangeModel() throws Exception {
        File f = File.createTempFile("testRenderRegionChangeModel", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int v = 2;
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        RGBBuffer bufferRegion;
        pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RegionDef r = new RegionDef();
        r.x = 0;
        r.y = 0;
        r.width = sizeX / v;
        r.height = sizeY / v;
        pDef.region = r;
        bufferRegion = re.render(pDef);
        Assert.assertNotNull(bufferRegion);
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel model = re.getModel();
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        bufferRegion = re.render(pDef);
        Assert.assertNotNull(bufferRegion);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the <code>renderCompressed</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderCompressedPlane() throws Exception {
        File f = File.createTempFile("testRenderCompressedPlane", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        // delete the file.
        byte[] values = re.renderCompressed(pDef);

        Assert.assertNotNull(values);
        // Create a buffered image.
        BufferedImage image = createImage(values);
        Assert.assertNotNull(image);
        Assert.assertEquals(image.getWidth(), p.getSizeX().getValue());
        Assert.assertEquals(image.getHeight(), p.getSizeY().getValue());
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the
     * <code>renderCompressed</code> method. For the comparison between plane
     * and region, see {@link #testRenderAsPackedIntRegion}. The
     * <code>renderCompressed</code> method first renders the region using the
     * <code>renderAsPackedInt</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderCompressedRegion() throws Exception {
        File f = File.createTempFile("testRenderCompressedRegion", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        BufferedImage imageRegion;
        byte[] region;
        int v = 2;
        pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RegionDef r = new RegionDef();
        r.x = 0;
        r.y = 0;
        r.width = sizeX / v;
        r.height = sizeY / v;
        pDef.region = r;
        region = re.renderCompressed(pDef);
        Assert.assertNotNull(region);
        imageRegion = createImage(region);
        Assert.assertNotNull(imageRegion);
        Assert.assertEquals(r.width, imageRegion.getWidth());
        Assert.assertEquals(r.height, imageRegion.getHeight());
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the <code>renderAsPackedInt</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPackedIntPlane() throws Exception {
        File f = File.createTempFile("testRenderAsPackedIntPlane", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        // delete the file.
        int[] buffer = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, p.getSizeX().getValue(),
                p.getSizeY().getValue());
        Assert.assertNotNull(image);

        // now change the model
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel model = re.getModel();
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        buffer = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(buffer);
        image = createImage(buffer, 32, p.getSizeX().getValue(), p.getSizeY()
                .getValue());
        Assert.assertNotNull(image);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the
     * <code>renderAsPackedInt</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPackedIntRegion() throws Exception {
        File f = File.createTempFile("testRenderAsPackedIntRegion", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int[] values = { 1, 2, 3, 4, 5 };
        int v;
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        BufferedImage imageRegion, imagePlane, cropImage;
        int[] region, plane;
        for (int i = 0; i < values.length; i++) {
            v = values[i];
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            RegionDef r = new RegionDef();
            r.x = 0;
            r.y = 0;
            r.width = sizeX / v;
            r.height = sizeY / v;
            pDef.region = r;
            region = re.renderAsPackedInt(pDef);
            Assert.assertNotNull(region);
            imageRegion = createImage(region, 32, r.width, r.height);
            Assert.assertNotNull(imageRegion);
            Assert.assertEquals(r.width, imageRegion.getWidth());
            Assert.assertEquals(r.height, imageRegion.getHeight());
            // now render a plane and compare the renderer value.
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            plane = re.renderAsPackedInt(pDef);
            Assert.assertNotNull(plane);
            checkIntBuffer(region, plane, sizeX - r.width, r.width);
        }
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the
     * <code>renderAsPackedInt</code> method, change the color model.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPackedIntRegionChangeModel() throws Exception {
        File f = File.createTempFile("testRenderAsPackedIntRegion", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int v = 2;
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        BufferedImage imageRegion;
        int[] region;
        pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RegionDef r = new RegionDef();
        r.x = 0;
        r.y = 0;
        r.width = sizeX / v;
        r.height = sizeY / v;
        pDef.region = r;
        region = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(region);
        imageRegion = createImage(region, 32, r.width, r.height);
        Assert.assertNotNull(imageRegion);
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel model = re.getModel();
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        region = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(region);
        imageRegion = createImage(region, 32, r.width, r.height);
        Assert.assertNotNull(imageRegion);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the stride parameter, not all pixels will
     * be rendered. The method uses the <code>renderAsPackedInt</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPackedIntStridePlane() throws Exception {
        File f = File.createTempFile("testRenderAsPackedIntStridePlane", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;
        // delete the file.
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        sizeX = sizeX / (pDef.stride + 1);
        sizeY = sizeY / (pDef.stride + 1);
        int[] buffer = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, sizeX, sizeY);
        Assert.assertNotNull(image);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the stride parameter, not all pixels will
     * be rendered. The method uses the <code>renderAsPackedInt</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPackedIntStrideRegion() throws Exception {
        File f = File.createTempFile("testRenderAsPackedIntStrideRegion", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        RegionDef regionDef = new RegionDef();
        regionDef.x = 0;
        regionDef.y = 0;
        regionDef.width = sizeX / 2;
        regionDef.height = sizeY / 2;
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;

        sizeX = regionDef.width / (pDef.stride + 1);
        sizeY = regionDef.height / (pDef.stride + 1);
        int[] buffer = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, sizeX, sizeY);
        Assert.assertNotNull(image);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the stride parameter, not all pixels will
     * be rendered. The method uses the <code>renderCompressed</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderCompressedStridePlane() throws Exception {
        File f = File.createTempFile("testRenderCompressedStridePlane", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;
        // delete the file.
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        sizeX = sizeX / (pDef.stride + 1);
        sizeY = sizeY / (pDef.stride + 1);
        byte[] buffer = re.renderCompressed(pDef);
        Assert.assertNotNull(buffer);
        BufferedImage image = createImage(buffer);
        Assert.assertNotNull(image);
        Assert.assertEquals(image.getWidth(), sizeX);
        Assert.assertEquals(image.getHeight(), sizeY);
        f.delete();
        re.close();
    }

    /**
     * Tests to render the region of a plane using the stride parameter, not all
     * pixels will be rendered. The method uses the
     * <code>renderCompressed</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderCompressedStrideRegion() throws Exception {
        File f = File.createTempFile("testRenderCompressedStridePlane", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        RegionDef regionDef = new RegionDef();
        regionDef.x = 0;
        regionDef.y = 0;
        regionDef.width = sizeX / 2;
        regionDef.height = sizeY / 2;
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;
        pDef.region = regionDef;
        // delete the file.

        sizeX = regionDef.width / (pDef.stride + 1);
        sizeY = regionDef.height / (pDef.stride + 1);
        byte[] buffer = re.renderCompressed(pDef);
        Assert.assertNotNull(buffer);
        BufferedImage image = createImage(buffer);
        Assert.assertNotNull(image);
        Assert.assertEquals(image.getWidth(), sizeX);
        Assert.assertEquals(image.getHeight(), sizeY);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a plane using the stride parameter, not all pixels will
     * be rendered. The method uses the <code>render</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderStridePlane() throws Exception {
        File f = File.createTempFile("testRenderStridePlane", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;
        // delete the file.
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        sizeX = sizeX / (pDef.stride + 1);
        sizeY = sizeY / (pDef.stride + 1);

        // hsb
        RGBBuffer buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        Assert.assertEquals(sizeX, buffer.sizeX1);
        Assert.assertEquals(sizeY, buffer.sizeX2);

        // greyscale
        RenderingModel model = re.getModel();
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a region of a plane using the stride parameter, not all
     * pixels will be rendered. The method uses the <code>render</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderStrideRegion() throws Exception {
        File f = File
                .createTempFile("testRenderStrideRegion", "." + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeX().getValue();
        RegionDef regionDef = new RegionDef();
        regionDef.x = 0;
        regionDef.y = 0;
        regionDef.width = sizeX / 2;
        regionDef.height = sizeY / 2;
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        pDef.stride = 1;
        pDef.region = regionDef;
        sizeX = regionDef.width / (pDef.stride + 1);
        sizeY = regionDef.height / (pDef.stride + 1);

        // hsb model
        RGBBuffer buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        Assert.assertEquals(sizeX, buffer.sizeX1);
        Assert.assertEquals(sizeY, buffer.sizeX2);

        // grey scale.
        RenderingModel model = re.getModel();
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        Iterator<IObject> i = models.iterator();
        RenderingModel m;
        // Change the color model and render
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        buffer = re.render(pDef);
        Assert.assertNotNull(buffer);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the <code>render</code>
     * method. The region requested it outside the size of the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderRegionOutsideRange() throws Exception {
        File f = File.createTempFile("testRenderRegionOutsideRange", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        RGBBuffer bufferRegion;
        pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RegionDef r = new RegionDef();
        int w = 8;
        int h = 8;
        r.x = sizeX - w;
        r.y = sizeY - h;
        r.width = 2 * w;
        r.height = 2 * h;
        pDef.region = r;
        bufferRegion = re.render(pDef);
        Assert.assertNotNull(bufferRegion);
        Assert.assertEquals(w, bufferRegion.sizeX1);
        Assert.assertEquals(h, bufferRegion.sizeX2);
        f.delete();
        re.close();
    }

    /**
     * Tests to render a given region of plane using the
     * <code>renderAsPackedInt</code> method. The region requested it outside
     * the size of the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderAsPacketIntRegionOutsideRange() throws Exception {
        File f = File.createTempFile("testRenderRegionOutsideRange", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int sizeX = p.getSizeX().getValue();
        int sizeY = p.getSizeY().getValue();
        PlaneDef pDef;
        int[] bufferRegion;
        pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RegionDef r = new RegionDef();
        int w = 8;
        int h = 8;
        r.x = sizeX - w;
        r.y = sizeY - h;
        r.width = 2 * w;
        r.height = 2 * h;
        pDef.region = r;
        bufferRegion = re.renderAsPackedInt(pDef);
        Assert.assertNotNull(bufferRegion);
        BufferedImage image = createImage(bufferRegion, 32, w, h);
        Assert.assertNotNull(image);
        f.delete();
        re.close();
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method multiple times in a short period of time.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsMultipleTimes() throws Exception {
        File file = File.createTempFile("testRenderingEngineSetters", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(file, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(file, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int n = 5;
        int diff = 20;
        long start, end;
        long time = 0;
        /*
         * for (int i = 0; i < n; i++) { start = System.currentTimeMillis();
         * re.saveCurrentSettings(); end = System.currentTimeMillis()-start; if
         * (i == 0) time = end; else Assert.assertTrue(end >= (time-diff) & end <=
         * (time+diff)); }
         */
        re.close();
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method multiple times in a short period of time.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsAll() throws Exception {
        File file = File.createTempFile("testRenderingEngineSetters", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(file, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(file, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        // z
        int z = 0;
        re.setDefaultZ(z);
        re.saveCurrentSettings();
        Assert.assertEquals(re.getDefaultZ(), z);
        // t
        int t = 1;
        re.setDefaultT(t);
        re.saveCurrentSettings();
        Assert.assertEquals(re.getDefaultT(), t);
        // tested in PixelsService
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> families = svc.getAllEnumerations(Family.class.getName());
        List<IObject> models = svc.getAllEnumerations(RenderingModel.class
                .getName());
        RenderingModel rm = re.getModel();
        Iterator<IObject> i;
        RenderingModel m;
        i = models.iterator();
        while (i.hasNext()) {
            m = (RenderingModel) i.next();
            if (m.getId().getValue() != rm.getId().getValue()) {
                rm = m;
                break;
            }
        }
        re.setModel(rm);
        re.saveCurrentSettings();
        Assert.assertEquals(re.getModel().getId().getValue(), rm.getId().getValue());

        int start = re.getQuantumDef().getCdStart().getValue() + 10;
        int end = re.getQuantumDef().getCdEnd().getValue() - 10;
        re.setCodomainInterval(start, end);
        re.saveCurrentSettings();
        Assert.assertEquals(re.getQuantumDef().getCdStart().getValue(), start);
        int DEPTH_7BIT = 127;
        re.setQuantumStrategy(DEPTH_7BIT);
        re.saveCurrentSettings();
        Assert.assertEquals(re.getQuantumDef().getBitResolution().getValue(),
                DEPTH_7BIT);

        // channels now
        double min = 0;
        double max = 10;
        int[] RGBA = { 255, 0, 10, 200 };
        int[] rgba;
        Family f = (Family) families.get(families.size() - 1);
        double coefficient = 0.5;
        boolean b;
        for (int j = 0; j < XMLMockObjects.SIZE_C; j++) {
            re.setActive(j, j == 0);
            re.saveCurrentSettings();
            Assert.assertEquals(j == 0, re.isActive(j));
            re.setChannelWindow(j, min, max);
            re.saveCurrentSettings();
            Assert.assertEquals(re.getChannelWindowStart(j), min);
            Assert.assertEquals(re.getChannelWindowEnd(j), max);
            // color
            re.setRGBA(j, RGBA[0], RGBA[1], RGBA[2], RGBA[3]);
            re.saveCurrentSettings();
            rgba = re.getRGBA(j);
            Assert.assertTrue(Arrays.equals(rgba, RGBA));
            b = !re.getChannelNoiseReduction(j);
            re.setQuantizationMap(j, f, coefficient, b);
            re.saveCurrentSettings();
            Assert.assertEquals(re.getChannelCurveCoefficient(j), coefficient);
            Assert.assertEquals(re.getChannelNoiseReduction(j), b);
            Assert.assertEquals(re.getChannelFamily(j).getId().getValue(), f.getId()
                    .getValue());
        }
        re.close();
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByGroupOwnerRWR() throws Exception {
        saveRenderingSettings("rwr---", GROUP_OWNER);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByAdminRWR() throws Exception {
        saveRenderingSettings("rwr---", ADMIN);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByGroupOwnerRWRA() throws Exception {
        saveRenderingSettings("rwra--", GROUP_OWNER);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByAdminRWRA() throws Exception {
        saveRenderingSettings("rwra--", ADMIN);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByGroupOwnerRWRW() throws Exception {
        saveRenderingSettings("rwrw--", GROUP_OWNER);
    }

    /**
     * Tests to modify the rendering settings using the rendering engine and
     * save the current settings using the <code>saveCurrentSettings</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveCurrentSettingsByAdminRWRW() throws Exception {
        saveRenderingSettings("rwrw--", ADMIN);
    }

    /**
     * Tests the retrieval of the rendering settings data using the rendering
     * engine.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineChannelWindowGetter() throws Exception {
        File f = File.createTempFile("testRenderingEngineChannelWindowGetter", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        // retrieve the rendering def
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        QuantumDef q1 = def.getQuantization();
        Assert.assertNotNull(q1);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        double s = 0.11;
        double e = 0.21;
        while (i.hasNext()) {
            c1 = i.next();
            e = c1.getInputEnd().getValue()+0.21;
            s = c1.getInputStart().getValue()+0.11;
            re.setChannelWindow(index, s, e);
            Assert.assertEquals(s, re.getChannelWindowStart(index));
            Assert.assertEquals(e, re.getChannelWindowEnd(index));
            index++;
        }
        re.close();
    }

    /**
     * Tests the retrieval of the lookup table info using the rendering
     * engine.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineChannelLookupTable() throws Exception {
        File f = File.createTempFile("testRenderingEngineChannelLookupTable", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        // retrieve the rendering def
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        String new_value = "cool.lut";
        while (i.hasNext()) {
            c1 = i.next();
            Assert.assertNull(c1.getLookupTable());
            re.setChannelLookupTable(index, new_value);
            Assert.assertEquals(re.getChannelLookupTable(index), new_value);
            index++;
        }
        re.saveCurrentSettings();
        re.close();
    }

    /**
     * Tests the retrieval of projection def
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineProjectionDef() throws Exception {
        File f = File.createTempFile("testRenderingEngineProjectionDef", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        //create the rendering def
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        re.close();
        //create projectionDef for now using IUpdate
        ProjectionDef pDef = new ProjectionDefI();
        pDef.setActive(omero.rtypes.rbool(true));
        pDef.setStartPlane(omero.rtypes.rint(0));
        pDef.setEndPlane(omero.rtypes.rint(1));
        ProjectionAxisI projection = new ProjectionAxisI();
        projection.setValue(omero.rtypes.rstring("Z"));
        pDef.setAxis(projection);
        ProjectionTypeI type = new ProjectionTypeI();
        type.setValue(omero.rtypes.rstring("maximum"));
        pDef.setType(type);
        // retrieve the rendering def
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        Assert.assertNotNull(def);
        def.addProjectionDef(pDef);
        factory.getUpdateService().saveAndReturnObject(def);
        def = factory.getPixelsService().retrieveRndSettings(id);
        List<ProjectionDef> list = def.copyProjections();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        ProjectionDef savedDef = def.getProjectionDef(0);
        Assert.assertEquals(true, savedDef.getActive().getValue());
        Assert.assertEquals(0, savedDef.getStartPlane().getValue());
        Assert.assertEquals(1, savedDef.getEndPlane().getValue());
        Assert.assertEquals("Z", savedDef.getAxis().getValue().getValue());
        Assert.assertEquals("maximum", savedDef.getType().getValue().getValue());
    }

    /**
     * Tests the retrieval of the lookup table info using the rendering
     * engine.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRenderingEngineSaveChannelLookupTable() throws Exception {
        File f = File.createTempFile("testRenderingEngineSaveChannelLookupTable", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        // retrieve the rendering def
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        String lut = "cool.lut";
        while (i.hasNext()) {
            c1 = i.next();
            Assert.assertNull(c1.getLookupTable());
            re.setChannelLookupTable(index, lut);
            index++;
        }
        re.saveCurrentSettings();
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels1 = def.copyWaveRendering();
        i = channels1.iterator();
        while (i.hasNext()) {
            c1 = i.next();
            Assert.assertEquals(c1.getLookupTable().getValue(), lut);
        }
    }

    /**
     * Tests if luts in the repo are currently applied and read.
     * @throws Exception
     */
    @Test
    public void testLutReaders() throws Exception {
        //First import an image
        File f = File.createTempFile("testLutReaders", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);

        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(
                ScriptServiceTest.LUT_MIMETYPE);
        Assert.assertNotNull(scripts);
        Assert.assertTrue(CollectionUtils.isNotEmpty(scripts));
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile of;
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingModel model = re.getModel();
        List<IObject> models = factory.getPixelsService().getAllEnumerations(
                RenderingModel.class
                .getName());
        Iterator<IObject> j = models.iterator();
        RenderingModel m;
        // Change the color model so it is not grey scale.
        while (j.hasNext()) {
            m = (RenderingModel) j.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        List<String> failures = new ArrayList<String>();
        byte[] ref = re.renderCompressed(pDef);
        //now change settings
        while (i.hasNext()) {
            of = i.next();
            re.setChannelLookupTable(0, of.getName().getValue());
            byte[] buffer = re.renderCompressed(pDef);
            Assert.assertNotNull(buffer);
            //check that the lut is correctly read.
            //if not read the color will be used.
            if (Arrays.equals(ref, buffer)) {
                failures.add(of.getName().getValue());
            }
        }
        if (failures.isEmpty()) {
            Assert.assertEquals(failures.size(), 0, "All LUTs read");
        } else {
            Iterator<String> s = failures.iterator();
            StringBuffer b = new StringBuffer();
            while (s.hasNext()) {
                b.append(s.next());
                b.append("\n");
            }
            Assert.fail("LUTs not read:"+b.toString());
        }
    }

    /**
     * Tests that a lut not supported saved in the settings is not applied.
     * The rendered image should be using color.
     * @throws Exception
     */
    @Test
    public void testLutNotInlist() throws Exception {
        //First import an image
        File f = File.createTempFile("testLutNotInlist", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        Assert.assertNotNull(channels1);
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingModel model = re.getModel();
        List<IObject> models = factory.getPixelsService().getAllEnumerations(
                RenderingModel.class
                .getName());
        Iterator<IObject> j = models.iterator();
        RenderingModel m;
        // Change the color model so it is not grey scale.
        while (j.hasNext()) {
            m = (RenderingModel) j.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        byte[] ref = re.renderCompressed(pDef);
        for (int k = 0; k < channels1.size(); k++) {
            re.setChannelLookupTable(k, "foo.lut");
        }
        byte[] buffer = re.renderCompressed(pDef);
        Assert.assertTrue(Arrays.equals(ref, buffer));
    }

    /**
     * Tests reverse intensity
     * @throws Exception
     */
    @Test
    public void testReverseIntensity() throws Exception {
        //First import an image
        File f = File.createTempFile("testReverseIntensity", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        int end = re.getQuantumDef().getCdEnd().getValue();
        //color model: greyscale
        for (int k = 0; k < channels.size(); k++) {
            int[] before = re.renderAsPackedInt(pDef);
            re.addCodomainMapToChannel(new omero.romio.ReverseIntensityMapContext(), k);
            int[] after = re.renderAsPackedInt(pDef);
            for (int i = 0; i < before.length; i++) {
                //get the discrete value w/o codomain map
                int bp = before[i] & 0x0ff;
                //get the discrete value with codomain map
                int ap = after[i] & 0x0ff;
                //check that the reverse intensity was applied
                Assert.assertEquals(ap, (end-bp));
                System.err.println(bp);
            }
        }
    }

    /**
     * Tests add and remove codomain map context
     * @throws Exception
     */
    @Test
    public void testAddAndRemoveCodomain() throws Exception {
        //First import an image
        File f = File.createTempFile("testAddAndRemoveCodomain", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            List<IObject> map = re.getCodomainMapContext(k);
            Assert.assertEquals(map.size(), 1);
            re.removeCodomainMapFromChannel(ctx, k);
            map = re.getCodomainMapContext(k);
            Assert.assertEquals(map.size(), 0);
        }
    }

    /**
     * Tests add the codomain map context twice
     * @throws Exception
     */
    @Test
    public void testAddCodomainTwice() throws Exception {
        //First import an image
        File f = File.createTempFile("testAddCodomainTwice", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            List<IObject> map = re.getCodomainMapContext(k);
            Assert.assertEquals(map.size(), 1);
            re.addCodomainMapToChannel(ctx, k);
            map = re.getCodomainMapContext(k);
            Assert.assertEquals(map.size(), 1);
        }
    }

    /**
     * Tests save the codomain and restart the rendering engine.
     * @throws Exception
     */
    @Test
    public void testSaveCodomainAndRestart() throws Exception {
        //First import an image
        File f = File.createTempFile("testSaveCodomainAndRestart", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        //restart the rendering engine
        re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        Assert.assertTrue(!channels.isEmpty());
        for (int k = 0; k < channels.size(); k++) {
            Assert.assertEquals(re.getCodomainMapContext(k).size(), 1);
        }
    }

    /**
     * Tests save the codomain and restart the rendering engine.
     * @throws Exception
     */
    @Test
    public void testAddRemoveChainAndSave() throws Exception {
        //First import an image
        File f = File.createTempFile("testAddRemoveChainAndSave", "."
                + OME_FORMAT);
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(f, xml.createImage(), true);
        List<Pixels> pixels = null;
        try {
            pixels = importFile(f, OME_FORMAT);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        List<CodomainMapContext> l;
        for (int k = 0; k < channels.size(); k++) {
            l = channels.get(k).copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }
        //with context
        int[] before = re.renderAsPackedInt(pDef);
        //remove context
        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.removeCodomainMapFromChannel(ctx, k);
        }
        //w/o context
        int[] after = re.renderAsPackedInt(pDef);
        Assert.assertFalse(Arrays.equals(after, before));
        //save the settings
        re.saveCurrentSettings();
        //Check that the context is not saved
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            l = channels.get(k).copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 0);
        }
        // check binary w/o context
        int[] afterSave = re.renderAsPackedInt(pDef);
        Assert.assertTrue(Arrays.equals(afterSave, after));
    }

    /**
     * Tests to check the rendering settings have correctly been reset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefault() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> luts = svc.getScriptsByMimetype(
                ScriptServiceTest.LUT_MIMETYPE);
        Assert.assertNotNull(luts);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        ParametersI param = new ParametersI();
        param.addId(id);
        String sql = "select pix from Pixels as pix " +
            "join fetch pix.image " +
            "join fetch pix.pixelsType " +
            "join fetch pix.channels as c " +
            "join fetch c.logicalChannel " +
            "where pix.id = :id";
        List<IObject> ll = iQuery.findAllByQuery(sql, param);
        pixels = (Pixels) ll.get(0);

        // Image
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        RenderingDef def1 = factory.getPixelsService().retrieveRndSettings(id);
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        List<ChannelBinding> channels = def1.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            re.setChannelLookupTable(k, luts.get(0).getName().getValue());
        }
        re.saveCurrentSettings();
        // method already tested
        //re.close();
        def1 = factory.getPixelsService().retrieveRndSettings(id);
        //reset and save
        re.resetDefaultSettings(true);
        re.close();
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(id);
        //Check that lut has been removed since we won't have one at import
        //Check that the list of codomain context is empty
        channels = def1.copyWaveRendering();
        ChannelBinding cb1, cb2;
        List<ChannelBinding> channels2 = def2.copyWaveRendering();
        List<Channel> l = pixels.copyChannels();
        Channel c;
        for (int k = 0; k < channels.size(); k++) {
            cb1 = channels.get(k);
            cb2 = channels2.get(k);
            c = l.get(k);
            Assert.assertNotNull(c);
            Assert.assertNull(c.getLookupTable());
            Assert.assertEquals(cb1.copySpatialDomainEnhancement().size(), 1);
            Assert.assertTrue(cb2.copySpatialDomainEnhancement().isEmpty());
            Assert.assertNotNull(cb1.getLookupTable());
            Assert.assertNull(cb2.getLookupTable());
        }
    }

    /**
     * Tests to check the rendering settings have correctly been reset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testResetDefaultNoSave() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> luts = svc.getScriptsByMimetype(
                ScriptServiceTest.LUT_MIMETYPE);
        Assert.assertNotNull(luts);
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        Image image = createBinaryImage();
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        ParametersI param = new ParametersI();
        param.addId(id);
        String sql = "select pix from Pixels as pix " +
            "join fetch pix.image " +
            "join fetch pix.pixelsType " +
            "join fetch pix.channels as c " +
            "join fetch c.logicalChannel " +
            "where pix.id = :id";
        List<IObject> ll = iQuery.findAllByQuery(sql, param);
        pixels = (Pixels) ll.get(0);

        // Image
        prx.setOriginalSettingsInSet(Image.class.getName(),
                Arrays.asList(image.getId().getValue()));
        RenderingDef def1 = factory.getPixelsService().retrieveRndSettings(id);
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        List<ChannelBinding> channels = def1.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            re.setChannelLookupTable(k, luts.get(0).getName().getValue());
        }
        re.saveCurrentSettings();
        // method already tested
        //re.close();
        def1 = factory.getPixelsService().retrieveRndSettings(id);
        //reset and save
        re.resetDefaultSettings(false);
        //Check that lut has been removed since we won't have one at import
        //Check that the list of codomain context is empty
        channels = def1.copyWaveRendering();
        ChannelBinding cb1;
        List<Channel> l = pixels.copyChannels();
        Channel c;
        for (int k = 0; k < channels.size(); k++) {
            cb1 = channels.get(k);
            c = l.get(k);
            Assert.assertNotNull(c);
            Assert.assertNull(c.getLookupTable());
            Assert.assertEquals(cb1.copySpatialDomainEnhancement().size(), 1);
            Assert.assertTrue(re.getCodomainMapContext(k).isEmpty());
            Assert.assertNotNull(cb1.getLookupTable());
            Assert.assertTrue(StringUtils.isBlank(re.getChannelLookupTable(k)));
        }
        re.close();
    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * This test does not use the rendering engine but directly the update
     * service.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTestNotFromRE() throws Exception {
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            ReverseIntensityContext ctx = new ReverseIntensityContextI();
            ctx.setReverse(omero.rtypes.rbool(true));
            c.addCodomainMapContext(ctx);
        }
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }
    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * This test does not use the rendering engine but directly the update
     * service.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTestNotFromRESaveAfterEach() throws Exception {
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();

        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            ReverseIntensityContext ctx = new ReverseIntensityContextI();
            ctx.setReverse(omero.rtypes.rbool(true));
            c.addCodomainMapContext(ctx);
            def = (RenderingDef) iUpdate.saveAndReturnObject(def);
        }
        def = (RenderingDef) iUpdate.saveAndReturnObject(def);
    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * using the rendering engine.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTest() throws Exception {
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < channels.size(); k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }

    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * using the rendering engine.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTestSaveForEachChannel() throws Exception {
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            //save after adding each context.
            re.saveCurrentSettings();
        }
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }

    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * using the rendering engine. This test adds context, saves them,
     * removes them, saves them.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTestAddRemoveSaveForEachChannel() throws Exception {
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            //save after adding each context.
            re.saveCurrentSettings();
        }

        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.removeCodomainMapFromChannel(ctx, k);
            //save after adding each context.
            re.saveCurrentSettings();
        }
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 0);
        }
        re.close();
    }

    /**
     * Tests to rendering settings with multiple codomain can be saved
     * using the rendering engine. This test adds context, saves them,
     * removes them, add them back and saves them.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMultipleCodomainTestAddRemoveAddSaveForEachChannel() throws Exception {
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            //save after adding each context.
            re.saveCurrentSettings();
        }

        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.removeCodomainMapFromChannel(ctx, k);
            ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
            //save after adding each context.
            re.saveCurrentSettings();
        }
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        for (int k = 0; k < channels.size(); k++) {
            ChannelBinding c = channels.get(k);
            List<CodomainMapContext> l  = c.copySpatialDomainEnhancement();
            Assert.assertEquals(l.size(), 1);
        }
        re.close();
    }

    /**
     * Tests to delete an image with rendering settings and codomain context
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithCodomainMap() throws Exception {
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        //delete the image
        Delete2 dc = Requests.delete().target(image).build();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
        //Check that the rendering def is delete
        sb = new StringBuilder();
        sb.append("select i from RenderingDef i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(def.getId().getValue());
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Tests to delete an image with rendering settings and codomain context
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithCodomainMap() throws Exception {
        int sizeC = 3;
        String perms = "rw----";
        EventContext ec = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ec.userId);
        iAdmin.getEventContext(); // Refresh
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        def = factory.getPixelsService().retrieveRndSettings(id);
        final Chgrp2 dc = Requests.chgrp().target(image).toGroup(g).build();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());

        Assert.assertNotEquals(g.getId().getValue(), ec.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec2 = loginUser(g);
        Assert.assertEquals(g.getId().getValue(), ec2.groupId);
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from RenderingDef i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(def.getId().getValue());
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Tests to delete an image with rendering settings and codomain context.
     * The image has settings saved by owner and other user.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageWithCodomainMapViewByOther() throws Exception {
        String perms = "rwra--";
        EventContext ec = newUserAndGroup(perms);
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        //Settings set by owner
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        disconnect();
        // login as another user.
        EventContext ctx2 = newUserInGroup(ec);
        makeGroupOwner();
        re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(id);
        disconnect();
        //delete the image by image owner
        loginUser(ec);
        Delete2 dc = Requests.delete().target(image).build();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
        //Check that the rendering def is delete
        sb = new StringBuilder();
        sb.append("select i from RenderingDef i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(def.getId().getValue());
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
        param = new ParametersI();
        param.addId(def2.getId().getValue());
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Tests to delete an image with rendering settings and codomain context.
     * The image has settings saved by owner and other user.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithCodomainMapViewByOther() throws Exception {
        String perms = "rwra--";
        EventContext ec = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ec.userId);
        iAdmin.getEventContext(); // Refresh
        int sizeC = 3;
        Image image = createBinaryImage(1, 1, 1, 1, sizeC);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        //Settings set by owner
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        disconnect();
        // login as another user.
        EventContext ctx2 = newUserInGroup(ec);
        makeGroupOwner();
        re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        def = factory.getPixelsService().retrieveRndSettings(id);
        channels = def.copyWaveRendering();
        Assert.assertEquals(channels.size(), sizeC);
        for (int k = 0; k < sizeC; k++) {
            omero.romio.ReverseIntensityMapContext ctx = new omero.romio.ReverseIntensityMapContext();
            re.addCodomainMapToChannel(ctx, k);
        }
        re.saveCurrentSettings();
        re.close();
        RenderingDef def2 = factory.getPixelsService().retrieveRndSettings(id);
        disconnect();
        Assert.assertNotEquals(def.getId().getValue(), def2.getId().getValue());
        //move the image by image owner
        loginUser(ec);
        final Chgrp2 dc = Requests.chgrp().target(image).toGroup(g).build();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(image.getId().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
        //Check that the rendering def is delete
        sb = new StringBuilder();
        sb.append("select i from RenderingDef i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(def.getId().getValue());
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));
        param = new ParametersI();
        param.addId(def2.getId().getValue());
        Assert.assertNull(iQuery.findByQuery(sb.toString(), param));

        //Go to the target group
        EventContext ec2 = loginUser(g);
        Assert.assertEquals(g.getId().getValue(), ec2.groupId);
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));
        param = new ParametersI();
        param.addId(image.getId().getValue());

        //Check that the image has been moved.
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from RenderingDef i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(def.getId().getValue());
        Assert.assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Tests reverse intensity with Lut
     * @throws Exception
     */
    @Test
    public void testReverseIntensityWithLut() throws Exception {
        //First import an image
        Image image = createBinaryImage(1, 1, 1, 1, 1);
        Pixels p = image.getPrimaryPixels();
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        int end = re.getQuantumDef().getCdEnd().getValue();
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScriptsByMimetype(
                ScriptServiceTest.LUT_MIMETYPE);
        Assert.assertNotNull(scripts);
        Assert.assertTrue(CollectionUtils.isNotEmpty(scripts));
        OriginalFile of = scripts.get(0);
        RenderingModel model = re.getModel();
        List<IObject> models = factory.getPixelsService().getAllEnumerations(
                RenderingModel.class
                .getName());
        Iterator<IObject> j = models.iterator();
        RenderingModel m;
        // Change the color model so it is not grey scale.
        while (j.hasNext()) {
            m = (RenderingModel) j.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        for (int k = 0; k < channels.size(); k++) {
            re.setChannelLookupTable(k, of.getName().getValue());
            byte[] before = re.renderCompressed(pDef);
            re.addCodomainMapToChannel(new omero.romio.ReverseIntensityMapContext(), k);
            byte[] after = re.renderCompressed(pDef);
            Assert.assertFalse(Arrays.equals(after, before));
        }
    }

    /**
     * Tests reverse intensity with color
     * @throws Exception
     */
    @Test
    public void testReverseIntensityWithColor() throws Exception {
        //First import an image
        Image image = createBinaryImage(1, 1, 1, 1, 1);
        Pixels p = image.getPrimaryPixels();
        long id = p.getId().getValue();
        factory.getRenderingSettingsService().setOriginalSettingsInSet(
                Pixels.class.getName(), Arrays.asList(id));
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        PlaneDef pDef = new PlaneDef();
        pDef.t = re.getDefaultT();
        pDef.z = re.getDefaultZ();
        pDef.slice = omero.romio.XY.value;
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        List<ChannelBinding> channels = def.copyWaveRendering();
        RenderingModel model = re.getModel();
        List<IObject> models = factory.getPixelsService().getAllEnumerations(
                RenderingModel.class
                .getName());
        Iterator<IObject> j = models.iterator();
        RenderingModel m;
        // Change the color model so it is not grey scale.
        while (j.hasNext()) {
            m = (RenderingModel) j.next();
            if (m.getId().getValue() != model.getId().getValue())
                re.setModel(m);
        }
        Color color = Color.CYAN;
        for (int k = 0; k < channels.size(); k++) {
            re.setRGBA(k, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            byte[] before = re.renderCompressed(pDef);
            re.addCodomainMapToChannel(new omero.romio.ReverseIntensityMapContext(), k);
            byte[] after = re.renderCompressed(pDef);
            Assert.assertFalse(Arrays.equals(after, before));
        }
    }
}
