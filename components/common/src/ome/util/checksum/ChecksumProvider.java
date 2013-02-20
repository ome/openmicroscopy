package ome.util.checksum;

import java.nio.ByteBuffer;

public interface ChecksumProvider {

    /**
     * String bufferToSha1(byte[] buffer); will be provided by the use of
     * Utils.bytesToHex(provideChecksum(buffer));
     */

    byte[] getChecksum(byte[] buffer);

    byte[] getChecksum(String fileName);

    byte[] getChecksum(ByteBuffer buffer);

}
