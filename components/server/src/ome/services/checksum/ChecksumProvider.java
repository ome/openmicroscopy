package ome.services.checksum;

import java.nio.ByteBuffer;

public interface ChecksumProvider {

    /**
     * String bufferToSha1(byte[] buffer); will be provided by the use of
     * Utils.bytesToHex(provideChecksum(buffer));
     */

    byte[] provideChecksum(byte[] buffer);

    byte[] provideChecksum(String fileName);

    byte[] provideChecksum(ByteBuffer buffer);

}
