/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import omero.RLong;
import omero.api.IPixelsPrx;
import omero.api.IRenderingSettingsPrx;
import omero.model.AcquisitionMode;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.Channel;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.DetectorType;
import omero.model.DimensionOrder;
import omero.model.ExperimentType;
import omero.model.Family;
import omero.model.FilamentType;
import omero.model.FilterType;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.Immersion;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LogicalChannel;
import omero.model.Medium;
import omero.model.MicrobeamManipulationType;
import omero.model.MicroscopeType;
import omero.model.PhotometricInterpretation;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Pulse;
import omero.model.RenderingDef;
import omero.model.RenderingModel;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>Pixels</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class PixelsServiceTest extends AbstractServerTest {

    /**
     * The maximum number of elements for the <code>immersion</code>
     * enumeration.
     */
    private static final int MAX_IMMERSION = 8;

    /**
     * The maximum number of elements for the <code>correction</code>
     * enumeration.
     */
    private static final int MAX_CORRECTION = 15;

    /**
     * The maximum number of elements for the <code>medium</code> enumeration.
     */
    private static final int MAX_MEDIUM = 6;

    /**
     * The maximum number of elements for the <code>microscope type</code>
     * enumeration.
     */
    private static final int MAX_MICROSCOPE_TYPE = 6;

    /**
     * The maximum number of elements for the <code>detector type</code>
     * enumeration.
     */
    private static final int MAX_DETECTOR_TYPE = 15;

    /**
     * The maximum number of elements for the <code>filter type</code>
     * enumeration.
     */
    private static final int MAX_FILTER_TYPE = 8;

    /**
     * The maximum number of elements for the <code>binning</code> enumeration.
     */
    private static final int MAX_BINNING = 4;

    /**
     * The maximum number of elements for the <code>contrast method</code>
     * enumeration.
     */
    private static final int MAX_CONTRAST_METHOD = 10;

    /**
     * The maximum number of elements for the <code>illumination</code>
     * enumeration.
     */
    private static final int MAX_ILLUMINATION = 6;

    /**
     * The maximum number of elements for the
     * <code>photometric interpretation</code> enumeration.
     */
    private static final int MAX_PHOTOMETRIC_INTERPRETATION = 6;

    /**
     * The maximum number of elements for the <code>acquisition mode</code>
     * enumeration.
     */
    private static final int MAX_ACQUISITION_MODE = 21;

    /**
     * The maximum number of elements for the <code>laser medium</code>
     * enumeration.
     */
    private static final int MAX_LASER_MEDIUM = 35;

    /**
     * The maximum number of elements for the <code>laser type</code>
     * enumeration.
     */
    private static final int MAX_LASER_TYPE = 9;

    /**
     * The maximum number of elements for the <code>pulse</code> enumeration.
     */
    private static final int MAX_PULSE = 7;

    /**
     * The maximum number of elements for the <code>arc type</code> enumeration.
     */
    private static final int MAX_ARC_TYPE = 5;

    /**
     * The maximum number of elements for the <code>filament type</code>
     * enumeration.
     */
    private static final int MAX_FILAMENT_TYPE = 4;

    /**
     * The maximum number of elements for the <code>format</code> enumeration.
     */
    private static final int MAX_FORMAT = 173;

    /**
     * The maximum number of elements for the <code>family</code> enumeration.
     */
    private static final int MAX_FAMILY = 4;

    /**
     * The maximum number of elements for the <code>Pixels Type</code>
     * enumeration.
     */
    private static final int MAX_PIXELS_TYPE = 11;

    /**
     * The maximum number of elements for the <code>Rendering Model</code>
     * enumeration.
     */
    private static final int MAX_RENDERING_MODEL = 2;

    /**
     * The maximum number of elements for the <code>Dimension order</code>
     * enumeration.
     */
    private static final int MAX_DIMENSION_ORDER = 6;

    /**
     * The maximum number of elements for the <code>Microbeam manipulation
     * type</code> enumeration.
     */
    private static final int MAX_MICROBEAM_MANIPULATION_TYPE = 9;

    /**
     * The maximum number of elements for the <code>Experiment type</code>
     * enumeration.
     */
    private static final int MAX_EXPERIMENT_TYPE = 17;

    /**
     * Tests if the objects returned are of the specified type.
     *
     * @param name
     *            The type of object to retrieve.
     * @param max
     *            The number of objects to retrieve.
     */
    private void checkEnumeration(String name, int max) throws Exception {
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> values = svc.getAllEnumerations(name);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.size() >= max);
        Iterator<IObject> i = values.iterator();
        int count = 0;
        String v;
        name = name + "I"; // b/c we handle the instances of the class.
        while (i.hasNext()) {
            v = i.next().getClass().getName();
            if (name.equals(v))
                count++;
        }
        Assert.assertTrue(values.size() >= count);
    }

    /**
     * Tests the retrieval of the pixels description.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRetrievePixelsDescription() throws Exception {
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        long id = pixels.getId().getValue();
        Pixels p = factory.getPixelsService().retrievePixDescription(id);
        Assert.assertNotNull(p);
        Assert.assertEquals(pixels.getSizeX().getValue(), p.getSizeX().getValue());
        Assert.assertEquals(pixels.getSizeY().getValue(), p.getSizeY().getValue());
        Assert.assertEquals(pixels.getSizeT().getValue(),p.getSizeT().getValue());
        Assert.assertEquals(pixels.getSizeZ().getValue(), p.getSizeZ().getValue());
        Assert.assertEquals(pixels.getSizeC().getValue(), p.getSizeC().getValue());
        Assert.assertEquals(pixels.sizeOfChannels(), ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        Assert.assertEquals(p.sizeOfChannels(), pixels.sizeOfChannels());
        Assert.assertEquals(pixels.getPhysicalSizeX().getValue(), p.getPhysicalSizeX()
                .getValue());
        Assert.assertEquals(pixels.getPhysicalSizeY().getValue(), p.getPhysicalSizeY()
                .getValue());
        Assert.assertEquals(pixels.getPhysicalSizeZ().getValue(), p.getPhysicalSizeZ()
                .getValue());
        Assert.assertNotNull(pixels.getPixelsType());
        Assert.assertNotNull(p.getPixelsType());
        Assert.assertEquals(pixels.getPixelsType().getValue().getValue(),
                p.getPixelsType().getValue().getValue());
        Channel channel;
        LogicalChannel lc;
        List<Long> ids = new ArrayList<Long>();
        for (int j = 0; j < p.sizeOfChannels(); j++) {
            channel = p.getChannel(j);
            Assert.assertNotNull(channel);
            ids.add(channel.getId().getValue());
            Assert.assertNotNull(channel.getStatsInfo());
            lc = channel.getLogicalChannel();
            Assert.assertNotNull(lc);
            Assert.assertNotNull(lc.getContrastMethod().getValue().getValue());
            Assert.assertNotNull(lc.getIllumination().getValue().getValue());
            Assert.assertNotNull(lc.getMode().getValue().getValue());
        }
        for (int j = 0; j < pixels.sizeOfChannels(); j++) {
            channel = pixels.getChannel(j);
            Assert.assertNotNull(channel);
            Assert.assertTrue(ids.contains(channel.getId().getValue()));
            Assert.assertNotNull(channel.getStatsInfo());
        }
    }

    /**
     * Tests the retrieval of the possible enumerations.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testGetAllEnumerations() throws Exception {
        // for instrument
        checkEnumeration(Immersion.class.getName(), MAX_IMMERSION);
        checkEnumeration(Correction.class.getName(), MAX_CORRECTION);
        checkEnumeration(Medium.class.getName(), MAX_MEDIUM);
        checkEnumeration(MicroscopeType.class.getName(), MAX_MICROSCOPE_TYPE);
        checkEnumeration(DetectorType.class.getName(), MAX_DETECTOR_TYPE);
        checkEnumeration(FilterType.class.getName(), MAX_FILTER_TYPE);
        checkEnumeration(Binning.class.getName(), MAX_BINNING);
        checkEnumeration(ContrastMethod.class.getName(), MAX_CONTRAST_METHOD);
        checkEnumeration(Illumination.class.getName(), MAX_ILLUMINATION);
        checkEnumeration(PhotometricInterpretation.class.getName(),
                MAX_PHOTOMETRIC_INTERPRETATION);
        checkEnumeration(AcquisitionMode.class.getName(), MAX_ACQUISITION_MODE);
        checkEnumeration(LaserMedium.class.getName(), MAX_LASER_MEDIUM);
        checkEnumeration(LaserType.class.getName(), MAX_LASER_TYPE);
        checkEnumeration(Pulse.class.getName(), MAX_PULSE);
        checkEnumeration(ArcType.class.getName(), MAX_ARC_TYPE);
        checkEnumeration(FilamentType.class.getName(), MAX_FILAMENT_TYPE);
        checkEnumeration(Format.class.getName(), MAX_FORMAT);
        checkEnumeration(DimensionOrder.class.getName(), MAX_DIMENSION_ORDER);
        checkEnumeration(MicrobeamManipulationType.class.getName(),
                MAX_MICROBEAM_MANIPULATION_TYPE);
        checkEnumeration(ExperimentType.class.getName(), MAX_EXPERIMENT_TYPE);
        // for rendering engine
        checkEnumeration(Family.class.getName(), MAX_FAMILY);
        checkEnumeration(PixelsType.class.getName(), MAX_PIXELS_TYPE);
        checkEnumeration(RenderingModel.class.getName(), MAX_RENDERING_MODEL);
    }

    /**
     * Tests the retrieval of a specified rendering settings for a given set of
     * pixels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRetrieveRenderingSettings() throws Exception {
        // Create some rendering settings.
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        IPixelsPrx svc = factory.getPixelsService();
        RenderingDef def = svc.retrieveRndSettings(pixels.getId().getValue());
        Assert.assertNotNull(def);
        long id = iAdmin.getEventContext().userId;
        RenderingDef def1 = svc.retrieveRndSettingsFor(pixels.getId()
                .getValue(), id);
        Assert.assertNotNull(def1);
        Assert.assertEquals(def1.getId().getValue(), def.getId().getValue());
        def1 = svc.retrieveRndSettingsFor(pixels.getId().getValue(), id + 1);
        Assert.assertNull(def1);
    }

    /**
     * Tests the retrieval of rendering settings for a given set of pixels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRetrieveAllRenderingSettings() throws Exception {
        // Create some rendering settings.
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixels.getId().getValue()));
        IPixelsPrx svc = factory.getPixelsService();
        long id = iAdmin.getEventContext().userId;
        List<IObject> defs = svc.retrieveAllRndSettings(pixels.getId()
                .getValue(), id);
        Assert.assertNotNull(defs);
        Assert.assertEquals(1, defs.size());
    }

    /**
     * Tests the creation of an image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateImage() throws Exception {
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> types = svc
                .getAllEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        RLong id = svc.createImage(10, 10, 10, 10, channels,
                (PixelsType) types.get(1), "test", "");
        Assert.assertNotNull(id);
        // Retrieve the image.
        ParametersI param = new ParametersI();
        param.addId(id.getValue());
        Image img = (Image) iQuery.findByQuery(
                "select i from Image i where i.id = :id", param);
        Assert.assertNotNull(img);
    }

    /**
     * Tests the saving of rendering settings.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSaveRndSettings() throws Exception {
        // Create some rendering settings.
        Image image = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER);
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        // Pixels first
        long id = pixels.getId().getValue();
        prx.setOriginalSettingsInSet(Pixels.class.getName(), Arrays.asList(id));
        IPixelsPrx svc = factory.getPixelsService();
        // Already tested
        RenderingDef def = svc.retrieveRndSettings(id);
        Assert.assertNotNull(def);
        // change z
        int v = 1;
        def.setDefaultZ(omero.rtypes.rint(v));
        svc.saveRndSettings(def);
        // retrieve the settings.
        def = svc.retrieveRndSettings(id);
        Assert.assertEquals(def.getDefaultZ().getValue(), v);
    }

}
