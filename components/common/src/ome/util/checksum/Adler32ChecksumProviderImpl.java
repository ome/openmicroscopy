package ome.util.checksum;

import java.nio.charset.Charset;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

public class Adler32ChecksumProviderImpl extends AbstractChecksumProvider {

    public Adler32ChecksumProviderImpl() {
        super(new HashFunction() {

            private Adler32Hasher hasher = new Adler32Hasher();

            public int bits() {
                return 1;
            }

            public HashCode hashBytes(byte[] input) {
                return this.hasher.putBytes(input).hash();
            }

            public HashCode hashBytes(byte[] input, int off, int len) {
                return this.hasher.putBytes(input, off, len).hash();
            }

            public HashCode hashInt(int input) {
                return this.hasher.putInt(input).hash();
            }

            public HashCode hashLong(long input) {
                return this.hasher.putLong(input).hash();
            }

            public HashCode hashString(CharSequence input) {
                return this.hasher.putString(input).hash();
            }

            public HashCode hashString(CharSequence input, Charset charset) {
                return this.hasher.putString(input, charset).hash();
            }

            public Hasher newHasher() {
                return this.hasher = new Adler32Hasher();
            }

            public Hasher newHasher(int expectedInputSize) {
                return this.newHasher();
            }

        });
    }

}
