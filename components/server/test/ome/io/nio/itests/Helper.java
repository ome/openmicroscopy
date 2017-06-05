/*
 * ome.io.nio.itests.Helper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author callan
 * 
 */
public class Helper {
    private static MessageDigest newSha1MessageDigest() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

        md.reset();
        return md;
    }

    public static byte[] calculateMessageDigest(ByteBuffer buffer) {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
    }

    public static byte[] calculateMessageDigest(byte[] buffer) {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
    }
}
