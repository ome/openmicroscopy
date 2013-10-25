/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import java.lang.reflect.Method;

/**
 * Methods for working with Ice.
 */
public class IceUtil {

    private static final Object ICE_ENCODING;

    private static final Method ICE_OUTPUT_STREAM_METHOD;

    private static final Method ICE_INPUT_STREAM_METHOD;

    static {
        Class<Ice.Communicator> ic = Ice.Communicator.class;
        Class<byte[]> b = byte[].class;
        Method os = null;
        Method is = null;
        Object enc = null;
        try {
            try {
                Class<?> ev = Class.forName("Ice.EncodingVersion");
                os = Ice.Util.class.getMethod("createOutputStream", ic, ev);
                is = Ice.Util.class.getMethod("createInputStream", ic, b, ev);
                enc = Ice.Util.class.getField("Encoding_1_0").get(null);
            } catch (ClassNotFoundException e) {
                // Then this is pre Ice3.5
                os = Ice.Util.class.getMethod("createOutputStream", ic);
                is = Ice.Util.class.getMethod("createInputStream", ic, b);
            }
        } catch (Exception e) {
            // This shouldn't be able to happen unless there's been
            // a breaking change in Ice.
            throw new RuntimeException("Cannot configure Ice", e);
        }
        ICE_ENCODING = enc;
        ICE_OUTPUT_STREAM_METHOD = os;
        ICE_INPUT_STREAM_METHOD = is;
    }

    /**
     * Creates an {@link Ice.OutputStream} with the appropriate encoding.
     * This should only be used parsing objects/streams between Ice versions,
     * for example when the data is or will be stored in the database.
     *
     * See ticket:11322
     */
    public static Ice.OutputStream createSafeOutputStream(Ice.Communicator ic) {
        Object[] args;
        if (ICE_ENCODING != null) {
            args = new Object[]{ic, ICE_ENCODING};
        } else {
            args = new Object[]{ic};
        }

        try {
            return (Ice.OutputStream) ICE_OUTPUT_STREAM_METHOD.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("ICE_INPUT_STREAM_METHOD failed", e);
        }
    }

    /**
     * Creates an {@link Ice.InputStream} with the appropriate encoding.
     * This should only be used parsing objects/streams between Ice versions,
     * for example when the data is or will be stored in the database.
     *
     * See ticket:11322
     */
    public static Ice.InputStream createSafeInputStream(Ice.Communicator ic, byte[] data) {
        Object[] args;
        if (ICE_ENCODING != null) {
            args = new Object[]{ic, data, ICE_ENCODING};
        } else {
            args = new Object[]{ic, data};
        }
        try {
            return (Ice.InputStream) ICE_INPUT_STREAM_METHOD.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("ICE_INPUT_STREAM_METHOD failed", e);
        }
    }
}
