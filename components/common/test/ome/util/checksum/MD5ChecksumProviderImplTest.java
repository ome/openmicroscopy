package ome.util.checksum;

import java.nio.ByteBuffer;

import ome.util.Utils;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MD5ChecksumProviderImplTest {

    private MD5ChecksumProviderImpl md5;

    // Using MD5('abc') as test vector value
    private static String TESTVECTOR = "900150983cd24fb0d6963f7d28e17f72";

    @BeforeClass
    protected void setUp() throws Exception {
        this.md5 = new MD5ChecksumProviderImpl();
    }

    @Test
    public void testGetChecksumWithByteArray() {
        String actual = Utils.bytesToHex(this.md5.getChecksum("abc".getBytes()));
        Assert.assertEquals(actual, TESTVECTOR);
    }

    @Test
    public void testGetChecksumWithByteBuffer() {
        String actual = Utils.bytesToHex(this.md5.getChecksum(
                ByteBuffer.wrap("abc".getBytes())));
        Assert.assertEquals(actual, TESTVECTOR);
    }

    @Test
    public void testGetChecksumWithEmptyByteBufferReturnsNull() {
        byte[] actual = this.md5.getChecksum(ByteBuffer.allocateDirect(0));
        Assert.assertNull(actual);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetChecksumWithFilePathStringShouldThrowUOE() {
        this.md5.getChecksum("foobar/biz/buz");
    }

}
