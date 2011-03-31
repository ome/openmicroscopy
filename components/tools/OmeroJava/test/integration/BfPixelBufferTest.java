/*
 *
 */

package integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.OriginalFileMetadataProvider;
import ome.io.nio.PixelData;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.RomioPixelBuffer;
import ome.services.blitz.repo.BfPixelBuffer;
import ome.services.OmeroOriginalFileMetadataProvider;
import omero.api.IPixelsPrx;
import omero.api.IQuery;
import omero.api.RawPixelsStorePrx;
import omero.model.Pixels;
import omero.ServerError;
import omero.util.IceMapper;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

@Test(groups = {"repo", "integration"})
public class BfPixelBufferTest extends AbstractTest {

    private BfPixelBuffer bf;
    private PixelBuffer pb;
    private RawPixelsStorePrx rps;
    private File destPath;
    private String destFileName;
    
    private void setUpTestFile(String fileName) throws Throwable, NoSuchAlgorithmException {
        File srcFile = ResourceUtils.getFile("classpath:" + fileName); 

        // This absolute reference needs to be fixed!
        String dataDirName = "/OMERO"
        String destPathName = UUID.randomUUID().toString();
        File dataDir = new File(dataDirName);
        destPath = new File(dataDir, destPathName);
        File destFile = new File(destPath, fileName);
        destPath.mkdir();

        // Copy file into repo
        FileUtils.copyFile(srcFile, destFile);
        // Import file
        List<Pixels> pixList = importFile(srcFile, fileName);
        log.debug(String.format("Imported: %s, pixid: %d",srcFile,pixList.get(0).getId().getValue()));

        // Access the imported pixels via a RawPixelsStore
        rps = factory.createRawPixelsStore();
        rps.setPixelsId(pixList.get(0).getId().getValue(), false);

        // Access the data from file via BfPixelBuffer
        destFileName = destFile.getCanonicalPath();
        bf = new BfPixelBuffer(destFileName);
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

    private void testgetColDirect() throws IOException, DimensionsOutOfBoundsException, ServerError{
        int midX = bf.getSizeX()/2;
        int midZ = bf.getSizeZ()/2;
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        byte[] buff1 = new byte[bf.getColSize()];
        byte[] buff2 = new byte[bf.getColSize()]; //rps has no getColSize
        bf.getColDirect(midX,midZ,midC,midT,buff1);
        buff2 = rps.getCol(midX,midZ,midC,midT);
        assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetRowDirect() throws IOException, DimensionsOutOfBoundsException, ServerError{
        int midY = bf.getSizeY()/2;
        int midZ = bf.getSizeZ()/2;
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        byte[] buff1 = new byte[bf.getRowSize()];
        byte[] buff2 = new byte[rps.getRowSize()];
        bf.getRowDirect(midY,midZ,midC,midT,buff1);
        buff2 = rps.getRow(midY,midZ,midC,midT);
        assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetPlaneDirect() throws IOException, DimensionsOutOfBoundsException, ServerError {
        int midZ = bf.getSizeZ()/2;
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        byte[] buff1 = new byte[bf.getPlaneSize()];
        byte[] buff2 = new byte[rps.getPlaneSize()];
        bf.getPlaneDirect(midZ,midC,midT,buff1);
        buff2 = rps.getPlane(midZ,midC,midT);
        assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetPlaneAsHypercube() throws IOException, DimensionsOutOfBoundsException, ServerError{
        int sizeX = bf.getSizeX();
        int sizeY = bf.getSizeY();
        int midZ = bf.getSizeZ()/2;
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        List<Integer> offset = Arrays.asList(new Integer[]{0,0,midZ,midC,midT});
        List<Integer> size = Arrays.asList(new Integer[]{sizeX,sizeY,1,1,1});
        List<Integer> step = Arrays.asList(new Integer[]{1,1,1,1,1});
        byte[] buff1 = new byte[bf.getPlaneSize()];
        byte[] buff2 = new byte[bf.getPlaneSize()];
        bf.getPlaneDirect(midZ,midC,midT,buff1);
        bf.getHypercubeDirect(offset,size,step,buff2);
        assertEquals(sha1(buff1), sha1(buff2));
    }


    private void testgetStackDirect() throws IOException, DimensionsOutOfBoundsException, ServerError {
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        byte[] buff1 = new byte[bf.getStackSize()];
        byte[] buff2 = new byte[rps.getStackSize()];
        bf.getStackDirect(midC,midT,buff1);
        buff2 = rps.getStack(midC,midT);
        assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testgetTimepointDirect() 
            throws IOException, DimensionsOutOfBoundsException, ServerError {
        int midT = bf.getSizeT()/2;
        byte[] buff1 = new byte[bf.getTimepointSize()];
        byte[] buff2 = new byte[rps.getTimepointSize()];
        bf.getTimepointDirect(midT,buff1);
        buff2 = rps.getTimepoint(midT);
        assertEquals(sha1(buff1), sha1(buff2));
    }

    private void testMessageDigest() throws IOException, ServerError {
        assertEquals(bf.calculateMessageDigest(), rps.calculateMessageDigest());
    }
    
    private void testOtherGetters() {
        assertEquals(bf.getPath(), destFileName);
    }

    private void testDimensionGetters() throws ServerError {
        assertEquals(rps.getRowSize()/rps.getByteWidth(), bf.getSizeX());
        assertEquals(rps.getPlaneSize()/rps.getRowSize(), bf.getSizeY());
        assertEquals(rps.getStackSize()/rps.getPlaneSize(), bf.getSizeZ());
        assertEquals(rps.getTimepointSize()/rps.getStackSize(), bf.getSizeC());
        assertEquals(rps.getTotalSize()/rps.getTimepointSize(), bf.getSizeT());
    }
    
    private void testSizeGetters() throws ServerError {
        assertEquals((int)rps.getRowSize(), (int)bf.getRowSize());
        assertEquals((int)rps.getPlaneSize()*rps.getByteWidth()/rps.getRowSize(), (int)bf.getColSize());
        assertEquals((int)rps.getPlaneSize(), (int)bf.getPlaneSize());
        assertEquals((int)rps.getStackSize(), (int)bf.getStackSize());
        assertEquals((int)rps.getTimepointSize(), (int)bf.getTimepointSize());
        assertEquals((int)rps.getTotalSize(), (int)bf.getTotalSize());
    }

    private void testOffsetGettersZero() throws DimensionsOutOfBoundsException {
        assertTrue(bf.getRowOffset(0,0,0,0) == 0);
        assertTrue(bf.getPlaneOffset(0,0,0) == 0);
        assertTrue(bf.getStackOffset(0,0) == 0);
        assertTrue(bf.getTimepointOffset(0) == 0);
    }

    private void testOffsetGetters() throws DimensionsOutOfBoundsException, ServerError {
        int midY = bf.getSizeY()/2;
        int midZ = bf.getSizeZ()/2;
        int midC = bf.getSizeC()/2;
        int midT = bf.getSizeT()/2;
        assertEquals((long)rps.getRowOffset(midY,midZ,midC,midT), (long)bf.getRowOffset(midY,midZ,midC,midT));
        assertEquals((long)rps.getPlaneOffset(midZ,midC,midT), (long)bf.getPlaneOffset(midZ,midC,midT));
        assertEquals((long)rps.getStackOffset(midC,midT), (long)bf.getStackOffset(midC,midT));
        assertEquals((long)rps.getTimepointOffset(midT), (long)bf.getTimepointOffset(midT));
    }
    
    private void testCheckBounds() {
        try {
            bf.checkBounds(-1,0,0,0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(bf.getSizeX(),0,0,0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,-1,0,0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,bf.getSizeY(),0,0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,-1,0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,bf.getSizeZ(),0,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,0,-1,0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,0,bf.getSizeC(),0);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,0,0,-1);
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
        try {
            bf.checkBounds(0,0,0,0,bf.getSizeT());
            fail("Failed to throw DimensionsOutOfBoundsException with dimension out of bounds.");
        } catch (DimensionsOutOfBoundsException e) {}
    }
}
