package ome.util.checksum;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ome.util.Utils;

public class SHA1ChecksumProviderImpl implements ChecksumProvider {

    private final HashFunction sha1 = Hashing.sha1();

    private static final int BYTEARRAYSIZE = 8192;

    public byte[] getChecksum(byte[] rawData) {
        return this.sha1.newHasher().putBytes(rawData).hash().asBytes();
    }

    public byte[] getChecksum(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException("provideChecksum() not"
                + "implemented for ByteBuffer.");
    }

    public byte[] getChecksum(String filePath) {
        FileInputStream fis = null;
        FileChannel fch = null;
        Hasher sha1Hasher = this.sha1.newHasher();
        try {
            fis = new FileInputStream(filePath);
            fch = fis.getChannel();
            MappedByteBuffer mbb = fch.map(FileChannel.MapMode.READ_ONLY, 0L, fch.size());
            byte[] byteArray = new byte[BYTEARRAYSIZE];
            int byteCount;
            while (mbb.hasRemaining()) {
                byteCount = Math.min(mbb.remaining(), BYTEARRAYSIZE);
                if (byteCount < BYTEARRAYSIZE) {
                    // This might be sub-optimal
                    byteArray = new byte[byteCount];
                }
                mbb.get(byteArray, 0, byteCount);
                sha1Hasher.putBytes(byteArray);
            }
            return sha1Hasher.hash().asBytes();
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            Utils.closeQuietly(fis);
            Utils.closeQuietly(fch);
        }
    }

}
