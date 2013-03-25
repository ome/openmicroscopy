/*
 * ome.io.nio.itests.PlaneIOUnitTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.util.PixelData;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;

import org.apache.commons.codec.binary.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author callan
 * 
 */
public class PlaneReadUnitTest extends AbstractManagedContextTest {
    private Integer planeCount;

    private Integer planeSize;

    private byte[][] originalDigests;

    private String path;

    private Pixels pixels;

    private PixbufIOFixture baseFixture;

    private String ROOT;

    private int getDigestOffset(int z, int c, int t) {
        int planeCountT = pixels.getSizeZ().intValue()
                * pixels.getSizeC().intValue();

        return planeCountT * t + pixels.getSizeZ() * c + z;
    }

    private String getPlaneCheckErrStr(int z, int c, int t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error with plane: ");
        sb.append("Z[");
        sb.append(z);
        sb.append("] ");
        sb.append("C[");
        sb.append(c);
        sb.append("] ");
        sb.append("T[");
        sb.append(t);
        sb.append("].");
        return sb.toString();
    }

    private byte[] createPlane(int planeSize, byte planeNo) {
        byte[] plane = new byte[planeSize];

        for (int i = 0; i < planeSize; i++) {
            plane[i] = planeNo;
        }

        return plane;
    }

    private void createPlanes() throws IOException {
        int byteWidth = PixelData.getBitDepth(pixels.getPixelsType().getValue()) / 8;
        planeCount = pixels.getSizeZ() * pixels.getSizeC() * pixels.getSizeT();
        planeSize = pixels.getSizeX() * pixels.getSizeY() * byteWidth;
        path = new PixelsService(ROOT).getPixelsPath(pixels.getId());
        originalDigests = new byte[planeCount][];

        FileOutputStream stream = new FileOutputStream(path);

        for (int i = 0; i < planeCount; i++) {
            byte[] plane = createPlane(planeSize.intValue(), (byte) (i - 128));
            originalDigests[i] = Helper.calculateMessageDigest(plane);
            stream.write(plane);
        }
    }

    @BeforeClass
    protected void setup() throws Exception {

        ROOT = getOmeroDataDir();

        // Create set up the base fixture which sets up the database for us
        baseFixture = new PixbufIOFixture(this.iContainer, this.iUpdate);
        pixels = baseFixture.setUp();

        // "Our" fixture which creates the planes needed for this test case.
        createPlanes();
    }

    @Test
    public void testInitialPlane() throws IOException,
            DimensionsOutOfBoundsException {
        PixelsService service = new PixelsService(ROOT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        PixelData plane = pixbuf.getPlane(0, 0, 0);

        byte[] messageDigest = Helper.calculateMessageDigest(plane.getData());

        assertEquals(Hex.encodeHexString(originalDigests[0]), Hex
                .encodeHexString(messageDigest));
    }

    @Test
    public void testLastPlane() throws IOException,
            DimensionsOutOfBoundsException {
        PixelsService service = new PixelsService(ROOT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        PixelData plane = pixbuf.getPlane(pixels.getSizeZ() - 1, pixels
                .getSizeC() - 1, pixels.getSizeT() - 1);
        int digestOffset = getDigestOffset(pixels.getSizeZ() - 1, pixels
                .getSizeC() - 1, pixels.getSizeT() - 1);

        byte[] messageDigest = Helper.calculateMessageDigest(plane.getData());

        assertEquals(Hex.encodeHexString(originalDigests[digestOffset]), Hex
                .encodeHexString(messageDigest));
    }

    @Test
    public void testAllPlanes() throws IOException,
            DimensionsOutOfBoundsException {
        PixelsService service = new PixelsService(ROOT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);

        String newMessageDigest;
        String oldMessageDigest;
        int digestOffset;
        for (int t = 0; t < pixels.getSizeT(); t++) {
            for (int c = 0; c < pixels.getSizeC(); c++) {
                for (int z = 0; z < pixels.getSizeZ(); z++) {
                    digestOffset = getDigestOffset(z, c, t);
                    PixelData plane = pixbuf.getPlane(z, c, t);
                    newMessageDigest = Hex.encodeHexString(Helper
                            .calculateMessageDigest(plane.getData()));
                    oldMessageDigest = Hex
                            .encodeHexString(originalDigests[digestOffset]);

                    assertEquals(getPlaneCheckErrStr(z, c, t),
                            oldMessageDigest, newMessageDigest);
                }
            }
        }
    }

    @AfterClass
    protected void tearDown() throws Exception {
        // Tear down the resources created as part of the base fixture
        baseFixture.tearDown();

        // Tear down the resources create in this fixture
        File f = new File(path);
        f.delete();
    }
}
