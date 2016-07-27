/*
 *   Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import loci.formats.ImageReader;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.RomioPixelBuffer;
import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.model.Pixels;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class BfPixelBufferTest extends AbstractServerTest {

    private BfPixelBuffer bf;

    private RawPixelsStorePrx rps;

    private File destPath;

    private String destFileName;

    private void setUpTestFile(String fileName) throws Throwable,
            NoSuchAlgorithmException {
        File srcFile = ResourceUtils.getFile("classpath:" + fileName);

        // Import file
        List<Pixels> pixList = importFile(srcFile, fileName);
        log.debug(String.format("Imported: %s, pixid: %d", srcFile, pixList
                .get(0).getId().getValue()));

        // Access the imported pixels via a RawPixelsStore
        rps = factory.createRawPixelsStore();
        rps.setPixelsId(pixList.get(0).getId().getValue(), false);

        // Access the data from file via BfPixelBuffer
        destFileName = srcFile.getCanonicalPath();
        bf = new BfPixelBuffer(destFileName, new ImageReader());
    }

    private void tidyUp() throws Throwable {
        FileUtils.deleteQuietly(destPath);
        rps.close();
    }

    public void testDV() throws Throwable {
        log.debug(String.format("DV test."));
        setUpTestFile("tinyTest.d3d.dv");
        testOtherGetters();
        testDimensionGetters();
        testSizeGetters();
        testOffsetGettersZero();
        testOffsetGetters();
        testCheckBounds();
        tidyUp();
    }

    public void testDVpixels() throws Throwable {
        log.debug(String.format("DV pixels test."));
        setUpTestFile("tinyTest.d3d.dv");
        testgetTimepointDirect();
        testgetStackDirect();
        testgetPlaneDirect();
        testgetPlaneAsHypercube();
        testgetRowDirect();
        testgetColDirect();
        tidyUp();
    }

    public void testJPG() throws Throwable {
        log.debug(String.format("JPG test."));
        setUpTestFile("test.jpg");
        testOtherGetters();
        testDimensionGetters();
        testSizeGetters();
        testOffsetGettersZero();
        testOffsetGetters();
        testCheckBounds();
        tidyUp();
    }

    public void testJPGpixels() throws Throwable {
        log.debug(String.format("JPG pixels test."));
        setUpTestFile("test.jpg");
        testgetTimepointDirect();
        testgetStackDirect();
        testgetPlaneDirect();
        testgetPlaneAsHypercube();
        testgetRowDirect();
        testgetColDirect();
        tidyUp();
    }

    public void testBMP() throws Throwable {
        log.debug(String.format("BMP test."));
        setUpTestFile("test.bmp");
        testOtherGetters();
        testDimensionGetters();
        testSizeGetters();
        testOffsetGettersZero();
        testOffsetGetters();
        testCheckBounds();
        tidyUp();
    }

    public void testBMPpixels() throws Throwable {
        log.debug(String.format("BMP pixels test."));
        setUpTestFile("test.bmp");
        testgetTimepointDirect();
        testgetStackDirect();
        testgetPlaneDirect();
        testgetPlaneAsHypercube();
        testgetRowDirect();
        testgetColDirect();
        tidyUp();
    }

    private void testgetColDirect() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int midX = bf.getSizeX() / 2;
        int midZ = bf.getSizeZ() / 2;
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        byte[] buff1 = new byte[bf.getColSize()];
        byte[] buff2 = new byte[bf.getColSize()]; // rps has no getColSize
        bf.getColDirect(midX, midZ, midC, midT, buff1);
        buff2 = rps.getCol(midX, midZ, midC, midT);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetRowDirect() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int midY = bf.getSizeY() / 2;
        int midZ = bf.getSizeZ() / 2;
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        byte[] buff1 = new byte[bf.getRowSize()];
        byte[] buff2 = new byte[rps.getRowSize()];
        bf.getRowDirect(midY, midZ, midC, midT, buff1);
        buff2 = rps.getRow(midY, midZ, midC, midT);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetPlaneDirect() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int midZ = bf.getSizeZ() / 2;
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        byte[] buff1 = new byte[bf.getPlaneSize().intValue()];
        byte[] buff2 = new byte[RomioPixelBuffer.safeLongToInteger(rps
                .getPlaneSize())];
        bf.getPlaneDirect(midZ, midC, midT, buff1);
        buff2 = rps.getPlane(midZ, midC, midT);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetPlaneAsHypercube() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int sizeX = bf.getSizeX();
        int sizeY = bf.getSizeY();
        int midZ = bf.getSizeZ() / 2;
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        List<Integer> offset = Arrays.asList(new Integer[] { 0, 0, midZ, midC,
                midT });
        List<Integer> size = Arrays.asList(new Integer[] { sizeX, sizeY, 1, 1,
                1 });
        List<Integer> step = Arrays.asList(new Integer[] { 1, 1, 1, 1, 1 });
        byte[] buff1 = new byte[bf.getPlaneSize().intValue()];
        byte[] buff2 = new byte[bf.getPlaneSize().intValue()];
        bf.getPlaneDirect(midZ, midC, midT, buff1);
        bf.getHypercubeDirect(offset, size, step, buff2);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetStackDirect() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        byte[] buff1 = new byte[bf.getStackSize().intValue()];
        byte[] buff2 = new byte[RomioPixelBuffer.safeLongToInteger(rps
                .getStackSize())];
        bf.getStackDirect(midC, midT, buff1);
        buff2 = rps.getStack(midC, midT);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetTimepointDirect() throws IOException,
            DimensionsOutOfBoundsException, ServerError {
        int midT = bf.getSizeT() / 2;
        byte[] buff1 = new byte[bf.getTimepointSize().intValue()];
        byte[] buff2 = new byte[RomioPixelBuffer.safeLongToInteger(rps
                .getTimepointSize())];
        bf.getTimepointDirect(midT, buff1);
        buff2 = rps.getTimepoint(midT);
        Assert.assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testOtherGetters() {
        Assert.assertEquals(bf.getPath(), destFileName);
    }

    private void testDimensionGetters() throws ServerError {
        Assert.assertEquals(rps.getRowSize() / rps.getByteWidth(), bf.getSizeX());
        Assert.assertEquals(rps.getPlaneSize() / rps.getRowSize(), bf.getSizeY());
        Assert.assertEquals(rps.getStackSize() / rps.getPlaneSize(), bf.getSizeZ());
        Assert.assertEquals(rps.getTimepointSize() / rps.getStackSize(), bf.getSizeC());
        Assert.assertEquals(rps.getTotalSize() / rps.getTimepointSize(), bf.getSizeT());
    }

    private void testSizeGetters() throws ServerError {
        Assert.assertEquals(rps.getRowSize(), bf.getRowSize().intValue());
        Assert.assertEquals(
                rps.getPlaneSize() * rps.getByteWidth() / rps.getRowSize(), bf
                        .getColSize().intValue());
        Assert.assertEquals(rps.getPlaneSize(), bf.getPlaneSize().longValue());
        Assert.assertEquals(rps.getStackSize(), bf.getStackSize().longValue());
        Assert.assertEquals(rps.getTimepointSize(), bf.getTimepointSize().longValue());
        Assert.assertEquals(rps.getTotalSize(), bf.getTotalSize().longValue());
    }

    private void testOffsetGettersZero() throws DimensionsOutOfBoundsException {
        Assert.assertEquals(0, bf.getRowOffset(0, 0, 0, 0).longValue());
        Assert.assertEquals(0, bf.getPlaneOffset(0, 0, 0).longValue());
        Assert.assertEquals(0, bf.getStackOffset(0, 0).longValue());
        Assert.assertEquals(0, bf.getTimepointOffset(0).longValue());
    }

    private void testOffsetGetters() throws DimensionsOutOfBoundsException,
            ServerError {
        int midY = bf.getSizeY() / 2;
        int midZ = bf.getSizeZ() / 2;
        int midC = bf.getSizeC() / 2;
        int midT = bf.getSizeT() / 2;
        Assert.assertEquals((long) rps.getRowOffset(midY, midZ, midC, midT),
                (long) bf.getRowOffset(midY, midZ, midC, midT));
        Assert.assertEquals((long) rps.getPlaneOffset(midZ, midC, midT),
                (long) bf.getPlaneOffset(midZ, midC, midT));
        Assert.assertEquals((long) rps.getStackOffset(midC, midT),
                (long) bf.getStackOffset(midC, midT));
        Assert.assertEquals((long) rps.getTimepointOffset(midT),
                (long) bf.getTimepointOffset(midT));
    }

    private void testCheckBounds() {
        try {
            bf.checkBounds(-1, 0, 0, 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(bf.getSizeX(), 0, 0, 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, -1, 0, 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, bf.getSizeY(), 0, 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, -1, 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, bf.getSizeZ(), 0, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, 0, -1, 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, 0, bf.getSizeC(), 0);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, 0, 0, -1);
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
        try {
            bf.checkBounds(0, 0, 0, 0, bf.getSizeT());
            Assert.fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {
        }
    }
}
