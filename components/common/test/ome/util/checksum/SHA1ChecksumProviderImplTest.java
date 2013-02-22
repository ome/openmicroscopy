package ome.util.checksum;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import ome.util.Utils;

import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SHA1ChecksumProviderImplTest {

    private SHA1ChecksumProviderImpl sha1;

    // Using SHA1('abc') as test vector value
    private static String TESTVECTOR = "a9993e364706816aba3e25717850c26c9cd0d89d";

    private static String SMALLFILESHA1 = "89336a1baf365cf51b67105019beca71b858c227";

    private static String BIGFILESHA1 = "c5362b32b0fbacb6ec4be7bc40b647405a8f73ce";

    @BeforeClass
    protected void setUp() throws Exception {
        this.sha1 = new SHA1ChecksumProviderImpl();
    }

    @Test
    public void testGetChecksumWithByteArray() {
        String actual = Utils.bytesToHex(this.sha1.getChecksum("abc".getBytes()));
        Assert.assertEquals(actual, TESTVECTOR);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetChecksumWithByteBuffer() {
        this.sha1.getChecksum(ByteBuffer.allocateDirect(0));
    }

    @Test
    public void testGetChecksumWithSmallFilePathString() {
        File smallFile = null;

        try {
            smallFile = ResourceUtils.getFile("classpath:cruisecontrol-test.txt");
        } catch (FileNotFoundException e) {
            Assert.fail("Test data cannot be accessed. Failing the test.");
        }

        String actual = Utils.bytesToHex(this.sha1
                .getChecksum(smallFile.getAbsolutePath()));

        Assert.assertEquals(actual, SMALLFILESHA1);
    }

    @Test
    public void testGetChecksumWithBigFilePathString() {
        File bigFile = null;

        try {
            bigFile = ResourceUtils.getFile("classpath:test.bmp");
        } catch (FileNotFoundException e) {
            Assert.fail("Test data cannot be accessed. Failing the test.");
        }

        String actual = Utils.bytesToHex(this.sha1
                .getChecksum(bigFile.getAbsolutePath()));

        Assert.assertEquals(actual, BIGFILESHA1);
    }
    
}
