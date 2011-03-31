package ome.io.nio.utests;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ome.conditions.ResourceError;
import ome.io.nio.DeltaVision;
import ome.util.PixelData;
import ome.model.core.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests of the ome.io.nio.DeltaVision PixelBuffer implementation
 * 
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt <p/>
 * 
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 */
public class DeltaVisionTest {

    private OriginalFile originalFile;

    private DeltaVision deltaVision;

    private static int setupCount = 0;

    private static Log log = LogFactory.getLog(DeltaVisionTest.class);

    public final static String FILENAME = "tinyTest.d3d.dv";

    @BeforeMethod
    public void setUp() {

        setupCount++;

        // run this once only
        if (setupCount == 1) {

            // create an OriginalFile object and DeltaVision for testing
            originalFile = new OriginalFile();
            // URL url = getClass().getResource("tinyTest.d3d.dv");
            try {
                File file = ResourceUtils.getFile("classpath:" + FILENAME);

                // File file = new File(url.toURI());
                originalFile.setPath(file.getCanonicalPath());
                // originalFile.setPath("OMERO/tinyTest.d3d.dv");
                deltaVision = new DeltaVision(file.getCanonicalPath(),
                        originalFile);

            } catch (Exception ex) {
                log.error("Exception caught, throwing ResourceError", ex
                        .getCause());
                throw new ResourceError(ex.getMessage()
                        + " Please check server log.");
            }

            log.info("Size X = " + deltaVision.getHeader().getSizeX());
            log.info("Size Y = " + deltaVision.getHeader().getSizeY());
            log.info("Size Z = " + deltaVision.getHeader().getSizeZ());
            log.info("Size C = " + deltaVision.getHeader().getSizeC());
            log.info("Size T = " + deltaVision.getHeader().getSizeT());

            log.info(deltaVision.getPath());
            log.info("Row size = "
                    + new Integer(deltaVision.getRowSize()).intValue());
            log.info("Stack size = "
                    + new Integer(deltaVision.getStackSize()).intValue());
            log.info("Plane size = "
                    + new Integer(deltaVision.getPlaneSize()).intValue());

            log.info("Timepoint size = " + deltaVision.getTimepointSize());
            log.info("Total size = " + deltaVision.getTotalSize().toString());

            log.info("ImageSequence = (should be 0=ZTC) "
                    + deltaVision.getHeader().getSequence());

        }
    }

    @AfterMethod
    public void tearDown() {
        if (setupCount == 13) {
            originalFile = null;
            deltaVision = null;
        }
    }

    @Test
    public void testGetSizeX() throws Exception {
        int result = deltaVision.getSizeX();
        assertTrue(result == 20);
    }

    @Test
    public void testGetSizeY() throws Exception {
        int result = deltaVision.getSizeY();
        assertTrue(result == 20);
    }

    @Test
    public void testGetSizeZ() throws Exception {
        int result = deltaVision.getSizeZ();
        assertTrue(result == 5);
    }

    @Test
    public void testGetSizeC() throws Exception {
        int result = deltaVision.getSizeC();
        assertTrue(result == 1);
    }

    @Test
    public void testGetSizeT() throws Exception {
        int result = deltaVision.getSizeT();
        assertTrue(result == 6);
    }

    @Test
    public void testTotalSize() throws Exception {
        String result = deltaVision.getTotalSize().toString();
        assertTrue(result.equals("24000"));
    }

    @Test
    public void testTimepointSize() throws Exception {
        Integer result = deltaVision.getTimepointSize();
        assertTrue(result.equals(new Integer(4000)));
    }

    @Test
    public void testRowSize() throws Exception {
        Integer result = deltaVision.getRowSize();
        assertTrue(result.equals(new Integer(40)));

    }

    @Test
    public void testStackSize() throws Exception {
        Integer result = deltaVision.getStackSize();
        assertTrue(result.equals(new Integer(4000)));
    }

    @Test
    public void testPlaneSize() throws Exception {
        Integer result = deltaVision.getPlaneSize();
        assertTrue(result.equals(new Integer(800)));

    }

    @Test(groups = "broken")
    public void testGetPlaneIndex() throws Exception {
        Long result = deltaVision.getPlaneOffset(2, 0, 3); // (2,0,3) plane 17
        // ?
        // log.info("Plane offset = " + result.intValue());
        // should be 17 * planeSize=800 = 13600
        assertTrue(result.equals(new Long(13600)));
    }

    @Test
    public void testGetStackIndex() throws Exception {
        Long result = deltaVision.getStackOffset(0, 2);
        // log.info("Stack offset = " + result.intValue());
        // should be 2 * stackSize=4000 = 8000
        assertTrue(result.equals(new Long(8000)));
    }

    @Test(groups = "broken")
    public void testGetDigest() throws Exception {
        MessageDigest md;
        String[] controls = { "472b52ffbeb0f6397571c8a8482e2e37", // 0
                "36afde357e0e89905403cf1563182798", // 1
                "87b7f3e5653550d550d54377e8aa32c3", // 2
                "1fd67721b392c746a8c82d63352b3930", // 3
                "4245d518c4c3ab9cf22211df8b59b9dc", // 4
                "d71ee5397381961763729e7806ba70e6", // 5
                "2d8819f668b9bcb3e77297c110fdc2b8", // 6
                "354f6a7a057bf6e229dce28483b6b666", // 7
                "44a9db6eb3e665897715561e881947e7", // 8
                "3e62132a4145f545034313dc3ccb8406", // 9
                "e67de55f57dcd328e082b1925d828286", // 10
                "ceaaea38d478de45d32f8c7488f9c003", // 11
                "d1992745966a8f6eb4e56fc458ad3ea1", // 12
                "588678a268c0875efd1570d34e39c6ab", // 13
                "a60f2e0b40a5bf0f41a6a68f8f79bbd4", // 14
                "8fb13befd7b046766e000daf61cb3029", // 15
                "1acadc8c7bedc55388fad61bc2a5d81d", // 16
                "b5ca57b86d799876ada84cf2c36dd726", // 17
                "0dd76877705ac7e27011c7d9314981c0", // 18
                "4ff64f897a512516b643870fb416e771", // 19
                "91b9dace231080254403fdcef181ff8a", // 20
                "d7683987d3a68d6d1ddbd2a5d9aacad7", // 21
                "1ff104dba9ac95275be383f6a6c4c04e", // 22
                "ae7623339f2486efef9178b19bc97ea1", // 23
                "c162e80f350dc2dbb20fbf8f479ae1e8", // 24
                "1f4278861506438b5a9e623b0db7b61c", // 25
                "2071ba00d672e3e73b5cdb771d7234d2", // 26
                "606ec319d5126ba99636f4b573947315", // 27
                "47662680c45544f9ba080072eddb7832", // 28
                "5ba8cc329b663dfdb59d3432b799e1e6" }; // 29

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required MD5 message digest algorithm unavailable.");
        }

        int begin = deltaVision.header.getPixelBeginOffset();
        int i = 0;
        int offset = 0;

        while (i < 30) {

            offset = begin + (800 * i);

            PixelData data = deltaVision.getRegion(new Integer(800), new Long(
                    offset)); // plane 0
            ByteBuffer buffer = data.getData();

            // File file = new File("myplane" + i + ".dat");
            // FileOutputStream out = new FileOutputStream (file);
            // byte[] planebytes = ByteBuffer.allocate(800).array();
            //            
            // for (int j=0; j<planebytes.length; j++) {
            // planebytes[j] = buffer.get(j);
            // }
            //   
            // out.write(planebytes);

            md.update(buffer);
            byte[] bytes = md.digest();

            System.out.println("md5 version is " + byteArrayToHexString(bytes));
            assertTrue(byteArrayToHexString(bytes).equals(controls[i]));

            i++;
            // out.close();

        }
    }

    static String byteArrayToHexString(byte in[]) {

        byte ch = 0x00;
        int i = 0;

        if (in == null || in.length <= 0) {
            return null;
        }

        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f" };

        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {

            ch = (byte) (in[i] & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[ch]);
            ch = (byte) (in[i] & 0x0F);
            out.append(pseudo[ch]);
            i++;

        }

        String rslt = new String(out);
        return rslt;
    }
}
