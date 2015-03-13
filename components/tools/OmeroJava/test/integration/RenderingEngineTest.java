/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import omero.api.IPixelsPrx;
import omero.api.RenderingEnginePrx;
import omero.model.ChannelBinding;
import omero.model.Family;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.model.RenderingModel;
import omero.romio.PlaneDef;
import omero.romio.RGBBuffer;
import omero.romio.RegionDef;
import omero.sys.EventContext;

import org.testng.annotations.Test;

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

        if (preload) {
            assertRendering(pixels); // View as owner
        }

        disconnect();
        // login as another user.
        EventContext ctx2 = newUserInGroup(ctx);
        switch (role) {
            case ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case GROUP_OWNER:
                makeGroupOwner();
        }
        assertRendering(pixels);
    }

    private void assertRendering(List<Pixels> pixels) throws Exception {
        Pixels p = pixels.get(0);
        long id = p.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(id);
        if (!(re.lookupRenderingDef(id))) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(id);
        }
        re.load();
        int t = re.getDefaultT();
        int v = t + 1;
        re.setDefaultT(v);
        re.saveCurrentSettings();
        assertEquals(re.getDefaultT(), v);
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        assertEquals(def.getDefaultT().getValue(), v);
        re.close();
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
            assertEquals(region[i], plane[j]);
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
            assertEquals(region[i], plane[j]);
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
            fail("We should not have been able to load it.");
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
        assertEquals(def.getDefaultZ().getValue(), re.getDefaultZ());
        assertEquals(def.getDefaultT().getValue(), re.getDefaultT());
        assertEquals(def.getModel().getValue().getValue(), re.getModel()
                .getValue().getValue());
        QuantumDef q1 = def.getQuantization();
        QuantumDef q2 = re.getQuantumDef();
        assertNotNull(q1);
        assertNotNull(q2);
        assertEquals(q1.getBitResolution().getValue(), q2.getBitResolution()
                .getValue());
        assertEquals(q1.getCdStart().getValue(), q2.getCdStart().getValue());
        assertEquals(q1.getCdEnd().getValue(), q2.getCdEnd().getValue());
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        int[] rgba;
        while (i.hasNext()) {
            c1 = i.next();
            rgba = re.getRGBA(index);
            assertEquals(c1.getRed().getValue(), rgba[0]);
            assertEquals(c1.getGreen().getValue(), rgba[1]);
            assertEquals(c1.getBlue().getValue(), rgba[2]);
            assertEquals(c1.getAlpha().getValue(), rgba[3]);
            assertEquals(c1.getCoefficient().getValue(),
                    re.getChannelCurveCoefficient(index));
            assertEquals(c1.getFamily().getValue().getValue(), re
                    .getChannelFamily(index).getValue().getValue());
            assertEquals(c1.getInputStart().getValue(),
                    re.getChannelWindowStart(index));
            assertEquals(c1.getInputEnd().getValue(),
                    re.getChannelWindowEnd(index));
            Boolean b1 = Boolean.valueOf(c1.getActive().getValue());
            Boolean b2 = Boolean.valueOf(re.isActive(index));
            assertTrue(b1.equals(b2));
            b1 = Boolean.valueOf(c1.getNoiseReduction().getValue());
            b2 = Boolean.valueOf(re.getChannelNoiseReduction(index));
            assertTrue(b1.equals(b2));
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
        assertEquals(re.getDefaultT(), v);
        v = def.getDefaultZ().getValue() + 1;
        re.setDefaultZ(v);
        assertEquals(re.getDefaultZ(), v);

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
        assertEquals(re.getModel().getId().getValue(), model.getId().getValue());
        QuantumDef qdef = def.getQuantization();
        int start = qdef.getCdStart().getValue() + 10;
        int end = qdef.getCdEnd().getValue() - 10;
        re.setCodomainInterval(start, end);
        assertEquals(re.getQuantumDef().getCdStart().getValue(), start);
        assertEquals(re.getQuantumDef().getCdEnd().getValue(), end);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        assertNotNull(channels1);
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
            assertTrue(Boolean.valueOf(b).equals(
                    Boolean.valueOf(re.isActive(index))));
            s = c1.getInputStart().getValue() + 1;
            e = c1.getInputEnd().getValue() + 1;
            re.setChannelWindow(index, s, e);
            assertEquals(re.getChannelWindowStart(index), s);
            assertEquals(re.getChannelWindowEnd(index), e);
            b = !c1.getNoiseReduction().getValue();
            re.setRGBA(index, RGBA[0], RGBA[1], RGBA[2], RGBA[3]);
            rgba = re.getRGBA(index);
            for (int k = 0; k < rgba.length; k++) {
                assertTrue(rgba[k] == RGBA[k]);
            }
            b = !c1.getNoiseReduction().getValue();
            re.setQuantizationMap(index, f, coefficient, b);
            assertTrue(Boolean.valueOf(re.getChannelNoiseReduction(index))
                    .equals(Boolean.valueOf(b)));
            assertEquals(re.getChannelCurveCoefficient(index), coefficient);
            assertEquals(re.getChannelFamily(index).getId().getValue(), f
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
        assertEquals(re.getDefaultT(), def.getDefaultT().getValue());
        re.resetDefaultSettings(false);
        assertEquals(re.getDefaultT(), t);
        // reload from db
        def = factory.getPixelsService().retrieveRndSettings(id);
        assertEquals(def.getDefaultT().getValue(), v);
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
        assertEquals(re.getDefaultT(), def.getDefaultT().getValue());
        re.resetDefaultSettings(true);
        assertEquals(re.getDefaultT(), t);
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
        int t = re.getDefaultT();
        int v = t + 1;
        re.setDefaultT(v);
        re.saveCurrentSettings();
        assertTrue(re.getDefaultT() == v);
        RenderingDef def = factory.getPixelsService().retrieveRndSettings(id);
        assertEquals(def.getDefaultT().getValue(), v);
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
        assertNotNull(buffer);
        assertEquals(p.getSizeX().getValue(), buffer.sizeX1);
        assertEquals(p.getSizeY().getValue(), buffer.sizeX2);
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
        assertNotNull(buffer);
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
            assertNotNull(bufferRegion);
            assertEquals(r.width, bufferRegion.sizeX1);
            assertEquals(r.height, bufferRegion.sizeX2);
            // now render a plane and compare the renderer value.
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            bufferPlane = re.render(pDef);
            assertNotNull(bufferPlane);
            // red band
            region = bufferRegion.bands[0];
            plane = bufferPlane.bands[0];
            assertNotNull(region);
            assertNotNull(plane);
            checkBuffer(region, plane, sizeX - r.width, r.width);
            // green band
            region = bufferRegion.bands[1];
            plane = bufferPlane.bands[1];
            assertNotNull(region);
            assertNotNull(plane);
            checkBuffer(region, plane, sizeX - r.width, r.width);
            // blue band
            region = bufferRegion.bands[2];
            plane = bufferPlane.bands[2];
            assertNotNull(region);
            assertNotNull(plane);
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
        assertNotNull(bufferRegion);
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
        assertNotNull(bufferRegion);
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

        assertNotNull(values);
        // Create a buffered image.
        BufferedImage image = createImage(values);
        assertNotNull(image);
        assertEquals(image.getWidth(), p.getSizeX().getValue());
        assertEquals(image.getHeight(), p.getSizeY().getValue());
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
        assertNotNull(region);
        imageRegion = createImage(region);
        assertNotNull(imageRegion);
        assertEquals(r.width, imageRegion.getWidth());
        assertEquals(r.height, imageRegion.getHeight());
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
        assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, p.getSizeX().getValue(),
                p.getSizeY().getValue());
        assertNotNull(image);

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
        assertNotNull(buffer);
        image = createImage(buffer, 32, p.getSizeX().getValue(), p.getSizeY()
                .getValue());
        assertNotNull(image);
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
            assertNotNull(region);
            imageRegion = createImage(region, 32, r.width, r.height);
            assertNotNull(imageRegion);
            assertEquals(r.width, imageRegion.getWidth());
            assertEquals(r.height, imageRegion.getHeight());
            // now render a plane and compare the renderer value.
            pDef = new PlaneDef();
            pDef.t = re.getDefaultT();
            pDef.z = re.getDefaultZ();
            pDef.slice = omero.romio.XY.value;
            plane = re.renderAsPackedInt(pDef);
            assertNotNull(plane);
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
        assertNotNull(region);
        imageRegion = createImage(region, 32, r.width, r.height);
        assertNotNull(imageRegion);
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
        assertNotNull(region);
        imageRegion = createImage(region, 32, r.width, r.height);
        assertNotNull(imageRegion);
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
        assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, sizeX, sizeY);
        assertNotNull(image);
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
        assertNotNull(buffer);
        BufferedImage image = createImage(buffer, 32, sizeX, sizeY);
        assertNotNull(image);
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
        assertNotNull(buffer);
        BufferedImage image = createImage(buffer);
        assertNotNull(image);
        assertEquals(image.getWidth(), sizeX);
        assertEquals(image.getHeight(), sizeY);
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
        assertNotNull(buffer);
        BufferedImage image = createImage(buffer);
        assertNotNull(image);
        assertEquals(image.getWidth(), sizeX);
        assertEquals(image.getHeight(), sizeY);
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
        assertNotNull(buffer);
        assertEquals(sizeX, buffer.sizeX1);
        assertEquals(sizeY, buffer.sizeX2);

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
        assertNotNull(buffer);
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
        assertNotNull(buffer);
        assertEquals(sizeX, buffer.sizeX1);
        assertEquals(sizeY, buffer.sizeX2);

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
        assertNotNull(buffer);
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
        assertNotNull(bufferRegion);
        assertEquals(w, bufferRegion.sizeX1);
        assertEquals(h, bufferRegion.sizeX2);
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
        assertNotNull(bufferRegion);
        BufferedImage image = createImage(bufferRegion, 32, w, h);
        assertNotNull(image);
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
         * (i == 0) time = end; else assertTrue(end >= (time-diff) & end <=
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
        assertEquals(re.getDefaultZ(), z);
        // t
        int t = 1;
        re.setDefaultT(t);
        re.saveCurrentSettings();
        assertEquals(re.getDefaultT(), t);
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
        assertEquals(re.getModel().getId().getValue(), rm.getId().getValue());

        int start = re.getQuantumDef().getCdStart().getValue() + 10;
        int end = re.getQuantumDef().getCdEnd().getValue() - 10;
        re.setCodomainInterval(start, end);
        re.saveCurrentSettings();
        assertEquals(re.getQuantumDef().getCdStart().getValue(), start);
        int DEPTH_7BIT = 127;
        re.setQuantumStrategy(DEPTH_7BIT);
        re.saveCurrentSettings();
        assertEquals(re.getQuantumDef().getBitResolution().getValue(),
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
            assertEquals(j == 0, re.isActive(j));
            re.setChannelWindow(j, min, max);
            re.saveCurrentSettings();
            assertEquals(re.getChannelWindowStart(j), min);
            assertEquals(re.getChannelWindowEnd(j), max);
            // color
            re.setRGBA(j, RGBA[0], RGBA[1], RGBA[2], RGBA[3]);
            re.saveCurrentSettings();
            rgba = re.getRGBA(j);
            for (int k = 0; k < rgba.length; k++) {
                assertEquals(rgba[k], RGBA[k]);
            }
            b = !re.getChannelNoiseReduction(j);
            re.setQuantizationMap(j, f, coefficient, b);
            re.saveCurrentSettings();
            assertEquals(re.getChannelCurveCoefficient(j), coefficient);
            assertEquals(re.getChannelNoiseReduction(j), b);
            assertEquals(re.getChannelFamily(j).getId().getValue(), f.getId()
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
        QuantumDef q1 = def.getQuantization();
        assertNotNull(q1);
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        assertNotNull(channels1);
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
            assertEquals(s, re.getChannelWindowStart(index));
            assertEquals(e, re.getChannelWindowEnd(index));
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
        List<ChannelBinding> channels1 = def.copyWaveRendering();
        assertNotNull(channels1);
        Iterator<ChannelBinding> i = channels1.iterator();
        ChannelBinding c1;
        int index = 0;
        while (i.hasNext()) {
            c1 = i.next();
            assertEquals(null, c1.getLookupTable());
            re.setChannelLookupTable(index, "foo");
            assertEquals("foo", re.getChannelLookupTable(index));
            index++;
        }
        re.saveCurrentSettings();
        re.close();
    }
}
