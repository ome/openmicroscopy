/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.formats.FormatException;

import ome.conditions.MissingPyramidException;
import ome.conditions.ResourceError;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.messages.MissingPyramidMessage;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.DefaultResultStub;
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
public class GetPixelBufferUnitTest extends MockObjectTestCase {

    private String root;

    private ome.model.core.Pixels pixels;

    private PixelBuffer pixelBuffer;

    private MockPixelsService service;

    @BeforeMethod
    private void setup() {
        root = PathUtil.getInstance().getTemporaryDataFilePath();
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
        service.stubPyramid(true);
        service.isRequirePyramid = true;
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    /**
     * Since no pyramid is required and no ROMIO pixels file exists,
     * then a new one will be created (i.e. pixel buffer is READ-WRITE).
     */
    @Test
    public void testWhenPyramidNotRequiredCreateRomioCalled() {
        service.stubRomio(true);
        service.isRequirePyramid = false;
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    /**
     * This test creates the ROMIO pixels file while requiring a pyramid which
     * forces an exception to be thrown.
     */
    @Test(expectedExceptions = MissingPyramidException.class)
    public void testPyramidMessagePublishedIfPixelsExist() throws Exception {
        service.stubPyramid(false);
        service.isRequirePyramid = true;
        service.retry = false;
        String path = touchRomio();
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(1, service.events.size());
    }

    @Test
    public void testPyramidMessageNotPublishedIfPyramidExists()
            throws Exception {
        service.stubPyramid(false);
        service.isRequirePyramid = true;
        service.retry = null; // no messages!
        touchRomio();
        touchPyramid();
        pixelBuffer = service.getPixelBuffer(pixels, false);
        assertEquals(0, service.events.size());
    }

    /**
     * This tests whether or not a pyramid is used even when it is not required.
     */
    @Test
    public void testReusePyramid() throws Exception {
        service.stubPyramid(false);
        service.isRequirePyramid = false;
        String path = service.getPixelsPath(pixels.getId());
        touchPyramid();
        pixelBuffer = service.getPixelBuffer(pixels, false);
        assertEquals(0, service.events.size());
    }

    /**
     * By passing a null to getPixelBuffer for the string, one sets useRomio.
     * Here we pass an absolute path, asking to NOT use romio.
     */
    @Test(expectedExceptions = MissingPyramidException.class)
    public void testDontUseRomio() {
        service.stubBf();
        service.isRequirePyramid = false; // Ignored; Original implies pyramid
        service.retry = false;
        service.path = new File(root, "bf.tiff").getAbsolutePath();
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    /** OriginalFile != null implies pyramid required currently. */
    @Test(expectedExceptions = MissingPyramidException.class)
    public void testHandleOriginalFileReturnsSomething() {
        service.stubBf();
        service.isRequirePyramid = false; // ignored.
        service.retry = false;
        service.path = "/tmp/foo";
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(1, service.events.size());
    }

    /**
     * If handleOriginalFile returns a null, then a PB must be created. In this
     * case, another method must be allowed. Here, it is createPyramid.
     */
    @Test
    public void testHandleOriginalFileReturnsNullForPyramid() {
        service.realPyramid(null);
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    /**
     * Like {@link #testHandleOriginalFileReturnsNullForPyramid()} but here no
     * pyramid is required, so createRomio is called WITH allowModification set.
     */
    @Test
    public void testHandleOriginalFileReturnsNullForRomio() {
        service.stubRomio(true);
        service.retry = false;
        service.isRequirePyramid = false;
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    /**
     * This is the traditional case: a ROMIO pixels file exists and there is not
     * pyramid, nor is one required and bypassOriginalFile is true, i.e. don't
     * look up anything. Returned PixelBuffer should be READ-ONLY.
     */
    @Test
    public void testPixelsFileExistsNoPyramid() throws Exception {
        service.stubRomio(false);
        service.isRequirePyramid = false;
        touchRomio();
        pixelBuffer = service.getPixelBuffer(pixels);
        assertEquals(0, service.events.size());
    }

    //
    // #4731 makePyramid
    //

    @Test
    public void testMakePyramidNoRomioNoArchived() throws Exception {
        service.stubPyramid(true); // 5189. allow creation for close to touch.
        assertEquals(null, service.makePyramid(pixels));
    }

    @Test
    public void testMakePyramidRomio() throws Exception {
        touchRomio();
        service.stubPyramid(true);
        service.stubRomio(false);
        service.makePyramid(pixels);
    }

    //
    // #5189 Fatal error issues to prevent loops
    //

    /**
     * Here a pyramid cannot be created because no romio file exists. After
     * the invocation, the pyramid file should exist but be empty.
     */
    @Test
    public void testFatalErrorInGeneratingPyramidNoRomio() throws Exception {
        service.stubPyramid(true);
        service.stubRomio(false);
        assertEquals(null, sizePyramid());
        File pyramid = filePyramid();
        if (!pyramid.getParentFile().exists()) {
            pyramid.getParentFile().mkdirs();
        }
        service.pyramid.buffer = new BfPyramidPixelBuffer(pixels, pyramid.getAbsolutePath(), true);
        try {
            service.makePyramid(pixels);
            assertEquals(0, sizePyramid().longValue());

            assertCorruptPyramid(pyramid);
        } finally {
            service.pyramid.buffer.close();
        }
    }

    /**
     * Here we try the creation with a corrupt romio file.
     */
    @Test
    public void testFatalErrorInGeneratingPyramidWithRomio() throws Exception {
        touchRomio();
        service.stubPyramid(true);
        Mock m = service.mockRomio(false);
        m.expects(once()).method("close");
        m.expects(atLeastOnce()).method("getSizeX").will(returnValue(pixels.getSizeX()));
        m.expects(atLeastOnce()).method("getSizeY").will(returnValue(pixels.getSizeY()));
        m.expects(atLeastOnce()).method("getSizeZ").will(returnValue(pixels.getSizeZ()));
        m.expects(atLeastOnce()).method("getSizeC").will(returnValue(pixels.getSizeC()));
        m.expects(atLeastOnce()).method("getSizeT").will(returnValue(pixels.getSizeT()));
        m.expects(once()).method("getTile").will(throwException(new IOException("MOCK")));

        assertEquals(null, sizePyramid());
        File pyramid = filePyramid();
        service.pyramid.buffer = new BfPyramidPixelBuffer(pixels, pyramid.getAbsolutePath(), true);
        try {
            service.makePyramid(pixels);
            assertEquals(0, sizePyramid().longValue());

            assertCorruptPyramid(pyramid);
        } finally {
            service.pyramid.buffer.close();
        }
    }

    private void assertCorruptPyramid(File pyramid) throws IOException,
            FormatException {

        BfPyramidPixelBuffer pb = null;
        try {
            pb = new BfPyramidPixelBuffer(pixels, pyramid.getAbsolutePath(), false);
            assertEquals(false, pb.isWrite());
            pb.getTileSize();
            fail("Should have thrown a resource error");
        } catch (ResourceError re) {
            // ok
        } finally {
            if (pb != null) {
                pb.close();
                pb = null;
            }
        }
    }



    //
    // Helpers
    //

    private String makePyramidPath(String path) {
        String pyramidPath = path + PixelsService.PYRAMID_SUFFIX;
        return pyramidPath;
    }

    private File filePyramid() {
        String path = service.getPixelsPath(pixels.getId());
        String pyramidPath = makePyramidPath(path);
        File pyramidFile = new File(pyramidPath);
        return pyramidFile;
    }

    /** Returns null if the file does not exist */
    private Long sizePyramid() throws Exception {
        File pyramidFile = filePyramid();
        if (pyramidFile.exists()) {
            return pyramidFile.length();
        } else {
            return null;
        }
    }

    private void touchPyramid() throws IOException {
        File pyramidFile = filePyramid();
        FileUtils.touch(pyramidFile);
    }

    private File fileRomio() throws Exception {
        String path = service.getPixelsPath(pixels.getId());
        return new File(path);

    }

    private String touchRomio() throws Exception {
        File romioFile = fileRomio();
        FileUtils.touch(romioFile);
        return romioFile.getAbsolutePath();
    }

    /**
     * Container for the return value of MockPixelsService.
     */
    static class Buffer {

        Mock mock;

        /**
         * Returned value from all the create methods. Typically ignored.
         * Setting it to null, however, will influence the outcome of calls to
         * {@link #handleOriginalFile(Pixels, OriginalFileMetadataProvider)}.
         */
        PixelBuffer buffer;

        /**
         * If null, then modification is not checked. Otherwise, must match
         * the value passed in.
         */
        Boolean modification;

        Buffer() {
            this(true);
        }

        Buffer(boolean stub) {
            mock = new Mock(PixelBuffer.class);
            buffer = (PixelBuffer) mock.proxy();
            if (stub) {
                mock.setDefaultStub(new DefaultResultStub());
            }
        }
    }

    static class MockPixelsService extends PixelsService {

        /** Whether or not this image is "big" */
        boolean isRequirePyramid = true;

        /** If null, then createPyramid is not allowed */
        Buffer pyramid;

        /** If null, then createRomio is not allowed */
        Buffer romio;

        /** If null, then createBf is not allowed */
        Buffer bf;

        /**
         * If null, then no call should be made to
         * {@link #handleMissingPyramid(Pixels, String)}. Otherwise, the value
         * will determine whether or not
         * {@link MissingPyramidMessage#setRetry()} is called.
         */
        Boolean retry = null;

        /**
         * Path to be returned by invocations of {@link #getOriginalFilePath(Pixels)}
         */
        String path = null;

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

        public boolean requiresPixelsPyramid(Pixels pixels) {
            return isRequirePyramid;
        }

        @Override
        protected PixelBuffer createPyramidPixelBuffer(Pixels pixels, String filePath, boolean write) {
            return get("createPyramid", pyramid, write);
        }

        @Override
        protected PixelBuffer createBfPixelBuffer(String filePath, int series) {
            return get("createBf", bf, null);
        }

        @Override
        protected PixelBuffer createRomioPixelBuffer(String pixelsFilePath,
                Pixels pixels, boolean allowModification) {
            return get("createRomio", romio, allowModification);
        }

        @Override
        protected String getOriginalFilePath(Pixels pixels) {
            return path;
        }

        //
        // Test only methods
        //

        protected PixelBuffer get(String method, Buffer buffer, Boolean modification) {
            if (buffer == null) {
                fail(method + " should not be called");
            }
            if (buffer.modification != null) {
                assertEquals(buffer.modification, modification);
            }
            return buffer.buffer;
        }

        protected void realPyramid(PixelBuffer pb) {
            pyramid = new Buffer();
            pyramid.buffer = pb;
        }

        protected void stubPyramid(boolean write) {
            pyramid = new Buffer();
            pyramid.modification = write;
        }

        protected void stubRomio(boolean write) {
            romio = new Buffer();
            romio.modification = write;
        }

        protected Mock mockRomio(boolean write) {
            romio = new Buffer(false);
            return romio.mock;
        }

        protected void stubBf() {
            bf = new Buffer();
        }

    }

}
