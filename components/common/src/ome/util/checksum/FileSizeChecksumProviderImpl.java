package ome.util.checksum;

import java.io.File;
import java.nio.ByteBuffer;

import com.google.common.hash.HashCode;

/**
 * Trivial checksum provider whose hash is simply the number of bytes that were put into the calculation.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public class FileSizeChecksumProviderImpl implements ChecksumProvider {
    private long size = 0;
    private boolean isChecksumCalculated = false;

    @Override
    public ChecksumProvider putBytes(byte[] byteArray) {
        verifyState();

        size += byteArray.length;
        return this;
    }

    @Override
    public ChecksumProvider putBytes(byte[] byteArray, int offset, int length) {
        verifyState();

        /* provide bounds exception specified by method documentation */
        @SuppressWarnings("unused")
        final byte start = byteArray[offset];
        @SuppressWarnings("unused")
        final byte end = byteArray[offset + length - 1];

        size += length;
        return this;
    }

    @Override
    public ChecksumProvider putBytes(ByteBuffer byteBuffer) {
        verifyState();

        if (!byteBuffer.hasArray()) {
            throw new IllegalArgumentException("Supplied ByteBuffer has " +
                    "inaccessible array.");
        }

        size += byteBuffer.limit() - byteBuffer.position();
        return this;
    }

    @Override
    public ChecksumProvider putFile(String filePath) {
        verifyState();

        size = new File(filePath).length();
        return this;
    }

    @Override
    public byte[] checksumAsBytes() {
        isChecksumCalculated = true;
        /* use Guava hash code to match the other algorithms */
        return HashCode.fromLong(size).asBytes();
    }

    @Override
    public String checksumAsString() {
        isChecksumCalculated = true;
        return HashCode.fromLong(size).toString();
    }

    private void verifyState() {
        if (isChecksumCalculated) {
            throw new IllegalStateException("Checksum state already set. " +
                    "Mutation illegal.");
        }
    }
}
