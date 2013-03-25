/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

// Java imports
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import ome.model.IObject;
import ome.model.internal.Permissions;

// Third-party libraries

// Application-internal dependencies

/**
 * various tools needed throughout Omero.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 * @DEV.TODO Grinder issues should be moved to test component to reduce deps.
 */
public class Utils {

    private final static Logger log = Logger.getLogger(Utils.class.getName());

    protected final static String CGLIB_IDENTIFIER = "$$EnhancerByCGLIB$$";

    protected final static String JAVASSIST_IDENTIFIER = "_$$_javassist";

    /**
     * finds the "true" class identified by a given Class object. This is
     * necessary because of possibly proxied instances.
     * 
     * @param source
     *            Regular or CGLIB-based class.
     * @return the regular Java class.
     */
    public static <T extends IObject> Class<T> trueClass(Class<T> source) {
        String s = source.getName();
        if (s.contains(CGLIB_IDENTIFIER)) { // TODO any other test?
            try {
                return (Class<T>) Class.forName(s.substring(0, s
                        .indexOf(CGLIB_IDENTIFIER)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException( /* TODO */
                "Classname contains " + CGLIB_IDENTIFIER
                        + " but base class cannout be found.");
            }
        } else if (s.contains(JAVASSIST_IDENTIFIER)) {
            try {
                return (Class<T>) Class.forName(s.substring(0, s
                        .indexOf(JAVASSIST_IDENTIFIER)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException( /* TODO */
                "Classname contains " + JAVASSIST_IDENTIFIER
                        + " but base class cannout be found.");
            }
        }
        return source;
    }

    static String msg = "Failed to instantiate %s. This may be caused by an "
            + "abstract class not being properly \"join fetch\"'d. Please review "
            + "your query or contact your server administrator.";

    /**
     * instantiates an object using the trueClass.
     * 
     * @param source
     *            Regular or CGLIB-based class.
     * @return the regular Java instance.
     */
    public static Object trueInstance(Class source) {
        Class trueClass = trueClass(source);
        Object result;
        try {
            result = trueClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(String.format(msg, trueClass), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not allowed to create class:"
                    + trueClass, e);
        }
        return result;
    }

    /**
     * primarily used in Grinder to discover what methods to call
     * 
     * @param clazz
     * @return
     */
    public static String[] getObjectVoidMethods(Class clazz) {
        Set set = new HashSet();

        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getReturnType().equals(Object.class)) {
                if (method.getParameterTypes().length == 0) {
                    set.add(method.getName());
                }
            }

        }

        return (String[]) set.toArray(new String[set.size()]);
    }

    /**
     * Returns the internal representation of a {@link Permissions} object.
     * Should be used with caution!
     */
    public static Object internalForm(Permissions p) {
        P pp = new P(p);
        return Long.valueOf(pp.toLong());
    }

    /**
     * Returns a {@link Permissions} instance from its internal representation.
     * Should be used with caution!
     */
    public static Permissions toPermissions(Object o) {
        P pp = new P((Long) o);
        return new Permissions(pp);
    }

    private static class P extends Permissions {
        private static final long serialVersionUID = -18133057809465999L;

        protected P(Permissions p) {
            revokeAll(p);
            grantAll(p);
        }

        protected P(Long l) {
            this.setPerm1(l.longValue());
        }

        long toLong() {
            return super.getPerm1();
        }
    }

    /**
     * Returns a {@link String} which can be used to correlate log messages.
     */
    public static String getThreadIdentifier() {
        return new StringBuilder(32).append(Runtime.getRuntime().hashCode())
                .append("::").append(Thread.currentThread().getId()).toString();
    }

    /**
     * Standard algorithm to convert a byte-array into a SHA1. Throws a
     * {@link RuntimeException} if {@link MessageDigest#getInstance(String)}
     * throws {@link NoSuchAlgorithmException}.
     * @deprecated As of 4.4.7,
     *             superseded by {@link ChecksumProvider#putBytes(byte[])}
     */
    @Deprecated
    public static String bufferToSha1(byte[] buffer) {
        MessageDigest md;

        md = newSha1MessageDigest();

        md.reset();
        md.update(buffer);
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    /**
     * Reads a file from disk and returns the SHA1 digest for it. An IOException
     * is thrown if anything occurs during reading.
     * @deprecated As of 4.4.7,
     *             superseded by {@link ChecksumProvider#putBytes(String)}
     */
    @Deprecated
    public static byte[] pathToSha1(String fileName) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DigestInputStream dis = null;
        try {
            MessageDigest sha1 = newSha1MessageDigest();
            fis = new FileInputStream(fileName);
            bis = new BufferedInputStream(fis);
            dis = new DigestInputStream(bis,sha1);
            while (dis.read() != -1);
            return sha1.digest();
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            closeQuietly(dis);
            closeQuietly(bis);
            closeQuietly(fis);
        }
    }
    /**
     * Calculates a MD5 digest for the given {@link ByteBuffer}
     * @deprecated As of 4.4.7,
     *             superseded by {@link ChecksumProvider#putBytes(ByteBuffer)}
     */
    @Deprecated
    public static byte[] calculateMessageDigest(ByteBuffer buffer) {
        MessageDigest md = newMd5MessageDigest();
        md.update(buffer);
        return md.digest();
    }

    /**
     * Calculates a MD5 digest for the given {@link byte[]}
     * @deprecated As of 4.4.7,
     *             superseded by {@link ChecksumProvider#putBytes(byte[])}
     */
    @Deprecated
    public static byte[] calculateMessageDigest(byte[] buffer) {
        MessageDigest md = newMd5MessageDigest();
        md.update(buffer);
        return md.digest();
    }

    /**
     * Standard algorithm to convert a byte array to a hex string.
     * 
     * @param data
     *            the byte[] to convert
     * @return String the converted byte[]
     * @deprecated As of 4.4.7,
     *             superseded by the use of <code>commons.codec.binary.Hex</code>
     */
    @Deprecated
    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
        }
        return buf.toString();
    }

    /**
     * Standard algorithm to convert a byte into a hex representation.
     * @deprecated As of 4.4.7,
     *             superseded by the use of <code>commons.codec.binary.Hex</code>
     */
    @Deprecated
    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar(data >>> 4 & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Standard algorithm to convert an int into a hex char.
     * @deprecated As of 4.4.7,
     *             superseded by the use of <code>commons.codec.binary.Hex</code>
     */
    @Deprecated
    public static char toHexChar(int i) {
        if (0 <= i && i <= 9) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + i - 10);
        }
    }

    // Helpers
    // =========================================================================

    public static void closeQuietly(Closeable is) {
        if (is == null) {
            log.fine("Closeable is null");
        } else {
            try {
                is.close();
            } catch (Exception e) {
                log.info("Exception on closing closeable " + is + ":" + e);
            }
        }
    }

    @Deprecated
    private static MessageDigest newMd5MessageDigest() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required MD5 message digest algorithm unavailable.");
        }

        md.reset();
        return md;
    }


    @Deprecated
    private static MessageDigest newSha1MessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }

    }
}