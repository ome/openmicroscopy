package ome.services.blitz.util;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import ome.util.checksum.ChecksumType;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.enums.ChecksumAlgorithmAdler32;
import omero.model.enums.ChecksumAlgorithmCRC32;
import omero.model.enums.ChecksumAlgorithmMD5128;
import omero.model.enums.ChecksumAlgorithmMurmur3128;
import omero.model.enums.ChecksumAlgorithmMurmur332;
import omero.model.enums.ChecksumAlgorithmSHA1160;

/**
 * Work with {@link ChecksumAlgorithm} enumeration instances,
 * including mapping their values back to {@link ChecksumType}s.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class ChecksumAlgorithmMapper {
    private static final ImmutableMap<String, ChecksumType> checksumAlgorithms =
            ImmutableMap.<String, ChecksumType> builder().
            put(ChecksumAlgorithmAdler32.value, ChecksumType.ADLER32).
            put(ChecksumAlgorithmCRC32.value, ChecksumType.CRC32).
            put(ChecksumAlgorithmMD5128.value, ChecksumType.MD5).
            put(ChecksumAlgorithmMurmur332.value, ChecksumType.MURMUR32).
            put(ChecksumAlgorithmMurmur3128.value, ChecksumType.MURMUR128).
            put(ChecksumAlgorithmSHA1160.value, ChecksumType.SHA1).
            build();

    private static ChecksumAlgorithm getChecksumAlgorithmWithValue(String name) {
        final ChecksumAlgorithm algorithm = new ChecksumAlgorithmI();
        algorithm.setValue(rstring(name));
        return algorithm;
    }

    public static ChecksumType getChecksumType(ome.model.enums.ChecksumAlgorithm algorithm) {
        return checksumAlgorithms.get(algorithm.getValue());
    }

    public static ChecksumType getChecksumType(ChecksumAlgorithm algorithm) {
        return checksumAlgorithms.get(algorithm.getValue().getValue());
    }

    public static ChecksumAlgorithm getChecksumAlgorithm(String name) {
        if (!checksumAlgorithms.containsKey(name)) {
            throw new IllegalArgumentException(name + " is not recognized as a value of the enumeration " +
                    ome.model.enums.ChecksumAlgorithm.class.getCanonicalName());
        }
        return getChecksumAlgorithmWithValue(name);
    }

    public static List<ChecksumAlgorithm> getAllChecksumAlgorithms() {
        final List<ChecksumAlgorithm> algorithms = new ArrayList<ChecksumAlgorithm>(checksumAlgorithms.size());
        for (final String name : checksumAlgorithms.keySet()) {
            algorithms.add(getChecksumAlgorithmWithValue(name));
        }
        return algorithms;
    }
}
