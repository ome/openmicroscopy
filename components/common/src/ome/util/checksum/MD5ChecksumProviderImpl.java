package ome.util.checksum;

import java.nio.ByteBuffer;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class MD5ChecksumProviderImpl implements ChecksumProvider {

    private final HashFunction md5 = Hashing.md5();

    public byte[] getChecksum(byte[] rawData) {
        return this.md5.newHasher().putBytes(rawData).hash().asBytes();
    }

    public byte[] getChecksum(ByteBuffer byteBuffer) {
        byte[] result = null;
        if (byteBuffer.hasArray()) {
            result = this.md5.newHasher().putBytes(byteBuffer.array()).hash().asBytes();
        }
        return result;
    }

    public byte[] getChecksum(String filePath) {
        throw new UnsupportedOperationException("provideChecksum() not"
                + "implemented for file path String.");
    }

}
