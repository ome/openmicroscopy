package ome.util.checksum;

import com.google.common.hash.HashFunction;

/**
 * Reverse the endianness of a checksum provider's hash.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public class AbstractChecksumProviderReverseEndian extends
        AbstractChecksumProvider {

    protected AbstractChecksumProviderReverseEndian(HashFunction hashFunction) {
        super(hashFunction);
    }

    @Override
    public byte[] checksumAsBytes() {
        final byte[] forward = super.checksumAsBytes();
        final byte[] backward = new byte[forward.length];
        int b = backward.length;
        for (int f = 0; f < forward.length; f++) {
            backward[--b] = forward[f];
        }
        return backward;
    }

    @Override
    public String checksumAsString() {
        final String checksum = super.checksumAsString();
        final int checksumLength = checksum.length();
        final StringBuffer sb = new StringBuffer(checksumLength);
        for (int i = 0; i < checksumLength; i += 2) {
            sb.insert(0, checksum.charAt(i));
            sb.insert(1, checksum.charAt(i + 1));
        }
        return sb.toString();
    }
}
