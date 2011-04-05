/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ome.conditions.MissingPyramidException;
import ome.io.messages.MissingPyramidMessage;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the logic of
 * {@link PixelsService#getPixelBuffer(Pixels, String, OriginalFileMetadataProvider, boolean)}
 * which has gotten far more complex with the introduction pyramids to support
 * big images.
 *
 * @since 4.3
 */
public class GetPixelBufferUnitTest {

    private String root;

    private ome.model.core.Pixels pixels;

    private PixelBuffer pixelBuffer;

    private OriginalFileMetadataProvider provider;

    private MockPixelsService service;

    @BeforeMethod
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
        provider = new TestingOriginalFileMetadataProvider();
        pixels = new Pixels();

        pixels.setId(1L);
        pixels.setSizeX(1);
        pixels.setSizeY(1);
        pixels.setSizeZ(1);
        pixels.setSizeC(1);
        pixels.setSizeT(1);

        PixelsType type = new PixelsType();
        type.setValue("uint16");
        pixels.setPixelsType(type);

        service = new MockPixelsService(root);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(root));
    }

    @Test
    public void testWhenPyramidRequiredOnlyCreatePyramidCalled() {
        service.allowCreatePyramid = true;
        service.isRequirePyramid = true;
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(0, service.events.size());
    }

    /**
     * Since no pyramid is required and no ROMIO pixels file exists,
     * then a new one will be created (i.e. pixel buffer is READ-WRITE).
     */
    @Test
    public void testWhenPyramidNotRequiredCreateRomioCalled() {
        service.allowCreateRomio = true;
        service.isRequirePyramid = false;
        service.allowModification = true;
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(0, service.events.size());
    }

    /**
     * This test creates the ROMIO pixels file while requiring a pyramid which
     * forces an exception to be thrown.
     */
    @Test(expectedExceptions = MissingPyramidException.class)
    public void testPyramidMessagePublishedIfPixelsExist() throws Exception {
        service.allowCreatePyramid = true;
        service.isRequirePyramid = true;
        service.retry = false;
        String path = touchRomio();
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(1, service.events.size());
    }

    @Test
    public void testPyramidMessageNotPublishedIfPyramidExists()
            throws Exception {
        service.allowCreatePyramid = true;
        service.isRequirePyramid = true;
        service.retry = null; // no messages!
        touchRomio();
        touchPyramid();
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(0, service.events.size());
    }

    /**
     * This tests whether or not a pyramid is used even when it is not required.
     */
    @Test
    public void testReusePyramid() throws Exception {
        service.allowCreatePyramid = true;
        service.isRequirePyramid = false;
        String path = service.getPixelsPath(pixels.getId());
        touchPyramid();
        pixelBuffer = service.getPixelBuffer(pixels, provider, true);
        assertEquals(0, service.events.size());
    }

    /**
     * By passing a null to getPixelBuffer for the string, one sets useRomio.
     * Here we pass an absolute path, asking to NOT use romio.
     */
    @Test
    public void testDontUseRomio() {
        service.allowCreateBf = true;
        service.isRequirePyramid = false;
        File bf = new File(root, "bf.tiff");
        pixelBuffer = service.getPixelBuffer(pixels, bf.getAbsolutePath(),
                provider, true);
        assertEquals(0, service.events.size());
    }

    @Test
    public void testHandleOriginalFileReturnsSomething() {
        service.allowHandle = true;
        pixelBuffer = service.getPixelBuffer(pixels, null, provider, false);
        assertEquals(0, service.events.size());
    }

    /**
     * If handleOriginalFile returns a null, then a PB must be created. In this
     * case, another method must be allowed. Here, it is createPyramid.
     */
    @Test
    public void testHandleOriginalFileReturnsNullForPyramid() {
        service.allowHandle = true;
        service.allowCreatePyramid = true;
        service.pb = null;
        pixelBuffer = service.getPixelBuffer(pixels, null, provider, false);
        assertEquals(0, service.events.size());
    }

    /**
     * Like {@link #testHandleOriginalFileReturnsNullForPyramid()} but here no
     * pyramid is required, so createRomio is called WITH allowModification set.
     */
    @Test
    public void testHandleOriginalFileReturnsNullForRomio() {
        service.allowHandle = true;
        service.allowCreateRomio = true;
        service.isRequirePyramid = false;
        service.allowModification = true;
        service.pb = null;
        pixelBuffer = service.getPixelBuffer(pixels, null, provider, false);
        assertEquals(0, service.events.size());
    }

    /**
     * This is the traditional case: a ROMIO pixels file exists and there is not
     * pyramid, nor is one required and bypassOriginalFile is true, i.e. don't
     * look up anything. Returned PixelBuffer should be READ-ONLY.
     */
    @Test
    public void testPixelsFileExistsNoPyramid() throws Exception {
        service.allowCreateRomio = true;
        service.isRequirePyramid = false;
        touchRomio();
        pixelBuffer = service.getPixelBuffer(pixels, null, provider, true);
        assertEquals(0, service.events.size());
    }

    //
    // Helpers
    //

    private String makePyramidPath(String path) {
        String pyramidPath = path + PixelsService.PYRAMID_SUFFIX;
        return pyramidPath;
    }

    private void touchPyramid() throws IOException {
        String path = service.getPixelsPath(pixels.getId());
        String pyramidPath = makePyramidPath(path);
        FileUtils.touch(new File(pyramidPath));
    }

    private String touchRomio() throws IOException {
        String path = service.getPixelsPath(pixels.getId());
        FileUtils.touch(new File(path));
        return path;
    }

    static class MockPixelsService extends PixelsService {

        /** Whether or not this image is "big" */
        boolean isRequirePyramid = true;

        /** Whether or not to allow {@link #createPyramidPixelBuffer(String)} */
        boolean allowCreatePyramid = false;

        /** Whether or not to allow {@link #createBfPixelBuffer(String)} */
        boolean allowCreateBf = false;

        /**
         * Whether or not to allow
         * {@link #createRomioPixelBuffer(String, Pixels, boolean)}
         */
        boolean allowCreateRomio = false;

        /**
         * Whether a call to
         * {@link #createRomioPixelBuffer(String, Pixels, boolean)} should have
         * the last parameter set to true or not.
         */
        boolean allowModification = false;

        /**
         * Whether or not
         * {@link #handleOriginalFile(Pixels, OriginalFileMetadataProvider) can
         * be called.
         */
        boolean allowHandle = false;

        /**
         * If null, then no call should be made to
         * {@link #handleMissingPyramid(Pixels, String)}. Otherwise, the value
         * will determine whether or not
         * {@link MissingPyramidMessage#setRetry()} is called.
         */
        Boolean retry = null;

        Mock pbMock = new Mock(PixelBuffer.class);

        /**
         * Returned value from all the create methods. Typically ignored.
         * Setting it to null, however, will influence the outcome of calls to
         * {@link #handleOriginalFile(Pixels, OriginalFileMetadataProvider)}.
         */
        PixelBuffer pb = (PixelBuffer) pbMock.proxy();

        /**
         * Storage of all events which are raised.
         */
        final List<ApplicationEvent> events = new ArrayList<ApplicationEvent>();

        MockPixelsService(String root) {
            super(root);
            setApplicationEventPublisher(new ApplicationEventPublisher() {
                public void publishEvent(ApplicationEvent arg0) {
                    MissingPyramidMessage mpm = (MissingPyramidMessage) arg0;
                    events.add(mpm);
                    if (retry == null) {
                        fail("Should not publish message");
                    } else if (retry) {
                        mpm.setRetry();
                    }
                }
            });
        }

        protected boolean isRequirePyramid(Pixels pixels) {
            return isRequirePyramid;
        }

        @Override
        protected PixelBuffer createPyramidPixelBuffer(Pixels pixels, String filePath) {
            if (!allowCreatePyramid) {
                fail("createPyramid should not be called");
            }
            return pb;
        }

        @Override
        protected PixelBuffer createBfPixelBuffer(String filePath) {
            if (!allowCreateBf) {
                fail("createBf should not be called");
            }
            return pb;
        }

        @Override
        protected PixelBuffer createRomioPixelBuffer(String pixelsFilePath,
                Pixels pixels, boolean allowModification) {
            if (!allowCreateRomio) {
                fail("createRomio should not be called");
            }
            assertEquals(this.allowModification, allowModification);
            return pb;
        }

        @Override
        protected PixelBuffer handleOriginalFile(Pixels pixels,
                OriginalFileMetadataProvider provider) {
            if (!allowHandle) {
                fail("handleOriginalFile should not be called");
            }
            return pb;
        }

    }

}
