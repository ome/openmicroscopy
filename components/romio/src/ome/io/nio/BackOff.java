/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import ome.model.core.Pixels;

/**
 * Strategy interface which is used by {@link PixelsService} to calculate the
 * appropriate backoff to report to users when pixel data is not available.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3.1
 */
public interface BackOff {

    void throwMissingPyramidException(String msg, Pixels pixels);

}
